package tmsdkobf;

public class fk {
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
}
