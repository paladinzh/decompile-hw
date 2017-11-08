package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Color;
import com.android.gallery3d.R;

public class AspectInfo {
    private static int alphaColor = Color.parseColor("#B2FFFFFF");
    private static int color = Color.parseColor("#99FFFFFF");
    private static int pressedColor = Color.parseColor("#FF0D9FFB");
    public int aspectX;
    public int aspectY;
    public int drawableId;
    public int pressDrawableId;
    public int textId;

    public AspectInfo(int drawableId, int pressDrawableId, int x, int y) {
        this.drawableId = drawableId;
        this.pressDrawableId = pressDrawableId;
        this.aspectX = x;
        this.aspectY = y;
    }

    public AspectInfo(int drawableId, int pressDrawableId, int textId, int x, int y) {
        this.drawableId = drawableId;
        this.pressDrawableId = pressDrawableId;
        this.textId = textId;
        this.aspectX = x;
        this.aspectY = y;
    }

    public static void initialize(Context context) {
        color = context.getColor(R.color.black_background_text_color);
        alphaColor = context.getColor(R.color.editor_label_color);
        pressedColor = context.getColor(R.color.black_background_text_selected_color);
    }

    public static int getColor() {
        return color;
    }

    public static int getAlphaColor() {
        return alphaColor;
    }
}
