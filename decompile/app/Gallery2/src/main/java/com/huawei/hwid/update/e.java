package com.huawei.hwid.update;

import android.content.Context;
import android.os.Handler;
import com.huawei.hwid.update.a.b;
import java.util.Map;

public class e {
    private static e a;
    private Map<Integer, b> b;
    private b c;
    private f d;
    private boolean e = false;
    private boolean f = false;

    public static synchronized e a() {
        e eVar;
        synchronized (e.class) {
            if (a == null) {
                a = new e();
            }
            eVar = a;
        }
        return eVar;
    }

    public void a(boolean z) {
        this.e = z;
    }

    public boolean b() {
        return this.f;
    }

    public void a(Map<Integer, b> map) {
        this.b = map;
    }

    public void a(int i) {
        if (this.b != null && this.b.containsKey(Integer.valueOf(i))) {
            this.b.remove(Integer.valueOf(i));
        }
    }

    public void c() {
        if (this.b != null) {
            this.b.clear();
            this.b = null;
        }
    }

    public b b(int i) {
        if (this.b != null && this.b.containsKey(Integer.valueOf(i))) {
            return (b) this.b.get(Integer.valueOf(i));
        }
        return null;
    }

    public void a(Context context, int i, Handler handler) {
        d();
        this.c = new b(context, new g(), i, handler);
        this.e = true;
        this.c.start();
    }

    public void d() {
        if (this.c != null) {
            this.c.a();
            this.c = null;
        }
        this.e = false;
    }

    public void e() {
        if (this.d != null) {
            this.d.a();
            this.d = null;
        }
    }

    public void a(Context context, Handler handler, int i) {
        this.d = new f(context, handler, i);
        this.d.start();
    }
}
