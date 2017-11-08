package com.android.gallery3d.ui;

import android.content.Context;
import com.huawei.gallery.util.ImmersionUtils;

public class HwTextureFactory {
    private static Texture buildCompoundIcon(Context context, int defaultDrawableId, boolean light, String darkDrawableName, String lightDrawableName) {
        if (ImmersionUtils.getControlColor(context) == 0) {
            return new ResourceTexture(context, defaultDrawableId);
        }
        if (light) {
            return new HwExtResourceTexture(context, lightDrawableName, defaultDrawableId);
        }
        return new HwExtResourceTexture(context, darkDrawableName, defaultDrawableId);
    }

    public static Texture buildCheckedFrameIcon(Context context, int defaultDrawableId) {
        return buildCheckedFrameIcon(context, defaultDrawableId, false);
    }

    public static Texture buildCheckedPressFrameIcon(Context context, int defaultDrawableId) {
        return buildCheckedPressFrameIcon(context, defaultDrawableId, false);
    }

    public static Texture buildCheckedFrameIcon(Context context, int defaultDrawableId, boolean light) {
        return buildCompoundIcon(context, defaultDrawableId, light, "btn_check_on_colorful_dark", "btn_check_on_colorful");
    }

    public static Texture buildCheckedPressFrameIcon(Context context, int defaultDrawableId, boolean light) {
        return buildCompoundIcon(context, defaultDrawableId, light, "btn_check_on_pressed_colorful_dark", "btn_check_on_pressed_colorful");
    }

    public static Texture buildCheckedOffFrameIcon(Context context, int defaultDrawableId) {
        return buildCheckedOffFrameIcon(context, defaultDrawableId, false);
    }

    public static Texture buildCheckedOffPressFrameIcon(Context context, int defaultDrawableId) {
        return buildCheckedOffPressFrameIcon(context, defaultDrawableId, false);
    }

    public static Texture buildCheckedOffFrameIcon(Context context, int defaultDrawableId, boolean light) {
        return buildCompoundIcon(context, defaultDrawableId, light, "btn_check_off_colorful_dark", "btn_check_off_colorful");
    }

    public static Texture buildCheckedOffPressFrameIcon(Context context, int defaultDrawableId, boolean light) {
        return buildCompoundIcon(context, defaultDrawableId, light, "btn_check_off_pressed_colorful_dark", "btn_check_off_pressed_colorful");
    }
}
