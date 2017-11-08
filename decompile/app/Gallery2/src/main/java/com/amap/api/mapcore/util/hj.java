package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/* compiled from: StatisticsManager */
public class hj {
    private static boolean a = true;

    public static synchronized void a(final hi hiVar, final Context context) {
        synchronized (hj.class) {
            fo.c().submit(new Runnable() {
                public void run() {
                    /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:32:0x0081
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r10 = this;
                    r1 = 0;
                    r3 = com.amap.api.mapcore.util.hj.class;
                    monitor-enter(r3);
                    r0 = new java.util.Random;	 Catch:{ all -> 0x0073 }
                    r0.<init>();	 Catch:{ all -> 0x0073 }
                    r2 = r3;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r4 = r2.a();	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r2 = r4;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r5 = com.amap.api.mapcore.util.fm.e;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r2 = com.amap.api.mapcore.util.fm.a(r2, r5);	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r5 = new java.io.File;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r5.<init>(r2);	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r2 = 1;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r6 = 1;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r8 = 102400; // 0x19000 float:1.43493E-40 double:5.05923E-319;	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r2 = com.amap.api.mapcore.util.gt.a(r5, r2, r6, r8);	 Catch:{ Throwable -> 0x0060, all -> 0x0076 }
                    r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x00b1 }
                    r5.<init>();	 Catch:{ Throwable -> 0x00b1 }
                    r6 = 100;	 Catch:{ Throwable -> 0x00b1 }
                    r0 = r0.nextInt(r6);	 Catch:{ Throwable -> 0x00b1 }
                    r0 = java.lang.Integer.toString(r0);	 Catch:{ Throwable -> 0x00b1 }
                    r0 = r5.append(r0);	 Catch:{ Throwable -> 0x00b1 }
                    r6 = java.lang.System.nanoTime();	 Catch:{ Throwable -> 0x00b1 }
                    r5 = java.lang.Long.toString(r6);	 Catch:{ Throwable -> 0x00b1 }
                    r0 = r0.append(r5);	 Catch:{ Throwable -> 0x00b1 }
                    r0 = r0.toString();	 Catch:{ Throwable -> 0x00b1 }
                    r0 = r2.b(r0);	 Catch:{ Throwable -> 0x00b1 }
                    r5 = 0;	 Catch:{ Throwable -> 0x00b1 }
                    r1 = r0.a(r5);	 Catch:{ Throwable -> 0x00b1 }
                    r1.write(r4);	 Catch:{ Throwable -> 0x00b1 }
                    r0.a();	 Catch:{ Throwable -> 0x00b1 }
                    r2.b();	 Catch:{ Throwable -> 0x00b1 }
                    if (r1 != 0) goto L_0x007d;
                L_0x005c:
                    if (r2 != 0) goto L_0x0086;
                L_0x005e:
                    monitor-exit(r3);	 Catch:{ all -> 0x0073 }
                    return;
                L_0x0060:
                    r0 = move-exception;
                    r2 = r1;
                L_0x0062:
                    r4 = "StatisticsManager";	 Catch:{ all -> 0x00af }
                    r5 = "applyStatics";	 Catch:{ all -> 0x00af }
                    com.amap.api.mapcore.util.fl.a(r0, r4, r5);	 Catch:{ all -> 0x00af }
                    if (r1 != 0) goto L_0x008f;
                L_0x006d:
                    if (r2 == 0) goto L_0x005e;
                L_0x006f:
                    r2.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x005e;	 Catch:{ all -> 0x0073 }
                L_0x0073:
                    r0 = move-exception;	 Catch:{ all -> 0x0073 }
                    monitor-exit(r3);	 Catch:{ all -> 0x0073 }
                    throw r0;
                L_0x0076:
                    r0 = move-exception;
                    r2 = r1;
                L_0x0078:
                    if (r1 != 0) goto L_0x009d;
                L_0x007a:
                    if (r2 != 0) goto L_0x00a6;
                L_0x007c:
                    throw r0;	 Catch:{ all -> 0x0073 }
                L_0x007d:
                    r1.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x005c;	 Catch:{ all -> 0x0073 }
                    r0 = move-exception;	 Catch:{ all -> 0x0073 }
                    r0.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x005c;	 Catch:{ all -> 0x0073 }
                L_0x0086:
                    r2.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x005e;	 Catch:{ all -> 0x0073 }
                    r0 = move-exception;	 Catch:{ all -> 0x0073 }
                    r0.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x005e;	 Catch:{ all -> 0x0073 }
                L_0x008f:
                    r1.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x006d;	 Catch:{ all -> 0x0073 }
                    r0 = move-exception;	 Catch:{ all -> 0x0073 }
                    r0.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x006d;	 Catch:{ all -> 0x0073 }
                    r0 = move-exception;	 Catch:{ all -> 0x0073 }
                    r0.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x005e;	 Catch:{ all -> 0x0073 }
                L_0x009d:
                    r1.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x007a;	 Catch:{ all -> 0x0073 }
                    r1 = move-exception;	 Catch:{ all -> 0x0073 }
                    r1.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x007a;	 Catch:{ all -> 0x0073 }
                L_0x00a6:
                    r2.close();	 Catch:{ all -> 0x0073 }
                    goto L_0x007c;	 Catch:{ all -> 0x0073 }
                    r1 = move-exception;	 Catch:{ all -> 0x0073 }
                    r1.printStackTrace();	 Catch:{ all -> 0x0073 }
                    goto L_0x007c;
                L_0x00af:
                    r0 = move-exception;
                    goto L_0x0078;
                L_0x00b1:
                    r0 = move-exception;
                    goto L_0x0062;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.amap.api.mapcore.util.hj.1.run():void");
                }
            });
        }
    }

    private static byte[] b(Context context) {
        Object c = c(context);
        Object e = e(context);
        byte[] bArr = new byte[(c.length + e.length)];
        System.arraycopy(c, 0, bArr, 0, c.length);
        System.arraycopy(e, 0, bArr, c.length, e.length);
        return a(context, bArr);
    }

    public static void a(Context context) {
        try {
            if (g(context)) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date()));
                stringBuffer.append(" ");
                stringBuffer.append(UUID.randomUUID().toString());
                stringBuffer.append(" ");
                if (stringBuffer.length() == 53) {
                    Object a = fi.a(stringBuffer.toString());
                    Object b = b(context);
                    byte[] bArr = new byte[(a.length + b.length)];
                    System.arraycopy(a, 0, bArr, 0, a.length);
                    System.arraycopy(b, 0, bArr, a.length, b.length);
                    gx.a().b(new fn(fi.c(bArr), GpsMeasureMode.MODE_2_DIMENSIONAL));
                }
            }
        } catch (Throwable th) {
            fl.a(th, "StatisticsManager", "updateStaticsData");
        }
    }

    private static byte[] a(Context context, byte[] bArr) {
        try {
            return fb.a(context, bArr);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private static byte[] c(Context context) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[0];
        try {
            fi.a(byteArrayOutputStream, "1.2.13.6");
            fi.a(byteArrayOutputStream, "Android");
            fi.a(byteArrayOutputStream, fc.q(context));
            fi.a(byteArrayOutputStream, fc.i(context));
            fi.a(byteArrayOutputStream, fc.f(context));
            fi.a(byteArrayOutputStream, Build.MANUFACTURER);
            fi.a(byteArrayOutputStream, Build.MODEL);
            fi.a(byteArrayOutputStream, Build.DEVICE);
            fi.a(byteArrayOutputStream, fc.r(context));
            fi.a(byteArrayOutputStream, ey.c(context));
            fi.a(byteArrayOutputStream, ey.d(context));
            fi.a(byteArrayOutputStream, ey.f(context));
            byteArrayOutputStream.write(new byte[]{(byte) 0});
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
                return toByteArray;
            } catch (Throwable th) {
                th.printStackTrace();
                return toByteArray;
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        return bArr;
    }

    private static int d(Context context) {
        int length;
        try {
            File file = new File(fm.a(context, fm.e));
            if (!file.exists()) {
                return 0;
            }
            length = file.list().length;
            return length;
        } catch (Throwable th) {
            fl.a(th, "StatisticsManager", "getFileNum");
            length = 0;
        }
    }

    private static byte[] e(Context context) {
        int i = 0;
        gt gtVar = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[0];
        String a = fm.a(context, fm.e);
        try {
            gtVar = gt.a(new File(a), 1, 1, 102400);
            File file = new File(a);
            if (file != null && file.exists()) {
                String[] list = file.list();
                int length = list.length;
                while (i < length) {
                    String str = list[i];
                    if (str.contains(".0")) {
                        byteArrayOutputStream.write(hk.a(gtVar, str.split("\\.")[0]));
                    }
                    i++;
                }
            }
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (gtVar == null) {
                return toByteArray;
            }
            try {
                gtVar.close();
                return toByteArray;
            } catch (Throwable th) {
                th.printStackTrace();
                return toByteArray;
            }
        } catch (Throwable e2) {
            fl.a(e2, "StatisticsManager", "getContent");
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (gtVar != null) {
                gtVar.close();
            }
        } catch (Throwable e22) {
            e22.printStackTrace();
        }
        return bArr;
    }

    private static void a(Context context, long j) {
        Throwable th;
        FileNotFoundException e;
        IOException e2;
        File file = new File(fm.a(context, "c.log"));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(fi.a(String.valueOf(j)));
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e3) {
                e = e3;
                try {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th22) {
                            th22.printStackTrace();
                        }
                    }
                } catch (Throwable th3) {
                    th22 = th3;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th4) {
                            th4.printStackTrace();
                        }
                    }
                    throw th22;
                }
            } catch (IOException e4) {
                e2 = e4;
                e2.printStackTrace();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable th222) {
                        th222.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            fileOutputStream = null;
            e.printStackTrace();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e6) {
            e2 = e6;
            fileOutputStream = null;
            e2.printStackTrace();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Throwable th5) {
            th222 = th5;
            fileOutputStream = null;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th222;
        }
    }

    private static long f(Context context) {
        Throwable th;
        File file = new File(fm.a(context, "c.log"));
        if (!file.exists()) {
            return 0;
        }
        FileInputStream fileInputStream;
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
            } catch (FileNotFoundException e) {
                th2 = e;
                fl.a(th2, "StatisticsManager", "getUpdateTime");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th22) {
                        th22.printStackTrace();
                    }
                }
                return 0;
            } catch (IOException e2) {
                th22 = e2;
                fl.a(th22, "StatisticsManager", "getUpdateTime");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th222) {
                        th222.printStackTrace();
                    }
                }
                return 0;
            } catch (Throwable th3) {
                th222 = th3;
                fl.a(th222, "StatisticsManager", "getUpdateTime");
                if (file != null) {
                    try {
                        if (file.exists()) {
                            file.delete();
                        }
                    } catch (Throwable th2222) {
                        th2222.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th22222) {
                        th22222.printStackTrace();
                    }
                }
                return 0;
            }
        } catch (FileNotFoundException e3) {
            th22222 = e3;
            fileInputStream = null;
            fl.a(th22222, "StatisticsManager", "getUpdateTime");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return 0;
        } catch (IOException e4) {
            th22222 = e4;
            fileInputStream = null;
            fl.a(th22222, "StatisticsManager", "getUpdateTime");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return 0;
        } catch (Throwable th4) {
            th22222 = th4;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th22222;
        }
    }

    private static boolean g(Context context) {
        try {
            if (fc.m(context) != 1 || !a || d(context) < 100) {
                return false;
            }
            boolean z;
            long f = f(context);
            long time = new Date().getTime();
            if (time - f >= 3600000) {
                z = true;
            } else {
                z = false;
            }
            if (!z) {
                return false;
            }
            a(context, time);
            a = false;
            return true;
        } catch (Throwable th) {
            fl.a(th, "StatisticsManager", "isUpdate");
        }
        return false;
    }
}
