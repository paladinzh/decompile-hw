package com.huawei.keyguard.policy;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.util.HwLog;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;

public class FingerPrintPolicy extends IRetryPolicy {
    private static long sLockoutDeadline = 0;
    private int mErrorCnt = 0;
    private FingerprintManagerEx mFingerprintManagerEx;
    private FingerprintManager mFingerprintService;
    private LockPatternUtilsEx mLockPatternUtilsEx;
    private SparseArray<Long> mSecureLoginTime = new SparseArray();
    private final int mUserId;

    public FingerPrintPolicy(Context context, int userId) {
        this.mFingerprintManagerEx = new FingerprintManagerEx(context);
        this.mLockPatternUtilsEx = new LockPatternUtilsEx(context);
        this.mFingerprintService = (FingerprintManager) context.getSystemService("fingerprint");
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public int addErrorCount() {
        this.mErrorCnt++;
        return this.mErrorCnt;
    }

    public int getErrorCount() {
        return this.mErrorCnt;
    }

    public int getRemainingChance() {
        if (this.mFingerprintManagerEx != null) {
            return this.mFingerprintManagerEx.getRemainingNum();
        }
        return 5 - this.mErrorCnt;
    }

    public TimeTickInfo getTimeTickInfo() {
        HwLog.e("KG_Policy_FP", "This is a reserved interface, should't be called", new Exception());
        return new TimeTickInfo();
    }

    public boolean isObserverRegistered(TimeObserver arg0) {
        return false;
    }

    public void registerObserver(TimeObserver arg0) {
    }

    public void resetErrorCount(Context context) {
        if (this.mErrorCnt > 0 || getLockoutDeadline() > 0 || getRemainingChance() < 5) {
            HwLog.i("KG_Policy_FP", "FingerPrint white state is reset");
            VerifyPolicy.getInstance(context).resetLockoutDeadline();
            if (this.mFingerprintService != null) {
                this.mFingerprintService.resetTimeout(new byte[0]);
            }
            this.mErrorCnt = 0;
            resetState();
        } else {
            HwLog.d("KG_Policy_FP", "FingerPrint state is already reset");
        }
        FingerBlackCounter.resetErrorCount();
        HwKeyguardPolicy.getInst().removeFingerprintMsg();
    }

    public void unregisterAll() {
    }

    public void unregisterObserver(TimeObserver arg0) {
    }

    public void checkLockDeadline() throws RequestThrottledException {
        long waitout = this.mFingerprintManagerEx.getRemainingTime();
        if (waitout > 0) {
            throw new RequestThrottledException((int) waitout);
        }
    }

    public boolean isThinkAsFail(int len, int hash) {
        return true;
    }

    void setFingerOpened(int userId) {
        long currentTime = SystemClock.elapsedRealtime();
        synchronized (this.mSecureLoginTime) {
            this.mSecureLoginTime.put(userId, Long.valueOf(currentTime));
        }
    }

    public static void setLockoutDeadline(long lockoutDeadline) {
        sLockoutDeadline = lockoutDeadline;
    }

    public static long getLockoutDeadline() {
        return sLockoutDeadline;
    }

    private static void resetState() {
        sLockoutDeadline = 0;
    }
}
