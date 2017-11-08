package com.huawei.powergenie.core.policy;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.Log;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.StateAction;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.integration.adapter.BroadcastAdapter;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import com.huawei.powergenie.integration.adapter.HardwareAdapter;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PolicyService extends BaseService implements IPolicy {
    private final ConfigUpdateManager mConfigUpdateManager;
    private final Context mContext;
    private ArrayList<String> mGmsAppList = new ArrayList();
    private final ICoreContext mICoreContext;
    private boolean mIsSupportExtrModeV2 = false;
    private int mOldPowerMode = 0;
    private int mPowerMode = 2;
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(boolean enabled) throws RemoteException {
            Log.i("PolicyService", "vr state change, enabled: " + enabled);
            PolicyService.this.processVRConnected(enabled);
        }
    };

    public PolicyService(ICoreContext coreContext, int crashCnt) {
        this.mICoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mConfigUpdateManager = new ConfigUpdateManager(coreContext, this);
    }

    public void start() {
        initPowerMode();
        initPolicy(this.mICoreContext.getContext());
        loadGmsAppList();
        registerVR();
    }

    public void onInputMsgEvent(MsgEvent event) {
        switch (event.getEventId()) {
            case 302:
                if (4 == this.mPowerMode) {
                    Log.i("PolicyService", "boot completed and exit extreme mode ");
                    processExtremeModeChanged(false);
                    return;
                }
                return;
            case 303:
                if (SharedPref.getCrashPid(this.mContext, 0) > 0) {
                    SharedPref.updateCrashPid(this.mContext, 0);
                }
                if (4 == this.mPowerMode) {
                    Log.i("PolicyService", "shutdown and exit extreme mode ");
                    processExtremeModeChanged(false);
                    return;
                }
                return;
            case 350:
                powerModeChanged(event.getIntent().getIntExtra("state", 0));
                return;
            case 358:
                this.mConfigUpdateManager.processCloundConfigEvent(event);
                return;
            case 361:
                processExtremeModeChanged(event.getIntent().getBooleanExtra("enable", false));
                return;
            case 363:
                processVRConnected(event.getIntent().getBooleanExtra("connected", false));
                return;
            default:
                return;
        }
    }

    public int getPowerMode() {
        return this.mPowerMode;
    }

    public int getOldPowerMode() {
        return this.mOldPowerMode;
    }

    public boolean isOffPowerMode() {
        return this.mPowerMode == 3;
    }

    public void sendExtremeMode(boolean enter) {
        BroadcastAdapter.sendFeatureEnable(this.mICoreContext.getContext(), !enter);
    }

    public boolean isSupportExtrModeV2() {
        return this.mIsSupportExtrModeV2;
    }

    public boolean isExtremeModeV2() {
        return this.mIsSupportExtrModeV2 && this.mPowerMode == 4;
    }

    public boolean supportScenarioRRC() {
        return SharedPref.getSettings(this.mICoreContext.getContext(), "packets_rrc", !this.mICoreContext.isHisiPlatform());
    }

    public boolean supportCinemaMode() {
        return HardwareAdapter.supportCinemaMode();
    }

    public boolean supportGmsGsfPolicy() {
        return this.mGmsAppList.size() > 0;
    }

    public boolean hasGmsApps() {
        int hasGms = SharedPref.getIntSettings(this.mICoreContext.getContext(), "has_gms", 0);
        if (hasGms == 0) {
            hasGms = isGmsInstalled(this.mICoreContext.getContext()) ? 1 : 2;
            SharedPref.updateIntSettings(this.mICoreContext.getContext(), "has_gms", hasGms);
        }
        if (1 == hasGms) {
            return true;
        }
        return false;
    }

    public void processGmsPkgChanged(boolean installed) {
        if (installed) {
            SharedPref.updateIntSettings(this.mContext, "has_gms", 1);
        } else {
            SharedPref.updateIntSettings(this.mICoreContext.getContext(), "has_gms", isGmsInstalled(this.mICoreContext.getContext()) ? 1 : 2);
        }
    }

    public boolean isGmsApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        return this.mGmsAppList.contains(pkgName);
    }

    private boolean loadGmsAppList() {
        this.mGmsAppList.clear();
        boolean loadAppList = XmlHelper.loadCustAppList("gms_app_list.xml", null, this.mGmsAppList);
        if (loadAppList) {
            return loadAppList;
        }
        this.mGmsAppList.clear();
        loadAppList = XmlHelper.loadResAppList(this.mContext, R.xml.gms_app_list, null, this.mGmsAppList);
        Log.i("PolicyService", "load default gms list result: " + loadAppList);
        return loadAppList;
    }

    private boolean isGmsInstalled(Context context) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        int packageNum = installedPackages != null ? installedPackages.size() : 0;
        int i = 0;
        while (i < packageNum) {
            String pkgName = ((PackageInfo) installedPackages.get(i)).packageName;
            if (pkgName == null || !this.mGmsAppList.contains(pkgName)) {
                i++;
            } else {
                Log.i("PolicyService", "isGmsInstalled pkgName:" + pkgName);
                return true;
            }
        }
        return false;
    }

    public boolean isDisabledGsfGms() {
        return SharedPref.isDisabledGsfGms(this.mICoreContext.getContext());
    }

    public void updateDisabledGsfGms(boolean disable) {
        SharedPref.writeDisabledGsfGms(this.mICoreContext.getContext(), disable);
    }

    public boolean isChinaMarketProduct() {
        return CommonAdapter.isChinaMarketProduct();
    }

    private void initPowerMode() {
        int mode = SystemProperties.getInt("persist.sys.smart_power", 0);
        if (mode == 0) {
            SystemProperties.set("persist.sys.smart_power", Integer.toString(2));
            mode = 2;
            Log.i("PolicyService", "First set power mode property.");
        }
        this.mIsSupportExtrModeV2 = SystemProperties.getBoolean("ro.config.power_saver_2", true);
        this.mPowerMode = mode;
        Log.i("PolicyService", "init power mode:" + mode + " mIsSupportExtrModeV2:" + this.mIsSupportExtrModeV2);
    }

    private void registerVR() {
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        Log.d("PolicyService", vrManager == null ? "service null" : "I got it! VrManagerService! ");
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Log.i("PolicyService", "Failed to register VR mode state listener: " + e);
            }
        }
    }

    private void processExtremeModeChanged(boolean enable) {
        if (enable && 4 != this.mPowerMode) {
            Log.i("PolicyService", "entry extreme power mode, old mode : " + this.mPowerMode);
            SharedPref.updateIntSettings(this.mContext, "old_power_mode", this.mPowerMode);
            powerModeChanged(4);
        } else if (enable || 4 != this.mPowerMode) {
            Log.e("PolicyService", "current mode: " + this.mPowerMode + " and not handle extrem mode changed:" + enable);
        } else {
            int newMode = SharedPref.getIntSettings(this.mContext, "old_power_mode", 2);
            SharedPref.removeSettingsKey(this.mContext, "old_power_mode");
            Log.i("PolicyService", "exit extreme power mode, restore mode : " + newMode);
            powerModeChanged(newMode);
        }
    }

    private void powerModeChanged(int newMode) {
        if (newMode < 1 || newMode > 4) {
            Log.e("PolicyService", "invalid new mode:" + newMode);
            return;
        }
        Log.i("PolicyService", "power mode:" + this.mPowerMode + " -> " + newMode);
        if (this.mPowerMode != newMode) {
            this.mOldPowerMode = this.mPowerMode;
            this.mPowerMode = newMode;
            SystemProperties.set("persist.sys.smart_power", Integer.toString(newMode));
            StateAction stAction = StateAction.obtain();
            stAction.resetAs(350, 1, "power mode");
            stAction.putExtra(newMode);
            notifyPowerActionChanged(this.mICoreContext, stAction);
            IThermal thermal = (IThermal) this.mICoreContext.getService("thermal");
            if (newMode == 3) {
                if (thermal != null) {
                    thermal.notifyUsePfmcThermalPolicy(true);
                }
            } else if (this.mOldPowerMode == 3 && thermal != null) {
                thermal.notifyUsePfmcThermalPolicy(false);
            }
        }
    }

    private void processVRConnected(boolean isConnected) {
        Log.i("PolicyService", "VR connected: " + isConnected);
        IThermal thermal = (IThermal) this.mICoreContext.getService("thermal");
        if (thermal != null) {
            thermal.notifyVRMode(isConnected);
        }
        StateAction stAction = StateAction.obtain();
        stAction.resetAs(361, 1, "vr mode");
        stAction.putExtra(isConnected);
        notifyPowerActionChanged(this.mICoreContext, stAction);
    }

    private void initPolicy(Context context) {
        int myPid = Process.myPid();
        int lastCrashPid = SharedPref.getCrashPid(context, 0);
        try {
            String verName = context.getPackageManager().getPackageInfo("com.huawei.powergenie", 0).versionName;
            Log.i("PolicyService", "mypid: " + myPid + " version: " + verName);
            String oldVersion = SharedPref.getVersion(context);
            String productVersion = SharedPref.getStringSettings(context, "product_version", "unknown");
            if (oldVersion == null || !oldVersion.equals(verName)) {
                Log.e("PolicyService", "new version :" + verName + " from:" + oldVersion);
                clearDatabase(context);
                SharedPref.clearSettingsPref(context);
                SharedPref.updateVersion(context, verName);
                SharedPref.updateStringSettings(context, "product_version", Build.DISPLAY);
            } else if (!(productVersion == null || productVersion.equals(Build.DISPLAY))) {
                Log.e("PolicyService", "Product version changed :" + productVersion + " to:" + Build.DISPLAY);
                deleteAppsDB(context);
                SharedPref.updateStringSettings(context, "product_version", Build.DISPLAY);
                SharedPref.updateSettings(context, "init_apps_finish", false);
            }
            PolicyInitialization.init(context);
        } catch (Exception e) {
            Log.e("PolicyService", "Create PG Exception :", e);
            if (lastCrashPid <= 0) {
                SharedPref.updateCrashPid(context, myPid);
                throw new IllegalArgumentException("!!!!A FATAL ERROR!!!!!");
            }
        }
        if (lastCrashPid > 0) {
            SharedPref.updateCrashPid(context, 0);
        }
    }

    private void clearDatabase(Context context) {
        File fDB = context.getDatabasePath("powergenie.db");
        if (fDB == null || !fDB.exists()) {
            Log.w("PolicyService", "not exist database: powergenie.db");
        } else {
            Log.i("PolicyService", "delete database: powergenie.db result:" + context.deleteDatabase("powergenie.db"));
        }
        deleteAppsDB(context);
    }

    private void deleteAppsDB(Context context) {
        File dbPath = context.getDatabasePath("pgstats.db");
        if (dbPath == null || !dbPath.exists()) {
            Log.w("PolicyService", "current not exist database: pgstats.db");
            return;
        }
        Log.i("PolicyService", "delete database: pgstats.db result:" + context.deleteDatabase("pgstats.db"));
    }

    protected void notifyAction(PowerAction action) {
        notifyPowerActionChanged(this.mICoreContext, action);
    }

    public boolean updateConfigList(String name, ArrayList<String> inOutList) {
        return this.mConfigUpdateManager.updateConfigList(name, inOutList);
    }

    public void dump(PrintWriter pw, String[] args) {
        this.mConfigUpdateManager.dump(pw, args);
    }
}
