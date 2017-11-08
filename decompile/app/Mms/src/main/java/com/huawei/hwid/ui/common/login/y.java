package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: PrivacyPolicyActivity */
class y implements OnClickListener {
    final /* synthetic */ PrivacyPolicyActivity a;

    y(PrivacyPolicyActivity privacyPolicyActivity) {
        this.a = privacyPolicyActivity;
    }

    public void onClick(View view) {
        this.a.a(this.a.i, this.a.m, ".html");
        this.a.g();
        this.a.e.setVisibility(8);
    }
}
