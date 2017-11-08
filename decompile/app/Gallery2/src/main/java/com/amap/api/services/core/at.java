package com.amap.api.services.core;

/* compiled from: Util */
class at {
    at() {
    }

    static String a(String str) {
        if (str == null) {
            return null;
        }
        String b = aa.b(str.getBytes());
        return ((char) ((b.length() % 26) + 65)) + b;
    }

    static String b(String str) {
        return str.length() >= 2 ? aa.a(str.substring(1)) : "";
    }
}
