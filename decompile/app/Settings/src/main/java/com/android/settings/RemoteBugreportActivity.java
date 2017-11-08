package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

public class RemoteBugreportActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int notificationType = getIntent().getIntExtra("android.app.extra.bugreport_notification_type", -1);
        if (notificationType == 2) {
            new Builder(this).setMessage(2131626991).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    RemoteBugreportActivity.this.finish();
                }
            }).setNegativeButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.finish();
                }
            }).create().show();
        } else if (notificationType == 1 || notificationType == 3) {
            int i;
            Builder title = new Builder(this).setTitle(2131626988);
            if (notificationType == 1) {
                i = 2131626990;
            } else {
                i = 2131626989;
            }
            title.setMessage(i).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    RemoteBugreportActivity.this.finish();
                }
            }).setNegativeButton(2131626993, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.sendBroadcastAsUser(new Intent("com.android.server.action.BUGREPORT_SHARING_DECLINED"), UserHandle.SYSTEM, "android.permission.DUMP");
                    RemoteBugreportActivity.this.finish();
                }
            }).setPositiveButton(2131626992, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.sendBroadcastAsUser(new Intent("com.android.server.action.BUGREPORT_SHARING_ACCEPTED"), UserHandle.SYSTEM, "android.permission.DUMP");
                    RemoteBugreportActivity.this.finish();
                }
            }).create().show();
        } else {
            Log.e("RemoteBugreportActivity", "Incorrect dialog type, no dialog shown. Received: " + notificationType);
        }
    }
}
