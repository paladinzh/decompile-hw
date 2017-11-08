package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class FdnIndicateDialogActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.v("FdnIndicateDialogActivity", "oncreate");
        requestWindowFeature(1);
        if (getIntent() == null) {
            MLog.w("FdnIndicateDialogActivity", "getIntent() is null in onCreate(), return it.");
            finish();
            return;
        }
        MLog.v("FdnIndicateDialogActivity", "show DIALOG_SET_SMSC_TO_FDN");
        showDialog(1);
    }

    protected Dialog onCreateDialog(int id) {
        Intent intent = getIntent();
        final String smscNumber = intent.getStringExtra(HarassNumberUtil.NUMBER);
        final int sub = intent.getIntExtra("subscription", 0);
        String smscName = "smsc";
        Context context = this;
        switch (id) {
            case 1:
                return new Builder(this).setTitle(R.string.message_send_failed_title).setMessage(R.string.message_fdn_send_failed_body).setPositiveButton(R.string.menu_preferences, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MLog.v("FdnIndicateDialogActivity", "start edit fdn ui, dialog dismiss");
                        Intent intent = new Intent();
                        intent.putExtra("name", "smsc");
                        intent.putExtra(HarassNumberUtil.NUMBER, smscNumber);
                        intent.putExtra("subscription", sub);
                        intent.putExtra("sms_flag", true);
                        intent.setClassName("com.android.phone", "com.android.phone.settings.fdn.EditFdnContactScreen");
                        try {
                            this.startActivity(intent);
                        } catch (SecurityException se) {
                            MLog.e("FdnIndicateDialogActivity", "SecurityException ", (Throwable) se);
                        }
                        FdnIndicateDialogActivity.this.finish();
                    }
                }).setNegativeButton(R.string.no, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        FdnIndicateDialogActivity.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        FdnIndicateDialogActivity.this.finish();
                    }
                }).create();
            default:
                MLog.e("FdnIndicateDialogActivity", "Unexpected dialog type.");
                finish();
                return null;
        }
    }

    public void finish() {
        super.finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    public void onBackPressed() {
        super.onBackPressed();
        dismissDialog(1);
        finish();
    }
}
