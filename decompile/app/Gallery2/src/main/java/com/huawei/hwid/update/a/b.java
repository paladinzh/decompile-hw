package com.huawei.hwid.update.a;

import android.content.Context;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.update.c;
import com.huawei.hwid.update.j;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class b {
    private int a;
    private String b = null;
    private String c = null;
    private String d = null;
    private String e = null;
    private String f = null;
    private String g = null;
    private String h = null;
    private String i = null;
    private String j = null;
    private String k = null;
    private String l = null;
    private String m = null;
    private String n = null;
    private String o = null;
    private String p = null;
    private String q = null;
    private String r = "";
    private int s;
    private boolean t = false;
    private boolean u = false;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(InputStream inputStream) {
        InputStream inputStream2 = null;
        try {
            inputStream2 = j.a(inputStream);
            XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setInput(inputStream2, XmlUtils.INPUT_ENCODING);
            String str = "";
            e.b("NewVersionInfo", "pullParser: " + newPullParser.toString());
            for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                String name = newPullParser.getName();
                if (eventType == 2) {
                    a(newPullParser, name);
                }
            }
            j.b(inputStream2, "NewVersionInfo");
        } catch (XmlPullParserException e) {
            e.d("NewVersionInfo", "parse filelist to versioninfo error");
            j.b(inputStream2, "NewVersionInfo");
        } catch (IOException e2) {
            e.d("NewVersionInfo", "parse filelist to versioninfo error");
            j.b(inputStream2, "NewVersionInfo");
        } catch (Throwable th) {
            Throwable th2 = th;
            InputStream inputStream3 = inputStream2;
            Throwable th3 = th2;
            j.b(inputStream3, "NewVersionInfo");
            throw th3;
        }
    }

    public void a(Context context, InputStream inputStream) {
        InputStream a;
        Throwable th;
        Throwable th2;
        InputStream inputStream2 = null;
        try {
            a = j.a(inputStream);
            try {
                XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
                newPullParser.setInput(a, XmlUtils.INPUT_ENCODING);
                String a2 = j.a(context);
                StringBuffer stringBuffer = new StringBuffer();
                String str = "";
                String str2 = null;
                for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                    String str3;
                    str = newPullParser.getName();
                    if (eventType != 2) {
                        str3 = str2;
                    } else if ("language".equalsIgnoreCase(str)) {
                        str3 = newPullParser.getAttributeValue(0);
                    } else if ("feature".equalsIgnoreCase(str)) {
                        if (a2.equals(str2)) {
                            this.r += newPullParser.nextText().trim() + c.a;
                        }
                        if ("en-us".equals(str2)) {
                            stringBuffer.append(!"".equals(this.r) ? this.r : newPullParser.nextText().trim() + c.a);
                            str3 = str2;
                        } else {
                            str3 = str2;
                        }
                    } else {
                        str3 = str2;
                    }
                    str2 = str3;
                }
                if ("".equals(this.r)) {
                    this.r = stringBuffer.toString();
                }
                j.b(a, "NewVersionInfo");
            } catch (XmlPullParserException e) {
                inputStream2 = a;
            } catch (Exception e2) {
            }
        } catch (XmlPullParserException e3) {
            try {
                e.d("NewVersionInfo", "parse changelog to versioninfo error");
                j.b(inputStream2, "NewVersionInfo");
            } catch (Throwable th3) {
                th = th3;
                a = inputStream2;
                th2 = th;
                j.b(a, "NewVersionInfo");
                throw th2;
            }
        } catch (Exception e4) {
            a = null;
            try {
                e.d("NewVersionInfo", "parse changelog to versioninfo error");
                j.b(a, "NewVersionInfo");
            } catch (Throwable th4) {
                th2 = th4;
                j.b(a, "NewVersionInfo");
                throw th2;
            }
        } catch (Throwable th32) {
            th = th32;
            a = null;
            th2 = th;
            j.b(a, "NewVersionInfo");
            throw th2;
        }
    }

    private void a(XmlPullParser xmlPullParser, String str) throws IOException, XmlPullParserException {
        if ("spath".equalsIgnoreCase(str)) {
            this.b = xmlPullParser.nextText();
            e.b("NewVersionInfo", "this.sPath: " + this.b);
        } else if ("dpath".equalsIgnoreCase(str)) {
            this.c = xmlPullParser.nextText();
            e.b("NewVersionInfo", "this.dPath: " + this.c);
        } else if ("md5".equalsIgnoreCase(str)) {
            this.g = xmlPullParser.nextText();
        } else if ("size".equalsIgnoreCase(str)) {
            this.a = Integer.parseInt(xmlPullParser.nextText());
            e.b("NewVersionInfo", "this.totalSize: " + this.a);
        } else if ("packageName".equalsIgnoreCase(str)) {
            this.m = xmlPullParser.nextText();
            e.b("NewVersionInfo", "this.packageName: " + this.m);
        } else if ("versionName".equalsIgnoreCase(str)) {
            this.o = xmlPullParser.nextText();
            e.b("NewVersionInfo", "this.versionName: " + this.o);
        } else if ("versionCode".equalsIgnoreCase(str)) {
            this.n = xmlPullParser.nextText();
            e.b("NewVersionInfo", "this.versionCode: " + this.n);
        }
    }

    public void a(String str) {
        this.f = str;
    }

    public void b(String str) {
        this.h = str;
    }

    public void c(String str) {
        this.i = str;
    }

    public void d(String str) {
        this.k = str;
    }

    public String a() {
        return this.n;
    }

    public String b() {
        return this.o;
    }

    public int c() {
        return this.a;
    }

    public void a(int i) {
        this.a = i;
    }

    public String d() {
        return this.b;
    }

    public String e() {
        this.d = this.k + "full" + File.separator + this.b;
        return this.d;
    }

    public String f() {
        return this.e;
    }

    public void e(String str) {
        this.e = str;
    }

    public String g() {
        return this.f;
    }

    public String h() {
        return this.g;
    }

    public String i() {
        return this.k;
    }

    public void f(String str) {
        this.q = str;
    }

    public void g(String str) {
        this.j = str;
    }

    public void h(String str) {
        this.l = str;
    }

    public void b(int i) {
        this.s = i;
    }

    public void a(boolean z) {
        this.t = z;
    }
}
