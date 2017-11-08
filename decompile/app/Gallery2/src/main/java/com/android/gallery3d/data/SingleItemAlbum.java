package com.android.gallery3d.data;

import java.util.ArrayList;

public class SingleItemAlbum extends MediaSet {
    private final MediaItem mItem;
    private final String mName = ("SingleItemAlbum(" + this.mItem.getClass().getSimpleName() + ")");

    public SingleItemAlbum(Path path, MediaItem item) {
        super(path, MediaObject.nextVersionNumber());
        this.mItem = item;
    }

    public int getMediaItemCount() {
        return 1;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> result = new ArrayList();
        if (start <= 0 && start + count > 0) {
            result.add(this.mItem);
        }
        return result;
    }

    public MediaItem getItem() {
        return this.mItem;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public String getName() {
        return this.mName;
    }

    public long reload() {
        return this.mDataVersion;
    }
}
