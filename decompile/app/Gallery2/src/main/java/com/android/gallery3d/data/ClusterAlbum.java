package com.android.gallery3d.data;

import com.android.gallery3d.data.MediaSet.ItemConsumer;
import java.util.ArrayList;

public class ClusterAlbum extends MediaSet implements ContentListener {
    private MediaSet mClusterAlbumSet;
    private MediaItem mCover;
    private DataManager mDataManager;
    private String mName = "";
    private ArrayList<Path> mPaths = new ArrayList();

    public ClusterAlbum(Path path, DataManager dataManager, MediaSet clusterAlbumSet) {
        super(path, MediaObject.nextVersionNumber());
        this.mDataManager = dataManager;
        this.mClusterAlbumSet = clusterAlbumSet;
        this.mClusterAlbumSet.addContentListener(this);
    }

    public void setCoverMediaItem(MediaItem cover) {
        this.mCover = cover;
    }

    public MediaItem getCoverMediaItem() {
        return this.mCover != null ? this.mCover : super.getCoverMediaItem();
    }

    void setMediaItems(ArrayList<Path> paths) {
        this.mPaths = paths;
        this.mDataVersion = MediaObject.nextVersionNumber();
    }

    void addDataVersion() {
        this.mDataVersion = MediaObject.nextVersionNumber();
    }

    ArrayList<Path> getMediaItems() {
        return this.mPaths;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public int getMediaItemCount() {
        return this.mPaths.size();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return getMediaItemFromPath(this.mPaths, start, count, this.mDataManager);
    }

    public static ArrayList<MediaItem> getMediaItemFromPath(ArrayList<Path> paths, int start, int count, DataManager dataManager) {
        if (start >= paths.size()) {
            return new ArrayList();
        }
        int end = Math.min(start + count, paths.size());
        final MediaItem[] buf = new MediaItem[(end - start)];
        dataManager.mapMediaItems(new ArrayList(paths.subList(start, end)), new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                buf[index] = item;
            }
        }, 0);
        ArrayList<MediaItem> result = new ArrayList(end - start);
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] != null) {
                result.add(buf[i]);
            }
        }
        return result;
    }

    protected int enumerateMediaItems(ItemConsumer consumer, int startIndex) {
        this.mDataManager.mapMediaItems(this.mPaths, consumer, startIndex);
        return this.mPaths.size();
    }

    public int getTotalMediaItemCount() {
        return this.mPaths.size();
    }

    public long reload() {
        if (this.mClusterAlbumSet.reload() > this.mDataVersion) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public void onContentDirty() {
        notifyContentChanged();
    }

    public int getSupportedOperations() {
        return 1029;
    }

    public void delete() {
        this.mDataManager.mapMediaItems(this.mPaths, new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                if (item != null && (item.getSupportedOperations() & 1) != 0) {
                    synchronized (DataManager.LOCK) {
                        item.delete();
                    }
                }
            }
        }, 0);
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
