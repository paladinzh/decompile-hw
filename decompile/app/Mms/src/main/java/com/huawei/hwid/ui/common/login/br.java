package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.g;

/* compiled from: SetRegisterEmailPasswordActivity */
class br implements OnClickListener {
    final /* synthetic */ SetRegisterEmailPasswordActivity a;

    br(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity) {
        this.a = setRegisterEmailPasswordActivity;
    }

    public void onClick(View view) {
        if (g.a(this.a.f, this.a.d, this.a.e, this.a.getApplicationContext())) {
            this.a.h();
            return;
        }
        a.b("RegisterEmailActivity", "the email is not allow");
        this.a.b.setEnabled(false);
    }
}
