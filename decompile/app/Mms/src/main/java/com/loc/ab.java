package com.loc;

import android.content.Context;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: SDKLogHandler */
public class ab extends aa implements UncaughtExceptionHandler {
    private static ExecutorService e;
    private Context d;

    /* compiled from: SDKLogHandler */
    private static class a implements br {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void a() {
            try {
                af.b(this.a);
            } catch (Throwable th) {
                aa.a(th, "LogNetListener", "onNetCompleted");
            }
        }
    }

    private ab(Context context, v vVar) {
        this.d = context;
        bq.a(new a(context));
        c();
    }

    public static synchronized ab a(Context context, v vVar) throws l {
        ab abVar;
        synchronized (ab.class) {
            if (vVar == null) {
                throw new l("sdk info is null");
            } else if (vVar.a() == null || "".equals(vVar.a())) {
                throw new l("sdk name is invalid");
            } else {
                try {
                    if (aa.a != null) {
                        aa.a.c = false;
                    } else {
                        aa.a = new ab(context, vVar);
                    }
                    aa.a.a(context, vVar, aa.a.c);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                abVar = (ab) aa.a;
            }
        }
        return abVar;
    }

    public static synchronized void a() {
        synchronized (ab.class) {
            try {
                if (e != null) {
                    e.shutdown();
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            try {
                if (aa.a != null) {
                    if (Thread.getDefaultUncaughtExceptionHandler() == aa.a && aa.a.b != null) {
                        Thread.setDefaultUncaughtExceptionHandler(aa.a.b);
                    }
                }
                aa.a = null;
            } catch (Throwable th2) {
                th2.printStackTrace();
            }
        }
    }

    public static void a(Throwable th, String str, String str2) {
        if (aa.a != null) {
            aa.a.a(th, 1, str, str2);
        }
    }

    static synchronized ExecutorService b() {
        ExecutorService executorService;
        synchronized (ab.class) {
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

    public static void b(v vVar, String str) {
        if (aa.a != null) {
            aa.a.a(vVar, str);
        }
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

    protected void a(final Context context, final v vVar, final boolean z) {
        try {
            ExecutorService b = b();
            if (b != null && !b.isShutdown()) {
                b.submit(new Runnable(this) {
                    final /* synthetic */ ab d;

                    public void run() {
                        try {
                            synchronized (Looper.getMainLooper()) {
                                new aq(context, true).a(vVar);
                            }
                            if (z) {
                                synchronized (Looper.getMainLooper()) {
                                    as asVar = new as(context);
                                    au auVar = new au();
                                    auVar.c(true);
                                    auVar.a(true);
                                    auVar.b(true);
                                    asVar.a(auVar);
                                }
                                af.a(this.d.d);
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

    protected void a(v vVar, String str) {
        af.a(this.d, vVar, str);
    }

    protected void a(Throwable th, int i, String str, String str2) {
        af.a(this.d, th, i, str, str2);
    }

    public void uncaughtException(Thread thread, Throwable th) {
        if (th != null) {
            a(th, 0, null, null);
            if (this.b != null) {
                this.b.uncaughtException(thread, th);
            }
        }
    }
}
