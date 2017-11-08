package com.android.contacts.speeddial;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper.SpeedDialContract;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class SpeedDialerActivity extends Activity {
    private static final String TAG = SpeedDialerActivity.class.getSimpleName();
    public AlertDialog mGlobalDialogReference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        if (ActivityManager.isUserAMonkey()) {
            finish();
        }
        setContentView(R.layout.speeddialer_preview);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                setResult(0);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mGlobalDialogReference = null;
    }

    public static void handleResultFromContactSelection(final Intent aData, final Activity aCallerActivityRef, final boolean aLaunchSpeedDialSettings) {
        final Bundle lBundle = aData.getExtras();
        if (lBundle == null || !lBundle.containsKey("key_speed_dial")) {
            HwLog.e(TAG, "Expected key is not found in intent's bundle!!!!");
        } else {
            new Thread() {
                public void run() {
                    Uri lSelectedContactUri = aData.getData();
                    if (lSelectedContactUri != null) {
                        long data_ID = Long.parseLong(lSelectedContactUri.getLastPathSegment());
                        ContentValues lValues = new ContentValues();
                        lValues.put("key_number", Integer.valueOf(lBundle.getInt("key_speed_dial")));
                        lValues.put("phone_data_id", Long.valueOf(data_ID));
                        aCallerActivityRef.getContentResolver().insert(SpeedDialContract.CONTENT_URI, lValues);
                    }
                    if (aLaunchSpeedDialSettings) {
                        Activity activity = aCallerActivityRef;
                        final Activity activity2 = aCallerActivityRef;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                activity2.startActivity(CommonUtilMethods.getSpeedDialIntent());
                            }
                        });
                    }
                }
            }.start();
        }
    }
}
