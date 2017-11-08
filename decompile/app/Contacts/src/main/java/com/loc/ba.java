package com.loc;

import android.content.Context;
import com.google.android.gms.location.places.Place;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/* compiled from: DexFileManager */
class ba {

    /* compiled from: DexFileManager */
    static class a {
        a() {
        }

        static bf a(aj ajVar, String str) {
            List c = ajVar.c(be.b(str), new be());
            return (c != null && c.size() > 0) ? (bf) c.get(0) : null;
        }

        static void a(aj ajVar, bf bfVar, String str) {
            ak beVar = new be();
            beVar.a(bfVar);
            ajVar.a(beVar, str);
        }
    }

    ba() {
    }

    static String a(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "dex";
    }

    static String a(Context context, aj ajVar, v vVar) {
        List c = ajVar.c(be.b(vVar.a(), "copy"), new be());
        if (c == null || c.size() == 0) {
            return null;
        }
        String e;
        a(c);
        for (int i = 0; i < c.size(); i++) {
            bf bfVar = (bf) c.get(i);
            if (a(context, ajVar, bfVar.a(), vVar)) {
                try {
                    a(context, ajVar, vVar, new a(b(vVar.a(), vVar.b()), bfVar.b(), vVar.a(), vVar.b(), bfVar.e()).a("usedex").a(), a(context, bfVar.a()));
                    e = bfVar.e();
                    break;
                } catch (Throwable th) {
                    aa.a(th, "DexFileManager", "loadAvailableDynamicSDKFile");
                }
            } else {
                a(context, ajVar, bfVar.a());
            }
        }
        e = null;
        return e;
    }

    static String a(Context context, String str) {
        return a(context) + File.separator + str;
    }

    static String a(Context context, String str, String str2) {
        return a(context, b(str, str2));
    }

    static String a(String str) {
        return str + ".dex";
    }

    static void a(Context context, aj ajVar, v vVar, bf bfVar, String str) throws Throwable {
        FileOutputStream fileOutputStream;
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        FileOutputStream fileOutputStream2 = null;
        InputStream fileInputStream;
        try {
            String a = vVar.a();
            b(context, ajVar, bfVar.a());
            fileInputStream = new FileInputStream(new File(str));
            try {
                fileOutputStream = new FileOutputStream(new File(a(context, a, vVar.b())), true);
                try {
                    byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                    while (true) {
                        int read = fileInputStream.read(bArr);
                        if (read <= 0) {
                            break;
                        }
                        fileOutputStream.write(bArr, 0, read);
                    }
                    a.a(ajVar, bfVar, be.b(bfVar.a()));
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e4) {
                    e2 = e4;
                    fileOutputStream2 = fileInputStream;
                    try {
                        throw e2;
                    } catch (Throwable th2) {
                        th = th2;
                        fileInputStream = fileOutputStream2;
                        fileOutputStream2 = fileOutputStream;
                    }
                } catch (IOException e5) {
                    e32 = e5;
                    fileOutputStream2 = fileOutputStream;
                    throw e32;
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream2 = fileOutputStream;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e62) {
                            e62.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e2 = e7;
                fileOutputStream = null;
                Object obj = fileInputStream;
                throw e2;
            } catch (IOException e8) {
                e32 = e8;
                throw e32;
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        } catch (FileNotFoundException e9) {
            e2 = e9;
            fileOutputStream = null;
            throw e2;
        } catch (IOException e10) {
            e32 = e10;
            fileInputStream = null;
            throw e32;
        } catch (Throwable th5) {
            th = th5;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            throw th;
        }
    }

    static void a(Context context, aj ajVar, String str) {
        c(context, ajVar, str);
    }

    public static void a(aj ajVar, Context context, v vVar) {
        ak beVar = new be();
        String a = vVar.a();
        String b = b(a, vVar.b());
        bf a2 = a.a(ajVar, b);
        if (a2 != null) {
            b(context, ajVar, b);
            List c = ajVar.c(be.a(a, a2.e()), beVar);
            if (c != null && c.size() > 0) {
                bf bfVar = (bf) c.get(0);
                bfVar.a("errorstatus");
                a.a(ajVar, bfVar, be.b(bfVar.a()));
                File file = new File(a(context, bfVar.a()));
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    static void a(List<bf> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int i2 = i + 1; i2 < list.size(); i2++) {
                bf bfVar = (bf) list.get(i);
                bf bfVar2 = (bf) list.get(i2);
                if (bh.a(bfVar2.e(), bfVar.e()) > 0) {
                    list.set(i, bfVar2);
                    list.set(i2, bfVar);
                }
            }
        }
    }

    static boolean a(Context context, aj ajVar, String str, v vVar) {
        return a(ajVar, str, a(context, str), vVar);
    }

    static boolean a(aj ajVar, String str, String str2, v vVar) {
        bf a = a.a(ajVar, str);
        return a != null && vVar.b().equals(a.d()) && a(str2, a.b());
    }

    static boolean a(String str, String str2) {
        String a = s.a(str);
        return a != null && a.equalsIgnoreCase(str2);
    }

    static String b(String str, String str2) {
        return s.b(str + str2) + ".jar";
    }

    static void b(Context context, aj ajVar, String str) {
        c(context, ajVar, str);
        c(context, ajVar, a(str));
    }

    static void b(final Context context, final String str, final String str2) {
        new Thread() {
            public void run() {
                try {
                    aj ajVar = new aj(context, bd.c());
                    List<bf> c = ajVar.c(be.a(str), new be());
                    if (c != null && c.size() > 0) {
                        for (bf bfVar : c) {
                            if (!str2.equalsIgnoreCase(bfVar.d())) {
                                ba.a(context, ajVar, bfVar.a());
                            }
                        }
                    }
                } catch (Throwable th) {
                    aa.a(th, "DexFileManager", "clearUnSuitableVersion");
                }
            }
        }.start();
    }

    private static void c(Context context, aj ajVar, String str) {
        File file = new File(a(context, str));
        if (file.exists()) {
            file.delete();
        }
        ajVar.a(be.b(str), new be());
    }
}
