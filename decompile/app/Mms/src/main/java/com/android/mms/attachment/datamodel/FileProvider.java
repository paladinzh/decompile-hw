package com.android.mms.attachment.datamodel;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public abstract class FileProvider extends ContentProvider {
    private static final Random RANDOM_ID = new Random();

    abstract File getFile(String str, String str2);

    protected static boolean isValidFileId(String fileId) {
        int index = fileId.startsWith("/") ? 1 : 0;
        while (index < fileId.length()) {
            if (!Character.isDigit(Character.valueOf(fileId.charAt(index)).charValue())) {
                return false;
            }
            index++;
        }
        return true;
    }

    public boolean onCreate() {
        return true;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int i = 0;
        String fileId = uri.getPath();
        if (!isValidFileId(fileId)) {
            return 0;
        }
        if (getFile(fileId, getExtensionFromUri(uri)).delete()) {
            i = 1;
        }
        return i;
    }

    public ParcelFileDescriptor openFile(Uri uri, String fileMode) throws FileNotFoundException {
        String fileId = uri.getPath();
        if (!isValidFileId(fileId)) {
            return null;
        }
        int mode;
        File file = getFile(fileId, getExtensionFromUri(uri));
        if (TextUtils.equals(fileMode, "r")) {
            mode = 268435456;
        } else {
            mode = 603979776;
        }
        return ParcelFileDescriptor.open(file, mode);
    }

    protected static String getExtensionFromUri(Uri uri) {
        return uri.getQueryParameter("ext");
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }
}
