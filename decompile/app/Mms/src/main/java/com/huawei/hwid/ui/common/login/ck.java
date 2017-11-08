package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class ck extends c {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;

    public ck(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(setRegisterPhoneNumPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.i.a(d.a());
        j.a(this.b, this.b.getString(m.a(this.b, "CS_register_success"), new Object[]{this.b.a(this.b.h, this.b.f)}), 1);
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        this.b.s();
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            this.b.b();
            this.b.i.a(d.a());
            if (errorStatus != null) {
                if (70002002 == errorStatus.getErrorCode()) {
                    if (this.b.j) {
                        this.b.t();
                    } else {
                        this.b.s();
                        this.b.a(this.b.getString(m.a(this.b, "CS_logining_message")));
                    }
                    this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
                } else if (70002067 == errorStatus.getErrorCode()) {
                    this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                } else if (70002068 == errorStatus.getErrorCode()) {
                    this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                } else if (70002069 == errorStatus.getErrorCode()) {
                    this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                } else if (70002039 != errorStatus.getErrorCode()) {
                    this.b.t();
                    this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("authcode_invalid", true);
                    this.b.setResult(-1, intent);
                    this.b.finish();
                }
                com.huawei.hwid.core.a.d.a(this.b.i, this.b);
            }
        }
        super.onFail(bundle);
    }
}
