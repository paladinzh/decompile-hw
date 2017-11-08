package com.huawei.mms.util;

import com.huawei.cspcommon.MLog;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public class QuotedPrintable {
    private static byte ESCAPE_CHAR = (byte) 61;
    private static final BitSet PRINTABLE_CHARS = new BitSet(256);

    static {
        int i;
        for (i = 33; i <= 60; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (i = 62; i <= 126; i++) {
            PRINTABLE_CHARS.set(i);
        }
    }

    public static final byte[] decodeQuotedPrintable(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i = 0;
        while (i < bytes.length) {
            byte b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    if ('\r' == ((char) bytes[i + 1]) && '\n' == ((char) bytes[i + 2])) {
                        i += 2;
                    } else {
                        i++;
                        int u = Character.digit((char) bytes[i], 16);
                        i++;
                        int l = Character.digit((char) bytes[i], 16);
                        if (u == -1 || l == -1) {
                            return null;
                        }
                        buffer.write((char) ((u << 4) + l));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
            buffer.write(b);
            i++;
        }
        return buffer.toByteArray();
    }

    public static String decodeQuotedPrintable(byte[] qp, String charset) {
        if (qp == null) {
            return null;
        }
        byte[] qpDecodedBytes = decodeQuotedPrintable(qp);
        if (qpDecodedBytes == null) {
            return null;
        }
        try {
            return new String(qpDecodedBytes, charset);
        } catch (UnsupportedEncodingException e) {
            if (HwMessageUtils.OPERATOR_SOFTBANK) {
                try {
                    return new String(qpDecodedBytes, "SHIFT_JIS");
                } catch (UnsupportedEncodingException e1) {
                    MLog.e("QuotedPrintable", "Charset SHIFT_JIS is not supported!");
                    e1.printStackTrace();
                    return null;
                }
            }
            try {
                return new String(qpDecodedBytes, "UTF-8");
            } catch (UnsupportedEncodingException e12) {
                MLog.e("QuotedPrintable", "Charset SHIFT_JIS is not supported!");
                e12.printStackTrace();
                return null;
            }
        }
    }
}
