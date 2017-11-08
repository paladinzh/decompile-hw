package com.android.gallery3d.data;

import android.database.Cursor;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryUtils;

public abstract class GalleryMediaSetBase extends MediaSet {
    static final String[] PROJECTION = GalleryMediaItem.copyProjection();
    private static final int QUERY_COUNT = (GalleryUtils.PRODUCT_LITE ? 100 : 500);
    protected static final String QUICK_CLAUSE = (" _id IN (select _id from gallery_media where media_type IN (1,3) ORDER BY showDateToken DESC limit 0, " + QUERY_COUNT + ")  AND ");
    protected int mCachedCount = -1;
    protected int mCachedVideoCount = -1;

    public GalleryMediaSetBase(Path path, long version) {
        super(path, version);
    }

    protected static MediaItem loadOrUpdateItem(Cursor cursor, DataManager dataManager, GalleryApp app) {
        GalleryMediaItem item;
        synchronized (DataManager.LOCK) {
            boolean isImage = cursor.getInt(24) == 1;
            Path path = (isImage ? GalleryImage.IMAGE_PATH : GalleryVideo.VIDEO_PATH).getChild(cursor.getInt(0));
            item = (GalleryMediaItem) dataManager.peekMediaObject(path);
            if (item != null) {
                item.updateContent(cursor);
            } else if (isImage) {
                item = new GalleryImage(path, app, cursor);
            } else {
                item = new GalleryVideo(path, app, cursor);
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

    protected String getQuickClause() {
        return "";
    }
}
