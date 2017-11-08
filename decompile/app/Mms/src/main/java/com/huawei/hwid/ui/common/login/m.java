package com.huawei.hwid.ui.common.login;

import android.os.Bundle;

/* compiled from: LoginRegisterCommonActivity */
public class m {
    boolean a = false;
    String b = "";
    String c = "com.huawei.hwid";
    String d = "";

    m() {
    }

    public m(boolean z, String str, String str2, String str3) {
        this.a = z;
        this.b = str;
        this.c = str2;
        this.d = str3;
    }

    public Bundle a() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("completed", this.a);
        if (this.a) {
            bundle.putString("authAccount", this.b);
            bundle.putString("accountType", this.c);
            bundle.putString("authtoken", this.d);
        }
        return bundle;
    }
}
