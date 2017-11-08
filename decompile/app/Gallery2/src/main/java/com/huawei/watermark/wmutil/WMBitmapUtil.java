package com.huawei.watermark.wmutil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

public class WMBitmapUtil {
    public static Options newOptions() {
        return newOptions(null);
    }

    public static Options newOptions(Bitmap inBitmap) {
        Options options = new Options();
        options.inSampleSize = 1;
        options.inMutable = true;
        if (!(inBitmap == null || inBitmap.isRecycled())) {
            options.inBitmap = inBitmap;
        }
        return options;
    }

    public static void recycleReuseBitmap(Bitmap wmBitmap) {
        WMBitmapFactory.getInstance().recycleBitmap(wmBitmap);
    }
}
