package tmsdk.common.tcc;

import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdkobf.mz;
import tmsdkobf.qv;
import tmsdkobf.qv.a;
import tmsdkobf.qx;

/* compiled from: Unknown */
public class SdcardScannerFactory {
    public static final long FLAG_GET_ALL_FILE = 8;
    public static final long FLAG_NEED_BASIC_INFO = 2;
    public static final long FLAG_NEED_EXTRA_INFO = 4;
    public static final long FLAG_SCAN_WIDE = 16;
    public static final int TYPE_QSCANNER = 1;
    public static boolean isLoadNativeOK;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), "dce-1.1.6-mfr");
    }

    public static DeepCleanEngine getDeepCleanEngine(Callback callback) {
        return getDeepCleanEngine(callback, 0);
    }

    public static DeepCleanEngine getDeepCleanEngine(Callback callback, int i) {
        if (!isLoadNativeOK) {
            return null;
        }
        DeepCleanEngine deepCleanEngine = new DeepCleanEngine(callback);
        return !deepCleanEngine.init(i) ? null : deepCleanEngine;
    }

    public static QSdcardScanner getQSdcardScanner(long j, a aVar, qx qxVar) {
        QSdcardScanner qSdcardScanner = (QSdcardScanner) getScanner(1, j, qxVar);
        if (qSdcardScanner == null) {
            return null;
        }
        qSdcardScanner.setListener(aVar);
        return qSdcardScanner;
    }

    private static qv getScanner(int i, long j, Object obj) {
        switch (i) {
            case 1:
                long nativeAllocate = nativeAllocate(i, j);
                return nativeAllocate == 0 ? null : new QSdcardScanner(nativeAllocate, i, j, obj);
            default:
                return null;
        }
    }

    private static native long nativeAllocate(int i, long j);
}
