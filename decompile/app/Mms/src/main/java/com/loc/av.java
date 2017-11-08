package com.loc;

import java.io.UnsupportedEncodingException;

/* compiled from: Util */
class av {
    av() {
    }

    static String a(String str) {
        if (str == null) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = str.getBytes();
        }
        String a = r.a(bytes);
        return ((char) ((a.length() % 26) + 65)) + a;
    }

    static String b(String str) {
        return str.length() >= 2 ? r.a(str.substring(1)) : "";
    }
}
