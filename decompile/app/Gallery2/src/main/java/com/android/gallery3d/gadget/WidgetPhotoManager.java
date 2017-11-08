package com.android.gallery3d.gadget;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class WidgetPhotoManager {
    private static final int ALBUM_BUCKET_INDEX = 6;
    private static final int ALBUM_NAME_INDEX = 5;
    private static final int ID_INDEX = 4;
    private static final int MINITYPE_INDEX = 2;
    private static final int MSG_ALL_DATABASE = 49;
    private static final int MSG_DESTORY_DATA = 52;
    private static final int MSG_QUERY_DATABASE = 50;
    private static final int MSG_QUERY_OVER = 51;
    private static final int MSG_UNREGEDIT_OBSERVER = 53;
    private static final String ORDERCLAUSE = "datetaken DESC, _id DESC";
    private static final int ORIENTATION_INDEX = 3;
    private static final int PATH_INDEX = 0;
    private static final String[] PROJECTION = new String[]{"_data", "_size", "mime_type", "orientation", "_id", "bucket_display_name", "bucket_id"};
    private static final int SIZE_INDEX = 1;
    private static final String TAG = "WidgetPhotoManager";
    private static final String WHERECLAUSE_EXCLUDE_HIDDEN = " AND bucket_id NOT IN (SELECT bucket_id FROM files WHERE title='.hidden') ";
    private static WidgetPhotoManager sPhotoManager;
    private List<AttributeEntry> mAttributeEntries;
    private ContentObserver mContentObserver;
    private ContentResolver mContentResolver;
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String bucketId = msg.obj;
            switch (msg.what) {
                case 49:
                    synchronized (WidgetPhotoManager.this) {
                        for (int i = 0; i < WidgetPhotoManager.this.mObserverList.size(); i++) {
                            Message message = Message.obtain();
                            message.what = 50;
                            message.obj = ((PhotoObserver) WidgetPhotoManager.this.mObserverList.get(i)).getBucketId();
                            message.arg1 = ((PhotoObserver) WidgetPhotoManager.this.mObserverList.get(i)).getUnitId();
                            sendMessageDelayed(message, 2000);
                        }
                    }
                    break;
                case 50:
                    GalleryLog.e(WidgetPhotoManager.TAG, "WidgetPhotoManager mQueryTask.execute(bucketId);");
                    WidgetPhotoManager.this.mQueryTask = new QueryTask(bucketId, msg.arg1);
                    WidgetPhotoManager.this.mQueryTask.execute(new String[]{bucketId});
                    break;
                case WidgetPhotoManager.MSG_QUERY_OVER /*51*/:
                    WidgetPhotoManager.this.onChange(bucketId, msg.arg1);
                    break;
                case WidgetPhotoManager.MSG_DESTORY_DATA /*52*/:
                    WidgetPhotoManager.this.clearData();
                    break;
                case WidgetPhotoManager.MSG_UNREGEDIT_OBSERVER /*53*/:
                    WidgetPhotoManager.this.unregeditObserver();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private boolean mHasRegedit = false;
    private ArrayList<PhotoObserver> mObserverList = new ArrayList();
    private QueryTask mQueryTask;
    private HashMap<Integer, List<PhotoInfo>> mUnitIdMapPhoto = new HashMap();

    public static class PhotoInfo {
        public String mAlbumName;
        public int mBucketId;
        public int mId;
        public String mMimetype;
        public int mOrientation;
        public String mUri;

        public PhotoInfo(String uri, String type, int size, int orientation, int id, String name, int bucketId) {
            this.mUri = uri;
            this.mMimetype = type;
            this.mOrientation = orientation;
            this.mId = id;
            this.mAlbumName = name;
            this.mBucketId = bucketId;
        }
    }

    public interface PhotoObserver {
        String getBucketId();

        int getUnitId();

        void onPhotoChange(int i);
    }

    private class QueryTask extends AsyncTask<String, Integer, List<PhotoInfo>> {
        public String mBucketId = null;
        public int mUnitId = 0;

        public QueryTask(String bucketId, int unitId) {
            this.mBucketId = bucketId;
            this.mUnitId = unitId;
        }

        protected List<PhotoInfo> doInBackground(String... params) {
            if (params == null || params.length == 0) {
                return new ArrayList();
            }
            return WidgetPhotoManager.this.queryDatabase(this.mBucketId, this.mUnitId);
        }

        protected void onPostExecute(List<PhotoInfo> result) {
            synchronized (WidgetPhotoManager.getInstance()) {
                WidgetPhotoManager.this.mUnitIdMapPhoto.remove(Integer.valueOf(this.mUnitId));
                WidgetPhotoManager.this.mUnitIdMapPhoto.put(Integer.valueOf(this.mUnitId), result);
            }
            GalleryLog.e(WidgetPhotoManager.TAG, "WidgetPhotoManager onPostExecute bucketId =" + this.mBucketId);
            Message msg = Message.obtain();
            msg.what = WidgetPhotoManager.MSG_QUERY_OVER;
            msg.obj = this.mBucketId;
            msg.arg1 = this.mUnitId;
            WidgetPhotoManager.this.mHandler.sendMessage(msg);
            super.onPostExecute(result);
        }
    }

    public static synchronized WidgetPhotoManager getInstance() {
        WidgetPhotoManager widgetPhotoManager;
        synchronized (WidgetPhotoManager.class) {
            if (sPhotoManager == null) {
                sPhotoManager = new WidgetPhotoManager();
            }
            widgetPhotoManager = sPhotoManager;
        }
        return widgetPhotoManager;
    }

    private WidgetPhotoManager() {
    }

    private synchronized void onChange(String bucketId, int unitId) {
        if (bucketId != null) {
            List<PhotoInfo> photoInfos = (List) this.mUnitIdMapPhoto.get(Integer.valueOf(unitId));
            for (int i = 0; i < this.mObserverList.size(); i++) {
                PhotoObserver observer = (PhotoObserver) this.mObserverList.get(i);
                if (bucketId.equals(observer.getBucketId()) && unitId == observer.getUnitId()) {
                    observer.onPhotoChange(photoInfos == null ? 0 : photoInfos.size());
                }
            }
        }
    }

    public synchronized PhotoInfo getPhotoInfo(String bucketId, int unitId, int position) {
        if (position < 0) {
            return null;
        }
        List<PhotoInfo> photoInfos = (List) this.mUnitIdMapPhoto.get(Integer.valueOf(unitId));
        if (photoInfos == null || photoInfos.size() == 0) {
            Message msg = Message.obtain();
            msg.what = 50;
            msg.obj = bucketId;
            msg.arg1 = unitId;
            this.mHandler.sendMessage(msg);
            return null;
        } else if (position >= photoInfos.size()) {
            return null;
        } else {
            return (PhotoInfo) photoInfos.get(position);
        }
    }

    private synchronized void clearData() {
        if (this.mUnitIdMapPhoto.size() != 0) {
            int i;
            ArrayList<Integer> unitIds = new ArrayList();
            for (i = 0; i < this.mObserverList.size(); i++) {
                PhotoObserver observer = (PhotoObserver) this.mObserverList.get(i);
                if (!unitIds.contains(Integer.valueOf(observer.getUnitId()))) {
                    unitIds.add(Integer.valueOf(observer.getUnitId()));
                }
            }
            Set<Integer> keySet = this.mUnitIdMapPhoto.keySet();
            ArrayList<Integer> deleteKeys = new ArrayList();
            for (Integer key : keySet) {
                if (!(key == null || unitIds.contains(key))) {
                    deleteKeys.add(key);
                }
            }
            for (i = 0; i < deleteKeys.size(); i++) {
                this.mUnitIdMapPhoto.remove(deleteKeys.get(i));
            }
        }
    }

    private String getCameraBucketId() {
        synchronized (this) {
            Context context = this.mContext;
        }
        if (context != null) {
            GalleryUtils.initializeStorageVolume(context);
        }
        return "bucket_id IN (" + MediaSetUtils.getCameraBucketId() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs() + ") ";
    }

    private String getScreenshotsBucketId() {
        synchronized (this) {
            Context context = this.mContext;
        }
        if (context != null) {
            GalleryUtils.initializeStorageVolume(context);
        }
        return "bucket_id IN (" + MediaSetUtils.getScreenshotsBucketID() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs() + ") ";
    }

    private List<PhotoInfo> queryDatabase(String bucket_id, int unitId) {
        Closeable closeable = null;
        String BUCKET_CAMERA = getCameraBucketId();
        String screenshotsBucket = getScreenshotsBucketId();
        String EXCLUDE_BURST_NOT_COVER = "substr(_data, 1, length(_data) - length('000.JPG')) NOT IN (SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM images WHERE " + GalleryUtils.getBurstQueryClause() + ")";
        String WHERE_CLAUSE_CAMERA_BURST = BUCKET_CAMERA + " AND " + EXCLUDE_BURST_NOT_COVER;
        String WHERE_CLAUSE_BUCKET_BURST = "bucket_id = ? AND " + EXCLUDE_BURST_NOT_COVER;
        String screenshotsWhereClause = screenshotsBucket + " AND " + EXCLUDE_BURST_NOT_COVER;
        List<PhotoInfo> result = new ArrayList();
        if (bucket_id == null) {
            return result;
        }
        String selection = WHERE_CLAUSE_BUCKET_BURST;
        String[] selectArgs = new String[]{bucket_id};
        if (bucket_id.contains("/")) {
            selection = "bucket_id = ? AND _display_name = ?";
            selectArgs = bucket_id.split("/");
        }
        selection = selection + WHERECLAUSE_EXCLUDE_HIDDEN + getSpecialHideQueryClause();
        try {
            synchronized (getInstance()) {
                if (this.mContext != null) {
                    GalleryUtils.initializeStorageVolume(this.mContext);
                    GalleryUtils.initializeScreenshotsRecoder(this.mContext);
                    if (bucket_id.equalsIgnoreCase(String.valueOf(MediaSetUtils.getCameraBucketId())) || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(bucket_id)) {
                        closeable = this.mContext.getContentResolver().query(WidgetPhotoView.ROOT_URI, PROJECTION, WHERE_CLAUSE_CAMERA_BURST, null, ORDERCLAUSE);
                    } else {
                        if (GalleryUtils.isScreenRecorderExist()) {
                            if (bucket_id.equalsIgnoreCase(String.valueOf(MediaSetUtils.getScreenshotsBucketID())) || GalleryStorageManager.getInstance().isOuterGalleryStorageScreenshotsBucketID(bucket_id)) {
                                closeable = this.mContext.getContentResolver().query(WidgetPhotoView.ROOT_URI, PROJECTION, screenshotsWhereClause, null, ORDERCLAUSE);
                            }
                        }
                        closeable = this.mContext.getContentResolver().query(WidgetPhotoView.ROOT_URI, PROJECTION, selection, selectArgs, ORDERCLAUSE);
                    }
                }
            }
            if (closeable == null) {
                Utils.closeSilently(closeable);
                return result;
            }
            result.addAll(getPhotoInfoList(closeable));
            Utils.closeSilently(closeable);
            return result;
        } catch (Exception e) {
            try {
                GalleryLog.i(TAG, "Catch an exception in queryDatabase() method." + e.getMessage());
            } finally {
                Utils.closeSilently(closeable);
            }
        }
    }

    private ArrayList<PhotoInfo> getPhotoInfoList(Cursor cursor) {
        ArrayList<PhotoInfo> result = new ArrayList();
        while (cursor.moveToNext()) {
            String uri = cursor.getString(0);
            String mimeType = cursor.getString(2);
            int fileSize = cursor.getInt(1);
            int orientation = cursor.getInt(3);
            int id = cursor.getInt(4);
            String name = cursor.getString(5);
            int bucketId = cursor.getInt(6);
            if (!DrmUtils.isDrmFile(uri) || DrmUtils.isDrmEnabled()) {
                result.add(new PhotoInfo(uri, mimeType, fileSize, orientation, id, name, bucketId));
            }
        }
        return result;
    }

    private void registerObserver(Context context) {
        if (!this.mHasRegedit) {
            this.mContentObserver = new ContentObserver(null) {
                public void onChange(boolean selfChange) {
                    GalleryLog.e(WidgetPhotoManager.TAG, "WidgetPhotoManager mContentObserver onChange");
                    WidgetPhotoManager.this.mHandler.removeMessages(49);
                    WidgetPhotoManager.this.mHandler.sendEmptyMessageDelayed(49, 2000);
                    super.onChange(selfChange);
                }
            };
            this.mContentResolver = context.getContentResolver();
            this.mContentResolver.registerContentObserver(WidgetPhotoView.ROOT_URI, true, this.mContentObserver);
            this.mHasRegedit = true;
        }
    }

    public synchronized void changePhotoObserver(Context context, int unitId, PhotoObserver observer) {
        regeditPhotoObserver(context, unitId, observer);
        this.mHandler.sendEmptyMessageDelayed(MSG_DESTORY_DATA, 15000);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void regeditPhotoObserver(Context context, int unitId, PhotoObserver observer) {
        if (!(context == null || observer == null)) {
            if (observer.getBucketId() != null) {
                if (this.mHandler.hasMessages(MSG_UNREGEDIT_OBSERVER)) {
                    this.mHandler.removeMessages(MSG_UNREGEDIT_OBSERVER);
                }
                if (this.mHandler.hasMessages(MSG_DESTORY_DATA)) {
                    this.mHandler.removeMessages(MSG_DESTORY_DATA);
                }
                if (!this.mObserverList.contains(observer)) {
                    this.mObserverList.add(observer);
                }
                if (this.mObserverList.size() == 1) {
                    registerObserver(context);
                    this.mContext = context;
                }
                initAlbumNameLayout(context);
                String bucketId = observer.getBucketId();
                Message msg = Message.obtain();
                msg.what = 50;
                msg.obj = bucketId;
                msg.arg1 = unitId;
                this.mHandler.sendMessageDelayed(msg, 100);
            }
        }
    }

    public synchronized void unregeditPhotoObserver(Context context, PhotoObserver observer) {
        if (this.mObserverList.contains(observer)) {
            this.mObserverList.remove(observer);
        }
        this.mHandler.sendEmptyMessageDelayed(MSG_UNREGEDIT_OBSERVER, 15000);
        this.mHandler.sendEmptyMessageDelayed(MSG_DESTORY_DATA, 15000);
    }

    private synchronized void unregeditObserver() {
        if (this.mObserverList.size() == 0 && this.mHasRegedit) {
            if (!(this.mContentResolver == null || this.mContentObserver == null)) {
                this.mContentResolver.unregisterContentObserver(this.mContentObserver);
                this.mHasRegedit = false;
                this.mContentResolver = null;
                this.mContentObserver = null;
                this.mContext = null;
            }
            if (this.mQueryTask != null) {
                this.mQueryTask.cancel(true);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void initAlbumNameLayout(Context context) {
        if (context != null) {
            this.mAttributeEntries = XmlUtils.parserXml("/data/skin/", "gallery3d_widget_layout.xml");
            if (this.mAttributeEntries == null || this.mAttributeEntries.size() == 0) {
                this.mAttributeEntries = XmlUtils.parserXml(context.getResources().getXml(R.xml.gallery3d_widget_layout));
            }
        }
    }

    public synchronized AttributeEntry getAlbumNameAttributeEntries(Context context) {
        if (this.mAttributeEntries == null || this.mAttributeEntries.size() == 0) {
            initAlbumNameLayout(context);
        }
        for (int i = 0; i < this.mAttributeEntries.size(); i++) {
            AttributeEntry entry = (AttributeEntry) this.mAttributeEntries.get(i);
            if (!TextUtils.isEmpty(entry.getId()) && "widget_album_name_2*1".equalsIgnoreCase(entry.getId())) {
                return entry;
            }
        }
        return null;
    }

    public synchronized AttributeEntry getAlbumCoverAttributeEntries(Context context) {
        if (this.mAttributeEntries == null || this.mAttributeEntries.size() == 0) {
            initAlbumNameLayout(context);
        }
        for (int i = 0; i < this.mAttributeEntries.size(); i++) {
            AttributeEntry entry = (AttributeEntry) this.mAttributeEntries.get(i);
            if (!TextUtils.isEmpty(entry.getId()) && "widget_album_cover_2*1".equalsIgnoreCase(entry.getId())) {
                return entry;
            }
        }
        return null;
    }

    private String getSpecialHideQueryClause() {
        if (this.mContext == null) {
            return "";
        }
        return GalleryUtils.getSpecialHideQueryClause(getGalleryContext(this.mContext));
    }

    @SuppressLint({"WorldReadableFiles"})
    private static Context getGalleryContext(Context context) {
        try {
            return context.createPackageContext(HicloudAccountManager.PACKAGE_NAME, 2);
        } catch (Throwable th) {
            GalleryLog.d(TAG, "Error when createPackageContext." + th.getMessage());
            return null;
        }
    }
}
