package com.common.imageloader.core.decode;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.common.imageloader.utils.L;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.io.IOException;

public class ApkIconImageDecoder implements ImageDecoder {
    private static final String FILE_TITLE_TAG = "file://";
    private static final String TAG = "ApkIconImageDecoder";

    public ApkIconImageDecoder(boolean loggingEnabled) {
    }

    public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
        String uri = decodingInfo.getOriginalImageUri();
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        L.i(TAG, "decode uri :" + uri);
        String filePath = "";
        String[] fileStrings = uri.split("file://");
        if (fileStrings.length > 1) {
            filePath = fileStrings[1];
        }
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        L.i(TAG, "decode apk file path :" + filePath);
        Drawable icon = null;
        PackageManager packageManager = GlobalContext.getContext().getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(filePath, 1);
        if (!(packageInfo == null || packageInfo.applicationInfo == null)) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            appInfo.publicSourceDir = filePath;
            icon = packageManager.getApplicationIcon(appInfo);
        }
        return drawableToBitmap(icon);
    }

    static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Config config;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }
}
