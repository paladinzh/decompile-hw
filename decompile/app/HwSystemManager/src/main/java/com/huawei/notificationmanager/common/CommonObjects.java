package com.huawei.notificationmanager.common;

import android.content.ContentValues;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class CommonObjects {

    public static class NotificationCfgInfo {
        public static final AlpComparator<NotificationCfgInfo> NOTIFICATION_ALP_COMPARATOR = new AlpComparator<NotificationCfgInfo>() {
            public String getStringKey(NotificationCfgInfo t) {
                return t.mAppLabel;
            }
        };
        public Drawable mAppIcon;
        public String mAppLabel;
        public int mCanForbid;
        public int mCfg;
        public int mFirstStartCfg;
        public int mHeadsupCfg;
        private boolean mHideContent;
        public int mIndex;
        public int mLockscreenCfg;
        public String mPkgName;
        private int mSoundVibrate;
        public int mStatusbarCfg;
        public int mUid;

        private void initDefaultValue() {
            this.mAppLabel = null;
            this.mAppIcon = null;
            this.mPkgName = null;
            this.mUid = 0;
            this.mCfg = 0;
            this.mStatusbarCfg = 1;
            this.mHeadsupCfg = 0;
            this.mCanForbid = 1;
            this.mIndex = 10000;
            this.mHideContent = false;
            this.mSoundVibrate = 3;
            this.mFirstStartCfg = 0;
            initLockscreenCfg();
        }

        public void initLockscreenCfg() {
            if (AbroadUtils.isAbroad()) {
                this.mLockscreenCfg = 1;
                HwLog.d("NotificationCfgInfo", "mLockscreenCfgdefaultvalue=SWITCH_MODE_OPEN");
                return;
            }
            this.mLockscreenCfg = 0;
            HwLog.d("NotificationCfgInfo", "mLockscreenCfgdefaultvalue=SWITCH_MODE_CLOSD");
        }

        public NotificationCfgInfo() {
            initDefaultValue();
        }

        public NotificationCfgInfo(String pkgName) {
            initDefaultValue();
            this.mPkgName = pkgName;
        }

        public NotificationCfgInfo(int uid, String pkgName) {
            int i = 1;
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.mCfg = 1;
            this.mStatusbarCfg = 1;
            HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(this.mPkgName);
            if (pkgInfo != null) {
                this.mAppLabel = pkgInfo.label();
            }
            this.mHideContent = false;
            this.mSoundVibrate = 3;
            if (!NotificationUtils.isSystemRemovable(pkgName)) {
                i = 0;
            }
            this.mHeadsupCfg = i;
            initLockscreenCfg();
        }

        public NotificationCfgInfo(HsmPkgInfo pkgInfo) {
            initDefaultValue();
            this.mAppLabel = pkgInfo.label();
            this.mAppIcon = pkgInfo.icon();
            this.mPkgName = pkgInfo.mPkgName;
            this.mUid = pkgInfo.mUid;
            initLockscreenCfg();
        }

        public void copyCfgsFrom(NotificationCfgInfo target) {
            this.mCfg = target.mCfg;
            this.mLockscreenCfg = target.mLockscreenCfg;
            this.mStatusbarCfg = target.mStatusbarCfg;
            this.mHeadsupCfg = target.mHeadsupCfg;
            this.mCanForbid = target.mCanForbid;
            this.mIndex = target.mIndex;
            this.mHideContent = target.mHideContent;
            this.mSoundVibrate = target.mSoundVibrate;
            this.mFirstStartCfg = target.mFirstStartCfg;
        }

        public void copyCfgsFrom(ContentValues target) {
            boolean z;
            int intValue;
            int i = 0;
            Integer integer = target.getAsInteger(ConstValues.NOTIFICATION_CFG);
            this.mCfg = integer == null ? 0 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_LOCKSCREEN_CFG);
            this.mLockscreenCfg = integer == null ? 0 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_STATUSBAR_CFG);
            this.mStatusbarCfg = integer == null ? 0 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_HEADSUP_CFG);
            this.mHeadsupCfg = integer == null ? 0 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_CANFORBID);
            this.mCanForbid = integer == null ? 0 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_INDEX);
            this.mIndex = integer == null ? 10000 : integer.intValue();
            integer = target.getAsInteger(ConstValues.NOTIFICATION_HIDE_CONTENT);
            if (integer == null || integer.intValue() <= 0) {
                z = false;
            } else {
                z = true;
            }
            this.mHideContent = z;
            integer = target.getAsInteger(ConstValues.NOTIFICATION_SOUND_VIBRATE);
            if (integer != null) {
                intValue = integer.intValue();
            } else {
                intValue = 3;
            }
            this.mSoundVibrate = intValue;
            integer = target.getAsInteger(ConstValues.NOTIFICATION_FIRSTSTART_CFG);
            if (integer != null) {
                i = integer.intValue();
            }
            this.mFirstStartCfg = i;
        }

        public void parseCfgsFrom(Cursor cursor) {
            boolean z = false;
            if (cursor != null) {
                this.mPkgName = cursor.getString(cursor.getColumnIndex("packageName"));
                this.mCfg = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_CFG));
                this.mLockscreenCfg = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_LOCKSCREEN_CFG));
                this.mStatusbarCfg = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_STATUSBAR_CFG));
                this.mHeadsupCfg = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_HEADSUP_CFG));
                this.mCanForbid = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_CANFORBID));
                this.mIndex = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_INDEX));
                if (cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_HIDE_CONTENT)) != 0) {
                    z = true;
                }
                this.mHideContent = z;
                this.mSoundVibrate = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_SOUND_VIBRATE));
                this.mFirstStartCfg = cursor.getInt(cursor.getColumnIndex(ConstValues.NOTIFICATION_FIRSTSTART_CFG));
                HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(this.mPkgName);
                if (pkgInfo != null) {
                    this.mAppLabel = pkgInfo.label();
                    this.mUid = pkgInfo.getUid();
                }
            }
        }

        public ContentValues getAsContentValue() {
            ContentValues value = new ContentValues();
            value.put("packageName", this.mPkgName);
            value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(this.mCfg));
            value.put(ConstValues.NOTIFICATION_LOCKSCREEN_CFG, Integer.valueOf(this.mLockscreenCfg));
            value.put(ConstValues.NOTIFICATION_STATUSBAR_CFG, Integer.valueOf(this.mStatusbarCfg));
            value.put(ConstValues.NOTIFICATION_HEADSUP_CFG, Integer.valueOf(this.mHeadsupCfg));
            value.put(ConstValues.NOTIFICATION_CANFORBID, Integer.valueOf(this.mCanForbid));
            value.put(ConstValues.NOTIFICATION_INDEX, Integer.valueOf(this.mIndex));
            value.put(ConstValues.NOTIFICATION_HIDE_CONTENT, Integer.valueOf(this.mHideContent ? 1 : 0));
            value.put(ConstValues.NOTIFICATION_SOUND_VIBRATE, Integer.valueOf(this.mSoundVibrate));
            value.put(ConstValues.NOTIFICATION_FIRSTSTART_CFG, Integer.valueOf(this.mFirstStartCfg));
            return value;
        }

        public ContentValues getCfgUpdateContentValue() {
            ContentValues value = new ContentValues();
            value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(this.mCfg));
            value.put(ConstValues.NOTIFICATION_LOCKSCREEN_CFG, Integer.valueOf(this.mLockscreenCfg));
            value.put(ConstValues.NOTIFICATION_STATUSBAR_CFG, Integer.valueOf(this.mStatusbarCfg));
            value.put(ConstValues.NOTIFICATION_HEADSUP_CFG, Integer.valueOf(this.mHeadsupCfg));
            value.put(ConstValues.NOTIFICATION_HIDE_CONTENT, Integer.valueOf(this.mHideContent ? 1 : 0));
            value.put(ConstValues.NOTIFICATION_SOUND_VIBRATE, Integer.valueOf(this.mSoundVibrate));
            value.put(ConstValues.NOTIFICATION_FIRSTSTART_CFG, Integer.valueOf(this.mFirstStartCfg));
            return value;
        }

        public boolean canForbid() {
            return this.mCanForbid != 0;
        }

        public void setCanForbid(boolean canForbid) {
            this.mCanForbid = canForbid ? 1 : 0;
        }

        public boolean isMainNotificationEnabled() {
            return this.mCfg == 1;
        }

        public void setMainNotificationEnabled(boolean enable) {
            this.mCfg = enable ? 1 : 0;
        }

        public boolean isStatusbarNotificationEnabled() {
            return this.mStatusbarCfg == 1;
        }

        public void setStatusbarNotificationEnabled(boolean enable) {
            this.mStatusbarCfg = enable ? 1 : 0;
        }

        public boolean isHeadsupNotificationEnabled() {
            return this.mHeadsupCfg == 1;
        }

        public void setHeadsupNotificationEnable(boolean enable) {
            this.mHeadsupCfg = enable ? 1 : 0;
        }

        public boolean isLockscreenNotificationEnabled() {
            return this.mLockscreenCfg == 1;
        }

        public void setLockscreenNotificationEnable(boolean enable) {
            this.mLockscreenCfg = enable ? 1 : 0;
        }

        public boolean isHideContent() {
            return this.mHideContent;
        }

        public void setHideContent(boolean hideContent) {
            this.mHideContent = hideContent;
        }

        public boolean isSoundEnable() {
            return (this.mSoundVibrate & 1) != 0;
        }

        public void setSoundEnable(boolean enable) {
            if (enable) {
                this.mSoundVibrate |= 1;
            } else {
                this.mSoundVibrate &= -2;
            }
        }

        public boolean isVibrateEnable() {
            return (this.mSoundVibrate & 2) != 0;
        }

        public void setVibrateEnable(boolean enable) {
            if (enable) {
                this.mSoundVibrate |= 2;
            } else {
                this.mSoundVibrate &= -3;
            }
        }
    }

    public static class NotificationLogInfo {
        private static final String TAG = "NotificationLogInfo";
        private long mActionTime;
        private Drawable mAppIcon;
        private String mAppLabel;
        private int mAppUid;
        private int mId;
        private String mPkgName;
        private String mText;
        private String mTitle;

        public NotificationLogInfo() {
            this.mAppLabel = null;
            this.mAppIcon = null;
            this.mPkgName = null;
            this.mAppUid = -1;
            this.mTitle = null;
            this.mText = null;
            this.mActionTime = 0;
            this.mId = -1;
        }

        public NotificationLogInfo(NotificationLogInfo logInfo) {
            this.mAppLabel = logInfo.mAppLabel;
            this.mAppIcon = logInfo.mAppIcon;
            this.mPkgName = logInfo.mPkgName;
            this.mAppUid = logInfo.mAppUid;
            this.mTitle = logInfo.mTitle;
            this.mText = logInfo.mText;
            this.mActionTime = logInfo.mActionTime;
            this.mId = logInfo.mId;
        }

        public String getPackageName() {
            return this.mPkgName;
        }

        public static NotificationLogInfo createLogItem(Cursor cursor, int nColIndexPkgName, int nColIndexLogTime, int nColIndexLogTitle, int nColIndexLogText, int nColIndexId) {
            NotificationLogInfo logInfo = new NotificationLogInfo();
            logInfo.mPkgName = cursor.getString(nColIndexPkgName);
            try {
                HsmPkgInfo appInfo = HsmPackageManager.getInstance().getPkgInfo(logInfo.mPkgName, 8192);
                logInfo.mAppIcon = appInfo.icon();
                logInfo.mAppLabel = appInfo.label();
                logInfo.mActionTime = cursor.getLong(nColIndexLogTime);
                logInfo.mTitle = cursor.getString(nColIndexLogTitle);
                logInfo.mText = cursor.getString(nColIndexLogText);
                logInfo.mId = cursor.getInt(nColIndexId);
                return logInfo;
            } catch (NameNotFoundException e) {
                HwLog.w(TAG, "createLogItem: Fail to find app info for " + logInfo.mPkgName);
                return null;
            }
        }
    }
}
