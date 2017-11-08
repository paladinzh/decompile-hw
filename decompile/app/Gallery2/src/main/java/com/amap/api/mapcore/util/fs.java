package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Looper;
import com.amap.api.mapcore.util.gt.b;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;

/* compiled from: LogProcessor */
public abstract class fs {
    private fh a;
    private int b;
    private gu c;
    private gt d;

    /* compiled from: LogProcessor */
    class a implements gu {
        final /* synthetic */ fs a;
        private gc b;

        a(fs fsVar, gc gcVar) {
            this.a = fsVar;
            this.b = gcVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, fm.a(this.a.b()));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    protected abstract String a(List<fh> list);

    protected abstract boolean a(Context context);

    protected fs(int i) {
        this.b = i;
    }

    private void e(Context context) {
        try {
            this.d = b(context, a());
        } catch (Throwable th) {
            fl.a(th, "LogProcessor", "LogUpDateProcessor");
        }
    }

    void a(fh fhVar, Context context, Throwable th, String str, String str2, String str3) {
        a(fhVar, context, c(th), str, str2, str3);
    }

    void a(fh fhVar, Context context, String str, String str2, String str3, String str4) {
        a(fhVar);
        String d = d();
        String a = a(context, fhVar);
        String a2 = ey.a(context);
        if (str != null && !"".equals(str)) {
            int b = b();
            StringBuilder stringBuilder = new StringBuilder();
            if (str3 != null) {
                stringBuilder.append("class:").append(str3);
            }
            if (str4 != null) {
                stringBuilder.append(" method:").append(str4).append("$").append("<br/>");
            }
            stringBuilder.append(str2);
            String a3 = a(str2);
            String a4 = a(a2, a, d, b, str, stringBuilder.toString());
            if (a4 != null && !"".equals(a4)) {
                String a5 = a(context, a4);
                String a6 = a();
                synchronized (Looper.getMainLooper()) {
                    gc gcVar = new gc(context);
                    a(gcVar, fhVar.a(), a3, b, a(context, a3, a6, a5, gcVar));
                }
            }
        }
    }

    void a(Context context, Throwable th, String str, String str2) {
        List<fh> f = f(context);
        if (f != null && f.size() != 0) {
            String a = a(th);
            if (a != null && !"".equals(a)) {
                for (fh fhVar : f) {
                    if (a(fhVar.f(), a)) {
                        a(fhVar, context, th, a.replaceAll("\n", "<br/>"), str, str2);
                        return;
                    }
                }
                if (a.contains("com.amap.api.col")) {
                    try {
                        a(new com.amap.api.mapcore.util.fh.a("collection", "1.0", "AMap_collection_1.0").a(new String[]{"com.amap.api.collection"}).a(), context, th, a, str, str2);
                    } catch (ex e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String a(Context context, fh fhVar) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"sim\":\"").append(fc.e(context)).append("\",\"sdkversion\":\"").append(fhVar.c()).append("\",\"product\":\"").append(fhVar.a()).append("\",\"ed\":\"").append(fhVar.e()).append("\",\"nt\":\"").append(fc.c(context)).append("\",\"np\":\"").append(fc.a(context)).append("\",\"mnc\":\"").append(fc.b(context)).append("\",\"ant\":\"").append(fc.d(context)).append("\"");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    void b(Context context) {
        List f = f(context);
        if (f != null && f.size() != 0) {
            String a = a(f);
            if (a != null && !"".equals(a)) {
                String d = d();
                String a2 = a(context, this.a);
                int b = b();
                String a3 = a(ey.a(context), a2, d, b, "ANR", a);
                if (a3 != null && !"".equals(a3)) {
                    String a4 = a(a);
                    String a5 = a(context, a3);
                    String a6 = a();
                    synchronized (Looper.getMainLooper()) {
                        gc gcVar = new gc(context);
                        a(gcVar, this.a.a(), a4, b, a(context, a4, a6, a5, gcVar));
                    }
                }
            }
        }
    }

    protected void a(fh fhVar) {
        this.a = fhVar;
    }

    private List<fh> f(Context context) {
        List<fh> list = null;
        try {
            synchronized (Looper.getMainLooper()) {
                list = new ge(context, false).a();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return list;
    }

    private void a(gc gcVar, String str, String str2, int i, boolean z) {
        gd b = fm.b(i);
        b.a(0);
        b.b(str);
        b.a(str2);
        gcVar.a(b);
    }

    protected String a(String str) {
        return fe.c(str);
    }

    protected gu a(gc gcVar) {
        try {
            if (this.c == null) {
                this.c = new a(this, gcVar);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this.c;
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
            return fb.e(context, fi.a(str));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private String d() {
        return fi.a(new Date().getTime());
    }

    protected String a(Throwable th) {
        String str = null;
        try {
            str = b(th);
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        return str;
    }

    private String c(Throwable th) {
        return th.toString();
    }

    private boolean a(Context context, String str, String str2, String str3, gc gcVar) {
        gt a;
        Throwable th;
        IOException e;
        b bVar;
        gt gtVar;
        OutputStream outputStream;
        OutputStream outputStream2;
        Object obj;
        Object obj2;
        b bVar2 = null;
        try {
            File file = new File(fm.a(context, str2));
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            a = gt.a(file, 1, 1, 20480);
            try {
                a.a(a(gcVar));
                b a2 = a.a(str);
                if (a2 == null) {
                    try {
                        byte[] a3 = fi.a(str3);
                        com.amap.api.mapcore.util.gt.a b = a.b(str);
                        OutputStream a4 = b.a(0);
                        try {
                            a4.write(a3);
                            b.a();
                            a.b();
                            if (a4 != null) {
                                try {
                                    a4.close();
                                } catch (Throwable th2) {
                                    th2.printStackTrace();
                                }
                            }
                            if (a2 != null) {
                                try {
                                    a2.close();
                                } catch (Throwable th22) {
                                    th22.printStackTrace();
                                }
                            }
                            if (!(a == null || a.a())) {
                                try {
                                    a.close();
                                } catch (Throwable th222) {
                                    th222.printStackTrace();
                                }
                            }
                            return true;
                        } catch (IOException e2) {
                            e = e2;
                            bVar = a2;
                            gtVar = a;
                            outputStream = a4;
                            bVar2 = bVar;
                            try {
                                e.printStackTrace();
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (Throwable th2222) {
                                        th2222.printStackTrace();
                                    }
                                }
                                if (bVar2 != null) {
                                    try {
                                        bVar2.close();
                                    } catch (Throwable th22222) {
                                        th22222.printStackTrace();
                                    }
                                }
                                try {
                                    gtVar.close();
                                } catch (Throwable th222222) {
                                    th222222.printStackTrace();
                                }
                                return false;
                            } catch (Throwable th3) {
                                th222222 = th3;
                                gt gtVar2 = gtVar;
                                outputStream2 = outputStream;
                                a = gtVar2;
                                if (outputStream2 != null) {
                                    try {
                                        outputStream2.close();
                                    } catch (Throwable th4) {
                                        th4.printStackTrace();
                                    }
                                }
                                if (bVar2 != null) {
                                    try {
                                        bVar2.close();
                                    } catch (Throwable th5) {
                                        th5.printStackTrace();
                                    }
                                }
                                try {
                                    a.close();
                                } catch (Throwable th52) {
                                    th52.printStackTrace();
                                }
                                throw th222222;
                            }
                        } catch (Throwable th6) {
                            th222222 = th6;
                            bVar = a2;
                            outputStream2 = a4;
                            bVar2 = bVar;
                            if (outputStream2 != null) {
                                outputStream2.close();
                            }
                            if (bVar2 != null) {
                                bVar2.close();
                            }
                            a.close();
                            throw th222222;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        bVar = a2;
                        gtVar = a;
                        obj = bVar2;
                        bVar2 = bVar;
                        e.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (bVar2 != null) {
                            bVar2.close();
                        }
                        if (!(gtVar == null || gtVar.a())) {
                            gtVar.close();
                        }
                        return false;
                    } catch (Throwable th7) {
                        th222222 = th7;
                        bVar = a2;
                        obj2 = bVar2;
                        bVar2 = bVar;
                        if (outputStream2 != null) {
                            outputStream2.close();
                        }
                        if (bVar2 != null) {
                            bVar2.close();
                        }
                        if (!(a == null || a.a())) {
                            a.close();
                        }
                        throw th222222;
                    }
                }
                if (a2 != null) {
                    try {
                        a2.close();
                    } catch (Throwable th2222222) {
                        th2222222.printStackTrace();
                    }
                }
                if (!(a == null || a.a())) {
                    try {
                        a.close();
                    } catch (Throwable th22222222) {
                        th22222222.printStackTrace();
                    }
                }
                return false;
            } catch (IOException e4) {
                e = e4;
                gtVar = a;
                obj = bVar2;
                e.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bVar2 != null) {
                    bVar2.close();
                }
                gtVar.close();
                return false;
            } catch (Throwable th8) {
                th22222222 = th8;
                obj2 = bVar2;
                if (outputStream2 != null) {
                    outputStream2.close();
                }
                if (bVar2 != null) {
                    bVar2.close();
                }
                a.close();
                throw th22222222;
            }
        } catch (IOException e5) {
            e = e5;
            gtVar = bVar2;
            outputStream = bVar2;
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            if (bVar2 != null) {
                bVar2.close();
            }
            gtVar.close();
            return false;
        } catch (Throwable th9) {
            th22222222 = th9;
            a = bVar2;
            outputStream2 = bVar2;
            if (outputStream2 != null) {
                outputStream2.close();
            }
            if (bVar2 != null) {
                bVar2.close();
            }
            a.close();
            throw th22222222;
        }
    }

    public static boolean a(String[] strArr, String str) {
        if (strArr == null || str == null) {
            return false;
        }
        try {
            for (String trim : str.split("\n")) {
                if (b(strArr, trim.trim())) {
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    public static boolean b(String[] strArr, String str) {
        if (strArr == null || str == null) {
            return false;
        }
        try {
            for (String str2 : strArr) {
                str = str.trim();
                if (str.startsWith("at ") && str.contains(str2 + ".") && str.endsWith(")")) {
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void c(Context context) {
        try {
            e(context);
            if (a(context)) {
                synchronized (Looper.getMainLooper()) {
                    gc gcVar = new gc(context);
                    a(gcVar, b());
                    List a = gcVar.a(0, fm.a(b()));
                    if (!(a == null || a.size() == 0)) {
                        String a2 = a(a, context);
                        if (a2 == null) {
                        } else if (c(a2) == 1) {
                            a(a, gcVar, b());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            fl.a(th, "LogProcessor", "processUpdateLog");
        }
    }

    private boolean b(String str) {
        if (this.d == null) {
            return false;
        }
        boolean c;
        try {
            c = this.d.c(str);
        } catch (Throwable th) {
            fl.a(th, "LogUpdateProcessor", "deleteLogData");
            c = false;
        }
        return c;
    }

    protected String a() {
        return fm.c(this.b);
    }

    protected int b() {
        return this.b;
    }

    private void a(gc gcVar, int i) {
        try {
            a(gcVar.a(2, fm.a(i)), gcVar, i);
        } catch (Throwable th) {
            fl.a(th, "LogProcessor", "processDeleteFail");
        }
    }

    private int c(String str) {
        int i = 1;
        int i2 = 0;
        try {
            byte[] b = gx.a().b(new fn(fi.c(fi.a(str))));
            if (b == null) {
                return 0;
            }
            try {
                JSONObject jSONObject = new JSONObject(fi.a(b));
                String str2 = "code";
                if (jSONObject.has(str2)) {
                    i2 = jSONObject.getInt(str2);
                }
            } catch (Throwable e) {
                fl.a(e, "LogProcessor", "processUpdate");
                i2 = 1;
            }
            return i2;
        } catch (Throwable e2) {
            if (e2.b() == 27) {
                i = 0;
            }
            fl.a(e2, "LogProcessor", "processUpdate");
            i2 = i;
        }
    }

    private void a(List<? extends gd> list, gc gcVar, int i) {
        if (list != null && list.size() > 0) {
            for (gd gdVar : list) {
                if (b(gdVar.b())) {
                    gcVar.a(gdVar.b(), gdVar.getClass());
                } else {
                    gdVar.a(2);
                    gcVar.b(gdVar);
                }
            }
        }
    }

    public static String d(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("\"key\":\"").append(ey.f(context)).append("\",\"platform\":\"android\",\"diu\":\"").append(fc.q(context)).append("\",\"pkg\":\"").append(ey.c(context)).append("\",\"model\":\"").append(Build.MODEL).append("\",\"appname\":\"").append(ey.b(context)).append("\",\"appversion\":\"").append(ey.d(context)).append("\",\"sysversion\":\"").append(VERSION.RELEASE).append("\",");
        } catch (Throwable th) {
            fl.a(th, "CInfo", "getPublicJSONInfo");
        }
        return stringBuilder.toString();
    }

    private String g(Context context) {
        String d;
        try {
            d = d(context);
            if ("".equals(d)) {
                return null;
            }
            d = fb.b(context, fi.a(d));
            return d;
        } catch (Throwable th) {
            fl.a(th, "LogProcessor", "getPublicInfo");
            d = null;
        }
    }

    private String a(List<? extends gd> list, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(g(context)).append("\",\"els\":[");
        Object obj = 1;
        for (gd gdVar : list) {
            Object obj2;
            String d = d(gdVar.b());
            if (d == null || "".equals(d)) {
                obj2 = obj;
            } else {
                String str = d + "||" + gdVar.c();
                if (obj == null) {
                    stringBuilder.append(",");
                } else {
                    obj = null;
                }
                stringBuilder.append("{\"log\":\"").append(str).append("\"}");
                obj2 = obj;
            }
            obj = obj2;
        }
        if (obj != null) {
            return null;
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    private String d(String str) {
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable th;
        InputStream a;
        try {
            if (this.d == null) {
                return null;
            }
            b a2 = this.d.a(str);
            if (a2 == null) {
                return null;
            }
            a = a2.a(0);
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = a.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    String a3 = fi.a(byteArrayOutputStream.toByteArray());
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e) {
                            fl.a(e, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e2) {
                            fl.a(e2, "LogProcessor", "readLog2");
                        }
                    }
                    return a3;
                } catch (Throwable th2) {
                    th = th2;
                    try {
                        fl.a(th, "LogProcessor", "readLog");
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable th3) {
                                fl.a(th3, "LogProcessor", "readLog1");
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable th32) {
                                fl.a(th32, "LogProcessor", "readLog2");
                            }
                        }
                        return null;
                    } catch (Throwable th4) {
                        th32 = th4;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable e22) {
                                fl.a(e22, "LogProcessor", "readLog1");
                            }
                        }
                        if (a != null) {
                            try {
                                a.close();
                            } catch (Throwable e222) {
                                fl.a(e222, "LogProcessor", "readLog2");
                            }
                        }
                        throw th32;
                    }
                }
            } catch (Throwable th5) {
                th32 = th5;
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (a != null) {
                    a.close();
                }
                throw th32;
            }
        } catch (Throwable th6) {
            th32 = th6;
            byteArrayOutputStream = null;
            a = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            throw th32;
        }
    }

    void c() {
        try {
            if (this.d != null && !this.d.a()) {
                this.d.close();
            }
        } catch (Throwable e) {
            fl.a(e, "LogProcessor", "closeDiskLru");
        } catch (Throwable e2) {
            fl.a(e2, "LogProcessor", "closeDiskLru");
        }
    }

    private gt b(Context context, String str) {
        gt a;
        try {
            File file = new File(fm.a(context, str));
            if (!file.exists() && !file.mkdirs()) {
                return null;
            }
            a = gt.a(file, 1, 1, 20480);
            return a;
        } catch (Throwable e) {
            fl.a(e, "LogProcessor", "initDiskLru");
            a = null;
        } catch (Throwable e2) {
            fl.a(e2, "LogProcessor", "initDiskLru");
            a = null;
        }
    }

    public static String b(Throwable th) {
        return fi.a(th);
    }
}
