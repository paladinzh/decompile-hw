package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetAgreementRequest */
public class g extends a {
    private static byte[] p = new byte[256];
    private String h = (c() + "/IUserInfoMng/getAgreement");
    private String i = "1";
    private String j = "7";
    private String k;
    private String l;
    private String m = "0";
    private Context n;
    private String o;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetAgreementReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "agreementID", this.i);
            t.a(a, "agreementOldVer", this.m);
            t.a(a, "reqClientType", this.j);
            t.a(a, "languageCode", this.l);
            t.a(a, "countryCode", this.o);
            a.endTag(null, "GetAgreementReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("agreementID", this.i);
            bundle.putString("agreementOldVer", this.m);
            bundle.putString("reqClientType", this.j);
            bundle.putString("languageCode", this.l);
            bundle.putString("countryCode", this.o);
            com.huawei.hwid.core.c.b.a.b("GetAgreementRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"agreementContent".equals(name)) {
                            if (!"agreementVer".endsWith(name)) {
                                break;
                            }
                            this.m = a.nextText();
                            break;
                        }
                        this.k = a.nextText();
                        com.huawei.hwid.core.c.b.a.a("GetAgreementRequest", "mAgreementContent: " + this.k);
                        break;
                    } else if (!"errorCode".equals(name)) {
                        if (!"errorDesc".equals(name)) {
                            break;
                        }
                        this.d = a.nextText();
                        break;
                    } else {
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        break;
                    }
                default:
                    break;
            }
        }
        if (this.b == 0 && !TextUtils.isEmpty(this.k)) {
            byte[] a2 = a(this.k.getBytes("UTF-8"));
            String c = i.c(this.n, this.i);
            if (!TextUtils.isEmpty(this.m)) {
                c = c + "-" + this.m;
            }
            a(this.n, d.i(this.n), c + ".zip", a2);
        }
    }

    public String g() {
        return this.h;
    }

    public void f(String str) {
        this.l = str;
    }

    public void g(String str) {
        this.m = str;
    }

    private String w() {
        return this.m;
    }

    public void h(String str) {
        this.o = str;
    }

    public void i(String str) {
        this.i = str;
    }

    public Bundle h() {
        boolean z = false;
        Bundle h = super.h();
        h.putString("agreeVersion", w());
        String str = "isAgreementUpdate";
        if (!TextUtils.isEmpty(this.k)) {
            z = true;
        }
        h.putBoolean(str, z);
        return h;
    }

    private byte[] a(byte[] bArr) {
        int length = ((bArr.length + 3) / 4) * 3;
        if (bArr.length > 0 && bArr[bArr.length - 1] == (byte) 61) {
            length--;
        }
        if (bArr.length > 1 && bArr[bArr.length - 2] == (byte) 61) {
            length--;
        }
        byte[] bArr2 = new byte[length];
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        for (byte b : bArr) {
            byte b2 = p[b & 255];
            if (b2 >= (byte) 0) {
                int i4 = i2 << 6;
                i2 = i3 + 6;
                i3 = i4 | b2;
                if (i2 < 8) {
                    int i5 = i3;
                    i3 = i2;
                    i2 = i5;
                } else {
                    int i6 = i2 - 8;
                    i2 = i + 1;
                    bArr2[i] = (byte) ((byte) ((i3 >> i6) & 255));
                    i = i2;
                    i2 = i3;
                    i3 = i6;
                }
            }
        }
        if (i == bArr2.length) {
            return bArr2;
        }
        return new byte[0];
    }

    public boolean a(Context context, String str, String str2, byte[] bArr) {
        FileOutputStream fileOutputStream;
        Throwable e;
        Throwable th;
        FileOutputStream fileOutputStream2 = null;
        try {
            File file = new File(str);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            fileOutputStream = new FileOutputStream(new File(str + str2));
            try {
                fileOutputStream.write(bArr);
                fileOutputStream.close();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e2) {
                        com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "IOException / " + e2.toString(), e2);
                    }
                }
                return true;
            } catch (FileNotFoundException e3) {
                fileOutputStream2 = fileOutputStream;
                try {
                    com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "writeAgreement FileNotFoundException:");
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable e22) {
                            com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "IOException / " + e22.toString(), e22);
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    fileOutputStream = fileOutputStream2;
                    e22 = th;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable th22) {
                            com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "IOException / " + th22.toString(), th22);
                        }
                    }
                    throw e22;
                }
            } catch (IOException e4) {
                fileOutputStream2 = fileOutputStream;
                com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "writeAgreement IOException:");
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Throwable e222) {
                        com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "IOException / " + e222.toString(), e222);
                    }
                }
                return false;
            } catch (Throwable th3) {
                e222 = th3;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw e222;
            }
        } catch (FileNotFoundException e5) {
            com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "writeAgreement FileNotFoundException:");
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            return false;
        } catch (IOException e6) {
            com.huawei.hwid.core.c.b.a.d("GetAgreementRequest", "writeAgreement IOException:");
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            return false;
        } catch (Throwable th222) {
            th = th222;
            fileOutputStream = null;
            e222 = th;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw e222;
        }
    }

    static {
        int i;
        for (i = 0; i < 256; i++) {
            p[i] = (byte) -1;
        }
        for (i = 65; i <= 90; i++) {
            p[i] = (byte) ((byte) (i - 65));
        }
        for (i = 97; i <= 122; i++) {
            p[i] = (byte) ((byte) ((i + 26) - 97));
        }
        for (i = 48; i <= 57; i++) {
            p[i] = (byte) ((byte) ((i + 52) - 48));
        }
        p[43] = (byte) 62;
        p[47] = (byte) 63;
    }

    public g(Context context, Bundle bundle) {
        this.n = context;
        f(d.g(context));
        h(d.f(context));
        i(bundle.getString("termsOrPolicy"));
        a(true);
        g(bundle.getString("agreeVersion"));
    }
}
