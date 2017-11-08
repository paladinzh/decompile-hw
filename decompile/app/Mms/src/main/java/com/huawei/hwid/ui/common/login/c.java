package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: LoginActivity */
class c implements OnClickListener {
    final /* synthetic */ LoginActivity a;

    c(LoginActivity loginActivity) {
        this.a = loginActivity;
    }

    public void onClick(View view) {
        if (this.a.f()) {
            this.a.i();
        }
        this.a.onBackPressed();
    }
}
