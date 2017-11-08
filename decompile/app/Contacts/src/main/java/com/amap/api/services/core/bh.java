package com.amap.api.services.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/* compiled from: Utils */
public class bh {
    static String a(Throwable th) {
        String a = as.a(th);
        if (a == null) {
            return null;
        }
        return a.replaceAll("\n", "<br/>");
    }

    public static String a(long j) {
        try {
            return new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS").format(new Date(j));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
