package com.huawei.gallery.burst;

import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.data.AutoLoaderThread;
import java.util.ArrayList;

public class BurstDataLoader {
    private long mDataVersion = -1;
    private final Listener mListener;
    private ReloadTask mReloadTask;
    private final ContentListener mSourceListener = new ContentListener() {
        public void onContentDirty() {
            if (BurstDataLoader.this.mReloadTask != null) {
                BurstDataLoader.this.mReloadTask.notifyDirty();
            }
        }
    };
    private final BurstPhotoSet mSourceSet;

    public interface Listener {
        void onLoadFinished(long j, long j2, ArrayList<MediaItem> arrayList);
    }

    private class ReloadTask extends AutoLoaderThread {
        private ReloadTask() {
        }

        protected void onLoad() {
            long dataVersion = BurstDataLoader.this.mSourceSet.reload();
            BurstDataLoader.this.informListener(BurstDataLoader.this.mDataVersion, dataVersion, BurstDataLoader.this.mSourceSet.getMediaItem(0, BurstDataLoader.this.mSourceSet.getMediaItemCount()));
            BurstDataLoader.this.mDataVersion = dataVersion;
        }
    }

    public BurstDataLoader(BurstPhotoSet sourceSet, Listener listener) {
        this.mSourceSet = sourceSet;
        this.mListener = listener;
    }

    public void resume() {
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
        this.mSourceSet.addContentListener(this.mSourceListener);
    }

    public void pause() {
        if (this.mReloadTask != null) {
            this.mReloadTask.terminate();
            this.mReloadTask = null;
            this.mSourceSet.removeContentListener(this.mSourceListener);
        }
    }

    private void informListener(long oldDataVersion, long newDataVersion, ArrayList<MediaItem> items) {
        this.mListener.onLoadFinished(oldDataVersion, newDataVersion, items);
    }
}
