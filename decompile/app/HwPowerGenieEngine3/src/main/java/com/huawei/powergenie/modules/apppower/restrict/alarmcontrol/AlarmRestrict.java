package com.huawei.powergenie.modules.apppower.restrict.alarmcontrol;

import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.PowerAction;
import java.util.ArrayList;

public final class AlarmRestrict {
    private final Handler mAlarmHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    AlarmRestrict.this.handleUnlockScreen();
                    return;
                default:
                    return;
            }
        }
    };
    private BlackListAlarmRestrict mBlackAlarmRestrict = null;
    private GmsGsfPolicy mGsfAlarmRestrict = null;
    private final IAppPowerAction mIAppPowerAction;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private ICoreContext mPGContext;
    private ArrayList<String> mPMPendingPkgsAlarms = new ArrayList();
    private UnifiedHeartbeat mUnifiedHeartbeat = null;

    public AlarmRestrict(ICoreContext pgcontext) {
        this.mPGContext = pgcontext;
        this.mIPolicy = (IPolicy) pgcontext.getService("policy");
        this.mIDeviceState = (IDeviceState) pgcontext.getService("device");
        this.mIAppPowerAction = (IAppPowerAction) pgcontext.getService("appmamager");
        if (SystemProperties.getBoolean("persist.sys.shb.switcher", false)) {
            this.mUnifiedHeartbeat = new UnifiedHeartbeat(pgcontext);
        }
        this.mGsfAlarmRestrict = new GmsGsfPolicy(pgcontext, this);
        this.mBlackAlarmRestrict = new BlackListAlarmRestrict(pgcontext, this);
    }

    public boolean handleStart() {
        if (!this.mBlackAlarmRestrict.initialAlarmRestrict()) {
            this.mBlackAlarmRestrict = null;
        }
        if (!this.mGsfAlarmRestrict.initGSFAlarm()) {
            this.mGsfAlarmRestrict = null;
        }
        return true;
    }

    public void handleAction(PowerAction action) {
        switch (action.getActionId()) {
            case 208:
            case 230:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handleAppFront(action.getPkgName());
                    return;
                }
                return;
            case 224:
                handleAppsAlarm(action.getPkgName(), action.getExtraInt(), action.getExtraString(), action.getExtraValString("alarmIntent"));
                return;
            case 275:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handleCtsState(action.getExtraBoolean());
                    return;
                }
                return;
            case 300:
                handleScreenState(true);
                return;
            case 301:
                handleScreenState(false);
                return;
            case 302:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handleBootComplete();
                    return;
                }
                return;
            case 304:
                handleUnlockScreen();
                return;
            case 305:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handlePackageState(true, action.getExtraString());
                    return;
                }
                return;
            case 307:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handlePackageState(false, action.getExtraString());
                    return;
                }
                return;
            case 310:
                if (this.mUnifiedHeartbeat != null) {
                    this.mUnifiedHeartbeat.handlePowerContected();
                    return;
                }
                return;
            case 311:
                if (this.mUnifiedHeartbeat != null) {
                    this.mUnifiedHeartbeat.handlePowerDiscontected();
                    return;
                }
                return;
            case 312:
            case 314:
                if (this.mBlackAlarmRestrict != null) {
                    this.mBlackAlarmRestrict.handleAlarmNetWorkChange();
                    return;
                }
                return;
            case 324:
                Log.w("AlarmRestrict", "unpending all alarms!");
                this.mIAppPowerAction.unpendingAllAlarms();
                return;
            case 329:
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handleOperatorAction(false);
                    return;
                }
                return;
            case 350:
                if (this.mBlackAlarmRestrict != null) {
                    this.mBlackAlarmRestrict.handlePowerModeChg(action.getExtraInt());
                    return;
                }
                return;
            case 356:
                if (this.mBlackAlarmRestrict != null) {
                    requestPendingAlarms(action.getExtraBoolean(), action.getExtraListString(), action.getExtraInt());
                    return;
                }
                return;
            case 357:
                if (this.mBlackAlarmRestrict != null) {
                    this.mBlackAlarmRestrict.handleCtrlSocketAction(action.getExtraBoolean(), action.getExtraString());
                    return;
                }
                return;
            case 358:
                if (this.mBlackAlarmRestrict != null) {
                    this.mBlackAlarmRestrict.handleUpdateConfig(action.getExtraValString("pushType"), action.getExtraValString("uri"));
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void handleAppsAlarm(String pkgName, int alarmType, String interval, String alarmIntent) {
        if (this.mUnifiedHeartbeat != null) {
            this.mUnifiedHeartbeat.handleAppsAlarm(pkgName, alarmType, interval, alarmIntent);
        }
        if (this.mIPolicy.getPowerMode() != 3 && this.mBlackAlarmRestrict != null) {
            this.mBlackAlarmRestrict.handleAppsAlarm(pkgName, alarmType);
        }
    }

    private void handleScreenState(boolean screenOn) {
        if (this.mBlackAlarmRestrict != null) {
            this.mBlackAlarmRestrict.handleScrStateInExtremeMode(screenOn);
        }
        if (this.mIPolicy.getPowerMode() != 4) {
            if (screenOn) {
                if (this.mPMPendingPkgsAlarms.size() > 0) {
                    Log.i("AlarmRestrict", "pm pending alarms:" + this.mPMPendingPkgsAlarms);
                }
                if (!this.mIDeviceState.isKeyguardPresent()) {
                    this.mAlarmHandler.removeMessages(1000);
                    this.mAlarmHandler.sendMessageDelayed(this.mAlarmHandler.obtainMessage(1000), 5000);
                }
            } else {
                this.mAlarmHandler.removeMessages(1000);
                if (this.mGsfAlarmRestrict != null) {
                    this.mGsfAlarmRestrict.handleScreenState(screenOn);
                }
                if (this.mIPolicy.getPowerMode() != 3) {
                    if (this.mBlackAlarmRestrict != null) {
                        this.mBlackAlarmRestrict.handleScreenState(screenOn);
                    }
                    if (this.mUnifiedHeartbeat != null) {
                        this.mUnifiedHeartbeat.handleScreenState(screenOn);
                    }
                }
            }
        }
    }

    private void handleUnlockScreen() {
        if (!(this.mIPolicy.getPowerMode() == 4 || this.mPGContext.isScreenOff())) {
            if (this.mBlackAlarmRestrict != null) {
                this.mBlackAlarmRestrict.handleScreenState(true);
            }
            if (this.mGsfAlarmRestrict != null) {
                this.mGsfAlarmRestrict.handleScreenState(true);
            }
            if (this.mUnifiedHeartbeat != null) {
                this.mUnifiedHeartbeat.handleScreenState(true);
            }
        }
    }

    protected boolean pendingAlarmsAdapter(ArrayList<String> applist) {
        if (this.mIAppPowerAction != null) {
            return this.mIAppPowerAction.pendingAppAlarms(applist, false);
        }
        Log.e("AlarmRestrict", "pending adapter is null");
        return false;
    }

    protected boolean unpendingAlarmsAdapter(ArrayList<String> applist) {
        if (this.mIAppPowerAction != null) {
            return this.mIAppPowerAction.unpendingAppAlarms(applist, false);
        }
        Log.e("AlarmRestrict", "unpending adapter is null");
        return false;
    }

    protected void requestPendingAlarms(boolean pending, ArrayList<String> applist, int type) {
        if (applist == null || applist.size() == 0) {
            Log.w("AlarmRestrict", (pending ? "pending" : "resume") + " alarm, but app list is empty");
            return;
        }
        if (type == 1) {
            String str;
            ArrayList<String> requestApplist = new ArrayList();
            for (String pkg : applist) {
                if (this.mBlackAlarmRestrict == null || !this.mBlackAlarmRestrict.isCtrlSocketAlarmRestrict(pkg)) {
                    Log.w("AlarmRestrict", "Non socket ctrl restrict app : " + pkg);
                } else {
                    requestApplist.add(pkg);
                }
            }
            if (pending) {
                pendingAlarmsAdapter(requestApplist);
            } else {
                unpendingAlarmsAdapter(requestApplist);
            }
            String str2 = "AlarmRestrict";
            StringBuilder stringBuilder = new StringBuilder();
            if (pending) {
                str = "pending";
            } else {
                str = "resume";
            }
            Log.d(str2, stringBuilder.append(str).append(" alarm from type:").append(type).append(", app list:").append(requestApplist).toString());
        } else {
            if (pending) {
                if (pendingAlarmsAdapter(applist)) {
                    for (String pkg2 : applist) {
                        if (!this.mPMPendingPkgsAlarms.contains(pkg2)) {
                            this.mPMPendingPkgsAlarms.add(pkg2);
                        }
                    }
                }
            } else if (unpendingAlarmsAdapter(getUnPendingAlarms(applist, 3))) {
                for (String pkg22 : applist) {
                    if (this.mPMPendingPkgsAlarms.contains(pkg22)) {
                        this.mPMPendingPkgsAlarms.remove(pkg22);
                    }
                }
            }
            Log.d("AlarmRestrict", (pending ? "pending" : "resume") + " alarm from type:" + type + ", app list:" + applist);
        }
    }

    protected ArrayList<String> getUnPendingAlarms(ArrayList<String> pkglist, int type) {
        ArrayList<String> tmpFreezingPkgAlarms = new ArrayList();
        for (String pkg : pkglist) {
            if (type != 2 && this.mGsfAlarmRestrict != null && this.mGsfAlarmRestrict.isAlarmRestrict(pkg)) {
                Log.i("AlarmRestrict", "gsf alarms! not resume alarm:" + pkg);
            } else if (type != 1 && this.mBlackAlarmRestrict != null && this.mBlackAlarmRestrict.isAlarmRestrict(pkg)) {
                Log.i("AlarmRestrict", "blacklist alarms restrict! not resume alarm:" + pkg);
            } else if (type != 3 && this.mPMPendingPkgsAlarms != null && this.mPMPendingPkgsAlarms.contains(pkg)) {
                Log.i("AlarmRestrict", "pm pending alarm! not resume alarm:" + pkg);
            } else if (type == 4 || this.mBlackAlarmRestrict == null || !this.mBlackAlarmRestrict.isCtrlSocketAlarmRestrict(pkg)) {
                tmpFreezingPkgAlarms.add(pkg);
            } else {
                Log.i("AlarmRestrict", "no net ctrl socket pending alarm! not resume alarm:" + pkg);
            }
        }
        return tmpFreezingPkgAlarms;
    }
}
