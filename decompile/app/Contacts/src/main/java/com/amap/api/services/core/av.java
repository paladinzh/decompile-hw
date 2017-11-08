package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: SDKLogHandler */
public class av extends ay implements UncaughtExceptionHandler {
    private static ExecutorService e;
    private Context d;

    /* compiled from: SDKLogHandler */
    private static class a implements ch {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void a() {
            try {
                bd.b(this.a);
            } catch (Throwable th) {
                av.a(th, "LogNetListener", "onNetCompleted");
                th.printStackTrace();
            }
        }
    }

    static synchronized ExecutorService a() {
        ExecutorService executorService;
        synchronized (av.class) {
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

    public static synchronized av a(Context context, ar arVar) throws ai {
        av avVar;
        synchronized (av.class) {
            if (arVar == null) {
                throw new ai("sdk info is null");
            } else if (arVar.a() == null || "".equals(arVar.a())) {
                throw new ai("sdk name is invalid");
            } else {
                try {
                    if (ay.a != null) {
                        ay.a.c = false;
                    } else {
                        ay.a = new av(context, arVar);
                    }
                    ay.a.a(context, arVar, ay.a.c);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                avVar = (av) ay.a;
            }
        }
        return avVar;
    }

    public static synchronized av b() {
        av avVar;
        synchronized (av.class) {
            avVar = (av) ay.a;
        }
        return avVar;
    }

    public static void a(Throwable th, String str, String str2) {
        if (ay.a != null) {
            ay.a.a(th, 1, str, str2);
        }
    }

    private av(Context context, ar arVar) {
        this.d = context;
        cg.a(new a(context));
        c();
    }

    private void c() {
        try {
            this.b = Thread.getDefaultUncaughtExceptionHandler();
            if (this.b == null) {
                Thread.setDefaultUncaughtExceptionHandler(this);
                this.c = true;
            } else if (this.b.toString().indexOf("com.amap.api") == -1) {
                Thread.setDefaultUncaughtExceptionHandler(this);
                this.c = true;
            } else {
                this.c = false;
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

    protected void a(Throwable th, int i, String str, String str2) {
        bd.a(this.d, th, i, str, str2);
    }

    protected void a(final Context context, final ar arVar, final boolean z) {
        try {
            ExecutorService a = a();
            if (a != null && !a.isShutdown()) {
                a.submit(new Runnable(this) {
                    final /* synthetic */ av d;

                    public void run() {
                        try {
                            synchronized (Looper.getMainLooper()) {
                                new bt(context, true).a(arVar);
                            }
                            if (z) {
                                synchronized (Looper.getMainLooper()) {
                                    bv bvVar = new bv(context);
                                    bx bxVar = new bx();
                                    bxVar.c(true);
                                    bxVar.a(true);
                                    bxVar.b(true);
                                    bvVar.a(bxVar);
                                }
                                bd.a(this.d.d);
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
