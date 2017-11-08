package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumberActivity */
class bj extends c {
    final /* synthetic */ RegisterViaPhoneNumberActivity b;

    public bj(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity, Context context) {
        this.b = registerViaPhoneNumberActivity;
        super(registerViaPhoneNumberActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        String a = d.a(this.b.g, this.b.t());
        j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_code_sms_send"), new Object[]{a}), 1);
        this.b.v();
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            this.b.b();
            if (errorStatus != null) {
                if (70002002 != errorStatus.getErrorCode()) {
                    int i;
                    int a = m.a(this.b, "CS_notification");
                    int i2;
                    if (70001102 == errorStatus.getErrorCode()) {
                        i2 = a;
                        a = m.a(this.b, "CS_verification_code_sms_overload_1h");
                        i = i2;
                    } else if (70001104 == errorStatus.getErrorCode()) {
                        i2 = a;
                        a = m.a(this.b, "CS_verification_code_sms_overload_24h");
                        i = i2;
                    } else if (70002030 != errorStatus.getErrorCode()) {
                        a = m.a(this.b, "CS_server_unavailable_message");
                        i = m.a(this.b, "CS_server_unavailable_title");
                    } else {
                        a = m.a(this.b, "CS_send_verification_error");
                        i = m.a(this.b, "CS_prompt_dialog_title");
                    }
                    Dialog create = j.a(this.b, a, i).create();
                    this.b.a(create);
                    create.show();
                } else if (!this.b.f) {
                    j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_code_sms_send"), new Object[]{d.c(this.b.t())}), 1);
                }
            }
        }
        super.onFail(bundle);
    }
}
