package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumberActivity */
class bi extends c {
    final /* synthetic */ RegisterViaPhoneNumberActivity b;

    public bi(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity, Context context) {
        this.b = registerViaPhoneNumberActivity;
        super(registerViaPhoneNumberActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        String string = bundle.getString("isAccountExist");
        a.b("RegisterViaPhoneNumberActivity", "CheckAccountcallBack state:" + string);
        if (!"1".equals(string)) {
            if (this.b.m()) {
                if (!"2".equals(string)) {
                }
            }
            this.b.f = false;
            this.b.c(this.b.t());
            return;
        }
        this.b.b();
        this.b.f = true;
        this.b.b.setError(this.b.getString(m.a(this.b, "CS_phone_already_exist")));
        this.b.b.requestFocus();
        this.b.b.selectAll();
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            Dialog create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        super.onFail(bundle);
    }
}
