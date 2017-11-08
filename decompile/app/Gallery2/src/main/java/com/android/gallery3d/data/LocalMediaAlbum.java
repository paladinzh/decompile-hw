package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ITimeLatLng.LatitudeLongitude;
import com.android.gallery3d.data.ITimeLatLng.TimeLatLng;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.data.AbsGroupData;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public abstract class LocalMediaAlbum extends LocalMergeQuerySet implements ITimeLatLng, IGroupAlbum {
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private static final Uri[] WATCH_URIS = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI, Constant.MOVE_OUT_IN_URI};
    private final String ORDER_BY;
    protected final GalleryApp mApplication;
    protected String mDeleteClause;
    ArrayList<LocalGroupData> mGroupDatas = new ArrayList();
    private TimeGroupAlbumHelper mGroupHelper = new TimeGroupAlbumHelper(this);
    protected boolean mIsCardLocationChanged = false;
    protected boolean mIsDataDirty = false;
    private boolean mModeChange = false;
    private final ChangeNotifier mNotifier;
    protected String mQueryClause;
    protected String mQueryClauseGroup;
    protected final ContentResolver mResolver;
    private long mStartTakenTime = 0;
    protected TimeLatLng mTimeLatLng = new TimeLatLng(this);

    public static class LocalGroupData extends AbsGroupData {
        public String endDate;
        public long endDatetaken;
        public String startDate;
        public long startDatetaken;
        public int videoCount;
    }

    protected abstract void initWhereClause();

    public LocalMediaAlbum(Path path, long version, GalleryApp application) {
        super(path, version);
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        initWhereClause();
        this.mNotifier = new ChangeNotifier((MediaSet) this, WATCH_URIS, application);
        this.ORDER_BY = "datetaken DESC, _id DESC";
    }

    protected String getQuickClause() {
        return "";
    }

    public String getWhereQueryClause() {
        return this.mQueryClause;
    }

    public String[] getWhereQueryClauseArgs(long startTakenTime) {
        return this.mTimeLatLng.getQueryClauseArgs(startTakenTime);
    }

    public String getTimeColumnName() {
        return "datetaken";
    }

    public String getMediaTableName() {
        return "files";
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Uri uri = MediaSet.decorateQueryExternalFileUri(EXTERNAL_FILE_URI, this.ORDER_BY, start, count);
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, PROJECTION, this.mQueryClause, this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY);
            if (closeable == null) {
                GalleryLog.w("LocalMediaAlbum", "query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.w("LocalMediaAlbum", "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public void setIdCache(ArrayList<Integer> idCache, int mediaItemsCount) {
        IdCacheQueryImpl.setIdCache(this.mResolver, MediaSet.decorateQueryExternalFileUri(EXTERNAL_FILE_URI, this.ORDER_BY, 0, Math.min(mediaItemsCount, 10000)), this.mQueryClause, this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY, idCache, "LocalMediaAlbum");
    }

    public ArrayList<MediaItem> getMediaItemFromCache(ArrayList<Integer> idCache, int start, int count) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(MediaSet.decorateQueryExternalFileUri(EXTERNAL_FILE_URI), PROJECTION, IdCacheQueryImpl.translateQueryClause(idCache, start, count), null, null);
            if (closeable == null) {
                GalleryLog.w("LocalMediaAlbum", "query fail");
                printExcuteInfo(startTime, "getMediaItemFromCache");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItemFromCache");
            return IdCacheQueryImpl.checkMediaItemsOrder(list, idCache, start);
        } catch (SecurityException e) {
            GalleryLog.w("LocalMediaAlbum", "No permission to query!");
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
            resetData();
        }
        return this.mDataVersion;
    }

    protected boolean needReset() {
        return (this.mNotifier.isDirty() || this.mModeChange || this.mIsCardLocationChanged) ? true : this.mIsDataDirty;
    }

    protected void resetData() {
        this.mModeChange = false;
        this.mIsCardLocationChanged = false;
        this.mIsDataDirty = false;
        this.mDataVersion = MediaObject.nextVersionNumber();
        this.mGroupDatas = new ArrayList();
        invalidCachedCount();
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        try {
            this.mResolver.delete(EXTERNAL_FILE_URI, this.mDeleteClause, this.mTimeLatLng.getDeleteClauseArgs(this.mStartTakenTime));
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalMediaAlbum");
        }
        GalleryUtils.startScanFavoriteService(this.mApplication.getAndroidContext());
    }

    public boolean isVirtual() {
        return true;
    }

    public boolean isLeafAlbum() {
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

    public RectF getRectByDatetaken(long startDate, long endDate) {
        return this.mTimeLatLng.getRectByDatetaken(this.mResolver, EXTERNAL_FILE_URI, startDate, endDate);
    }

    public List<LatitudeLongitude> getLatLongByDatetaken(long startDate, long endDate) {
        return this.mTimeLatLng.getLatLongByDatetaken(this.mResolver, EXTERNAL_FILE_URI, startDate, endDate, this.ORDER_BY);
    }

    public int getBatchSize() {
        return 500;
    }

    public List<MediaItem> getMediaItem(int start, int count, AbsGroupData groupSpec) {
        if (!(groupSpec instanceof LocalGroupData)) {
            return new ArrayList(0);
        }
        LocalGroupData groupData = (LocalGroupData) groupSpec;
        return getMediaItem(start, count, groupData.startDatetaken, groupData.endDatetaken);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count, long startDate, long endDate) {
        GalleryUtils.assertNotInRenderThread();
        ArrayList<MediaItem> list = new ArrayList();
        if (this.mQueryClauseGroup == null) {
            return list;
        }
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        Uri uri = MediaSet.decorateQueryExternalFileUri(EXTERNAL_FILE_URI, this.ORDER_BY, start, count);
        Closeable closeable = null;
        try {
            ContentResolver contentResolver = this.mResolver;
            String[] strArr = PROJECTION;
            closeable = contentResolver.query(uri, strArr, this.mQueryClauseGroup, this.mTimeLatLng.getQueryClauseGroupArgs(this.mStartTakenTime, startDate, endDate), this.ORDER_BY);
            if (closeable == null) {
                GalleryLog.w("LocalMediaAlbum", "query fail: " + uri);
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            GalleryLog.d("LocalMediaAlbum", "getGroupMediaItem time(ms):" + (System.currentTimeMillis() - startTime));
            return list;
        } catch (SecurityException e) {
            GalleryLog.w("LocalMediaAlbum", "No permission to query!");
            return list;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public TimeBucketPageViewMode getMode() {
        return this.mTimeLatLng.getMode();
    }

    public boolean updateMode(boolean beBiggerView) {
        this.mModeChange = this.mTimeLatLng.updateMode(beBiggerView);
        return this.mModeChange;
    }

    private ArrayList<LocalGroupData> getGroupData(TimeBucketPageViewMode mode) {
        long startTime = System.currentTimeMillis();
        ArrayList<LocalGroupData> groupDatas = this.mGroupHelper.genGroupData(this.mResolver, EXTERNAL_FILE_URI, this.mGroupHelper.buildGroupDataProjection(mode, "datetaken"), getQuickClause() + this.mQueryClause + ")" + " GROUP BY (normalized_date", this.mTimeLatLng.getQueryClauseArgs(this.mStartTakenTime), this.ORDER_BY);
        GalleryLog.d("LocalMediaAlbum", "getGroupData time(ms):" + (System.currentTimeMillis() - startTime));
        return groupDatas;
    }

    public ArrayList<AbsGroupData> getGroupData() {
        ArrayList<AbsGroupData> result = new ArrayList();
        result.addAll(this.mGroupDatas);
        return result;
    }
}
