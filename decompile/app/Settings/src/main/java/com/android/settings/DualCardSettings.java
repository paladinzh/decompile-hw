package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DualCardSettings extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMultiSimEnabled() && !Utils.isChinaTelecomArea()) {
            try {
                Intent intent = new Intent();
                intent.setAction("com.huawei.settings.intent.DUAL_CARD_SETTINGS");
                startActivity(intent);
                finish();
            } catch (ActivityNotFoundException ex) {
                Log.e("DualCardSettings", ex.getMessage());
                finish();
            }
        }
    }
}
