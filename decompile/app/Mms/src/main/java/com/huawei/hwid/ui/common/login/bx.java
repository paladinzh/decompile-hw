package com.huawei.hwid.ui.common.login;

import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IActiveMemberCallback;

/* compiled from: SetRegisterEmailPasswordActivity */
class bx implements IActiveMemberCallback {
    final /* synthetic */ Bundle a;
    final /* synthetic */ bw b;

    bx(bw bwVar, Bundle bundle) {
        this.b = bwVar;
        this.a = bundle;
    }

    public void callback(String str, String str2, int i) {
        a.b("RegisterEmailActivity", "retCode:" + str + "memLevel:" + i);
        if ("0".equals(str)) {
            this.a.putInt("rightsID", i);
            com.huawei.hwid.c.a.a(this.b.b, this.a);
        }
        this.b.b.r = this.b.b();
        this.b.b.a(this.b.b.r, this.a);
    }
}
