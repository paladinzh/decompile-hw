package com.huawei.gallery.photoshare;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.List;

public class GalleryPhotoShareFeatureProvider extends ContentProvider {
    private static final UriMatcher mMatcher = new UriMatcher(-1);

    static {
        mMatcher.addURI("com.huawei.gallery3d.photoshare.provider", "is_support_cloudphoto", 1);
        mMatcher.addURI("com.huawei.gallery3d.photoshare.provider", "is_support_family_share", 2);
    }

    public boolean onCreate() {
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor c;
        int match = mMatcher.match(uri);
        GalleryLog.v("GalleryPhotoShareFeatureProvider", uri.toString());
        if (match == 2) {
            boolean isSupportSns;
            c = new MatrixCursor(new String[]{"is_support_family_share"});
            Object[] objArr = new Object[1];
            if (PhotoShareUtils.isSupportPhotoShare()) {
                isSupportSns = isSupportSns();
            } else {
                isSupportSns = false;
            }
            objArr[0] = Boolean.valueOf(isSupportSns);
            c.addRow(objArr);
        } else if (match == 1) {
            c = new MatrixCursor(new String[]{"featureName", "value"});
            if ("IsGallerySupportPhotoShare".equalsIgnoreCase(selectionArgs[0])) {
                c.addRow(new Object[]{"IsGallerySupportPhotoShare", Boolean.valueOf(false)});
            }
            if ("IsGallerySupportCallHisyncService".equalsIgnoreCase(selectionArgs[0])) {
                c.addRow(new Object[]{"IsGallerySupportCallHisyncService", Boolean.valueOf(true)});
            }
            if ("IsGallerySupportJumpToPhotoShareMain".equalsIgnoreCase(selectionArgs[0])) {
                c.addRow(new Object[]{"IsGallerySupportJumpToPhotoShareMain", Boolean.valueOf(PhotoShareUtils.isGallerySupportPhotoShare())});
            }
            if ("IsGallerySupportCloudClassify".equalsIgnoreCase(selectionArgs[0])) {
                c.addRow(new Object[]{"IsGallerySupportCloudClassify", Boolean.valueOf(PhotoShareUtils.isGallerySupportPhotoShare())});
            }
        } else {
            throw new IllegalArgumentException("Unkown URI " + uri);
        }
        return c;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private boolean isSupportSns() {
        List<ResolveInfo> resolveInfo = getContext().getPackageManager().queryIntentActivities(new Intent("com.huawei.android.cg.startSnsActivity"), 0);
        return resolveInfo != null && resolveInfo.size() == 1;
    }
}
