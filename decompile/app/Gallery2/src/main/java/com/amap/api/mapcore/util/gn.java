package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import dalvik.system.DexFile;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.List;

/* compiled from: DexFileManager */
public class gn {

    /* compiled from: DexFileManager */
    public static class a {
        public static void a(fu fuVar, gr grVar, String str) {
            fuVar.a((Object) grVar, str);
        }

        public static gr a(fu fuVar, String str) {
            List b = fuVar.b(gr.b(str), gr.class);
            if (b != null && b.size() > 0) {
                return (gr) b.get(0);
            }
            return null;
        }

        public static List<gr> a(fu fuVar, String str, String str2) {
            return fuVar.b(gr.b(str, str2), gr.class);
        }
    }

    static String a(String str) {
        return str + ".o";
    }

    static String a(Context context, String str, String str2) {
        return fe.b(str + str2 + fc.q(context)) + ".png";
    }

    static String b(Context context, String str, String str2) {
        return a(context, a(context, str, str2));
    }

    static String a(Context context, String str) {
        return a(context) + File.separator + str;
    }

    static String a(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "pngex";
    }

    static void a(Context context, fh fhVar) {
        try {
            String b = b(context, fhVar.a(), fhVar.b());
            if (!TextUtils.isEmpty(b)) {
                File file = new File(b);
                File parentFile = file.getParentFile();
                if (file.exists()) {
                    String a = a(context, a(file.getName()));
                    DexFile loadDex = DexFile.loadDex(b, a, 0);
                    if (loadDex != null) {
                        loadDex.close();
                        a(context, file, a, fhVar);
                    }
                    return;
                }
                if (parentFile != null && parentFile.exists()) {
                    c(context, fhVar.a(), fhVar.b());
                }
            }
        } catch (Throwable th) {
            gs.a(th, "BaseClassLoader", "getInstanceByThread()");
        }
    }

    static void b(Context context, String str) {
        fu fuVar = new fu(context, gq.a());
        List a = a.a(fuVar, str, "copy");
        gs.a(a);
        if (a != null && a.size() > 1) {
            int size = a.size();
            for (int i = 1; i < size; i++) {
                c(context, fuVar, ((gr) a.get(i)).a());
            }
        }
    }

    static void a(Context context, fu fuVar, String str) {
        c(context, fuVar, str);
        c(context, fuVar, a(str));
    }

    static void c(final Context context, final String str, final String str2) {
        fo.c().submit(new Runnable() {
            public void run() {
                try {
                    fu fuVar = new fu(context, gq.a());
                    List<gr> b = fuVar.b(gr.a(str), gr.class);
                    if (b != null && b.size() > 0) {
                        for (gr grVar : b) {
                            if (!str2.equalsIgnoreCase(grVar.d())) {
                                gn.c(context, fuVar, grVar.a());
                            }
                        }
                    }
                } catch (Throwable th) {
                    gs.a(th, "FileManager", "clearUnSuitableV");
                }
            }
        });
    }

    static void a(fu fuVar, Context context, String str) {
        List<gr> a = a.a(fuVar, str, "used");
        if (a != null && a.size() > 0) {
            for (gr grVar : a) {
                gr grVar2;
                if (grVar2 != null && grVar2.c().equals(str)) {
                    a(context, fuVar, grVar2.a());
                    List b = fuVar.b(gr.a(str, grVar2.e()), gr.class);
                    if (b != null && b.size() > 0) {
                        grVar2 = (gr) b.get(0);
                        grVar2.c("errorstatus");
                        a.a(fuVar, grVar2, gr.b(grVar2.a()));
                        File file = new File(a(context, grVar2.a()));
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    static void a(Context context, fu fuVar, fh fhVar, String str, String str2) throws Throwable {
        Closeable fileInputStream;
        Closeable randomAccessFile;
        Throwable th;
        Closeable closeable;
        Closeable closeable2;
        try {
            String a = fhVar.a();
            String a2 = a(context, a, fhVar.b());
            a(context, fuVar, a2);
            fileInputStream = new FileInputStream(new File(str));
            try {
                fileInputStream.read(new byte[32]);
                File file = new File(b(context, a, fhVar.b()));
                randomAccessFile = new RandomAccessFile(file, "rw");
                try {
                    Object obj = new byte[1024];
                    int i = 0;
                    while (true) {
                        int read = fileInputStream.read(obj);
                        if (read <= 0) {
                            break;
                        }
                        if (read != 1024) {
                            Object obj2 = new byte[read];
                            System.arraycopy(obj, 0, obj2, 0, read);
                            randomAccessFile.seek((long) i);
                            randomAccessFile.write(obj2);
                        } else {
                            randomAccessFile.seek((long) i);
                            randomAccessFile.write(obj);
                        }
                        i += read;
                    }
                    gr a3 = new com.amap.api.mapcore.util.gr.a(a2, fe.a(file.getAbsolutePath()), a, fhVar.b(), str2).a("used").a();
                    a.a(fuVar, a3, gr.b(a3.a()));
                    try {
                        gs.a(fileInputStream);
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                    try {
                        gs.a(randomAccessFile);
                    } catch (Throwable th22) {
                        th22.printStackTrace();
                    }
                } catch (Throwable th3) {
                    th22 = th3;
                }
            } catch (Throwable th4) {
                th22 = th4;
                randomAccessFile = null;
                try {
                    gs.a(fileInputStream);
                } catch (Throwable th5) {
                    th5.printStackTrace();
                }
                try {
                    gs.a(randomAccessFile);
                } catch (Throwable th52) {
                    th52.printStackTrace();
                }
                throw th22;
            }
        } catch (Throwable th6) {
            th22 = th6;
            randomAccessFile = null;
            fileInputStream = null;
            gs.a(fileInputStream);
            gs.a(randomAccessFile);
            throw th22;
        }
    }

    static String a(Context context, fu fuVar, fh fhVar) {
        List b = fuVar.b(gr.b(fhVar.a(), "copy"), gr.class);
        if (b == null || b.size() == 0) {
            return null;
        }
        String e;
        gs.a(b);
        for (int i = 0; i < b.size(); i++) {
            gr grVar = (gr) b.get(i);
            if (gs.a(context, fuVar, grVar.a(), fhVar)) {
                try {
                    a(context, fuVar, fhVar, a(context, grVar.a()), grVar.e());
                    e = grVar.e();
                    break;
                } catch (Throwable th) {
                    gs.a(th, "FileManager", "loadAvailableD");
                }
            } else {
                c(context, fuVar, grVar.a());
            }
        }
        e = null;
        return e;
    }

    static void a(Context context, File file, fh fhVar) {
        File parentFile = file.getParentFile();
        if (!file.exists() && parentFile != null && parentFile.exists()) {
            c(context, fhVar.a(), fhVar.b());
        }
    }

    private static void c(Context context, fu fuVar, String str) {
        File file = new File(a(context, str));
        if (file.exists()) {
            file.delete();
        }
        fuVar.a(gr.b(str), gr.class);
    }

    private static void a(Context context, File file, String str, fh fhVar) {
        Object obj = null;
        fu fuVar = new fu(context, gq.a());
        gr a = a.a(fuVar, file.getName());
        if (a != null) {
            obj = a.e();
        }
        File file2 = new File(str);
        if (!TextUtils.isEmpty(obj) && file2.exists()) {
            String a2 = fe.a(str);
            String name = file2.getName();
            a.a(fuVar, new com.amap.api.mapcore.util.gr.a(name, a2, fhVar.a(), fhVar.b(), obj).a("useod").a(), gr.b(name));
        }
    }
}
