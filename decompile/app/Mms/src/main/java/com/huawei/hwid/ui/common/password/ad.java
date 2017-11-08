package com.huawei.hwid.ui.common.password;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class ad extends c {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity b;

    public ad(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity, Context context, a aVar) {
        this.b = resetPwdByPhoneNumberVerificationActivity;
        super(resetPwdByPhoneNumberVerificationActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_code_sms_send"), new Object[]{d.c(this.b.f)}), 1);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                if (errorStatus.getErrorCode() != 70002001) {
                    int i;
                    int a = m.a(this.a, "CS_notification");
                    int i2;
                    if (70001102 == errorStatus.getErrorCode()) {
                        i2 = a;
                        a = m.a(this.a, "CS_verification_code_sms_overload_1h");
                        i = i2;
                    } else if (70001104 == errorStatus.getErrorCode()) {
                        i2 = a;
                        a = m.a(this.a, "CS_verification_code_sms_overload_24h");
                        i = i2;
                    } else if (70002030 != errorStatus.getErrorCode()) {
                        i2 = a;
                        a = m.a(this.a, "CS_security_phone_error");
                        i = i2;
                    } else {
                        a = m.a(this.a, "CS_send_verification_error");
                        i = m.a(this.a, "CS_prompt_dialog_title");
                    }
                    Dialog create = j.a(this.a, a, i).create();
                    this.b.a(create);
                    create.show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("error_prompt", this.b.getString(m.a(this.b, "CS_username_not_exist")));
                    this.b.setResult(1, intent);
                    this.b.finish();
                }
            }
        }
        super.onFail(bundle);
    }
}
