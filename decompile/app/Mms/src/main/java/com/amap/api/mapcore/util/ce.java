package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: SDKLogHandler */
public class ce extends cb implements UncaughtExceptionHandler {
    private static ExecutorService e;
    private Context d;

    /* compiled from: SDKLogHandler */
    private static class a implements dh {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void a() {
            try {
                cc.b(this.a);
            } catch (Throwable th) {
                cb.a(th, "LogNetListener", "onNetCompleted");
            }
        }
    }

    public static synchronized ce a(Context context, bv bvVar) throws bk {
        ce ceVar;
        synchronized (ce.class) {
            if (bvVar == null) {
                throw new bk("sdk info is null");
            } else if (bvVar.a() == null || "".equals(bvVar.a())) {
                throw new bk("sdk name is invalid");
            } else {
                try {
                    if (cb.a != null) {
                        cb.a.c = false;
                    } else {
                        cb.a = new ce(context, bvVar);
                    }
                    cb.a.a(context, bvVar, cb.a.c);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                ceVar = (ce) cb.a;
            }
        }
        return ceVar;
    }

    public static synchronized ce a() {
        ce ceVar;
        synchronized (ce.class) {
            ceVar = (ce) cb.a;
        }
        return ceVar;
    }

    public static void a(Throwable th, String str, String str2) {
        if (cb.a != null) {
            cb.a.a(th, 1, str, str2);
        }
    }

    public static synchronized void b() {
        synchronized (ce.class) {
            try {
                if (e != null) {
                    e.shutdown();
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            try {
                if (cb.a != null) {
                    if (Thread.getDefaultUncaughtExceptionHandler() == cb.a && cb.a.b != null) {
                        Thread.setDefaultUncaughtExceptionHandler(cb.a.b);
                    }
                }
                cb.a = null;
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
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

    protected void a(Throwable th, int i, String str, String str2) {
        cc.a(this.d, th, i, str, str2);
    }

    protected void a(final Context context, final bv bvVar, final boolean z) {
        try {
            ExecutorService c = c();
            if (c != null && !c.isShutdown()) {
                c.submit(new Runnable(this) {
                    final /* synthetic */ ce d;

                    public void run() {
                        try {
                            synchronized (Looper.getMainLooper()) {
                                new cu(context, true).a(bvVar);
                            }
                            if (z) {
                                synchronized (Looper.getMainLooper()) {
                                    cv cvVar = new cv(context);
                                    cw cwVar = new cw();
                                    cwVar.c(true);
                                    cwVar.a(true);
                                    cwVar.b(true);
                                    cvVar.a(cwVar);
                                }
                                cc.a(this.d.d);
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

    private ce(Context context, bv bvVar) {
        this.d = context;
        df.a(new a(context));
        d();
    }

    private void d() {
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

    public void b(Throwable th, String str, String str2) {
        if (th != null) {
            try {
                a(th, 1, str, str2);
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
        }
    }

    static synchronized ExecutorService c() {
        ExecutorService executorService;
        synchronized (ce.class) {
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
