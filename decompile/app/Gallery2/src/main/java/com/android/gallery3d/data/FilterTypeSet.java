package com.android.gallery3d.data;

import com.android.gallery3d.data.MediaSet.ItemConsumer;
import java.util.ArrayList;

public class FilterTypeSet extends MediaSet implements ContentListener {
    private final ArrayList<MediaSet> mAlbums = new ArrayList();
    private final MediaSet mBaseSet;
    private final DataManager mDataManager;
    private final int mMediaType;
    private final ArrayList<Path> mPaths = new ArrayList();

    public FilterTypeSet(Path path, DataManager dataManager, MediaSet baseSet, int mediaType) {
        super(path, -1);
        this.mDataManager = dataManager;
        this.mBaseSet = baseSet;
        this.mMediaType = mediaType;
        this.mBaseSet.addContentListener(this);
    }

    public String getName() {
        return this.mBaseSet.getName();
    }

    public MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    public int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    public int getMediaItemCount() {
        return this.mPaths.size();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return ClusterAlbum.getMediaItemFromPath(this.mPaths, start, count, this.mDataManager);
    }

    public long reload() {
        if (this.mBaseSet.reload() > this.mDataVersion) {
            updateData();
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    private void updateData() {
        int i;
        this.mAlbums.clear();
        String basePath = "/filter/mediatype/" + this.mMediaType;
        int n = this.mBaseSet.getSubMediaSetCount();
        for (i = 0; i < n; i++) {
            MediaSet set = this.mBaseSet.getSubMediaSet(i);
            if (set != null) {
                MediaSet filteredSet = this.mDataManager.getMediaSet(basePath + "/{" + set.getPath().toString() + "}");
                if (filteredSet != null) {
                    filteredSet.reload();
                    if (filteredSet.getMediaItemCount() > 0 || filteredSet.getSubMediaSetCount() > 0) {
                        this.mAlbums.add(filteredSet);
                    }
                }
            }
        }
        this.mPaths.clear();
        final int total = this.mBaseSet.getMediaItemCount();
        final Path[] buf = new Path[total];
        this.mBaseSet.enumerateMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if (item.getMediaType() == FilterTypeSet.this.mMediaType && index >= 0 && index < total) {
                    buf[index] = item.getPath();
                }
            }
        });
        for (i = 0; i < total; i++) {
            if (buf[i] != null) {
                this.mPaths.add(buf[i]);
            }
        }
    }

    public int getSupportedOperations() {
        return 5;
    }

    public void delete() {
        this.mDataManager.mapMediaItems(this.mPaths, new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if ((item.getSupportedOperations() & 1) != 0) {
                    item.delete();
                }
            }
        }, 0);
    }
}
