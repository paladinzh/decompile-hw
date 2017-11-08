package com.huawei.watermark.wmutil;

import android.content.Context;
import com.android.gallery3d.R;
import com.android.gallery3d.gadget.XmlUtils;

public class WMResourceUtil {
    private static int getObjectWithId(Context context, String resName, String type) {
        if (WMStringUtil.isEmptyString(resName)) {
            return 0;
        }
        return context.getResources().getIdentifier(resName, type, context.getPackageName());
    }

    public static int getLayoutId(Context context, String resName) {
        return getObjectWithId(context, resName, XmlUtils.START_TAG);
    }

    public static int getStringId(Context context, String resName) {
        return getObjectWithId(context, resName, "string");
    }

    public static int getDrawableId(Context context, String resName) {
        if (WMStringUtil.isEmptyString(resName)) {
            return 0;
        }
        return getObjectWithId(context, resName, "drawable");
    }

    public static int getStyleId(Context context, String resName) {
        return getObjectWithId(context, resName, "style");
    }

    public static int getId(Context context, String resName) {
        return getObjectWithId(context, resName, "id");
    }

    public static int getAnimid(Context context, String resName) {
        return getObjectWithId(context, resName, "anim");
    }

    private static int getDimenId(Context context, String resName) {
        return getObjectWithId(context, resName, "dimen");
    }

    public static int getArrayId(Context context, String resName) {
        return getObjectWithId(context, resName, "array");
    }

    public static int getDimensionPixelSize(Context context, String idStr) {
        if (context == null) {
            return 0;
        }
        return getDimensionPixelSize(context, getDimenId(context, idStr));
    }

    public static int getDimensionPixelSize(Context context, int id) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getDimensionPixelSize(id);
    }

    public static boolean isTabletProduct(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet_product_wm);
    }

    public static boolean isHighResTabletProduct(Context context) {
        return context.getResources().getBoolean(R.bool.is_high_res_product_wm);
    }
}
