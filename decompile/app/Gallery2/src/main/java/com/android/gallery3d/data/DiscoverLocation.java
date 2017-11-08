package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.GeoKnowledge;
import com.huawei.gallery.servicemanager.DiscoverLocationNameManager;
import com.huawei.gallery.servicemanager.DiscoverLocationNameManager.DiscoverLocationNameListener;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Locale;

public class DiscoverLocation extends GalleryMediaSetBase implements DiscoverLocationNameListener {
    private static final MyPrinter LOG = new MyPrinter("DiscoverLocation");
    private static Uri[] sWatchUris = new Uri[]{GeoKnowledge.URI, GalleryMedia.URI};
    private GalleryApp mApplication;
    private Uri mBaseUri;
    private String mExcludeHiddenBuckets;
    private String mGeoCode;
    private DiscoverLocationNameManager mGeoNameManager;
    private Handler mHandler;
    private boolean mIsCloudAutoUploadSwitchOpen = false;
    private Runnable mNameFoundRunnable = new NameFoundRunnable();
    private final ChangeNotifier mNotifier;
    private String mOrderClause;
    private final ReloadNotifier mReloadNotifier;
    protected final ContentResolver mResolver;
    private String mWhereClause;

    private class NameFoundRunnable implements Runnable {
        private NameFoundRunnable() {
        }

        public void run() {
            DiscoverLocation.LOG.d("process localized name.");
            DiscoverLocation.this.mApplication.getDataManager().notifyChange(Constant.RELOAD_DISCOVER_LOCATION);
        }
    }

    public DiscoverLocation(Path path, GalleryApp application, String geoCode) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mGeoNameManager = (DiscoverLocationNameManager) application.getAppComponent(DiscoverLocationNameManager.class);
        this.mResolver = application.getContentResolver();
        this.mNotifier = new ChangeNotifier((MediaSet) this, sWatchUris, application);
        this.mBaseUri = GalleryMedia.URI;
        this.mGeoCode = geoCode;
        this.mWhereClause = "geo_code = ? AND " + "substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN (SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE _data LIKE '%BURST____COVER.JPG' )";
        this.mOrderClause = "showDateToken DESC, _id DESC";
        this.mReloadNotifier = new ReloadNotifier(this, Constant.RELOAD_DISCOVER_LOCATION, application);
    }

    public long reload() {
        boolean isCloudChanged = false;
        if (this.mIsCloudAutoUploadSwitchOpen != CloudSwitchHelper.isCloudAutoUploadSwitchOpen()) {
            isCloudChanged = true;
            this.mIsCloudAutoUploadSwitchOpen = CloudSwitchHelper.isCloudAutoUploadSwitchOpen();
        }
        synchronized (this) {
            this.mExcludeHiddenBuckets = BucketHelper.getExcludeHiddenWhereClause(this.mApplication.getAndroidContext());
            boolean dataDirty = this.mNotifier.isDirty() | this.mReloadNotifier.isDirty();
            if (dataDirty || isCloudChanged) {
                if (dataDirty) {
                    this.mCachedCount = -1;
                }
                this.mDataVersion = MediaObject.nextVersionNumber();
            }
        }
        return this.mDataVersion;
    }

    public String getName() {
        return this.mGeoNameManager.getGeoName(this, this.mGeoCode, Locale.getDefault());
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1) {
            Closeable closeable = null;
            try {
                String whereClause = this.mWhereClause + " AND " + this.mExcludeHiddenBuckets;
                if (!this.mIsCloudAutoUploadSwitchOpen) {
                    whereClause = "local_media_id !=-1 AND " + whereClause;
                }
                closeable = this.mResolver.query(this.mBaseUri, new String[]{"count(1)"}, whereClause, new String[]{this.mGeoCode}, null);
                if (closeable == null) {
                    GalleryLog.w("DiscoverLocation", "query fail");
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                GalleryLog.w("DiscoverLocation", "query done: " + this.mCachedCount);
                Utils.closeSilently(closeable);
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("DiscoverLocation");
                return 0;
            } catch (Exception e2) {
                GalleryLog.w("DiscoverLocation", "query count fail." + e2.getMessage());
                return 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
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
            String whereClause = this.mWhereClause + " AND " + this.mExcludeHiddenBuckets;
            if (!this.mIsCloudAutoUploadSwitchOpen) {
                whereClause = "local_media_id !=-1 AND " + whereClause;
            }
            closeable = this.mResolver.query(uri, PROJECTION, whereClause, new String[]{this.mGeoCode}, this.mOrderClause);
            if (closeable == null) {
                GalleryLog.w("DiscoverLocation", "discover query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            GalleryLog.w("DiscoverLocation", "discover query success: " + uri);
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("DiscoverLocation");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public void getLatLong(double[] latlng) {
        MediaItem item = getCoverMediaItem();
        if (item == null) {
            LOG.d(" cover item is null: " + this.mGeoCode);
        } else {
            item.getLatLong(latlng);
        }
    }

    public void onDiscoverLocationNameFound(String geoCode, String language, String geoName) {
        LOG.d("geocode: " + geoCode + ", geoname: " + geoName);
        this.mHandler.post(this.mNameFoundRunnable);
    }
}
