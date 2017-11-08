package com.huawei.hwid.ui.common.password;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: FindpwdbyPhonenumberActivity */
class u extends c {
    final /* synthetic */ FindpwdbyPhonenumberActivity b;

    public u(FindpwdbyPhonenumberActivity findpwdbyPhonenumberActivity, Context context) {
        this.b = findpwdbyPhonenumberActivity;
        super(findpwdbyPhonenumberActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        try {
            super.onSuccess(bundle);
            if (this.b.g >= 0 && this.b.g < this.b.f.size()) {
                String str = "";
                if (this.b.c.getText() != null) {
                    str = this.b.c.getText().toString();
                }
                if (str.contains("+")) {
                    str = d.d(str);
                }
                a.b("FindpwdbyPhonenumberActivity", "temp:" + f.a(str));
                j.a(this.b, this.b.getString(m.a(this.b, "CS_verification_code_sms_send"), new Object[]{d.c(str)}), 1);
                Intent intent = new Intent();
                intent.putExtra("phoneNumber", d.c(str));
                intent.putExtra("hwid", this.b.d);
                intent.putExtra("siteId", this.b.e);
                intent.putExtra("requestTokenType", this.b.h);
                intent.setClass(this.b, ResetPwdByPhoneNumberVerificationActivity.class);
                this.b.startActivityForResult(intent, 2);
                return;
            }
            a.d("FindpwdbyPhonenumberActivity", "click Err, maybe not init failed");
            this.b.finish();
        } catch (Throwable th) {
            a.d("FindpwdbyPhonenumberActivity", th.toString(), th);
        }
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            a.e("FindpwdbyPhonenumberActivity", "the security phone is wrong");
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
                    intent.putExtra("error_prompt", this.b.getString(m.a(this.a, "CS_username_not_exist")));
                    this.b.setResult(1, intent);
                    this.b.finish();
                }
            }
        }
        super.onFail(bundle);
    }
}
