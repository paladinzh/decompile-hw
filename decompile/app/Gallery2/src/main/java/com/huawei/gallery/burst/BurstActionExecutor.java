package com.huawei.gallery.burst;

import android.os.Bundle;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.actionbar.Action;
import java.util.ArrayList;

public class BurstActionExecutor {
    private boolean isProcessing = false;
    private final ExecutorListener mListener;
    private Future<?> mTask;

    private class ActionOperation extends BaseJob<Void> {
        private final BurstAction mAction;
        private final Bundle mData;
        private final ArrayList<MediaItem> mItems;

        public ActionOperation(GalleryContext context, Action operation, ArrayList<MediaItem> itemArray, Bundle data) {
            this.mAction = BurstActionFactory.createAction(context, operation);
            this.mItems = itemArray;
            if (data == null) {
                data = new Bundle();
            }
            this.mData = data;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Void run(JobContext jc) {
            int result = 1;
            if (this.mAction.onProgressStart(this.mItems, this.mData, BurstActionExecutor.this.mListener)) {
                int i = 0;
                while (i < this.mItems.size()) {
                    MediaItem item = (MediaItem) this.mItems.get(i);
                    if (jc.isCancelled()) {
                        result = 3;
                        break;
                    }
                    try {
                        if (!this.mAction.execute(item, this.mData, BurstActionExecutor.this.mListener)) {
                            result = 2;
                        }
                        i++;
                    } catch (Throwable th) {
                        this.mAction.onProgressComplete(result, BurstActionExecutor.this.mListener, this.mData);
                        BurstActionExecutor.this.isProcessing = false;
                    }
                }
                this.mAction.onProgressComplete(result, BurstActionExecutor.this.mListener, this.mData);
                BurstActionExecutor.this.isProcessing = false;
                return null;
            }
            throw new RuntimeException("start fail");
        }

        public String workContent() {
            return "work with " + this.mAction.getClass().getName() + " process: " + this.mItems.size() + " items.";
        }
    }

    public interface ExecutorListener {
        void onActionDone(Action action, boolean z, Bundle bundle);
    }

    public BurstActionExecutor(ExecutorListener listener) {
        this.mListener = listener;
    }

    public void startAction(GalleryContext context, Action action, ArrayList<MediaItem> itemArray, Bundle data) {
        this.isProcessing = true;
        stopTask();
        this.mTask = context.getThreadPool().submit(new ActionOperation(context, action, itemArray, data), null);
    }

    private void stopTask() {
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask.waitDone();
            this.mTask = null;
        }
    }

    public boolean isProcessing() {
        return this.isProcessing;
    }
}
