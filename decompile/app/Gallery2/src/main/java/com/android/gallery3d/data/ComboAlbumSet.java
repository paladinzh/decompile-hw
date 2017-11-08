package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;

public class ComboAlbumSet extends MediaSet implements ContentListener {
    private final String mName;
    private final MediaSet[] mSets;

    public ComboAlbumSet(Path path, GalleryApp application, MediaSet[] mediaSets) {
        super(path, MediaObject.nextVersionNumber());
        this.mSets = mediaSets;
        for (MediaSet set : this.mSets) {
            set.addContentListener(this);
        }
        this.mName = application.getResources().getString(R.string.set_label_all_albums);
    }

    public MediaSet getSubMediaSet(int index) {
        for (MediaSet set : this.mSets) {
            int size = set.getSubMediaSetCount();
            if (index < size) {
                return set.getSubMediaSet(index);
            }
            index -= size;
        }
        return null;
    }

    public int getSubMediaSetCount() {
        int count = 0;
        for (MediaSet set : this.mSets) {
            count += set.getSubMediaSetCount();
        }
        return count;
    }

    public String getName() {
        return this.mName;
    }

    public boolean isLoading() {
        for (MediaSet isLoading : this.mSets) {
            if (isLoading.isLoading()) {
                return true;
            }
        }
        return false;
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
