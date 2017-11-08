package com.fyusion.sdk.viewer.internal.b.b;

import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public final class l extends ThreadPoolExecutor {
    private static final long b = TimeUnit.SECONDS.toMillis(10);
    private final boolean a;

    /* compiled from: Unknown */
    private static final class a implements ThreadFactory {
        private final String a;
        private final b b;
        private final boolean c;
        private int d;

        a(String str, b bVar, boolean z) {
            this.a = str;
            this.b = bVar;
            this.c = z;
        }

        public synchronized Thread newThread(@NonNull Runnable runnable) {
            Thread anonymousClass1;
            anonymousClass1 = new Thread(this, runnable, "viewer-" + this.a + "-thread-" + this.d) {
                final /* synthetic */ a a;

                public void run() {
                    Process.setThreadPriority(9);
                    if (this.a.c) {
                        StrictMode.setThreadPolicy(new Builder().detectNetwork().penaltyDeath().build());
                    }
                    try {
                        super.run();
                    } catch (Throwable th) {
                        this.a.b.a(th);
                    }
                }
            };
            this.d++;
            return anonymousClass1;
        }
    }

    /* compiled from: Unknown */
    public enum b {
        IGNORE,
        LOG {
            protected void a(Throwable th) {
                if (th != null && Log.isLoggable("FyuseExecutor", 6)) {
                    Log.e("FyuseExecutor", "UriRequest threw uncaught throwable", th);
                }
            }
        },
        THROW {
            protected void a(Throwable th) {
                super.a(th);
                if (th != null) {
                    throw new RuntimeException("UriRequest threw uncaught throwable", th);
                }
            }
        };
        
        public static final b d = null;

        static {
            d = LOG;
        }

        protected void a(Throwable th) {
        }
    }

    l(int i, int i2, long j, String str, b bVar, boolean z, boolean z2) {
        this(i, i2, j, str, bVar, z, z2, new PriorityBlockingQueue());
    }

    l(int i, int i2, long j, String str, b bVar, boolean z, boolean z2, BlockingQueue<Runnable> blockingQueue) {
        super(i, i2, j, TimeUnit.MILLISECONDS, blockingQueue, new a(str, bVar, z));
        this.a = z2;
    }

    l(int i, String str, b bVar, boolean z, boolean z2) {
        this(i, i, 0, str, bVar, z, z2);
    }

    public static l a() {
        return a(1, "disk-cache", b.d);
    }

    public static l a(int i, String str, b bVar) {
        return new l(i, str, bVar, true, false);
    }

    private <T> Future<T> a(Future<T> future) {
        if (this.a) {
            Object obj = null;
            while (!future.isDone()) {
                try {
                    future.get();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e2) {
                    obj = 1;
                } catch (Throwable th) {
                    if (obj != null) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            if (obj != null) {
                Thread.currentThread().interrupt();
            }
        }
        return future;
    }

    public static l b() {
        return b(e(), "source", b.d);
    }

    public static l b(int i, String str, b bVar) {
        return new l(i, str, bVar, false, false);
    }

    public static l c() {
        return new l(1, "decode", b.d, true, false);
    }

    public static l d() {
        return new l(Math.max(1, e()), "encode", b.d, true, false);
    }

    public static int e() {
        File[] listFiles;
        try {
            File file = new File("/sys/devices/system/cpu/");
            final Pattern compile = Pattern.compile("cpu[0-9]+");
            listFiles = file.listFiles(new FilenameFilter() {
                public boolean accept(File file, String str) {
                    return compile.matcher(str).matches();
                }
            });
        } catch (Throwable th) {
            if (Log.isLoggable("FyuseExecutor", 6)) {
                Log.e("FyuseExecutor", "Failed to calculate accurate cpu count", th);
            }
            listFiles = null;
        }
        return Math.min(4, Math.max(Math.max(1, Runtime.getRuntime().availableProcessors()), listFiles == null ? 0 : listFiles.length));
    }

    public void execute(Runnable runnable) {
        if (this.a) {
            runnable.run();
        } else {
            super.execute(runnable);
        }
    }

    @NonNull
    public Future<?> submit(Runnable runnable) {
        return a(super.submit(runnable));
    }

    @NonNull
    public <T> Future<T> submit(Runnable runnable, T t) {
        return a(super.submit(runnable, t));
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return a(super.submit(callable));
    }
}
