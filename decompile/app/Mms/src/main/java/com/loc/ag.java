package com.loc;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.location.places.Place;
import com.loc.bi.b;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

/* compiled from: LogProcessor */
abstract class ag {
    private v a;
    private int b;
    private bl c;
    private bi d;

    /* compiled from: LogProcessor */
    class a implements bl {
        final /* synthetic */ ag a;
        private an b;

        a(ag agVar, an anVar) {
            this.a = agVar;
            this.b = anVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, this.a.b());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    protected ag(int i) {
        this.b = i;
    }

    private String a(Context context, v vVar) {
        return o.a(context, vVar);
    }

    private String a(Context context, String str) {
        try {
            return o.a(context, str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
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

    private String a(List<ap> list, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(g(context)).append("\",\"els\":[");
        Object obj = 1;
        Iterator it = list.iterator();
        while (true) {
            Object obj2 = obj;
            if (!it.hasNext()) {
                break;
            }
            ap apVar = (ap) it.next();
            String d = d(apVar.b());
            if (d == null) {
                obj = obj2;
            } else if ("".equals(d)) {
                obj = obj2;
            } else {
                String str = d + "||" + apVar.d();
                if (obj2 == null) {
                    stringBuilder.append(",");
                } else {
                    obj2 = null;
                }
                stringBuilder.append("{\"log\":\"").append(str).append("\"}");
                obj = obj2;
            }
        }
        if (obj2 != null) {
            return null;
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    private void a(an anVar, int i) {
        try {
            a(anVar.a(2, i), anVar, i);
        } catch (Throwable th) {
            aa.a(th, "LogProcessor", "processDeleteFail");
        }
    }

    private void a(an anVar, String str, String str2, int i, boolean z) {
        ap apVar = new ap();
        apVar.a(0);
        apVar.b(str);
        apVar.a(str2);
        anVar.b(apVar, i);
    }

    private void a(List<ap> list, an anVar, int i) {
        if (list != null && list.size() > 0) {
            for (ap apVar : list) {
                if (b(apVar.b())) {
                    anVar.a(apVar.b(), i);
                } else {
                    apVar.a(2);
                    anVar.a(apVar, apVar.a());
                }
            }
        }
    }

    private boolean a(Context context, String str, String str2, String str3, an anVar) {
        bi a;
        OutputStream a2;
        IOException e;
        b bVar;
        bi biVar;
        Object obj;
        OutputStream outputStream;
        Throwable th;
        OutputStream outputStream2;
        Object obj2;
        b bVar2 = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getFilesDir().getAbsolutePath());
            stringBuilder.append(af.a);
            stringBuilder.append(str2);
            File file = new File(stringBuilder.toString());
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            a = bi.a(file, 1, 1, 20480);
            try {
                a.a(a(anVar));
                b a3 = a.a(str);
                if (a3 == null) {
                    byte[] bytes;
                    com.loc.bi.a b;
                    try {
                        bytes = str3.getBytes("UTF-8");
                        b = a.b(str);
                        a2 = b.a(0);
                    } catch (IOException e2) {
                        e = e2;
                        bVar = a3;
                        biVar = a;
                        obj = bVar2;
                        bVar2 = bVar;
                        try {
                            e.printStackTrace();
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (Throwable th2) {
                                    th2.printStackTrace();
                                }
                            }
                            if (bVar2 != null) {
                                try {
                                    bVar2.close();
                                } catch (Throwable th22) {
                                    th22.printStackTrace();
                                }
                            }
                            try {
                                biVar.close();
                            } catch (Throwable th222) {
                                th222.printStackTrace();
                            }
                            return false;
                        } catch (Throwable th3) {
                            th222 = th3;
                            bi biVar2 = biVar;
                            outputStream2 = outputStream;
                            a = biVar2;
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
                            throw th222;
                        }
                    } catch (Throwable th6) {
                        th222 = th6;
                        bVar = a3;
                        obj2 = bVar2;
                        bVar2 = bVar;
                        if (outputStream2 != null) {
                            outputStream2.close();
                        }
                        if (bVar2 != null) {
                            bVar2.close();
                        }
                        a.close();
                        throw th222;
                    }
                    try {
                        a2.write(bytes);
                        b.a();
                        a.b();
                        if (a2 != null) {
                            try {
                                a2.close();
                            } catch (Throwable th2222) {
                                th2222.printStackTrace();
                            }
                        }
                        if (a3 != null) {
                            try {
                                a3.close();
                            } catch (Throwable th22222) {
                                th22222.printStackTrace();
                            }
                        }
                        if (!(a == null || a.a())) {
                            try {
                                a.close();
                            } catch (Throwable th222222) {
                                th222222.printStackTrace();
                            }
                        }
                        return true;
                    } catch (IOException e3) {
                        e = e3;
                        bVar = a3;
                        biVar = a;
                        outputStream = a2;
                        bVar2 = bVar;
                        e.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (bVar2 != null) {
                            bVar2.close();
                        }
                        biVar.close();
                        return false;
                    } catch (Throwable th7) {
                        th222222 = th7;
                        bVar = a3;
                        outputStream2 = a2;
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
                }
                if (a3 != null) {
                    try {
                        a3.close();
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
                biVar = a;
                obj = bVar2;
                e.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bVar2 != null) {
                    bVar2.close();
                }
                biVar.close();
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
            biVar = bVar2;
            outputStream = bVar2;
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            if (bVar2 != null) {
                bVar2.close();
            }
            if (!(biVar == null || biVar.a())) {
                biVar.close();
            }
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

    private bi b(Context context, String str) {
        bi a;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getFilesDir().getAbsolutePath());
            stringBuilder.append(af.a);
            stringBuilder.append(str);
            File file = new File(stringBuilder.toString());
            if (!file.exists() && !file.mkdirs()) {
                return null;
            }
            a = bi.a(file, 1, 1, 20480);
            return a;
        } catch (Throwable e) {
            aa.a(e, "LogProcessor", "initDiskLru");
            a = null;
            return a;
        } catch (Throwable e2) {
            aa.a(e2, "LogProcessor", "initDiskLru");
            a = null;
            return a;
        }
    }

    private boolean b(String str) {
        if (this.d == null) {
            return false;
        }
        boolean c;
        try {
            c = this.d.c(str);
        } catch (Throwable e) {
            aa.a(e, "LogUpdateProcessor", "deleteLogData");
            c = false;
            return c;
        } catch (Throwable e2) {
            aa.a(e2, "LogUpdateProcessor", "deleteLogData");
            c = false;
            return c;
        }
        return c;
    }

    private int c(String str) {
        byte[] b;
        int i = 1;
        try {
            b = w.b(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            b = w.b(str.getBytes());
        }
        try {
            byte[] b2 = bo.a().b(new ah(b));
            if (b2 == null) {
                return 0;
            }
            String str2;
            int i2;
            try {
                str2 = new String(b2, "UTF-8");
            } catch (UnsupportedEncodingException e2) {
                str2 = new String(b2);
            }
            try {
                JSONObject jSONObject = new JSONObject(str2);
                str2 = "code";
                i2 = !jSONObject.has(str2) ? 0 : jSONObject.getInt(str2);
            } catch (Throwable e3) {
                aa.a(e3, "LogProcessor", "processUpdate");
                i2 = i;
                return i2;
            }
            return i2;
        } catch (Throwable e32) {
            if (e32.a() == 27) {
                i = 0;
            }
            aa.a(e32, "LogProcessor", "processUpdate");
        }
    }

    private String c(Throwable th) {
        return th.toString();
    }

    private String d() {
        return w.a(new Date().getTime());
    }

    private String d(String str) {
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable e;
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
                    byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                    while (true) {
                        int read = a.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    String byteArrayOutputStream2 = byteArrayOutputStream.toString("utf-8");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e2) {
                            aa.a(e2, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e22) {
                            aa.a(e22, "LogProcessor", "readLog2");
                        }
                    }
                    return byteArrayOutputStream2;
                } catch (IOException e3) {
                    e = e3;
                    aa.a(e, "LogProcessor", "readLog");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e4) {
                            aa.a(e4, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e42) {
                            aa.a(e42, "LogProcessor", "readLog2");
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    e42 = th;
                    aa.a(e42, "LogProcessor", "readLog");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e422) {
                            aa.a(e422, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e4222) {
                            aa.a(e4222, "LogProcessor", "readLog2");
                        }
                    }
                    return null;
                }
            } catch (IOException e5) {
                e4222 = e5;
                byteArrayOutputStream = null;
                aa.a(e4222, "LogProcessor", "readLog");
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (a != null) {
                    a.close();
                }
                return null;
            } catch (Throwable th2) {
                e4222 = th2;
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable e222) {
                        aa.a(e222, "LogProcessor", "readLog1");
                    }
                }
                if (a != null) {
                    try {
                        a.close();
                    } catch (Throwable e2222) {
                        aa.a(e2222, "LogProcessor", "readLog2");
                    }
                }
                throw e4222;
            }
        } catch (IOException e6) {
            e4222 = e6;
            byteArrayOutputStream = null;
            a = null;
            aa.a(e4222, "LogProcessor", "readLog");
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            return null;
        } catch (Throwable th3) {
            e4222 = th3;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (a != null) {
                a.close();
            }
            throw e4222;
        }
    }

    private void d(Context context) {
        try {
            this.d = b(context, a());
        } catch (Throwable th) {
            aa.a(th, "LogProcessor", "LogUpDateProcessor");
        }
    }

    private List<v> e(Context context) {
        List<v> list = null;
        try {
            synchronized (Looper.getMainLooper()) {
                list = new aq(context, false).a();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return list;
    }

    private String f(Context context) {
        return m.a(context);
    }

    private String g(Context context) {
        String a;
        try {
            a = o.a(context);
            if ("".equals(a)) {
                return null;
            }
            a = o.c(context, a.getBytes("UTF-8"));
            return a;
        } catch (Throwable e) {
            aa.a(e, "LogProcessor", "getPublicInfo");
            a = null;
            return a;
        } catch (Throwable e2) {
            aa.a(e2, "LogProcessor", "getPublicInfo");
            a = null;
            return a;
        }
    }

    protected bl a(an anVar) {
        try {
            if (this.c == null) {
                this.c = new a(this, anVar);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this.c;
    }

    protected String a() {
        return af.b(this.b);
    }

    protected String a(String str) {
        return s.c(str);
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

    protected abstract String a(List<v> list);

    void a(Context context, Throwable th, String str, String str2) {
        List<v> e = e(context);
        if (e != null && e.size() != 0) {
            String a = a(th);
            if (a != null && !"".equals(a)) {
                for (v vVar : e) {
                    if (a(vVar.f(), a)) {
                        a(vVar, context, th, a, str, str2);
                        return;
                    }
                }
                if (a.contains("com.amap.api.col")) {
                    try {
                        a(new com.loc.v.a("collection", "1.0", "AMap_collection_1.0").a(new String[]{"com.amap.api.collection"}).a(), context, th, a, str, str2);
                    } catch (l e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    protected void a(v vVar) {
        this.a = vVar;
    }

    void a(v vVar, Context context, Throwable th, String str, String str2, String str3) {
        a(vVar);
        String d = d();
        String a = a(context, vVar);
        String f = f(context);
        String c = c(th);
        if (c != null && !"".equals(c)) {
            int b = b();
            StringBuilder stringBuilder = new StringBuilder();
            if (str2 != null) {
                stringBuilder.append("class:").append(str2);
            }
            if (str3 != null) {
                stringBuilder.append(" method:").append(str3).append("$").append("<br/>");
            }
            stringBuilder.append(str);
            String a2 = a(str);
            String a3 = a(f, a, d, b, c, stringBuilder.toString());
            if (a3 != null && !"".equals(a3)) {
                String a4 = a(context, a3);
                String a5 = a();
                synchronized (Looper.getMainLooper()) {
                    an anVar = new an(context);
                    a(anVar, vVar.a(), a2, b, a(context, a2, a5, a4, anVar));
                }
            }
        }
    }

    protected abstract boolean a(Context context);

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

    protected int b() {
        return this.b;
    }

    String b(Throwable th) {
        String a = w.a(th);
        return a == null ? null : a.replaceAll("\n", "<br/>");
    }

    void b(Context context) {
        List e = e(context);
        if (e != null && e.size() != 0) {
            String a = a(e);
            if (a != null && !"".equals(a)) {
                String d = d();
                String a2 = a(context, this.a);
                int b = b();
                String a3 = a(f(context), a2, d, b, "ANR", a);
                if (a3 != null && !"".equals(a3)) {
                    String a4 = a(a);
                    String a5 = a(context, a3);
                    String a6 = a();
                    synchronized (Looper.getMainLooper()) {
                        an anVar = new an(context);
                        a(anVar, this.a.a(), a4, b, a(context, a4, a6, a5, anVar));
                    }
                }
            }
        }
    }

    void c() {
        try {
            if (this.d != null && !this.d.a()) {
                this.d.close();
            }
        } catch (Throwable e) {
            aa.a(e, "LogProcessor", "closeDiskLru");
        } catch (Throwable e2) {
            aa.a(e2, "LogProcessor", "closeDiskLru");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void c(Context context) {
        try {
            d(context);
            if (a(context)) {
                synchronized (Looper.getMainLooper()) {
                    an anVar = new an(context);
                    a(anVar, b());
                    List a = anVar.a(0, b());
                    if (!(a == null || a.size() == 0)) {
                        String a2 = a(a, context);
                        if (a2 == null) {
                        } else if (c(a2) == 1) {
                            a(a, anVar, b());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            aa.a(th, "LogProcessor", "processUpdateLog");
        }
    }
}
