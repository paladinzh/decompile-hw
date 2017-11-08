package com.loc;

import android.content.Context;
import android.text.TextUtils;
import dalvik.system.DexFile;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* compiled from: DynamicClassLoader */
class bb extends ClassLoader {
    private static bb c = null;
    private static boolean h = true;
    volatile boolean a = true;
    private final Context b;
    private final Map<String, Class<?>> d = new HashMap();
    private DexFile e = null;
    private String f;
    private v g;

    private bb(Context context, ClassLoader classLoader) {
        super(classLoader);
        this.b = context;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized bb a(final Context context, v vVar, final String str, final String str2, String str3, ClassLoader classLoader) {
        synchronized (bb.class) {
            if (!TextUtils.isEmpty(str)) {
                if (!TextUtils.isEmpty(str2)) {
                    bc.a(context, vVar);
                    File file = new File(str);
                    File parentFile = file.getParentFile();
                    if (file.exists()) {
                        if (c == null) {
                            new Date().getTime();
                            try {
                                c = new bb(context.getApplicationContext(), classLoader);
                                c.g = vVar;
                                c.a(str, str2 + File.separator + ba.a(file.getName()));
                            } catch (Throwable th) {
                                aa.a(th, "DynamicClassLoader", "getInstance()");
                            }
                            new Date().getTime();
                            new Thread() {
                                public void run() {
                                    try {
                                        bb.c.a(context, str, str2);
                                    } catch (Throwable th) {
                                        aa.a(th, "DynamicClassLoader", "getInstance()");
                                    }
                                }
                            }.start();
                        }
                        bb bbVar = c;
                        return bbVar;
                    } else if (h && parentFile != null) {
                        if (parentFile.exists()) {
                            ba.b(context, vVar.a(), vVar.b());
                            h = false;
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static synchronized void a(Context context, v vVar, String str, String str2, String str3, ClassLoader classLoader, String str4) {
        synchronized (bb.class) {
            if (!TextUtils.isEmpty(str)) {
                if (!TextUtils.isEmpty(str2)) {
                    try {
                        File file = new File(str);
                        File parentFile = file.getParentFile();
                        if (file.exists()) {
                            String str5 = str2 + File.separator + ba.a(file.getName());
                            DexFile loadDex = DexFile.loadDex(str, str5, 0);
                            if (loadDex != null) {
                                loadDex.close();
                                a(new File(str5), str5, str4, new aj(context, bd.c()), vVar);
                            }
                        } else if (h && parentFile != null) {
                            if (parentFile.exists()) {
                                ba.b(context, vVar.a(), vVar.b());
                                h = false;
                            }
                        }
                    } catch (Throwable th) {
                        aa.a(th, "DynamicClassLoader", "getInstanceByThread()");
                    }
                }
            }
        }
    }

    private void a(Context context, String str, String str2) {
        new Date().getTime();
        try {
            aj ajVar = new aj(context, bd.c());
            File file = new File(str);
            a(ajVar, file.getName());
            if (!a(ajVar, this.g, file.getAbsolutePath())) {
                this.a = false;
                ba.b(this.b, ajVar, file.getName());
                String a = ba.a(this.b, ajVar, this.g);
                if (!TextUtils.isEmpty(a)) {
                    this.f = a;
                    a(this.b, this.g, str, str2, null, this.b.getClassLoader(), a);
                }
            }
            if (file.exists()) {
                String str3 = str2 + File.separator + ba.a(file.getName());
                File file2 = new File(str3);
                if (file2.exists()) {
                    if (!a(ajVar, ba.a(file.getName()), this.f)) {
                        a(str, str2 + File.separator + ba.a(file.getName()));
                        a(file2, str3, this.f, ajVar, this.g);
                    }
                }
                new Date().getTime();
            }
        } catch (Throwable th) {
            aa.a(th, "DynamicClassLoader", "verifyDynamicSDK()");
        }
    }

    private void a(aj ajVar, String str) {
        bf a = a.a(ajVar, str);
        if (a != null) {
            this.f = a.e();
        }
    }

    private static void a(File file, String str, String str2, aj ajVar, v vVar) {
        if (!TextUtils.isEmpty(str2)) {
            Object a = s.a(str);
            if (!TextUtils.isEmpty(a)) {
                String name = file.getName();
                a.a(ajVar, new a(name, a, vVar.a(), vVar.b(), str2).a("useodex").a(), be.b(name));
            }
        }
    }

    private void a(String str, String str2) {
        try {
            this.d.clear();
            c();
            this.e = DexFile.loadDex(str, str2, 0);
        } catch (Throwable e) {
            aa.a(e, "DynamicClassLoader", "loadDexFile()");
        } catch (Throwable e2) {
            aa.a(e2, "DynamicClassLoader", "loadDexFile()");
        }
    }

    private boolean a(aj ajVar, v vVar, String str) {
        return ba.a(ajVar, ba.b(vVar.a(), vVar.b()), str, vVar);
    }

    private boolean a(aj ajVar, String str, String str2) {
        String a = ba.a(this.b, str);
        if (ba.a(ajVar, str, a, this.g)) {
            return true;
        }
        if (a.a(ajVar, str) != null) {
            return false;
        }
        if (!TextUtils.isEmpty(this.f)) {
            a.a(ajVar, new a(str, s.a(a), this.g.a(), this.g.b(), str2).a("useodex").a(), be.b(str));
        }
        return true;
    }

    private void c() {
        if (this.e != null) {
            try {
                this.e.close();
            } catch (Throwable e) {
                aa.a(e, "DynamicClassLoader", "releaseDexFile()");
            } catch (Throwable e2) {
                aa.a(e2, "DynamicClassLoader", "releaseDexFile()");
            }
        }
    }

    boolean a() {
        return this.e != null;
    }

    protected Class<?> findClass(String str) throws ClassNotFoundException {
        try {
            if (this.e != null) {
                Class<?> cls = (Class) this.d.get(str);
                if (cls != null) {
                    return cls;
                }
                cls = this.e.loadClass(str, this);
                this.d.put(str, cls);
                if (cls != null) {
                    return cls;
                }
                throw new ClassNotFoundException(str);
            }
            throw new ClassNotFoundException(str);
        } catch (Throwable th) {
            aa.a(th, "DynamicClassLoader", "findClass()");
            ClassNotFoundException classNotFoundException = new ClassNotFoundException(str);
        }
    }
}
