package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterResetVerifyEmailActivity */
class am extends c {
    final /* synthetic */ RegisterResetVerifyEmailActivity b;

    public am(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity, Context context, a aVar) {
        this.b = registerResetVerifyEmailActivity;
        super(registerResetVerifyEmailActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_active_email_send"), new Object[]{this.b.k}), 1);
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                int i;
                int a = m.a(this.b, "CS_prompt_dialog_title");
                int i2;
                if (70002019 == errorStatus.getErrorCode()) {
                    i2 = a;
                    a = m.a(this.b, "CS_email_already_verified");
                    i = i2;
                } else if (70001104 == errorStatus.getErrorCode()) {
                    i2 = a;
                    a = m.a(this.b, "CS_verification_email_overload_24h");
                    i = i2;
                } else if (70001102 != errorStatus.getErrorCode()) {
                    a = m.a(this.b, "CS_server_unavailable_message");
                    i = m.a(this.b, "CS_server_unavailable_title");
                } else {
                    i2 = a;
                    a = m.a(this.b, "CS_verification_email_overload_1h");
                    i = i2;
                }
                Dialog create = j.a(this.b, a, i).create();
                this.b.a(create);
                create.show();
            }
        }
        super.onFail(bundle);
    }
}
