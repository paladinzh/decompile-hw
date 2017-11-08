package com.coremedia.iso;

import com.android.gallery3d.gadget.XmlUtils;
import java.io.UnsupportedEncodingException;

public final class Utf8 {
    public static byte[] convert(String s) {
        if (s == null) {
            return null;
        }
        try {
            return s.getBytes(XmlUtils.INPUT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String convert(byte[] b) {
        if (b == null) {
            return null;
        }
        try {
            return new String(b, XmlUtils.INPUT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static int utf8StringLengthInBytes(String utf8) {
        if (utf8 == null) {
            return 0;
        }
        try {
            return utf8.getBytes(XmlUtils.INPUT_ENCODING).length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }
}
