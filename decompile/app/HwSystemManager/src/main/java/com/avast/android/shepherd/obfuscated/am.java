package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import android.os.Environment;
import com.avast.android.shepherd.obfuscated.as.a;
import com.huawei.systemmanager.power.comm.TimeConst;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/* compiled from: Unknown */
public class am {

    /* compiled from: Unknown */
    /* renamed from: com.avast.android.shepherd.obfuscated.am$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[al.values().length];

        static {
            try {
                a[al.TEST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[al.STAGE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[al.SANDBOX.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static ah a(Context context, al alVar) {
        aj ajVar = new aj();
        ajVar.i = a.a();
        ajVar.f = TimeConst.POWER_SAVING_TIMEOUT;
        ajVar.g = TimeConst.POWER_SAVING_TIMEOUT;
        ajVar.d = a(alVar);
        ajVar.e = b(alVar);
        ajVar.b = c(alVar);
        return new ah(ajVar, an.a(context));
    }

    private static String a(al alVar) {
        switch (AnonymousClass1.a[alVar.ordinal()]) {
            case 1:
                return "https://auth-test.ff.avast.com:443";
            case 2:
                return "https://auth.ff.avast.com:443";
            case 3:
                return "https://auth.ff.avast.com:443";
            default:
                return "https://auth.ff.avast.com:443";
        }
    }

    private static String b(al alVar) {
        switch (AnonymousClass1.a[alVar.ordinal()]) {
            case 1:
                return "http://streamback-test.ff.avast.com:80";
            case 2:
                return "http://lon23.ff.avast.com:80";
            case 3:
                return "http://streamback-sandbox.ff.avast.com:80";
            default:
                return "http://streamback.ff.avast.com:80";
        }
    }

    private static KeyStore c(al alVar) {
        FileInputStream fileInputStream;
        Throwable th;
        switch (AnonymousClass1.a[alVar.ordinal()]) {
            case 1:
            case 2:
                try {
                    String str = Environment.getExternalStorageDirectory() + File.separator + "streamback_stage.bks";
                    System.setProperty("javax.net.ssl.trustStore", str);
                    fileInputStream = new FileInputStream(str);
                    try {
                        KeyStore instance = KeyStore.getInstance("BKS");
                        instance.load(fileInputStream, "".toCharArray());
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return instance;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = null;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            case 3:
                return null;
            default:
                return null;
        }
    }
}
