package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.ui.common.password.FindpwdByHwIdActivity;

/* compiled from: LoginActivity */
class g implements OnClickListener {
    final /* synthetic */ LoginActivity a;

    g(LoginActivity loginActivity) {
        this.a = loginActivity;
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        intent.setClass(this.a, FindpwdByHwIdActivity.class);
        this.a.startActivityForResult(intent, 100);
    }
}
