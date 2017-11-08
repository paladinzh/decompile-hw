package com.huawei.systemmanager.push;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class PushFileProvider extends ContentProvider {
    public static final String AUTHORITY = "com.huawei.systemmanager.push.provider";
    public static final Uri BASE_URI = Uri.parse("content://com.huawei.systemmanager.push.provider");
    private static String TAG = "PushFileProvider";

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return "*/*";
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        int size = segments.size();
        File file = null;
        if (size > 1) {
            file = new File(getContext().getCacheDir(), ((String) segments.get(size - 2)) + "/" + ((String) segments.get(size - 1)));
        } else if (size > 0) {
            file = new File(getContext().getCacheDir(), (String) segments.get(size - 1));
        }
        if (file != null && file.exists()) {
            return ParcelFileDescriptor.open(file, ShareCfg.PERMISSION_MODIFY_CALENDAR);
        }
        HwLog.w(TAG, "file does not exist which uri is " + uri);
        return null;
    }
}
