package com.huawei.keyguard;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.keyguard.HwCustKeyguardUpdateMonitor;
import com.android.keyguard.HwCustManager;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.android.keyguard.KeyguardUpdateMonitor.StrongAuthTracker;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.policy.FingerBlackCounter;
import com.huawei.keyguard.policy.FingerPrintPolicy;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.support.HiddenSpace;
import com.huawei.keyguard.support.OucScreenOnCounter;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.DisabledFeatureUtils;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.charge.ChargingAnimController;
import fyusion.vislib.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class HwKeyguardUpdateMonitor extends KeyguardUpdateMonitor {
    protected static HwKeyguardUpdateMonitor sInstance;
    private int mAuthedUser = -10000;
    private HwCustKeyguardUpdateMonitor mCustKeyGuardUpdateMonitor = ((HwCustKeyguardUpdateMonitor) HwCustManager.getInstance().getHwCustObj(HwCustKeyguardUpdateMonitor.class, new Object[0]));
    private boolean mIsChangeTipsForAbnormalReboot = false;
    private boolean mIsInDreaming = false;
    private boolean mKeyguardSecurityModeChecked = false;
    private boolean mOccluded = false;
    private String mProcessName = BuildConfig.FLAVOR;
    protected boolean mShowing = false;

    public static class SafeArrayList extends ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> {
        public KeyguardUpdateMonitorCallback getCallBack(int index) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = null;
            synchronized (this) {
                WeakReference<KeyguardUpdateMonitorCallback> ref = index >= super.size() ? null : (WeakReference) super.get(index);
                if (ref != null) {
                    keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) ref.get();
                }
            }
            return keyguardUpdateMonitorCallback;
        }

        public void removeCallback(KeyguardUpdateMonitorCallback callback) {
            synchronized (this) {
                int i = super.size() - 1;
                while (i >= 0) {
                    if (get(i) == null || ((WeakReference) get(i)).get() == callback) {
                        remove(i);
                    }
                    i--;
                }
            }
        }

        public void regist(KeyguardUpdateMonitorCallback callback) {
            synchronized (this) {
                for (int i = 0; i < super.size(); i++) {
                    if (getCallBack(i) == callback) {
                        HwLog.e("KeyguardUpdateMonitor", "regist from backgroud", new Exception());
                        return;
                    }
                }
                add(new WeakReference(callback));
            }
        }

        public int size() {
            int size;
            synchronized (this) {
                size = super.size();
            }
            return size;
        }
    }

    private HwKeyguardUpdateMonitor(Context context) {
        super(context);
        if (this.mCustKeyGuardUpdateMonitor != null) {
            this.mCustKeyGuardUpdateMonitor.registerHwReceiver(context);
        }
        if (!this.mDeviceProvisioned && KeyguardCfg.isSupportFpPasswordTimeout() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
            addUnsecureUserToLocktimeOut(false);
        }
    }

    public static HwKeyguardUpdateMonitor getInstance(Context context) {
        HwKeyguardUpdateMonitor hwKeyguardUpdateMonitor;
        synchronized (HwKeyguardUpdateMonitor.class) {
            if (sInstance == null) {
                sInstance = new HwKeyguardUpdateMonitor(context);
            }
            hwKeyguardUpdateMonitor = sInstance;
        }
        return hwKeyguardUpdateMonitor;
    }

    public static HwKeyguardUpdateMonitor getInstance() {
        HwKeyguardUpdateMonitor hwKeyguardUpdateMonitor;
        synchronized (HwKeyguardUpdateMonitor.class) {
            if (sInstance == null && GlobalContext.getContext() != null) {
                sInstance = new HwKeyguardUpdateMonitor(GlobalContext.getContext());
                HwLog.w("KeyguardUpdateMonitor", "HwKeyguardUpdateMonitor not inited");
            }
            hwKeyguardUpdateMonitor = sInstance;
        }
        return hwKeyguardUpdateMonitor;
    }

    public int getPhoneState() {
        return this.mPhoneState;
    }

    public void dispatchSetTransparent(boolean isTran) {
    }

    public CharSequence getTelephonyPlmn() {
        return null;
    }

    public CharSequence getTelephonyPlmn(int subscription) {
        return null;
    }

    public CharSequence getTelephonySpn() {
        return null;
    }

    public CharSequence getTelephonySpn(int subscription) {
        return BuildConfig.FLAVOR;
    }

    public void setAlternateUnlockEnabled(boolean state) {
    }

    public void sendKeyguardReset() {
        HwLog.d("KeyguardUpdateMonitor", "sendKeyguardReset");
        super.sendKeyguardReset();
    }

    protected void handleKeyguardBouncerChanged(int bouncer) {
        super.handleKeyguardBouncerChanged(bouncer);
        AppHandler.sendSingleMessage(10, 50);
    }

    public void onKeyguardVisibilityChanged(boolean showing) {
        super.onKeyguardVisibilityChanged(showing);
        AppHandler.sendSingleMessage(10, 100);
    }

    public void registerCallback(KeyguardUpdateMonitorCallback callback) {
        if (!GlobalContext.isRunningInUI()) {
            HwLog.w("KeyguardUpdateMonitor", "registerCallback not in UI.", new AndroidRuntimeException("Must execute in UI"));
        }
        super.registerCallback(callback);
    }

    public void removeCallback(KeyguardUpdateMonitorCallback callback) {
        if (!GlobalContext.isRunningInUI()) {
            HwLog.w("KeyguardUpdateMonitor", "registerCallback not in UI.", new AndroidRuntimeException("Must execute in UI"));
        }
        super.removeCallback(callback);
    }

    public void clearFailedUnlockAttempts() {
        super.clearFailedUnlockAttempts();
        VerifyPolicy.getInstance(this.mContext).clearFailedUnlockAttempts();
        if (OsUtils.getCurrentUser() == 0 && RemoteLockUtils.isDeviceRemoteLocked(this.mContext)) {
            RemoteLockUtils.resetDeviceRemoteLocked(this.mContext);
        }
    }

    public int getFailedUnlockAttempts(int userId) {
        return VerifyPolicy.getInstance(this.mContext).getFailedUnlockAttempts();
    }

    public void reportFailedStrongAuthUnlockAttempt(int userId) {
    }

    protected void handleBatteryUpdate(BatteryStatus status) {
        BatteryStateInfo.getInst().updateBatteryInfo(this.mContext, status);
        super.handleBatteryUpdate(status);
    }

    protected boolean isFingerprintDisabled(int userId) {
        return FpUtils.isFingerprintEnabled(this.mContext, userId) ? super.isFingerprintDisabled(userId) : true;
    }

    public void setDreamingState(boolean inDream) {
        synchronized (this) {
            this.mIsInDreaming = inDream;
        }
    }

    public boolean isInDreamingState() {
        boolean z;
        synchronized (this) {
            z = this.mIsInDreaming;
        }
        return z;
    }

    public void setKeyguardViewState(boolean showing, boolean occluded) {
        if (showing != this.mShowing || occluded != this.mOccluded) {
            if (KeyguardCfg.isSupportFpPasswordTimeout()) {
                addUnsecureUserToLocktimeOut(!showing ? occluded : true);
            }
            this.mShowing = showing;
            this.mOccluded = occluded;
            HwLog.v("KeyguardUpdateMonitor", "setKeyguardViewState: " + showing + "-" + occluded);
            if (this.mShowing) {
                this.mFingerprintAlreadyAuthenticated = false;
            }
            if (occluded) {
                ChargingAnimController.getInst(this.mContext).onKeyguardExit();
            }
            AppHandler.sendImmediateMessage(10);
        }
    }

    public boolean shouldShowing() {
        return this.mShowing && !this.mOccluded;
    }

    public boolean isShowing() {
        return this.mShowing ? this.mKeyguardIsVisible : false;
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public void dispatchShutDown() {
        HwLog.w("KeyguardUpdateMonitor", "Shutdown will check unexecute error");
        RetryPolicy.checkAllUnexecuteError(this.mContext);
    }

    protected void handleUserSwitching(int userId, IRemoteCallback reply) {
        super.handleUserSwitching(userId, reply);
        RetryPolicy.checkAllUnexecuteError(this.mContext);
    }

    public boolean getUserCanSkipBouncer(int userId) {
        return !isLockout() ? super.getUserCanSkipBouncer(userId) : false;
    }

    public boolean isLockout() {
        try {
            RetryPolicy.getDefaultPolicy(this.mContext).checkLockDeadline();
            return false;
        } catch (RequestThrottledException e) {
            HwLog.w("KeyguardUpdateMonitor", "current is lockout");
            return true;
        }
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        if (!isChangeTipsForAbnormalReboot()) {
            this.mIsChangeTipsForAbnormalReboot = true;
            FpUtils.setResetTypeHasRead(this.mContext, true);
        }
        this.mAuthedUser = KeyguardUpdateMonitor.getCurrentUser();
        if (OsUtils.isOwner()) {
            KeyguardWallpaper.getInst(this.mContext).reloadPicturesAfterFirstLogin();
        }
        super.reportSuccessfulStrongAuthUnlockAttempt();
        this.mAuthedUser = -10000;
    }

    public boolean isChangeTipsForAbnormalReboot() {
        return this.mIsChangeTipsForAbnormalReboot;
    }

    public void setChangeTipsForAbnormalReboot(boolean changeTipsForAbnormalReboot) {
        this.mIsChangeTipsForAbnormalReboot = changeTipsForAbnormalReboot;
    }

    public boolean isFingerprintUnlockTimedOut(int userId) {
        return super.hasFingerprintUnlockTimedOut(userId);
    }

    public boolean hasFingerprintUnlockTimedOut(int userId) {
        if (FingerBlackCounter.shouldLockout()) {
            HwLog.d("KeyguardUpdateMonitor", "Fingerprint timeout screenoff ");
            return true;
        } else if (FingerPrintPolicy.getLockoutDeadline() > SystemClock.elapsedRealtime()) {
            HwLog.d("KeyguardUpdateMonitor", "Fingerprint timeout screenon");
            return true;
        } else {
            if (isLockout()) {
                HwLog.d("KeyguardUpdateMonitor", "Fingerprint timeout pswd");
            }
            boolean timeOut = super.hasFingerprintUnlockTimedOut(userId);
            if (KeyguardCfg.isSupportFpPasswordTimeout()) {
                if (timeOut && restoreUserAuthInfo()) {
                    timeOut = !this.mStrongAuthNotTimedOut.contains(Integer.valueOf(userId));
                }
                if (timeOut) {
                    HwLog.i("KeyguardUpdateMonitor", "hasFingerprintUnlockTimedOut for userId: " + userId + " with last time is : " + System.getStringForUser(this.mContext.getContentResolver(), "verify_unlock_success_time", userId));
                }
                return timeOut;
            }
            if (timeOut) {
                HwLog.d("KeyguardUpdateMonitor", "Fingerprint timeout");
            }
            if (timeOut) {
                timeOut = !isLoggedIn3Days();
            }
            return timeOut;
        }
    }

    public boolean isLockScreenDisabled(Context context) {
        return this.mLockPatternUtils == null ? true : this.mLockPatternUtils.isLockScreenDisabled(OsUtils.getCurrentUser());
    }

    public void onCancel() {
        HwLog.w("KeyguardUpdateMonitor", "Fingerprint is canceld");
        if (this.mFingerprintRunningState == 2) {
            setFingerprintRunningState(0);
        }
    }

    protected void handleStartedGoingToSleep(int arg1) {
        MusicInfo.getInst().resetShowingState();
        KeyguardTheme.getInst().checkStyle(this.mContext, false, false);
        super.handleStartedGoingToSleep(arg1);
        OucScreenOnCounter.getInst(this.mContext).trigger(2);
    }

    protected void handleScreenTurnedOn() {
        super.handleScreenTurnedOn();
        OucScreenOnCounter.getInst(this.mContext).trigger(1);
    }

    public void checkSecurityMode(SecurityMode securityMode) {
        if ((securityMode == SecurityMode.PIN || securityMode == SecurityMode.Password) && !this.mKeyguardSecurityModeChecked) {
            int lockType = System.getIntForUser(this.mContext.getContentResolver(), "lockscreen.password_type", 0, UserHandle.myUserId());
            if (lockType != 0) {
                if (securityMode != SecurityMode.PIN || lockType < 262144) {
                    if (securityMode == SecurityMode.Password && lockType < 262144) {
                    }
                    return;
                }
                RadarUtil.uploadUnlockScreenTypeMismatch(this.mContext, "current mode is :" + securityMode + ", settings mode is " + lockType);
                this.mKeyguardSecurityModeChecked = true;
                HwLog.d("KeyguardUpdateMonitor", "checkSecurityMode(" + securityMode + ") set mode checked true");
                return;
            }
            return;
        }
        HwLog.d("KeyguardUpdateMonitor", "checkSecurityMode(" + securityMode + this.mKeyguardSecurityModeChecked + ") just return");
    }

    public void setCustLanguageHw(State state, Context context) {
        if (state == State.PIN_REQUIRED && this.mCustKeyGuardUpdateMonitor != null && context != null) {
            this.mCustKeyGuardUpdateMonitor.setCustLanguage(context);
        }
    }

    public boolean isSimPinSecure() {
        for (Entry<Integer, SimData> entry : this.mSimDatas.entrySet()) {
            SimData simdata = (SimData) entry.getValue();
            if (simdata != null && KeyguardUpdateMonitor.isSimPinSecure(simdata.simState)) {
                return true;
            }
        }
        HwLog.e("KeyguardUpdateMonitor", "isSimPinSecure mSimDatas is null or empty ");
        return false;
    }

    public int getNextSubIdForState(State state) {
        int resultId = -1;
        int bestSlotId = Integer.MAX_VALUE;
        for (Entry<Integer, SimData> entry : this.mSimDatas.entrySet()) {
            SimData simData = (SimData) entry.getValue();
            if (simData != null && state == simData.simState && bestSlotId > simData.slotId) {
                resultId = simData.subId;
                bestSlotId = simData.slotId;
            }
        }
        return resultId;
    }

    public boolean isInBouncer() {
        return this.mBouncer;
    }

    public void setCancelSubscriptId(int subId) {
        this.mCancelSubscriptId = subId;
        this.mCancelSubscriptSoltId = SubscriptionManager.getSlotId(subId);
    }

    public void resetCancelSubscriptId() {
        this.mCancelSubscriptId = -1;
        this.mCancelSubscriptSoltId = -1;
    }

    public void dismissKeyguard(Intent intent) {
        HwKeyguardPolicy.getInst().startActivity(intent, false);
    }

    public boolean isCameraDisable() {
        return isCameraDisable(isSecure());
    }

    private boolean isCameraDisable(boolean isSecure) {
        if (DisabledFeatureUtils.getCameraDisabled()) {
            return true;
        }
        if ((!isSecure || isDeviceProvisioned()) && !isRestrictAsEncrypt()) {
            return false;
        }
        return true;
    }

    public boolean transitionToCamera() {
        if (this.mContext == null) {
            HwLog.e("KeyguardUpdateMonitor", "transitionToCamera context is null");
            return false;
        }
        boolean isSecure = isSecure();
        if (isCameraDisable(isSecure)) {
            return false;
        }
        if (CoverViewManager.getInstance(this.mContext).isCoverAdded()) {
            HwLog.w("KeyguardUpdateMonitor", "transitionToCamera Camera skiped as cover close");
            return false;
        } else if (KeyguardCfg.isCameraExists()) {
            launchCamera(isSecure);
            HwLockScreenReporter.report(this.mContext, 124, BuildConfig.FLAVOR);
            return true;
        } else {
            dismissKeyguard(getCameraIntent(isSecure));
            HwLog.w("KeyguardUpdateMonitor", "transitionToCamera Camera not exist");
            return false;
        }
    }

    public boolean isSecure() {
        if (this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
            return true;
        }
        return KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinSecure();
    }

    private static Intent getCameraIntent(boolean isSecure) {
        if (isSecure) {
            return new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608).addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage("com.huawei.camera");
        }
        return new Intent("android.media.action.STILL_IMAGE_CAMERA").addFlags(268435456).addFlags(536870912).addFlags(67108864).setPackage("com.huawei.camera");
    }

    private void launchCamera(boolean isSecure) {
        final Intent intent = getCameraIntent(isSecure);
        if (this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
            HwLog.d("KeyguardUpdateMonitor", "launchCamera with dismissKeyguard Secure");
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    Runnable dismissRunnable = new Runnable() {
                        public void run() {
                            HwKeyguardPolicy.getInst().preventNextAnimation();
                        }
                    };
                    try {
                        HwLog.i("KeyguardUpdateMonitor", "startActivityAsUser : result = " + ActivityManagerNative.getDefault().startActivityAsUser(null, HwKeyguardUpdateMonitor.this.mContext.getBasePackageName(), intent, intent.resolveTypeIfNeeded(HwKeyguardUpdateMonitor.this.mContext.getContentResolver()), null, null, 0, 268435456, null, null, OsUtils.getCurrentUser()));
                    } catch (RemoteException e) {
                        HwLog.w("KeyguardUpdateMonitor", "Unable to start camera activity", e);
                    }
                    GlobalContext.getUIHandler().post(dismissRunnable);
                }
            });
            return;
        }
        HwLog.d("KeyguardUpdateMonitor", "launchCamera with dismissKeyguard inSecure");
        dismissKeyguard(intent);
    }

    public boolean canUseFingerWhenOcclued() {
        boolean z = false;
        if (KeyguardCfg.isFrontFpNavigationSupport()) {
            return false;
        }
        if (isOccluded()) {
            z = this.mShowing;
        }
        return z;
    }

    private boolean isLoggedIn3Days() {
        boolean ret = getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
        if (ret) {
            HwLog.i("KeyguardUpdateMonitor", "SystemUI may crashed at earlier time. " + SystemClock.uptimeMillis());
        }
        return ret;
    }

    protected void handleUserSwitchComplete(int userId) {
        super.handleUserSwitchComplete(userId);
        KeyguardUpdateMonitor.setCurrentUser(userId);
        HiddenSpace hiddenSp = HiddenSpace.getInstance();
        if (!isRestrictAsEncrypt()) {
            this.mHandler.sendEmptyMessageDelayed(65537, 500);
        } else if (hiddenSp.ismIsSwitchUserByPassword()) {
            HwLog.i("KeyguardUpdateMonitor", "don't show bouncer, due to switch user by password!");
        } else {
            this.mHandler.sendEmptyMessage(65538);
        }
        hiddenSp.setmIsSwitchUserByPassword(false);
    }

    public boolean isFirstTimeStartup() {
        return OsUtils.isOwner() && !getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
    }

    public boolean isFirstTimeStartupAndEncrypted() {
        return OsUtils.isOwner() ? isRestrictAsEncrypt() : false;
    }

    public boolean isRestrictAsEncrypt() {
        boolean z = false;
        int userId = KeyguardUpdateMonitor.getCurrentUser();
        if (!KeyguardCfg.isCredentialProtected(this.mContext) || this.mStrongAuthNotTimedOut.contains(Integer.valueOf(userId))) {
            return false;
        }
        if (this.mLockPatternUtils.isSecure(userId) && !getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
            z = true;
        }
        return z;
    }

    public void handleExtMessage(Message msg) {
        switch (msg.what) {
            case 65537:
                KeyguardTheme.getInst().checkStyle(this.mContext, true, true);
                AppHandler.sendMessage(2);
                return;
            case 65538:
                if (isShowing()) {
                    HwKeyguardPolicy.getInst().dismiss();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean isSecure(int user) {
        return this.mAuthedUser != user ? this.mLockPatternUtils.isSecure(user) : true;
    }

    protected void handleBootCompleted() {
        super.handleBootCompleted();
        OucScreenOnCounter.getInst(this.mContext).cleanOldData();
    }

    protected boolean isScreenshotProcess() {
        if (TextUtils.isEmpty(this.mProcessName)) {
            return false;
        }
        return this.mProcessName.contains("screenshot");
    }

    public void setCurrenProcessName() {
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            HwLog.e("KeyguardUpdateMonitor", "setCurrenProcessName with activityManager wrong!");
            return;
        }
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos == null || processInfos.isEmpty()) {
            HwLog.e("KeyguardUpdateMonitor", "setCurrenProcessName with processInfos wrong!");
            return;
        }
        for (RunningAppProcessInfo process : processInfos) {
            if (process.pid == pid) {
                this.mProcessName = process.processName;
                break;
            }
        }
        HwLog.i("KeyguardUpdateMonitor", "setCurrenProcessName with mProcessName is : " + this.mProcessName);
    }

    public void setFpAuthenticated(boolean status) {
        this.mFingerprintAlreadyAuthenticated = status;
    }

    protected void onUserAuthTimeout(int userId) {
        restoreUserAuthInfo();
        this.mStrongAuthNotTimedOut.remove(Integer.valueOf(userId));
        saveUserAuthInfo();
        HwLog.w("KeyguardUpdateMonitor", "ACTION_STRONG_AUTH_TIMEOUT for : " + userId);
    }

    protected void saveUserAuthInfo() {
        SharedPreferences sp = this.mContext.getSharedPreferences("magazine_preferences", 0);
        HashSet<String> values = new HashSet();
        for (Integer i : this.mStrongAuthNotTimedOut) {
            values.add(i.toString());
        }
        sp.edit().putStringSet("key_valide_author", values).apply();
    }

    protected void afterUserAuthed() {
        restoreUserAuthInfo();
        saveUserAuthInfo();
    }

    private void addUnsecureUserToLocktimeOut(boolean keyguardShow) {
        if (!keyguardShow) {
            int userId = KeyguardUpdateMonitor.getCurrentUser();
            if (!this.mLockPatternUtils.isSecure(userId)) {
                super.scheduleStrongAuthTimeout();
                this.mStrongAuthNotTimedOut.add(Integer.valueOf(userId));
                saveUserAuthInfo();
            }
        }
    }

    protected boolean restoreUserAuthInfo() {
        try {
            if (!restoreUserAuthInfoInner()) {
                return false;
            }
            HwLog.w("KeyguardUpdateMonitor", "!!! App maybe restarted. !!!");
            return true;
        } catch (ClassCastException e) {
            HwLog.e("KeyguardUpdateMonitor", "restoreUserAuthInfo fail", e);
            return false;
        } catch (NumberFormatException e2) {
            HwLog.e("KeyguardUpdateMonitor", "restoreUserAuthInfo fail", e2);
            return false;
        }
    }

    private boolean restoreUserAuthInfoInner() throws NumberFormatException, ClassCastException {
        boolean z = false;
        SharedPreferences sp = this.mContext.getSharedPreferences("magazine_preferences", 0);
        if (!sp.contains("key_valide_author")) {
            return false;
        }
        Set<String> values = sp.getStringSet("key_valide_author", new HashSet());
        StrongAuthTracker tracker = getStrongAuthTracker();
        int oldSize = this.mStrongAuthNotTimedOut.size();
        for (String str : values) {
            int user = Integer.parseInt(str);
            if (!this.mStrongAuthNotTimedOut.contains(Integer.valueOf(user)) && tracker.hasUserAuthenticatedSinceBoot(user)) {
                this.mStrongAuthNotTimedOut.add(Integer.valueOf(user));
            }
        }
        if (this.mStrongAuthNotTimedOut.size() > oldSize) {
            z = true;
        }
        return z;
    }
}
