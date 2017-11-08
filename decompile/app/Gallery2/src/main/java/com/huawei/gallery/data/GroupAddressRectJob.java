package com.huawei.gallery.data;

import android.graphics.RectF;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.app.AddressAcquire;

public class GroupAddressRectJob extends BaseJob<RectF> {
    private final int mIndex;
    private final AddressAcquire mListener;

    public GroupAddressRectJob(AddressAcquire listener, int index) {
        this.mListener = listener;
        this.mIndex = index;
    }

    public RectF run(JobContext jc) {
        if (jc.isCancelled()) {
            return null;
        }
        return this.mListener.getAddressRect(this.mIndex);
    }

    public String workContent() {
        return "query address rect";
    }
}
