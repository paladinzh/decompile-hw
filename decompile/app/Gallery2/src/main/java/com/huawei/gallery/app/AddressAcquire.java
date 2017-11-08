package com.huawei.gallery.app;

import android.graphics.RectF;
import com.android.gallery3d.util.ThreadPool.JobContext;

public interface AddressAcquire {
    RectF getAddressRect(int i);

    String getAddressString(int i, boolean z, boolean z2, JobContext jobContext);
}
