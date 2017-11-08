package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.util.Log;

public class TetherProvisioningActivity extends Activity {
    private static final boolean DEBUG = Log.isLoggable("TetherProvisioningAct", 3);
    private ResultReceiver mResultReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mResultReceiver = (ResultReceiver) getIntent().getParcelableExtra("extraProvisionCallback");
        int tetherType = getIntent().getIntExtra("extraAddTetherType", -1);
        String[] provisionApp = getResources().getStringArray(17235992);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(provisionApp[0], provisionApp[1]);
        intent.putExtra("TETHER_TYPE", tetherType);
        if (DEBUG) {
            Log.d("TetherProvisioningAct", "Starting provisioning app: " + provisionApp[0] + "." + provisionApp[1]);
        }
        if (getPackageManager().queryIntentActivities(intent, 65536).isEmpty()) {
            Log.e("TetherProvisioningAct", "Provisioning app is configured, but not available.");
            this.mResultReceiver.send(11, null);
            finish();
            return;
        }
        startActivityForResultAsUser(intent, 0, UserHandle.CURRENT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            int result;
            if (DEBUG) {
                Log.d("TetherProvisioningAct", "Got result from app: " + resultCode);
            }
            if (resultCode == -1) {
                result = 0;
            } else {
                result = 11;
            }
            this.mResultReceiver.send(result, null);
            finish();
        }
    }
}
