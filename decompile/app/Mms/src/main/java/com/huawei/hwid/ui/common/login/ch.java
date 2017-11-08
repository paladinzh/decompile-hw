package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class ch extends c {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;

    public ch(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(setRegisterPhoneNumPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.l = bundle.getInt("siteID", 0);
        this.b.i = new com.huawei.hwid.core.a.c(this.b, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.b.f);
        if (f.FromOOBE == this.b.q()) {
            this.b.i.a(true);
        }
        if (this.b.m() || 1 == this.b.r) {
            a.b("RegisterPhoneNumActivity", "is thirdAccountBindHwAccount, onlyBindPhoneForThird = " + this.b.r);
            this.b.a(this.b.f, this.b.l);
            return;
        }
        this.b.i();
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            Dialog create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        if (this.b.i == null) {
            this.b.i = new com.huawei.hwid.core.a.c(this.b, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.b.f);
        }
        this.b.i.a(d.a());
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.i.d(errorStatus.getErrorReason());
        }
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        super.onFail(bundle);
    }
}
