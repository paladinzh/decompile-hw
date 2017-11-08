package com.huawei.gallery.burst;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ChangeNotifier;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.ReloadNotifier;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.database.CloudRecycleTableOperateHelper;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.io.Closeable;
import java.util.ArrayList;

public class BurstPhotoSet extends MediaSet {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*), sum(_size)"};
    private final GalleryApp mApplication;
    private Uri mBaseUri;
    private final int mBucketId;
    private final String mBurstId;
    private int mCachedCount = -1;
    private long mCachedSize = 0;
    private boolean mIsLocalSource;
    private final String mName;
    private final ChangeNotifier mNotifier;
    private String[] mProjection;
    private int mReloadType = 6;
    private final ReloadNotifier mReloader;
    private final ContentResolver mResolver;
    private boolean mReverse = false;

    public BurstPhotoSet(Path path, GalleryApp application, boolean isLocal, int bucketId, String burstId) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mBucketId = bucketId;
        this.mBurstId = burstId;
        this.mName = burstId + "_BURST";
        initBySourceType(isLocal);
        this.mNotifier = new ChangeNotifier((MediaSet) this, this.mBaseUri, application);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUM, application);
    }

    private void initBySourceType(boolean isLocal) {
        this.mIsLocalSource = isLocal;
        if (this.mIsLocalSource) {
            this.mBaseUri = Media.EXTERNAL_CONTENT_URI;
            this.mProjection = LocalImage.copyProjection();
            return;
        }
        this.mBaseUri = GalleryMedia.URI;
        this.mProjection = GalleryMediaItem.copyProjection();
    }

    public void setOrderClauseReverse(boolean reverse) {
        this.mReverse = reverse;
    }

    public static String[] copyCountProjection() {
        return (String[]) COUNT_PROJECTION.clone();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> list = new ArrayList();
        if (isInvalid()) {
            return list;
        }
        DataManager dataManager = this.mApplication.getDataManager();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(this.mBaseUri.buildUpon().appendQueryParameter("limit", start + "," + count).build(), this.mProjection, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(this.mBucketId), this.mBurstId}, this.mReverse ? "datetaken DESC, title DESC" : "datetaken ASC, title ASC");
            if (closeable == null) {
                return list;
            }
            while (closeable.moveToNext()) {
                MediaItem item;
                int id = closeable.getInt(0);
                if (this.mIsLocalSource) {
                    item = loadOrUpdateLocalBurst(id, closeable, dataManager, this.mApplication);
                } else {
                    item = loadOrUpdateGalleryBurst(id, closeable, dataManager, this.mApplication);
                }
                list.add(item);
            }
            Utils.closeSilently(closeable);
            return list;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("BurstPhotoSet");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private MediaItem loadOrUpdateGalleryBurst(int id, Cursor cursor, DataManager dataManager, GalleryApp app) {
        GalleryMediaItem item;
        Path path = GalleryImage.IMAGE_PATH.getChild(id);
        synchronized (DataManager.LOCK) {
            item = (GalleryMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new GalleryImage(path, app, cursor);
            } else {
                item.updateContent(cursor);
            }
        }
        return item;
    }

    private MediaItem loadOrUpdateLocalBurst(int id, Cursor cursor, DataManager dataManager, GalleryApp app) {
        LocalImage item;
        Path path = LocalImage.ITEM_PATH.getChild(id);
        synchronized (DataManager.LOCK) {
            item = (LocalImage) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new LocalImage(path, app, cursor);
            } else {
                item.updateContent(cursor);
            }
        }
        return item;
    }

    public int getMediaItemCount() {
        if (isInvalid()) {
            return 0;
        }
        if (this.mCachedCount != -1) {
            return this.mCachedCount;
        }
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(this.mBaseUri, COUNT_PROJECTION, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(this.mBucketId), this.mBurstId}, null);
            if (closeable != null) {
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                this.mCachedSize = closeable.getLong(1);
                return this.mCachedCount;
            }
            Utils.closeSilently(closeable);
            return 0;
        } catch (Throwable th) {
            GalleryLog.w("BurstPhotoSet", "query fail." + th.getMessage());
            return 0;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public long getBurstSize() {
        GalleryLog.d("BurstPhotoSet", "itemCount: " + getMediaItemCount() + ", totalSize: " + this.mCachedSize);
        return this.mCachedSize;
    }

    public String getName() {
        return this.mName;
    }

    public long reload() {
        boolean reloadFlag = this.mReloader.isDirty();
        if (reloadFlag) {
            this.mReloadType = this.mReloader.getReloadType();
        }
        if ((this.mNotifier.isDirty() | reloadFlag) != 0) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mCachedCount = -1;
        }
        return this.mDataVersion;
    }

    public int getSupportedOperations() {
        return 1029;
    }

    public void delete() {
        delete(null);
    }

    public void delete(GalleryContext galleryContext) {
        if (!RecycleUtils.supportRecycle() || galleryContext == null) {
            setDeleteFlag();
        } else {
            recycleBurstPhoto(galleryContext);
        }
        try {
            this.mResolver.delete(GalleryMedia.URI, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(this.mBucketId), this.mBurstId});
        } catch (Exception e) {
            GalleryLog.e("photoshareLogTag", "setDeleteFlag delete gallery_media err " + e.toString());
        }
        try {
            this.mResolver.delete(Media.EXTERNAL_CONTENT_URI, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(this.mBucketId), this.mBurstId});
        } catch (SecurityException e2) {
            GalleryLog.noPermissionForMediaProviderLog("BurstPhotoSet");
        }
        if (RecycleUtils.supportRecycle()) {
            CloudRecycleTableOperateHelper.clearCloudBurstPhoto(this.mBucketId, this.mBurstId, this.mApplication.getContentResolver());
        }
        GalleryUtils.startScanFavoriteService(this.mApplication.getAndroidContext());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setDeleteFlag() {
        if (!TextUtils.isEmpty(PhotoShareUtils.getCloudAlbumIdByBucketId(String.valueOf(this.mBucketId)))) {
            Closeable closeable = null;
            Object obj = null;
            try {
                closeable = this.mResolver.query(GalleryMedia.URI, new String[]{"cloud_media_id"}, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' AND cloud_media_id!=-1", new String[]{String.valueOf(this.mBucketId), this.mBurstId}, null);
                StringBuffer sb = new StringBuffer();
                if (closeable != null) {
                    while (closeable.moveToNext()) {
                        sb.append(",").append(closeable.getString(0));
                    }
                }
                if (sb.length() > 0) {
                    obj = sb.substring(1);
                }
                Utils.closeSilently(closeable);
            } catch (Exception e) {
                GalleryLog.e("photoshareLogTag", "queryBurst error " + e.toString());
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
            if (!TextUtils.isEmpty(obj)) {
                try {
                    ContentValues values = new ContentValues();
                    values.put("deleteFlag", Integer.valueOf(1));
                    this.mApplication.getContentResolver().update(PhotoShareConstants.CLOUD_FILE_TABLE_URI, values, "id IN (" + obj + ")", null);
                } catch (Exception e2) {
                    GalleryLog.e("photoshareLogTag", "update cloud file error " + e2.toString());
                }
            }
            if (PhotoShareUtils.getServer() != null) {
                try {
                    PhotoShareUtils.getServer().deleteGeneralFile();
                } catch (RemoteException e3) {
                    PhotoShareUtils.dealRemoteException(e3);
                }
            }
        }
    }

    private void recycleBurstPhoto(GalleryContext galleryContext) {
        SparseIntArray idMediaTypeArray = RecycleUtils.queryMediaIdTypeArray(this.mResolver, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(this.mBucketId), this.mBurstId});
        if (idMediaTypeArray.size() > 0) {
            RecycleUtils.recyclePhotoItem(this.mApplication.getDataManager(), idMediaTypeArray, new MenuExecutor(galleryContext, new SelectionManager(galleryContext, false)));
        }
    }

    public boolean isLeafAlbum() {
        return true;
    }

    private boolean isInvalid() {
        return (this.mReloadType & 2) == 0;
    }
}
