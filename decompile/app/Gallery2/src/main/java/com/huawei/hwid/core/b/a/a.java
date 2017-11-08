package com.huawei.hwid.core.b.a;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.d.k;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParserException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class a {
    private static String h = com.huawei.hwid.vermanager.b.a().a();
    protected int a = SmsCheckResult.ESCT_200;
    protected int b = -1;
    protected int c = -1;
    protected String d;
    protected String e;
    protected int f = 3;
    protected int g = 1;
    private String i = "";
    private ArrayList<Integer> j = new ArrayList();
    private boolean k = false;
    private boolean l = false;
    private boolean m = true;
    private boolean n = false;
    private int o = 0;
    private String p;
    private boolean q = false;
    private d r = d.XMLType;

    static class a extends HandlerThread {
        private b a = null;
        private com.huawei.hwid.core.helper.handler.b b = null;

        public a(String str, com.huawei.hwid.core.helper.handler.b bVar) {
            super(str);
            this.b = bVar;
        }

        protected void onLooperPrepared() {
            this.a = new b(this.b);
            super.onLooperPrepared();
        }

        public b a() {
            int i = 1000;
            while (this.a == null) {
                int i2 = i - 1;
                if (i <= 0) {
                    break;
                }
                try {
                    sleep(4);
                } catch (Throwable e) {
                    com.huawei.hwid.core.d.b.e.d("RequestManager", e.getMessage(), e);
                }
                i = i2;
            }
            return this.a;
        }
    }

    static class b extends Handler {
        private com.huawei.hwid.core.helper.handler.b a;

        public b(com.huawei.hwid.core.helper.handler.b bVar) {
            this.a = bVar;
        }

        public void a() {
            getLooper().quit();
        }

        public void handleMessage(Message message) {
            if (message.what == 0) {
                this.a.e((Bundle) message.obj);
                a();
            }
            super.handleMessage(message);
        }
    }

    public enum d {
        XMLType,
        URLType,
        JSONType
    }

    public static class e {
        private static String a = com.huawei.hwid.vermanager.b.a().b();
        private static String b = com.huawei.hwid.vermanager.b.a().c();
        private static String c = com.huawei.hwid.vermanager.b.a().d();
        private static String d = com.huawei.hwid.vermanager.b.a().e();
    }

    protected abstract void a(String str) throws XmlPullParserException, IOException;

    protected abstract String e() throws IllegalArgumentException, IllegalStateException, IOException;

    public abstract String g();

    public void a(d dVar) {
        this.r = dVar;
    }

    public d a() {
        return this.r;
    }

    public static synchronized String c() {
        String a;
        synchronized (a.class) {
            a = e.c;
        }
        return a;
    }

    protected String d() {
        String b;
        synchronized (a.class) {
            b = e.b;
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

    public ArrayList<Integer> m() {
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
        this.o = i;
        String g = g();
        String str = "";
        if (this.o >= 1 && this.o <= 999) {
            str = String.valueOf(i);
        }
        this.p = k.a(g, new String[]{"\\{0\\}", str});
    }

    public int r() {
        return this.o;
    }

    public String a(Context context) {
        if (TextUtils.isEmpty(this.p)) {
            return g() + "?Version=" + "10002" + "&cVersion=" + com.huawei.hwid.core.d.b.p(context);
        }
        return this.p + "?Version=" + "10002" + "&cVersion=" + com.huawei.hwid.core.d.b.p(context);
    }

    public void d(String str) {
        this.p = str;
    }

    public boolean s() {
        return this.m;
    }

    public int t() {
        return this.f;
    }

    public void d(int i) {
        this.f = i;
    }

    protected void b(boolean z) {
        this.n = z;
    }

    protected b a(Context context, a aVar, com.huawei.hwid.core.helper.handler.b bVar) {
        a aVar2 = new a("BackgroundHandlerThread", bVar);
        aVar2.start();
        return aVar2.a();
    }
}
