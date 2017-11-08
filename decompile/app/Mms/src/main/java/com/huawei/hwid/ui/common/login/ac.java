package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.ui.common.c;

/* compiled from: PrivacyPolicyActivity */
class ac extends c {
    final /* synthetic */ PrivacyPolicyActivity b;

    public ac(PrivacyPolicyActivity privacyPolicyActivity, Context context) {
        this.b = privacyPolicyActivity;
        super(privacyPolicyActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.n = bundle.getString("agreeVersion");
        this.b.m = i.c(this.b, this.b.l);
        boolean z = bundle.getBoolean("isAgreementUpdate");
        this.b.g.setVisibility(0);
        if (z) {
            this.b.a(this.b.i, this.b.m, ".html");
            if (this.b.a == null || !this.b.a.isAlive()) {
                this.b.a = new ad(this);
                this.b.a.start();
                return;
            }
            return;
        }
        this.b.j();
        this.b.g.setVisibility(0);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.e.setVisibility(0);
            this.b.d.setVisibility(4);
        }
        super.onFail(bundle);
    }
}
