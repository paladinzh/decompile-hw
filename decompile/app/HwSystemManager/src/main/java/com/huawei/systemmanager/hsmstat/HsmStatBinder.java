package com.huawei.systemmanager.hsmstat;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.hsmstat.IHsmStatService.Stub;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.hsmstat.base.StatEntry;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class HsmStatBinder extends Stub implements HsmService {
    public static final String NAME = "HsmStat";
    public static final String PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    public static final String TAG = "HsmStat_info_StatBinder";
    private final Context mContext;
    private final IHsmStat mDelegate;
    private boolean mDelegetEnable;
    private HsmStatBinderHandler mHandler;
    private final ProcessControl mProcessControl;
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (HsmStatBinder.this.mProcessControl != null && HsmStatBinder.this.mProcessControl.checkIfSystemManager(pid)) {
                if (foregroundActivities) {
                    HsmStatBinder.this.mHandler.sendEmptyMessage(30);
                } else {
                    HsmStatBinder.this.mHandler.sendEmptyMessage(32);
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (HsmStatBinder.this.mProcessControl.checkIfUiPid(pid)) {
                HsmStatBinder.this.mHandler.sendEmptyMessage(34);
            }
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    HsmStatBinder.this.mHandler.sendEmptyMessage(37);
                } else if (HsmStatConst.BRAODCAST_ACTION_STAT_DIALLY.equals(action)) {
                    HsmStatBinder.this.mHandler.statRDially();
                }
            }
        }
    };
    private boolean mRegister;
    private boolean mRegisterUserSwitcher;
    private UserAgreementStateReceiver mUAStateReceiver;
    private boolean mUserSwitch = true;
    private ContentObserver mUserSwitchObserver;

    private static class ProcessControl {
        int mServicePid = Process.myPid();
        int mUiPid = -1;

        ProcessControl() {
        }

        void updatePid(int pid) {
            if (this.mServicePid != pid) {
                this.mUiPid = pid;
            }
        }

        boolean checkIfSystemManager(int pid) {
            if (this.mUiPid == pid || this.mServicePid == pid) {
                return true;
            }
            return false;
        }

        boolean checkIfUiPid(int pid) {
            return this.mUiPid == pid;
        }
    }

    private class StateObserver extends ContentObserver {
        public StateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            HwLog.i(HsmStatBinder.TAG, "User experience state changes");
            HsmStatBinder.this.checkAndUpdateServiceState();
        }
    }

    private class UserAgreementStateReceiver extends HsmBroadcastReceiver {
        private UserAgreementStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            HwLog.i(HsmStatBinder.TAG, "User agreement state changes");
            HsmStatBinder.this.checkAndUpdateServiceState();
        }
    }

    public HsmStatBinder(Context ctx) {
        this.mContext = ctx;
        this.mDelegate = getDeleget(ctx);
        this.mHandler = new HsmStatBinderHandler(this.mContext, this.mDelegate);
        this.mDelegetEnable = this.mDelegate.isEnable();
        this.mProcessControl = new ProcessControl();
    }

    public boolean eStat(String key, String value) {
        enforceCallingPermission();
        if (checkAntimalStat(key, value)) {
            return true;
        }
        if (!isEnableInnner()) {
            return false;
        }
        this.mHandler.obtainMessage(1, new StatEntry(key, value)).sendToTarget();
        return true;
    }

    public boolean rStat() {
        enforceCallingPermission();
        if (isEnableInnner()) {
            return this.mHandler.statR();
        }
        HwLog.i(TAG, "enable false, call statR event ignore");
        return false;
    }

    public boolean isEnable() {
        enforceCallingPermission();
        return isEnableInnner();
    }

    public boolean setEnable(boolean enable) {
        enforceCallingPermission();
        this.mUserSwitch = enable;
        this.mDelegetEnable = this.mDelegate.setEnable(enable);
        boolean enableResult = isEnableInnner();
        if (enableResult) {
            start();
        } else {
            stop();
        }
        return enableResult;
    }

    private boolean isEnableInnner() {
        boolean z = false;
        if (!HsmStatConst.isFeatureEnable()) {
            return false;
        }
        if (this.mDelegetEnable) {
            z = this.mUserSwitch;
        }
        return z;
    }

    private IHsmStat getDeleget(Context ctx) {
        return new HiHsmStat(ctx);
    }

    public void init() {
        initialUserSwitcher();
        if (this.mDelegetEnable) {
            if (isEnableInnner()) {
                start();
            } else {
                HwLog.i(TAG, "init, is not enable, do not start");
            }
            return;
        }
        HwLog.i(TAG, "deleget is not enable, need not init");
    }

    public void onDestroy() {
        stop();
        unregisterUserSwitcher();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int pid = intent.getIntExtra(HsmStatConst.KEY_PROCESS_ID, -1);
            if (!(pid == -1 || this.mProcessControl == null)) {
                this.mProcessControl.updatePid(pid);
            }
        }
    }

    private void start() {
        this.mHandler.start();
        if (this.mRegister) {
            HwLog.i(TAG, "binder already started");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(HsmStatConst.BRAODCAST_ACTION_STAT_DIALLY);
        this.mContext.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            } else {
                HwLog.i(TAG, "am == null,register failed!");
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterProcessObserver RemoteException!");
        }
        ((AlarmManager) this.mContext.getSystemService("alarm")).setRepeating(1, 0, 86400000, geteEverydayReportPendingIntent());
        this.mRegister = true;
    }

    private void stop() {
        this.mHandler.stop();
        if (this.mRegister) {
            this.mContext.unregisterReceiver(this.mReceiver);
            try {
                IActivityManager am = ActivityManagerNative.getDefault();
                if (am != null) {
                    am.unregisterProcessObserver(this.mProcessObserver);
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "unregisterProcessObserver RemoteException!");
            }
            ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(geteEverydayReportPendingIntent());
            this.mRegister = false;
            return;
        }
        HwLog.i(TAG, "has not started yet");
    }

    public void activityStat(int action, String activityName, String params) throws RemoteException {
        enforceCallingPermission();
        if (isEnableInnner()) {
            int msg;
            if (action == 1) {
                msg = 20;
            } else if (action == 2) {
                msg = 22;
            } else if (action == 3) {
                msg = 24;
            } else if (action == 4) {
                msg = 25;
            } else {
                return;
            }
            this.mHandler.obtainMessage(msg, new StatEntry(activityName, params)).sendToTarget();
        }
    }

    private PendingIntent geteEverydayReportPendingIntent() {
        Intent intent = new Intent(HsmStatConst.BRAODCAST_ACTION_STAT_DIALLY);
        intent.setPackage(this.mContext.getPackageName());
        return PendingIntent.getBroadcast(this.mContext, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
    }

    private void enforceCallingPermission() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private final void initialUserSwitcher() {
        this.mUserSwitchObserver = new StateObserver(this.mHandler);
        this.mUAStateReceiver = new UserAgreementStateReceiver();
        this.mUserSwitch = getStateSwitch();
        HwLog.i(TAG, "hsmstat state initial value:" + this.mUserSwitch);
        Uri obserUri = Secure.getUriFor(HsmStatConst.USER_EXPERIENCE_SWITCH_NAME);
        if (obserUri == null) {
            obserUri = Secure.CONTENT_URI;
        }
        if (!this.mRegisterUserSwitcher) {
            this.mContext.getContentResolver().registerContentObserver(obserUri, true, this.mUserSwitchObserver);
            IntentFilter filter = new IntentFilter();
            filter.addAction(UserAgreementHelper.ACTION_USERAGREEMENT_STATE_CHANGE);
            this.mContext.registerReceiver(this.mUAStateReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            this.mRegisterUserSwitcher = true;
        }
    }

    private final void unregisterUserSwitcher() {
        if (this.mRegisterUserSwitcher) {
            if (this.mUserSwitchObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mUserSwitchObserver);
            }
            if (this.mUAStateReceiver != null) {
                this.mContext.unregisterReceiver(this.mUAStateReceiver);
            }
            this.mRegisterUserSwitcher = false;
        }
    }

    private final boolean getStateSwitch() {
        boolean isUserExperienceSwitchOn = 1 == Secure.getInt(this.mContext.getContentResolver(), HsmStatConst.USER_EXPERIENCE_SWITCH_NAME, -1);
        boolean isUserAgreementSwitchOn = UserAgreementHelper.getUserAgreementState(this.mContext);
        HwLog.i(TAG, "UE State = " + isUserExperienceSwitchOn + ", UA State = " + isUserAgreementSwitchOn);
        return isUserExperienceSwitchOn ? isUserAgreementSwitchOn : false;
    }

    private void checkAndUpdateServiceState() {
        boolean preState = isEnableInnner();
        this.mUserSwitch = getStateSwitch();
        HwLog.i(TAG, "hsmstat state changed, new state is " + this.mUserSwitch);
        boolean curState = isEnableInnner();
        if ((preState ^ curState) == 0) {
            return;
        }
        if (curState) {
            start();
        } else {
            stop();
        }
    }

    private boolean checkAntimalStat(String key, String value) {
        if (HsmStatConst.isFeatureEnable() && this.mDelegetEnable) {
            String baseInfo = String.valueOf(Events.E_ANTIMAL_BASE_INFO);
            String alertResult = String.valueOf(Events.E_ANTIMAL_ALERT_RESULT);
            String uninstall = String.valueOf(Events.E_ANTIMAL_UNINSTALL_LAUNCHER);
            String restore = String.valueOf(Events.E_AMTIMAL_RESTORE_LAUNCHER);
            if (TextUtils.equals(key, baseInfo) || TextUtils.equals(key, alertResult) || TextUtils.equals(key, uninstall) || TextUtils.equals(key, restore)) {
                HwLog.i(TAG, "checkIsAntimalStat is antimal stat");
                this.mHandler.statImmediately(new StatEntry(key, value));
                return true;
            }
        }
        return false;
    }
}
