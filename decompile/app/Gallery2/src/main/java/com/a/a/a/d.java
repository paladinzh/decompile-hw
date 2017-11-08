package com.a.a.a;

import com.a.a.b.a;
import com.a.a.i;
import java.util.Map;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

/* compiled from: Unknown */
public class d {
    public static long a(String str) {
        try {
            return DateUtils.parseDate(str).getTime();
        } catch (DateParseException e) {
            return 0;
        }
    }

    public static a a(i iVar) {
        Object obj;
        long j;
        long currentTimeMillis = System.currentTimeMillis();
        Map map = iVar.c;
        long j2 = 0;
        long j3 = 0;
        long j4 = 0;
        Object obj2 = null;
        String str = (String) map.get("Date");
        if (str != null) {
            j2 = a(str);
        }
        str = (String) map.get("Cache-Control");
        if (str == null) {
            obj = null;
        } else {
            String[] split = str.split(",");
            obj = null;
            j = 0;
            j4 = 0;
            for (String trim : split) {
                String trim2 = trim2.trim();
                if (trim2.equals("no-cache") || trim2.equals("no-store")) {
                    return null;
                }
                if (trim2.startsWith("max-age=")) {
                    try {
                        j4 = Long.parseLong(trim2.substring(8));
                    } catch (Exception e) {
                    }
                } else if (trim2.startsWith("stale-while-revalidate=")) {
                    try {
                        j = Long.parseLong(trim2.substring(23));
                    } catch (Exception e2) {
                    }
                } else if (trim2.equals("must-revalidate") || trim2.equals("proxy-revalidate")) {
                    obj = 1;
                }
            }
            j3 = j4;
            j4 = j;
            obj2 = obj;
            int i = 1;
        }
        str = (String) map.get("Expires");
        long a = str == null ? 0 : a(str);
        str = (String) map.get("Last-Modified");
        long a2 = str == null ? 0 : a(str);
        str = (String) map.get("ETag");
        if (obj == null) {
            if ((j2 <= 0 ? 1 : null) == null) {
                if ((a < j2 ? 1 : null) == null) {
                    j = (a - j2) + currentTimeMillis;
                    j3 = j;
                }
            }
            j = 0;
            j3 = 0;
        } else {
            j3 = (j3 * 1000) + currentTimeMillis;
            j = obj2 == null ? (1000 * j4) + j3 : j3;
        }
        a aVar = new a();
        aVar.a = iVar.b;
        aVar.b = str;
        aVar.f = j3;
        aVar.e = j;
        aVar.c = j2;
        aVar.d = a2;
        aVar.g = map;
        return aVar;
    }

    public static String a(Map<String, String> map) {
        return a(map, "ISO-8859-1");
    }

    public static String a(Map<String, String> map, String str) {
        String str2 = (String) map.get("Content-Type");
        if (str2 != null) {
            String[] split = str2.split(";");
            for (int i = 1; i < split.length; i++) {
                String[] split2 = split[i].trim().split("=");
                if (split2.length == 2 && split2[0].equals("charset")) {
                    return split2[1];
                }
            }
        }
        return str;
    }
}
