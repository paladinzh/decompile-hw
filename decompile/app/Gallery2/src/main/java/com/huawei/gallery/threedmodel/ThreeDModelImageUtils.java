package com.huawei.gallery.threedmodel;

import android.os.ServiceManager;
import com.android.gallery3d.util.GalleryLog;

public class ThreeDModelImageUtils {
    private static boolean sIsThreeDModelImageNativeSupport;

    static {
        sIsThreeDModelImageNativeSupport = false;
        if (ServiceManager.getService("com.huawei.servicehost") != null) {
            sIsThreeDModelImageNativeSupport = true;
            return;
        }
        GalleryLog.d("ThreeDModelImageUtils", "get servicehost fail");
        sIsThreeDModelImageNativeSupport = false;
    }

    public static boolean isThreeDModelImageNativeSupport() {
        return sIsThreeDModelImageNativeSupport;
    }

    public static boolean is3DModelImageSpecialFileType(int specialFileType) {
        if (isThreeDModelImageNativeSupport() && specialFileType == 16) {
            return true;
        }
        return false;
    }
}
