package com.android.mms.attachment.utils;

import android.os.Handler;
import com.huawei.mms.util.HwBackgroundLoader;

public class ThreadUtil {
    public static Handler getMainThreadHandler() {
        return HwBackgroundLoader.getUIHandler();
    }
}
