package tmsdk.common.module.intelli_sms;

import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdkobf.mz;

/* compiled from: Unknown */
public class Buffalo {
    private static boolean isLoadNativeOK;
    private volatile boolean pk = false;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.BUFFALO_LIBNAME));
    }

    public static boolean isLoadNative() {
        if (!isLoadNativeOK) {
            isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.BUFFALO_LIBNAME));
        }
        d.e("QQPimSecure", "Buffalo isLoadNativeOK? " + isLoadNativeOK);
        return isLoadNativeOK;
    }

    public native int nativeCheckSmsHash(String str, String str2, int i, DecomposeResult decomposeResult);

    public synchronized int nativeCheckSmsHash_c(String str, String str2, int i, DecomposeResult decomposeResult) {
        if (!this.pk) {
            return -1;
        }
        return nativeCheckSmsHash(str, str2, i, decomposeResult);
    }

    public native void nativeFinishHashChecker();

    public synchronized void nativeFinishHashChecker_c() {
        nativeFinishHashChecker();
        this.pk = false;
    }

    public native int nativeInitHashChecker(String str);

    public synchronized boolean nativeInitHashChecker_c(String str) {
        if (nativeInitHashChecker(str) == 0) {
            this.pk = true;
        }
        return this.pk;
    }
}
