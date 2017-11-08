package com.android.gallery3d.data;

import com.android.gallery3d.data.MediaDetails.MediaDetailsListener;
import com.android.gallery3d.util.Future;

public abstract class MediaDetailsTask {
    protected int mIndex;
    protected int mKey;
    protected MediaDetailsListener mListener = null;

    public abstract Object getInitValue();

    public abstract Future<String> submitJob();

    public void setListener(MediaDetailsListener listener, int index, int key) {
        this.mListener = listener;
        this.mIndex = index;
        this.mKey = key;
    }
}
