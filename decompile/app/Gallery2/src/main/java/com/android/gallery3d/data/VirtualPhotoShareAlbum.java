package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import java.util.ArrayList;

public class VirtualPhotoShareAlbum extends MediaSet implements ContentListener {
    public static final Path PHOTOSHARE_PATH = Path.fromString("/photoshare/local");
    private final GalleryApp mApplication;
    private ArrayList<MediaItem> mCovers = new ArrayList();
    private long mCoversVersion = -1;
    private final MediaSet mSource;

    public VirtualPhotoShareAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mSource = application.getDataManager().getMediaSet(PHOTOSHARE_PATH);
        this.mSource.addContentListener(this);
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.hicloud_gallery_new);
    }

    public synchronized long reload() {
        if (this.mSource.reload() > this.mDataVersion) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public int getMediaItemCount() {
        return this.mSource.getSubMediaSetCount();
    }

    public int getSubMediaSetCount() {
        return this.mSource.getSubMediaSetCount();
    }

    public MediaSet getSubMediaSet(int index) {
        return this.mSource.getSubMediaSet(index);
    }

    public String getLabel() {
        return "photoshare";
    }

    public int getSupportedOperations() {
        return 0;
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

    public boolean isVirtual() {
        return true;
    }

    public void delete() {
        for (int i = getMediaItemCount() - 1; i >= 0; i--) {
            MediaSet mediaSet = this.mSource.getSubMediaSet(i);
            if (mediaSet != null) {
                mediaSet.delete();
            }
        }
    }

    public int getTotalVideoCount() {
        return this.mSource.getTotalVideoCount();
    }

    public int getTotalMediaItemCount() {
        return this.mSource.getTotalMediaItemCount();
    }
}
