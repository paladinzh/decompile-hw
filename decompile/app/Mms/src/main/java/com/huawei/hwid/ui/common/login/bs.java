package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterEmailPasswordActivity */
class bs extends c {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public bs(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context, Bundle bundle) {
        this.b = setRegisterEmailPasswordActivity;
        super(setRegisterEmailPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        Intent intent = new Intent(this.b, RegisterResetVerifyEmailActivity.class);
        intent.putExtra("isFromRegister", true);
        intent.putExtra("weixinloginsuccess", true);
        intent.putExtra("emailName", this.b.f);
        intent.putExtra("third_is_weixin_login", true);
        this.b.startActivityForResult(intent, 1);
        this.b.j = false;
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            Dialog create = j.a(this.a, m.a(this.a, "CS_server_unavailable_message"), m.a(this.a, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        f.a(this.a).a(this.a, this.b.f, "com.huawei.hwid");
        super.onFail(bundle);
    }
}
