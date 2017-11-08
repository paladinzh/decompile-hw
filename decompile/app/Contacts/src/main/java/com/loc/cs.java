package com.loc;

import android.text.TextUtils;
import java.util.zip.CRC32;

/* compiled from: Req */
public class cs {
    public String A = null;
    public String B = null;
    public String C = null;
    public String D = null;
    public String E = null;
    public String F = null;
    public byte[] G = null;
    public String a = CallInterceptDetails.BRANDED_STATE;
    public short b = (short) 0;
    public String c = null;
    public String d = null;
    public String e = null;
    public String f = null;
    public String g = null;
    public String h = null;
    public String i = null;
    public String j = null;
    public String k = null;
    public String l = null;
    public String m = null;
    public String n = null;
    public String o = null;
    public String p = null;
    public String q = null;
    public String r = null;
    public String s = null;
    public String t = null;
    public String u = null;
    public String v = null;
    public String w = null;
    public String x = null;
    public String y = null;
    public String z = null;

    private String a(String str, int i) {
        String[] split = this.B.split("\\*")[i].split(",");
        return !str.equals("lac") ? !str.equals("cellid") ? !str.equals("signal") ? null : split[2] : split[1] : split[0];
    }

    public static void a(byte[] bArr, int i) {
    }

    private byte[] b(String str) {
        String[] split = str.split(":");
        if (split == null || split.length != 6) {
            String[] strArr = new String[6];
            for (int i = 0; i < strArr.length; i++) {
                strArr[i] = "0";
            }
            split = strArr;
        }
        byte[] bArr = new byte[6];
        for (int i2 = 0; i2 < split.length; i2++) {
            if (split[i2].length() > 2) {
                split[i2] = split[i2].substring(0, 2);
            }
            bArr[i2] = (byte) ((byte) Integer.parseInt(split[i2], 16));
        }
        return bArr;
    }

    public String a(String str) {
        if (!this.A.contains(str + ">")) {
            return "0";
        }
        int indexOf = this.A.indexOf(str + ">");
        return this.A.substring((indexOf + str.length()) + 1, this.A.indexOf("</" + str));
    }

    public byte[] a() {
        b();
        int i = 3072;
        if (this.G != null) {
            i = (this.G.length + 1) + 3072;
        }
        Object obj = new byte[i];
        obj[0] = (byte) Byte.parseByte(this.a);
        Object b = cw.b(this.b);
        System.arraycopy(b, 0, obj, 1, b.length);
        int length = b.length + 1;
        try {
            b = this.c.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th) {
            e.a(th, "Req", "buildV4Dot2");
            obj[length] = null;
            length++;
        }
        try {
            b = this.d.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th2) {
            e.a(th2, "Req", "buildV4Dot21");
            obj[length] = null;
            length++;
        }
        try {
            b = this.o.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th22) {
            e.a(th22, "Req", "buildV4Dot22");
            obj[length] = null;
            length++;
        }
        try {
            b = this.e.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th222) {
            e.a(th222, "Req", "buildV4Dot23");
            obj[length] = null;
            length++;
        }
        try {
            b = this.f.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th2222) {
            e.a(th2222, "Req", "buildV4Dot24");
            obj[length] = null;
            length++;
        }
        try {
            b = this.g.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th22222) {
            e.a(th22222, "Req", "buildV4Dot25");
            obj[length] = null;
            length++;
        }
        try {
            b = this.u.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th222222) {
            e.a(th222222, "Req", "buildV4Dot26");
            obj[length] = null;
            length++;
        }
        try {
            b = this.h.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th2222222) {
            e.a(th2222222, "Req", "buildV4Dot27");
            obj[length] = null;
            length++;
        }
        try {
            b = this.p.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th22222222) {
            e.a(th22222222, "Req", "buildV4Dot28");
            obj[length] = null;
            length++;
        }
        try {
            b = this.q.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            i = b.length + length;
        } catch (Throwable th222222222) {
            e.a(th222222222, "Req", "buildV4Dot29");
            obj[length] = null;
            i = length + 1;
        }
        if (TextUtils.isEmpty(this.t)) {
            obj[i] = null;
            length = i + 1;
        } else {
            Object b2 = b(this.t);
            obj[i] = (byte) ((byte) b2.length);
            i++;
            System.arraycopy(b2, 0, obj, i, b2.length);
            length = b2.length + i;
        }
        try {
            b = this.v.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th2222222222) {
            e.a(th2222222222, "Req", "buildV4Dot211");
            obj[length] = null;
            length++;
        }
        try {
            b = this.w.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        } catch (Throwable th22222222222) {
            e.a(th22222222222, "Req", "buildV4Dot212");
            obj[length] = null;
            length++;
        }
        try {
            b = this.x.getBytes("GBK");
            obj[length] = (byte) ((byte) b.length);
            length++;
            System.arraycopy(b, 0, obj, length, b.length);
            i = b.length + length;
        } catch (Throwable th222222222222) {
            e.a(th222222222222, "Req", "buildV4Dot213");
            obj[length] = null;
            i = length + 1;
        }
        obj[i] = (byte) Byte.parseByte(this.y);
        i++;
        obj[i] = (byte) Byte.parseByte(this.j);
        i++;
        if (this.j.equals(CallInterceptDetails.BRANDED_STATE)) {
            obj[i] = (byte) Byte.parseByte(this.k);
            i++;
        }
        if (this.j.equals(CallInterceptDetails.BRANDED_STATE) || this.j.equals(CallInterceptDetails.UNBRANDED_STATE)) {
            b2 = cw.c(Integer.parseInt(this.l));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
        }
        if (this.j.equals(CallInterceptDetails.BRANDED_STATE) || this.j.equals(CallInterceptDetails.UNBRANDED_STATE)) {
            b2 = cw.c(Integer.parseInt(this.m));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
        }
        if (this.j.equals(CallInterceptDetails.BRANDED_STATE) || this.j.equals(CallInterceptDetails.UNBRANDED_STATE)) {
            b2 = cw.e(this.n);
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
        }
        obj[i] = (byte) Byte.parseByte(this.z);
        i++;
        if (this.z.equals(CallInterceptDetails.BRANDED_STATE)) {
            b2 = cw.e(a("mcc"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.e(a("mnc"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.e(a("lac"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.f(a("cellid"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            length = b2.length + i;
            i = Integer.parseInt(a("signal"));
            if (i > 127) {
                i = 0;
            }
            obj[length] = (byte) ((byte) i);
            i = length + 1;
            if (this.B.length() != 0) {
                int length2 = this.B.split("\\*").length;
                obj[i] = (byte) ((byte) length2);
                i++;
                length = 0;
                while (length < length2) {
                    Object e = cw.e(a("lac", length));
                    System.arraycopy(e, 0, obj, i, e.length);
                    i += e.length;
                    e = cw.f(a("cellid", length));
                    System.arraycopy(e, 0, obj, i, e.length);
                    int length3 = e.length + i;
                    i = Integer.parseInt(a("signal", length));
                    if (i > 127) {
                        i = 0;
                    }
                    obj[length3] = (byte) ((byte) i);
                    length++;
                    i = length3 + 1;
                }
            } else {
                obj[i] = null;
                i++;
            }
        } else if (this.z.equals(CallInterceptDetails.UNBRANDED_STATE)) {
            b2 = cw.e(a("mcc"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.e(a("sid"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.e(a("nid"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.e(a("bid"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.f(a("lon"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            i += b2.length;
            b2 = cw.f(a("lat"));
            System.arraycopy(b2, 0, obj, i, b2.length);
            length = b2.length + i;
            i = Integer.parseInt(a("signal"));
            if (i > 127) {
                i = 0;
            }
            obj[length] = (byte) ((byte) i);
            i = length + 1;
            obj[i] = null;
            i++;
        }
        if (this.C.length() != 0) {
            String[] split;
            obj[i] = 1;
            length = i + 1;
            try {
                split = this.C.split(",");
                b = b(split[0]);
                System.arraycopy(b, 0, obj, length, b.length);
                length += b.length;
                b = split[2].getBytes("GBK");
                obj[length] = (byte) ((byte) b.length);
                length++;
                System.arraycopy(b, 0, obj, length, b.length);
                length += b.length;
            } catch (Throwable th2222222222222) {
                e.a(th2222222222222, "Req", "buildV4Dot216");
                b = b("00:00:00:00:00:00");
                System.arraycopy(b, 0, obj, length, b.length);
                i = b.length + length;
                obj[i] = null;
                i++;
                obj[i] = (byte) Byte.parseByte("0");
            }
            i = Integer.parseInt(split[1]);
            if (i > 127) {
                i = 0;
            }
            obj[length] = (byte) Byte.parseByte(String.valueOf(i));
            i = length + 1;
        } else {
            obj[i] = null;
            i++;
        }
        String[] split2 = this.D.split("\\*");
        if (TextUtils.isEmpty(this.D) || split2.length == 0) {
            obj[i] = null;
            length = i + 1;
        } else {
            obj[i] = (byte) ((byte) split2.length);
            length = i + 1;
            for (String split3 : split2) {
                String[] split4 = split3.split(",");
                e = b(split4[0]);
                System.arraycopy(e, 0, obj, length, e.length);
                length3 = e.length + length;
                try {
                    b2 = split4[2].getBytes("GBK");
                    obj[length3] = (byte) ((byte) b2.length);
                    length3++;
                    System.arraycopy(b2, 0, obj, length3, b2.length);
                    length = b2.length + length3;
                } catch (Throwable th3) {
                    e.a(th3, "Req", "buildV4Dot217");
                    obj[length3] = null;
                    length = length3 + 1;
                }
                length3 = Integer.parseInt(split4[1]);
                if (length3 > 127) {
                    length3 = 0;
                }
                obj[length] = (byte) Byte.parseByte(String.valueOf(length3));
                length++;
            }
            b = cw.b(Integer.parseInt(this.E));
            System.arraycopy(b, 0, obj, length, b.length);
            length += b.length;
        }
        try {
            b = this.F.getBytes("GBK");
            if (b.length > 127) {
                b = null;
            }
            CRC32 crc32;
            byte[] bArr;
            if (b != null) {
                obj[length] = (byte) ((byte) b.length);
                length++;
                System.arraycopy(b, 0, obj, length, b.length);
                i = b.length + length;
                length = this.G != null ? 0 : this.G.length;
                e = cw.b(length);
                System.arraycopy(e, 0, obj, i, e.length);
                i += e.length;
                if (length > 0) {
                    System.arraycopy(this.G, 0, obj, i, this.G.length);
                    i += this.G.length;
                }
                b2 = new byte[i];
                System.arraycopy(obj, 0, b2, 0, i);
                crc32 = new CRC32();
                crc32.update(b2);
                e = cw.a(crc32.getValue());
                bArr = new byte[(e.length + i)];
                System.arraycopy(b2, 0, bArr, 0, i);
                System.arraycopy(e, 0, bArr, i, e.length);
                i = e.length;
                a(bArr, 0);
                return bArr;
            }
            obj[length] = (byte) 0;
            i = length + 1;
            if (this.G != null) {
            }
            e = cw.b(length);
            System.arraycopy(e, 0, obj, i, e.length);
            i += e.length;
            if (length > 0) {
                System.arraycopy(this.G, 0, obj, i, this.G.length);
                i += this.G.length;
            }
            b2 = new byte[i];
            System.arraycopy(obj, 0, b2, 0, i);
            crc32 = new CRC32();
            crc32.update(b2);
            e = cw.a(crc32.getValue());
            bArr = new byte[(e.length + i)];
            System.arraycopy(b2, 0, bArr, 0, i);
            System.arraycopy(e, 0, bArr, i, e.length);
            i = e.length;
            a(bArr, 0);
            return bArr;
        } catch (Throwable th22222222222222) {
            e.a(th22222222222222, "Req", "buildV4Dot218");
            obj[length] = null;
        }
    }

    public void b() {
        String str;
        if (TextUtils.isEmpty(this.a)) {
            this.a = "";
        }
        if (TextUtils.isEmpty(this.c)) {
            this.c = "";
        }
        if (TextUtils.isEmpty(this.d)) {
            this.d = "";
        }
        if (TextUtils.isEmpty(this.e)) {
            this.e = "";
        }
        if (TextUtils.isEmpty(this.f)) {
            this.f = "";
        }
        if (TextUtils.isEmpty(this.g)) {
            this.g = "";
        }
        if (TextUtils.isEmpty(this.h)) {
            this.h = "";
        }
        if (TextUtils.isEmpty(this.i)) {
            this.i = "";
        }
        if (TextUtils.isEmpty(this.j)) {
            str = "0";
            this.j = str;
        } else if (!(this.j.equals(CallInterceptDetails.BRANDED_STATE) || this.j.equals(CallInterceptDetails.UNBRANDED_STATE))) {
            str = "0";
            this.j = str;
        }
        if (TextUtils.isEmpty(this.k)) {
            str = "0";
            this.k = str;
        } else if (!(this.k.equals("0") || this.k.equals(CallInterceptDetails.BRANDED_STATE))) {
            str = "0";
            this.k = str;
        }
        if (TextUtils.isEmpty(this.l)) {
            this.l = "";
        }
        if (TextUtils.isEmpty(this.m)) {
            this.m = "";
        }
        if (TextUtils.isEmpty(this.n)) {
            this.n = "";
        }
        if (TextUtils.isEmpty(this.o)) {
            this.o = "";
        }
        if (TextUtils.isEmpty(this.p)) {
            this.p = "";
        }
        if (TextUtils.isEmpty(this.q)) {
            this.q = "";
        }
        if (TextUtils.isEmpty(this.r)) {
            this.r = "";
        }
        if (TextUtils.isEmpty(this.s)) {
            this.s = "";
        }
        if (TextUtils.isEmpty(this.t)) {
            this.t = "";
        }
        if (TextUtils.isEmpty(this.u)) {
            this.u = "";
        }
        if (TextUtils.isEmpty(this.v)) {
            this.v = "";
        }
        if (TextUtils.isEmpty(this.w)) {
            this.w = "";
        }
        if (TextUtils.isEmpty(this.x)) {
            this.x = "";
        }
        if (TextUtils.isEmpty(this.y)) {
            str = "0";
            this.y = str;
        } else if (!(this.y.equals(CallInterceptDetails.BRANDED_STATE) || this.y.equals(CallInterceptDetails.UNBRANDED_STATE))) {
            str = "0";
            this.y = str;
        }
        if (TextUtils.isEmpty(this.z)) {
            str = "0";
            this.z = str;
        } else if (!(this.z.equals(CallInterceptDetails.BRANDED_STATE) || this.z.equals(CallInterceptDetails.UNBRANDED_STATE))) {
            str = "0";
            this.z = str;
        }
        if (TextUtils.isEmpty(this.A)) {
            this.A = "";
        }
        if (TextUtils.isEmpty(this.B)) {
            this.B = "";
        }
        if (TextUtils.isEmpty(this.C)) {
            this.C = "";
        }
        if (TextUtils.isEmpty(this.D)) {
            this.D = "";
        }
        if (TextUtils.isEmpty(this.E)) {
            this.E = "";
        }
        if (TextUtils.isEmpty(this.F)) {
            this.F = "";
        }
        if (this.G == null) {
            this.G = new byte[0];
        }
    }
}
