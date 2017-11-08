package com.huawei.systemmanager.power.model;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.util.PowerNotificationUtils;
import com.huawei.systemmanager.util.HwLog;

public class PowerModeDialogControl {
    private static final String SUPERPOWER_DIALOG_CANCLE_WITHOUT_REMIND = "1";
    private static final String SUPERPOWER_DIALOG_CANCLE_WITH_REMIND = "0";
    private static final String SUPERPOWER_DIALOG_ENABLE_WITHOUT_REMIND = "3";
    private static final String SUPERPOWER_DIALOG_ENABLE_WITH_REMIND = "2";
    private static final String TAG = PowerModeDialogControl.class.getSimpleName();
    private static Builder dialogBuilder = null;
    private static Context mContext;
    private static OnKeyListener mOnKeyListener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() != 0 || 4 != keyCode) {
                return false;
            }
            dialog.cancel();
            return true;
        }
    };
    private static AlertDialog mRemindDialog = null;
    private static OnClickListener mSaveModeClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            String[] strArr;
            if (which == -1) {
                String str;
                Context -get1 = PowerModeDialogControl.mContext;
                String str2 = SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME;
                String str3 = SharedPrefKeyConst.POWER_SAVE_MODE_DIALOG_REMIND_KEY;
                if (PowerModeDialogControl.saveMode_hasSelection) {
                    str = "1";
                } else {
                    str = "0";
                }
                SharePrefWrapper.setPrefValue(-get1, str2, str3, str);
                HwLog.i(PowerModeDialogControl.TAG, "PowerModelStateChange, open save mode from saveMode notification and dialog.");
                String statParam2 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                HsmStat.statE((int) Events.E_POWER_POWERMODE_SELECT, statParam2);
                PowerModeControl.getInstance(PowerModeDialogControl.mContext).changePowerMode(4);
                PowerNotificationUtils.showPowerModeQuitNotification(PowerModeDialogControl.mContext);
                dialog.dismiss();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = PowerModeDialogControl.saveMode_hasSelection ? "2" : "3";
                HsmStat.statE((int) Events.E_POWER_SVAEMODE_DIALOG_REMIND, HsmStatConst.constructJsonParams(strArr));
            } else if (which == -2) {
                dialog.cancel();
                strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                strArr[1] = PowerModeDialogControl.saveMode_hasSelection ? "0" : "1";
                HsmStat.statE((int) Events.E_POWER_SVAEMODE_DIALOG_REMIND, HsmStatConst.constructJsonParams(strArr));
            }
        }
    };
    private static boolean saveMode_hasSelection = false;

    public static void showSuperModeDialog(Context mAppContext, OnClickListener clickListener) {
        showSuperModeDialog(mAppContext, clickListener, mOnKeyListener, null);
    }

    public static void showSuperModeDialog(Context mAppContext, OnClickListener clickListener, OnCancelListener cancelListener) {
        showSuperModeDialog(mAppContext, clickListener, mOnKeyListener, cancelListener);
    }

    public static void showSuperModeDialog(Context mAppContext, OnClickListener clickListener, OnKeyListener onKeyListener, OnCancelListener cancelListener) {
        mContext = mAppContext.getApplicationContext();
        if (mRemindDialog != null && mRemindDialog.isShowing()) {
            mRemindDialog.cancel();
        }
        if (dialogBuilder != null) {
            dialogBuilder = null;
        }
        if (mRemindDialog != null) {
            mRemindDialog = null;
        }
        dialogBuilder = new Builder(mAppContext, mAppContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        dialogBuilder.setTitle(R.string.super_power_saving_mode_alert_title);
        dialogBuilder.setMessage(mAppContext.getString(R.string.super_power_saving_mode_alert_content_upgrade));
        dialogBuilder.setPositiveButton(R.string.super_power_saving_mode_alert_enter, clickListener);
        dialogBuilder.setOnCancelListener(cancelListener);
        dialogBuilder.setOnKeyListener(onKeyListener);
        dialogBuilder.setNegativeButton(R.string.alert_dialog_cancel, clickListener);
        mRemindDialog = dialogBuilder.create();
        mRemindDialog.setCanceledOnTouchOutside(false);
        mRemindDialog.getWindow().setType(2003);
        mRemindDialog.show();
    }

    public static void showSaveModeDialog(Context mAppContext) {
        showSaveModeDialog(mAppContext, mSaveModeClickListener, mOnKeyListener, null);
    }

    public static void showSaveModeDialog(Context mAppContext, OnClickListener clickListener, OnKeyListener onKeyListener, OnCancelListener cancelListener) {
        boolean z = true;
        mContext = mAppContext.getApplicationContext();
        if (mRemindDialog != null && mRemindDialog.isShowing()) {
            mRemindDialog.cancel();
        }
        if (dialogBuilder != null) {
            dialogBuilder = null;
        }
        if (mRemindDialog != null) {
            mRemindDialog = null;
        }
        dialogBuilder = new Builder(mAppContext, mAppContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        String remindState = SharePrefWrapper.getPrefValue(mAppContext, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_SAVE_MODE_DIALOG_REMIND_KEY, String.valueOf(1));
        dialogBuilder.setTitle(R.string.power_save_mode_dialog_title);
        dialogBuilder.setMessage(R.string.power_save_mode_dialog_time);
        dialogBuilder.setPositiveButton(R.string.super_power_saving_mode_alert_enter, clickListener);
        dialogBuilder.setNegativeButton(mAppContext.getString(R.string.alert_dialog_cancel), clickListener);
        dialogBuilder.setOnKeyListener(onKeyListener);
        dialogBuilder.setOnCancelListener(cancelListener);
        mRemindDialog = dialogBuilder.create();
        View contentView = mRemindDialog.getLayoutInflater().inflate(R.layout.prevent_explain_dialog, null);
        mRemindDialog.setView(contentView);
        CheckBox cb = (CheckBox) contentView.findViewById(R.id.prevent_explain_check);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                PowerModeDialogControl.saveMode_hasSelection = arg1;
            }
        });
        if (!remindState.equals("1")) {
            z = false;
        }
        cb.setChecked(z);
        mRemindDialog.setCanceledOnTouchOutside(false);
        mRemindDialog.getWindow().setType(2003);
        mRemindDialog.show();
    }
}
