package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterEmailPasswordActivity */
class bu extends c {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public bu(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context) {
        this.b = setRegisterEmailPasswordActivity;
        super(setRegisterEmailPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        Intent intent = new Intent("com.huawei.third.ACTION_UPGRADE_SUCCESS");
        intent.setPackage("com.huawei.hwid");
        intent.putExtra("requestTokenType", this.b.k());
        intent.putExtra("displayName", this.b.f);
        intent.putExtras(this.b.getIntent());
        intent.putExtras(bundle);
        this.b.startActivityForResult(intent, 2);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            this.b.b();
            this.b.g.a(d.a());
            if (errorStatus != null) {
                Dialog create;
                if (70002002 != errorStatus.getErrorCode()) {
                    create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
                    this.b.a(create);
                    create.show();
                    this.b.g.c(String.valueOf(errorStatus.getErrorCode()));
                } else {
                    this.b.d.setText("");
                    this.b.e.setText("");
                    create = j.a(this.b, m.a(this.b, "CS_email_already_exist"), m.a(this.b, "CS_notification")).create();
                    this.b.a(create);
                    create.show();
                    this.b.g.c(String.valueOf(errorStatus.getErrorCode()));
                }
            }
            com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        }
        super.onFail(bundle);
    }
}
