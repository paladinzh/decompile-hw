package com.amap.api.services.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.maps.AMapException;
import java.net.Proxy;

/* compiled from: NetManger */
public class ci extends cf {
    private static ci a;
    private co b;
    private Handler c;

    /* compiled from: NetManger */
    /* renamed from: com.amap.api.services.core.ci$1 */
    class AnonymousClass1 extends cp {
        final /* synthetic */ cj a;
        final /* synthetic */ ck b;
        final /* synthetic */ ci c;

        public void a() {
            try {
                this.c.a(this.c.b(this.a, false), this.b);
            } catch (ai e) {
                this.c.a(e, this.b);
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
                        ((cn) message.obj).b.a();
                        break;
                    case 1:
                        cn cnVar = (cn) message.obj;
                        cnVar.b.a(cnVar.a);
                        break;
                    default:
                        return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static ci a(boolean z) {
        return a(z, 5);
    }

    private static synchronized ci a(boolean z, int i) {
        ci ciVar;
        synchronized (ci.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = co.a(i);
                        }
                    }
                    ciVar = a;
                } else {
                    a = new ci(z, i);
                    ciVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return ciVar;
    }

    private ci(boolean z, int i) {
        if (z) {
            this.b = co.a(i);
        }
        try {
            if (Looper.myLooper() != null) {
                this.c = new a();
            } else {
                this.c = new a(Looper.getMainLooper());
            }
        } catch (Throwable th) {
            av.a(th, "NetManger", "NetManger1");
            th.printStackTrace();
        }
    }

    public byte[] b(cj cjVar) throws ai {
        ai e;
        try {
            cl a = a(cjVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            av.b().b(th, "NetManager", "makeSyncPostRequest");
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] d(cj cjVar) throws ai {
        ai e;
        try {
            cl b = b(cjVar, false);
            if (b == null) {
                return null;
            }
            return b.a;
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] e(cj cjVar) throws ai {
        ai e;
        try {
            cl b = b(cjVar, true);
            if (b == null) {
                return null;
            }
            return b.a;
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    public cl b(cj cjVar, boolean z) throws ai {
        ai e;
        Proxy proxy = null;
        try {
            c(cjVar);
            if (cjVar.g != null) {
                proxy = cjVar.g;
            }
            return new cg(cjVar.e, cjVar.f, proxy, z).a(cjVar.g(), cjVar.c(), cjVar.b());
        } catch (ai e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    private void a(ai aiVar, ck ckVar) {
        cn cnVar = new cn();
        cnVar.a = aiVar;
        cnVar.b = ckVar;
        Message obtain = Message.obtain();
        obtain.obj = cnVar;
        obtain.what = 1;
        this.c.sendMessage(obtain);
    }

    private void a(cl clVar, ck ckVar) {
        ckVar.a(clVar.b, clVar.a);
        cn cnVar = new cn();
        cnVar.b = ckVar;
        Message obtain = Message.obtain();
        obtain.obj = cnVar;
        obtain.what = 0;
        this.c.sendMessage(obtain);
    }
}
