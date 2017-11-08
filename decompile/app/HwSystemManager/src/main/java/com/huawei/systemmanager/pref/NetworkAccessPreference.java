package com.huawei.systemmanager.pref;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.rainbow.CloudClientOperation;

public class NetworkAccessPreference {
    private static final String IS_ACCESS_DIALOG_OPEN = "network_access_dialog_should_open";
    public static final String SP_NETWORK_ACCESS = "network_access";
    public static final String STORAGE_MANAGER_NETWORK_ACCESS = "storage_manager_network_access";
    protected static final String TAG = "NetworkAccessPreference";

    public static boolean isNetworkAccessDialogShow() {
        return GlobalContext.getContext().getSharedPreferences(SP_NETWORK_ACCESS, 0).getBoolean(IS_ACCESS_DIALOG_OPEN, true);
    }

    public static void setNetworkAccessDialogShow(boolean shouldShow) {
        Editor editor = GlobalContext.getContext().getSharedPreferences(SP_NETWORK_ACCESS, 0).edit();
        editor.putBoolean(IS_ACCESS_DIALOG_OPEN, shouldShow);
        editor.commit();
    }

    public static void setNetworkAccess(boolean b) {
        Context context = GlobalContext.getContext();
        AntiVirusTools.setAutoUpdate(context, b);
        if (!AntiVirusTools.isAbroad()) {
            PreferenceHelper.setState(context, "harassment_auto_update_state", b);
        }
    }

    public static void setNetworkAccessForCloudClient(boolean value) {
        Context context = GlobalContext.getContext();
        if (value) {
            CloudClientOperation.openSystemManageCloudsWithInit(context);
        } else {
            CloudClientOperation.closeSystemManageClouds(context);
        }
    }

    public static boolean isStorageManagerNetworkAccessAllow(Context ctx) {
        return ctx.getSharedPreferences(SP_NETWORK_ACCESS, 0).getBoolean(STORAGE_MANAGER_NETWORK_ACCESS, false);
    }

    public static void setStorageManagerNetworkAccess(Context ctx, boolean isCheck) {
        Editor editor = ctx.getSharedPreferences(SP_NETWORK_ACCESS, 0).edit();
        editor.putBoolean(STORAGE_MANAGER_NETWORK_ACCESS, isCheck);
        editor.commit();
    }
}
