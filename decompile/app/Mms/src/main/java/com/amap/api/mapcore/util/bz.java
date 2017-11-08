package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import com.amap.api.mapcore.util.cx.b;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/* compiled from: StatisticsManager */
public class bz {
    private static boolean a = true;

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
                    Object a = bx.a(stringBuffer.toString());
                    Object b = b(context);
                    byte[] bArr = new byte[(a.length + b.length)];
                    System.arraycopy(a, 0, bArr, 0, a.length);
                    System.arraycopy(b, 0, bArr, a.length, b.length);
                    dd.a().b(new cd(bx.c(bArr), "2"));
                }
            }
        } catch (Throwable e) {
            cb.a(e, "StatisticsManager", "updateStaticsData");
        } catch (Throwable e2) {
            cb.a(e2, "StatisticsManager", "updateStaticsData");
        }
    }

    private static byte[] a(Context context, byte[] bArr) {
        try {
            return bn.c(context, bArr);
        } catch (CertificateException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e3) {
            e3.printStackTrace();
            return null;
        } catch (IOException e4) {
            e4.printStackTrace();
            return null;
        } catch (InvalidKeyException e5) {
            e5.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e6) {
            e6.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e7) {
            e7.printStackTrace();
            return null;
        } catch (BadPaddingException e8) {
            e8.printStackTrace();
            return null;
        }
    }

    private static byte[] c(Context context) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[0];
        try {
            bx.a(byteArrayOutputStream, "1.2.12.5");
            bx.a(byteArrayOutputStream, bq.q(context));
            bx.a(byteArrayOutputStream, bq.i(context));
            bx.a(byteArrayOutputStream, bq.f(context));
            bx.a(byteArrayOutputStream, Build.MANUFACTURER);
            bx.a(byteArrayOutputStream, Build.MODEL);
            bx.a(byteArrayOutputStream, Build.DEVICE);
            bx.a(byteArrayOutputStream, bq.r(context));
            bx.a(byteArrayOutputStream, bl.c(context));
            bx.a(byteArrayOutputStream, bl.d(context));
            bx.a(byteArrayOutputStream, bl.f(context));
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
            File file = new File(cc.a(context, cc.e));
            if (!file.exists()) {
                return 0;
            }
            length = file.list().length;
            return length;
        } catch (Throwable th) {
            cb.a(th, "StatisticsManager", "getFileNum");
            length = 0;
        }
    }

    private static byte[] e(Context context) {
        int i = 0;
        cx cxVar = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[0];
        String a = cc.a(context, cc.e);
        try {
            cxVar = cx.a(new File(a), 1, 1, 10240);
            File file = new File(a);
            if (file != null && file.exists()) {
                String[] list = file.list();
                int length = list.length;
                while (i < length) {
                    String str = list[i];
                    if (str.contains(".0")) {
                        byteArrayOutputStream.write(a(cxVar, str.split("\\.")[0]));
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
            if (cxVar == null) {
                return toByteArray;
            }
            try {
                cxVar.close();
                return toByteArray;
            } catch (Throwable th) {
                th.printStackTrace();
                return toByteArray;
            }
        } catch (Throwable e2) {
            cb.a(e2, "StatisticsManager", "getContent");
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (cxVar != null) {
                cxVar.close();
            }
        } catch (Throwable e22) {
            e22.printStackTrace();
        }
        return bArr;
    }

    private static byte[] a(cx cxVar, String str) {
        b a;
        Throwable th;
        InputStream inputStream = null;
        byte[] bArr = new byte[0];
        try {
            a = cxVar.a(str);
            try {
                inputStream = a.a(0);
                bArr = new byte[inputStream.available()];
                inputStream.read(bArr);
                cxVar.c(str);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
                if (a != null) {
                    try {
                        a.close();
                    } catch (Throwable th22) {
                        th22.printStackTrace();
                    }
                }
                return bArr;
            } catch (Throwable th3) {
                th22 = th3;
                try {
                    th22.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th222) {
                            th222.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable th2222) {
                            th2222.printStackTrace();
                        }
                    }
                    return bArr;
                } catch (Throwable th4) {
                    th2222 = th4;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th5) {
                            th5.printStackTrace();
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable th52) {
                            th52.printStackTrace();
                        }
                    }
                    throw th2222;
                }
            }
        } catch (Throwable th6) {
            th2222 = th6;
            a = null;
            if (inputStream != null) {
                inputStream.close();
            }
            if (a != null) {
                a.close();
            }
            throw th2222;
        }
    }

    private static void a(Context context, long j) {
        Throwable th;
        FileNotFoundException e;
        IOException e2;
        File file = new File(cc.a(context, "c.log"));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(bx.a(String.valueOf(j)));
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
        FileInputStream fileInputStream;
        Throwable th;
        File file = new File(cc.a(context, "c.log"));
        if (!file.exists()) {
            return 0;
        }
        try {
            fileInputStream = new FileInputStream(file);
            try {
                byte[] bArr = new byte[fileInputStream.available()];
                fileInputStream.read(bArr);
                long parseLong = Long.parseLong(bx.a(bArr));
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
                cb.a(th2, "StatisticsManager", "getUpdateTime");
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
                cb.a(th22, "StatisticsManager", "getUpdateTime");
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
                cb.a(th222, "StatisticsManager", "getUpdateTime");
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
            cb.a(th22222, "StatisticsManager", "getUpdateTime");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return 0;
        } catch (IOException e4) {
            th22222 = e4;
            fileInputStream = null;
            cb.a(th22222, "StatisticsManager", "getUpdateTime");
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
            if (bq.m(context) != 1 || !a || d(context) < 100) {
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
            cb.a(th, "StatisticsManager", "isUpdate");
        }
        return false;
    }
}
