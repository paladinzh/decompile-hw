package com.amap.api.mapcore.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.net.Proxy;

/* compiled from: NetManger */
public class hc extends gx {
    private static hc a;
    private hl b;
    private Handler c;

    /* compiled from: NetManger */
    /* renamed from: com.amap.api.mapcore.util.hc$1 */
    class AnonymousClass1 extends hm {
        final /* synthetic */ hd a;
        final /* synthetic */ he b;
        final /* synthetic */ hc c;

        public void a() {
            try {
                this.c.a(this.c.b(this.a, false), this.b);
            } catch (ex e) {
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
                        ((hg) message.obj).b.a();
                        break;
                    case 1:
                        hg hgVar = (hg) message.obj;
                        hgVar.b.a(hgVar.a);
                        break;
                    default:
                        return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public static hc a(boolean z) {
        return a(z, 5);
    }

    private static synchronized hc a(boolean z, int i) {
        hc hcVar;
        synchronized (hc.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = hl.a(i);
                        }
                    }
                    hcVar = a;
                } else {
                    a = new hc(z, i);
                    hcVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return hcVar;
    }

    private hc(boolean z, int i) {
        if (z) {
            this.b = hl.a(i);
        }
        try {
            if (Looper.myLooper() != null) {
                this.c = new a();
            } else {
                this.c = new a(Looper.getMainLooper());
            }
        } catch (Throwable th) {
            fo.b(th, "NetManger", "NetManger1");
            th.printStackTrace();
        }
    }

    public byte[] b(hd hdVar) throws ex {
        ex e;
        try {
            hf a = a(hdVar, false);
            if (a == null) {
                return null;
            }
            return a.a;
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            fo.a().c(th, "NetManager", "makeSyncPostRequest");
            e2 = new ex("未知的错误");
        }
    }

    public byte[] d(hd hdVar) throws ex {
        ex e;
        try {
            hf b = b(hdVar, false);
            if (b == null) {
                return null;
            }
            return b.a;
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new ex("未知的错误");
        }
    }

    public hf b(hd hdVar, boolean z) throws ex {
        ex e;
        Proxy proxy = null;
        try {
            c(hdVar);
            if (hdVar.h != null) {
                proxy = hdVar.h;
            }
            return new ha(hdVar.f, hdVar.g, proxy, z).a(hdVar.c(), hdVar.a(), hdVar.b());
        } catch (ex e2) {
            throw e2;
        } catch (Throwable th) {
            th.printStackTrace();
            e2 = new ex("未知的错误");
        }
    }

    private void a(ex exVar, he heVar) {
        hg hgVar = new hg();
        hgVar.a = exVar;
        hgVar.b = heVar;
        Message obtain = Message.obtain();
        obtain.obj = hgVar;
        obtain.what = 1;
        this.c.sendMessage(obtain);
    }

    private void a(hf hfVar, he heVar) {
        heVar.a(hfVar.b, hfVar.a);
        hg hgVar = new hg();
        hgVar.b = heVar;
        Message obtain = Message.obtain();
        obtain.obj = hgVar;
        obtain.what = 0;
        this.c.sendMessage(obtain);
    }
}
