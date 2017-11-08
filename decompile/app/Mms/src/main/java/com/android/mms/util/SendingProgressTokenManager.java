package com.android.mms.util;

import java.util.HashMap;

public class SendingProgressTokenManager {
    private static final HashMap<Object, Long> TOKEN_POOL = new HashMap();

    public static synchronized long get(Object key) {
        long longValue;
        synchronized (SendingProgressTokenManager.class) {
            Long token = (Long) TOKEN_POOL.get(key);
            longValue = token != null ? token.longValue() : -1;
        }
        return longValue;
    }

    public static synchronized void put(Object key, long token) {
        synchronized (SendingProgressTokenManager.class) {
            TOKEN_POOL.put(key, Long.valueOf(token));
        }
    }

    public static synchronized void remove(Object key) {
        synchronized (SendingProgressTokenManager.class) {
            TOKEN_POOL.remove(key);
        }
    }
}
