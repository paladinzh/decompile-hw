package com.huawei.systemmanager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Process;
import android.os.UserHandle;
import com.huawei.harassmentinterception.service.HarassmentInterceptionService;
import com.huawei.netassistant.wifisecure.HsmWifiDetectManager;
import com.huawei.notificationmanager.restore.ReStoreNotificationService;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.permission.HoldService;
import com.huawei.permissionmanager.utils.SuperAppPermisionChecker;
import com.huawei.systemmanager.antivirus.AntiVirusService;
import com.huawei.systemmanager.antivirus.engine.AntiVirusEngineFactory;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.applock.datacenter.AppLockService;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverDataCenter;
import com.huawei.systemmanager.power.service.BgPowerManagerService;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.sdk.tmsdk.TMSdkEngine;
import com.huawei.systemmanager.service.MainService;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.AppCleanUpService;
import com.huawei.systemmanager.spacecleanner.service.StorageMonitorService;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference;
import com.huawei.systemmanager.startupmgr.service.AccountMonitorService;
import com.huawei.systemmanager.startupmgr.service.StartupResidentService;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;
import com.huawei.systemmanager.util.procpolicy.ProcessUtil;
import com.huawei.systemmanager.widget.OneKeyCleanActivity;

public class SystemManagerApplication extends Application {
    private static final String TAG = "SystemManagerApplication";

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GlobalContext.setContext(getApplicationContext());
        HsmPackageManager.getInstance().onConfigurationChanged(newConfig);
    }

    public void onCreate() {
        super.onCreate();
        GlobalContext.setContext(getApplicationContext());
        HwLog.i(TAG, "onCreate");
        if (new ProcessPolicy().shouldInitTmsEngine()) {
            setEMUITheme();
        }
        Utility.initWifiDataOnlyStatus();
        if (ProcessPolicy.shouldCheckNetworkSetting()) {
            UserAgreementHelper.resetNetworkSettings(getApplicationContext());
        }
        if (AntiVirusTools.isAbroad()) {
            UserAgreementHelper.turnOnNetSettings();
        }
        if (Utility.isFirstBoot(getApplicationContext())) {
            SpaceSettingPreference.getDefault().initSettings();
            if (ProcessUtil.getInstance().isServiceProcess()) {
                DataSaverDataCenter.initDaSaverProtectedList(getApplicationContext());
            }
        } else if (new ProcessPolicy().shouldCheckSpaceSetting()) {
            SpaceSettingPreference.getDefault().checkSettings();
        }
        SysCoreUtils.initDarkThemeStateForAppStart();
        startServices();
        initData();
    }

    private void setEMUITheme() {
        Resources res = getResources();
        if (res != null) {
            int themeId = res.getIdentifier(OneKeyCleanActivity.EMUI_THEME, null, null);
            if (themeId != 0) {
                setTheme(themeId);
                return;
            }
            return;
        }
        HwLog.w(TAG, "setEMUITheme got null Resources!");
    }

    private void startServices() {
        startMainService();
        startPermissionService();
        startNotificatinService();
        startHarassmentInterceptionService();
        startBgStaticsService();
        startAppLockService();
        startDeviceStorageMonitorService();
        startSpaceCleanService();
        startStarupService();
        startAccountMonitorService();
        startAntivirusService();
        restoreBrokenNotification();
        startCoreService();
    }

    private void startAccountMonitorService() {
        startService(new Intent(this, AccountMonitorService.class));
    }

    private void restoreBrokenNotification() {
        if (!Helper.isRestoreBrokenNotificationCompleted(GlobalContext.getContext())) {
            GlobalContext.getContext().startService(new Intent(GlobalContext.getContext(), ReStoreNotificationService.class));
        }
    }

    private void startCoreService() {
        startService(new Intent(this, CoreService.class));
    }

    private void initData() {
        initHsmStat();
        SuperAppPermisionChecker.getInstance(this);
        if (new ProcessPolicy().shouldInitTmsEngine() && TMSEngineFeature.shouldInitTmsEngine()) {
            try {
                TMSdkEngine.initTMSDK(getApplicationContext());
                TMSEngineFeature.setSupportTMS(true);
            } catch (Exception e) {
                HwLog.e(TAG, "init tms5 engine failed");
                e.printStackTrace();
            } catch (Error error) {
                HwLog.e(TAG, "init tms5 engine failed:" + error);
            }
        }
        if (new ProcessPolicy().shouldInitTmsEngine() && AntiVirusTools.isAbroad()) {
            AntiVirusEngineFactory.newInstance().onInit(getApplicationContext());
        }
        HsmWifiDetectManager.initWifiSecFeature(getApplicationContext());
    }

    private void startPermissionService() {
        startService(new Intent(this, HoldService.class));
    }

    private void startNotificatinService() {
    }

    private void startHarassmentInterceptionService() {
        startServiceAsUser(new Intent(this, HarassmentInterceptionService.class), UserHandle.OWNER);
    }

    private void startBgStaticsService() {
        startService(new Intent(this, BgPowerManagerService.class));
    }

    private void startDeviceStorageMonitorService() {
        startService(new Intent(this, StorageMonitorService.class));
    }

    private void startAppLockService() {
        if (AppLockPwdUtils.isPasswordSet(GlobalContext.getContext())) {
            Intent intent = new Intent(this, AppLockService.class);
            intent.addFlags(0);
            startService(intent);
        }
    }

    private void startSpaceCleanService() {
        startService(new Intent(this, AppCleanUpService.class));
    }

    private void startStarupService() {
        startService(new Intent(this, StartupResidentService.class));
    }

    private void startAntivirusService() {
        startService(new Intent(this, AntiVirusService.class));
    }

    public static void startMainService() {
        Context ctx = GlobalContext.getContext();
        Intent intent = new Intent(ctx, MainService.class);
        intent.putExtra(HsmStatConst.KEY_PROCESS_ID, Process.myPid());
        ctx.startService(intent);
    }

    private void initHsmStat() {
        HsmStat.init(this);
    }
}
