package com.huawei.watermark.wmutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.wmdata.WMFileProcessor;
import java.io.Closeable;
import java.io.InputStream;

public class WMFileUtil {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMFileUtil.class.getSimpleName());

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                WMLog.e(TAG, String.format("closeSilently got exception: %s", new Object[]{t.getMessage()}));
            }
        }
    }

    public static Bitmap decodeBitmap(Context context, String wmPath, String picName) {
        Closeable closeable = null;
        Closeable closeable2 = null;
        Bitmap bmp = null;
        try {
            WMLog.v(TAG, "WMBitmapFactory decodeBitmap :" + wmPath + " " + picName);
            closeable = WMFileProcessor.getInstance().openZipInputStream(context, wmPath);
            closeable2 = WMZipUtil.openZipEntryInputStream(picName, closeable);
            Options options = WMBitmapUtil.newOptions();
            options.inMutable = false;
            bmp = BitmapFactory.decodeStream(closeable2, null, options);
        } catch (Exception e) {
            WMLog.e(TAG, "decode picture got an exception", e);
        } finally {
            closeSilently(closeable2);
            closeSilently(closeable);
        }
        return bmp;
    }

    public static Bitmap decodeWMThumbBitmap(Context context, String wmPath, String picName) {
        Closeable closeable = null;
        Closeable closeable2 = null;
        Bitmap bmp = null;
        try {
            WMLog.v(TAG, "WMBitmapFactory decodeWMThumbBitmap :" + wmPath + " " + picName);
            closeable = WMFileProcessor.getInstance().openZipInputStream(context, wmPath);
            closeable2 = WMZipUtil.openZipEntryInputStream(picName, closeable);
            bmp = WMBitmapFactory.getInstance().decodeStream(closeable2);
        } catch (Exception e) {
            WMLog.e(TAG, "decode picture got an exception", e);
        } finally {
            closeSilently(closeable2);
            closeSilently(closeable);
        }
        return bmp;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isFileExist(Context context, String wmPath, String picName) {
        Closeable closeable = null;
        InputStream fis = null;
        boolean res = true;
        try {
            WMLog.v(TAG, "WMBitmapFactory isFileExist :" + wmPath + " " + picName);
            closeable = WMFileProcessor.getInstance().openZipInputStream(context, wmPath);
            fis = WMZipUtil.openZipEntryInputStream(picName, closeable);
            if (fis == null) {
                res = false;
            }
            closeSilently(fis);
            closeSilently(closeable);
            return res;
        } catch (Exception e) {
            WMLog.e(TAG, "decode picture got an exception", e);
            return false;
        } catch (Throwable th) {
            closeSilently(fis);
            closeSilently(closeable);
        }
    }
}
