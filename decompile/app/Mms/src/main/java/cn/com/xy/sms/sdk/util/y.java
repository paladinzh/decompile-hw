package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.db.entity.B;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* compiled from: Unknown */
public final class y extends Thread {
    private static boolean a = false;

    public static synchronized void a() {
        synchronized (y.class) {
            if (!a) {
                new y().start();
            }
        }
    }

    private static void a(int i) {
        List a = B.a(i, 0);
        if (!a.isEmpty()) {
            SceneconfigUtil.insertOrUpdateSceneConfigAndRequestScenceConfig(a, i, false);
        }
    }

    private static void a(String str) {
        try {
            TimerTask zVar = new z();
            Timer timer = new Timer();
            Date date = new Date();
            Calendar instance = Calendar.getInstance();
            try {
                date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(new StringBuilder(String.valueOf(instance.get(1) + "/" + (instance.get(2) + 1) + "/" + instance.get(5))).append(" ").append(str).toString());
            } catch (Throwable th) {
            }
            timer.schedule(zVar, date);
        } catch (Throwable th2) {
        }
    }

    public final void run() {
        try {
            if (!a) {
                a = true;
                try {
                    Thread.sleep(20000);
                    J.a(false);
                    Thread.sleep(10000);
                } catch (Throwable th) {
                }
                a(0);
                a(1);
                F.a(false);
                SceneconfigUtil.updateData();
                a = false;
            }
        } catch (Throwable th2) {
        }
    }
}
