package com.huawei.gallery.data;

import android.graphics.RectF;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;

public class AddressRegionLoader implements FutureListener<RectF> {
    private RectF mAddressRegion = null;
    private final AddressRegionListener mListener;
    public final int mSlotIndex;
    private Future<RectF> mTask;

    public interface AddressRegionListener {
        void onAddressRegion(Object obj);

        Future<RectF> submitAddressRectTask(FutureListener<RectF> futureListener, int i);
    }

    public void onFutureDone(Future<RectF> future) {
        synchronized (this) {
            this.mAddressRegion = (RectF) future.get();
        }
        if (this.mListener != null) {
            this.mListener.onAddressRegion(this);
        }
    }

    public AddressRegionLoader(int index, AddressRegionListener listener) {
        this.mSlotIndex = index;
        this.mListener = listener;
    }

    public synchronized RectF get() {
        return this.mAddressRegion;
    }

    public synchronized void startLoad() {
        if (this.mTask == null && this.mListener != null) {
            this.mTask = this.mListener.submitAddressRectTask(this, this.mSlotIndex);
        }
    }

    public synchronized void cancelLoad() {
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
    }
}
