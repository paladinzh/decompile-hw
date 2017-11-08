package com.huawei.gallery.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.gallery3d.util.GalleryUtils;
import java.io.File;

public class DroidSansChineseslimTextView extends TextView {
    public DroidSansChineseslimTextView(Context context) {
        super(context);
        setTypeFace();
    }

    public DroidSansChineseslimTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeFace();
    }

    public DroidSansChineseslimTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeFace();
    }

    private void setTypeFace() {
        if (GalleryUtils.isSupportSlimType() && !isUsingCustomFont() && isHaveChineseSlimFontType()) {
            setTypeface(Typeface.createFromFile("/system/fonts/DroidSansChineseslim.ttf"));
        }
    }

    private static boolean isHaveChineseSlimFontType() {
        return new File(new StringBuffer("/system/fonts/DroidSansChineseslim.ttf").toString()).exists();
    }

    private static boolean isUsingCustomFont() {
        File userFontPath = new File(new StringBuffer("/data/skin/fonts").toString());
        if (userFontPath.exists() && userFontPath.isDirectory() && userFontPath.listFiles().length > 0) {
            return true;
        }
        return false;
    }
}
