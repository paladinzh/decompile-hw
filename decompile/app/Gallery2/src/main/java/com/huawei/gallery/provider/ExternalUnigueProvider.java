package com.huawei.gallery.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.File;

public class ExternalUnigueProvider extends ContentProvider {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final Uri BASE_URI = Uri.parse("content://com.huawei.gallery.recycle/");
    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    private ExternalUniqueDBHelper mExternalUniqueDBHelper;

    static {
        boolean z;
        if (ExternalUnigueProvider.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
        URI_MATCHER.addURI("com.huawei.gallery.recycle", "add_recycle_file_unique_id", 30);
        URI_MATCHER.addURI("com.huawei.gallery.recycle", "get_recycle_file_unique_id", 31);
        URI_MATCHER.addURI("com.huawei.gallery.recycle", "del_recycle_file_unique_id", 32);
    }

    public boolean onCreate() {
        return true;
    }

    private static boolean dbFileExisted() {
        return new File(getRecycleBinInnerPath(), "gallery_recycle.db").exists();
    }

    private synchronized ExternalUniqueDBHelper getExternalUniqueDBHelper() {
        if (this.mExternalUniqueDBHelper == null || !dbFileExisted()) {
            this.mExternalUniqueDBHelper = new ExternalUniqueDBHelper(getContext(), getRecycleBinInnerPath(), "gallery_recycle.db");
        }
        return this.mExternalUniqueDBHelper;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!-assertionsDisabled) {
            if ((URI_MATCHER.match(uri) == 31 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return getExternalUniqueDBHelper().getReadableDatabase().query("recycle_file_unique_id", projection, selection, selectionArgs, null, null, sortOrder);
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (!-assertionsDisabled) {
            if ((URI_MATCHER.match(uri) == 30 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        long rowId = getExternalUniqueDBHelper().getWritableDatabase().insert("recycle_file_unique_id", null, values);
        GalleryLog.d("ExternalUnigueProvider", "insert:" + rowId);
        return ContentUris.withAppendedId(BASE_URI, rowId);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!-assertionsDisabled) {
            if ((URI_MATCHER.match(uri) == 32 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        return getExternalUniqueDBHelper().getWritableDatabase().delete("recycle_file_unique_id", selection, selectionArgs);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static String getRecycleBinInnerPath() {
        return GalleryStorageManager.getInstance().getInnerGalleryStorage().getPath() + "/Pictures/.Gallery2/recycle/";
    }
}
