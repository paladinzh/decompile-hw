package com.android.contacts.hap.camcard.bcr;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import com.android.contacts.util.ContactPhotoUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class CCardPhotoUtils {
    private static final String TAG = CCardPhotoUtils.class.getSimpleName();

    CCardPhotoUtils() {
    }

    static Uri generateTmpPhotoFile(Context context, File rootFile, String fileName) {
        return ContactPhotoUtils.getUriByFileProvider(context, "com.android.contacts.files", new File(rootFile, fileName));
    }

    static Uri generatePhotoUri(Context context, File file) {
        return ContactPhotoUtils.getUriByFileProvider(context, "com.android.contacts.files", file);
    }

    static File getRootFilePath(Context context) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = context.getExternalCacheDir();
        }
        if (!(dir == null || dir.mkdirs())) {
            dir.deleteOnExit();
        }
        return dir;
    }

    static String generateTempPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        return "CCardPhoto-" + new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.US).format(date) + ".jpg";
    }

    static String generateTempPhotoFileDirectory() {
        Date date = new Date(System.currentTimeMillis());
        return "CCardPhoto-" + new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.US).format(date);
    }
}
