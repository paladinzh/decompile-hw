package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdkobf.ew;
import tmsdkobf.ey;
import tmsdkobf.ez;
import tmsdkobf.fi;
import tmsdkobf.mz;

/* compiled from: Unknown */
public class AmScannerStatic {
    public static final int ERR_EXPIRED = -100;
    public static final int ERR_NONE = 0;
    public static final int ERR_UNKNOW = -10;
    private static boolean LL = false;
    public static final int VERSION = 3;
    private long object = 0;

    static {
        LL = false;
        LL = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_VIRUS_SCAN_LIBNAME));
    }

    public AmScannerStatic(Context context, String str) {
        this.object = newObject(context, str);
        d.d("AmScannerStatic", "load ams, object = " + this.object);
        if (this.object == 0) {
            throw new OutOfMemoryError();
        }
        initScanner(this.object);
    }

    private static native void deleteObject(long j);

    public static native String getVersion();

    private static native int initScanner(long j);

    public static native int load(String str);

    public static ew loadAmfHeader(Context context, String str) {
        if (str != null) {
            fi fiVar = new fi();
            fiVar.Z("UTF-8");
            fiVar.n();
            AtomicReference atomicReference = new AtomicReference();
            if (nativeLoadAmfHeaderBytes(context, str, atomicReference) == 0) {
                byte[] bArr = (byte[]) atomicReference.get();
                if (bArr != null) {
                    fiVar.b(bArr);
                    return (ew) fiVar.a("vci", new ew());
                }
            }
        }
        return null;
    }

    private static native int nativeLoadAmfHeaderBytes(Context context, String str, AtomicReference<byte[]> atomicReference);

    private static native int nativeUpdateMalwareInfoBytes(Context context, String str, byte[] bArr, byte[] bArr2);

    private static native long newObject(Context context, String str);

    private static native int scanApkBytes(long j, byte[] bArr, AtomicReference<byte[]> atomicReference);

    public static native void unload();

    public static int updateBase(Context context, String str, ez ezVar, List<ey> list) {
        if (ezVar == null || list == null) {
            return -6;
        }
        fi fiVar = new fi();
        fiVar.Z("UTF-8");
        fiVar.n();
        fiVar.put("vsi", ezVar);
        byte[] m = fiVar.m();
        fiVar.l();
        fiVar.put("vil", list);
        return nativeUpdateMalwareInfoBytes(context, str, m, fiVar.m());
    }

    protected void finalize() {
        if (this.object != 0) {
            deleteObject(this.object);
            this.object = 0;
        }
    }

    public QScanResult scanApk(ApkKey apkKey) {
        if (apkKey != null) {
            fi fiVar = new fi();
            fiVar.Z("UTF-8");
            fiVar.n();
            fiVar.put("ak", apkKey);
            AtomicReference atomicReference = new AtomicReference();
            int scanApkBytes = scanApkBytes(this.object, fiVar.m(), atomicReference);
            if (scanApkBytes == 0) {
                byte[] bArr = (byte[]) atomicReference.get();
                if (bArr != null) {
                    fiVar.l();
                    fiVar.b(bArr);
                    return (QScanResult) fiVar.a("qsr", new QScanResult());
                }
                d.f("AmScannerStatic", "null result data");
                return null;
            }
            d.f("AmScannerStatic", "scanApkBytes() returned " + scanApkBytes);
            return null;
        }
        d.f("AmScannerStatic", "null argument");
        return null;
    }
}
