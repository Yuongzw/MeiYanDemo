package com.yuong.meiyandemo.face;

import android.util.Log;

import java.util.Arrays;

/**
 * 人脸封装类  native 反射生成
 */
public class Face {
    public float[] eyesRect;//人眼数组
    //人脸的宽高
    public int width;
    public int height;
    //送检测图片的宽高
    public int imgWidth;
    public int imgHeight;

    public Face( int width, int height, int imgWidth, int imgHeight, float[] eyesRect) {
        this.width = width;
        this.height = height;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.eyesRect = eyesRect;
        Log.w("yuongzw", "Face:" + toString());
    }

    @Override
    public String toString() {
        return "Face{" +
                "eyesRect=" + Arrays.toString(eyesRect) +
                ", width=" + width +
                ", height=" + height +
                ", imgWidth=" + imgWidth +
                ", imgHeight=" + imgHeight +
                '}';
    }
}
