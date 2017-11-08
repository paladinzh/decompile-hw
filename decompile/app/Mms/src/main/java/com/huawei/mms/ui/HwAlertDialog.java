package com.huawei.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseArray;
import android.widget.Button;
import com.google.android.gms.R;

public class HwAlertDialog {
    private Builder builder;
    private SparseArray<Integer> colorIds;
    private Context mContext;
    private AlertDialog mDialog;

    public HwAlertDialog(Context context) {
        this.builder = new Builder(context);
        this.mContext = context;
    }

    public HwAlertDialog setTitle(int resId) {
        this.builder.setTitle(resId);
        return this;
    }

    public HwAlertDialog setMessage(CharSequence msg) {
        this.builder.setMessage(msg);
        return this;
    }

    public HwAlertDialog setCancelable(boolean cancelable) {
        this.builder.setCancelable(cancelable);
        return this;
    }

    public HwAlertDialog setPositiveButton(int resId, OnClickListener l) {
        this.builder.setPositiveButton(resId, l);
        return this;
    }

    public HwAlertDialog setNegativeButton(int resId, OnClickListener l) {
        this.builder.setNegativeButton(resId, l);
        return this;
    }

    public void setButtonColor(int which, int color) {
        if (this.mDialog != null) {
            Button button = this.mDialog.getButton(which);
            if (button != null) {
                button.setTextColor(color);
            } else {
                return;
            }
        }
        if (this.colorIds == null) {
            this.colorIds = new SparseArray();
        }
        this.colorIds.append(which, Integer.valueOf(color));
    }

    public HwAlertDialog show() {
        this.mDialog = this.builder.show();
        if (this.colorIds != null) {
            int len = this.colorIds.size();
            for (int i = 0; i < len; i++) {
                setButtonColor(this.colorIds.keyAt(i), ((Integer) this.colorIds.valueAt(i)).intValue());
            }
        }
        setButtonColor(-1, this.mContext.getResources().getColor(R.drawable.text_color_red));
        return this;
    }
}
