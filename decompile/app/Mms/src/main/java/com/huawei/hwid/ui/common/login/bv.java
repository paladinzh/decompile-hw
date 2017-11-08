package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterEmailPasswordActivity */
class bv extends c {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public bv(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context) {
        this.b = setRegisterEmailPasswordActivity;
        super(setRegisterEmailPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.k = bundle.getInt("siteID", 0);
        this.b.g = new com.huawei.hwid.core.a.c(this.b, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.b.f);
        if (f.FromOOBE == this.b.q() || this.b.i) {
            this.b.g.a(true);
        }
        if (this.b.m()) {
            this.b.a(this.b.f, e.d(this.b, this.b.d.getText().toString()), this.b.k);
        } else {
            this.b.i();
        }
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            Dialog create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        if (this.b.g == null) {
            this.b.g = new com.huawei.hwid.core.a.c(this.b, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.b.f);
        }
        this.b.g.a(d.a());
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            this.b.g.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.g.d(errorStatus.getErrorReason());
        }
        com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        super.onFail(bundle);
    }
}
