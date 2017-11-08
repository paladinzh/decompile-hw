package com.android.settings.fingerprint.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.EnrollmentCallback;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.RadarReporter;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BiometricManager {
    private static String PKG_KEY_GUARD = "com.android.keyguard";
    private static Object sEnrollLock = new Object();
    private static long sLatestChallenge = 0;
    private AuthenticationCallback mAuthCallback = new AuthenticationCallback() {
        public void onAuthenticationSucceeded(AuthenticationResult result) {
            int fingerId = result.getFingerprint().getFingerId();
            Log.i("BiometricManager", "Authentication succeeded. Identified fp id = " + fingerId);
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(31, fingerId, 0));
        }

        public void onAuthenticationFailed() {
            Log.i("BiometricManager", "Authentication failed.");
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(32));
        }

        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            Log.e("BiometricManager", "Error occurred during authentication, errMsgId = " + errMsgId + ", errString = " + errString);
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(33, errMsgId, 0));
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Log.i("BiometricManager", "Authentication tips, helpMsgId = " + helpMsgId + ", helpString = " + helpString);
            switch (helpMsgId) {
                case 2001:
                    Log.i("BiometricManager", "Authenticaton help FINGERPRINT_WAITTING_FINGER message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(11));
                    return;
                case 2002:
                    Log.i("BiometricManager", "Authenticaton help FINGERPRINT_DOWN message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(12));
                    return;
                case 2003:
                    Log.i("BiometricManager", "Authenticaton help FINGERPRINT_UP message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(13));
                    return;
                default:
                    Log.w("BiometricManager", "Authenticaton help unsupported message arrived.");
                    return;
            }
        }
    };
    private CalibrationCallback mCalibrationCallback;
    private CancellationSignal mCalibrationCancel;
    private int mCalibrationCurStep = -1;
    private int mCalibrationTotalStep = -1;
    private CaptureCallback mCaptureCallback;
    private Context mContext;
    private EnrollCallback mEnrollCallback;
    private int mEnrollProgress = 0;
    private int mEnrollRemaining = -1;
    private int mEnrollSteps = -1;
    private EnrollmentCallback mEnrollmentCallback = new EnrollmentCallback() {
        public void onEnrollmentProgress(int remaining) {
            int i;
            Log.i("BiometricManager", "Enrollment progress callback enter, remaining = " + remaining);
            if (BiometricManager.this.mEnrollSteps == -1) {
                if (remaining == 8 || remaining == 12 || remaining == 6) {
                    BiometricManager.this.mEnrollSteps = remaining - 1;
                } else {
                    BiometricManager.this.mEnrollSteps = remaining;
                }
                Log.i("BiometricManager", "Enrollment progress callback enter, mEnrollSteps = " + BiometricManager.this.mEnrollSteps);
            }
            if (BiometricManager.this.mHasDuplicated) {
                BiometricManager.this.mEnrollRemaining = BiometricManager.this.mEnrollSteps + 1;
                BiometricManager.this.mHasDuplicated = false;
            } else {
                BiometricManager.this.mEnrollRemaining = remaining;
            }
            int currentStep = Math.max(0, (BiometricManager.this.mEnrollSteps + 1) - BiometricManager.this.mEnrollRemaining);
            int progress = (currentStep * 100) / (BiometricManager.this.mEnrollSteps + 1);
            BiometricManager.this.mEnrollProgress = progress;
            EnrollProgress enrollProgress = new EnrollProgress(BiometricManager.this.mEnrollProgress, currentStep - 1, BiometricManager.this.mEnrollSteps + 1);
            if (BiometricManager.this.mLastDirection == 2004) {
                i = 2009;
            } else {
                i = BiometricManager.this.mLastDirection;
            }
            enrollProgress.guideDirection = i;
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(23, enrollProgress));
            Log.i("BiometricManager", "Enrollment progress = " + progress + ", currentStep = " + enrollProgress.currentStep + ", totalStep = " + enrollProgress.totalSteps);
            if (BiometricManager.this.mEnrollRemaining == 1) {
                BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(35, new ProgressData(BiometricManager.this.mLastDirection == 2004 ? 2009 : BiometricManager.this.mLastDirection, BiometricManager.this.mEnrollProgress)));
                return;
            }
            if (progress == 100) {
                BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(21, 0, 0));
            }
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            Log.i("BiometricManager", "Enrollment help message arrived, helpMsgId = " + helpMsgId + ", helpString = " + helpString);
            if (isGoogleHelp(helpMsgId)) {
                BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(25, BiometricManager.translateOriginHelp(helpMsgId), 0));
            } else {
                handleCustAction(helpMsgId);
            }
        }

        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            Log.i("BiometricManager", "Enrollment failed, errMsgId = " + errMsgId + ", errString = " + errString);
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(22, errMsgId, 0));
            HashMap<Short, Object> map = new HashMap();
            map.put(Short.valueOf((short) 0), Integer.valueOf(errMsgId));
            map.put(Short.valueOf((short) 1), errString);
            RadarReporter.reportRadar(907018007, map);
        }

        private boolean isGoogleHelp(int code) {
            if (code == 1 || code == 2 || code == 3 || code == 4 || code == 5) {
                return true;
            }
            return false;
        }

        private void handleCustAction(int code) {
            switch (code) {
                case 2001:
                    Log.i("BiometricManager", "Enrollment help FINGERPRINT_WAITTING_FINGER message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(11));
                    return;
                case 2002:
                    Log.i("BiometricManager", "Enrollment help FINGERPRINT_DOWN message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(12));
                    return;
                case 2003:
                    Log.i("BiometricManager", "Enrollment help FINGERPRINT_UP message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(13));
                    return;
                case 2004:
                case 2005:
                case 2006:
                case 2007:
                case 2008:
                case 2009:
                case 2010:
                case 2011:
                case 2012:
                    Log.i("BiometricManager", "Enrollment help guide progress message arrived.");
                    BiometricManager.this.mLastDirection = code;
                    return;
                case 2014:
                case 2015:
                case 2016:
                    if (code == 2016) {
                        BiometricManager.this.mHasDuplicated = true;
                    }
                    Log.i("BiometricManager", "Enrollment help Huawei customized fingerprint help message arrived.");
                    BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(25, BiometricManager.translateCustHelp(code), 0));
                    return;
                default:
                    Log.w("BiometricManager", "Enrollment unsupported help message arrived.");
                    return;
            }
        }
    };
    private CancellationSignal mEnrollmentCancel;
    private EventHandler mEventHandler;
    private CancellationSignal mFingerprintCancel;
    private FingerprintManagerEx mFingerprintManagerEx;
    private FingerprintManager mFpm;
    private boolean mHasDuplicated = false;
    private IdentifyCallback mIdentifyCallback;
    private int mLastDirection = 2004;
    private EnrollmentCallback mOnCalibrationCallback = new OnCalibrationCallback();
    private RemovalCallback mRemovalCallback = new RemovalCallback() {
        public void onRemovalSucceeded(Fingerprint fingerprint) {
            Log.i("BiometricManager", String.format("Fingerprint deleted successfully, fingerprint ID : [%d], fingerprint name : [%s]", new Object[]{Integer.valueOf(fingerprint.getFingerId()), fingerprint.getName()}));
            System.putIntForUser(BiometricManager.this.mContext.getContentResolver(), "fingerprint_alipay_dialog", 1, UserHandle.myUserId());
            FingerprintUtils.onFingerprintNumChanged(BiometricManager.this.mContext, UserHandle.myUserId());
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
            Log.e("BiometricManager", String.format("Fingerprint deleted failed, fingerprint ID : [%d], fingerprint name : [%s], err code : [%d], err message : [%s]", new Object[]{Integer.valueOf(fp.getFingerId()), fp.getName(), Integer.valueOf(errMsgId), errString.toString()}));
        }
    };

    public interface IdentifyCallback {
        void onIdentified(int i);

        void onIdentifyError(int i);

        void onNoMatch();
    }

    public interface CaptureCallback {
        void onCaptureCompleted();

        void onInput();

        void onWaitingForInput();
    }

    public interface CalibrationCallback {
        void onCalibrationError(int i);

        void onCalibrationHelp(int i);

        void onCalibrationProgress(CalibrationProgress calibrationProgress);
    }

    public interface EnrollCallback {
        void onDuplicated();

        void onEnrolled(int i);

        void onEnrollmentFailed(int i);

        void onFingerLowCoverge();

        void onFingerRegionChange();

        void onFingerStill();

        void onGuideProgress(ProgressData progressData);

        void onImageLowQuality();

        void onProgress(EnrollProgress enrollProgress);

        void onSensorDirty();
    }

    public static class CalibrationProgress {
        public int currentStep;
        public int totalSteps;

        public CalibrationProgress(int currentStep, int totalSteps) {
            this.currentStep = currentStep;
            this.totalSteps = totalSteps;
        }
    }

    public static class EnrollProgress {
        public int currentStep;
        public int guideDirection = 2004;
        public int percents;
        public int totalSteps;

        public EnrollProgress(int percents, int currentStep, int totalSteps) {
            this.percents = percents;
            this.currentStep = currentStep;
            this.totalSteps = totalSteps;
        }
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i("BiometricManager", "msg.what in biometric = " + msg.what);
            switch (msg.what) {
                case 11:
                    Log.i("BiometricManager", "Handle MSG_FP_WAITING message.");
                    if (BiometricManager.this.mCaptureCallback != null) {
                        BiometricManager.this.mCaptureCallback.onWaitingForInput();
                        return;
                    }
                    return;
                case 12:
                    Log.i("BiometricManager", "Handle MSG_FP_DOWN message.");
                    if (BiometricManager.this.mCaptureCallback != null) {
                        BiometricManager.this.mCaptureCallback.onInput();
                        return;
                    }
                    return;
                case 13:
                    Log.i("BiometricManager", "Handle MSG_FP_UP message.");
                    if (BiometricManager.this.mCaptureCallback != null) {
                        BiometricManager.this.mCaptureCallback.onCaptureCompleted();
                        return;
                    }
                    return;
                case 21:
                    Log.i("BiometricManager", "Handle MSG_FP_ENROLLED message.");
                    if (BiometricManager.this.mEnrollCallback != null) {
                        BiometricManager.this.mEnrollCallback.onEnrolled(msg.arg1);
                        return;
                    }
                    return;
                case 22:
                    if (BiometricManager.this.mEnrollCallback != null) {
                        int err = 0;
                        if (msg.arg1 == 3) {
                            err = 1;
                        }
                        BiometricManager.this.mEnrollCallback.onEnrollmentFailed(err);
                        return;
                    }
                    return;
                case 23:
                    EnrollProgress prog = msg.obj;
                    Log.i("BiometricManager", "Handle MSG_FP_PROGRESS message, progress = " + prog.percents);
                    if (BiometricManager.this.mEnrollCallback != null) {
                        BiometricManager.this.mEnrollCallback.onProgress(prog);
                        return;
                    }
                    return;
                case 24:
                    if (BiometricManager.this.mEnrollCallback != null) {
                        BiometricManager.this.mEnrollCallback.onDuplicated();
                        return;
                    }
                    return;
                case 25:
                    if (BiometricManager.this.mEnrollCallback != null) {
                        Log.i("BiometricManager", "Handle help message, msg.arg1 = " + msg.arg1);
                        switch (msg.arg1) {
                            case 1:
                                BiometricManager.this.mEnrollCallback.onFingerLowCoverge();
                                return;
                            case 2:
                                BiometricManager.this.mEnrollCallback.onImageLowQuality();
                                return;
                            case 3:
                                BiometricManager.this.mEnrollCallback.onSensorDirty();
                                return;
                            case 5:
                                BiometricManager.this.mEnrollCallback.onFingerStill();
                                return;
                            case 1001:
                                BiometricManager.this.mEnrollCallback.onFingerRegionChange();
                                return;
                            case 1003:
                                BiometricManager.this.mEnrollCallback.onDuplicated();
                                return;
                            default:
                                Log.w("BiometricManager", "Unsupported help code = " + msg.arg1);
                                return;
                        }
                    }
                    return;
                case 31:
                    BiometricManager.this.mFingerprintCancel = null;
                    if (BiometricManager.this.mIdentifyCallback != null) {
                        BiometricManager.this.mIdentifyCallback.onIdentified(msg.arg1);
                        return;
                    }
                    return;
                case 32:
                    if (BiometricManager.this.mIdentifyCallback != null) {
                        BiometricManager.this.mIdentifyCallback.onNoMatch();
                        return;
                    }
                    return;
                case 33:
                    if (BiometricManager.this.mIdentifyCallback != null) {
                        BiometricManager.this.mIdentifyCallback.onIdentifyError(msg.arg1);
                        return;
                    }
                    return;
                case 35:
                    if (BiometricManager.this.mEnrollCallback != null) {
                        BiometricManager.this.mEnrollCallback.onGuideProgress((ProgressData) msg.obj);
                        return;
                    }
                    return;
                case 50:
                    if (BiometricManager.this.mCalibrationCallback != null) {
                        BiometricManager.this.mCalibrationCallback.onCalibrationProgress((CalibrationProgress) msg.obj);
                        return;
                    }
                    return;
                case 51:
                    if (BiometricManager.this.mCalibrationCallback != null) {
                        BiometricManager.this.mCalibrationCallback.onCalibrationHelp(msg.arg1);
                        return;
                    }
                    return;
                case 52:
                    if (BiometricManager.this.mCalibrationCallback != null) {
                        BiometricManager.this.mCalibrationCallback.onCalibrationError(msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class OnCalibrationCallback extends EnrollmentCallback {
        private OnCalibrationCallback() {
        }

        public void onEnrollmentProgress(int remaining) {
            Log.i("BiometricManager", "calibration onEnrollmentProgress start, remaining = " + remaining);
            if (BiometricManager.this.mCalibrationTotalStep == -1) {
                BiometricManager.this.mCalibrationTotalStep = remaining;
            }
            int curCalibrationProgress = BiometricManager.this.mCalibrationTotalStep - remaining;
            if (curCalibrationProgress <= BiometricManager.this.mCalibrationCurStep) {
                Log.i("BiometricManager", "calibration no progress made, mCalibrationCurStep = " + BiometricManager.this.mCalibrationCurStep + ", curCalibrationProgress = " + curCalibrationProgress);
                return;
            }
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(50, new CalibrationProgress(curCalibrationProgress, BiometricManager.this.mCalibrationTotalStep)));
            BiometricManager.this.mCalibrationCurStep = curCalibrationProgress;
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            Log.i("BiometricManager", "calibration onEnrollmentHelp start, helpMsgId = " + helpMsgId + ", helpString = " + helpString);
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(51, helpMsgId, 0));
        }

        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            Log.i("BiometricManager", "calibration onEnrollmentError start, errMsgId = " + errMsgId + ", errString = " + errString);
            BiometricManager.this.mEventHandler.sendMessage(BiometricManager.this.mEventHandler.obtainMessage(52, errMsgId, 0));
        }
    }

    public static class ProgressData {
        public int guidedNextDirection;
        public int guidedProgress;

        public ProgressData(int guidedNextDirection, int guidedProgress) {
            this.guidedNextDirection = guidedNextDirection;
            this.guidedProgress = guidedProgress;
        }
    }

    public static BiometricManager open(Context context) {
        FingerprintManager fpm = (FingerprintManager) context.getSystemService("fingerprint");
        FingerprintManagerEx fpMgrEx = new FingerprintManagerEx(context);
        if (fpm == null || fpMgrEx == null) {
            return null;
        }
        Looper looper = Looper.myLooper();
        if (looper == null) {
            Looper.prepare();
        }
        return new BiometricManager(fpm, fpMgrEx, looper, context);
    }

    private BiometricManager(FingerprintManager fpm, FingerprintManagerEx fpMgrEx, Looper looper, Context context) {
        this.mFpm = fpm;
        this.mContext = context;
        this.mFingerprintManagerEx = fpMgrEx;
        if (looper != null) {
            this.mEventHandler = new EventHandler(looper);
            return;
        }
        HandlerThread handlerThread = new HandlerThread("BiometricManager");
        handlerThread.start();
        this.mEventHandler = new EventHandler(handlerThread.getLooper());
    }

    public void setCaptureCallback(CaptureCallback captureCallback) {
        this.mCaptureCallback = captureCallback;
    }

    public void release() {
    }

    public int startEnroll(EnrollCallback enrollCallback, byte[] token, int userId) {
        if (enrollCallback == null) {
            return -1;
        }
        Log.i("BiometricManager", "Biometric startEnroll.");
        this.mLastDirection = 2004;
        this.mEnrollCallback = enrollCallback;
        this.mEnrollSteps = -1;
        this.mHasDuplicated = false;
        this.mEnrollmentCancel = new CancellationSignal();
        setFingerprintSensorMode(0);
        this.mFpm.enroll(token, this.mEnrollmentCancel, 0, userId, this.mEnrollmentCallback);
        return 0;
    }

    public void cancelEnroll() {
        if (this.mEnrollmentCancel == null) {
            Log.w("BiometricManager", "Enrollment not started!");
        } else if (this.mEnrollmentCancel.isCanceled()) {
            Log.w("BiometricManager", "Enrollment already canceled!");
        } else {
            this.mEnrollmentCancel.cancel();
            this.mEnrollSteps = -1;
            this.mHasDuplicated = false;
        }
    }

    public void startCalibration(CalibrationCallback calibrationCallback, int userId) {
        if (calibrationCallback == null) {
            Log.w("BiometricManager", "OnCalibrationCallback is null!");
            return;
        }
        Log.i("BiometricManager", "Biometric startCalibration.");
        this.mCalibrationCallback = calibrationCallback;
        this.mCalibrationCancel = new CancellationSignal();
        setFingerprintSensorMode(1);
        int tokenLen = getCalibrationTokenLen();
        if (tokenLen <= 0) {
            tokenLen = 69;
        }
        this.mFpm.enroll(new byte[tokenLen], this.mCalibrationCancel, 0, userId, this.mOnCalibrationCallback);
    }

    public void cancelCalibration() {
        if (this.mCalibrationCancel == null) {
            Log.w("BiometricManager", "Calibration not started!");
        } else if (this.mCalibrationCancel.isCanceled()) {
            Log.w("BiometricManager", "Calibration already canceled!");
        } else {
            this.mCalibrationCancel.cancel();
        }
    }

    public void setFingerprintSensorMode(int mode) {
        setCalibrateMode(this.mFingerprintManagerEx, mode);
    }

    public boolean needSensorCalibration() {
        return isFpNeedCalibrate(this.mFingerprintManagerEx);
    }

    private static int getCalibrationTokenLen() {
        int ret = 69;
        try {
            ret = ((Integer) Class.forName("huawei.android.hardware.fingerprint.FingerprintManagerEx").getDeclaredMethod("getTokenLen", null).invoke(null, (Object[]) null)).intValue();
        } catch (Exception e) {
            Log.e("BiometricManager", "getCalibrationTokenLen error ! " + e.getMessage());
        } catch (Exception ex) {
            Log.e("BiometricManager", "getCalibrationTokenLen error !" + ex.getMessage());
        }
        Log.i("BiometricManager", "getCalibrationTokenLen = " + ret);
        return ret;
    }

    public static boolean isFpNeedCalibrate(FingerprintManagerEx managerEx) {
        boolean ret = false;
        try {
            ret = ((Boolean) Class.forName("huawei.android.hardware.fingerprint.FingerprintManagerEx").getDeclaredMethod("isFpNeedCalibrate", null).invoke(managerEx, (Object[]) null)).booleanValue();
        } catch (Exception e) {
            Log.e("BiometricManager", "isFpNeedCalibrate error ! " + e.getMessage());
        } catch (Exception ex) {
            Log.e("BiometricManager", "isFpNeedCalibrate error !" + ex.getMessage());
        }
        Log.i("BiometricManager", "isFpNeedCalibrate = " + ret);
        return ret;
    }

    public static void setCalibrateMode(FingerprintManagerEx managerEx, int mode) {
        try {
            Class.forName("huawei.android.hardware.fingerprint.FingerprintManagerEx").getDeclaredMethod("setCalibrateMode", new Class[]{Integer.TYPE}).invoke(managerEx, new Object[]{Integer.valueOf(mode)});
        } catch (Exception e) {
            Log.e("BiometricManager", "setCalibrateMode error ! " + e.getMessage());
        } catch (Exception ex) {
            Log.e("BiometricManager", "setCalibrateMode error !" + ex.getMessage());
        }
    }

    public int startIdentify(IdentifyCallback identifyCallback, int flags, int userId) {
        if (identifyCallback == null) {
            return -1;
        }
        Log.i("BiometricManager", "Biometric startIdentify, flags = " + flags);
        this.mIdentifyCallback = identifyCallback;
        this.mFingerprintCancel = new CancellationSignal();
        this.mFpm.authenticate(null, this.mFingerprintCancel, flags, this.mAuthCallback, null, userId);
        return 0;
    }

    public void cancelIdentify() {
        if (this.mFingerprintCancel == null) {
            Log.w("BiometricManager", "Authentication not started!");
        } else if (this.mFingerprintCancel.isCanceled()) {
            Log.w("BiometricManager", "Authentication already canceled!");
        } else {
            this.mFingerprintCancel.cancel();
        }
    }

    public void abort() {
    }

    public int setAssociation(Context context, String pkgName, boolean enabled) {
        return setAssociation(context, pkgName, enabled, UserHandle.myUserId());
    }

    public int setAssociation(Context context, String pkgName, boolean enabled, int userId) {
        int i = 0;
        if (context == null) {
            Log.e("BiometricManager", "setAssociation, null context.");
            return -1;
        } else if (PKG_KEY_GUARD.equals(pkgName)) {
            int i2;
            ContentResolver contentResolver = context.getContentResolver();
            String str = "fp_keyguard_enable";
            if (enabled) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (!Secure.putIntForUser(contentResolver, str, i2, userId)) {
                i = -1;
            }
            return i;
        } else {
            Log.e("BiometricManager", "setAssociation, invalid package name= " + pkgName);
            return -1;
        }
    }

    public boolean getAssociation(Context context, String pkgName, int userId) {
        boolean z = true;
        if (context == null) {
            Log.e("BiometricManager", "getAssociation, null context.");
            return true;
        } else if (PKG_KEY_GUARD.equals(pkgName)) {
            int ret = Secure.getIntForUser(context.getContentResolver(), "fp_keyguard_enable", 1, userId);
            if (ret < 0 || ret > 1) {
                Log.e("BiometricManager", "getAssociation, invalid keyguard enable value = " + ret);
                ret = 1;
            }
            if (ret != 1) {
                z = false;
            }
            return z;
        } else {
            Log.e("BiometricManager", "getAssociation, invalid package name = " + pkgName);
            return true;
        }
    }

    public int setFingerPrintProperty(String prop, int value) {
        return 0;
    }

    public static boolean isFingerprintSupported(Context context) {
        FingerprintManager fpm = (FingerprintManager) context.getSystemService("fingerprint");
        if (fpm == null) {
            return false;
        }
        Log.d("BiometricManager", "fpm.isHardwareDetected() = " + fpm.isHardwareDetected());
        return fpm.isHardwareDetected();
    }

    public static boolean isFingerprintEnabled(Context context) {
        return isFingerprintSupported(context);
    }

    public static boolean setFingerprintEnabled(boolean isEnabled) {
        Log.d("BiometricManager", "setFingerprintEnabled = " + isEnabled);
        return true;
    }

    public static boolean hasFpTemplates(int privacyMode, Context context, int userId) {
        if (!isFingerprintEnabled(context)) {
            return false;
        }
        BiometricManager brm = open(context);
        if (brm == null) {
            return false;
        }
        boolean res = brm.getFingerprintList(userId).size() > 0;
        brm.release();
        return res;
    }

    public static boolean isKeyGuardNotBinded(Context context, int userId) {
        if (!isFingerprintEnabled(context)) {
            return false;
        }
        BiometricManager brm = open(context);
        if (brm == null) {
            return false;
        }
        boolean res;
        if (brm.getAssociation(context, PKG_KEY_GUARD, userId)) {
            res = false;
        } else {
            res = true;
        }
        brm.release();
        return res;
    }

    private static int translateOriginHelp(int originCode) {
        switch (originCode) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            default:
                return -1;
        }
    }

    private static int translateCustHelp(int custCode) {
        switch (custCode) {
            case 2014:
                return 1001;
            case 2015:
                return 1002;
            case 2016:
                return 1003;
            default:
                return -1;
        }
    }

    public List<Fingerprint> getFingerprintList() {
        return getFingerprintList(UserHandle.myUserId());
    }

    public List<Fingerprint> getFingerprintList(int userId) {
        List<Fingerprint> enrolledFingerprints = this.mFpm.getEnrolledFingerprints(userId);
        return enrolledFingerprints == null ? new ArrayList() : enrolledFingerprints;
    }

    public Fingerprint getFingerprint(int fpId) {
        return getFingerprint(fpId, UserHandle.myUserId());
    }

    public Fingerprint getFingerprint(int fpId, int userId) {
        for (Fingerprint fp : getFingerprintList(userId)) {
            if (fp.getFingerId() == fpId) {
                return fp;
            }
        }
        return null;
    }

    public void removeFingerprint(Fingerprint fp, RemovalCallback removeCallback, int userId) {
        if (fp == null) {
            Log.e("BiometricManager", "invalid arguments, fp is null");
        } else if (removeCallback == null) {
            Log.e("BiometricManager", "invalid arguments, removeCallback is null");
        } else {
            Log.d("BiometricManager", "execute removeFingerprint");
            this.mFpm.remove(fp, userId, removeCallback);
        }
    }

    public void renameFingerprint(int fpId, String fpName, int userId) {
        if (fpName == null) {
            Log.e("BiometricManager", "null fingerprint name, rename request ignored.");
        }
        this.mFpm.rename(fpId, userId, fpName);
    }

    public long preEnrollSafe() {
        Log.d("BiometricManager", "execute preEnrollSafe");
        return preEnrollInternal(this.mFpm);
    }

    public int postEnrollSafe(long challenge) {
        Log.d("BiometricManager", "execute postEnrollSafe");
        return postEnrollInternal(this.mFpm, challenge);
    }

    private static long preEnrollInternal(FingerprintManager fpm) {
        long j;
        synchronized (sEnrollLock) {
            sLatestChallenge = fpm.preEnroll();
            j = sLatestChallenge;
        }
        return j;
    }

    private static int postEnrollInternal(FingerprintManager fpm, long challenge) {
        int ret = 0;
        synchronized (sEnrollLock) {
            if (challenge == sLatestChallenge) {
                Log.d("BiometricManager", "FingerprintManager postEnroll executed!");
                ret = fpm.postEnroll();
            } else {
                Log.w("BiometricManager", "challenge mismatched, do not execute FingerprintManager postEnroll!");
            }
        }
        return ret;
    }

    public int getEnrolledFpNum() {
        return getEnrolledFpNum(UserHandle.myUserId());
    }

    public int getEnrolledFpNum(int userId) {
        List<Fingerprint> fps = this.mFpm.getEnrolledFingerprints(userId);
        return fps == null ? 0 : fps.size();
    }
}
