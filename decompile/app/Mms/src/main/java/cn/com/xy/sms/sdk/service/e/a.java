package cn.com.xy.sms.sdk.service.e;

import android.os.Process;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.net.NetUtil;

/* compiled from: Unknown */
public final class a implements Runnable {
    private static int d = 1;
    private static int e = 2;
    public String a = null;
    public String b = null;
    public boolean c = false;
    private int f = 0;

    private a() {
    }

    public a(int i) {
        this.f = i;
    }

    private void a() {
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                g.a(this.b, this.a);
            }
            g.a();
        } catch (Throwable th) {
            th.getMessage();
        }
    }

    private void b() {
        g.a(this.b, this.a, false);
        g.b(this.b, this.a, false);
        f.e();
        synchronized (this) {
            g.b = false;
        }
    }

    private void c() {
        this.a = null;
        this.b = null;
    }

    public final void run() {
        try {
            Process.setThreadPriority(10);
            this.c = true;
            switch (this.f) {
                case 1:
                    g.a(this.b, this.a, false);
                    g.b(this.b, this.a, false);
                    f.e();
                    synchronized (this) {
                        g.b = false;
                    }
                    break;
                case 2:
                    try {
                        if (NetUtil.checkAccessNetWork(2)) {
                            g.a(this.b, this.a);
                        }
                        g.a();
                        break;
                    } catch (Throwable th) {
                        th.getMessage();
                        break;
                    }
            }
            this.c = false;
            c();
        } catch (Throwable th2) {
            try {
                th2.getMessage();
            } finally {
                this.c = false;
                c();
            }
        }
    }
}
