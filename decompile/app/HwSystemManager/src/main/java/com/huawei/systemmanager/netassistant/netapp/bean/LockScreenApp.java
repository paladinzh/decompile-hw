package com.huawei.systemmanager.netassistant.netapp.bean;

import android.graphics.drawable.Drawable;
import android.net.HwNetworkPolicyManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.control.NetAppPermissionExcutor;
import com.huawei.systemmanager.netassistant.netapp.entry.INetApp;
import com.huawei.systemmanager.netassistant.netapp.entry.INetAppControl;
import com.huawei.systemmanager.netassistant.netapp.entry.INetAppTraffic;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;

public abstract class LockScreenApp implements INetApp, INetAppControl, INetAppTraffic {
    public static final AlpComparator<LockScreenApp> NETASSISTANT_LOCKSCREEN_APP_COMPARATOR = new AlpComparator<LockScreenApp>() {
        public String getStringKey(LockScreenApp t) {
            return t.getLabel() != null ? t.getLabel().toString() : "";
        }
    };
    NetAppInfo mNetAppInfo;
    HwNetworkPolicyManager mPolicyManager = HwNetworkPolicyManager.from(GlobalContext.getContext());
    long mobileTraffic;
    long wifiTraffic;

    public abstract int getAppType();

    public LockScreenApp(int uid) {
        this.mNetAppInfo = NetAppInfo.buildInfo(uid);
    }

    public long getMobileTraffic() {
        return this.mobileTraffic;
    }

    public long getWifiTraffic() {
        return this.wifiTraffic;
    }

    public boolean isMobileAccess() {
        return (this.mPolicyManager.getHwUidPolicy(getUid()) & 1) == 0;
    }

    public boolean isWifiAccess() {
        return (this.mPolicyManager.getHwUidPolicy(getUid()) & 2) == 0;
    }

    public void accessMobile() {
        AppPermissionController controller = new AppPermissionController(0, 0, this.mNetAppInfo.mUid, getAppType());
        NetAppPermissionExcutor.execute(controller);
    }

    public void accessWifi() {
        AppPermissionController controller = new AppPermissionController(0, 1, this.mNetAppInfo.mUid, getAppType());
        NetAppPermissionExcutor.execute(controller);
    }

    public void denyMobile() {
        AppPermissionController controller = new AppPermissionController(1, 0, this.mNetAppInfo.mUid, getAppType());
        NetAppPermissionExcutor.execute(controller);
    }

    public void denyWifi() {
        AppPermissionController controller = new AppPermissionController(1, 1, this.mNetAppInfo.mUid, getAppType());
        NetAppPermissionExcutor.execute(controller);
    }

    public int getUid() {
        return this.mNetAppInfo == null ? -1 : this.mNetAppInfo.mUid;
    }

    public CharSequence getLabel() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.mAppLabel;
    }

    public boolean isMultiApp() {
        return this.mNetAppInfo == null ? false : this.mNetAppInfo.isMultiPkg;
    }

    public Drawable getIcon() {
        return this.mNetAppInfo == null ? null : this.mNetAppInfo.getIcon();
    }

    public String getMoreAppSummary() {
        if (isMultiApp()) {
            return GlobalContext.getString(R.string.net_assistant_more_application);
        }
        return "";
    }
}
