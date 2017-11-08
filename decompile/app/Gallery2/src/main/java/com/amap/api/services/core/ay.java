package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: SDKLogHandler */
public class ay implements UncaughtExceptionHandler {
    private static ay a;
    private static ExecutorService e;
    private UncaughtExceptionHandler b;
    private Context c;
    private boolean d = true;

    /* compiled from: SDKLogHandler */
    private static class a implements br {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void a() {
            try {
                bf.b(this.a);
            } catch (Throwable th) {
                ay.a(th, "LogNetListener", "onNetCompleted");
                th.printStackTrace();
            }
        }
    }

    static synchronized ExecutorService a() {
        ExecutorService executorService;
        synchronized (ay.class) {
            try {
                if (e != null) {
                    if (!e.isShutdown()) {
                        executorService = e;
                    }
                }
                e = Executors.newSingleThreadExecutor();
            } catch (Throwable th) {
                th.printStackTrace();
            }
            executorService = e;
        }
        return executorService;
    }

    public static synchronized ay a(Context context, ad adVar) throws v {
        ay ayVar;
        synchronized (ay.class) {
            if (adVar == null) {
                throw new v("sdk info is null");
            } else if (adVar.a() == null || "".equals(adVar.a())) {
                throw new v("sdk name is invalid");
            } else {
                try {
                    if (a != null) {
                        a.d = false;
                    } else {
                        a = new ay(context, adVar);
                    }
                    a.a(context, adVar, a.d);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                ayVar = a;
            }
        }
        return ayVar;
    }

    public static synchronized ay b() {
        ay ayVar;
        synchronized (ay.class) {
            ayVar = a;
        }
        return ayVar;
    }

    public static void a(Throwable th, String str, String str2) {
        if (a != null) {
            a.a(th, 1, str, str2);
        }
    }

    private ay(Context context, ad adVar) {
        this.c = context;
        bq.a(new a(context));
        c();
    }

    private void c() {
        try {
            this.b = Thread.getDefaultUncaughtExceptionHandler();
            if (this.b == null) {
                Thread.setDefaultUncaughtExceptionHandler(this);
                this.d = true;
            } else if (this.b.toString().indexOf("com.amap.api") == -1) {
                Thread.setDefaultUncaughtExceptionHandler(this);
                this.d = true;
            } else {
                this.d = false;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        if (th != null) {
            a(th, 0, null, null);
            if (this.b != null) {
                this.b.uncaughtException(thread, th);
            }
        }
    }

    public void b(Throwable th, String str, String str2) {
        if (th != null) {
            try {
                a(th, 1, str, str2);
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
        }
    }

    private void a(Throwable th, int i, String str, String str2) {
        bf.a(this.c, th, i, str, str2);
    }

    private void a(final Context context, final ad adVar, final boolean z) {
        try {
            ExecutorService a = a();
            if (a != null && !a.isShutdown()) {
                a.submit(new Runnable(this) {
                    final /* synthetic */ ay d;

                    public void run() {
                        try {
                            synchronized (Looper.getMainLooper()) {
                                new an(context).a(adVar);
                            }
                            if (z) {
                                synchronized (Looper.getMainLooper()) {
                                    aq aqVar = new aq(context);
                                    as asVar = new as();
                                    asVar.c(true);
                                    asVar.a(true);
                                    asVar.b(true);
                                    aqVar.a(asVar);
                                }
                                bf.a(this.d.c);
                            }
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            }
        } catch (RejectedExecutionException e) {
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
