package com.huawei.systemmanager.rainbow.client.tipsmanager;

public class NotificationConfigInfo {
    private int mPermissionCfg;
    private int mPermissionCode;
    private String mPermissionTrust;
    private boolean mPermissionValid;
    private String mPkgName;
    private int mSendNotification;
    private boolean mSendNotificationValid;

    public NotificationConfigInfo(String pkgName) {
        this.mPkgName = null;
        this.mPermissionCode = 0;
        this.mPermissionCfg = 0;
        this.mPermissionTrust = "false";
        this.mPermissionValid = false;
        this.mSendNotification = 2;
        this.mSendNotificationValid = false;
        this.mPkgName = pkgName;
        this.mPermissionCode = 0;
        this.mPermissionCfg = 0;
        this.mPermissionTrust = "false";
        this.mPermissionValid = false;
        this.mSendNotification = 2;
        this.mSendNotificationValid = false;
    }

    public NotificationConfigInfo() {
        this.mPkgName = null;
        this.mPermissionCode = 0;
        this.mPermissionCfg = 0;
        this.mPermissionTrust = "false";
        this.mPermissionValid = false;
        this.mSendNotification = 2;
        this.mSendNotificationValid = false;
        this.mPkgName = "";
        this.mPermissionCode = 0;
        this.mPermissionCfg = 0;
        this.mPermissionTrust = "false";
        this.mPermissionValid = false;
        this.mSendNotification = 2;
        this.mSendNotificationValid = false;
    }

    public NotificationConfigInfo(NotificationConfigInfo configInfo) {
        this.mPkgName = null;
        this.mPermissionCode = 0;
        this.mPermissionCfg = 0;
        this.mPermissionTrust = "false";
        this.mPermissionValid = false;
        this.mSendNotification = 2;
        this.mSendNotificationValid = false;
        this.mPkgName = configInfo.mPkgName;
        this.mPermissionCode = configInfo.mPermissionCode;
        this.mPermissionCfg = configInfo.mPermissionCfg;
        this.mPermissionTrust = configInfo.mPermissionTrust;
        this.mPermissionValid = configInfo.mPermissionValid;
        this.mSendNotification = configInfo.mSendNotification;
        this.mSendNotificationValid = configInfo.mSendNotificationValid;
    }

    public String toString() {
        return "NotificationConfigInfo [mPkgName=" + this.mPkgName + ", mPermissionCode=" + this.mPermissionCode + ", mPermissionCfg=" + this.mPermissionCfg + ", mPermissionTrust=" + this.mPermissionTrust + ", mPermissionValid=" + this.mPermissionValid + ", mSendNotification=" + this.mSendNotification + ", mSendNotificationValid=" + this.mSendNotificationValid + "]";
    }

    public boolean equals(Object obj) {
        boolean z = true;
        boolean z2 = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NotificationConfigInfo)) {
            return false;
        }
        NotificationConfigInfo inObj = (NotificationConfigInfo) obj;
        if (this.mPermissionValid && this.mSendNotificationValid) {
            if (this.mPermissionCode != inObj.mPermissionCode || this.mPermissionCfg != inObj.mPermissionCfg) {
                z = false;
            } else if (this.mSendNotification != inObj.mSendNotification) {
                z = false;
            }
            return z;
        } else if (this.mPermissionValid) {
            if (this.mPermissionCode == inObj.mPermissionCode && this.mPermissionCfg == inObj.mPermissionCfg) {
                z2 = true;
            }
            return z2;
        } else if (!this.mSendNotificationValid) {
            return false;
        } else {
            if (this.mSendNotification != inObj.mSendNotification) {
                z = false;
            }
            return z;
        }
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }
}
