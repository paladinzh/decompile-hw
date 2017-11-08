package com.huawei.gallery.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import com.android.gallery3d.R;

public class PermissionInfoAlert {
    private final Activity mActivity;
    private AlertDialog mDialog = null;

    public PermissionInfoAlert(Activity activity) {
        this.mActivity = activity;
    }

    public void start() {
        if (this.mActivity != null) {
            this.mDialog = new Builder(this.mActivity).setTitle(R.string.error_permissions_title).setMessage(R.string.error_permissions).setCancelable(false).setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 4) {
                        if (PermissionInfoAlert.this.mDialog != null) {
                            PermissionInfoAlert.this.mDialog.dismiss();
                        }
                        if (PermissionInfoAlert.this.mActivity != null) {
                            PermissionInfoAlert.this.mActivity.finish();
                        }
                    }
                    return true;
                }
            }).setPositiveButton(this.mActivity.getResources().getString(R.string.close), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (PermissionInfoAlert.this.mDialog != null) {
                        PermissionInfoAlert.this.mDialog.dismiss();
                    }
                    if (PermissionInfoAlert.this.mActivity != null) {
                        PermissionInfoAlert.this.mActivity.finish();
                    }
                }
            }).show();
        }
    }

    public void stop() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
            if (this.mActivity != null) {
                this.mActivity.finish();
            }
        }
    }
}
