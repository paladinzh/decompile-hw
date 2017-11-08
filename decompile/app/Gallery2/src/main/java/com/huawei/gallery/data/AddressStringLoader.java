package com.huawei.gallery.data;

import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;

public class AddressStringLoader implements FutureListener<String> {
    private String mAddress = null;
    private final AddressStringListener mListener;
    public final int mSlotIndex;
    private Future<String> mTask;

    public interface AddressStringListener {
        void onAddressString(Object obj);

        Future<String> submitAddressStringTask(FutureListener<String> futureListener, int i);
    }

    public void onFutureDone(Future<String> future) {
        synchronized (this) {
            this.mAddress = (String) future.get();
        }
        if (this.mListener != null) {
            this.mListener.onAddressString(this);
        }
    }

    public AddressStringLoader(int index, AddressStringListener listener) {
        this.mSlotIndex = index;
        this.mListener = listener;
    }

    public synchronized String get() {
        return this.mAddress;
    }

    public synchronized void startLoad() {
        if (this.mTask == null && this.mListener != null) {
            this.mTask = this.mListener.submitAddressStringTask(this, this.mSlotIndex);
        }
    }

    public synchronized void cancelLoad() {
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
    }
}
