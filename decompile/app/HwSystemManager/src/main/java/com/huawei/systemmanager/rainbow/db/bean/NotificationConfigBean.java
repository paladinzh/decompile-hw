package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class NotificationConfigBean {
    private String mCanForbidden;
    private String mHeadsupCfg;
    private String mIsControlled;
    private String mLockscreenCfg;
    private String mNotificationCfg;
    private String mPackageName;
    private String mStatusbarCfg;

    public static NotificationConfigBean fromCursor(Cursor c, int pkgIndex, int canIndex, int notiIndex, int statBarIndex, int lockIndex, int headIndex, int ctlIndex) {
        NotificationConfigBean result = new NotificationConfigBean();
        result.mPackageName = c.getString(pkgIndex);
        result.mCanForbidden = c.getString(canIndex);
        result.mNotificationCfg = c.getString(notiIndex);
        result.mStatusbarCfg = c.getString(statBarIndex);
        result.mLockscreenCfg = c.getString(lockIndex);
        result.mHeadsupCfg = c.getString(headIndex);
        result.mIsControlled = c.getString(ctlIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public boolean getCanForbiddenValue() {
        return !"1".equals(this.mCanForbidden);
    }

    public String getNotificationCfg() {
        return this.mNotificationCfg;
    }

    public boolean getStatusbarCfg() {
        return !"1".equals(this.mStatusbarCfg);
    }

    public boolean getLockscreenCfg() {
        return !"1".equals(this.mLockscreenCfg);
    }

    public boolean getHeadsupCfg() {
        return !"1".equals(this.mHeadsupCfg);
    }

    @Deprecated
    public boolean isControlled() {
        return "0".equals(this.mIsControlled);
    }

    public String toString() {
        return "[NotificationConfigBean]mPackageName:" + this.mPackageName + ",mCanForbidden:" + this.mCanForbidden + ",mNotificationCfg:" + this.mNotificationCfg + ",mStatusbarCfg:" + this.mStatusbarCfg + ",mLockscreenCfg:" + this.mLockscreenCfg + ",mHeadsupCfg:" + this.mHeadsupCfg + ",mIsControlled:" + this.mIsControlled;
    }
}
