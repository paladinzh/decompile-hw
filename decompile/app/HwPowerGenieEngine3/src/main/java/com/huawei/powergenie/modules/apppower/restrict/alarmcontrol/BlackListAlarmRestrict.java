package com.huawei.powergenie.modules.apppower.restrict.alarmcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.XmlHelper;
import java.util.ArrayList;

public class BlackListAlarmRestrict {
    private final AlarmRestrict mAlarmCtrl;
    private ArrayList<String> mAlarmRestricHasNetList = new ArrayList();
    private ArrayList<String> mAlarmRestricNoNetList = new ArrayList();
    private final Context mContext;
    private ArrayList<String> mCtrlSocketAlarmRestricList = new ArrayList();
    ArrayList<String> mExtrAlarmBlackList = new ArrayList<String>() {
        {
            add("com.huawei.systemmanager");
            add("com.huawei.android.chr");
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    BlackListAlarmRestrict.this.restrictAlarmList(true, true);
                    return;
                case 101:
                    BlackListAlarmRestrict.this.restrictAlarmList(false, true);
                    return;
                case 102:
                    BlackListAlarmRestrict.this.restrictAlarmList(true, false);
                    return;
                case 105:
                    Log.i("BlackListAlarmRestrict", "pending extreme mode blacklist apps alarm.");
                    BlackListAlarmRestrict.this.mAlarmCtrl.pendingAlarmsAdapter(BlackListAlarmRestrict.this.mExtrAlarmBlackList);
                    return;
                case 106:
                    Log.i("BlackListAlarmRestrict", "unpending extreme mode blacklist apps alarm.");
                    BlackListAlarmRestrict.this.mAlarmCtrl.unpendingAlarmsAdapter(BlackListAlarmRestrict.this.mExtrAlarmBlackList);
                    return;
                default:
                    return;
            }
        }
    };
    private final IAppManager mIAppManager;
    private final IAppPowerAction mIAppPowerAction;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private boolean mIsHasNetAlarmRestrict = false;
    private boolean mIsNoNetAlarmRestrict = false;
    private boolean mPhbHasNetBlackListUpdated = false;
    private boolean mPhbNoNetBlackListUpdated = false;

    protected BlackListAlarmRestrict(ICoreContext coreContext, AlarmRestrict alarmCtrl) {
        this.mICoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        this.mAlarmCtrl = alarmCtrl;
    }

    protected void handleAppsAlarm(String pkgName, int alarmType) {
    }

    protected void handleAlarmNetWorkChange() {
        if (!this.mICoreContext.isScreenOff() || this.mIPolicy.getPowerMode() == 3) {
            return;
        }
        if (this.mIPolicy.isExtremeModeV2()) {
            ArrayList<String> launcherWhiteList = new ArrayList();
            ArrayList<String> pkgsInLauncher = this.mIAppManager.getExtrModeV2ReserveApps();
            if (pkgsInLauncher != null && pkgsInLauncher.size() > 0) {
                for (String pkg : pkgsInLauncher) {
                    if (this.mAlarmRestricNoNetList.contains(pkg)) {
                        launcherWhiteList.add(pkg);
                    }
                }
                if (this.mIDeviceState.isNetworkConnected()) {
                    Log.i("BlackListAlarmRestrict", "network connected, unpending launcher whitelist: " + launcherWhiteList);
                    disableAlarm(false, launcherWhiteList, true);
                } else {
                    Log.i("BlackListAlarmRestrict", "network disconnected, pending launcher whitelist: " + launcherWhiteList);
                    disableAlarm(true, launcherWhiteList, true);
                }
            }
            return;
        }
        if (this.mIDeviceState.isNetworkConnected()) {
            Log.i("BlackListAlarmRestrict", "network connected.");
            delayUnpendingNoNetAlarms();
        } else {
            Log.i("BlackListAlarmRestrict", "network disconnected.");
            delayPendingAlarms(true);
        }
    }

    protected void handleScreenState(boolean screenOn) {
        if (screenOn) {
            restoreAlarmRestrict(false);
            if (this.mCtrlSocketAlarmRestricList.size() > 0) {
                disableCtrlSocketAlarm(false, this.mCtrlSocketAlarmRestricList);
                this.mCtrlSocketAlarmRestricList.clear();
                return;
            }
            return;
        }
        if (!this.mIDeviceState.isNetworkConnected()) {
            delayPendingAlarms(true);
        }
        delayPendingAlarms(false);
    }

    protected void handleScrStateInExtremeMode(boolean screenOn) {
        if (this.mIPolicy.isExtremeModeV2()) {
            if (screenOn) {
                if (this.mIsNoNetAlarmRestrict) {
                    disableAlarm(false, this.mAlarmRestricNoNetList, true);
                }
                if (this.mCtrlSocketAlarmRestricList.size() > 0) {
                    disableCtrlSocketAlarm(false, this.mCtrlSocketAlarmRestricList);
                    this.mCtrlSocketAlarmRestricList.clear();
                }
                this.mHandler.removeMessages(105);
                postDelayAlarmMsg(106, 3000);
            } else {
                if (!this.mIsNoNetAlarmRestrict) {
                    if (this.mIDeviceState.isNetworkConnected()) {
                        ArrayList<String> restrictNoNetList = new ArrayList();
                        restrictNoNetList.addAll(this.mAlarmRestricNoNetList);
                        ArrayList<String> extrModeReserveApps = this.mIAppManager.getExtrModeV2ReserveApps();
                        if (extrModeReserveApps != null && extrModeReserveApps.size() > 0) {
                            restrictNoNetList.removeAll(extrModeReserveApps);
                            Log.i("BlackListAlarmRestrict", "remove launcher apps: " + extrModeReserveApps);
                        }
                        disableAlarm(true, restrictNoNetList, true);
                    } else {
                        disableAlarm(true, this.mAlarmRestricNoNetList, true);
                    }
                }
                this.mHandler.removeMessages(106);
                postDelayAlarmMsg(105, 3000);
            }
        }
    }

    protected void handlePowerModeChg(int newMode) {
        if (newMode == 4) {
            restrictAlarmInExtrMode(true);
        } else if (newMode == 3) {
            restoreAlarmRestrict(false);
        } else if (this.mIPolicy.getOldPowerMode() == 4) {
            restrictAlarmInExtrMode(false);
        }
    }

    protected void handleCtrlSocketAction(boolean state, String applistStr) {
        Log.i("BlackListAlarmRestrict", "handleCtrlSocketAction : socket state = " + state + ", applistStr = " + applistStr);
        if (!this.mICoreContext.isScreenOff()) {
            Log.w("BlackListAlarmRestrict", "mobile current state is screen on!");
        } else if (this.mIPolicy.getPowerMode() != 2) {
            Log.i("BlackListAlarmRestrict", "ctrl socket do nothing in power mode: " + this.mIPolicy.getPowerMode());
        } else if (this.mCtrlSocketAlarmRestricList.size() > 0) {
            Log.w("BlackListAlarmRestrict", "current scroff has sent once, not to excute !");
        } else if (this.mIsNoNetAlarmRestrict) {
            Log.w("BlackListAlarmRestrict", "no net alarm has restricted!");
            updateCtrlSockAlarmRestrictList(state, applistStr);
        } else {
            if (updateCtrlSockAlarmRestrictList(state, applistStr)) {
                disableCtrlSocketAlarm(true, this.mCtrlSocketAlarmRestricList);
            }
        }
    }

    protected boolean initialAlarmRestrict() {
        loadRestrictApps(R.xml.phb_no_net, "phb_no_net", this.mAlarmRestricNoNetList);
        loadRestrictApps(R.xml.phb_scroff_blacklist, "phb_scroff_blacklist", this.mAlarmRestricHasNetList);
        if (this.mAlarmRestricNoNetList.size() == 0 && this.mAlarmRestricHasNetList.size() == 0) {
            Log.i("BlackListAlarmRestrict", "no any alarm list");
            return false;
        }
        Log.i("BlackListAlarmRestrict", "phb_no_net : " + this.mAlarmRestricNoNetList);
        Log.i("BlackListAlarmRestrict", "phb_scroff_blacklist : " + this.mAlarmRestricHasNetList);
        mergeNoNetListAndNetList();
        return true;
    }

    protected void restoreAlarmRestrict(boolean force) {
        removeAllPendingAlarmMsg();
        if (force || this.mIsNoNetAlarmRestrict) {
            disableAlarm(false, this.mAlarmRestricNoNetList, true);
        }
        if (force || this.mIsHasNetAlarmRestrict) {
            disableAlarm(false, this.mAlarmRestricHasNetList, false);
        }
    }

    private void restrictAlarmList(boolean disable, boolean isNoNetOperate) {
        if (isNoNetOperate) {
            if (disable != this.mIsNoNetAlarmRestrict) {
                if (disable && this.mPhbNoNetBlackListUpdated) {
                    synchronized (this) {
                        if (loadRestrictApps(R.xml.phb_no_net, "phb_no_net", this.mAlarmRestricNoNetList)) {
                            Log.i("BlackListAlarmRestrict", "reload phb_no_net: " + this.mAlarmRestricNoNetList);
                            mergeNoNetListAndNetList();
                        }
                        this.mPhbNoNetBlackListUpdated = false;
                    }
                }
                disableAlarm(disable, this.mAlarmRestricNoNetList, true);
            }
        } else if (disable != this.mIsHasNetAlarmRestrict) {
            if (disable && this.mPhbHasNetBlackListUpdated) {
                synchronized (this) {
                    if (loadRestrictApps(R.xml.phb_scroff_blacklist, "phb_scroff_blacklist", this.mAlarmRestricHasNetList)) {
                        Log.i("BlackListAlarmRestrict", "reload phb_scroff_blacklist: " + this.mAlarmRestricHasNetList);
                        mergeNoNetListAndNetList();
                    }
                    this.mPhbHasNetBlackListUpdated = false;
                }
            }
            disableAlarm(disable, this.mAlarmRestricHasNetList, false);
        }
    }

    private void disableAlarm(boolean disable, ArrayList<String> restrictList, boolean isNoNetOperate) {
        boolean result;
        if (!disable) {
            result = this.mAlarmCtrl.unpendingAlarmsAdapter(this.mAlarmCtrl.getUnPendingAlarms(restrictList, 1));
        } else if (!isNoNetOperate || this.mCtrlSocketAlarmRestricList.size() <= 0) {
            result = this.mAlarmCtrl.pendingAlarmsAdapter(restrictList);
        } else {
            ArrayList<String> restrictAlarmList = new ArrayList(restrictList);
            restrictAlarmList.removeAll(this.mCtrlSocketAlarmRestricList);
            if (restrictAlarmList.size() <= 0) {
                Log.i("BlackListAlarmRestrict", "restrictAlarmList.size() <= 0");
                return;
            }
            result = this.mAlarmCtrl.pendingAlarmsAdapter(restrictAlarmList);
        }
        if (!result) {
            Log.e("BlackListAlarmRestrict", "fail pending alarm state:" + disable);
        } else if (isNoNetOperate) {
            String str;
            this.mIsNoNetAlarmRestrict = disable;
            String str2 = "BlackListAlarmRestrict";
            StringBuilder stringBuilder = new StringBuilder();
            if (disable) {
                str = "[pending";
            } else {
                str = "[resume";
            }
            Log.i(str2, stringBuilder.append(str).append("] no net alarms").toString());
        } else {
            this.mIsHasNetAlarmRestrict = disable;
            Log.i("BlackListAlarmRestrict", (disable ? "[pending" : "[resume") + "] net alarms");
        }
    }

    protected boolean isAlarmRestrict(String pkg) {
        if (pkg == null || ((!this.mIsNoNetAlarmRestrict || !this.mAlarmRestricNoNetList.contains(pkg)) && (!this.mIsHasNetAlarmRestrict || !this.mAlarmRestricHasNetList.contains(pkg)))) {
            return false;
        }
        return true;
    }

    private void restrictAlarmInExtrMode(boolean restrict) {
        if (restrict) {
            if (!this.mIPolicy.isExtremeModeV2()) {
                postDelayAlarmMsg(100, 3000);
            }
            postDelayAlarmMsg(102, 3000);
            return;
        }
        restoreAlarmRestrict(false);
    }

    private void postDelayAlarmMsg(int msgTag, long delayMs) {
        this.mHandler.removeMessages(msgTag);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgTag), delayMs);
    }

    private void removeAllPendingAlarmMsg() {
        if (this.mHandler.hasMessages(100)) {
            this.mHandler.removeMessages(100);
        }
        if (this.mHandler.hasMessages(101)) {
            this.mHandler.removeMessages(101);
        }
        if (this.mHandler.hasMessages(102)) {
            this.mHandler.removeMessages(102);
        }
    }

    private void delayPendingAlarms(boolean isNoNetAlarms) {
        postDelayAlarmMsg(isNoNetAlarms ? 100 : 102, 3000);
    }

    private void delayUnpendingNoNetAlarms() {
        postDelayAlarmMsg(101, 3000);
    }

    private boolean updateCtrlSockAlarmRestrictList(boolean state, String applistStr) {
        ArrayList<String> restrictNoNetList = new ArrayList();
        restrictNoNetList.addAll(this.mAlarmRestricNoNetList);
        if (state) {
            if (applistStr == null) {
                Log.w("BlackListAlarmRestrict", "no apps register socket. why state is open!");
                return false;
            }
            String[] appsArray = applistStr.split("\t");
            int len = appsArray.length;
            for (int i = 0; i < len; i++) {
                if (this.mAlarmRestricNoNetList.contains(appsArray[i])) {
                    restrictNoNetList.remove(appsArray[i]);
                }
            }
        }
        this.mCtrlSocketAlarmRestricList.addAll(restrictNoNetList);
        return true;
    }

    private void disableCtrlSocketAlarm(boolean disable, ArrayList<String> restrictList) {
        boolean result;
        if (disable) {
            result = this.mAlarmCtrl.pendingAlarmsAdapter(restrictList);
        } else {
            result = this.mAlarmCtrl.unpendingAlarmsAdapter(this.mAlarmCtrl.getUnPendingAlarms(restrictList, 4));
        }
        if (result) {
            String str;
            String str2 = "BlackListAlarmRestrict";
            StringBuilder stringBuilder = new StringBuilder();
            if (disable) {
                str = "[pending";
            } else {
                str = "[resume";
            }
            Log.i(str2, stringBuilder.append(str).append("] ctrl socket alarms").toString());
            return;
        }
        Log.e("BlackListAlarmRestrict", "fail pending ctrl socket alarm state:" + disable);
    }

    protected boolean isCtrlSocketAlarmRestrict(String pkg) {
        if (pkg == null || !this.mCtrlSocketAlarmRestricList.contains(pkg)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleUpdateConfig(String type, String uri) {
        if (!(type == null || uri == null || !type.equals("pg_config_list"))) {
            synchronized (this) {
                this.mPhbNoNetBlackListUpdated = true;
                this.mPhbHasNetBlackListUpdated = true;
            }
        }
    }

    private boolean loadRestrictApps(int resId, String name, ArrayList<String> out) {
        out.clear();
        XmlHelper.loadResAppList(this.mContext, resId, "black_list", out);
        this.mIPolicy.updateConfigList(name, out);
        return true;
    }

    private void mergeNoNetListAndNetList() {
        if (this.mAlarmRestricHasNetList.size() > 0) {
            for (String pName : this.mAlarmRestricHasNetList) {
                if (this.mAlarmRestricNoNetList.contains(pName)) {
                    this.mAlarmRestricNoNetList.remove(pName);
                }
            }
        }
    }
}
