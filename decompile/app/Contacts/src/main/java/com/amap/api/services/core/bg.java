package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;
import com.amap.api.services.core.bz.a;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/* compiled from: LogWriter */
abstract class bg {
    private ar a;

    protected abstract int a();

    protected abstract cc a(bq bqVar);

    protected abstract String a(String str);

    protected abstract String a(List<ar> list);

    protected abstract String b();

    bg() {
    }

    static bg a(int i) {
        bg baVar;
        switch (i) {
            case 0:
                baVar = new ba();
                break;
            case 1:
                baVar = new bc();
                break;
            case 2:
                baVar = new ax();
                break;
            default:
                return null;
        }
        return baVar;
    }

    void a(Context context, Throwable th, String str, String str2) {
        List<ar> b = b(context);
        if (b != null && b.size() != 0) {
            String a = a(th);
            if (a != null && !"".equals(a)) {
                for (ar arVar : b) {
                    if (a(arVar.f(), a)) {
                        a(arVar);
                        String c = c();
                        String a2 = a(context, arVar);
                        String c2 = c(context);
                        String b2 = b(th);
                        if (b2 != null && !"".equals(b2)) {
                            int a3 = a();
                            StringBuilder stringBuilder = new StringBuilder();
                            if (str != null) {
                                stringBuilder.append("class:").append(str);
                            }
                            if (str2 != null) {
                                stringBuilder.append(" method:").append(str2).append("$").append("<br/>");
                            }
                            stringBuilder.append(a);
                            String a4 = a(a);
                            String a5 = a(c2, a2, c, a3, b2, stringBuilder.toString());
                            if (a5 != null && !"".equals(a5)) {
                                String a6 = a(context, a5);
                                String b3 = b();
                                synchronized (Looper.getMainLooper()) {
                                    bq bqVar = new bq(context);
                                    a(bqVar, arVar.a(), a4, a3, a(context, a4, b3, a6, bqVar));
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    void a(Context context) {
        List b = b(context);
        if (b != null && b.size() != 0) {
            String a = a(b);
            if (a != null && !"".equals(a)) {
                String c = c();
                String a2 = a(context, this.a);
                int a3 = a();
                String a4 = a(c(context), a2, c, a3, "ANR", a);
                if (a4 != null && !"".equals(a4)) {
                    String a5 = a(a);
                    String a6 = a(context, a4);
                    String b2 = b();
                    synchronized (Looper.getMainLooper()) {
                        bq bqVar = new bq(context);
                        a(bqVar, this.a.a(), a5, a3, a(context, a5, b2, a6, bqVar));
                    }
                }
            }
        }
    }

    protected void a(ar arVar) {
        this.a = arVar;
    }

    private List<ar> b(Context context) {
        List<ar> list = null;
        try {
            synchronized (Looper.getMainLooper()) {
                list = new bt(context, false).a();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return list;
    }

    private void a(bq bqVar, String str, String str2, int i, boolean z) {
        bs bsVar = new bs();
        bsVar.a(0);
        bsVar.b(str);
        bsVar.a(str2);
        bqVar.b(bsVar, i);
    }

    private String a(String str, String str2, String str3, int i, String str4, String str5) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(str2).append(",").append("\"timestamp\":\"");
        stringBuffer.append(str3);
        stringBuffer.append("\",\"et\":\"");
        stringBuffer.append(i);
        stringBuffer.append("\",\"classname\":\"");
        stringBuffer.append(str4);
        stringBuffer.append("\",");
        stringBuffer.append("\"detail\":\"");
        stringBuffer.append(str5);
        stringBuffer.append("\"");
        return stringBuffer.toString();
    }

    private String a(Context context, String str) {
        try {
            return at.a(context, str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private String c() {
        return bh.a(new Date().getTime());
    }

    protected String a(Throwable th) {
        String str = null;
        try {
            str = bh.a(th);
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        return str;
    }

    private String b(Throwable th) {
        return th.toString();
    }

    private String a(Context context, ar arVar) {
        return al.a(context, arVar);
    }

    private String c(Context context) {
        return aj.a(context);
    }

    private boolean a(Context context, String str, String str2, String str3, bq bqVar) {
        Throwable th;
        IOException e;
        bz bzVar;
        OutputStream outputStream;
        bz bzVar2 = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getFilesDir().getAbsolutePath());
            stringBuilder.append(bd.a);
            stringBuilder.append(str2);
            File file = new File(stringBuilder.toString());
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            bz a = bz.a(file, 1, 1, 20480);
            try {
                a.a(a(bqVar));
                if (a.a(str) == null) {
                    byte[] bytes = str3.getBytes("UTF-8");
                    a b = a.b(str);
                    OutputStream a2 = b.a(0);
                    try {
                        a2.write(bytes);
                        b.a();
                        a.b();
                        if (a2 != null) {
                            try {
                                a2.close();
                            } catch (Throwable th2) {
                                th2.printStackTrace();
                            }
                        }
                        if (!(a == null || a.a())) {
                            try {
                                a.close();
                            } catch (Throwable th22) {
                                th22.printStackTrace();
                            }
                        }
                        return true;
                    } catch (IOException e2) {
                        e = e2;
                        bzVar = a;
                        outputStream = a2;
                        bzVar2 = bzVar;
                        e.printStackTrace();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable th222) {
                                th222.printStackTrace();
                            }
                        }
                        if (!(bzVar2 == null || bzVar2.a())) {
                            try {
                                bzVar2.close();
                            } catch (Throwable th2222) {
                                th2222.printStackTrace();
                            }
                        }
                        return false;
                    } catch (Throwable th3) {
                        th2222 = th3;
                        bzVar = a;
                        outputStream = a2;
                        bzVar2 = bzVar;
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable th4) {
                                th4.printStackTrace();
                            }
                        }
                        if (!(bzVar2 == null || bzVar2.a())) {
                            try {
                                bzVar2.close();
                            } catch (Throwable th5) {
                                th5.printStackTrace();
                            }
                        }
                        throw th2222;
                    }
                }
                if (!(a == null || a.a())) {
                    try {
                        a.close();
                    } catch (Throwable th22222) {
                        th22222.printStackTrace();
                    }
                }
                return false;
            } catch (IOException e3) {
                e = e3;
                bzVar = a;
                outputStream = null;
                bzVar2 = bzVar;
                e.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                bzVar2.close();
                return false;
            } catch (Throwable th6) {
                th22222 = th6;
                bzVar = a;
                outputStream = null;
                bzVar2 = bzVar;
                if (outputStream != null) {
                    outputStream.close();
                }
                bzVar2.close();
                throw th22222;
            }
        } catch (IOException e4) {
            e = e4;
            outputStream = null;
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            bzVar2.close();
            return false;
        } catch (Throwable th7) {
            th22222 = th7;
            if (outputStream != null) {
                outputStream.close();
            }
            bzVar2.close();
            throw th22222;
        }
    }

    protected boolean a(String[] strArr, String str) {
        if (strArr == null || str == null) {
            return false;
        }
        try {
            for (String indexOf : strArr) {
                if (str.indexOf(indexOf) != -1) {
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }
}
