package com.amap.api.mapcore.util;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import com.amap.api.mapcore.util.gn.a;
import dalvik.system.DexFile;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/* compiled from: DynamicClassLoader */
class go extends gk {
    private PublicKey g = null;

    protected java.lang.Class<?> findClass(java.lang.String r6) throws java.lang.ClassNotFoundException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:49:0x005f
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r5 = this;
        r1 = 0;
        r0 = r5.c;	 Catch:{ Throwable -> 0x002b }
        if (r0 == 0) goto L_0x0025;
    L_0x0005:
        r2 = r5.b;	 Catch:{ Throwable -> 0x003e }
        monitor-enter(r2);	 Catch:{ Throwable -> 0x003e }
        r0 = r5.b;	 Catch:{ all -> 0x003b }
        r0 = r0.get(r6);	 Catch:{ all -> 0x003b }
        r0 = (java.lang.Class) r0;	 Catch:{ all -> 0x003b }
        monitor-exit(r2);	 Catch:{ all -> 0x0064 }
    L_0x0011:
        if (r0 != 0) goto L_0x004a;
    L_0x0013:
        r0 = r5.c;	 Catch:{ Throwable -> 0x002b }
        r1 = r0.loadClass(r6, r5);	 Catch:{ Throwable -> 0x002b }
        if (r1 == 0) goto L_0x004b;
    L_0x001b:
        r2 = r5.b;	 Catch:{ Throwable -> 0x0054 }
        monitor-enter(r2);	 Catch:{ Throwable -> 0x0054 }
        r0 = r5.b;	 Catch:{ all -> 0x0051 }
        r0.put(r6, r1);	 Catch:{ all -> 0x0051 }
        monitor-exit(r2);	 Catch:{ all -> 0x0051 }
    L_0x0024:
        return r1;
    L_0x0025:
        r0 = new java.lang.ClassNotFoundException;	 Catch:{ Throwable -> 0x002b }
        r0.<init>(r6);	 Catch:{ Throwable -> 0x002b }
        throw r0;	 Catch:{ Throwable -> 0x002b }
    L_0x002b:
        r0 = move-exception;
        r1 = "dLoader";
        r2 = "findCl";
        com.amap.api.mapcore.util.gs.a(r0, r1, r2);
        r0 = new java.lang.ClassNotFoundException;
        r0.<init>(r6);
        throw r0;
    L_0x003b:
        r0 = move-exception;
    L_0x003c:
        monitor-exit(r2);	 Catch:{ all -> 0x003b }
        throw r0;	 Catch:{ Throwable -> 0x003e }
    L_0x003e:
        r0 = move-exception;
    L_0x003f:
        r2 = "dLoader";	 Catch:{ Throwable -> 0x002b }
        r3 = "findCl";	 Catch:{ Throwable -> 0x002b }
        com.amap.api.mapcore.util.gs.a(r0, r2, r3);	 Catch:{ Throwable -> 0x002b }
        r0 = r1;	 Catch:{ Throwable -> 0x002b }
        goto L_0x0011;	 Catch:{ Throwable -> 0x002b }
    L_0x004a:
        return r0;	 Catch:{ Throwable -> 0x002b }
    L_0x004b:
        r0 = new java.lang.ClassNotFoundException;	 Catch:{ Throwable -> 0x002b }
        r0.<init>(r6);	 Catch:{ Throwable -> 0x002b }
        throw r0;	 Catch:{ Throwable -> 0x002b }
    L_0x0051:
        r0 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0051 }
        throw r0;	 Catch:{ Throwable -> 0x0054 }
    L_0x0054:
        r0 = move-exception;
        r2 = "dLoader";	 Catch:{ Throwable -> 0x002b }
        r3 = "findCl";	 Catch:{ Throwable -> 0x002b }
        com.amap.api.mapcore.util.gs.a(r0, r2, r3);	 Catch:{ Throwable -> 0x002b }
        goto L_0x0024;
        r1 = move-exception;
        r4 = r1;
        r1 = r0;
        r0 = r4;
        goto L_0x003f;
    L_0x0064:
        r1 = move-exception;
        r4 = r1;
        r1 = r0;
        r0 = r4;
        goto L_0x003c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.amap.api.mapcore.util.go.findClass(java.lang.String):java.lang.Class<?>");
    }

    go(Context context, fh fhVar, boolean z) throws Exception {
        super(context, fhVar, z);
        String b = gn.b(context, fhVar.a(), fhVar.b());
        String a = gn.a(context);
        b(b, a);
        File file = new File(b);
        if (z) {
            a(b, a + File.separator + gn.a(file.getName()));
            b(context, b, a);
        }
    }

    void a(String str, String str2) throws Exception {
        try {
            if (this.c == null) {
                b();
                this.c = DexFile.loadDex(str, str2, 0);
            }
        } catch (Throwable th) {
            gs.a(th, "dLoader", "loadFile");
            Exception exception = new Exception("load file fail");
        }
    }

    private void c() {
        if (this.g == null) {
            this.g = gs.a();
        }
    }

    private void a(JarFile jarFile, JarEntry jarEntry) throws IOException {
        try {
            Closeable inputStream = jarFile.getInputStream(jarEntry);
            do {
            } while (inputStream.read(new byte[FragmentTransaction.TRANSIT_EXIT_MASK]) > 0);
            try {
                gs.a(inputStream);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
    }

    private boolean a(File file, Certificate[] certificateArr) {
        try {
            if (certificateArr.length > 0) {
                int length = certificateArr.length - 1;
                if (length >= 0) {
                    certificateArr[length].verify(this.g);
                    return true;
                }
            }
        } catch (Throwable e) {
            gs.a(e, "DyLoader", "check");
        }
        return false;
    }

    private boolean a(File file) {
        JarFile jarFile;
        Throwable th;
        try {
            c();
            jarFile = new JarFile(file);
            try {
                JarEntry jarEntry = jarFile.getJarEntry("classes.dex");
                if (jarEntry != null) {
                    a(jarFile, jarEntry);
                    Certificate[] certificates = jarEntry.getCertificates();
                    if (certificates != null) {
                        boolean a = a(file, certificates);
                        if (jarFile != null) {
                            try {
                                jarFile.close();
                            } catch (Throwable th2) {
                            }
                        }
                        return a;
                    }
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (Throwable th3) {
                        }
                    }
                    return false;
                }
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (Throwable th4) {
                    }
                }
                return false;
            } catch (Throwable th5) {
                th = th5;
                try {
                    gs.a(th, "DyLoader", "verify");
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (Throwable th6) {
                        }
                    }
                    return false;
                } catch (Throwable th7) {
                    th = th7;
                    if (jarFile != null) {
                        try {
                            jarFile.close();
                        } catch (Throwable th8) {
                        }
                    }
                    throw th;
                }
            }
        } catch (Throwable th9) {
            th = th9;
            jarFile = null;
            if (jarFile != null) {
                jarFile.close();
            }
            throw th;
        }
    }

    private boolean a(fu fuVar, fh fhVar, String str) {
        if (a(new File(str))) {
            return gs.a(fuVar, gn.a(this.a, fhVar.a(), fhVar.b()), str, fhVar);
        }
        return false;
    }

    private boolean a(fu fuVar, String str, String str2) {
        String a = gn.a(this.a, str);
        if (gs.a(fuVar, str, a, this.e)) {
            return true;
        }
        if (a.a(fuVar, str) != null) {
            return false;
        }
        if (!TextUtils.isEmpty(this.f)) {
            a.a(fuVar, new gr.a(str, fe.a(a), this.e.a(), this.e.b(), str2).a("useod").a(), gr.b(str));
        }
        return true;
    }

    private void b(String str, String str2) throws Exception {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            throw new Exception("dexPath or dexOutputDir is null.");
        }
    }

    private void b(final Context context, final String str, final String str2) {
        fo.c().submit(new Runnable(this) {
            final /* synthetic */ go d;

            public void run() {
                try {
                    this.d.a(context, str, str2);
                } catch (Throwable th) {
                    gs.a(th, "dLoader", "run()");
                }
            }
        });
    }

    private void a(fu fuVar, File file) {
        gr a = a.a(fuVar, file.getName());
        if (a != null) {
            this.f = a.e();
        }
    }

    private void b(fu fuVar, File file) {
        this.d = false;
        gn.a(this.a, fuVar, file.getName());
        Object a = gn.a(this.a, fuVar, this.e);
        if (!TextUtils.isEmpty(a)) {
            this.f = a;
            gn.a(this.a, this.e);
        }
    }

    void a(Context context, String str, String str2) throws Exception {
        new Date().getTime();
        try {
            fu fuVar = new fu(context, gq.a());
            File file = new File(str);
            a(fuVar, file);
            if (!a(fuVar, this.e, file.getAbsolutePath())) {
                b(fuVar, file);
            }
            if (file.exists()) {
                if (new File(str2 + File.separator + gn.a(file.getName())).exists()) {
                    if (!a(fuVar, gn.a(file.getName()), this.f)) {
                        gn.a(this.a, this.e);
                    }
                }
                new Date().getTime();
            }
        } catch (Throwable th) {
            gs.a(th, "dLoader", "verifyD()");
        }
    }
}
