package com.android.gallery3d.data;

import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class CloudSwitchHelper {
    private static Boolean mIsCloudAutoUploadSwitchOpen;

    public static synchronized boolean isCloudAutoUploadSwitchOpen() {
        boolean z = false;
        synchronized (CloudSwitchHelper.class) {
            if (mIsCloudAutoUploadSwitchOpen == null) {
                if (PhotoShareUtils.isCloudPhotoSwitchOpen() && !PhotoShareUtils.hasNeverSynchronizedCloudData()) {
                    z = true;
                }
                mIsCloudAutoUploadSwitchOpen = Boolean.valueOf(z);
            }
            z = mIsCloudAutoUploadSwitchOpen.booleanValue();
        }
        return z;
    }

    public static synchronized void resetCloudAutoUploadSwitch() {
        synchronized (CloudSwitchHelper.class) {
            mIsCloudAutoUploadSwitchOpen = null;
        }
    }
}
