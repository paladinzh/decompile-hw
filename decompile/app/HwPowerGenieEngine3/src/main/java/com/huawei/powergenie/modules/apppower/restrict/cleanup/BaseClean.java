package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IScenario;
import java.util.ArrayList;
import java.util.List;

public class BaseClean {
    public static final ArrayList<String> mForceProtectList = new ArrayList<String>() {
        {
            add("android");
            add("com.huawei.powergenie");
            add("com.android.calendar");
            add("com.android.providers.calendar");
            add("com.android.deskclock");
            add("com.android.systemui");
            add("com.android.mms");
            add("com.futurewei.ecens.mocalite");
            add("com.huawei.android.totemweather");
            add("com.huawei.android.FloatTasks");
            add("com.huawei.numberlocation");
            add("com.huawei.android.launcher");
            add("com.huawei.android.supersaver");
            add("com.huawei.android.remotecontrol");
            add("com.huawei.android.ds");
            add("com.google.android.gsf");
            add("com.google.android.gms");
            add("com.huawei.message");
            add("com.android.keyguard");
            add("com.huawei.floatMms");
            add("com.android.contacts");
            add("com.whatsapp");
        }
    };
    protected final ActivityManager mActivityManager = ((ActivityManager) this.mContext.getSystemService("activity"));
    protected final Context mContext;
    protected final IAppManager mIAppManager;
    protected final IAppPowerAction mIAppPowerAction;
    protected final IAppType mIAppType;
    protected final ICoreContext mICoreContext;
    protected final IDeviceState mIDeviceState;
    protected final IPolicy mIPolicy;
    protected final IScenario mIScenario;
    protected final PackageManager mPackageManager = this.mContext.getPackageManager();

    public BaseClean(ICoreContext coreContext) {
        this.mContext = coreContext.getContext();
        this.mICoreContext = coreContext;
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIScenario = (IScenario) coreContext.getService("scenario");
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
    }

    protected ArrayList<String> getImportentServices() {
        ArrayList<String> importentService = new ArrayList();
        List<RunningServiceInfo> services = this.mActivityManager.getRunningServices(100);
        int NS = services != null ? services.size() : 0;
        for (int i = 0; i < NS; i++) {
            RunningServiceInfo si = (RunningServiceInfo) services.get(i);
            if (!(((si.flags & 8) == 0 && (si.flags & 4) == 0) || si.service == null)) {
                String pkgName = si.service.getPackageName();
                if (pkgName != null && this.mIAppManager.isSystemApp(this.mContext, pkgName)) {
                    importentService.add(pkgName);
                }
            }
        }
        return importentService;
    }

    protected void forceStopApp(String pkgName, String reason) {
        this.mIAppPowerAction.forceStopApp(pkgName, reason);
    }

    protected String getDefaultInputMethod() {
        return this.mIAppType.getDefaultInputMethod();
    }

    protected ArrayList<String> getTopTasksApps(int tops) {
        return this.mIAppManager.getTopTasksApps(tops);
    }

    protected String getUsingLauncher() {
        return this.mIAppType.getUsingLauncher();
    }

    protected String getDefaultLauncher() {
        return this.mIAppType.getDefaultLauncher();
    }

    protected String getCurLiveWallpaper() {
        return this.mIAppType.getCurLiveWallpaper();
    }

    protected ArrayList<String> getAudioPlayingPkg() {
        return this.mIAppManager.getAudioPlayingPkg();
    }

    protected ArrayList<String> getActiveWidgetApp() {
        ArrayList<String> appWidgetPkgs = new ArrayList();
        this.mIAppManager.loadAppWidget(appWidgetPkgs);
        return appWidgetPkgs;
    }

    protected boolean wakeupApps(List<String> pkgNames, String reason) {
        return this.mIAppManager.wakeupApps(pkgNames, reason);
    }
}
