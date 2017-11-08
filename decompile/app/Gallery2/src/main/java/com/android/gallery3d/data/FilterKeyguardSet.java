package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import java.util.ArrayList;

public class FilterKeyguardSet extends MediaSet implements ContentListener {
    private final GalleryApp mApplication;
    private final MediaSet mBaseSet;
    private final ArrayList<Path> mPaths = new ArrayList();

    public FilterKeyguardSet(Path path, GalleryApp application, MediaSet baseSet) {
        super(path, -1);
        this.mApplication = application;
        this.mBaseSet = baseSet;
        this.mBaseSet.addContentListener(this);
    }

    public String getName() {
        return this.mBaseSet.getName();
    }

    public int getMediaItemCount() {
        return this.mPaths.size();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return ClusterAlbum.getMediaItemFromPath(this.mPaths, start, count, this.mApplication.getDataManager());
    }

    public long reload() {
        if (this.mBaseSet.reload() > this.mDataVersion) {
            filterObject();
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    private void filterObject() {
        this.mPaths.clear();
        final int total = this.mBaseSet.getMediaItemCount();
        final Path[] pathBuffer = new Path[total];
        this.mBaseSet.enumerateMediaItems(new ItemConsumer() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void consume(int index, MediaItem item) {
                if (index >= 0 && index < total && !item.isDrm()) {
                    String filePath = item.getFilePath();
                    if (filePath != null) {
                        String comparePath = filePath.toUpperCase();
                        if (comparePath.endsWith(".JPG") || comparePath.endsWith(".JPEG") || comparePath.endsWith(".PNG")) {
                            pathBuffer[index] = item.getPath();
                        }
                    }
                }
            }
        });
        for (int i = 0; i < total; i++) {
            if (pathBuffer[i] != null) {
                this.mPaths.add(pathBuffer[i]);
            }
        }
    }

    public int getBucketId() {
        return this.mBaseSet.getBucketId();
    }

    public String getLabel() {
        return this.mBaseSet.getLabel();
    }

    public void onContentDirty() {
        notifyContentChanged();
    }
}
