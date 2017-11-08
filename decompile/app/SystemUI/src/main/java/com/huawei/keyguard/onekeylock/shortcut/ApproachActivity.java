package com.huawei.keyguard.onekeylock.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$string;
import com.huawei.keyguard.onekeylock.OneKeyLockActivity;
import com.huawei.keyguard.util.HwLog;

public class ApproachActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent approachIntent = getIntent();
        if (approachIntent == null || !"android.intent.action.CREATE_SHORTCUT".equals(approachIntent.getAction())) {
            Intent intent = new Intent();
            intent.setClass(this, OneKeyLockActivity.class);
            startActivity(intent);
        } else {
            HwLog.d("ApproachActivity", "action_create_shortcut for onekey lock.");
            Intent addShortcut = new Intent();
            addShortcut.putExtra("android.intent.extra.shortcut.NAME", getResources().getString(R$string.LauncherIcon_Home_ScreenLock));
            addShortcut.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(this, R$drawable.ic_onekey_widget_shortcut));
            addShortcut.putExtra("android.intent.extra.shortcut.INTENT", new Intent(this, ApproachActivity.class));
            setResult(-1, addShortcut);
        }
        finish();
    }
}
