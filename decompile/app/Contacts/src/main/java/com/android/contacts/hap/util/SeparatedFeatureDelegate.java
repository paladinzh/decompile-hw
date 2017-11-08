package com.android.contacts.hap.util;

import android.content.Context;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.utils.BackgroundGenricHandler;

public class SeparatedFeatureDelegate {
    private static Context mContext;
    private static volatile Boolean mIsInstalled = null;

    public static boolean isInstalled(Context context) {
        if (mIsInstalled == null) {
            if (context == null) {
                return false;
            }
            mIsInstalled = Boolean.valueOf(RefelctionUtils.isLibraryInstalled(context, "com.android.contacts.separated"));
        }
        return mIsInstalled.booleanValue();
    }

    public static void initAsync(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
            BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
                public void run() {
                    if (SeparatedFeatureDelegate.isInstalled(SeparatedFeatureDelegate.mContext) && EmuiFeatureManager.isChinaArea()) {
                        RefelctionUtils.invokeMethodFromDex(null, SeparatedFeatureDelegate.mContext, "com.android.contacts.update.UpdateHelper", "init", new Class[]{Context.class}, null, new Object[]{SeparatedFeatureDelegate.mContext});
                    }
                }
            }, 1000);
        }
    }
}
