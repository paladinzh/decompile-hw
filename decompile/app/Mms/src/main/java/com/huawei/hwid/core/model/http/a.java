package com.huawei.hwid.core.model.http;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.autonavi.amap.mapcore.MapCore;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.b.b;
import com.huawei.hwid.core.helper.handler.c;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: HttpRequest */
public abstract class a {
    private static String h = b.a();
    protected int a = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
    protected int b = -1;
    protected int c = -1;
    protected String d;
    protected String e;
    protected int f = 3;
    protected int g = 1;
    private String i = "";
    private ArrayList j = new ArrayList();
    private boolean k = false;
    private boolean l = false;
    private boolean m = true;
    private boolean n = false;
    private int o = 0;
    private String p;
    private e q = e.XMLType;

    protected abstract void a(String str) throws XmlPullParserException, IOException;

    protected abstract String e() throws IllegalArgumentException, IllegalStateException, IOException;

    public abstract String g();

    static {
        com.huawei.hwid.b.a.a();
    }

    public void a(e eVar) {
        this.q = eVar;
    }

    public e a() {
        return this.q;
    }

    protected String c() {
        String a;
        synchronized (a.class) {
            a = f.a;
        }
        return a;
    }

    protected String d() {
        String b;
        synchronized (a.class) {
            b = f.b;
        }
        return b;
    }

    protected String f() {
        return null;
    }

    protected void b(String str) {
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
    }

    public Bundle h() {
        return i();
    }

    public Bundle i() {
        Bundle bundle = new Bundle();
        bundle.putInt("responseCode", this.a);
        bundle.putInt("resultCode", this.b);
        bundle.putInt("errorCode", this.c);
        bundle.putString("errorDesc", this.d);
        bundle.putString("TGC", this.e);
        bundle.putIntegerArrayList("UIHandlerErrCodeList", m());
        bundle.putBoolean("isUIHandlerAllErrCode", n());
        bundle.putBoolean("isIngoreTokenErr", o());
        return bundle;
    }

    public int j() {
        return this.a;
    }

    public void a(int i) {
        this.a = i;
    }

    public int k() {
        return this.b;
    }

    public String l() {
        return this.d;
    }

    public void c(String str) {
        this.e = str;
    }

    public void d(String str) {
        this.i = str;
    }

    public void b(int i) {
        this.j.add(Integer.valueOf(i));
    }

    public ArrayList m() {
        return this.j;
    }

    public void a(boolean z) {
        this.k = z;
    }

    public boolean n() {
        return this.k;
    }

    public boolean o() {
        return this.l;
    }

    public int p() {
        return this.g;
    }

    public String q() {
        String g = g();
        if (TextUtils.isEmpty(g)) {
            return "";
        }
        return g.substring(g.lastIndexOf("/") + 1);
    }

    public void c(int i) {
        String str;
        this.o = i;
        String g = g();
        if (this.o >= 1 && this.o <= MapCore.MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER) {
            Object obj = "";
            if (g.startsWith("http://setting")) {
                obj = "http://setting";
            } else if (g.startsWith("https://setting")) {
                obj = "https://setting";
            }
            if (TextUtils.isEmpty(obj)) {
                str = g;
            } else {
                int length = obj.length();
                str = g.substring(0, length) + this.o + g.substring(length);
            }
        } else {
            str = g;
        }
        this.p = str;
    }

    public int r() {
        return this.o;
    }

    public String s() {
        if (TextUtils.isEmpty(this.p)) {
            return g() + "?Version=10000";
        }
        return this.p + "?Version=10000";
    }

    public void e(String str) {
        this.p = str;
    }

    public boolean t() {
        return this.m;
    }

    public boolean u() {
        return this.n;
    }

    public int v() {
        return this.f;
    }

    public void d(int i) {
        this.f = i;
    }

    protected void b(boolean z) {
        this.n = z;
    }

    protected c a(Context context, a aVar, c cVar) {
        b bVar = new b("BackgroundHandlerThread", cVar);
        bVar.start();
        return bVar.a();
    }
}
