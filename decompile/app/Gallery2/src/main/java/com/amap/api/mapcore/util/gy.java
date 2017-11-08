package com.amap.api.mapcore.util;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/* compiled from: BinaryRequest */
public abstract class gy extends hd {
    protected Context a;
    protected fh b;

    public abstract byte[] d();

    public abstract byte[] e();

    public boolean h() {
        return true;
    }

    public gy(Context context, fh fhVar) {
        if (context != null) {
            this.a = context.getApplicationContext();
        }
        this.b = fhVar;
    }

    public Map<String, String> b() {
        String f = ey.f(this.a);
        String a = fb.a();
        String a2 = fb.a(this.a, a, "key=" + f);
        Map<String, String> hashMap = new HashMap();
        hashMap.put("ts", a);
        hashMap.put("key", f);
        hashMap.put("scode", a2);
        return hashMap;
    }

    public final byte[] g() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(m());
            byteArrayOutputStream.write(i());
            byteArrayOutputStream.write(n());
            byteArrayOutputStream.write(o());
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
            } catch (Throwable th) {
                fl.a(th, "BinaryRequest", "getEntityBytes");
            }
            return toByteArray;
        } catch (Throwable th2) {
            fl.a(th2, "BinaryRequest", "getEntityBytes");
        }
        return null;
    }

    private byte[] m() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(fi.a("PANDORA$"));
            byteArrayOutputStream.write(new byte[]{(byte) 1});
            byteArrayOutputStream.write(new byte[]{(byte) 0});
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
            } catch (Throwable th) {
                fl.a(th, "BinaryRequest", "getBinaryHead");
            }
            return toByteArray;
        } catch (Throwable th2) {
            fl.a(th2, "BinaryRequest", "getBinaryHead");
        }
        return null;
    }

    public byte[] i() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] a;
            byteArrayOutputStream.write(new byte[]{(byte) 3});
            if (h()) {
                a = fb.a(this.a, false);
                byteArrayOutputStream.write(a(a));
                byteArrayOutputStream.write(a);
            } else {
                byteArrayOutputStream.write(new byte[]{(byte) 0, (byte) 0});
            }
            a = fi.a(f());
            if (a != null) {
                if (a.length > 0) {
                    byteArrayOutputStream.write(a(a));
                    byteArrayOutputStream.write(a);
                    a = fi.a(j());
                    if (a != null) {
                        if (a.length > 0) {
                            byteArrayOutputStream.write(a(a));
                            byteArrayOutputStream.write(a);
                            a = byteArrayOutputStream.toByteArray();
                            byteArrayOutputStream.close();
                            return a;
                        }
                    }
                    byteArrayOutputStream.write(new byte[]{(byte) 0, (byte) 0});
                    a = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                    return a;
                }
            }
            byteArrayOutputStream.write(new byte[]{(byte) 0, (byte) 0});
            a = fi.a(j());
            if (a != null) {
                if (a.length > 0) {
                    byteArrayOutputStream.write(a(a));
                    byteArrayOutputStream.write(a);
                    a = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                    return a;
                }
            }
            byteArrayOutputStream.write(new byte[]{(byte) 0, (byte) 0});
            a = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
            } catch (Throwable th) {
                fl.a(th, "BinaryRequest", "getRequestEncryptData");
            }
            return a;
        } catch (Throwable th2) {
            fl.a(th2, "BinaryRequest", "getRequestEncryptData");
        }
        return new byte[]{(byte) 0};
    }

    public String j() {
        return String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{this.b.c(), this.b.a()});
    }

    protected String f() {
        return "2.1";
    }

    protected byte[] a(byte[] bArr) {
        byte length = (byte) (bArr.length % 256);
        return new byte[]{(byte) ((byte) (bArr.length / 256)), (byte) length};
    }

    private byte[] n() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] d = d();
            if (d != null) {
                if (d.length != 0) {
                    byteArrayOutputStream.write(new byte[]{(byte) 1});
                    byteArrayOutputStream.write(a(d));
                    byteArrayOutputStream.write(d);
                    d = byteArrayOutputStream.toByteArray();
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable th) {
                        fl.a(th, "BinaryRequest", "getRequestRawData");
                    }
                    return d;
                }
            }
            byteArrayOutputStream.write(new byte[]{(byte) 0});
            d = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
            } catch (Throwable th2) {
                fl.a(th2, "BinaryRequest", "getRequestRawData");
            }
            return d;
        } catch (Throwable th3) {
            fl.a(th3, "BinaryRequest", "getRequestRawData");
        }
        return new byte[]{(byte) 0};
    }

    private byte[] o() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] e = e();
            if (e != null) {
                if (e.length != 0) {
                    byteArrayOutputStream.write(new byte[]{(byte) 1});
                    e = fb.a(this.a, e);
                    byteArrayOutputStream.write(a(e));
                    byteArrayOutputStream.write(e);
                    e = byteArrayOutputStream.toByteArray();
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable th) {
                        fl.a(th, "BinaryRequest", "getRequestEncryptData");
                    }
                    return e;
                }
            }
            byteArrayOutputStream.write(new byte[]{(byte) 0});
            e = byteArrayOutputStream.toByteArray();
            try {
                byteArrayOutputStream.close();
            } catch (Throwable th2) {
                fl.a(th2, "BinaryRequest", "getRequestEncryptData");
            }
            return e;
        } catch (Throwable th3) {
            fl.a(th3, "BinaryRequest", "getRequestEncryptData");
        }
        return new byte[]{(byte) 0};
    }
}
