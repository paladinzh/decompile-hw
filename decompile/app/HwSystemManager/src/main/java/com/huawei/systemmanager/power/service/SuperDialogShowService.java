package com.huawei.systemmanager.power.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.PowerModeDialogControl;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;

public class SuperDialogShowService extends Service {
    private static final String MUTIL_CARD_HOOK = "OFFHOOK";
    private static final String MUTIL_CARD_RING = "RINGING";
    private static final String SUPERPOWER_DIALOG_CANCLE_WITHOUT_REMIND = "1";
    private static final String SUPERPOWER_DIALOG_CANCLE_WITH_REMIND = "0";
    private static final String SUPERPOWER_DIALOG_ENABLE_WITHOUT_REMIND = "3";
    private static final String SUPERPOWER_DIALOG_ENABLE_WITH_REMIND = "2";
    private static final String TAG = "SuperDialogShowService";
    private static AlertDialog mRemindDialog = null;
    private boolean hasSelection = false;
    private Context mAppContext = null;
    private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                HwLog.e(SuperDialogShowService.TAG, "mPhoneReceiver null intent!");
                return;
            }
            String phoneReceiverState = intent.getStringExtra("state");
            HwLog.i(SuperDialogShowService.TAG, "phoneReceiverState = " + phoneReceiverState);
            if (SuperDialogShowService.MUTIL_CARD_RING.equals(phoneReceiverState) || SuperDialogShowService.MUTIL_CARD_HOOK.equals(phoneReceiverState)) {
                SuperDialogShowService.this.releaseServiceForCall();
            }
        }
    };

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.mAppContext = getApplicationContext();
        registerPhoneListener();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i(TAG, "onStartCommand");
        Bundle extras = intent != null ? intent.getExtras() : null;
        if (intent == null || extras == null) {
            HwLog.e(TAG, "onStartCommand intent or bundle null!");
            return 2;
        }
        String label = extras.getString(ApplicationConstant.SUPER_DIALOG_LABEL);
        if (ApplicationConstant.NORMAL_SUPER_DIALOG.equals(label)) {
            if (!Utility.isOwnerUser()) {
                return 2;
            }
            showNormalSuperDialog(extras.getString(ApplicationConstant.SUPER_DIALOG_PACKAGEFROM));
        } else if (ApplicationConstant.LOW_BATTERY_SUPER_DIALOG.equals(label)) {
            if (Utility.superPowerEntryEnable() && !Utility.isWifiOnlyMode() && !Utility.isDataOnlyMode() && UserHandle.myUserId() == 0) {
                showLowBatteryRemindDialog();
            }
        } else if (ApplicationConstant.POWER_SAVE_MODE_DIALOG.equals(label)) {
            showSaveModeRemindDialog();
        }
        return 2;
    }

    public void onDestroy() {
        HwLog.i(TAG, "The SuperDialogShowService destory!");
        unregisterReceiver(this.mPhoneReceiver);
        super.onDestroy();
    }

    private void showNormalSuperDialog(final String packageNameFrom) {
        PowerModeDialogControl.showSuperModeDialog(this.mAppContext, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String statParam;
                if (which == -1) {
                    statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_KEY, packageNameFrom, HsmStatConst.PARAM_VAL, "1");
                    HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_ENTER, statParam);
                    SysCoreUtils.enterSuperPowerSavingMode(SuperDialogShowService.this.mAppContext);
                    dialog.dismiss();
                    SuperDialogShowService.this.stopSelf();
                } else if (which == -2) {
                    statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_KEY, packageNameFrom, HsmStatConst.PARAM_VAL, "0");
                    HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_ENTER, statParam);
                    dialog.cancel();
                    SuperDialogShowService.this.stopSelf();
                }
            }
        });
    }

    private void showSaveModeRemindDialog() {
        PowerModeDialogControl.showSaveModeDialog(this.mAppContext);
    }

    private void showLowBatteryRemindDialog() {
        PowerModeDialogControl.showSuperModeDialog(this.mAppContext, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String statParam;
                if (which == -1) {
                    statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "3");
                    HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_REMIND, statParam);
                    SysCoreUtils.enterSuperPowerSavingMode(SuperDialogShowService.this.mAppContext);
                    dialog.dismiss();
                    SuperDialogShowService.this.stopSelf();
                } else if (which == -2) {
                    statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                    HsmStat.statE((int) Events.E_POWER_SUPERSVAEMODE_DIALOG_REMIND, statParam);
                    dialog.cancel();
                    SuperDialogShowService.this.stopSelf();
                }
            }
        });
    }

    private void registerPhoneListener() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(this.mPhoneReceiver, commandFilter);
    }

    private void releaseServiceForCall() {
        if (mRemindDialog != null) {
            mRemindDialog.cancel();
        }
        stopSelf();
    }
}
