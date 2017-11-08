package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: RegisterViaPhoneNumberActivity */
class bf implements OnClickListener {
    final /* synthetic */ RegisterViaPhoneNumberActivity a;

    bf(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity) {
        this.a = registerViaPhoneNumberActivity;
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.a.getIntent());
        intent.setFlags(67108864);
        intent.setClass(this.a, RegisterViaEmailActivity.class);
        this.a.startActivity(intent);
        this.a.finish();
    }
}
