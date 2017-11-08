package tmsdkobf;

public class fm {
    private static final char[] mh = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final byte[] mi = new byte[0];

    public static String c(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            byte b = bArr[i];
            cArr[(i * 2) + 1] = (char) mh[b & 15];
            cArr[(i * 2) + 0] = (char) mh[((byte) (b >>> 4)) & 15];
        }
        return new String(cArr);
    }

    public static byte a(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - 48);
        }
        if (c >= 'a' && c <= 'f') {
            return (byte) ((c - 97) + 10);
        }
        if (c >= 'A' && c <= 'F') {
            return (byte) ((c - 65) + 10);
        }
        return (byte) 0;
    }

    public static byte[] ac(String str) {
        if (str == null || str.equals("")) {
            return mi;
        }
        byte[] bArr = new byte[(str.length() / 2)];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) ((byte) ((a(str.charAt(i * 2)) * 16) + a(str.charAt((i * 2) + 1))));
        }
        return bArr;
    }
}
