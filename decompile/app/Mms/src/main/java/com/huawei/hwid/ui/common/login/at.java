package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: RegisterViaEmailActivity */
class at implements OnClickListener {
    final /* synthetic */ RegisterViaEmailActivity a;

    at(RegisterViaEmailActivity registerViaEmailActivity) {
        this.a = registerViaEmailActivity;
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.a.getIntent());
        intent.setFlags(67108864);
        intent.setClass(this.a, RegisterViaPhoneNumberActivity.class);
        this.a.startActivity(intent);
        this.a.finish();
    }
}
