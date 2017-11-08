package com.intsig.scanner;

import android.content.Context;
import android.content.pm.Signature;
import android.text.TextUtils;
import com.android.gallery3d.settings.HicloudAccountManager;
import java.security.MessageDigest;

/* compiled from: Unknown */
public class ScannerSDK {
    private int mDetectMode = 2;

    /* compiled from: Unknown */
    public static class IllegalAppException extends Exception {
        private String mMsg;

        public IllegalAppException(String str) {
            this.mMsg = str;
        }

        public String getMessage() {
            return this.mMsg;
        }

        public String toString() {
            return this.mMsg;
        }
    }

    public ScannerSDK(Context context) throws IllegalAppException {
        String packageName = context.getPackageName();
        String signature = getSignature(context, packageName);
        String str = null;
        if ("E3F7501FA6AC4D44E405C8B9087C0E41".equalsIgnoreCase(signature)) {
            if ("com.huawei.camera".equals(packageName)) {
                str = "0f0620e1a2797bcfb3b4004550-UhnJrv";
            }
        } else if ("F66394486453141E6502F436EB072054".equalsIgnoreCase(signature)) {
            if (HicloudAccountManager.PACKAGE_NAME.equals(packageName)) {
                str = "374439a753802c8b25f5004550-UhnJrv";
            }
        } else if ("35D12BDE77B99749EC29F110338D2E7C".equalsIgnoreCase(signature)) {
            if (HicloudAccountManager.PACKAGE_NAME.equals(packageName)) {
                str = "4fafafade7db20cb3ce5004550-UhnJrv";
            }
        } else if ("70C209F19BE8A65B927E481C52FF8F66".equalsIgnoreCase(signature)) {
            if ("com.huawei.camera".equals(packageName)) {
                str = "a9e4c5b856084f570eda004550-UhnJrv";
            }
        } else if ("49DB2C4305A5B3902419F6790B86F5F4".equalsIgnoreCase(signature) && "com.intsig.imageprocessdemo".equals(packageName)) {
            str = "2415fac22258bbb48322004550-vagfvt";
        }
        try {
            init(context, str);
        } catch (IllegalAppException e) {
            throw e;
        }
    }

    private static String byteArrayToHex(byte[] bArr) {
        int i = 0;
        char[] cArr = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder stringBuilder = new StringBuilder();
        int length = bArr.length;
        while (i < length) {
            byte b = bArr[i];
            stringBuilder.append(cArr[(b >> 4) & 15]);
            stringBuilder.append(cArr[b & 15]);
            i++;
        }
        return stringBuilder.toString();
    }

    private String getSignature(Context context, String str) {
        try {
            Signature[] signatureArr = context.getPackageManager().getPackageInfo(str, 64).signatures;
            if (signatureArr.length != 0) {
                return stringMD5(signatureArr[0].toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void init(Context context, String str) throws IllegalAppException {
        if (TextUtils.isEmpty(str)) {
            throw new IllegalAppException("appKey shouldn't be empty");
        }
        int initSDKEngine = ScannerEngine.initSDKEngine(context, str);
        if (initSDKEngine != 0) {
            if (initSDKEngine == -3) {
                throw new IllegalAppException("time is Expired ");
            } else if (initSDKEngine != -2) {
                throw new IllegalAppException("unknown error code=" + initSDKEngine);
            } else {
                throw new IllegalAppException("packagename or signature is illegal");
            }
        }
    }

    private static String stringMD5(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            return byteArrayToHex(instance.digest());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean trimImage(int i, int i2, int[] iArr, int i3) {
        return ScannerEngine.trimImageS(i, i2, iArr, this.mDetectMode, i3) >= 0;
    }

    public int decodeImageS(String str) {
        return ScannerEngine.decodeImageS(str);
    }

    public void destroyContext(int i) {
        ScannerEngine.destroyThreadContext(i);
    }

    public int[] detectBorder(int i, int i2) {
        int[] iArr = new int[8];
        return ScannerEngine.detectImageS(i, i2, iArr, this.mDetectMode) >= 0 ? iArr : null;
    }

    public int initThreadContext() {
        return ScannerEngine.initThreadContext();
    }

    public void releaseImage(int i) {
        ScannerEngine.releaseImageS(i);
    }

    public void saveImage(int i, String str, int i2) {
        ScannerEngine.encodeImageS(i, str, i2, false);
    }

    public boolean trimImage(int i, int i2, int[] iArr) {
        return trimImage(i, i2, iArr, 0);
    }
}
