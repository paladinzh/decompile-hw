package com.fyusion.sdk.viewer.internal.b.b.a;

import android.support.annotation.Nullable;
import android.util.Log;
import com.fyusion.sdk.core.util.d;
import com.fyusion.sdk.viewer.internal.b.b.a.a.c;
import com.fyusion.sdk.viewer.internal.b.e;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class g implements a {
    private final List<a> a;
    private final Map<String, File> b;
    private File c;
    private File d;
    private WeakReference<com.fyusion.sdk.viewer.internal.b.b.a.a.a> e = null;
    private final ThreadPoolExecutor f = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory(this) {
        final /* synthetic */ g a;

        {
            this.a = r1;
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "frame-disk-cache-thread");
            thread.setPriority(1);
            return thread;
        }
    });
    private Runnable g = new Runnable(this) {
        final /* synthetic */ g a;

        {
            this.a = r1;
        }

        public void run() {
            synchronized (this.a.a) {
                Iterator it = this.a.a.iterator();
                while (it.hasNext()) {
                    try {
                        long a = d.a();
                        a aVar = (a) it.next();
                        File file = aVar.b;
                        if (file.exists()) {
                            file.renameTo(new File(this.a.d, file.getName()));
                        }
                        it.remove();
                        Log.d("FramesDiskCache", "renaming " + file + " took " + d.a(a) + " now signalling to the listener");
                        if (aVar.a != null) {
                            com.fyusion.sdk.viewer.internal.b.b.a.a.a aVar2 = (com.fyusion.sdk.viewer.internal.b.b.a.a.a) this.a.e.get();
                            if (aVar2 != null) {
                                aVar2.b(aVar.a);
                            }
                        }
                    } catch (Throwable e) {
                        Log.d("FramesDiskCache", "e", e);
                    }
                }
                Log.d("FramesDiskCache", "Entries remaining: " + this.a.a.size());
            }
            for (File file2 : this.a.d.listFiles()) {
                long a2 = d.a();
                com.fyusion.sdk.common.util.a.a(file2);
                Log.d("FramesDiskCache", "deleting: " + file2 + " took: " + d.a(a2));
            }
        }
    };

    /* compiled from: Unknown */
    private class a {
        e a;
        File b;
        final /* synthetic */ g c;

        a(g gVar, e eVar, File file) {
            this.c = gVar;
            this.a = eVar;
            this.b = file;
        }
    }

    g(File file, File file2) {
        this.c = file;
        this.d = file2;
        this.b = new HashMap();
        this.a = new ArrayList();
    }

    @Nullable
    public File a(e eVar) {
        return null;
    }

    public void a() {
        synchronized (this.a) {
            for (File file : this.c.listFiles()) {
                if (!this.d.equals(file)) {
                    Log.d("FramesDiskCache", "clear: " + file.getName());
                    this.a.add(new a(this, null, file));
                }
            }
            if (this.f.getQueue().size() < 1) {
                this.f.submit(this.g);
            }
        }
    }

    public void a(com.fyusion.sdk.viewer.internal.b.b.a.a.a aVar) {
        this.e = new WeakReference(aVar);
    }

    public void a(e eVar, c cVar) {
        File file = new File(this.c, eVar.d());
        a aVar = new a(this, eVar, file);
        synchronized (this.a) {
            if (this.a.remove(aVar)) {
                Log.d("FramesDiskCache", "removing from to be deleted files list " + file.getName());
            }
            if (!this.b.containsKey(eVar.d())) {
                this.b.put(eVar.d(), file);
            }
        }
        cVar.a(file);
    }

    public void b(e eVar) {
        if (eVar != null) {
            synchronized (this.a) {
                File file = (File) this.b.remove(eVar.d());
                if (file == null) {
                    Log.d("FramesDiskCache", "File " + eVar.d() + " not present");
                    com.fyusion.sdk.viewer.internal.b.b.a.a.a aVar = (com.fyusion.sdk.viewer.internal.b.b.a.a.a) this.e.get();
                    if (aVar != null) {
                        aVar.b(eVar);
                    }
                } else {
                    this.a.add(new a(this, eVar, file));
                    if (this.f.getQueue().size() < 1) {
                        this.f.submit(this.g);
                    }
                }
            }
        }
    }
}
