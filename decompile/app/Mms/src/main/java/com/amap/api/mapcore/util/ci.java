package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import com.amap.api.mapcore.util.cx.b;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;

/* compiled from: LogProcessor */
abstract class ci {
    private bv a;
    private int b;
    private da c;
    private cx d;

    /* compiled from: LogProcessor */
    class a implements da {
        final /* synthetic */ ci a;
        private cs b;

        a(ci ciVar, cs csVar) {
            this.a = ciVar;
            this.b = csVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, cc.a(this.a.b()));
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    protected abstract String a(List<bv> list);

    protected abstract boolean a(Context context);

    protected ci(int i) {
        this.b = i;
    }

    private void d(Context context) {
        try {
            this.d = b(context, a());
        } catch (Throwable th) {
            cb.a(th, "LogProcessor", "LogUpDateProcessor");
        }
    }

    void a(bv bvVar, Context context, Throwable th, String str, String str2, String str3) {
        a(bvVar);
        String d = d();
        String a = bn.a(context, bvVar);
        String a2 = bl.a(context);
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
            String a3 = a(str);
            String a4 = a(a2, a, d, b, c, stringBuilder.toString());
            if (a4 != null && !"".equals(a4)) {
                String a5 = a(context, a4);
                String a6 = a();
                synchronized (Looper.getMainLooper()) {
                    cs csVar = new cs(context);
                    a(csVar, bvVar.a(), a3, b, a(context, a3, a6, a5, csVar));
                }
            }
        }
    }

    void a(Context context, Throwable th, String str, String str2) {
        List<bv> e = e(context);
        if (e != null && e.size() != 0) {
            String a = a(th);
            if (a != null && !"".equals(a)) {
                for (bv bvVar : e) {
                    if (a(bvVar.e(), a)) {
                        a(bvVar, context, th, a, str, str2);
                        return;
                    }
                }
                if (a.contains("com.amap.api.col")) {
                    try {
                        a(new com.amap.api.mapcore.util.bv.a("collection", "1.0", "AMap_collection_1.0").a(new String[]{"com.amap.api.collection"}).a(), context, th, a, str, str2);
                    } catch (bk e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    void b(Context context) {
        List e = e(context);
        if (e != null && e.size() != 0) {
            String a = a(e);
            if (a != null && !"".equals(a)) {
                String d = d();
                String a2 = bn.a(context, this.a);
                int b = b();
                String a3 = a(bl.a(context), a2, d, b, "ANR", a);
                if (a3 != null && !"".equals(a3)) {
                    String a4 = a(a);
                    String a5 = a(context, a3);
                    String a6 = a();
                    synchronized (Looper.getMainLooper()) {
                        cs csVar = new cs(context);
                        a(csVar, this.a.a(), a4, b, a(context, a4, a6, a5, csVar));
                    }
                }
            }
        }
    }

    protected void a(bv bvVar) {
        this.a = bvVar;
    }

    private List<bv> e(Context context) {
        List<bv> list = null;
        try {
            synchronized (Looper.getMainLooper()) {
                list = new cu(context, false).a();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return list;
    }

    private void a(cs csVar, String str, String str2, int i, boolean z) {
        ct b = cc.b(i);
        b.a(0);
        b.b(str);
        b.a(str2);
        csVar.a(b);
    }

    protected String a(String str) {
        return bs.c(str);
    }

    protected da a(cs csVar) {
        try {
            if (this.c == null) {
                this.c = new a(this, csVar);
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
            return bn.a(context, bx.a(str));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private String d() {
        return bx.a(new Date().getTime());
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

    private boolean a(Context context, String str, String str2, String str3, cs csVar) {
        cx a;
        Throwable th;
        IOException e;
        b bVar;
        cx cxVar;
        OutputStream outputStream;
        OutputStream outputStream2;
        Object obj;
        Object obj2;
        b bVar2 = null;
        try {
            File file = new File(cc.a(context, str2));
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            a = cx.a(file, 1, 1, 20480);
            try {
                a.a(a(csVar));
                b a2 = a.a(str);
                if (a2 == null) {
                    try {
                        byte[] a3 = bx.a(str3);
                        com.amap.api.mapcore.util.cx.a b = a.b(str);
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
                            cxVar = a;
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
                                    cxVar.close();
                                } catch (Throwable th222222) {
                                    th222222.printStackTrace();
                                }
                                return false;
                            } catch (Throwable th3) {
                                th222222 = th3;
                                cx cxVar2 = cxVar;
                                outputStream2 = outputStream;
                                a = cxVar2;
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
                        cxVar = a;
                        obj = bVar2;
                        bVar2 = bVar;
                        e.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (bVar2 != null) {
                            bVar2.close();
                        }
                        if (!(cxVar == null || cxVar.a())) {
                            cxVar.close();
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
                cxVar = a;
                obj = bVar2;
                e.printStackTrace();
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bVar2 != null) {
                    bVar2.close();
                }
                cxVar.close();
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
            cxVar = bVar2;
            outputStream = bVar2;
            e.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            if (bVar2 != null) {
                bVar2.close();
            }
            cxVar.close();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void c(Context context) {
        try {
            d(context);
            if (a(context)) {
                synchronized (Looper.getMainLooper()) {
                    cs csVar = new cs(context);
                    a(csVar, b());
                    List a = csVar.a(0, cc.a(b()));
                    if (!(a == null || a.size() == 0)) {
                        String a2 = a(a, context);
                        if (a2 == null) {
                        } else if (c(a2) == 1) {
                            a(a, csVar, b());
                        }
                    }
                }
            }
        } catch (Throwable th) {
            cb.a(th, "LogProcessor", "processUpdateLog");
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
            cb.a(e, "LogUpdateProcessor", "deleteLogData");
            c = false;
        } catch (Throwable e2) {
            cb.a(e2, "LogUpdateProcessor", "deleteLogData");
            c = false;
        }
        return c;
    }

    protected String a() {
        return cc.c(this.b);
    }

    protected int b() {
        return this.b;
    }

    private void a(cs csVar, int i) {
        try {
            a(csVar.a(2, cc.a(i)), csVar, i);
        } catch (Throwable th) {
            cb.a(th, "LogProcessor", "processDeleteFail");
        }
    }

    private int c(String str) {
        int i = 1;
        int i2 = 0;
        try {
            byte[] b = dd.a().b(new cd(bx.c(bx.a(str))));
            if (b == null) {
                return 0;
            }
            try {
                JSONObject jSONObject = new JSONObject(bx.a(b));
                String str2 = "code";
                if (jSONObject.has(str2)) {
                    i2 = jSONObject.getInt(str2);
                }
            } catch (Throwable e) {
                cb.a(e, "LogProcessor", "processUpdate");
                i2 = 1;
            }
            return i2;
        } catch (Throwable e2) {
            if (e2.b() == 27) {
                i = 0;
            }
            cb.a(e2, "LogProcessor", "processUpdate");
            i2 = i;
        }
    }

    private void a(List<? extends ct> list, cs csVar, int i) {
        if (list != null && list.size() > 0) {
            for (ct ctVar : list) {
                if (b(ctVar.b())) {
                    csVar.a(ctVar.b(), ctVar.getClass());
                } else {
                    ctVar.a(2);
                    csVar.b(ctVar);
                }
            }
        }
    }

    private String f(Context context) {
        String a;
        try {
            a = bn.a(context);
            if ("".equals(a)) {
                return null;
            }
            a = bn.d(context, bx.a(a));
            return a;
        } catch (Throwable th) {
            cb.a(th, "LogProcessor", "getPublicInfo");
            a = null;
        }
    }

    private String a(List<? extends ct> list, Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"pinfo\":\"").append(f(context)).append("\",\"els\":[");
        Object obj = 1;
        for (ct ctVar : list) {
            Object obj2;
            String d = d(ctVar.b());
            if (d == null || "".equals(d)) {
                obj2 = obj;
            } else {
                String str = d + "||" + ctVar.c();
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
        InputStream a;
        Throwable e;
        ByteArrayOutputStream byteArrayOutputStream;
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
                    String a3 = bx.a(byteArrayOutputStream.toByteArray());
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e2) {
                            cb.a(e2, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e22) {
                            cb.a(e22, "LogProcessor", "readLog2");
                        }
                    }
                    return a3;
                } catch (IOException e3) {
                    e = e3;
                    cb.a(e, "LogProcessor", "readLog");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e4) {
                            cb.a(e4, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e42) {
                            cb.a(e42, "LogProcessor", "readLog2");
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    e42 = th;
                    cb.a(e42, "LogProcessor", "readLog");
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e422) {
                            cb.a(e422, "LogProcessor", "readLog1");
                        }
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Throwable e4222) {
                            cb.a(e4222, "LogProcessor", "readLog2");
                        }
                    }
                    return null;
                }
            } catch (IOException e5) {
                e4222 = e5;
                byteArrayOutputStream = null;
                cb.a(e4222, "LogProcessor", "readLog");
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
                        cb.a(e222, "LogProcessor", "readLog1");
                    }
                }
                if (a != null) {
                    try {
                        a.close();
                    } catch (Throwable e2222) {
                        cb.a(e2222, "LogProcessor", "readLog2");
                    }
                }
                throw e4222;
            }
        } catch (IOException e6) {
            e4222 = e6;
            byteArrayOutputStream = null;
            a = null;
            cb.a(e4222, "LogProcessor", "readLog");
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

    void c() {
        try {
            if (this.d != null && !this.d.a()) {
                this.d.close();
            }
        } catch (Throwable e) {
            cb.a(e, "LogProcessor", "closeDiskLru");
        } catch (Throwable e2) {
            cb.a(e2, "LogProcessor", "closeDiskLru");
        }
    }

    private cx b(Context context, String str) {
        cx a;
        try {
            File file = new File(cc.a(context, str));
            if (!file.exists() && !file.mkdirs()) {
                return null;
            }
            a = cx.a(file, 1, 1, 20480);
            return a;
        } catch (Throwable e) {
            cb.a(e, "LogProcessor", "initDiskLru");
            a = null;
        } catch (Throwable e2) {
            cb.a(e2, "LogProcessor", "initDiskLru");
            a = null;
        }
    }

    String b(Throwable th) {
        String a = bx.a(th);
        if (a == null) {
            return null;
        }
        return a.replaceAll("\n", "<br/>");
    }
}
