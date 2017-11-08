package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: PrivacyPolicyActivity */
class v implements OnClickListener {
    final /* synthetic */ PrivacyPolicyActivity a;

    v(PrivacyPolicyActivity privacyPolicyActivity) {
        this.a = privacyPolicyActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
