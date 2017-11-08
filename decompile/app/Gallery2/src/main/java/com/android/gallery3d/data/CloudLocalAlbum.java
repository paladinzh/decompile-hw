package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.SparseArray;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import java.io.Closeable;
import java.util.ArrayList;

public class CloudLocalAlbum extends GalleryMediaSetBase {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};
    private static final int QQ_ALBUM_PATH_BUCKET_ID = GalleryUtils.getBucketId("/tencent/QQ_Images");
    private static final int WEIBO_ALBUM_PATH_BUCKET_ID = GalleryUtils.getBucketId("/sina/weibo/weibo");
    private static final int WEIXIN_ALBUM_PATH_BUCKET_ID = GalleryUtils.getBucketId("/tencent/MicroMsg/WeiXin");
    private static final String WHERE_CLAUSE_BUCKET_BURST = ("cloud_bucket_id = ? AND substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN ( SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE " + GalleryUtils.getBurstQueryClause() + ")");
    private static final SparseArray<String> sThirdAutoUploadAlbumPath = new SparseArray();
    private String mAlbumId;
    private PhotoShareAlbumInfo mAlbumInfo;
    private int mAlbumType;
    protected final GalleryApp mApplication;
    protected Uri mBaseUri = GalleryMedia.URI;
    private String mName;
    private final ChangeNotifier mNotifier;
    private String mRelativePath;
    private int mResId;
    protected final ContentResolver mResolver;

    static {
        sThirdAutoUploadAlbumPath.put(QQ_ALBUM_PATH_BUCKET_ID, "default-album-101");
        sThirdAutoUploadAlbumPath.put(WEIXIN_ALBUM_PATH_BUCKET_ID, "default-album-102");
        sThirdAutoUploadAlbumPath.put(WEIBO_ALBUM_PATH_BUCKET_ID, "default-album-103");
    }

    public CloudLocalAlbum(Path path, GalleryApp application, String albumId, String name, String relativePath) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mAlbumId = albumId;
        if ("default-album-1".equals(this.mAlbumId) || "default-album-2".equals(this.mAlbumId)) {
            this.mAlbumType = 1;
        } else {
            this.mAlbumType = 10;
        }
        this.mName = name;
        this.mRelativePath = relativePath;
        this.mResId = CloudTableOperateHelper.getResId(this.mRelativePath);
        this.mAlbumInfo = new PhotoShareLocalAlbumInfo(this.mAlbumId, this.mName);
        this.mNotifier = new ChangeNotifier((MediaSet) this, GalleryMedia.URI, application);
    }

    public static String getThirdAutoUploadAlbumId(int bucketId) {
        return (String) sThirdAutoUploadAlbumPath.get(bucketId);
    }

    public void setRelativePath(String relativePath) {
        this.mRelativePath = relativePath;
        this.mResId = CloudTableOperateHelper.getResId(this.mRelativePath);
    }

    public void setName(String name) {
        this.mName = name;
    }

    public PhotoShareAlbumInfo getAlbumInfo() {
        return this.mAlbumInfo;
    }

    public int getAlbumType() {
        return this.mAlbumType;
    }

    public int getMediaItemCount() {
        Closeable closeable;
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1) {
            closeable = null;
            try {
                closeable = this.mResolver.query(this.mBaseUri, COUNT_PROJECTION, WHERE_CLAUSE_BUCKET_BURST, new String[]{String.valueOf(this.mAlbumId)}, null);
                if (closeable == null) {
                    GalleryLog.w("CloudLocalAlbum", "query fail");
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                GalleryLog.w("CloudLocalAlbum", "query success " + this.mCachedCount);
                Utils.closeSilently(closeable);
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("CloudLocalAlbum");
                return 0;
            } catch (Exception e2) {
                GalleryLog.w("CloudLocalAlbum", "quey cloud local count fail." + e2.getMessage());
                return 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        if (this.mCachedVideoCount == -1) {
            closeable = null;
            try {
                closeable = this.mResolver.query(this.mBaseUri, COUNT_PROJECTION, "media_type=3 AND cloud_bucket_id= ?", new String[]{String.valueOf(this.mAlbumId)}, null);
                if (closeable == null) {
                    GalleryLog.w("CloudLocalAlbum", "query fail");
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedVideoCount = closeable.getInt(0);
                GalleryLog.w("CloudLocalAlbum", "query count success. " + this.mCachedVideoCount);
                Utils.closeSilently(closeable);
            } catch (SecurityException e3) {
                GalleryLog.noPermissionForMediaProviderLog("CloudLocalAlbum");
                GalleryLog.w("CloudLocalAlbum", "fail to move to next." + e3.getMessage());
                return 0;
            } catch (Exception e22) {
                GalleryLog.w("CloudLocalAlbum", "move to next fail." + e22.getMessage());
                return 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        GalleryLog.v("CloudLocalAlbum", "query success");
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        Uri uri = this.mBaseUri.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, GalleryMediaItem.copyProjection(), WHERE_CLAUSE_BUCKET_BURST, new String[]{String.valueOf(this.mAlbumId)}, "showDateToken DESC, _display_name DESC");
            if (closeable == null) {
                GalleryLog.w("CloudLocalAlbum", "cloud local query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            GalleryLog.w("CloudLocalAlbum", "cloud local query success: " + uri);
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("CloudLocalAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            invalidCachedCount();
        }
        return this.mDataVersion;
    }

    public String getName() {
        if (this.mResId != -1) {
            return this.mApplication.getResources().getString(this.mResId);
        }
        return this.mName;
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
