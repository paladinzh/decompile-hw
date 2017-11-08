package com.huawei.hwid.core.datatype;

import com.huawei.hwid.core.c.b.a;

/* compiled from: AppInfo */
public class c {
    private String a = "";
    private String b = "";
    private String c = "";
    private boolean d = false;
    private boolean e = false;
    private boolean f = false;
    private boolean g = false;
    private boolean h = false;
    private boolean i = false;
    private int j = 0;
    private String k = "";
    private int l = 0;
    private boolean m = false;

    public boolean a() {
        return this.f;
    }

    public void a(boolean z) {
        this.f = z;
    }

    public String b() {
        return this.b;
    }

    public void a(String str) {
        this.b = str;
    }

    public String c() {
        return this.c;
    }

    public void b(String str) {
        this.c = str;
    }

    public void b(boolean z) {
        this.d = z;
    }

    public void c(boolean z) {
        this.e = z;
    }

    public void c(String str) {
        this.a = str;
    }

    public String d() {
        return this.a;
    }

    public void d(boolean z) {
        this.g = z;
    }

    public void a(int i) {
        this.j = i;
    }

    public boolean e() {
        return this.i;
    }

    public void e(boolean z) {
        this.i = z;
    }

    public String f() {
        return this.k;
    }

    public void d(String str) {
        this.k = str;
    }

    public void b(int i) {
        this.l = i;
    }

    public void f(boolean z) {
        this.m = z;
        a.b("AppInfo", "mActivateVip:" + this.m);
    }

    public String toString() {
        return "mAppID:" + this.a + " ;mReqClientType:" + this.b + " ;mDefaultChannel:" + this.c + " ;popLogin:" + String.valueOf(this.d) + " ;chooseAccount:" + String.valueOf(this.e) + ";mScope:" + this.j + ";mChooseWindow:" + this.i + ";mCheckPsd:" + this.h + ";mNeedAuth:" + this.f + ";mAccountName:" + this.k + ";mSdkType:" + this.l + ";mIsFromAPK:" + this.g + ";mActivateVip:" + this.m;
    }
}
