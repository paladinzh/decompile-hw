package com.huawei.systemmanager.comm.daulapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class DualAppDialog {
    private static final String LOG_TAG = "DualAppDialog";
    private AlertDialog mDialog;

    private static class DialogButtonClickListener implements OnClickListener {
        private DualAppDialogCallBack mCallBack;

        DialogButtonClickListener(DualAppDialogCallBack callBack) {
            this.mCallBack = callBack;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    if (this.mCallBack != null) {
                        this.mCallBack.onNegativeBtnClick();
                        return;
                    }
                    return;
                case -1:
                    if (this.mCallBack != null) {
                        this.mCallBack.onPositiveBtnClick();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public DualAppDialog(Context context, String title, String describe, String positiveButtonTxt, String negativeButtonTxt, DualAppDialogCallBack callBack) {
        createConfirmDialog(context, title, describe, positiveButtonTxt, negativeButtonTxt, callBack);
    }

    private void createConfirmDialog(Context context, String title, String describe, String positiveButtonTxt, String negativeButtonTxt, DualAppDialogCallBack callBack) {
        HwLog.d(LOG_TAG, "dialog created");
        View confirmLayout = LayoutInflater.from(context).inflate(R.layout.dualapp_confirm_dialog, null);
        TextView describeText = (TextView) confirmLayout.findViewById(R.id.dialog_description);
        if (describeText != null) {
            describeText.setText(describe);
        }
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setView(confirmLayout);
        DialogButtonClickListener dialogButtonClickListener = new DialogButtonClickListener(callBack);
        builder.setPositiveButton(positiveButtonTxt, dialogButtonClickListener);
        builder.setNegativeButton(negativeButtonTxt, dialogButtonClickListener);
        this.mDialog = builder.create();
    }

    public void show() {
        try {
            if (this.mDialog != null) {
                this.mDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
