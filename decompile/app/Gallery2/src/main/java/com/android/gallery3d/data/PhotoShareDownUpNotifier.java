package com.android.gallery3d.data;

import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotoShareDownUpNotifier {
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);
    private final MediaSet mMediaSet;

    public PhotoShareDownUpNotifier(MediaSet mediaSet) {
        this.mMediaSet = mediaSet;
        PhotoShareUtils.registerDownUp(this);
    }

    public void onChange() {
        if (this.mContentDirty.compareAndSet(false, true)) {
            this.mMediaSet.notifyContentChanged();
        }
    }

    public boolean isDirty() {
        return this.mContentDirty.compareAndSet(true, false);
    }
}
