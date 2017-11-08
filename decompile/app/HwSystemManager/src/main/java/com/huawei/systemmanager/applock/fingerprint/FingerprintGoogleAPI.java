package com.huawei.systemmanager.applock.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.CancellationSignal;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;

class FingerprintGoogleAPI extends AuthenticationCallback implements IFingerprintAuth {
    private static final int DEFAULT_LOCKOUT_TIME = 30000;
    private static final String TAG = "FingerprintGoogleAPI";
    private IFingerprintAuthCallback mCallback;
    private CancellationSignal mCancellationSignal;
    private Context mCtx;
    private FingerprintManagerEx mFingerprintExt = new FingerprintManagerEx(this.mCtx);
    private FingerprintManager mFingerprintManager = ((FingerprintManager) this.mCtx.getSystemService("fingerprint"));

    private FingerprintGoogleAPI(Context ctx) {
        this.mCtx = ctx;
    }

    public static IFingerprintAuth create(Context ctx) {
        return new FingerprintGoogleAPI(ctx);
    }

    public int getRemainedNum() {
        int ret = this.mFingerprintExt.getRemainingNum();
        HwLog.i(TAG, "getRemainedNum return: " + ret);
        if (ret <= 0) {
            return 1;
        }
        return ret;
    }

    public long getRemainedTime() {
        long ret = this.mFingerprintExt.getRemainingTime();
        HwLog.i(TAG, "getRemainedTime return: " + ret);
        if (0 >= ret) {
            ret = 30000;
        }
        return 500 + ret;
    }

    public boolean isFingerprintReady() {
        if (this.mFingerprintManager != null) {
            HwLog.d(TAG, "isFingerprintReady hardware ready: " + this.mFingerprintManager.isHardwareDetected() + ", hasEnrolledFingerprints: " + this.mFingerprintManager.hasEnrolledFingerprints());
        }
        if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
            return false;
        }
        return this.mFingerprintManager.hasEnrolledFingerprints();
    }

    public boolean startAuthenticate(IFingerprintAuthCallback callback) {
        this.mCallback = callback;
        this.mCancellationSignal = new CancellationSignal();
        this.mFingerprintManager.authenticate(null, this.mCancellationSignal, 0, this, null);
        return true;
    }

    public boolean cancelAuthenticate() {
        if (this.mCancellationSignal != null) {
            this.mCancellationSignal.cancel();
            this.mCancellationSignal = null;
        }
        return true;
    }

    public void release() {
        this.mCallback = null;
    }

    public void onAuthenticationError(int errorCode, CharSequence errString) {
        HwLog.e(TAG, "onAuthenticationError " + errorCode + SqlMarker.COMMA_SEPARATE + errString);
        if (7 == errorCode && this.mCallback != null) {
            this.mCallback.onFingerprintAuthLockout();
        }
    }

    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        HwLog.d(TAG, "onAuthenticationHelp " + helpCode + SqlMarker.COMMA_SEPARATE + helpString);
    }

    public void onAuthenticationSucceeded(AuthenticationResult result) {
        if (this.mCallback != null) {
            this.mCallback.onFingerprintAuthSuccess();
        }
    }

    public void onAuthenticationFailed() {
        HwLog.e(TAG, "onAuthenticationFailed");
        if (this.mCallback != null) {
            this.mCallback.onFingerprintAuthFailed();
        }
    }
}
