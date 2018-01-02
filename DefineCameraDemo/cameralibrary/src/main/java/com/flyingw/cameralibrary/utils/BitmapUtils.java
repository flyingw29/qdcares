package com.flyingw.cameralibrary.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flyingw on 2017/12/27.
 */

public class BitmapUtils {
    public static int max = 0;
    public static boolean act_bool = true;
    public static ArrayList<Bitmap> bmp = new ArrayList<>();
    public static List<String> drr = new ArrayList<>();

    /**
     * @param bitmap 位图
     * @param width  宽
     * @param height 高
     * @return 生成图片
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    /**
     * 将图片旋转90度
     *
     * @param bitMap 需要旋转的图片
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bitMap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitMap, 0, 0, bitMap.getWidth(), bitMap.getHeight(), matrix, true);
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bitMap;
        }
        if (bitMap != returnBm) {
            bitMap.recycle();
        }
        return returnBm;
    }
}
