package com.android.settingslib.drawer;

import android.app.Activity;
import android.app.IHwActivitySplitterImpl;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Intent;
import com.android.settingslib.Utils;
import java.util.HashSet;

public class SplitUtils {
    private static HashSet<ComponentName> mUnSplitSet = new HashSet();

    static {
        mUnSplitSet.add(new ComponentName("com.google.android.gms", "com.google.android.gms.app.settings.GoogleSettingsLink"));
        mUnSplitSet.add(new ComponentName("com.huawei.hwid", "com.huawei.hwid.ui.extend.setting.StartUpGuideLoginActivity"));
    }

    public static boolean notSupportSplit(Intent intent) {
        if (intent == null || mUnSplitSet.size() == 0) {
            return false;
        }
        ComponentName comp = intent.getComponent();
        if (comp != null) {
            return mUnSplitSet.contains(comp);
        }
        return false;
    }

    public static boolean reachSplitSize(Activity activity) {
        if (activity == null) {
            return false;
        }
        IHwActivitySplitterImpl iHwActivitySplitterImpl = getIHwActivitySplitterImpl(activity, true);
        if (iHwActivitySplitterImpl != null) {
            return iHwActivitySplitterImpl.reachSplitSize();
        }
        return false;
    }

    public static void setTargetIntent(Intent intent, Activity activity) {
        if (intent != null && activity != null) {
            IHwActivitySplitterImpl iHwActivitySplitterImpl = getIHwActivitySplitterImpl(activity);
            if (iHwActivitySplitterImpl != null) {
                iHwActivitySplitterImpl.setTargetIntent(intent);
            }
        }
    }

    public static void cancelSplit(Intent intent, Activity activity) {
        if (intent != null && activity != null) {
            IHwActivitySplitterImpl iHwActivitySplitterImpl = getIHwActivitySplitterImpl(activity);
            if (iHwActivitySplitterImpl != null) {
                iHwActivitySplitterImpl.cancelSplit(intent);
            }
        }
    }

    public static boolean isSplitMode(Activity activity) {
        if (activity == null) {
            return false;
        }
        IHwActivitySplitterImpl iHwActivitySplitterImpl = getIHwActivitySplitterImpl(activity);
        if (iHwActivitySplitterImpl != null) {
            return iHwActivitySplitterImpl.isSplitMode();
        }
        return false;
    }

    public static boolean handleMultiWindowMode(Activity activity) {
        if (activity == null || !activity.isInMultiWindowMode()) {
            return false;
        }
        IHwActivitySplitterImpl iHwActivitySplitterImpl = getIHwActivitySplitterImpl(activity);
        if (iHwActivitySplitterImpl == null) {
            return false;
        }
        if (!iHwActivitySplitterImpl.reachSplitSize()) {
            iHwActivitySplitterImpl.finishAllSubActivities();
            iHwActivitySplitterImpl.reduceIndexView();
        }
        return true;
    }

    protected static boolean isBaseActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean z;
        if (activity.getClass().getName().equals("com.android.settings.HWSettings")) {
            z = true;
        } else {
            z = activity.getClass().getName().equals("com.android.settings.Settings");
        }
        return z;
    }

    protected static IHwActivitySplitterImpl getIHwActivitySplitterImpl(Activity activity) {
        if (activity == null || !Utils.IS_TABLET) {
            return null;
        }
        return HwFrameworkFactory.getHwActivitySplitterImpl(activity, isBaseActivity(activity));
    }

    protected static IHwActivitySplitterImpl getIHwActivitySplitterImpl(Activity activity, boolean base) {
        if (activity == null || !Utils.IS_TABLET) {
            return null;
        }
        return HwFrameworkFactory.getHwActivitySplitterImpl(activity, !base ? isBaseActivity(activity) : true);
    }
}
