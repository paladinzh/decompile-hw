package com.amap.api.services.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import org.apache.http.HttpEntity;

/* compiled from: NetManger */
public class bs {
    private static bs a;
    private av b;
    private Handler c;

    /* compiled from: NetManger */
    /* renamed from: com.amap.api.services.core.bs$1 */
    class AnonymousClass1 extends ax {
        final /* synthetic */ bt b;
        final /* synthetic */ bu c;
        final /* synthetic */ bs d;

        public void a() {
            try {
                this.d.a(this.d.b(this.b, false), this.c);
            } catch (v e) {
                this.d.a(e, this.c);
            }
        }
    }

    /* compiled from: NetManger */
    static class a extends Handler {
        private a(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                switch (message.what) {
                    case 0:
                        ((bx) message.obj).b.a();
                        break;
                    case 1:
                        bx bxVar = (bx) message.obj;
                        bxVar.b.a(bxVar.a);
                        break;
                    default:
                        return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static bs a(boolean z) {
        return a(z, 5);
    }

    private static synchronized bs a(boolean z, int i) {
        bs bsVar;
        synchronized (bs.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = av.a(i);
                        }
                    }
                    bsVar = a;
                } else {
                    a = new bs(z, i);
                    bsVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return bsVar;
    }

    private bs(boolean z, int i) {
        if (z) {
            this.b = av.a(i);
        }
        try {
            if (Looper.myLooper() != null) {
                this.c = new a();
            } else {
                this.c = new a(Looper.getMainLooper());
            }
        } catch (Throwable th) {
            ay.a(th, "NetManger", "NetManger1");
            th.printStackTrace();
        }
    }

    public byte[] a(bt btVar) throws v {
        v e;
        try {
            bv a = a(btVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (v e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new v("未知的错误");
        }
    }

    public byte[] b(bt btVar) throws v {
        v e;
        try {
            bv a = a(btVar, true);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (v e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new v("未知的错误");
        }
    }

    private bv a(bt btVar, boolean z) throws v {
        v e;
        Proxy proxy = null;
        HttpEntity e2 = btVar.e();
        byte[] e_ = btVar.e_();
        try {
            c(btVar);
            if (btVar.g != null) {
                proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(btVar.g.getHostName(), btVar.g.getPort()));
            }
            bq bqVar = new bq(btVar.e, btVar.f, proxy, z);
            if (e2 == null && e_ == null) {
                return bqVar.b(btVar.b(), btVar.d_(), btVar.c_());
            }
            if (e_ == null) {
                return bqVar.a(btVar.b(), btVar.d_(), btVar.c_(), e2);
            }
            return bqVar.a(btVar.b(), btVar.d_(), btVar.c_(), e_);
        } catch (v e3) {
            throw e3;
        } catch (Throwable th) {
            th.printStackTrace();
            e3 = new v("未知的错误");
        }
    }

    private bv b(bt btVar, boolean z) throws v {
        v e;
        Proxy proxy = null;
        try {
            c(btVar);
            if (btVar.g != null) {
                proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(btVar.g.getHostName(), btVar.g.getPort()));
            }
            return new bq(btVar.e, btVar.f, proxy, z).a(btVar.b(), btVar.d_(), btVar.c_());
        } catch (v e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new v("未知的错误");
        }
    }

    private void c(bt btVar) throws v {
        if (btVar == null) {
            throw new v("requeust is null");
        } else if (btVar.b() == null || "".equals(btVar.b())) {
            throw new v("request url is empty");
        }
    }

    private void a(v vVar, bu buVar) {
        bx bxVar = new bx();
        bxVar.a = vVar;
        bxVar.b = buVar;
        Message obtain = Message.obtain();
        obtain.obj = bxVar;
        obtain.what = 1;
        this.c.sendMessage(obtain);
    }

    private void a(bv bvVar, bu buVar) {
        buVar.a(bvVar.b, bvVar.a);
        bx bxVar = new bx();
        bxVar.b = buVar;
        Message obtain = Message.obtain();
        obtain.obj = bxVar;
        obtain.what = 0;
        this.c.sendMessage(obtain);
    }
}
