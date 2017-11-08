package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterEmailPasswordActivity */
class by extends c {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public by(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context) {
        this.b = setRegisterEmailPasswordActivity;
        super(setRegisterEmailPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.g.a(d.a());
        j.a(this.b, this.b.getString(m.a(this.b, "CS_register_success"), new Object[]{this.b.f}), 1);
        com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        this.b.s();
    }

    public void onFail(Bundle bundle) {
        boolean z = bundle.getBoolean("isRequestSuccess", false);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            if (z) {
                this.b.b();
                if (70002002 != errorStatus.getErrorCode()) {
                    if (70002067 == errorStatus.getErrorCode()) {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    } else if (70002068 == errorStatus.getErrorCode()) {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    } else if (70002069 != errorStatus.getErrorCode()) {
                        this.b.t();
                    } else {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    }
                } else if (this.b.h) {
                    this.b.t();
                } else {
                    this.b.s();
                    this.b.a(this.b.getString(m.a(this.b, "CS_logining_message")));
                }
            }
            this.b.g.a(d.a());
            this.b.g.c(String.valueOf(errorStatus.getErrorCode()));
        }
        com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        super.onFail(bundle);
    }
}
