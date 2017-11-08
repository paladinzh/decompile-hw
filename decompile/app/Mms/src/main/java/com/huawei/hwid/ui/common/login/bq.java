package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterEmailPasswordActivity */
class bq implements OnClickListener {
    final /* synthetic */ SetRegisterEmailPasswordActivity a;
    private boolean b = false;

    bq(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity) {
        this.a = setRegisterEmailPasswordActivity;
    }

    public void onClick(View view) {
        boolean z = false;
        if (!this.b) {
            z = true;
        }
        this.b = z;
        j.a(this.a, this.a.d, this.a.p, this.b);
        j.a(this.a, this.a.e, this.a.p, this.b);
    }
}
