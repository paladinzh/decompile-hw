package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: SDKLogHandler */
public class fo extends fl implements UncaughtExceptionHandler {
    private static ExecutorService e;
    private Context d;

    /* compiled from: SDKLogHandler */
    private static class a implements hb {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void a() {
            try {
                fm.b(this.a);
            } catch (Throwable th) {
                fl.a(th, "LogNetListener", "onNetCompleted");
            }
        }
    }

    public static synchronized fo a(Context context, fh fhVar) throws ex {
        fo foVar;
        synchronized (fo.class) {
            if (fhVar == null) {
                throw new ex("sdk info is null");
            } else if (fhVar.a() == null || "".equals(fhVar.a())) {
                throw new ex("sdk name is invalid");
            } else {
                try {
                    if (fl.a != null) {
                        fl.a.c = false;
                    } else {
                        fl.a = new fo(context, fhVar);
                    }
                    fl.a.a(context, fhVar, fl.a.c);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                foVar = (fo) fl.a;
            }
        }
        return foVar;
    }

    public static synchronized fo a() {
        fo foVar;
        synchronized (fo.class) {
            foVar = (fo) fl.a;
        }
        return foVar;
    }

    public static void b(Throwable th, String str, String str2) {
        if (fl.a != null) {
            fl.a.a(th, 1, str, str2);
        }
    }

    public static synchronized void b() {
        synchronized (fo.class) {
            try {
                if (e != null) {
                    e.shutdown();
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            try {
                if (fl.a != null) {
                    if (Thread.getDefaultUncaughtExceptionHandler() == fl.a && fl.a.b != null) {
                        Thread.setDefaultUncaughtExceptionHandler(fl.a.b);
                    }
                }
                fl.a = null;
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        if (th != null) {
            a(th, 0, null, null);
            if (this.b != null) {
                try {
                    Thread.setDefaultUncaughtExceptionHandler(this.b);
                } catch (Throwable th2) {
                }
                this.b.uncaughtException(thread, th);
            }
        }
    }

    protected void a(Throwable th, int i, String str, String str2) {
        fm.a(this.d, th, i, str, str2);
    }

    protected void a(final Context context, final fh fhVar, final boolean z) {
        try {
            ExecutorService c = c();
            if (c != null && !c.isShutdown()) {
                c.submit(new Runnable(this) {
                    final /* synthetic */ fo d;

                    public void run() {
                        try {
                            synchronized (Looper.getMainLooper()) {
                                new ge(context, true).a(fhVar);
                            }
                            if (z) {
                                synchronized (Looper.getMainLooper()) {
                                    gf gfVar = new gf(context);
                                    gg ggVar = new gg();
                                    ggVar.c(true);
                                    ggVar.a(true);
                                    ggVar.b(true);
                                    gfVar.a(ggVar);
                                }
                                fm.a(this.d.d);
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

    private fo(Context context, fh fhVar) {
        this.d = context;
        ha.a(new a(context));
        d();
    }

    private void d() {
        try {
            this.b = Thread.getDefaultUncaughtExceptionHandler();
            if (this.b != null) {
                String obj = this.b.toString();
                if (obj.indexOf("com.amap.api") == -1) {
                    if (obj.indexOf("com.amap.loc") == -1) {
                        Thread.setDefaultUncaughtExceptionHandler(this);
                        this.c = true;
                        return;
                    }
                }
                this.c = false;
                return;
            }
            Thread.setDefaultUncaughtExceptionHandler(this);
            this.c = true;
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void c(Throwable th, String str, String str2) {
        if (th != null) {
            try {
                a(th, 1, str, str2);
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
        }
    }

    public static synchronized ExecutorService c() {
        ExecutorService executorService;
        synchronized (fo.class) {
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
}
