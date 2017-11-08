package com.huawei.systemmanager.util;

import android.app.Activity;
import android.app.IHwActivitySplitterImpl;
import android.common.HwFrameworkFactory;

public class SplitModeUtil {
    public static boolean isSplitMode(Activity activity) {
        if (activity == null) {
            return false;
        }
        IHwActivitySplitterImpl mIHwActivitySplitterImpl = HwFrameworkFactory.getHwActivitySplitterImpl(activity, true);
        return mIHwActivitySplitterImpl != null && mIHwActivitySplitterImpl.isSplitMode();
    }
}
