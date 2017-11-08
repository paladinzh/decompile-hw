package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import java.util.ArrayList;

public class VirtualOtherAlbum extends MediaSet implements ContentListener {
    private static final Path INSIDE_PATH = Path.fromString("/local/all/inside");
    private final GalleryApp mApplication;
    private ArrayList<MediaItem> mCovers = new ArrayList();
    private long mCoversVersion = -1;
    private final MediaSet mSource;

    public VirtualOtherAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mSource = application.getDataManager().getMediaSet(INSIDE_PATH);
        this.mSource.addContentListener(this);
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.other_album);
    }

    public synchronized long reload() {
        if (this.mSource.reload() > this.mDataVersion) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public int getMediaItemCount() {
        return this.mSource.getSubMediaSetCount();
    }

    public String getLabel() {
        return "other";
    }

    public int getSubMediaSetCount() {
        return this.mSource.getSubMediaSetCount();
    }

    public MediaSet getSubMediaSet(int index) {
        return this.mSource.getSubMediaSet(index);
    }

    public int getSupportedOperations() {
        return 1029;
    }

    public boolean isVirtual() {
        return true;
    }

    public synchronized MediaItem[] getMultiCoverMediaItem() {
        if (this.mCoversVersion != this.mDataVersion) {
            int minCount = Math.min(this.mSource.getSubMediaSetCount(), 4);
            ArrayList<MediaItem> covers = new ArrayList();
            for (int i = 0; i < minCount; i++) {
                MediaSet mediaSet = this.mSource.getSubMediaSet(i);
                if (mediaSet != null) {
                    covers.add(mediaSet.getCoverMediaItem());
                }
            }
            this.mCovers.clear();
            this.mCovers.addAll(covers);
            this.mCoversVersion = this.mDataVersion;
        }
        return (MediaItem[]) this.mCovers.toArray(new MediaItem[this.mCovers.size()]);
    }

    public void delete() {
        for (int i = getMediaItemCount() - 1; i >= 0; i--) {
            MediaSet mediaSet = this.mSource.getSubMediaSet(i);
            if (mediaSet != null) {
                mediaSet.delete();
            }
        }
    }
}
