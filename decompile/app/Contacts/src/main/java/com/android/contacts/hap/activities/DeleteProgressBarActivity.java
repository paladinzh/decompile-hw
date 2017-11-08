package com.android.contacts.hap.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.google.android.gms.R;
import huawei.android.app.HwProgressDialog;

public class DeleteProgressBarActivity extends Activity {
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            boolean mAlive = false;
            if (!DeleteProgressBarActivity.this.isFinishing()) {
                mAlive = true;
            }
            if (!mAlive) {
                return;
            }
            int value;
            if (msg.what == 2) {
                value = msg.arg1;
                if (value >= 100) {
                    DeleteProgressBarActivity.this.showProgressDialog(value);
                } else {
                    DeleteProgressBarActivity.this.finish();
                }
            } else if (msg.what == 1 && DeleteProgressBarActivity.this.mProgressDialog != null) {
                value = msg.arg1;
                DeleteProgressBarActivity.this.mProgressDialog.setProgress(value);
                if (value >= DeleteProgressBarActivity.this.mTotal) {
                    DeleteProgressBarActivity.this.mProgressDialog.dismiss();
                    DeleteProgressBarActivity.this.finish();
                }
            } else if (msg.what == 6) {
                int themeID = DeleteProgressBarActivity.this.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                if (themeID == 0) {
                    Toast.makeText(DeleteProgressBarActivity.this, DeleteProgressBarActivity.this.getString(R.string.msg_sim_not_deletable_Toast), 1).show();
                } else {
                    Toast.makeText(new ContextThemeWrapper(DeleteProgressBarActivity.this.getApplicationContext(), themeID), DeleteProgressBarActivity.this.getString(R.string.msg_sim_not_deletable_Toast), 1).show();
                }
                DeleteProgressBarActivity.this.mProgressDialog.dismiss();
                DeleteProgressBarActivity.this.finish();
            }
        }
    };
    private HwProgressDialog mProgressDialog;
    private long[] mSelectedItemIds;
    private int mTotal;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        this.mSelectedItemIds = getIntent().getLongArrayExtra("contacts_ids");
        if (this.mSelectedItemIds == null) {
            finish();
        } else if (savedInstanceState == null || !savedInstanceState.getBoolean("recreate")) {
            deleteService();
        } else {
            finish();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("recreate", true);
        super.onSaveInstanceState(outState);
    }

    private void deleteService() {
        startService(ExtendedContactSaveService.createDeleteSelectedContactsIntent(this, this.mSelectedItemIds, new Messenger(this.handler)));
        this.mTotal = this.mSelectedItemIds.length;
    }

    private void showProgressDialog(int count) {
        this.mProgressDialog = new HwProgressDialog(this);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(getString(R.string.delete_contacts_message));
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setMax(count);
        this.mProgressDialog.setIndeterminate(false);
        this.mProgressDialog.show();
        this.mProgressDialog.disableCancelButton();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }
}
