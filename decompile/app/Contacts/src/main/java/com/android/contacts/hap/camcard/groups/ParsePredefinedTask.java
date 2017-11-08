package com.android.contacts.hap.camcard.groups;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.contacts.ext.phone.SetupPhoneAccount;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;

public class ParsePredefinedTask implements Runnable {
    private static final String TAG = ParsePredefinedTask.class.getSimpleName();
    private Context lContext;

    public ParsePredefinedTask(Context context) {
        this.lContext = context;
    }

    public void run() {
        int state = this.lContext.getPackageManager().getApplicationEnabledSetting("com.android.contacts");
        if (state == 2 || state == 3) {
            HwLog.w(TAG, "Contacts app was disabled, so don't parse vcard.");
            return;
        }
        SharedPreferences sp = SharePreferenceUtil.getDefaultSp_de(this.lContext);
        String keyForCamcardGroupUpdate = "camcard_group_update";
        if (!sp.getBoolean("camcard_group_update", false)) {
            CamcardGroup.updateIsCamcardToGroupIfNeed(this.lContext.getApplicationContext());
            sp.edit().putBoolean("camcard_group_update", true).apply();
        }
        String keyUpdateSync2Title = "camcard_group_update_sync2title";
        if (!sp.getBoolean("camcard_group_update_sync2title", false)) {
            CamcardGroup.updateGroupSync2Title(this.lContext.getApplicationContext());
            sp.edit().putBoolean("camcard_group_update_sync2title", true).apply();
        }
        String keyForUpdatingPredefGroups = "predef_groups_updated";
        if (!sp.getBoolean("predef_groups_updated", false)) {
            SetupPhoneAccount.updatePredefinedGroupsLabelRes(this.lContext);
            sp.edit().putBoolean("predef_groups_updated", true).commit();
        }
    }
}
