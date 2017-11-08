package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InternationalRoamingDualCardSettings extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMultiSimEnabled() && Utils.isChinaTelecomArea()) {
            try {
                Intent intent = new Intent();
                intent.setAction("com.huawei.settings.intent.INTERNATIONAL_ROAMING");
                startActivity(intent);
                finish();
            } catch (ActivityNotFoundException ex) {
                Log.e("RoamingDualCard", "ActivityNotFoundException" + ex.toString());
                finish();
            }
        }
    }
}
