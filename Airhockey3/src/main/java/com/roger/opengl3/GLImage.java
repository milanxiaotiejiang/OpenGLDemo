package com.roger.opengl3;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by zhangyuanyuan on 2017/11/27.
 */

public class GLImage {

    public static Bitmap iBitmap;
    public static Bitmap jBitmap;
    public static Bitmap kBitmap;
    public static Bitmap lBitmap;
    public static Bitmap mBitmap;
    public static Bitmap nBitmap;
    public static Bitmap close_Bitmap;


    public static void load(Resources resources) {
        iBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_1);
        jBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_2);
        kBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_3);
        lBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_4);
        mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_5);
        nBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_6);
        close_Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_7);
    }

}
