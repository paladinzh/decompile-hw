package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ITimeLatLng.LatitudeLongitude;
import com.android.gallery3d.data.ITimeLatLng.TimeLatLng;
import com.android.gallery3d.data.LocalMediaAlbum.LocalGroupData;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.WhiteList;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class GalleryMediaTimegroupAlbum extends GalleryMediaSetBase implements IGroupAlbum, ITimeLatLng {
    protected static final Uri GALLERY_URI = GalleryMedia.URI;
    protected final String ORDER_BY;
    private final Uri[] WATCH_URIS;
    protected final GalleryApp mApplication;
    protected String mDeleteClause;
    protected String mExcludeHiddenWhereClause = "1 = 1";
    ArrayList<LocalGroupData> mGroupDatas = new ArrayList();
    private TimeGroupAlbumHelper mGroupHelper = new TimeGroupAlbumHelper(this);
    protected boolean mIsCardLocationChanged = false;
    private boolean mIsDataDirty = false;
    private boolean mIsQuickMode = true;
    private boolean mModeChange = false;
    private final ChangeNotifier mNotifier;
    protected String mQueryClause;
    protected String mQueryClauseGroup;
    protected final ContentResolver mResolver;
    protected long mStartTakenTime = 0;
    protected final TimeLatLng mTimeLatLng = new TimeLatLng(this);

    public GalleryMediaTimegroupAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        initWhereClause();
        this.ORDER_BY = getOrderBy();
        this.WATCH_URIS = getWatchUris();
        this.mNotifier = new ChangeNotifier((MediaSet) this, this.WATCH_URIS, application);
    }

    protected String getOrderBy() {
        return "showDateToken DESC, _id DESC";
    }

    protected Uri[] getWatchUris() {
        return new Uri[]{GalleryMedia.URI, Constant.MOVE_OUT_IN_URI, Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI, Constant.SETTIGNS_URI, Files.getContentUri("external")};
    }

    protected void initLocalClause() {
        String str;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        boolean isCloudAutoUploadSwitchOpen = PhotoShareUtils.isCloudPhotoSwitchOpen() ? !PhotoShareUtils.hasNeverSynchronizedCloudData() : false;
        String localBucketSet = "(bucket_id IN (" + (MediaSetUtils.getCameraBucketId() + " , " + storageManager.getOuterGalleryStorageCameraBucketIDs() + " , " + MediaSetUtils.getScreenshotsBucketID() + ", " + storageManager.getOuterGalleryStorageScreenshotsBucketIDs()) + ") AND local_media_id !=-1)";
        String cloudBucketSet = "((bucket_id IN (" + PhotoShareUtils.getAutoUploadBucketIds() + ") AND cloud_media_id = -1) OR " + "cloud_media_id !=-1)";
        StringBuilder append = new StringBuilder().append("substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN (SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE media_type = 1 AND ");
        if (isCloudAutoUploadSwitchOpen) {
            str = cloudBucketSet;
        } else {
            str = localBucketSet;
        }
        String excludeBurstNotCoverInSet = append.append(str).append(" AND ").append(GalleryUtils.getBurstQueryClause()).append(")").toString();
        TimeLatLng timeLatLng = this.mTimeLatLng;
        if (!isCloudAutoUploadSwitchOpen) {
            cloudBucketSet = localBucketSet;
        }
        timeLatLng.initWhereClause(cloudBucketSet, excludeBurstNotCoverInSet);
    }

    protected void initWhereClause() {
        initLocalClause();
        this.mQueryClause = this.mTimeLatLng.mWhereClauseSet;
        this.mDeleteClause = this.mTimeLatLng.mWhereClauseDeleteSet;
        this.mQueryClauseGroup = this.mTimeLatLng.mQueryClauseGroup;
    }

    public void reset() {
        WhiteList.getBucketIdForWhiteListWithoutPreLoadedPath(true);
        initWhereClause();
        this.mIsDataDirty = true;
    }

    public String getName() {
        return null;
    }

    public void setQuickMode(boolean enable) {
        this.mIsDataDirty = this.mIsQuickMode != enable;
        this.mIsQuickMode = enable;
    }

    protected String getQuickClause() {
        if (this.mIsQuickMode) {
            return QUICK_CLAUSE;
        }
        return super.getQuickClause();
    }

    public String getWhereQueryClause() {
        return this.mQueryClause;
    }

    public boolean isQuickMode() {
        return this.mIsQuickMode;
    }

    public String[] getWhereQueryClauseArgs(long startTakenTime) {
        return this.mTimeLatLng.getQueryClauseArgs(startTakenTime);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Uri uri = MediaSet.decorateQueryExternalFileUri(GALLERY_URI, this.ORDER_BY, start, count);
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, PROJECTION, getQueryClauseExtra() + " AND " + getQuickClause() + this.mQueryClause + " AND " + this.mExcludeHiddenWhereClause, this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY);
            if (closeable == null) {
                GalleryLog.w("GalleryMediaTimegroupAlbum", "query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.w("GalleryMediaTimegroupAlbum", "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public void setIdCache(ArrayList<Integer> idCache, int mediaItemsCount) {
        IdCacheQueryImpl.setIdCache(this.mResolver, MediaSet.decorateQueryExternalFileUri(GALLERY_URI, this.ORDER_BY, 0, Math.min(mediaItemsCount, 10000)), getQueryClauseExtra() + " AND " + getQuickClause() + this.mQueryClause + " AND " + this.mExcludeHiddenWhereClause, this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY, idCache, "GalleryMediaTimegroupAlbum");
    }

    public ArrayList<MediaItem> getMediaItemFromCache(ArrayList<Integer> idCache, int start, int count) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(MediaSet.decorateQueryExternalFileUri(GALLERY_URI), PROJECTION, getQueryClauseExtra() + " AND " + IdCacheQueryImpl.translateQueryClause(idCache, start, count), null, null);
            if (closeable == null) {
                GalleryLog.w("GalleryMediaTimegroupAlbum", "query fail");
                printExcuteInfo(startTime, "getMediaItemFromCache");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItemFromCache");
            return IdCacheQueryImpl.checkMediaItemsOrder(list, idCache, start);
        } catch (SecurityException e) {
            GalleryLog.w("GalleryMediaTimegroupAlbum", "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public boolean isIdCacheReady(ArrayList<Integer> idCache, int start, int count) {
        return IdCacheQueryImpl.isIdCacheReady(idCache, start, count);
    }

    public boolean resetIdCache(ArrayList<Integer> idCache) {
        return IdCacheQueryImpl.resetIdCache(idCache);
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1 || this.mCachedVideoCount == -1) {
            this.mGroupDatas = getGroupData(this.mTimeLatLng.getMode());
            this.mCachedCount = this.mGroupHelper.getMediaItemCount(this.mGroupDatas);
            this.mCachedVideoCount = this.mGroupHelper.getVideoItemCount(this.mGroupDatas);
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public long reload() {
        if (needReset()) {
            this.mExcludeHiddenWhereClause = BucketHelper.getExcludeHiddenWhereClause(this.mApplication.getAndroidContext());
            resetData();
        }
        return this.mDataVersion;
    }

    protected boolean needReset() {
        return (this.mNotifier.isDirty() || this.mModeChange || this.mIsCardLocationChanged) ? true : this.mIsDataDirty;
    }

    protected void resetData() {
        this.mModeChange = false;
        this.mIsDataDirty = false;
        this.mIsCardLocationChanged = false;
        this.mDataVersion = MediaObject.nextVersionNumber();
        this.mGroupDatas = new ArrayList();
        invalidCachedCount();
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        try {
            this.mResolver.delete(GALLERY_URI, getQueryClauseExtra() + " AND " + this.mDeleteClause, this.mTimeLatLng.getDeleteClauseArgs(this.mStartTakenTime));
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryMediaTimegroupAlbum");
        }
        GalleryUtils.startScanFavoriteService(this.mApplication.getAndroidContext());
    }

    public boolean isVirtual() {
        GalleryLog.v("GalleryMediaTimegroupAlbum", "is a virtual album");
        return true;
    }

    public boolean isLeafAlbum() {
        GalleryLog.v("GalleryMediaTimegroupAlbum", "is a leaf album");
        return true;
    }

    public void setStartTakenTime(long takenTime) {
        if (this.mStartTakenTime != takenTime) {
            this.mModeChange = true;
            this.mStartTakenTime = takenTime;
        }
    }

    public long getStartTakenTime() {
        return this.mStartTakenTime;
    }

    @SuppressWarnings({"WA_NOT_IN_LOOP", "UW_UNCOND_WAIT"})
    public RectF getRectByDatetaken(long startDate, long endDate) {
        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                synchronized (this) {
                    wait(100);
                }
            }
            return this.mTimeLatLng.getRectByDatetaken(this.mResolver, GALLERY_URI, startDate, endDate);
        } catch (InterruptedException e) {
            GalleryLog.w("GalleryMediaTimegroupAlbum", "query interrupt!");
            return null;
        }
    }

    @SuppressWarnings({"WA_NOT_IN_LOOP", "UW_UNCOND_WAIT"})
    public List<LatitudeLongitude> getLatLongByDatetaken(long startDate, long endDate) {
        try {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                synchronized (this) {
                    wait(120);
                }
            }
        } catch (InterruptedException e) {
            GalleryLog.w("GalleryMediaTimegroupAlbum", "query interrupt!");
        }
        return this.mTimeLatLng.getLatLongByDatetaken(this.mResolver, GALLERY_URI, startDate, endDate, this.ORDER_BY, getQueryClauseExtra());
    }

    public int getBatchSize() {
        return 500;
    }

    public List<MediaItem> getMediaItem(int start, int count, AbsGroupData groupSpec) {
        if (!(groupSpec instanceof LocalGroupData)) {
            return new ArrayList(0);
        }
        LocalGroupData localGroupData = (LocalGroupData) groupSpec;
        return getMediaItem(start, count, localGroupData.startDatetaken, localGroupData.endDatetaken);
    }

    public boolean updateMode(boolean beBiggerView) {
        this.mModeChange = this.mTimeLatLng.updateMode(beBiggerView);
        return this.mModeChange;
    }

    protected String getQueryClauseExtra() {
        return "1 = 1";
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count, long startDate, long endDate) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList();
        if (this.mQueryClauseGroup == null) {
            return list;
        }
        DataManager dataManager = this.mApplication.getDataManager();
        Uri uri = MediaSet.decorateQueryExternalFileUri(GALLERY_URI, this.ORDER_BY, start, count);
        Closeable closeable = null;
        try {
            ContentResolver contentResolver = this.mResolver;
            String[] strArr = PROJECTION;
            String str = getQueryClauseExtra() + " AND " + getQuickClause() + this.mQueryClauseGroup + " AND " + this.mExcludeHiddenWhereClause;
            closeable = contentResolver.query(uri, strArr, str, this.mTimeLatLng.getQueryClauseGroupArgs(this.mStartTakenTime, startDate, endDate), this.ORDER_BY);
            if (closeable == null) {
                GalleryLog.w("GalleryMediaTimegroupAlbum", "gallery media query fail: " + uri);
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            GalleryLog.d("GalleryMediaTimegroupAlbum", "getGroupMediaItem time(ms):" + (System.currentTimeMillis() - startTime));
            return list;
        } catch (SecurityException e) {
            GalleryLog.w("GalleryMediaTimegroupAlbum", "No permission to query gallery media!");
            return list;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public TimeBucketPageViewMode getMode() {
        return this.mTimeLatLng.getMode();
    }

    private ArrayList<LocalGroupData> getGroupData(TimeBucketPageViewMode mode) {
        long startTime = System.currentTimeMillis();
        ArrayList<LocalGroupData> groupDatas = this.mGroupHelper.genGroupData(this.mResolver, GALLERY_URI, this.mGroupHelper.buildGroupDataProjection(mode, "showDateToken"), getQueryClauseExtra() + " AND " + getQuickClause() + this.mQueryClause + " AND " + this.mExcludeHiddenWhereClause + ")" + " GROUP BY (normalized_date", this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY);
        GalleryLog.d("GalleryMediaTimegroupAlbum", "getGroupData time(ms):" + (System.currentTimeMillis() - startTime));
        return groupDatas;
    }

    public ArrayList<AbsGroupData> getGroupData() {
        ArrayList<AbsGroupData> result = new ArrayList();
        result.addAll(this.mGroupDatas);
        return result;
    }

    public static MediaItem[] getMediaItemById(GalleryApp application, ArrayList<Integer> ids) {
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) {
            return result;
        }
        int idLow = ((Integer) ids.get(0)).intValue();
        int idHigh = ((Integer) ids.get(ids.size() - 1)).intValue();
        String[] projection = PROJECTION;
        Uri baseUri = GalleryMedia.URI;
        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Closeable closeable = null;
        try {
            closeable = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?", new String[]{String.valueOf(idLow), String.valueOf(idHigh)}, "_id");
            if (closeable != null) {
                int n = ids.size();
                int i = 0;
                while (i < n && closeable.moveToNext()) {
                    int id = closeable.getInt(0);
                    while (((Integer) ids.get(i)).intValue() < id) {
                        i++;
                        if (i >= n) {
                            return result;
                        }
                    }
                    if (((Integer) ids.get(i)).intValue() <= id) {
                        result[i] = GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, application);
                        i++;
                    }
                }
            }
            Utils.closeSilently(closeable);
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryMediaTimegroupAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
        return result;
    }

    public boolean supportCacheQuery() {
        return true;
    }

    public String getTimeColumnName() {
        return "showDateToken";
    }

    public String getMediaTableName() {
        return "gallery_media";
    }
}
