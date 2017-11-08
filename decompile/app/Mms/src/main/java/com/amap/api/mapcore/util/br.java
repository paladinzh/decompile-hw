package com.amap.api.mapcore.util;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/* compiled from: Encrypt */
public class br {
    private static final char[] a = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] b = new byte[128];

    public static String a(String str) {
        return bx.a(b(str));
    }

    public static String a(byte[] bArr) {
        try {
            return c(bArr);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    static {
        int i = 0;
        int i2 = 48;
        while (i < 128) {
            b[i] = (byte) -1;
            i++;
        }
        for (i = 65; i <= 90; i++) {
            b[i] = (byte) ((byte) (i - 65));
        }
        for (i = 97; i <= 122; i++) {
            b[i] = (byte) ((byte) ((i - 97) + 26));
        }
        while (i2 <= 57) {
            b[i2] = (byte) ((byte) ((i2 - 48) + 52));
            i2++;
        }
        b[43] = (byte) 62;
        b[47] = (byte) 63;
    }

    static byte[] a(byte[] bArr, byte[] bArr2) {
        try {
            return b(bArr, bArr2);
        } catch (Throwable e) {
            cb.a(e, "Encrypt", "aesEncrypt");
            return null;
        } catch (Throwable e2) {
            cb.a(e2, "Encrypt", "aesEncrypt");
            return null;
        } catch (Throwable e22) {
            cb.a(e22, "Encrypt", "aesEncrypt");
            return null;
        } catch (Throwable e222) {
            cb.a(e222, "Encrypt", "aesEncrypt");
            return null;
        } catch (Throwable e2222) {
            cb.a(e2222, "Encrypt", "aesEncrypt");
            return null;
        } catch (Throwable e22222) {
            cb.a(e22222, "Encrypt", "aesEncrypt");
            return null;
        }
    }

    private static byte[] b(byte[] bArr, byte[] bArr2) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(bx.a());
        Key secretKeySpec = new SecretKeySpec(bArr, "AES");
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        try {
            instance.init(1, secretKeySpec, ivParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return instance.doFinal(bArr2);
    }

    static byte[] a(byte[] bArr, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        instance.init(1, key);
        return instance.doFinal(bArr);
    }

    public static byte[] b(String str) {
        int i = 0;
        if (str == null) {
            return new byte[0];
        }
        byte[] a = bx.a(str);
        int length = a.length;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(length);
        while (i < length) {
            while (true) {
                int i2 = i + 1;
                byte b = b[a[i]];
                if (i2 < length && b == (byte) -1) {
                    i = i2;
                }
            }
            if (b == (byte) -1) {
                break;
            }
            while (true) {
                i = i2 + 1;
                byte b2 = b[a[i2]];
                if (i < length && b2 == (byte) -1) {
                    i2 = i;
                }
            }
            if (b2 == (byte) -1) {
                break;
            }
            byteArrayOutputStream.write((b << 2) | ((b2 & 48) >>> 4));
            while (true) {
                i2 = i + 1;
                byte b3 = a[i];
                if (b3 == (byte) 61) {
                    return byteArrayOutputStream.toByteArray();
                }
                b = b[b3];
                if (i2 < length && b == (byte) -1) {
                    i = i2;
                }
            }
            if (b == (byte) -1) {
                break;
            }
            byteArrayOutputStream.write(((b2 & 15) << 4) | ((b & 60) >>> 2));
            while (true) {
                i = i2 + 1;
                byte b4 = a[i2];
                if (b4 == (byte) 61) {
                    return byteArrayOutputStream.toByteArray();
                }
                b4 = b[b4];
                if (i < length && b4 == (byte) -1) {
                    i2 = i;
                }
            }
            if (b4 == (byte) -1) {
                break;
            }
            byteArrayOutputStream.write(b4 | ((b & 3) << 6));
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static String c(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        int length = bArr.length;
        int i = 0;
        while (i < length) {
            int i2 = i + 1;
            int i3 = bArr[i] & 255;
            if (i2 == length) {
                stringBuffer.append(a[i3 >>> 2]);
                stringBuffer.append(a[(i3 & 3) << 4]);
                stringBuffer.append("==");
                break;
            }
            int i4 = i2 + 1;
            i2 = bArr[i2] & 255;
            if (i4 == length) {
                stringBuffer.append(a[i3 >>> 2]);
                stringBuffer.append(a[((i3 & 3) << 4) | ((i2 & 240) >>> 4)]);
                stringBuffer.append(a[(i2 & 15) << 2]);
                stringBuffer.append("=");
                break;
            }
            i = i4 + 1;
            i4 = bArr[i4] & 255;
            stringBuffer.append(a[i3 >>> 2]);
            stringBuffer.append(a[((i3 & 3) << 4) | ((i2 & 240) >>> 4)]);
            stringBuffer.append(a[((i2 & 15) << 2) | ((i4 & 192) >>> 6)]);
            stringBuffer.append(a[i4 & 63]);
        }
        return stringBuffer.toString();
    }

    public static String b(byte[] bArr) {
        try {
            return c(bArr);
        } catch (Throwable th) {
            cb.a(th, "Encrypt", "encodeBase64");
            return null;
        }
    }
}
