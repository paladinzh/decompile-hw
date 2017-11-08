package com.huawei.hwid.ui.common.login;

import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IActiveMemberCallback;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class cj implements IActiveMemberCallback {
    final /* synthetic */ Bundle a;
    final /* synthetic */ ci b;

    cj(ci ciVar, Bundle bundle) {
        this.b = ciVar;
        this.a = bundle;
    }

    public void callback(String str, String str2, int i) {
        a.b("RegisterPhoneNumActivity", "retCode:" + str + "memLevel:" + i);
        if ("0".equals(str)) {
            this.a.putInt("rightsID", i);
            com.huawei.hwid.c.a.a(this.b.b, this.a);
        }
        this.b.b.a(this.b.b(), this.a);
    }
}
