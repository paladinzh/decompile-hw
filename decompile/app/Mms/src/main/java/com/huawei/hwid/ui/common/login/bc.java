package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class bc extends c {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity b;

    public bc(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity, Context context) {
        this.b = registerViaPhoneNumVerificationActivity;
        super(registerViaPhoneNumVerificationActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        this.b.f.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.f, this.b);
        this.b.h = System.currentTimeMillis();
        this.b.s.sendEmptyMessageDelayed(0, 0);
        Intent intent = new Intent(this.b, SetRegisterPhoneNumPasswordActivity.class);
        intent.putExtras(this.b.getIntent());
        intent.putExtra("verifycode", this.b.d.getText().toString());
        intent.putExtra("user_phone", this.b.g);
        intent.putExtra("is_hottalk_account", this.b.i);
        this.b.startActivityForResult(intent, 2);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                if (70002039 == errorStatus.getErrorCode() || 70001201 == errorStatus.getErrorCode() || 70002003 == errorStatus.getErrorCode()) {
                    this.b.d.setError(this.b.getString(m.a(this.b, "CS_incorrect_verificode")));
                    this.b.d.requestFocus();
                    this.b.d.selectAll();
                    this.b.f.a(d.a());
                    this.b.f.c(String.valueOf(errorStatus.getErrorCode()));
                    this.b.f.d(errorStatus.getErrorReason());
                    com.huawei.hwid.core.a.d.a(this.b.f, this.b);
                }
            }
            this.b.a(j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).show());
        }
        super.onFail(bundle);
    }
}
