package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/* compiled from: OfflineLocManager */
public class hh {
    public static void a(Context context) {
        try {
            if (e(context)) {
                a(context, System.currentTimeMillis());
                String b = b(context);
                if (!TextUtils.isEmpty(b)) {
                    gx.a().b(new fn(fi.c(fi.a(b)), "6"));
                }
            }
        } catch (Throwable th) {
            fl.a(th, "OfflineLocManager", "updateOfflineLocData");
        }
    }

    private static void a(Context context, long j) {
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(fm.a(context, "f.log"));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file);
            try {
                fileOutputStream2.write(fi.a(String.valueOf(j)));
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th2 = th3;
                fileOutputStream = fileOutputStream2;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th2;
            }
        } catch (Throwable th4) {
            th2 = th4;
            fl.a(th2, "OfflineLocManager", "updateLogUpdateTime");
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private static String a(String str) {
        Object obj = 1;
        gt gtVar = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            gtVar = gt.a(new File(str), 1, 1, 204800);
            File file = new File(str);
            if (file != null) {
                if (file.exists()) {
                    for (String str2 : file.list()) {
                        String str22;
                        if (str22.contains(".0")) {
                            str22 = fi.a(hk.a(gtVar, str22.split("\\.")[0]));
                            if (obj == null) {
                                stringBuilder.append(",");
                            } else {
                                obj = null;
                            }
                            stringBuilder.append("{\"log\":\"").append(str22).append("\"}");
                        }
                    }
                }
            }
            if (gtVar != null) {
                try {
                    gtVar.close();
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        } catch (Throwable th2) {
            fl.a(th2, "StatisticsManager", "getContent");
            if (gtVar != null) {
                gtVar.close();
            }
        } catch (Throwable th22) {
            th22.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String b(Context context) {
        Object a = a(fm.a(context, fm.f));
        if (TextUtils.isEmpty(a)) {
            return null;
        }
        String f = f(context);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(f).append("\",\"els\":[");
        stringBuilder.append(a);
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    private static int c(Context context) {
        int length;
        try {
            File file = new File(fm.a(context, fm.f));
            if (!file.exists()) {
                return 0;
            }
            length = file.list().length;
            return length;
        } catch (Throwable th) {
            fl.a(th, "OfflineLocManager", "getFileNum");
            length = 0;
        }
    }

    private static long d(Context context) {
        FileInputStream fileInputStream;
        Throwable th;
        File file = new File(fm.a(context, "f.log"));
        if (!file.exists()) {
            return 0;
        }
        try {
            fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[fileInputStream.available()];
                fileInputStream.read(bArr);
                long parseLong = Long.parseLong(fi.a(bArr));
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
                return parseLong;
            } catch (Throwable th3) {
                th2 = th3;
                try {
                    fl.a(th2, "OfflineLocManager", "getUpdateTime");
                    if (file != null) {
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } catch (Throwable th4) {
                    th2 = th4;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th5) {
                            th5.printStackTrace();
                        }
                    }
                    throw th2;
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th22) {
                        th22.printStackTrace();
                    }
                }
                return System.currentTimeMillis();
            }
        } catch (Throwable th6) {
            th22 = th6;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th22;
        }
    }

    private static boolean e(Context context) {
        try {
            if (fc.m(context) == 1) {
                boolean z;
                if (System.currentTimeMillis() - d(context) <= 604800000) {
                    z = true;
                } else {
                    z = false;
                }
                return !z || c(context) >= 100;
            }
        } catch (Throwable th) {
            fl.a(th, "StatisticsManager", "isUpdate");
        }
        return false;
    }

    private static String f(Context context) {
        return fb.b(context, fi.a(g(context)));
    }

    private static String g(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"key\":\"").append(ey.f(context)).append("\",\"platform\":\"android\",\"diu\":\"").append(fc.q(context)).append("\",\"mac\":\"").append(fc.i(context)).append("\",\"tid\":\"").append(fc.f(context)).append("\",\"manufacture\":\"").append(Build.MANUFACTURER).append("\",\"device\":\"").append(Build.DEVICE).append("\",\"sim\":\"").append(fc.r(context)).append("\",\"pkg\":\"").append(ey.c(context)).append("\",\"model\":\"").append(Build.MODEL).append("\",\"appversion\":\"").append(ey.d(context)).append("\"");
        } catch (Throwable th) {
            fl.a(th, "CInfo", "getPublicJSONInfo");
        }
        return stringBuilder.toString();
    }
}
