package com.android.gallery3d.data;

import android.database.Cursor;
import android.util.SparseArray;
import com.android.gallery3d.app.GalleryApp;
import java.util.ArrayList;

public abstract class LocalMergeQuerySet extends MediaSet {
    private static final SparseArray<Integer> LOCAL_VIDEO_COLUMN_INDEX_MAP = new SparseArray();
    private static final int MEDIA_TYPE_INDEX = getColumnIndex(PROJECTION, "media_type");
    static final String[] PROJECTION = getImageAndVideoMergeProjection();
    protected int mCachedCount = -1;
    protected int mCachedVideoCount = -1;

    static {
        LocalVideo.initColumnIndexMap(LOCAL_VIDEO_COLUMN_INDEX_MAP, PROJECTION);
    }

    public LocalMergeQuerySet(Path path, long version) {
        super(path, version);
    }

    private static String[] getImageAndVideoMergeProjection() {
        int i = 0;
        ArrayList<String> mergeProjection = new ArrayList();
        for (String column : LocalImage.PROJECTION) {
            String column2;
            mergeProjection.add(column2);
        }
        String[] strArr = LocalVideo.PROJECTION;
        int length = strArr.length;
        while (i < length) {
            column2 = strArr[i];
            if (!mergeProjection.contains(column2)) {
                mergeProjection.add(column2);
            }
            i++;
        }
        if (!mergeProjection.contains("media_type")) {
            mergeProjection.add("media_type");
        }
        return (String[]) mergeProjection.toArray(new String[mergeProjection.size()]);
    }

    private static int getColumnIndex(String[] projection, String column) {
        for (int index = 0; index < projection.length; index++) {
            if (projection[index].equalsIgnoreCase(column)) {
                return index;
            }
        }
        return -1;
    }

    protected static MediaItem loadOrUpdateItem(Cursor cursor, DataManager dataManager, GalleryApp app) {
        LocalMediaItem item;
        synchronized (DataManager.LOCK) {
            boolean isImage = cursor.getInt(MEDIA_TYPE_INDEX) == 1;
            Path path = (isImage ? LocalImage.ITEM_PATH : LocalVideo.ITEM_PATH).getChild(cursor.getInt(0));
            item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item == null) {
                item = isImage ? new LocalImage(path, app, cursor) : new LocalVideo(path, app, cursor, true, LOCAL_VIDEO_COLUMN_INDEX_MAP);
            } else if (isImage) {
                item.updateContent(cursor);
            } else {
                ((LocalVideo) item).updateContent(cursor, true, LOCAL_VIDEO_COLUMN_INDEX_MAP);
            }
        }
        return item;
    }

    protected void invalidCachedCount() {
        this.mCachedCount = -1;
        this.mCachedVideoCount = -1;
    }

    public int getTotalVideoCount() {
        if (this.mCachedVideoCount == -1) {
            getMediaItemCount();
        }
        return this.mCachedVideoCount;
    }
}
