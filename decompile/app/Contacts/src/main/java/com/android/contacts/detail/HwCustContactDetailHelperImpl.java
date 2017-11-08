package com.android.contacts.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.Toast;
import com.google.android.gms.R;

public class HwCustContactDetailHelperImpl extends HwCustContactDetailHelper {
    private boolean isDisplayContactsShortcutToast() {
        return SystemProperties.getBoolean("ro.config.hw_hideEmuiInfo", false);
    }

    public void showContactsShortcutToast(Intent shortcutIntent, Activity parentActivity) {
        if (isDisplayContactsShortcutToast()) {
            Bundle bundle = shortcutIntent.getExtras();
            if (bundle != null && parentActivity != null) {
                String displayName = (String) bundle.getCharSequence("android.intent.extra.shortcut.NAME");
                Toast.makeText(parentActivity, String.format(parentActivity.getResources().getString(R.string.shortcut_installed_toast), new Object[]{displayName}), 1).show();
            }
        }
    }
}
