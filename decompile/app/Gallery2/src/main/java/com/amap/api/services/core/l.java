package com.amap.api.services.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/* compiled from: ManifestConfig */
public class l {
    public static ad a;
    private static l b;
    private static Context c;
    private a d;
    private HandlerThread e = new m(this, "manifestThread");

    /* compiled from: ManifestConfig */
    class a extends Handler {
        String a = "handleMessage";
        final /* synthetic */ l b;

        public a(l lVar, Looper looper) {
            this.b = lVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message != null) {
                switch (message.what) {
                    case 3:
                        try {
                            o oVar = (o) message.obj;
                            if (oVar == null) {
                                oVar = new o(false, false);
                            }
                            ay.a(l.c, c.a(oVar.a()));
                            l.a = c.a(oVar.a());
                            break;
                        } catch (Throwable th) {
                            d.a(th, "ManifestConfig", this.a);
                            break;
                        }
                }
            }
        }
    }

    private l(Context context) {
        c = context;
        a = c.a(false);
        try {
            this.e.start();
            this.d = new a(this, Looper.getMainLooper());
        } catch (Throwable th) {
            d.a(th, "ManifestConfig", "ManifestConfig");
        }
    }

    public static l a(Context context) {
        if (b == null) {
            b = new l(context);
        }
        return b;
    }
}
