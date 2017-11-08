package com.huawei.systemmanager.startupmgr.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import com.huawei.systemmanager.R;

public class ModifyConfirmDialog implements OnClickListener, OnDismissListener {
    private static AlertDialog mDialog = null;
    private boolean mAllOp = false;
    private ISwitchChangeCallback mCallback;
    private boolean mClickOK = false;
    private boolean mListenViewChecked = false;
    private int mPosition = 0;

    public static ModifyConfirmDialog createNewAllOpDialog(Activity ctx, ISwitchChangeCallback callback, boolean isChecked) {
        ModifyConfirmDialog confirmDlg = new ModifyConfirmDialog(ctx, callback, isChecked);
        confirmDlg.setToAllOpDialog();
        return confirmDlg;
    }

    public static ModifyConfirmDialog createNewItemOpDialog(Activity ctx, ISwitchChangeCallback callback, int position, boolean isChecked) {
        ModifyConfirmDialog confirmDlg = new ModifyConfirmDialog(ctx, callback, isChecked);
        confirmDlg.setItemPosition(position);
        return confirmDlg;
    }

    public static void dismiss(Activity activity) {
        if (mDialog != null && activity == mDialog.getOwnerActivity()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void setTitle(int resId) {
        if (mDialog != null) {
            mDialog.setTitle(resId);
        }
    }

    public void setTitle(String title) {
        if (mDialog != null) {
            mDialog.setTitle(title);
        }
    }

    public void setMessage(CharSequence detail) {
        if (mDialog != null) {
            mDialog.setMessage(detail);
        }
    }

    public void show() {
        this.mClickOK = false;
        if (mDialog != null) {
            mDialog.show();
        }
    }

    private ModifyConfirmDialog(Activity ctx, ISwitchChangeCallback callback, boolean isChecked) {
        this.mCallback = callback;
        this.mListenViewChecked = isChecked;
        createAlertDialog(ctx, this.mListenViewChecked, this, this);
    }

    private static synchronized void createAlertDialog(Activity ctx, boolean listenViewChecked, OnClickListener clickListener, OnDismissListener dismissListener) {
        synchronized (ModifyConfirmDialog.class) {
            int themeID = ctx.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            if (mDialog != null) {
                Activity ownerActivity = mDialog.getOwnerActivity();
                if (!(ownerActivity == null || ownerActivity.isDestroyed())) {
                    mDialog.dismiss();
                    mDialog = null;
                }
            }
            mDialog = new Builder(ctx, themeID).create();
            mDialog.setOwnerActivity(ctx);
            mDialog.setButton(-1, ctx.getString(listenViewChecked ? R.string.permit : R.string.forbidden), clickListener);
            mDialog.setButton(-2, ctx.getString(R.string.cancel), clickListener);
            mDialog.setOnDismissListener(dismissListener);
        }
    }

    private void setToAllOpDialog() {
        this.mAllOp = true;
    }

    private void setItemPosition(int position) {
        this.mAllOp = false;
        this.mPosition = position;
    }

    private void handleConfirm() {
        if (this.mAllOp) {
            this.mCallback.allOpSwitchChanged(this.mListenViewChecked);
        } else {
            this.mCallback.itemSwitchChanged(this.mPosition, this.mListenViewChecked);
        }
    }

    private void handleCancel() {
        if (this.mAllOp) {
            this.mCallback.allOpSwitchChangeCancelled(this.mListenViewChecked);
        } else {
            this.mCallback.itemSwitchChangeCancelled(this.mPosition, this.mListenViewChecked);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mClickOK) {
            handleConfirm();
        } else {
            handleCancel();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (-1 == which) {
            this.mClickOK = true;
        }
    }
}
