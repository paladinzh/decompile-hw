package com.avast.android.sdk.engine.obfuscated;

import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Unknown */
public class f {
    static final /* synthetic */ boolean a;

    static {
        boolean z = false;
        if (!f.class.desiredAssertionStatus()) {
            z = true;
        }
        a = z;
    }

    private static Key a(byte[] bArr) {
        if (bArr.length == 0) {
            bArr = new byte[20];
        }
        return new SecretKeySpec(bArr, "HmacSHA1");
    }

    private static byte[] a(String str) {
        if (str.length() % 2 != 0) {
            str = "0" + str;
        }
        byte[] bArr = new byte[(str.length() / 2)];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) ((byte) Integer.parseInt(str.substring(i * 2, (i * 2) + 2), 16));
        }
        return bArr;
    }

    private static byte[] a(byte[] bArr, Mac mac) {
        mac.update(bArr);
        byte[] doFinal = mac.doFinal();
        mac.reset();
        return doFinal;
    }

    public static byte[] a(byte[] bArr, byte[] bArr2) throws NoSuchAlgorithmException, InvalidKeyException {
        return a(bArr2, b(bArr));
    }

    public static byte[] a(byte[] bArr, byte[] bArr2, int i) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac b = b(bArr);
        int ceil = (int) Math.ceil(((double) i) / RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE);
        byte[] bArr3 = new byte[0];
        byte[] bArr4 = new byte[0];
        for (int i2 = 0; i2 < ceil; i2++) {
            bArr3 = a(a(bArr3, bArr2, a(Integer.toHexString(i2 + 1))), b);
            bArr4 = a(bArr4, bArr3);
        }
        Object obj = new byte[i];
        System.arraycopy(bArr4, 0, obj, 0, i);
        return obj;
    }

    private static byte[] a(byte[] bArr, byte[]... bArr2) {
        int length = bArr.length + 0;
        for (byte[] length2 : bArr2) {
            length += length2.length;
        }
        Object obj = new byte[length];
        int length3 = bArr.length;
        System.arraycopy(bArr, 0, obj, 0, length3);
        length = length3;
        for (Object obj2 : bArr2) {
            System.arraycopy(obj2, 0, obj, length, obj2.length);
            length += obj2.length;
        }
        return obj;
    }

    private static Mac b(byte[] bArr) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac instance = Mac.getInstance("HmacSHA1");
        if (!a && instance == null) {
            throw new AssertionError();
        }
        instance.init(a(bArr));
        return instance;
    }
}
