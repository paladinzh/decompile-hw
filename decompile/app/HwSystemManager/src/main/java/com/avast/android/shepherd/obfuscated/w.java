package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import android.content.Intent;
import com.avast.android.shepherd.Shepherd;
import com.avast.android.shepherd.obfuscated.v.a;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
class w extends Thread {
    private final Semaphore a = new Semaphore(0);
    private final Context b;
    private final ab c;
    private final AtomicInteger d = new AtomicInteger(0);
    private WeakReference<a> e = null;

    public w(Context context) {
        this.b = context.getApplicationContext();
        this.c = ab.a(context);
    }

    private boolean a() {
        Throwable e;
        x.c("Downloading new config...");
        MessageLite a = new ad(this.b, this.c.b()).a();
        boolean z;
        try {
            al alVar = al.PRODUCTION;
            if (Shepherd.getConfig().getCommonConfig().useSandboxShepherd()) {
                alVar = al.SANDBOX;
            } else if (Shepherd.getConfig().getCommonConfig().useStagingShepherd()) {
                alVar = al.STAGE;
            }
            this.c.b(System.currentTimeMillis());
            af.a a2 = af.a.a(ap.a(this.b, a, ak.SHEPHERD, ao.NOTHING, null, alVar).h());
            x.c("ConfigDownloaderThread: response hasTtl= " + a2.c() + " " + "getTtl=" + a2.d());
            if (a2.c()) {
                this.c.a(System.currentTimeMillis() + (((long) a2.d()) * 1000));
                z = true;
            } else {
                z = false;
            }
            try {
                x.c("ConfigDownloaderThread: response hasContent= " + a2.e());
                if (a2.e()) {
                    synchronized (this) {
                        a aVar = (a) this.e.get();
                        if (aVar != null) {
                            aVar.onNewConfigDownloaded(this.b, a2.f().toByteArray());
                        }
                    }
                }
                this.c.a(false);
                if (!z) {
                    this.c.a(System.currentTimeMillis() + 28800000);
                }
                x.c("ConfigDownloaderThread: Config downloaded");
                return true;
            } catch (InvalidProtocolBufferException e2) {
                e = e2;
            } catch (Throwable th) {
                e = th;
                this.c.a(true);
                x.b("ConfigDownloaderThread: " + e.getMessage(), e);
                return z;
            }
        } catch (InvalidProtocolBufferException e3) {
            e = e3;
            z = false;
            this.c.a(true);
            x.b("ConfigDownloaderThread: " + e.getMessage(), e);
            if (z) {
            }
        } catch (Throwable th2) {
            e = th2;
            z = false;
            this.c.a(true);
            x.b("ConfigDownloaderThread: " + e.getMessage(), e);
            if (z) {
            }
        }
    }

    synchronized void a(a aVar) {
        this.e = new WeakReference(aVar);
    }

    void a(boolean z) {
        if (z) {
            this.d.incrementAndGet();
        }
        this.a.release();
    }

    public void run() {
        while (true) {
            try {
                this.a.acquire();
            } catch (InterruptedException e) {
            }
            if (this.d.get() <= 0) {
                Object obj = null;
            } else {
                this.d.decrementAndGet();
                int i = 1;
            }
            if (!(((this.c.a() <= System.currentTimeMillis() ? 1 : null) == null && r0 == null) || a() || r0 != null)) {
                this.c.a(System.currentTimeMillis() + 28800000);
            }
            x.c("ConfigDownloaderThread: Going to inform the broadcast receiver now");
            this.b.sendBroadcast(new Intent("com.avast.android.shepherd.NEXT_UPDATE_TIME_SET"));
        }
    }
}
