package cn.com.xy.sms.sdk.a;

import android.os.Process;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public final class a {
    public static ExecutorService a = Executors.newFixedThreadPool(1);
    public static final ExecutorService b = Executors.newFixedThreadPool(2);
    public static ExecutorService c = Executors.newFixedThreadPool(1);
    public static ExecutorService d = Executors.newFixedThreadPool(1);
    public static final ExecutorService e = Executors.newFixedThreadPool(1);
    public static ExecutorService f = Executors.newFixedThreadPool(1);
    public static ExecutorService g = Executors.newFixedThreadPool(1);
    public static ExecutorService h = Executors.newFixedThreadPool(2);
    private static String i = "xy_update_pubinfo_1";
    private static String j = "xy_query_pubinfo_1";
    private static ExecutorService k = null;
    private static String l = "xy_logo_1";
    private static String m = "xy_richpool_1";
    private static ExecutorService n = null;
    private static String o = "xy_msgUrlPool_1";
    private static String p = "xy_local_bg_1";
    private static String q = "xy_net_bg_1";
    private static String r = "xy_dexutil_pool_1";
    private static String s = "xy_baseparse_1";
    private static ExecutorService t = null;
    private static String u = "xy_feature_parse_1";
    private static String v = "xy_service_data_query";

    public static synchronized ExecutorService a() {
        ExecutorService executorService;
        synchronized (a.class) {
            if (t == null) {
                t = Executors.newFixedThreadPool(1);
            }
            executorService = t;
        }
        return executorService;
    }

    public static void a(String str, int i) {
        try {
            Thread currentThread = Thread.currentThread();
            currentThread.setName(new StringBuilder(String.valueOf(str)).append(currentThread.hashCode()).toString());
            Process.setThreadPriority(i);
        } catch (Throwable th) {
        }
    }

    public static synchronized ExecutorService b() {
        ExecutorService executorService;
        synchronized (a.class) {
            if (k == null) {
                k = Executors.newFixedThreadPool(1);
            }
            executorService = k;
        }
        return executorService;
    }

    public static synchronized ExecutorService c() {
        ExecutorService executorService;
        synchronized (a.class) {
            if (n == null) {
                n = Executors.newFixedThreadPool(1);
            }
            executorService = n;
        }
        return executorService;
    }
}
