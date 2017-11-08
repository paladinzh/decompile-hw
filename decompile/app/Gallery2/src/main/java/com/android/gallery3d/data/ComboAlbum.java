package com.android.gallery3d.data;

import java.util.ArrayList;

public class ComboAlbum extends MediaSet implements ContentListener {
    private String mName;
    private final MediaSet[] mSets;

    public ComboAlbum(Path path, MediaSet[] mediaSets, String name) {
        super(path, MediaObject.nextVersionNumber());
        this.mSets = mediaSets;
        for (MediaSet set : this.mSets) {
            set.addContentListener(this);
        }
        this.mName = name;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> items = new ArrayList();
        for (MediaSet set : this.mSets) {
            int size = set.getMediaItemCount();
            if (count < 1) {
                break;
            }
            if (start < size) {
                ArrayList<MediaItem> fetchItems = set.getMediaItem(start, start + count <= size ? count : size - start);
                items.addAll(fetchItems);
                count -= fetchItems.size();
                start = 0;
            } else {
                start -= size;
            }
        }
        return items;
    }

    public int getMediaItemCount() {
        int count = 0;
        for (MediaSet set : this.mSets) {
            count += set.getMediaItemCount();
        }
        return count;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public String getName() {
        return this.mName;
    }

    public long reload() {
        boolean changed = false;
        for (MediaSet reload : this.mSets) {
            if (reload.reload() > this.mDataVersion) {
                changed = true;
            }
        }
        if (changed) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public void onContentDirty() {
        notifyContentChanged();
    }
}
