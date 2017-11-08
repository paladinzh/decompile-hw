package com.huawei.gallery.servicemanager;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BucketHelper;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.util.HashSet;
import java.util.WeakHashMap;

public class CloudManager {
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsCloudAutoUploadSwitchOpen = false;
    private boolean mIsUploading = false;
    private WeakHashMap<UploadListener, Object> mUploadListener = new WeakHashMap(3);

    public interface UploadListener {
        void onStatusChanged(CloudManager cloudManager, int i);
    }

    private static class MediaItemStr {
        String imageStr;
        String videoStr;

        private MediaItemStr() {
            this.imageStr = null;
            this.videoStr = null;
        }
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            CloudManager.this.notifyUploadStatus(0);
        }
    }

    public CloudManager(Application context) {
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("notify thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        context.getContentResolver().registerContentObserver(GalleryMedia.URI, false, new MyContentObserver(this.mHandler));
    }

    public void registerUploadListener(UploadListener listener) {
        this.mUploadListener.put(listener, null);
    }

    public void forceRefresh() {
        notifyUploadStatus(0);
    }

    public void notifyUploadStatus(int type) {
        for (UploadListener l : new HashSet(this.mUploadListener.keySet())) {
            l.onStatusChanged(this, 0);
        }
    }

    public boolean isUploading() {
        return this.mIsUploading;
    }

    public boolean isCloudAutoUploadSwitchOpen() {
        return this.mIsCloudAutoUploadSwitchOpen;
    }

    public String getUploadingString() {
        String excludeHiddenWhereClause;
        String str;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        boolean z = PhotoShareUtils.isCloudPhotoSwitchOpen() ? !PhotoShareUtils.hasNeverSynchronizedCloudData() : false;
        this.mIsCloudAutoUploadSwitchOpen = z;
        if (this.mIsCloudAutoUploadSwitchOpen) {
            excludeHiddenWhereClause = BucketHelper.getExcludeHiddenWhereClause(this.mContext);
        } else {
            excludeHiddenWhereClause = "1 = 1";
        }
        String localBucketIdLimit = "bucket_id IN (" + (MediaSetUtils.getCameraBucketId() + " , " + storageManager.getOuterGalleryStorageCameraBucketIDs() + " , " + MediaSetUtils.getScreenshotsBucketID() + ", " + storageManager.getOuterGalleryStorageScreenshotsBucketIDs()) + ") AND local_media_id != -1";
        String cloudBucketIdLimit = "((bucket_id IN (" + PhotoShareUtils.getAutoUploadBucketIds() + ") AND cloud_media_id = -1) OR " + "cloud_media_id !=-1) AND " + excludeHiddenWhereClause;
        String queryUploadingBucketIdLimit = "bucket_id IN (" + PhotoShareUtils.getAutoUploadBucketIds() + ") AND cloud_media_id = -1 AND relative_cloud_media_id = -1 AND " + excludeHiddenWhereClause;
        StringBuilder append = new StringBuilder().append("substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN (SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE media_type = 1 AND ");
        if (this.mIsCloudAutoUploadSwitchOpen) {
            str = cloudBucketIdLimit;
        } else {
            str = localBucketIdLimit;
        }
        String excludeBurstNotCoverInSet = append.append(str).append(" AND ").append(GalleryUtils.getBurstQueryClause()).append(")").toString();
        String queryUploadingSelection = queryUploadingBucketIdLimit + ") GROUP BY (media_type";
        String localSelection = localBucketIdLimit + " AND " + excludeBurstNotCoverInSet + " ) GROUP BY (media_type";
        String cloudSelection = cloudBucketIdLimit + " AND " + excludeBurstNotCoverInSet + " ) GROUP BY (media_type";
        String[] projection = new String[]{"media_type ", " count(1) item_count"};
        Resources r = this.mContext.getResources();
        String format = null;
        if (PhotoShareUtils.getLogOnAccount() != null && this.mIsCloudAutoUploadSwitchOpen) {
            format = getCloudUploadString(r, projection, queryUploadingSelection);
        }
        if (format == null) {
            this.mIsUploading = false;
            if (this.mIsCloudAutoUploadSwitchOpen) {
                return getLocalMediaString(r, projection, cloudSelection);
            }
            return getLocalMediaString(r, projection, localSelection);
        }
        this.mIsUploading = true;
        return format;
    }

    public String getLocalMediaString(Resources r, String[] projection, String selection) {
        MediaItemStr itemStr = queryMediaItemCount(r, projection, selection);
        String imageStr = itemStr.imageStr;
        String videoStr = itemStr.videoStr;
        String format = "";
        if (imageStr != null && videoStr != null) {
            return r.getString(R.string.local_media_message_two_type, new Object[]{imageStr, videoStr});
        } else if (imageStr != null) {
            return r.getString(R.string.local_media_message_one_type, new Object[]{imageStr});
        } else if (videoStr == null) {
            return format;
        } else {
            return r.getString(R.string.local_media_message_one_type, new Object[]{videoStr});
        }
    }

    public String getCloudUploadString(Resources r, String[] projection, String selection) {
        MediaItemStr itemStr = queryMediaItemCount(r, projection, selection);
        String imageStr = itemStr.imageStr;
        String videoStr = itemStr.videoStr;
        boolean isWifiConnected = PhotoShareUtils.isWifiConnected(this.mContext);
        if (imageStr == null || videoStr == null) {
            if (imageStr != null) {
                if (isWifiConnected) {
                    return r.getString(R.string.cloud_upload_message_one_type, new Object[]{imageStr});
                }
                return r.getString(R.string.cloud_wait_upload_message_one_type, new Object[]{imageStr});
            } else if (videoStr == null) {
                return null;
            } else {
                if (isWifiConnected) {
                    return r.getString(R.string.cloud_upload_message_one_type, new Object[]{videoStr});
                }
                return r.getString(R.string.cloud_wait_upload_message_one_type, new Object[]{videoStr});
            }
        } else if (isWifiConnected) {
            return r.getString(R.string.cloud_upload_message_two_type, new Object[]{imageStr, videoStr});
        } else {
            return r.getString(R.string.cloud_wait_upload_message_two_type, new Object[]{imageStr, videoStr});
        }
    }

    private MediaItemStr queryMediaItemCount(Resources r, String[] projection, String selection) {
        Closeable closeable = null;
        MediaItemStr result = new MediaItemStr();
        int imgCount = 0;
        int videoCount = 0;
        try {
            closeable = this.mContext.getContentResolver().query(GalleryMedia.URI, projection, selection, null, null);
            if (closeable == null) {
                return result;
            }
            while (closeable.moveToNext()) {
                int type = closeable.getInt(0);
                int count = closeable.getInt(1);
                if (type == 1) {
                    imgCount = count;
                } else if (type == 3) {
                    videoCount = count;
                }
            }
            if (imgCount > 0) {
                result.imageStr = r.getQuantityString(R.plurals.message_type_image, imgCount, new Object[]{Integer.valueOf(imgCount)});
            }
            if (videoCount > 0) {
                result.videoStr = r.getQuantityString(R.plurals.message_type_video, videoCount, new Object[]{Integer.valueOf(videoCount)});
            }
            Utils.closeSilently(closeable);
            return result;
        } finally {
            Utils.closeSilently(closeable);
        }
    }
}
