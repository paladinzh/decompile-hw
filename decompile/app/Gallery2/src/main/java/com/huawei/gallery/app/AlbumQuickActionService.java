package com.huawei.gallery.app;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.GalleryData.FavoriteWhereClause;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class AlbumQuickActionService extends QuickActionService {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)", "SUM((CASE WHEN media_type=3 THEN 1 ELSE 0 END))"};
    private static Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private static final String WHERECLAUSE_MY_FAVORITE_TYPE = ("is_hw_favorite = " + String.valueOf(1));
    protected int mCachedCount = 0;
    protected int mCachedVideoCount = 0;
    private GalleryData mFavoriteData;
    private ComponentName mGalleryMainActivity;
    private ComponentName mHwCameraPhotoActivity;
    private String mQueryClause;
    private String[] mQueryClauseArgs = null;
    private ComponentName mSlotAlbumActivity;

    public List<QuickAction> onGetQuickActions(ComponentName targetActivityName) {
        this.mSlotAlbumActivity = new ComponentName(this, SlotAlbumActivity.class);
        this.mHwCameraPhotoActivity = new ComponentName(this, HwCameraPhotoActivity.class);
        this.mGalleryMainActivity = new ComponentName(this, GalleryMain.class);
        ArrayList<AlbumContact> album_contact = new ArrayList();
        album_contact.add(new AlbumContact(R.string.album_quick_action_favourites, getFavoriteIntent(), null));
        album_contact.add(new AlbumContact(R.string.album_quick_action_search, getSearchIntent(), null));
        album_contact.add(new AlbumContact(R.string.album_quick_action_newest_photo, getNewestPhotoIntent(), null));
        album_contact.add(new AlbumContact(R.string.album_quick_action_smart_recognition, getSmartRecognitionIntent(), null));
        ArrayList<QuickAction> actions = new ArrayList();
        for (int i = 0; i < album_contact.size(); i++) {
            Intent intent = ((AlbumContact) album_contact.get(i)).getIntent();
            actions.add(new QuickAction(getApplicationContext().getString(((AlbumContact) album_contact.get(i)).getResId()), null, intent.getComponent(), PendingIntent.getActivity(this, i, intent, 0).getIntentSender()));
        }
        return actions;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Intent getFavoriteIntent() {
        Bundle data = new Bundle();
        Intent intent = new Intent();
        this.mFavoriteData = ((GalleryApp) getApplication()).getGalleryData();
        if (this.mCachedCount == 0 || this.mCachedVideoCount == 0) {
            Closeable closeable = null;
            this.mQueryClause = getQueryClause();
            try {
                closeable = getContentResolver().query(EXTERNAL_FILE_URI, COUNT_PROJECTION, this.mQueryClause, this.mQueryClauseArgs, null);
                if (closeable == null) {
                    this.mCachedCount = 0;
                    this.mCachedVideoCount = 0;
                } else if (closeable.moveToNext()) {
                    this.mCachedCount = closeable.getInt(0);
                    this.mCachedVideoCount = closeable.getInt(1);
                } else {
                    this.mCachedCount = 0;
                    this.mCachedVideoCount = 0;
                }
                Utils.closeSilently(closeable);
            } catch (SQLException e) {
                this.mCachedCount = 0;
                this.mCachedVideoCount = 0;
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
        }
        if (this.mCachedCount <= 0) {
            intent.setAction("android.intent.action.GET_ALBUM_CONTENT");
            intent.setFlags(268468224);
            intent.setComponent(this.mGalleryMainActivity);
        } else {
            data.putString("media-path", "/virtual/favorite");
            data.putBoolean("only-local-camera-video-album", false);
            intent.putExtras(data);
            intent.setFlags(268468224);
            intent.setComponent(this.mSlotAlbumActivity);
        }
        return intent;
    }

    private Intent getNewestPhotoIntent() {
        Intent intent = new Intent();
        Uri uri = getLatestPictureUri(getContentResolver());
        if (uri == null) {
            intent.setAction("android.intent.action.GET_ALBUM_CONTENT");
            intent.setFlags(268468224);
            intent.setComponent(this.mGalleryMainActivity);
        } else {
            intent.setAction("com.huawei.gallery.action.VIEW_PHOTO_FROM_HWCAMERA");
            intent.setFlags(268468224);
            intent.setComponent(this.mHwCameraPhotoActivity);
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("keep-from-camera", false);
        }
        return intent;
    }

    private Intent getSmartRecognitionIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.GET_PHOTOSHARE_CONTENT");
        intent.setFlags(268468224);
        intent.setComponent(this.mGalleryMainActivity);
        return intent;
    }

    private Intent getSearchIntent() {
        return getSmartRecognitionIntent();
    }

    private Uri getLatestPictureUri(ContentResolver resolver) {
        Uri uri;
        long cameraBucketId = (long) MediaSetUtils.getCameraBucketId();
        long secondaryCameraBucketId = (long) MediaSetUtils.getBucketId(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs());
        String whereClauseMediaTypeAll = "media_type IN (1,3) AND ";
        String whereClause = "datetaken > ? AND bucket_id IN (" + cameraBucketId + " , " + secondaryCameraBucketId + ") AND " + "substr(_data, 1, length(_data) - length('000.JPG')) NOT IN " + "(SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM files WHERE media_type = 1 AND " + "bucket_id IN (" + cameraBucketId + " , " + secondaryCameraBucketId + ") AND " + "_data LIKE '%BURST____COVER.JPG')";
        String orderBy = "datetaken DESC, _id DESC";
        Uri uri2 = EXTERNAL_FILE_URI.buildUpon().appendQueryParameter("limit", "0,1").build();
        Closeable closeable = null;
        Uri picUri = null;
        try {
            closeable = resolver.query(uri2, new String[]{"mime_type", "_id"}, whereClauseMediaTypeAll + whereClause, new String[]{"0"}, orderBy);
            if (closeable == null) {
                uri = null;
                return uri;
            }
            closeable.moveToFirst();
            try {
                String mimeType = closeable.getString(closeable.getColumnIndexOrThrow("mime_type"));
                if (mimeType == null) {
                    Utils.closeSilently(closeable);
                    return null;
                }
                int index = closeable.getInt(closeable.getColumnIndexOrThrow("_id"));
                if (mimeType.startsWith("image/")) {
                    picUri = Media.EXTERNAL_CONTENT_URI;
                } else if (mimeType.startsWith("video/")) {
                    picUri = Video.Media.EXTERNAL_CONTENT_URI;
                }
                if (picUri != null) {
                    picUri = Uri.withAppendedPath(picUri, Integer.toString(index));
                }
                Utils.closeSilently(closeable);
                return picUri;
            } catch (CursorIndexOutOfBoundsException e) {
                Utils.closeSilently(closeable);
                return null;
            }
        } catch (SQLException e2) {
            uri = null;
            return uri;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private String getQueryClause() {
        this.mQueryClauseArgs = null;
        FavoriteWhereClause favoriteWhere = this.mFavoriteData.getFavoriteWhereClause();
        if (favoriteWhere == null) {
            return " media_type in (1,3) AND  bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') AND " + WHERECLAUSE_MY_FAVORITE_TYPE + getSpecialHideQueryClause();
        }
        this.mQueryClauseArgs = favoriteWhere.mPaths;
        return " media_type in (1,3) AND  bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') AND " + favoriteWhere.mWhereClause + getSpecialHideQueryClause();
    }

    private String getSpecialHideQueryClause() {
        return GalleryUtils.getSpecialHideQueryClause(((GalleryApp) getApplication()).getAndroidContext());
    }
}
