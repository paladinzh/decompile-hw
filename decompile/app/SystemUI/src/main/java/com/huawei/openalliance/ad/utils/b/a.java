package com.huawei.openalliance.ad.utils.b;

import android.util.Log;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class a extends h {
    private static a b = new a();
    private String c;
    private boolean d;
    private boolean e;
    private boolean f;

    /* compiled from: Unknown */
    public static class a extends j {
        private BlockingQueue<b> a = new LinkedBlockingQueue();

        public a() {
            super("logger");
        }

        public void a(b bVar) {
            boolean offer = this.a.offer(bVar);
            if (!offer) {
                Log.w("AndroidLogger", "add record to queue result:" + offer);
            }
        }

        protected boolean a() {
            return true;
        }

        protected boolean b() {
            try {
                b bVar = (b) this.a.poll(3, TimeUnit.SECONDS);
                if (bVar != null) {
                    if (!g.a(bVar.a)) {
                        bVar.b.b(bVar.a);
                    } else if (bVar.a != null) {
                        return false;
                    }
                }
            } catch (InterruptedException e) {
            }
            return true;
        }

        protected void c() {
        }
    }

    /* compiled from: Unknown */
    public static class b {
        g a;
        h b;

        public b(g gVar, h hVar) {
            if (gVar == null && hVar == null) {
                throw new NullPointerException("record and logger is null");
            }
            this.b = hVar;
            this.a = gVar;
        }
    }

    private void b(String str, f fVar, String str2) {
        if (c(fVar)) {
            String str3 = this.c + "." + str;
            switch (fVar.a()) {
                case 2:
                    Log.v(str3, str2);
                    break;
                case 3:
                    Log.d(str3, str2);
                    break;
                case 4:
                    Log.i(str3, str2);
                    break;
                case 5:
                    Log.w(str3, str2);
                    break;
                case 6:
                    Log.e(str3, str2);
                    break;
                default:
                    Log.w(str3, "[" + fVar.toString() + "] " + str2);
                    break;
            }
        }
    }

    private boolean c(f fVar) {
        switch (fVar.a()) {
            case 2:
                return false;
            case 3:
                return this.d;
            case 4:
                return this.e;
            case 5:
            case 6:
                return this.f;
            default:
                return false;
        }
    }

    public void a(g gVar) {
        b.a(new b(gVar, this));
    }

    public boolean a(f fVar) {
        return this.a.a() <= fVar.a();
    }

    public void b(g gVar) {
        try {
            String e = gVar.e();
            b(gVar.b, gVar.c, e);
            a(gVar.b, gVar.c, gVar.d() + e);
        } catch (OutOfMemoryError e2) {
            Log.e("AndroidLogger", "write error");
        }
    }
}
