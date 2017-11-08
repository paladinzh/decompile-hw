package com.huawei.hwid.core.model.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.huawei.hwid.core.c.b.a;
import java.util.Date;

public class FetchCountryIPService extends Service {
    private static Thread a = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStart(Intent intent, int i) {
        Object obj = 1;
        a.b("FetchCountryIPService", "onStart");
        if (a == null || !a.isAlive()) {
            Object obj2;
            long a = com.huawei.hwid.core.b.a.a((Context) this).a("lastCheckDate", 0);
            long time = new Date().getTime();
            if (a <= time) {
                obj2 = 1;
            } else {
                obj2 = null;
            }
            if (obj2 == null) {
                a = 0;
            }
            if (a != 0) {
                if (time - a > 1296000000) {
                    obj = null;
                }
            }
            a((Context) this);
            if (a != null) {
                a.start();
            }
            stopSelf();
        }
    }

    public static synchronized void a(Context context) {
        synchronized (FetchCountryIPService.class) {
            a(new Thread(new a(context)));
        }
    }

    public void onDestroy() {
        a.b("FetchCountryIPService", "onDestroy");
        a(null);
        super.onDestroy();
    }

    private static synchronized void a(Thread thread) {
        synchronized (FetchCountryIPService.class) {
            a = thread;
        }
    }
}
