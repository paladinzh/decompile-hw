package com.android.contacts.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;

public class DisplayModeActivity extends Activity implements OnCancelListener, OnClickListener {
    private int selected;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        StatisticalHelper.sendReport(5021, 0);
        showDialog(R.id.display_mode);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case R.id.display_mode:
                int i;
                final SharedPreferences pref = SharePreferenceUtil.getDefaultSp_de(this);
                if (CommonUtilMethods.getShowCallLogMergeStatus(this)) {
                    i = 1;
                } else {
                    i = 0;
                }
                this.selected = i;
                Builder builder = new Builder(this).setTitle(R.string.dialer_call_log_merge).setOnCancelListener(this).setNegativeButton(17039360, this);
                builder.setSingleChoiceItems(new String[]{getString(R.string.dialer_call_log_by_time), getString(R.string.dialer_call_log_by_number)}, this.selected, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                StatisticalHelper.sendReport(5021, 1);
                                break;
                            case 1:
                                StatisticalHelper.sendReport(5021, 2);
                                break;
                        }
                        if (DisplayModeActivity.this.selected != which) {
                            pref.edit().putBoolean("reference_is_refresh_calllog", true).commit();
                        }
                        DisplayModeActivity.this.setResult(which);
                        DisplayModeActivity.this.finish();
                    }
                });
                return builder.create();
            default:
                return super.onCreateDialog(id, args);
        }
    }

    public void onCancel(DialogInterface arg0) {
        setResult(this.selected);
        finish();
    }

    public void onClick(DialogInterface arg0, int arg1) {
        setResult(this.selected);
        finish();
    }

    public void finish() {
        removeDialog(R.id.display_mode);
        super.finish();
        overridePendingTransition(0, 0);
    }
}
