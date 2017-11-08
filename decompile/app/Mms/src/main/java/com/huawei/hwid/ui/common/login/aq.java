package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterResetVerifyEmailActivity */
class aq extends c {
    final /* synthetic */ RegisterResetVerifyEmailActivity b;

    public aq(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity, Context context) {
        this.b = registerResetVerifyEmailActivity;
        super(registerResetVerifyEmailActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_reset_pwd_email_send"), (Object[]) new String[]{this.b.k}), 1);
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                int i;
                a.b("RegisterResetVerifyEmailActivity", "sendEmailProcess ==> HttpStatusCode =" + errorStatus.getErrorCode());
                int a = m.a(this.a, "CS_notification");
                int i2;
                if (70001104 == errorStatus.getErrorCode()) {
                    i2 = a;
                    a = m.a(this.a, "CS_verification_email_overload_24h");
                    i = i2;
                } else if (70001102 != errorStatus.getErrorCode()) {
                    a = m.a(this.a, "CS_security_email_error");
                    i = m.a(this.a, "CS_prompt_dialog_title");
                } else {
                    i2 = a;
                    a = m.a(this.a, "CS_verification_email_overload_1h");
                    i = i2;
                }
                Dialog create = j.a(this.a, a, i).create();
                this.b.a(create);
                create.show();
            }
        }
        super.onFail(bundle);
    }
}
