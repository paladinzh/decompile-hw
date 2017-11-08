package com.huawei.hwid.ui.common.password;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;
import com.huawei.hwid.ui.common.login.RegisterResetVerifyEmailActivity;

/* compiled from: FindpwdByHwIdActivity */
class f extends c {
    final /* synthetic */ FindpwdByHwIdActivity b;
    private String d;

    public f(FindpwdByHwIdActivity findpwdByHwIdActivity, Context context, String str) {
        this.b = findpwdByHwIdActivity;
        super(findpwdByHwIdActivity, context);
        this.d = str;
    }

    public void onSuccess(Bundle bundle) {
        a.b("FindpwdByHwIdActivity", "onSuccess email");
        super.onSuccess(bundle);
        this.b.b();
        Intent intent = new Intent(this.b, RegisterResetVerifyEmailActivity.class);
        intent.putExtra("isFromRegister", false);
        intent.putExtra("accountName", this.b.e);
        intent.putExtra("emailName", this.d);
        intent.putExtra("siteId", this.b.f);
        this.b.startActivityForResult(intent, 4);
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                int i;
                int a = m.a(this.a, "CS_notification");
                int i2;
                if (errorStatus.getErrorCode() == 70001104) {
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
