package com.huawei.powergenie.modules.apppower.restrict.gpsrestrict;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.XmlHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public final class AppsGPSRestrict {
    private static final boolean RESTRICT_GPS_NORMAL_MODE = SystemProperties.getBoolean("ro.config.hw_restrict_gps", false);
    private static HashMap<String, Integer> mRestrictGpsBlacklist = new HashMap();
    private Context mContext;
    private String mFrontPkg = null;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    AppsGPSRestrict.this.startGpsRestrict();
                    return;
                default:
                    return;
            }
        }
    };
    private final IAppManager mIAppManager;
    private final IAppPowerAction mIAppPowerAction;
    private final IPolicy mIPolicy;
    private final IScenario mIScenario;

    public AppsGPSRestrict(ICoreContext coreContext) {
        this.mContext = coreContext.getContext();
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIScenario = (IScenario) coreContext.getService("scenario");
        if (RESTRICT_GPS_NORMAL_MODE || this.mIPolicy.getPowerMode() == 1) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 5000);
        }
    }

    public void handleAction(PowerAction action) {
        switch (action.getActionId()) {
            case 208:
            case 230:
                handleAppFront(action.getPkgName());
                return;
            case 324:
                if (RESTRICT_GPS_NORMAL_MODE || this.mIPolicy.getPowerMode() == 1) {
                    loadGpsRestrictBlackList();
                    restoreAllAppUseGps();
                    return;
                }
                return;
            case 350:
                handlePowerModeChange(action.getExtraInt());
                return;
            default:
                return;
        }
    }

    private void startGpsRestrict() {
        loadGpsRestrictBlackList();
        restrictAllAppUseGps();
    }

    private void stopGpsRestrict() {
        restoreAllAppUseGps();
        mRestrictGpsBlacklist.clear();
    }

    private void handleAppFront(String newFront) {
        if (RESTRICT_GPS_NORMAL_MODE || this.mIPolicy.getPowerMode() == 1) {
            handleGpsRestrict(newFront, this.mFrontPkg);
        }
        this.mFrontPkg = newFront;
    }

    private void handlePowerModeChange(int newMode) {
        if (RESTRICT_GPS_NORMAL_MODE || newMode == 1) {
            this.mHandler.removeMessages(1000);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 5000);
        } else if (this.mIPolicy.getOldPowerMode() != 1) {
        } else {
            if (this.mHandler.hasMessages(1000)) {
                this.mHandler.removeMessages(1000);
            } else {
                stopGpsRestrict();
            }
        }
    }

    private void handleGpsRestrict(String newFront, String oldFront) {
        if (mRestrictGpsBlacklist.containsKey(newFront) && ((Integer) mRestrictGpsBlacklist.get(newFront)).intValue() == 1) {
            Log.i("AppsGPSRestrict", "restore front app use gps...");
            restrictAppUseGps(false, newFront);
        }
        if (newFront == null || !newFront.equals(oldFront)) {
            ArrayList<String> frontAppList = this.mIScenario.getAboveLauncherPkgs();
            if (frontAppList == null || !frontAppList.contains(oldFront)) {
                if (mRestrictGpsBlacklist.containsKey(oldFront) && ((Integer) mRestrictGpsBlacklist.get(oldFront)).intValue() == 0) {
                    Log.i("AppsGPSRestrict", "restrict background app use gps...");
                    restrictAppUseGps(true, oldFront);
                }
                return;
            }
            Log.i("AppsGPSRestrict", oldFront + " is above launcher app, just return...");
            return;
        }
        Log.i("AppsGPSRestrict", oldFront + " the same as new front, just return...");
    }

    private void restrictAllAppUseGps() {
        Log.i("AppsGPSRestrict", "restrict all backlist app use gps...");
        for (Entry entry : mRestrictGpsBlacklist.entrySet()) {
            String name = (String) entry.getKey();
            if (this.mFrontPkg == null || this.mFrontPkg.equals(name)) {
                Log.i("AppsGPSRestrict", "front app cannot restrict use gps...");
            } else {
                restrictAppUseGps(true, name);
            }
        }
    }

    private void restoreAllAppUseGps() {
        Log.i("AppsGPSRestrict", "restore all app use gps...");
        for (Entry entry : mRestrictGpsBlacklist.entrySet()) {
            restrictAppUseGps(false, (String) entry.getKey());
        }
    }

    private void restrictAppUseGps(boolean restrict, String pkgName) {
        int state = restrict ? 1 : 0;
        mRestrictGpsBlacklist.put(pkgName, Integer.valueOf(state));
        int uid = this.mIAppManager.getUidByPkg(pkgName);
        if (uid > 0) {
            Log.i("AppsGPSRestrict", "name: " + pkgName + " GPS state: " + state);
            this.mIAppPowerAction.proxyApp(pkgName, uid, restrict, true);
        }
    }

    private void loadGpsRestrictBlackList() {
        ArrayList<String> restrictGpsBlacklist = new ArrayList();
        XmlHelper.loadResAppList(this.mContext, R.xml.gps_restrict_blacklist, null, restrictGpsBlacklist);
        this.mIPolicy.updateConfigList("gps_restrict_blacklist", restrictGpsBlacklist);
        Log.i("AppsGPSRestrict", "gps restrict blacklist: " + restrictGpsBlacklist);
        for (String name : restrictGpsBlacklist) {
            mRestrictGpsBlacklist.put(name, Integer.valueOf(0));
        }
    }
}
