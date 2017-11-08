package tmsdk.common.module.intelli_sms;

import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdkobf.mz;
import tmsdkobf.nk;

/* compiled from: Unknown */
public class NativeBumblebee {
    private static boolean isLoadNativeOK;
    private nk Cs;
    private volatile boolean pk = false;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.INTELLI_SMSCHECK_LIBNAME));
    }

    public static boolean isLoadNative() {
        if (!isLoadNativeOK) {
            isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.INTELLI_SMSCHECK_LIBNAME));
        }
        d.e("QQPimSecure", "Bumble isLoadNativeOK? " + isLoadNativeOK);
        return isLoadNativeOK;
    }

    public native int nativeCalcMap(int i);

    public synchronized int nativeCalcMap_c(int i) {
        if (!this.pk) {
            return 0;
        }
        return nativeCalcMap(i);
    }

    public native int nativeCheckSms(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult);

    public synchronized int nativeCheckSms_c(SmsCheckInput smsCheckInput, SmsCheckResult smsCheckResult) {
        if (!this.pk) {
            return -1;
        }
        if (this.Cs != null) {
            this.Cs.a(smsCheckInput.sender, smsCheckInput.sms, smsCheckInput.uiSmsType);
        }
        return nativeCheckSms(smsCheckInput, smsCheckResult);
    }

    public native void nativeFinishSmsChecker();

    public synchronized void nativeFinishSmsChecker_c() {
        nativeFinishSmsChecker();
        this.pk = false;
        if (this.Cs != null) {
            this.Cs.fm();
        }
    }

    public native String nativeGetSmsInfo(String str, String str2);

    public synchronized String nativeGetSmsInfo_c(String str, String str2) {
        if (!this.pk) {
            return null;
        }
        return nativeGetSmsInfo(str, str2);
    }

    public native int nativeInitSmsChecker(int i, String str);

    public synchronized int nativeInitSmsChecker_c(int i, String str) {
        int nativeInitSmsChecker;
        nativeInitSmsChecker = nativeInitSmsChecker(i, str);
        if (nativeInitSmsChecker == 0) {
            this.pk = true;
            this.Cs = nk.fk();
            this.Cs.fl();
        }
        return nativeInitSmsChecker;
    }

    public native int nativeIsPrivateSms(String str, String str2);

    public synchronized int nativeIsPrivateSms_c(String str, String str2) {
        if (!this.pk) {
            return -1;
        }
        return nativeIsPrivateSms(str, str2);
    }
}
