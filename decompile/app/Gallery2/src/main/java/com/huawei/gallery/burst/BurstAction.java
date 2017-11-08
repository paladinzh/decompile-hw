package com.huawei.gallery.burst;

import android.os.Bundle;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.burst.BurstActionExecutor.ExecutorListener;
import java.util.ArrayList;

public class BurstAction {
    protected final Action mAction;
    protected final GalleryContext mContext;

    public BurstAction(GalleryContext context, Action action) {
        this.mContext = context;
        this.mAction = action;
    }

    public boolean onProgressStart(ArrayList<MediaItem> arrayList, Bundle data, ExecutorListener listener) {
        return true;
    }

    public boolean execute(MediaItem item, Bundle data, ExecutorListener listener) {
        return true;
    }

    public void onProgressComplete(int result, ExecutorListener listener, Bundle data) {
        boolean z = true;
        if (listener != null) {
            Action action = this.mAction;
            if (result != 1) {
                z = false;
            }
            listener.onActionDone(action, z, data);
        }
    }
}
