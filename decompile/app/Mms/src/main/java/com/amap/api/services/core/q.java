package com.amap.api.services.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/* compiled from: ManifestConfig */
public class q {
    public static ar a;
    private static q b;
    private static Context c;
    private a d;
    private HandlerThread e = new r(this, "manifestThread");

    /* compiled from: ManifestConfig */
    class a extends Handler {
        String a = "handleMessage";
        final /* synthetic */ q b;

        public a(q qVar, Looper looper) {
            this.b = qVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message != null) {
                switch (message.what) {
                    case 3:
                        try {
                            s sVar = (s) message.obj;
                            if (sVar == null) {
                                sVar = new s(false, false);
                            }
                            av.a(q.c, h.a(sVar.a()));
                            q.a = h.a(sVar.a());
                            break;
                        } catch (Throwable th) {
                            i.a(th, "ManifestConfig", this.a);
                            break;
                        }
                }
            }
        }
    }

    private q(Context context) {
        c = context;
        a = h.a(false);
        try {
            this.e.start();
            this.d = new a(this, Looper.getMainLooper());
        } catch (Throwable th) {
            i.a(th, "ManifestConfig", "ManifestConfig");
        }
    }

    public static q a(Context context) {
        if (b == null) {
            b = new q(context);
        }
        return b;
    }
}
