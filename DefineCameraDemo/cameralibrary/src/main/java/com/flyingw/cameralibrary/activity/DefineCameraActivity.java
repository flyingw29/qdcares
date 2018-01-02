package com.flyingw.cameralibrary.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.flyingw.cameralibrary.R;
import com.flyingw.cameralibrary.utils.BitmapUtils;
import com.flyingw.cameralibrary.utils.LogUtils;
import com.flyingw.cameralibrary.utils.ToastUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.flyingw.cameralibrary.utils.BitmapUtils.rotateBitmapByDegree;


/**
 * @author flyingw on 2018/1/2
 */

public class DefineCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Button btnCameraCancel;
    private Button btnCameraCapture;
    private Button btnCameraOk;
    private SurfaceView svCamera;

    Bitmap dataBitmap;
    private Camera camera = null;
    private String thumbnailPath = "";
    private String filename = "";
    private byte[] buffer = null;
    private final int TYPE_FILE_IMAGE = 1;
    private SurfaceHolder surfaceHolder;

    String imgInfo;
    String imgInfoThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_camera);
        initView();
        addListener();
        imgInfo = getExternalFilesDir("imgInfo") + "/";
        imgInfoThumbnail = getExternalFilesDir("imgInfoThumbnail") + "/";
    }

    /**
     * 控件初始化
     */
    private void initView() {
        btnCameraCancel = (Button) findViewById(R.id.btn_camera_cancel);
        btnCameraCapture = (Button) findViewById(R.id.btn_camera_capture);
        btnCameraOk = (Button) findViewById(R.id.btn_camera_ok);
        svCamera = (SurfaceView) findViewById(R.id.sv_camera);
    }

    /**
     * 监听事件
     */
    private void addListener() {
        btnCameraCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //相机资源未释放完时再次调用会导致系统崩溃
                //延迟500毫秒，等待资源释放
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                btnCameraCapture.setVisibility(View.VISIBLE);
                btnCameraCancel.setVisibility(View.INVISIBLE);
                btnCameraOk.setVisibility(View.INVISIBLE);
            }
        });
        btnCameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    camera.takePicture(null, null, pictureCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                btnCameraCapture.setVisibility(View.INVISIBLE);
                btnCameraCancel.setVisibility(View.VISIBLE);
                btnCameraOk.setVisibility(View.VISIBLE);
            }
        });
        btnCameraOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存图片
                saveImageToFile();
                //相机资源未释放完时再次调用会导致系统崩溃
                //延迟500毫秒，等待资源释放
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                btnCameraCapture.setVisibility(View.VISIBLE);
                btnCameraCancel.setVisibility(View.INVISIBLE);
                btnCameraOk.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null == camera) {
            getCameraInstance();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //没有可处理的SurfaceView
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        setCameraDisplayOrientation(this, 0, camera);
        //停止Camera的预览
        try {
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //重新开启Camera的预览功能
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            //实现自动对焦
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        //实现相机的参数初始化
                        initCamera();
                        //只有加上了这一句，才会自动对焦。
                        camera.cancelAutoFocus();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.release();
        camera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = getCameraInstance();
        }
        surfaceHolder = svCamera.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 得到相机对象
     *
     * @return camera
     */
    private Camera getCameraInstance() {
        try {
            initCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 相机初始化
     */
    private void initCamera() {
        if (camera != null) {
            return;
        }
        camera = Camera.open();
        //设置旋转
        setCameraDisplayOrientation(this, 0, camera);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //1连续对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        camera.startPreview();
        //2如果要实现连续的自动对焦，这一句必须加上
        camera.cancelAutoFocus();
    }

    /**
     * 释放Camera资源
     */
    private void releaseCamera() {
        if (camera != null) {
            //取消回调
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            // compensate the mirror
            result = (360 - result) % 360;
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 拍照成功后的处理
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                LogUtils.e("picture taken data: null");
            } else {
                buffer = new byte[data.length];
                // 获取一份拍的照片保存起来
                buffer = data.clone();
            }
        }
    };

    /**
     * 保存图片
     */
    private void saveImageToFile() {
        File file = getOutFile(TYPE_FILE_IMAGE);
        if (file == null) {
            ToastUtils.showShortToast(getApplicationContext(), "文件创建失败,请检查SD卡读写权限");
            return;
        }

        if (buffer == null) {
            LogUtils.e("自定义相机Buffer: null");
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 保存缩略图
        saveThumbnailToFile(file.getPath());
    }

    /**
     * 保存缩略图
     *
     * @param path 路径
     */
    private void saveThumbnailToFile(String path) {
        int maxImgNum = 2;
        Bitmap newBitmap;
        dataBitmap = BitmapFactory.decodeFile(path);
        if (dataBitmap != null) {
            newBitmap = BitmapUtils.zoomBitmap(dataBitmap,
                    dataBitmap.getWidth() / 2, dataBitmap.getHeight() / 2);

        } else {
            ToastUtils.showShortToast(this, "您拍的照片丢失了，请重拍");
            return;
        }

        Bitmap bitmap = rotateBitmapByDegree(newBitmap);

        saveBitmapToSDCard(filename, bitmap);

        BitmapUtils.drr.add(thumbnailPath);
        if (BitmapUtils.drr.size() > maxImgNum) {
            DefineCameraActivity.this.finish();
        }
    }

    /**
     * 保存缩略图到SD
     *
     * @param bitName 位图文件名
     * @param bitmap  位图文件
     */
    private void saveBitmapToSDCard(String bitName, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String saveDir = imgInfoThumbnail;
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String fileName = saveDir + "/" + bitName;
            File file = new File(fileName);
            file.createNewFile();
            thumbnailPath = file.getPath();

            fileOutputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(byteArray);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 生成输出文件
     *
     * @param fileType 文件类型
     * @return 文件
     */
    private File getOutFile(int fileType) {

        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_REMOVED.equals(storageState)) {
            ToastUtils.showShortToast(getApplicationContext(), "SD卡不存在");
            return null;
        }

        File mediaStorageDir = new File(imgInfo);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        return new File(getFilePath(mediaStorageDir, fileType));
    }

    /**
     * 生成输出文件路径
     *
     * @param mediaStorageDir 文件
     * @param fileType        文件类型
     * @return 文件路径
     */
    private String getFilePath(File mediaStorageDir, int fileType) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHMMSS", Locale.CHINA)
                .format(new Date());
        filename = "IMG_" + timeStamp + ".JPEG";
        String filePath = mediaStorageDir.getPath() + File.separator;
        if (fileType == TYPE_FILE_IMAGE) {
            filePath += filename;
        } else {
            return null;
        }
        return filePath;
    }

    @Override
    protected void onDestroy() {
        if (dataBitmap != null && !dataBitmap.isRecycled()) {
            dataBitmap.recycle();
        }
        super.onDestroy();
    }
}