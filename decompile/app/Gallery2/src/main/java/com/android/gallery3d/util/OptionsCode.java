package com.android.gallery3d.util;

import android.graphics.BitmapFactory.Options;
import java.lang.reflect.Field;

public class OptionsCode {
    private static Field sErrorCodeField;

    static {
        try {
            sErrorCodeField = Options.class.getDeclaredField("errorCode");
        } catch (NoSuchFieldException e) {
            GalleryLog.d("OptionsCode", "there are no errorCode field in BitmapFactory.Options");
        }
    }

    public static int getErrorCode(Options options) {
        if (sErrorCodeField == null) {
            return 0;
        }
        try {
            return sErrorCodeField.getInt(options);
        } catch (IllegalAccessException e) {
            GalleryLog.d("OptionsCode", "get error code illegal access exception");
            return 0;
        }
    }
}
