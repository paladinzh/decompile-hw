package com.huawei.rcs.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class HwRcseUtility {
    public static final String TAG = "HwRcseUtility";

    public static Bitmap getBitmapfromType(Context context, String Path) {
        Drawable icon = getTypeIcon(context, getMimeType(getExtensionName(Path)));
        if (icon != null) {
            return drawableToBitmap(icon);
        }
        return null;
    }

    public static String getExtensionName(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf(46);
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static String getMimeType(String subfix) {
        if (TextUtils.isEmpty(subfix)) {
            return null;
        }
        String postfix = subfix.toLowerCase(Locale.getDefault());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix);
        if (TextUtils.isEmpty(mimeType)) {
            if (postfix.equals("gz")) {
                return "application/x-gzip";
            }
            if (postfix.equals("bz")) {
                return "application/x-bzip";
            }
            if (postfix.equals("bz2")) {
                return "application/x-bzip2";
            }
            if (postfix.matches("java|php|c|cpp|xml|py|log")) {
                return "text/plain";
            }
        }
        return mimeType;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable getTypeIcon(Context context, String mimeType) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromParts("file", "", null), mimeType);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 65536);
        if (list.size() != 0) {
            return ((ResolveInfo) list.get(0)).activityInfo.loadIcon(pm);
        }
        return null;
    }

    public static String formatFileSize(long fileSize) {
        float size = (float) fileSize;
        String unit = ConstValues.B_VERSION_CHAR;
        if (size > 1024.0f) {
            size /= 1024.0f;
            unit = "KB";
        }
        if (size > 1024.0f) {
            size /= 1024.0f;
            unit = "MB";
        }
        if (size > 1024.0f) {
            size /= 1024.0f;
            unit = "GB";
        }
        return new DecimalFormat("0.0").format((double) size) + unit;
    }
}
