package com.android.gallery3d.data;

import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool.JobContext;

public class MediaDetailsSetSizeTask extends MediaDetailsTask {
    private final GalleryContext mActivity;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final SelectionManager mSelectionManager;

    private class SetSizeJob extends BaseJob<String> {
        private SetSizeJob() {
        }

        public String run(JobContext jc) {
            return Formatter.formatFileSize(MediaDetailsSetSizeTask.this.mActivity.getActivityContext(), MediaDetailsSetSizeTask.this.getMultiSelectedDetails(jc));
        }

        public boolean isHeavyJob() {
            return true;
        }

        public String workContent() {
            return "set size (especially burst)";
        }
    }

    private class SetSizeListener implements FutureListener<String> {
        private SetSizeListener() {
        }

        public void onFutureDone(Future<String> future) {
            handleInMain((String) future.get());
        }

        private void handleInMain(final String size) {
            MediaDetailsSetSizeTask.this.mHandler.post(new Runnable() {
                public void run() {
                    if (MediaDetailsSetSizeTask.this.mListener != null) {
                        MediaDetailsSetSizeTask.this.mListener.onDetailsChange(MediaDetailsSetSizeTask.this.mIndex, MediaDetailsSetSizeTask.this.mKey, size);
                    }
                }
            });
        }
    }

    public MediaDetailsSetSizeTask(GalleryContext context, SelectionManager selectionManager) {
        this.mActivity = context;
        this.mSelectionManager = selectionManager;
    }

    public Future<String> submitJob() {
        return this.mActivity.getThreadPool().submit(new SetSizeJob(), new SetSizeListener());
    }

    private long getMultiSelectedDetails(JobContext jc) {
        long sum = 0;
        for (Path path : this.mSelectionManager.getSelected(false, jc)) {
            if (jc.isCancelled()) {
                return 0;
            }
            MediaItem item = (MediaItem) this.mActivity.getDataManager().getMediaObject(path);
            if (item != null) {
                sum += item.getSize();
            }
        }
        return sum;
    }

    public Object getInitValue() {
        return null;
    }
}
