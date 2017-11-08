package com.amap.api.mapcore.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.maps.AMapException;
import java.net.Proxy;

/* compiled from: NetManger */
public class di extends dd {
    private static di a;
    private dn b;
    private Handler c;

    /* compiled from: NetManger */
    /* renamed from: com.amap.api.mapcore.util.di$1 */
    class AnonymousClass1 extends dp {
        final /* synthetic */ dj a;
        final /* synthetic */ dk b;
        final /* synthetic */ di c;

        public void a() {
            try {
                this.c.a(this.c.b(this.a, false), this.b);
            } catch (bk e) {
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
                        ((dm) message.obj).b.a();
                        break;
                    case 1:
                        dm dmVar = (dm) message.obj;
                        dmVar.b.a(dmVar.a);
                        break;
                    default:
                        return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static di a(boolean z) {
        return a(z, 5);
    }

    private static synchronized di a(boolean z, int i) {
        di diVar;
        synchronized (di.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = dn.a(i);
                        }
                    }
                    diVar = a;
                } else {
                    a = new di(z, i);
                    diVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return diVar;
    }

    private di(boolean z, int i) {
        if (z) {
            this.b = dn.a(i);
        }
        try {
            if (Looper.myLooper() != null) {
                this.c = new a();
            } else {
                this.c = new a(Looper.getMainLooper());
            }
        } catch (Throwable th) {
            ce.a(th, "NetManger", "NetManger1");
            th.printStackTrace();
        }
    }

    public byte[] b(dj djVar) throws bk {
        bk e;
        try {
            dl a = a(djVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            ce.a().b(th, "NetManager", "makeSyncPostRequest");
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    public byte[] d(dj djVar) throws bk {
        bk e;
        try {
            dl b = b(djVar, false);
            if (b == null) {
                return null;
            }
            return b.a;
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    public dl b(dj djVar, boolean z) throws bk {
        bk e;
        Proxy proxy = null;
        try {
            c(djVar);
            if (djVar.i != null) {
                proxy = djVar.i;
            }
            return new df(djVar.g, djVar.h, proxy, z).a(djVar.a(), djVar.c(), djVar.b());
        } catch (bk e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    private void a(bk bkVar, dk dkVar) {
        dm dmVar = new dm();
        dmVar.a = bkVar;
        dmVar.b = dkVar;
        Message obtain = Message.obtain();
        obtain.obj = dmVar;
        obtain.what = 1;
        this.c.sendMessage(obtain);
    }

    private void a(dl dlVar, dk dkVar) {
        dkVar.a(dlVar.b, dlVar.a);
        dm dmVar = new dm();
        dmVar.b = dkVar;
        Message obtain = Message.obtain();
        obtain.obj = dmVar;
        obtain.what = 0;
        this.c.sendMessage(obtain);
    }
}
