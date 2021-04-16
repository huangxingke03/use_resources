package com.example.resources.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.palette.graphics.Palette;

/**
 * 主色调工具类
 */
public class PaletteHelper {

    /**
     * 设置图片主色调
     *
     * @param bitmap
     * @return
     */
    public static void setPaletteColor(Bitmap bitmap, final View view, RelativeLayout shadowLl) {
        if (bitmap == null) {
            setColor(view, shadowLl, Color.BLACK);
            return;
        }
        Palette.from(bitmap).maximumColorCount(10).generate(palette -> {
            Palette.Swatch s = palette.getDominantSwatch();//独特的一种
            Palette.Swatch s1 = palette.getVibrantSwatch();       //获取到充满活力的这种色调
            Palette.Swatch s2 = palette.getLightVibrantSwatch();   //获取充满活力的亮
            Palette.Swatch s3 = palette.getMutedSwatch();           //获取柔和的色调
            Palette.Swatch s4 = palette.getLightMutedSwatch();    //获取柔和的亮
            if (s != null) {
                setColor(view, shadowLl, s.getRgb());
            } else if (s1 != null) {
                setColor(view, shadowLl, s.getRgb());
            } else if (s1 != null) {
                setColor(view, shadowLl, s1.getRgb());
            } else if (s2 != null) {
                setColor(view, shadowLl, s2.getRgb());
            } else if (s3 != null) {
                setColor(view, shadowLl, s3.getRgb());
            } else if (s4 != null) {
                setColor(view, shadowLl, s4.getRgb());
            }
        });

    }

    private static void setColor(View view, RelativeLayout shadowLl, int color) {
        view.setBackgroundColor(color);
        int[] colors = {color, 0};
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadowLl.setBackground(drawable);
    }


    /**
     * 设置 View 背景主色调
     *
     * @param bitmap
     * @return
     */
    public static void setPaletteColor(Bitmap bitmap, final View view) {
        if (bitmap == null) {
            view.setBackgroundColor(Color.WHITE);
            return;
        }
        Palette.from(bitmap).maximumColorCount(10).generate(palette -> {
            if (palette == null) return;
            Palette.Swatch s = palette.getDominantSwatch();       //独特的一种
            Palette.Swatch s1 = palette.getVibrantSwatch();       //获取到充满活力的这种色调
            Palette.Swatch s2 = palette.getLightVibrantSwatch();  //获取充满活力的亮
            Palette.Swatch s3 = palette.getMutedSwatch();         //获取柔和的色调
            Palette.Swatch s4 = palette.getLightMutedSwatch();    //获取柔和的亮
            if (s != null) {
                view.setBackgroundColor(s.getRgb());
            } else if (s1 != null) {
                view.setBackgroundColor(s1.getRgb());
            } else if (s2 != null) {
                view.setBackgroundColor(s2.getRgb());
            } else if (s3 != null) {
                view.setBackgroundColor(s3.getRgb());
            } else if (s4 != null) {
                view.setBackgroundColor(s4.getRgb());
            }
        });

    }
}
