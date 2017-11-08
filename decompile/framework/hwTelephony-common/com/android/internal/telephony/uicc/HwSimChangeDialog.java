package com.android.internal.telephony.uicc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class HwSimChangeDialog {
    private static HwSimChangeDialog df = null;
    AlertDialog dialog;
    private Context mContext;

    private HwSimChangeDialog() {
    }

    public static synchronized HwSimChangeDialog getInstance() {
        HwSimChangeDialog hwSimChangeDialog;
        synchronized (HwSimChangeDialog.class) {
            if (df == null) {
                df = new HwSimChangeDialog();
            }
            hwSimChangeDialog = df;
        }
        return hwSimChangeDialog;
    }

    public AlertDialog getSimAddDialog(Context c, boolean isAdded, int mSlotId) {
        String title;
        String message;
        String buttonTxt;
        this.mContext = c;
        Context contextThemeWrapper = new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        if (this.dialog != null) {
            this.dialog.dismiss();
        }
        Resources r = Resources.getSystem();
        if (isAdded) {
            title = r.getString(17040367);
        } else {
            title = r.getString(17040364);
        }
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (isAdded) {
                message = r.getString(33685750);
            } else {
                message = r.getString(33685751);
            }
            message = String.format(message, new Object[]{Integer.valueOf(mSlotId + 1)});
        } else if (isAdded) {
            message = r.getString(33685752);
        } else {
            message = r.getString(33685753);
        }
        if (isAdded) {
            buttonTxt = r.getString(33685754);
        } else {
            buttonTxt = r.getString(33685755);
        }
        this.dialog = new Builder(this.mContext, 33947691).setTitle(title).setMessage(message).create();
        OnClickListener listenerCancel = new OnClickListener() {
            public void onClick(View arg0) {
                if (HwSimChangeDialog.this.dialog != null) {
                    HwSimChangeDialog.this.dialog.dismiss();
                }
            }
        };
        LinearLayout layout = new LinearLayout(this.mContext);
        layout.setOrientation(0);
        if (isAdded) {
            Button buttonRestart = new Button(contextThemeWrapper);
            buttonRestart.setText(buttonTxt);
            buttonRestart.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    if (HwSimChangeDialog.this.dialog != null) {
                        HwSimChangeDialog.this.dialog.dismiss();
                    }
                    Intent reboot = new Intent("android.intent.action.REBOOT");
                    reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    reboot.setFlags(268435456);
                    HwSimChangeDialog.this.mContext.startActivity(reboot);
                }
            });
            LayoutParams lpButtonRestart = new LayoutParams(-2, -2, 1.0f);
            buttonRestart.setLayoutParams(lpButtonRestart);
            Button buttonIgnore = new Button(contextThemeWrapper);
            buttonIgnore.setText(r.getString(33685756));
            buttonIgnore.setOnClickListener(listenerCancel);
            LayoutParams lpButtonIgnore = new LayoutParams(-2, -2, 1.0f);
            buttonIgnore.setLayoutParams(lpButtonIgnore);
            layout.addView(buttonIgnore, lpButtonIgnore);
            layout.addView(buttonRestart, lpButtonRestart);
        } else {
            Button buttonConfirm = new Button(contextThemeWrapper);
            buttonConfirm.setText(buttonTxt);
            buttonConfirm.setOnClickListener(listenerCancel);
            LayoutParams lpButtonConfirm = new LayoutParams(-2, -2, 1.0f);
            buttonConfirm.setLayoutParams(lpButtonConfirm);
            layout.addView(buttonConfirm, lpButtonConfirm);
        }
        this.dialog.setCancelable(true);
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setView(layout, Dp2Px(this.mContext, 15.0f), Dp2Px(this.mContext, 12.0f), Dp2Px(this.mContext, 15.0f), Dp2Px(this.mContext, 12.0f));
        return this.dialog;
    }

    private int Dp2Px(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
