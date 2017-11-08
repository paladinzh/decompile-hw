package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.g;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class cd implements OnClickListener {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity a;

    cd(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity) {
        this.a = setRegisterPhoneNumPasswordActivity;
    }

    public void onClick(View view) {
        if (g.a(this.a.f, this.a.d, this.a.e, this.a.getApplicationContext())) {
            this.a.h();
        } else {
            this.a.b.setEnabled(false);
        }
    }
}
