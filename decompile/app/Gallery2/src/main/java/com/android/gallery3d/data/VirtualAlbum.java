package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.GalleryData.FavoriteWhereClause;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.util.ArrayList;

public class VirtualAlbum extends LocalMergeQuerySet {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)", "SUM((CASE WHEN media_type=3 THEN 1 ELSE 0 END))"};
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private static final String WHERECLAUSE_IMAGE_TYPE = (" AND media_type = " + String.valueOf(1));
    private static final String WHERECLAUSE_MY_FAVORITE_TYPE = ("is_hw_favorite = " + String.valueOf(1));
    private static final String WHERECLAUSE_VIDEO_TYPE = (" AND media_type = " + String.valueOf(3));
    private static final Uri[] mWatchUris = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI};
    private final GalleryApp mApplication;
    private GalleryData mFavoriteData;
    private final int mLoadType;
    private final ChangeNotifier mMyFavoriteReloader;
    private final ChangeNotifier mNotifier;
    private final String mOrderClause;
    private String mQueryClause;
    private String[] mQueryClauseArgs = null;
    private final ReloadNotifier mReloader;
    private final ContentResolver mResolver;
    private String mVirtualType;
    private String mWhereClause3DModelType;
    private String mWhereClauseCamera3dPanoramaType;
    private String mWhereClauseCameraVideoType;
    private String mWhereClauseDocRectifyType;
    private String mWhereClauseScreenshotsVideoType;

    public VirtualAlbum(Path path, GalleryApp application, int type) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mLoadType = type;
        this.mResolver = application.getContentResolver();
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUM, application);
        this.mMyFavoriteReloader = new ChangeNotifier((MediaSet) this, Constant.MYFAVORITE_URI, application);
        this.mFavoriteData = this.mApplication.getGalleryData();
        initLocalClause();
        this.mVirtualType = path.getSuffix();
        this.mQueryClause = initQueryClause(this.mVirtualType);
        this.mOrderClause = "datetaken DESC, _id DESC";
    }

    private void initLocalClause() {
        String str;
        this.mWhereClauseCameraVideoType = "media_type = " + String.valueOf(3) + " AND bucket_id IN (" + MediaSetUtils.getCameraBucketId() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs() + ")";
        StringBuilder append = new StringBuilder().append("media_type = ").append(String.valueOf(1)).append(" AND bucket_id IN (").append(MediaSetUtils.getCameraBucketId()).append(" , ").append(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs()).append(")").append(" AND ").append("special_file_type").append(" in ( ").append(20);
        if (FyuseFile.isSupport3DPanoramaAPK()) {
            str = ", 11";
        } else {
            str = "";
        }
        this.mWhereClauseCamera3dPanoramaType = append.append(str).append(")").toString();
        this.mWhereClauseDocRectifyType = "media_type = " + String.valueOf(1) + " AND " + " bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') AND " + "hw_rectify_offset" + " > 0 ";
        this.mWhereClause3DModelType = "media_type = " + String.valueOf(1) + " AND " + "special_file_type" + " = " + 16;
        this.mWhereClauseScreenshotsVideoType = "media_type = " + String.valueOf(3) + " AND bucket_id IN (" + MediaSetUtils.getScreenshotsBucketID() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs() + ")";
    }

    public void reset() {
        initLocalClause();
        this.mQueryClause = initQueryClause(this.mVirtualType);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        GalleryUtils.assertNotInRenderThread();
        Uri uri = EXTERNAL_FILE_URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> mediaItemList = new ArrayList();
        DataManager dataManager = this.mApplication.getDataManager();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, PROJECTION, this.mQueryClause, this.mQueryClauseArgs, this.mOrderClause);
            if (closeable == null) {
                GalleryLog.w("VirtualAlbum", "query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return mediaItemList;
            }
            while (closeable.moveToNext()) {
                mediaItemList.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return mediaItemList;
        } catch (Exception e) {
            GalleryLog.w("VirtualAlbum", "query fail: " + e);
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1 || this.mCachedVideoCount == -1) {
            Closeable closeable = null;
            try {
                closeable = this.mResolver.query(EXTERNAL_FILE_URI, COUNT_PROJECTION, this.mQueryClause, this.mQueryClauseArgs, null);
                if (closeable == null) {
                    GalleryLog.w("VirtualAlbum", "query fail");
                    this.mCachedCount = 0;
                    this.mCachedVideoCount = 0;
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                this.mCachedVideoCount = closeable.getInt(1);
                Utils.closeSilently(closeable);
            } catch (Exception e) {
                GalleryLog.w("VirtualAlbum", "query fail:" + e);
                this.mCachedCount = 0;
                this.mCachedVideoCount = 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public String getName() {
        int nameId;
        if ("favorite".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.virtual_folder_my_favorite_general;
        } else if ("camera_video".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.camera_video;
        } else if ("screenshots_video".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.screenshots_video;
        } else if ("3d_panorama".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.panorama;
        } else if ("doc_rectify".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.folder_doc_rectify;
        } else if ("3d_model_image".equalsIgnoreCase(this.mVirtualType)) {
            nameId = R.string.capture_mode_3dcreator;
        } else {
            throw new RuntimeException("bad virtual type: " + this.mVirtualType);
        }
        return this.mApplication.getResources().getString(nameId);
    }

    public long reload() {
        if (this.mNotifier.isDirty() || this.mReloader.isDirty() || ("favorite".equalsIgnoreCase(this.mVirtualType) && this.mMyFavoriteReloader.isDirty())) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            invalidCachedCount();
        }
        this.mQueryClause = initQueryClause(this.mVirtualType);
        return this.mDataVersion;
    }

    public int getSupportedOperations() {
        return 1029;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        Closeable closeable = null;
        try {
            if ("3d_panorama".equalsIgnoreCase(this.mVirtualType)) {
                closeable = this.mResolver.query(EXTERNAL_FILE_URI, new String[]{"_data", "special_file_type"}, this.mQueryClause, this.mQueryClauseArgs, null);
                if (closeable != null && closeable.getCount() > 0) {
                    while (closeable.moveToNext()) {
                        String filePath = closeable.getString(0);
                        if (!FyuseFile.startDeleteFyuseFile(this.mResolver, filePath, closeable.getInt(1))) {
                            GalleryLog.e("VirtualAlbum", "delete fyuse data fail: " + filePath);
                        }
                    }
                }
            }
            this.mResolver.delete(EXTERNAL_FILE_URI, this.mQueryClause, this.mQueryClauseArgs);
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("VirtualAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
        this.mApplication.getDataManager().broadcastLocalDeletion();
        GalleryUtils.startScanFavoriteService(this.mApplication.getAndroidContext());
    }

    protected int enumerateTotalMediaItems(ItemConsumer consumer, int startIndex) {
        if (RecycleUtils.supportRecycle()) {
            return super.enumerateTotalMediaItems(consumer, startIndex);
        }
        return getMediaItemCount();
    }

    private String initQueryClause(String type) {
        this.mQueryClauseArgs = null;
        if ("favorite".equalsIgnoreCase(this.mVirtualType)) {
            FavoriteWhereClause favoriteWhere = this.mFavoriteData.getFavoriteWhereClause();
            if (favoriteWhere == null) {
                return " media_type in (1,3) AND  bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') AND " + WHERECLAUSE_MY_FAVORITE_TYPE + getExtraQueryClause() + getSpecialHideQueryClause();
            }
            this.mQueryClauseArgs = favoriteWhere.mPaths;
            return " media_type in (1,3) AND  bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') AND " + favoriteWhere.mWhereClause + getExtraQueryClause() + getSpecialHideQueryClause();
        } else if ("camera_video".equalsIgnoreCase(this.mVirtualType)) {
            return this.mWhereClauseCameraVideoType + getExtraQueryClause();
        } else {
            if ("screenshots_video".equalsIgnoreCase(this.mVirtualType)) {
                return this.mWhereClauseScreenshotsVideoType + getExtraQueryClause();
            }
            if ("3d_panorama".equalsIgnoreCase(this.mVirtualType)) {
                return this.mWhereClauseCamera3dPanoramaType;
            }
            if ("doc_rectify".equalsIgnoreCase(this.mVirtualType)) {
                return this.mWhereClauseDocRectifyType + getExtraQueryClause();
            }
            if ("3d_model_image".equalsIgnoreCase(this.mVirtualType)) {
                return this.mWhereClause3DModelType + getExtraQueryClause();
            }
            throw new RuntimeException("bad virtual type: " + type);
        }
    }

    private String getExtraQueryClause() {
        switch (this.mLoadType) {
            case 3:
            case 9:
            case 15:
            case 32:
            case AMapException.ERROR_CODE_QUOTA /*35*/:
                return "";
            case 4:
            case 19:
                return WHERECLAUSE_IMAGE_TYPE;
            case 5:
            case 22:
                return WHERECLAUSE_VIDEO_TYPE;
            default:
                throw new RuntimeException("bad reload type: " + this.mLoadType);
        }
    }

    private String getSpecialHideQueryClause() {
        return GalleryUtils.getSpecialHideQueryClause(this.mApplication.getAndroidContext());
    }

    public boolean isVirtual() {
        return true;
    }

    public String getLabel() {
        return this.mVirtualType;
    }
}
