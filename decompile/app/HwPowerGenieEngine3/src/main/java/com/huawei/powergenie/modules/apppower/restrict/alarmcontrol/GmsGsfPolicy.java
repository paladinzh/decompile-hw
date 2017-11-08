package com.huawei.powergenie.modules.apppower.restrict.alarmcontrol;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import java.util.ArrayList;

public class GmsGsfPolicy {
    private static final boolean GMS_ALARM_SCROFF_PENDING_FEATURE = SystemProperties.getBoolean("ro.config.hw_gmsalarm_pending", true);
    private final AlarmRestrict mAlarmCtrl;
    private final Context mContext;
    private String mFrontApp = null;
    private final GmsAlarmCtrl mGmsAlarmCtrl;
    private final Handler mGsfGmsHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    GmsGsfPolicy.this.tryPendingGSFAlarm();
                    return;
                case 107:
                    GmsGsfPolicy.this.checkInstalledGmsApp();
                    return;
                case 109:
                    GmsGsfPolicy.this.checkFirstBoot();
                    return;
                case 111:
                    GmsGsfPolicy.this.forceKillGsfApp();
                    return;
                default:
                    return;
            }
        }
    };
    private final ArrayList<String> mGsfPkg = new ArrayList<String>() {
        {
            add("com.google.android.gms");
            add("com.google.android.gsf");
            add("com.google.android.gsf.login");
        }
    };
    private final IAppManager mIAppManager;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private boolean mIsGsfAlarmPending = false;
    private boolean mIsGsfAppInFront = false;

    class GmsAlarmCtrl {
        private final Handler mGmsAlarmCtrlHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100000:
                        if (GmsAlarmCtrl.this.isGmsNetRestricted()) {
                            GmsAlarmCtrl.this.pendingGmsAlarm(true);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        private boolean mIsGmsAlarmPending = false;

        GmsAlarmCtrl() {
        }

        protected void handleScreenState(boolean isScrOn) {
            if (GmsGsfPolicy.GMS_ALARM_SCROFF_PENDING_FEATURE && GmsGsfPolicy.this.mIPolicy.isChinaMarketProduct() && !GmsGsfPolicy.this.mIPolicy.isDisabledGsfGms()) {
                if (isScrOn) {
                    this.mGmsAlarmCtrlHandler.removeMessages(100000);
                    pendingGmsAlarm(false);
                } else {
                    Message msg = GmsGsfPolicy.this.mGsfGmsHandler.obtainMessage(100000);
                    this.mGmsAlarmCtrlHandler.removeMessages(100000);
                    this.mGmsAlarmCtrlHandler.sendMessageDelayed(msg, 5000);
                }
            }
        }

        private void pendingGmsAlarm(boolean isPending) {
            if (isPending == this.mIsGmsAlarmPending) {
                Log.i("GmsGsfPolicy", "The same state to ctrl gms alarm : " + isPending);
                return;
            }
            if (isPending) {
                Log.i("GmsGsfPolicy", "pending gsf gms alarm.");
                GmsGsfPolicy.this.mAlarmCtrl.pendingAlarmsAdapter(GmsGsfPolicy.this.mGsfPkg);
            } else {
                Log.i("GmsGsfPolicy", "unpending gsf gms alarm.");
                GmsGsfPolicy.this.mAlarmCtrl.unpendingAlarmsAdapter(GmsGsfPolicy.this.mGsfPkg);
            }
            this.mIsGmsAlarmPending = isPending;
        }

        private boolean isGmsNetRestricted() {
            int uid = GmsGsfPolicy.this.mIAppManager.getUidByPkg("com.google.android.gsf");
            if (uid != -1) {
                return CommonAdapter.isNetworkRestricted(GmsGsfPolicy.this.mContext, uid);
            }
            return false;
        }
    }

    protected GmsGsfPolicy(ICoreContext coreContext, AlarmRestrict alarmCtrl) {
        this.mContext = coreContext.getContext();
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mAlarmCtrl = alarmCtrl;
        this.mGmsAlarmCtrl = new GmsAlarmCtrl();
    }

    protected boolean initGSFAlarm() {
        if (this.mIPolicy.supportGmsGsfPolicy()) {
            if (this.mIPolicy.hasGmsApps() && this.mIPolicy.isDisabledGsfGms()) {
                Log.i("GmsGsfPolicy", "init enable gsf because has gms app.");
                disableGsfApp(false);
            }
            Log.i("GmsGsfPolicy", "gsf gms disabled :" + this.mIPolicy.isDisabledGsfGms() + " has gms app:" + this.mIPolicy.hasGmsApps());
            return true;
        }
        if (this.mIPolicy.isDisabledGsfGms()) {
            disableGsfApp(false);
        }
        Log.i("GmsGsfPolicy", "not supports gms gsf policy.");
        return false;
    }

    protected void handleBootComplete() {
        if (permitDisableGsfGms()) {
            delayDisableGmsGsf(180000);
        }
    }

    private boolean permitDisableGsfGms() {
        if (!this.mIPolicy.isChinaMarketProduct() || !this.mIDeviceState.isChinaOperator() || this.mIPolicy.isDisabledGsfGms() || this.mIPolicy.hasGmsApps()) {
            return false;
        }
        return true;
    }

    private void checkFirstBoot() {
        if (permitDisableGsfGms()) {
            if (this.mIDeviceState.isDeviceProvisioned() && this.mIDeviceState.isUserSetupComplete()) {
                Log.i("GmsGsfPolicy", "No gms app in first boot disable Gsf app.");
                disableGsfApp(true);
            } else {
                Log.w("GmsGsfPolicy", "user setup or device provisioned is not complete and delay disable gsf.");
                delayDisableGmsGsf(60000);
            }
        }
    }

    protected void handleOperatorAction(boolean isChina) {
        if (!isChina) {
            if (this.mIsGsfAlarmPending) {
                Log.i("GmsGsfPolicy", "not china operator, unpending gsf");
                disableGsfAlarm(false);
            }
            if (this.mIPolicy.isDisabledGsfGms()) {
                Log.i("GmsGsfPolicy", "not china operator, enable gsf gms !");
                disableGsfApp(false);
            }
        }
    }

    protected void handleAppFront(String pkgName) {
        this.mFrontApp = pkgName;
        if (this.mIPolicy.isDisabledGsfGms() && pkgName != null && this.mGsfPkg.contains(pkgName)) {
            this.mIsGsfAppInFront = true;
            Log.i("GmsGsfPolicy", "Gsf in front, enable gsf gms !");
            disableGsfApp(false);
        }
    }

    protected void handleCtsState(boolean ctsStart) {
        if (ctsStart && this.mIPolicy.isDisabledGsfGms()) {
            Log.i("GmsGsfPolicy", "stc running, enable  all!");
            disableGsfApp(false);
        }
    }

    protected void handlePackageState(boolean isAdded, String pkg) {
        if (this.mIPolicy.isGmsApp(pkg)) {
            if (isAdded) {
                if (this.mIPolicy.isDisabledGsfGms()) {
                    Log.i("GmsGsfPolicy", "install gms app, enable Gsf app.");
                    disableGsfApp(false);
                }
            } else if (this.mIPolicy.isChinaMarketProduct() && this.mIDeviceState.isChinaOperator()) {
                delayCheckGmsApps();
            }
            this.mIPolicy.processGmsPkgChanged(isAdded);
        }
    }

    private void checkInstalledGmsApp() {
        if (permitDisableGsfGms()) {
            Log.i("GmsGsfPolicy", "uninstall all gms app, disable gsf gms.");
            disableGsfApp(true);
        }
    }

    protected void handleScreenState(boolean screenOn) {
        this.mGmsAlarmCtrl.handleScreenState(screenOn);
        if (!screenOn) {
            if (this.mIsGsfAppInFront && this.mFrontApp != null && !this.mGsfPkg.contains(this.mFrontApp) && permitDisableGsfGms()) {
                this.mIsGsfAppInFront = false;
                Log.i("GmsGsfPolicy", "Gsf app background when screen off, disable gsf component !");
                disableGsfApp(true);
            }
            if (this.mIPolicy.hasGmsApps() && !this.mIsGsfAlarmPending && permitPendingGSFAlarm()) {
                this.mGsfGmsHandler.sendMessageDelayed(this.mGsfGmsHandler.obtainMessage(100), 5000);
            }
        } else if (this.mIsGsfAlarmPending) {
            Log.i("GmsGsfPolicy", "screen unlock, unpending gsf gms");
            disableGsfAlarm(false);
        }
    }

    private boolean permitPendingGSFAlarm() {
        if (!GMS_ALARM_SCROFF_PENDING_FEATURE && this.mIPolicy.isChinaMarketProduct()) {
            return this.mIDeviceState.isChinaOperator();
        }
        return false;
    }

    private void tryPendingGSFAlarm() {
        if (this.mIPolicy.hasGmsApps() && !this.mIsGsfAlarmPending && permitPendingGSFAlarm()) {
            boolean gmsAppRunning = false;
            for (String pkgName : this.mIAppManager.getRuningApp(this.mContext)) {
                if (this.mIPolicy.isGmsApp(pkgName)) {
                    gmsAppRunning = true;
                    break;
                }
            }
            if (!gmsAppRunning) {
                Log.i("GmsGsfPolicy", "no gms running at screen off, pending");
                disableGsfAlarm(true);
            }
        }
    }

    protected boolean isAlarmRestrict(String pkg) {
        return this.mIsGsfAlarmPending ? this.mGsfPkg.contains(pkg) : false;
    }

    private void disableGsfAlarm(boolean isDisable) {
        if (isDisable) {
            Log.i("GmsGsfPolicy", "pending gsf gms alarm.");
            this.mIsGsfAlarmPending = true;
            this.mAlarmCtrl.pendingAlarmsAdapter(this.mGsfPkg);
            return;
        }
        Log.i("GmsGsfPolicy", "unpending gsf gms alarm.");
        this.mIsGsfAlarmPending = false;
        this.mAlarmCtrl.unpendingAlarmsAdapter(this.mGsfPkg);
    }

    private void disableGsfApp(boolean isDisable) {
        if (isDisable && this.mIDeviceState.isCtsRunning()) {
            Log.e("GmsGsfPolicy", "CTS device not disable Gsf app !");
            return;
        }
        if (!isDisable) {
            removeKillGsfMsg();
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            try {
                this.mIPolicy.updateDisabledGsfGms(isDisable);
                for (String name : this.mGsfPkg) {
                    PackageInfo infoReceiver = pm.getPackageInfo(name, 514);
                    if (!(infoReceiver == null || infoReceiver.receivers == null)) {
                        for (ActivityInfo aInfo : infoReceiver.receivers) {
                            if (aInfo != null) {
                                disableAppComponent(pm, new ComponentName(aInfo.applicationInfo.packageName, aInfo.name), isDisable);
                            }
                        }
                    }
                    PackageInfo pInfoProviders = pm.getPackageInfo(name, 520);
                    if (!(pInfoProviders == null || pInfoProviders.providers == null)) {
                        for (ProviderInfo pInfo : pInfoProviders.providers) {
                            if (pInfo != null) {
                                disableAppComponent(pm, new ComponentName(pInfo.applicationInfo.packageName, pInfo.name), isDisable);
                            }
                        }
                    }
                    PackageInfo pInfoServices = pm.getPackageInfo(name, 516);
                    if (!(pInfoServices == null || pInfoServices.services == null)) {
                        for (ServiceInfo sInfo : pInfoServices.services) {
                            if (sInfo != null) {
                                disableAppComponent(pm, new ComponentName(sInfo.applicationInfo.packageName, sInfo.name), isDisable);
                            }
                        }
                    }
                    Log.i("GmsGsfPolicy", name + " component was " + (isDisable ? "disabled !" : "enabled !"));
                }
                if (isDisable) {
                    delayKillGsfMsg();
                }
            } catch (IllegalArgumentException e) {
                Log.w("GmsGsfPolicy", "no google gsf app !");
            } catch (Exception e2) {
                Log.w("GmsGsfPolicy", "Google gsf app restrict fail !");
            }
        }
    }

    private void disableAppComponent(PackageManager pm, ComponentName cp, boolean isDisable) {
        int state = pm.getComponentEnabledSetting(cp);
        if (isDisable) {
            if (state != 2) {
                pm.setComponentEnabledSetting(cp, 2, 1);
            }
        } else if (state == 2) {
            pm.setComponentEnabledSetting(cp, 0, 1);
        }
    }

    private void forceKillGsfApp() {
        if (this.mIPolicy.isDisabledGsfGms()) {
            ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
            for (String name : this.mGsfPkg) {
                am.forceStopPackageAsUser(name, -1);
                Log.i("GmsGsfPolicy", "Force stop " + name);
            }
            return;
        }
        Log.i("GmsGsfPolicy", "Cannot force stop gsf app, disable state = " + this.mIPolicy.isDisabledGsfGms());
    }

    private void delayDisableGmsGsf(long delay) {
        Message msg = this.mGsfGmsHandler.obtainMessage(109);
        this.mGsfGmsHandler.removeMessages(109);
        this.mGsfGmsHandler.sendMessageDelayed(msg, delay);
    }

    private void delayCheckGmsApps() {
        Message msg = this.mGsfGmsHandler.obtainMessage(107);
        this.mGsfGmsHandler.removeMessages(107);
        this.mGsfGmsHandler.sendMessageDelayed(msg, 10000);
    }

    private void delayKillGsfMsg() {
        Log.i("GmsGsfPolicy", "remove kill gsf app msg !");
        this.mGsfGmsHandler.removeMessages(111);
        this.mGsfGmsHandler.sendMessageDelayed(this.mGsfGmsHandler.obtainMessage(111), 10000);
    }

    private void removeKillGsfMsg() {
        Log.i("GmsGsfPolicy", "remove kill gsf app msg !");
        this.mGsfGmsHandler.removeMessages(111);
    }
}
