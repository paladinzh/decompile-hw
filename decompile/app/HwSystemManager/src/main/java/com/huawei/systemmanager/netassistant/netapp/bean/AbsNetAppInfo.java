package com.huawei.systemmanager.netassistant.netapp.bean;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.entry.INetApp;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.util.HwLog;

public abstract class AbsNetAppInfo implements INetApp {
    public static final AlpComparator<AbsNetAppInfo> ABS_NET_APP_ALP_COMPARATOR = new AlpComparator<AbsNetAppInfo>() {
        public String getStringKey(AbsNetAppInfo t) {
            if (t == null || t.getLabel() == null) {
                return "";
            }
            return t.getLabel().toString();
        }
    };
    public static final int APP_TYPE_INSTALLED = 0;
    public static final int APP_TYPE_SYSTEM = 1;
    public static final int PERMISSION_ACCESS = 0;
    public static final int PERMISSION_DENY = 1;
    private static final String TAG = "AbsNetAppInfo";
    public static final int TYPE_MOBILE = 0;
    public static final int TYPE_WIFI = 1;
    protected NetAppInfo mNetAppInfo;
    public int permissionMobile;
    public int permissionWifi;

    public static class DefaultNetAppInfo extends AbsNetAppInfo {
        int uidType;

        public DefaultNetAppInfo(int uid, int pWifi, int pMobile, boolean isSystem) {
            super(uid);
            this.permissionMobile = pMobile;
            this.permissionWifi = pWifi;
            this.uidType = isSystem ? 1 : 0;
        }

        public int getUidType() {
            return this.uidType;
        }
    }

    public abstract int getUidType();

    public AbsNetAppInfo(int uid) {
        this.mNetAppInfo = NetAppInfo.buildInfo(uid);
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
        return this.mNetAppInfo.getIcon();
    }

    public String getMultiAppStringId() {
        if (isMultiApp()) {
            return GlobalContext.getString(R.string.net_assistant_more_application);
        }
        return "";
    }

    public int getPermissionCfg(AppPermissionController data) {
        if (data.getType() == 0) {
            return getPermissionCfg(data.getPermission(), this.permissionWifi);
        }
        if (data.getType() == 1) {
            return getPermissionCfg(this.permissionMobile, data.getPermission());
        }
        return 0;
    }

    public int getPermission(int type) {
        if (type == 0) {
            return this.permissionMobile;
        }
        if (type == 1) {
            return this.permissionWifi;
        }
        HwLog.e(TAG, "error permission type");
        return -1;
    }

    public void setPermission(int type, int permission) {
        if (type == 0) {
            this.permissionMobile = permission;
        } else if (type == 1) {
            this.permissionWifi = permission;
        } else {
            HwLog.e(TAG, "error permission type");
        }
    }

    public static int getWifiPermission(int permissionCfg) {
        if ((permissionCfg & 16384) == 0) {
            return 0;
        }
        return 1;
    }

    public static int getMobilePermission(int permissionCfg) {
        if ((permissionCfg & 8192) == 0) {
            return 0;
        }
        return 1;
    }

    public static int getPermissionCfg(int mobilePerm, int wifiPerm) {
        return (mobilePerm == 1 ? 8192 : 0) | (wifiPerm == 1 ? 16384 : 0);
    }

    public boolean isMobileAccess() {
        return this.permissionMobile == 0;
    }

    public boolean isWifiAccess() {
        return this.permissionWifi == 0;
    }
}
