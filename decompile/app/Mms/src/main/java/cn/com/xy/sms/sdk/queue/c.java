package cn.com.xy.sms.sdk.queue;

import android.os.Process;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
public final class c extends Thread {
    private static int a = 1;
    private static int b = 2;
    private static boolean c = false;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a() {
        synchronized (c.class) {
            if (!NetUtil.checkAccessNetWork(1)) {
            } else if (!c) {
                c = true;
                new c().start();
            }
        }
    }

    private static void a(int i) {
        String queryLog = DexUtil.queryLog(Integer.valueOf(i), KeyManager.channel, NetUtil.APPVERSION);
        try {
            if (!StringUtils.isNull(queryLog) && !"[]".equals(queryLog)) {
                NetUtil.requestNewTokenIfNeed(null);
                NetUtil.executeNewServiceHttpRequest(NetUtil.URL_LOG_SERVICE, queryLog, new d(queryLog, NetUtil.getToken(), i), false, false, true, null);
            }
        } catch (Exception e) {
        }
    }

    private static void a(int i, String str) {
        try {
            if (!StringUtils.isNull(str) && !"[]".equals(str)) {
                NetUtil.requestNewTokenIfNeed(null);
                NetUtil.executeNewServiceHttpRequest(NetUtil.URL_LOG_SERVICE, str, new d(str, NetUtil.getToken(), i), false, false, true, null);
            }
        } catch (Exception e) {
        }
    }

    public final void run() {
        try {
            setName("xiaoyuan_ReportLogQueue");
            KeyManager.initAppKey();
            Process.setThreadPriority(i.b);
            a(1);
            a(2);
        } catch (Throwable th) {
        } finally {
            c = false;
        }
    }
}
