package com.huawei.hwid.core.datatype;

import android.text.TextUtils;

public class a {
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

    public void a(String str) {
        this.b = str;
    }

    public String a() {
        return this.c;
    }

    public void b(String str) {
        if (d(str)) {
            this.c = str;
        }
    }

    private boolean d(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return true;
    }

    public void c(String str) {
        this.a = str;
    }

    public String b() {
        return this.a;
    }

    public String toString() {
        return "mAppID:" + this.a + " ;mReqClientType:" + this.b + " ;mDefaultChannel:" + this.c + " ;popLogin:" + String.valueOf(this.d) + " ;chooseAccount:" + String.valueOf(this.e) + ";mScope:" + this.j + ";mChooseWindow:" + this.i + ";mCheckPsd:" + this.h + ";mNeedAuth:" + this.f + ";mAccountName:" + this.k + ";mSdkType:" + this.l + ";mIsFromAPK:" + this.g + ";mActivateVip:" + this.m;
    }
}
