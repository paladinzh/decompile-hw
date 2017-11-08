package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;

public class HwCustAccountSyncSettingsImpl extends HwCustAccountSyncSettings {
    static final String ATT_ACCOUNT_NAME = "AT&T Address Book";

    public void customizeAccountSync(Account account, Bundle extras) {
        if (SystemProperties.getBoolean("ro.config.att.aab", false) && account != null && account.name.equals(ATT_ACCOUNT_NAME)) {
            extras.putBoolean("manualsync", true);
        }
    }

    public void customizeAutoSync(Account account, Activity activity) {
        if (SystemProperties.getBoolean("ro.config.att.aab", false) && account != null && account.name.equals(ATT_ACCOUNT_NAME)) {
            activity.sendBroadcast(new Intent("com.android.sync.ATT_ACCOUNT_SETTINGS_CHANGE"));
        }
    }
}
