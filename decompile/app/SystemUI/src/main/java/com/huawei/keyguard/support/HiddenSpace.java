package com.huawei.keyguard.support;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.huawei.android.os.UserManagerEx;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.util.List;

public class HiddenSpace {
    private static HiddenSpace mHiddenSpace = null;
    private boolean mHasAuthenticate = true;
    private boolean mIsSwitchUserByPassword = false;
    private SecurityMode mOwnerSecurityMode = SecurityMode.None;
    private SecurityMode mPrivateSecurityMode = SecurityMode.None;
    private int mPrivateUserId = -100;

    public int getmPrivateUserId() {
        return this.mPrivateUserId;
    }

    public SecurityMode getmPrivateSecurityMode() {
        return this.mPrivateSecurityMode;
    }

    public SecurityMode getmOwnerSecurityMode() {
        return this.mOwnerSecurityMode;
    }

    public boolean ismHasAuthenticate() {
        return this.mHasAuthenticate;
    }

    public boolean ismIsSwitchUserByPassword() {
        return this.mIsSwitchUserByPassword;
    }

    public void setmIsSwitchUserByPassword(boolean mIsSwitchUserByPassword) {
        this.mIsSwitchUserByPassword = mIsSwitchUserByPassword;
    }

    public static HiddenSpace getInstance() {
        HiddenSpace hiddenSpace;
        synchronized (HiddenSpace.class) {
            if (mHiddenSpace == null) {
                mHiddenSpace = new HiddenSpace();
            }
            hiddenSpace = mHiddenSpace;
        }
        return hiddenSpace;
    }

    public static boolean switchUserForHiddenSpace(Context context, int uid) {
        List<UserInfo> users = OsUtils.getAllUsers(context);
        if (users != null) {
            for (UserInfo info : users) {
                if (info.id == uid) {
                    HwLockScreenReporter.report(context, 161, BuildConfig.FLAVOR);
                    OsUtils.switchUser(uid);
                    return true;
                }
            }
        }
        HwLog.w("HiddenSpace", "can't switch ,because user is not exist!");
        return false;
    }

    public static boolean isCalling(Context context) {
        boolean result = false;
        if (context == null) {
            HwLog.w("HiddenSpace", "isCalling mContext is null ");
            return false;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (!(telephonyManager == null || telephonyManager.getCallState() == 0)) {
            result = true;
        }
        return result;
    }

    public static boolean isHiddenSpace(Context context, int uid) {
        UserManager um = (UserManager) context.getSystemService("user");
        if (um != null) {
            return isHiddenSpace(um.getUserInfo(uid));
        }
        HwLog.w("HiddenSpace", "get users fail");
        return false;
    }

    public static boolean isHiddenSpace(UserInfo info) {
        if (info != null) {
            return UserManagerEx.isHwHiddenSpace(info);
        }
        HwLog.w("HiddenSpace", "user is not exist");
        return false;
    }

    public static boolean isFingerPrintAllowForHiddenSpace(Context context, int uid) {
        if ((!isHiddenSpace(context, uid) && uid != 0) || uid == OsUtils.getCurrentUser() || !isCalling(context)) {
            return true;
        }
        HwLog.w("HiddenSpace", "Not allowed switch user when calling ");
        return false;
    }

    public static boolean isHiddenSpaceOrOwnerSwitchOnFpUnlock(Context context, int userId) {
        if (isHiddenSpace(context, userId)) {
            return FpUtils.isFingerprintEnabled(context, 0);
        }
        if (userId == 0) {
            List<UserInfo> users = OsUtils.getAllUsers(context);
            if (users != null) {
                for (UserInfo info : users) {
                    if (isHiddenSpace(info)) {
                        return FpUtils.isFingerprintEnabled(context, info.id);
                    }
                }
            }
        }
        return false;
    }

    public static int getPrivateUserId(Context context) {
        if (context == null) {
            HwLog.e("HiddenSpace", "context is null!");
            return -100;
        }
        List<UserInfo> users = OsUtils.getAllUsers(context);
        if (users != null) {
            for (UserInfo info : users) {
                if (isHiddenSpace(info)) {
                    return info.id;
                }
            }
        }
        HwLog.i("HiddenSpace", "don't find the hidden space userId!");
        return -100;
    }

    public static SecurityMode getLockMode(Context context, int uid) {
        if (context == null) {
            HwLog.e("HiddenSpace", "context is null, can't get lock mode!");
            return SecurityMode.None;
        }
        LockPatternUtilsEx mLockPatternUtils = VerifyPolicy.getInstance(context).getLockPatternUtils();
        if (mLockPatternUtils == null) {
            HwLog.e("HiddenSpace", "LockPatternUtils is null, can't get lock mode!");
            return SecurityMode.None;
        }
        int security = mLockPatternUtils.getActivePasswordQuality(uid);
        switch (security) {
            case 0:
                return SecurityMode.None;
            case 65536:
                return SecurityMode.Pattern;
            case 131072:
            case 196608:
                return SecurityMode.PIN;
            case 262144:
            case 327680:
            case 393216:
            case 524288:
                return SecurityMode.Password;
            default:
                throw new IllegalStateException("Unknown security quality:" + security);
        }
    }

    public void updateHiddenSpaceData(Context mContext) {
        HwLog.e("HiddenSpace", "update HiddenSpaceData!");
        this.mPrivateUserId = getPrivateUserId(mContext);
        this.mOwnerSecurityMode = getLockMode(mContext, 0);
        this.mHasAuthenticate = KeyguardUpdateMonitor.getInstance(mContext).getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
        if (this.mPrivateUserId != -100) {
            this.mPrivateSecurityMode = getLockMode(mContext, this.mPrivateUserId);
        }
    }
}
