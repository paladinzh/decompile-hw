package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IActiveMemberCallback;

/* compiled from: RegisterResetVerifyEmailActivity */
class ap implements IActiveMemberCallback {
    final /* synthetic */ Bundle a;
    final /* synthetic */ ao b;

    ap(ao aoVar, Bundle bundle) {
        this.b = aoVar;
        this.a = bundle;
    }

    public void callback(String str, String str2, int i) {
        a.b("RegisterResetVerifyEmailActivity", "retCode:" + str + " memLevel:" + i);
        if ("0".equals(str)) {
            this.a.putInt("rightsID", i);
            com.huawei.hwid.c.a.a(this.b.b, this.a);
            this.b.b.a(this.b.b(), this.a);
            return;
        }
        this.b.b.a(true, new Intent().putExtra("bundle", this.a));
    }
}
