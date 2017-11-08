package cn.com.xy.sms.sdk.util;

/* compiled from: Unknown */
public class DataEnCipher {
    public native boolean getChannelData(int i);

    public native boolean getKeyData(int i);

    public native byte[] xyBase64Decode2(String str);

    public native String xyBase64Encode2(byte[] bArr, int i);

    public native byte[] xyDecrypt(byte[] bArr, int i, byte[] bArr2, int i2);

    public native byte[] xyEncrypt(byte[] bArr, int i, byte[] bArr2, int i2);
}
