package com.flyingw.cameralibrary.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author flyingw on 2017/12/27.
 */
public class ToSaveUtils {
    /**
     * @param bm
     * @param picName
     */
    public static void saveBitmap(Bitmap bm, String picName, String sdPath) {
        try {
            if (!isFileExist("", sdPath)) {
                File tempf = createSDDir("", sdPath);
            }
            File f = new File(sdPath, picName + ".JPEG");
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建文件目录
     *
     * @param dirName
     * @return
     * @throws IOException
     */
    private static File createSDDir(String dirName, String sdPath) throws IOException {
        File dir = new File(sdPath + dirName);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            dir.mkdir();
            Log.e("createSDDir:", dir.getAbsolutePath());
        }
        return dir;
    }

    private static boolean isFileExist(String fileName, String sdPath) {
        File file = new File(sdPath + fileName);
        file.isFile();
        return file.exists();
    }

    public static void delFile(String fileName, String sdPath) {
        File file = new File(sdPath + fileName);
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    public static void deleteDir(String sdPath) {
        File dir = new File(sdPath);
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                deleteDir(sdPath);
            }
        }
        dir.delete();
    }
}
