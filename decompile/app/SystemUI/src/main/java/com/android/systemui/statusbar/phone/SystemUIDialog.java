package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.WindowManager.LayoutParams;

public class SystemUIDialog extends AlertDialog {
    private final Context mContext;

    public SystemUIDialog(Context context) {
        this(context, context.getResources().getIdentifier("androidhwext.R.style.Theme_Emui_Dialog_Alert", null, null));
    }

    public SystemUIDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        applyFlags(this);
        LayoutParams attrs = getWindow().getAttributes();
        attrs.setTitle(getClass().getSimpleName());
        getWindow().setAttributes(attrs);
    }

    public void setShowForAllUsers(boolean show) {
        setShowForAllUsers(this, show);
    }

    public void setMessage(int resId) {
        setMessage(this.mContext.getString(resId));
    }

    public void setPositiveButton(int resId, OnClickListener onClick) {
        setButton(-1, this.mContext.getString(resId), onClick);
    }

    public void setNegativeButton(int resId, OnClickListener onClick) {
        setButton(-2, this.mContext.getString(resId), onClick);
    }

    public static void setShowForAllUsers(AlertDialog dialog, boolean show) {
        if (show) {
            LayoutParams attributes = dialog.getWindow().getAttributes();
            attributes.privateFlags |= 16;
            return;
        }
        attributes = dialog.getWindow().getAttributes();
        attributes.privateFlags &= -17;
    }

    public static void applyFlags(AlertDialog dialog) {
        dialog.getWindow().setType(2014);
        dialog.getWindow().addFlags(655360);
    }
}
