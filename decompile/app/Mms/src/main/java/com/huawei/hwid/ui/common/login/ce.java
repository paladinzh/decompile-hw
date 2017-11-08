package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.j;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.c;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class ce extends c {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;
    private Bundle d;

    public ce(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context, Bundle bundle) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(setRegisterPhoneNumPasswordActivity, context);
        this.d = bundle;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        Class a = j.a("com.huawei.third.ui.BindWeixinAccountSuccessActivity");
        if (a != null) {
            Intent intent = new Intent(this.a, a);
            intent.putExtras(this.d);
            intent.putExtra("open_weixin_from_login_or_register", "register");
            this.b.startActivityForResult(intent, 2);
            return;
        }
        a.b("RegisterPhoneNumActivity", "cls is null, onSuccess error");
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            Dialog create = com.huawei.hwid.ui.common.j.a(this.a, m.a(this.a, "CS_server_unavailable_message"), m.a(this.a, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        f.a(this.a).a(this.a, this.b.f, "com.huawei.hwid");
        super.onFail(bundle);
    }
}
