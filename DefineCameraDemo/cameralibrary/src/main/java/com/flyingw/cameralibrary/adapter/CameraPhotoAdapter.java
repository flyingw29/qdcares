package com.flyingw.cameralibrary.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author flyingw on 2017/12/29.
 */
public class CameraPhotoAdapter extends PagerAdapter {

    private ArrayList<View> list = null;
    private int size;

    public CameraPhotoAdapter(ArrayList<View> list) {
        this.list = list;
        size = list == null ? 0 : list.size();
    }

    public void setList(ArrayList<View> list) {
        this.list = list;
        size = list == null ? 0 : list.size();
    }

    @Override
    public int getCount() {
        return size;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position % size));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            container.addView(list.get(position % size), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.get(position % size);
    }
}
