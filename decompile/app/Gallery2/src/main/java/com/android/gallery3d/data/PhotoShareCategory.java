package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoShareCategory extends MediaSet implements ContentListener {
    private static final MyPrinter LOG = new MyPrinter("PhotoShareCategory");
    private final List<MediaSet> mAlbums;
    private GalleryApp mApplication;
    private DataManager mManager;
    private final String[] mSets;
    private Map<String, MediaSet> mSubMediaSets = new HashMap(10);

    public PhotoShareCategory(Path path, GalleryApp application, String[] mediaSets) {
        super(path, MediaObject.nextVersionNumber());
        this.mManager = application.getDataManager();
        try {
            this.mSets = (String[]) mediaSets.clone();
            for (String set : this.mSets) {
                getMediaSet(set);
            }
            this.mAlbums = new ArrayList(this.mSets.length);
            this.mApplication = application;
        } catch (Exception e) {
            path.clearObject();
            LOG.e("init media set failed. " + (mediaSets == null ? "mediaSets is null" : ""));
            throw new RuntimeException("init media set failed .", e);
        }
    }

    private MediaSet getMediaSet(String path) {
        MediaSet set = (MediaSet) this.mSubMediaSets.get(path);
        if (set == null) {
            set = this.mManager.getMediaSet(path);
            if (set != null) {
                this.mSubMediaSets.put(path, set);
                set.addContentListener(this);
            }
        }
        return set;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MediaSet getSubMediaSet(int index) {
        synchronized (this.mAlbums) {
            if (index >= 0) {
                if (index < this.mAlbums.size()) {
                    MediaSet mediaSet = (MediaSet) this.mAlbums.get(index);
                    return mediaSet;
                }
            }
        }
    }

    public int getSubMediaSetCount() {
        int size;
        synchronized (this.mAlbums) {
            size = this.mAlbums.size();
        }
        return size;
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.set_label_category);
    }

    public boolean isLoading() {
        for (String mediaSet : this.mSets) {
            MediaSet set = getMediaSet(mediaSet);
            if (set != null && set.isLoading()) {
                return true;
            }
        }
        return false;
    }

    public long reload() {
        boolean changed = false;
        for (String mediaSet : this.mSets) {
            MediaSet mediaset = getMediaSet(mediaSet);
            if ((mediaset != null ? mediaset.reload() : 0) > this.mDataVersion) {
                changed = true;
            }
        }
        if (changed) {
            synchronized (this.mAlbums) {
                this.mAlbums.clear();
                for (String path : this.mSets) {
                    mediaset = getMediaSet(path);
                    if (mediaset != null) {
                        int itemCount = mediaset.getTotalMediaItemCount();
                        LOG.d(" reload " + mediaset.getPath() + ", mediaItemCount: " + itemCount);
                        if (itemCount > 0) {
                            this.mAlbums.add(mediaset);
                        }
                    }
                }
            }
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public void onContentDirty() {
        LOG.d("onContentDirty");
        notifyContentChanged();
    }
}
