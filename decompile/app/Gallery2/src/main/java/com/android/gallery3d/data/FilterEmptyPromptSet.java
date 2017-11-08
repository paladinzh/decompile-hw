package com.android.gallery3d.data;

import java.util.ArrayList;

public class FilterEmptyPromptSet extends MediaSet implements ContentListener {
    private MediaSet mBaseSet;
    private ArrayList<MediaItem> mEmptyItem = new ArrayList(1);

    public FilterEmptyPromptSet(Path path, MediaSet baseSet, MediaItem emptyItem) {
        super(path, -1);
        this.mEmptyItem.add(emptyItem);
        this.mBaseSet = baseSet;
        this.mBaseSet.addContentListener(this);
    }

    public int getMediaItemCount() {
        int itemCount = this.mBaseSet.getMediaItemCount();
        if (itemCount > 0) {
            return itemCount;
        }
        return 1;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        if (this.mBaseSet.getMediaItemCount() > 0) {
            return this.mBaseSet.getMediaItem(start, count);
        }
        if (start == 0 && count == 1) {
            return this.mEmptyItem;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public long reload() {
        return this.mBaseSet.reload();
    }

    public String getName() {
        return this.mBaseSet.getName();
    }
}
