package com.android.contacts.hap.rcs.activities;

import android.app.Activity;
import android.os.Bundle;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.google.android.gms.R;

public class RcsPreCallPreviewActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        getWindow().addFlags(67108864);
        getWindow().addFlags(134217728);
        setContentView(R.layout.pre_call_preview_activity);
    }
}
