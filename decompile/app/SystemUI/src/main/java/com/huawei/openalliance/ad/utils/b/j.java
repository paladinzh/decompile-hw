package com.huawei.openalliance.ad.utils.b;

/* compiled from: Unknown */
public abstract class j extends Thread {
    private volatile boolean a;
    private volatile boolean b;
    private l c;

    public j(String str) {
        this(str, null);
    }

    public j(String str, l lVar) {
        super(str);
        this.a = true;
        this.b = false;
        this.c = lVar;
    }

    private boolean f() {
        return a();
    }

    private void g() {
        e();
        c();
    }

    private boolean h() {
        return b();
    }

    protected boolean a() {
        return true;
    }

    protected abstract boolean b();

    protected void c() {
    }

    public String d() {
        StringBuilder stringBuilder = new StringBuilder(64);
        stringBuilder.append(getName());
        stringBuilder.append('{');
        stringBuilder.append(getId());
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    protected void e() {
        if (!this.b && this.c != null) {
            this.c.a(this);
        }
    }

    public void run() {
        if (f()) {
            while (this.a) {
                try {
                    if (!h()) {
                        break;
                    }
                } catch (Exception e) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                }
            }
        }
        g();
    }

    public String toString() {
        return d();
    }
}
