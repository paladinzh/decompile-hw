package com.huawei.mms.crypto.account.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import com.huawei.cspcommon.MLog;
import java.lang.reflect.Field;

public abstract class CheckFragment extends DialogFragment {
    protected Dialog mDialog;

    public abstract void handlePwdError();

    public void setDialogCanDismiss(boolean canDismiss) {
        if (this.mDialog == null) {
            MLog.i("CheckFragment", "setDialogDisplayProperty, dialog is null");
            return;
        }
        try {
            Field field = this.mDialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(this.mDialog, Boolean.valueOf(canDismiss));
        } catch (Exception e) {
            MLog.e("CheckFragment", "Exception:", (Throwable) e);
        }
    }
}
