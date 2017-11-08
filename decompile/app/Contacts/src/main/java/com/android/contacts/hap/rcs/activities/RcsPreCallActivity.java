package com.android.contacts.hap.rcs.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.rcs.dialer.RcsPreCallFragment;
import com.google.android.gms.R;

public class RcsPreCallActivity extends Activity {
    private RcsPreCallFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (1 == getResources().getConfiguration().orientation || isInMultiWindowMode()) {
            Window win = getWindow();
            LayoutParams winParams = win.getAttributes();
            winParams.flags |= 67108864;
            win.setAttributes(winParams);
        }
        setContentView(R.layout.pre_call_activity);
        this.mFragment = (RcsPreCallFragment) getFragmentManager().findFragmentById(R.id.pre_call_fragment);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed() {
        this.mFragment.onBackPressed();
        super.onBackPressed();
    }
}
