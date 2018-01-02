package com.flyingw.cameralibrary.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flyingw.cameralibrary.R;
import com.flyingw.cameralibrary.adapter.CameraPhotoAdapter;
import com.flyingw.cameralibrary.utils.BitmapUtils;
import com.flyingw.cameralibrary.utils.ToSaveUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flyingw on 2018/1/2
 */

public class CameraPhotoActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    ViewPager viewPager;
    Button btnPhotoCancel;
    Button btnPhotoDelete;
    Button btnPhotoOk;
    RelativeLayout rvPhoto;

    private CameraPhotoAdapter adapter;
    private ArrayList<View> list = null;

    private ArrayList<Bitmap> bmp = new ArrayList<>();
    private List<String> drr = new ArrayList<>();
    private List<String> del = new ArrayList<>();
    private int max;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_photo);
        initView();
        initViewPager();
        addListener();
    }


    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnPhotoDelete = (Button) findViewById(R.id.btn_photo_delete);
        btnPhotoCancel = (Button) findViewById(R.id.btn_photo_cancel);
        btnPhotoOk = (Button) findViewById(R.id.btn_photo_ok);
        rvPhoto = (RelativeLayout) findViewById(R.id.rv_photo);
        rvPhoto.setBackgroundColor(0x70000000);
        max = BitmapUtils.max;
    }

    private void addListener() {
        viewPager.addOnPageChangeListener(this);
        btnPhotoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnPhotoDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImg();
            }
        });
        btnPhotoOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImg();
                finish();
            }
        });
    }

    /**
     * 初始化
     */
    private void initViewPager() {

        for (int i = 0; i < BitmapUtils.bmp.size(); i++) {
            bmp.add(BitmapUtils.bmp.get(i));
        }
        for (int i = 0; i < BitmapUtils.drr.size(); i++) {
            drr.add(BitmapUtils.drr.get(i));
        }

        for (int i = 0; i < bmp.size(); i++) {
            initImgData(bmp.get(i));
        }
        adapter = new CameraPhotoAdapter(list);

        viewPager.setAdapter(adapter);

        Intent intent = getIntent();
        int id = intent.getIntExtra("ID", 0);
        viewPager.setCurrentItem(id);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        count = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 图片初始化
     *
     * @param bitmap 位图
     */
    private void initImgData(Bitmap bitmap) {
        if (null == list) {
            list = new ArrayList<>();
        }
        ImageView img = new ImageView(this);
        img.setBackgroundColor(0xFF000000);
        img.setImageBitmap(bitmap);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        list.add(img);
    }

    /**
     * 删除图片
     */
    private void deleteImg() {
        if (1 == list.size()) {
            BitmapUtils.bmp.clear();
            BitmapUtils.drr.clear();
            BitmapUtils.max = 0;
            ToSaveUtils.deleteDir("");
            finish();
        } else {
            String newStr = drr.get(count).substring(
                    drr.get(count).lastIndexOf("/") + 1,
                    drr.get(count).lastIndexOf("."));
            bmp.remove(count);
            drr.remove(count);
            del.add(newStr);
            max--;
            viewPager.removeAllViews();
            list.remove(count);
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 提交操作
     */
    private void saveImg() {

        BitmapUtils.bmp = bmp;
        BitmapUtils.drr = drr;
        BitmapUtils.max = max;
        for (int i = 0; i < del.size(); i++) {
            ToSaveUtils.delFile(del.get(i) + ".JPEG", "");
        }
    }

}