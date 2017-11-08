package com.android.gallery3d.data;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.StitchingChangeListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.util.ArrayList;

public class SecureAlbum extends MediaSet implements StitchingChangeListener {
    private static final String[] PROJECTION = new String[]{"_id"};
    private static final Uri[] mWatchUris = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI};
    private ArrayList<Boolean> mAllItemTypes = new ArrayList();
    private ArrayList<Path> mAllItems = new ArrayList();
    private String mCameraBucketId;
    private Context mContext;
    private DataManager mDataManager;
    private ArrayList<Path> mExistingItems = new ArrayList();
    private int mMaxImageId = Target.SIZE_ORIGINAL;
    private int mMaxVideoId = Target.SIZE_ORIGINAL;
    private int mMinImageId = Integer.MAX_VALUE;
    private int mMinVideoId = Integer.MAX_VALUE;
    private final ChangeNotifier mNotifier;
    private boolean mShowUnlockItem;
    private MediaItem mUnlockItem;

    public SecureAlbum(Path path, GalleryApp application, MediaItem unlock) {
        boolean z = true;
        super(path, MediaObject.nextVersionNumber());
        this.mContext = application.getAndroidContext();
        this.mDataManager = application.getDataManager();
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mUnlockItem = unlock;
        this.mCameraBucketId = getCameraBucketID();
        if (isCameraBucketEmpty(Media.EXTERNAL_CONTENT_URI) && isCameraBucketEmpty(Video.Media.EXTERNAL_CONTENT_URI)) {
            z = false;
        }
        this.mShowUnlockItem = z;
    }

    private String getCameraBucketID() {
        return String.valueOf(Constant.CAMERA_PATH.toLowerCase().hashCode());
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        int existingCount = this.mExistingItems.size();
        if (start >= existingCount + 1) {
            return new ArrayList();
        }
        int end = Math.min(start + count, existingCount);
        final MediaItem[] buf = new MediaItem[(end - start)];
        this.mDataManager.mapMediaItems(new ArrayList(this.mExistingItems.subList(start, end)), new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                buf[index] = item;
            }
        }, 0);
        ArrayList<MediaItem> result = new ArrayList(end - start);
        for (Object add : buf) {
            result.add(add);
        }
        if (this.mShowUnlockItem) {
            result.add(this.mUnlockItem);
        }
        return result;
    }

    public int getMediaItemCount() {
        return (this.mShowUnlockItem ? 1 : 0) + this.mExistingItems.size();
    }

    public String getName() {
        return "secure";
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            updateExistingItems();
        }
        return this.mDataVersion;
    }

    private ArrayList<Integer> queryExistingIds(Uri uri, int minId, int maxId) {
        ArrayList<Integer> ids = new ArrayList();
        if (minId == Integer.MAX_VALUE || maxId == Target.SIZE_ORIGINAL) {
            return ids;
        }
        Closeable closeable = null;
        try {
            closeable = this.mContext.getContentResolver().query(uri, PROJECTION, "_id BETWEEN ? AND ?", new String[]{String.valueOf(minId), String.valueOf(maxId)}, null);
            if (closeable == null) {
                return ids;
            }
            while (closeable.moveToNext()) {
                ids.add(Integer.valueOf(closeable.getInt(0)));
            }
            Utils.closeSilently(closeable);
            return ids;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("SecureAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    @SuppressWarnings({"NP_LOAD_OF_KNOWN_NULL_VALUE"})
    private boolean isCameraBucketEmpty(Uri baseUri) {
        Closeable closeable = null;
        try {
            closeable = this.mContext.getContentResolver().query(baseUri.buildUpon().appendQueryParameter("limit", "1").build(), PROJECTION, "bucket_id = ?", new String[]{String.valueOf(this.mCameraBucketId)}, null);
            if (closeable == null) {
                return true;
            }
            boolean z = closeable.getCount() == 0;
            Utils.closeSilently(closeable);
            return z;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("SecureAlbum");
            return true;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private void updateExistingItems() {
        if (this.mAllItems.size() != 0) {
            ArrayList<Integer> imageIds = queryExistingIds(Media.EXTERNAL_CONTENT_URI, this.mMinImageId, this.mMaxImageId);
            ArrayList<Integer> videoIds = queryExistingIds(Video.Media.EXTERNAL_CONTENT_URI, this.mMinVideoId, this.mMaxVideoId);
            this.mExistingItems.clear();
            for (int i = this.mAllItems.size() - 1; i >= 0; i--) {
                Path path = (Path) this.mAllItems.get(i);
                boolean isVideo = ((Boolean) this.mAllItemTypes.get(i)).booleanValue();
                int id = Integer.parseInt(path.getSuffix());
                if (isVideo) {
                    if (videoIds.contains(Integer.valueOf(id))) {
                        this.mExistingItems.add(path);
                    }
                } else if (imageIds.contains(Integer.valueOf(id))) {
                    this.mExistingItems.add(path);
                }
            }
        }
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
