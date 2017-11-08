package tmsdkobf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* compiled from: Unknown */
public class nb {
    private static final char[] BW = "0123456789abcdef".toCharArray();

    public static byte[] bG(int i) {
        return new byte[]{(byte) ((byte) ((i >> 24) & 255)), (byte) ((byte) ((i >> 16) & 255)), (byte) ((byte) ((i >> 8) & 255)), (byte) ((byte) (i & 255))};
    }

    public static byte[] cF(String str) {
        return n(str.getBytes());
    }

    public static String cG(String str) {
        byte[] cF = cF(str);
        if (cF == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(cF.length * 2);
        for (byte b : cF) {
            stringBuilder.append(Integer.toHexString(b & 255).substring(0, 1));
        }
        return stringBuilder.toString();
    }

    public static byte[] n(byte[] bArr) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(bArr);
            return instance.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String o(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder(bArr.length * 3);
        for (byte b : bArr) {
            int i = b & 255;
            stringBuilder.append(BW[i >> 4]);
            stringBuilder.append(BW[i & 15]);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String p(byte[] bArr) {
        return o(n(bArr));
    }
}
