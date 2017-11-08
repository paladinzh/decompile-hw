package com.huawei.gallery.data;

import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.app.AddressAcquire;

public class GroupAddressStringJob extends BaseJob<String> {
    private final boolean mDayView;
    private final int mIndex;
    private final AddressAcquire mListener;

    public GroupAddressStringJob(AddressAcquire listener, int index, boolean dayView) {
        this.mListener = listener;
        this.mIndex = index;
        this.mDayView = dayView;
    }

    public String run(JobContext jc) {
        if (jc.isCancelled()) {
            return null;
        }
        String address = this.mListener.getAddressString(this.mIndex, this.mDayView, false, jc);
        if ("HAS_LOCATION_ITEM".equals(address)) {
            address = null;
        }
        return address;
    }

    public boolean isHeavyJob() {
        return true;
    }

    public String workContent() {
        return "query address string. dayView ? " + this.mDayView;
    }
}
