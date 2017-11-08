package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.SingalThread;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: GLMapTimer */
public class dp {
    m a;
    a b;
    private int c = 30;
    private int d = 33;
    private boolean e = false;
    private Object f = new Object();

    /* compiled from: GLMapTimer */
    private class a extends SingalThread {
        volatile int a;
        volatile boolean b;
        volatile boolean c;
        final /* synthetic */ dp d;
        private int e;

        public a(dp dpVar) {
            this.d = dpVar;
            this.a = 0;
            this.b = false;
            this.c = false;
            this.e = 100;
            this.a = 90;
            this.logTag = "render";
        }

        public void a() {
            this.a = 0;
        }

        private void a(int i) {
            if (!this.c) {
                this.a = i;
                doAwake();
            }
        }

        private void b() throws InterruptedException {
            while (this.a > 0 && !this.c) {
                this.d.a.requestRender();
                if (this.a > 0) {
                    this.a--;
                }
                sleep((long) this.d.d);
            }
        }

        private void a(int i, int i2) throws InterruptedException {
            int i3 = i2 / this.e;
            int i4 = 0;
            int i5 = 0;
            while (this.a <= 0 && !this.c) {
                i5++;
                if (i5 >= i3) {
                    if (i != -1) {
                        i4++;
                    }
                    this.d.a.requestRender();
                    i5 = 0;
                }
                if (i != -1) {
                    if (i4 >= i) {
                        return;
                    }
                }
                sleep((long) this.e);
            }
        }

        public void run() {
            while (!this.b) {
                try {
                    b();
                    a(30, 100);
                    if (this.a > 0) {
                        if (this.c) {
                        }
                    }
                    a(5, (int) SmsCheckResult.ESCT_200);
                    if (this.a > 0) {
                        if (this.c) {
                        }
                    }
                    a(2, 500);
                    if (this.a > 0) {
                        if (this.c) {
                        }
                    }
                    if (this.d.e) {
                        a(-1, 10000);
                        if (this.a > 0 && this.c) {
                        }
                    } else {
                        doWait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public dp(m mVar) {
        this.a = mVar;
    }

    public void a(int i) {
        if (this.c != i && i > 0) {
            this.d = 1000 / i;
            this.c = i;
        }
    }

    public void a() {
        synchronized (this.f) {
            if (this.b != null) {
                this.b.a();
                this.b.b = true;
                if (this.b.isAlive()) {
                    try {
                        this.b.interrupt();
                        this.b = null;
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public void b() {
        synchronized (this.f) {
            if (this.b != null && this.b.isAlive()) {
                this.b.b = true;
                try {
                    this.b.interrupt();
                    this.b = null;
                } catch (Exception e) {
                }
            }
            this.b = new a(this);
            this.b.start();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b(int i) {
        try {
            synchronized (this.f) {
                if (!(this.b == null || this.b.c)) {
                    this.b.a(i);
                }
            }
        } catch (NullPointerException e) {
        }
    }

    public boolean c() {
        synchronized (this.f) {
            if (this.b == null) {
                return true;
            }
            boolean z = this.b.c;
            return z;
        }
    }
}
