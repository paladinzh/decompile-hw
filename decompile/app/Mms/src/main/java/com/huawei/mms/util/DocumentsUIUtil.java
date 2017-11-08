package com.huawei.mms.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Downloads.Impl;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import com.huawei.cspcommon.MLog;

public class DocumentsUIUtil {
    private static String TAG = "DocumentsUIUtil";
    static final boolean isKitKat = (VERSION.SDK_INT >= 19);

    public static Uri convertUri(Context context, Uri uri) {
        if (uri == null || context == null) {
            MLog.e(TAG, "convertUri-> uri or context is null, return directly.");
            return uri;
        }
        MLog.e(TAG, "isKitKat = " + isKitKat);
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            return convertDocumentsUri(context, uri);
        }
        return uri;
    }

    private static Uri convertDocumentsUri(Context context, Uri uri) {
        try {
            return convertDocumentsUriInternal(context, uri);
        } catch (Exception ex) {
            MLog.e(TAG, "convertDocumentsUri->ex:", (Throwable) ex);
            return uri;
        }
    }

    private static Uri convertDocumentsUriInternal(Context context, Uri uri) {
        String path = "";
        MLog.e(TAG, "convertDocumentsUri->uri:" + uri);
        String[] split;
        if (isExternalStorageDocument(uri)) {
            split = DocumentsContract.getDocumentId(uri).split(":");
            if ("primary".equalsIgnoreCase(split[0])) {
                path = Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                MLog.e(TAG, "type is not primary.");
            }
        } else if (isDownloadsDocument(uri)) {
            path = getDataColumn(context, ContentUris.withAppendedId(Impl.PUBLICLY_ACCESSIBLE_DOWNLOADS_URI, Long.parseLong(DocumentsContract.getDocumentId(uri))), null, null);
        } else if (isMediaDocument(uri)) {
            String type = DocumentsContract.getDocumentId(uri).split(":")[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String selection = "_id=?";
            path = getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
        }
        if (TextUtils.isEmpty(path)) {
            MLog.e(TAG, "convertDocumentsUri->path is empty, return orgin uri");
            return uri;
        }
        Uri resultUri = Uri.parse("file://" + path);
        MLog.e(TAG, "convertDocumentsUri->resultUri:" + resultUri);
        return resultUri;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            return string;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
