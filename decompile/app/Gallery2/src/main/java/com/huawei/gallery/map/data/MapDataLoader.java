package com.huawei.gallery.map.data;

import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaSet;
import com.huawei.gallery.data.AutoLoaderThread;
import com.huawei.gallery.map.data.MapAlbumSet.MapDataListener;

public class MapDataLoader {
    private MapDataListener mListener;
    private int mMapButtonType = 1;
    private ReloadTask mReloadTask;
    private final ContentListener mSourceListener = new ContentListener() {
        public void onContentDirty() {
            if (MapDataLoader.this.mReloadTask != null) {
                MapDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    };
    private final MapAlbumSet mSourceSet;

    private class ReloadTask extends AutoLoaderThread {
        private ReloadTask() {
        }

        protected void onLoad() {
            MapDataLoader.this.mSourceSet.setShowArea(MapDataLoader.this.mMapButtonType);
            MapDataLoader.this.mSourceSet.reload();
        }
    }

    public MapDataLoader(MediaSet sourceSet, MapDataListener listener) {
        this.mSourceSet = (MapAlbumSet) sourceSet;
        this.mListener = listener;
    }

    public void resume() {
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
        this.mSourceSet.setDataListener(this.mListener);
        this.mSourceSet.addContentListener(this.mSourceListener);
    }

    public void triggerReload(int mapButtonType) {
        if (this.mReloadTask != null) {
            this.mReloadTask.notifyDirty();
        }
        this.mMapButtonType = mapButtonType;
    }

    public void pause() {
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
            this.mSourceSet.setDataListener(null);
            this.mSourceSet.removeContentListener(this.mSourceListener);
        }
    }
}
