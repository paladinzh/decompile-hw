package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.m;

/* compiled from: RegisterResetVerifyEmailActivity */
class ah implements OnClickListener {
    final /* synthetic */ RegisterResetVerifyEmailActivity a;

    ah(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity) {
        this.a = registerResetVerifyEmailActivity;
    }

    public void onClick(View view) {
        if (this.a.r) {
            this.a.c(this.a.getString(m.a(this.a, "CS_register_verify_email_later_tip_new")));
            return;
        }
        this.a.setResult(-1);
        this.a.finish();
    }
}
