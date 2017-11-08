package com.huawei.keyguard.data;

import android.content.Context;
import android.text.TextUtils;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;

public class KeyguardInfo {
    private static final String TAG = null;
    private static KeyguardInfo inst = null;
    private Context mContext;
    private int mKeyguardNotificationSize;
    private LockPatternUtils mLockPatternUtils = null;
    private boolean mShowOnKeyguard;

    public static KeyguardInfo getInst(Context context) {
        KeyguardInfo keyguardInfo;
        synchronized (KeyguardInfo.class) {
            if (inst == null) {
                inst = new KeyguardInfo(context.getApplicationContext());
            }
            keyguardInfo = inst;
        }
        return keyguardInfo;
    }

    private KeyguardInfo(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    public boolean isOwnerInfoEnabled(int userId) {
        return this.mLockPatternUtils.isOwnerInfoEnabled(userId);
    }

    public String getOwnerInfo(int userId) {
        String str = null;
        try {
            if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
                str = this.mLockPatternUtils.getDeviceOwnerInfo();
            }
            if (!TextUtils.isEmpty(str)) {
                return str;
            }
            if (isOwnerInfoEnabled(userId)) {
                str = this.mLockPatternUtils.getOwnerInfo(userId);
            }
            if (TextUtils.isEmpty(str)) {
                str = BuildConfig.FLAVOR;
            }
            return str;
        } catch (SecurityException ex) {
            HwLog.w(TAG, "getOwnerInfo fail", ex);
        }
    }

    public String getDeviceInfo() {
        return getDeviceInfo(OsUtils.getCurrentUser());
    }

    public String getDeviceInfo(int userId) {
        boolean isRemoteLock = RemoteLockUtils.isDeviceRemoteLocked(this.mContext);
        String remoteInfo = RemoteLockUtils.getDeviceRemoteLockedInfo(this.mContext);
        if (!isRemoteLock || TextUtils.isEmpty(remoteInfo)) {
            return getOwnerInfo(userId);
        }
        HwLog.i(TAG, "device remote locked.");
        return remoteInfo;
    }

    public int getKeyguardNotificationSize() {
        return this.mKeyguardNotificationSize;
    }

    public boolean getShowOnKeyguard() {
        return this.mShowOnKeyguard;
    }

    public void updateNotificationOnKeyguard(boolean show) {
        this.mShowOnKeyguard = show;
    }

    public void setKeyguardNotificationSize(int keyguardNotificationSize) {
        this.mKeyguardNotificationSize = keyguardNotificationSize;
        if (this.mShowOnKeyguard) {
            AppHandler.sendMessage(4);
        }
    }
}
