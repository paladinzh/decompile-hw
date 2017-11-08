package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class ac extends c {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity b;

    public ac(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity, Context context) {
        this.b = resetPwdByPhoneNumberVerificationActivity;
        super(resetPwdByPhoneNumberVerificationActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        Intent intent = new Intent(this.b, ResetPwdByPhoneNumberActivity.class);
        intent.putExtra("verifycode", this.b.d.getText().toString());
        intent.putExtra("phoneNumber", this.b.f);
        intent.putExtra("hwid", this.b.j);
        intent.putExtra("siteId", this.b.k);
        this.b.startActivityForResult(intent, 2);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null) {
                if (70002039 == errorStatus.getErrorCode() || 70001201 == errorStatus.getErrorCode() || 70002003 == errorStatus.getErrorCode()) {
                    this.b.d.setError(this.b.getString(m.a(this.b, "CS_input_right_verifycode")));
                    this.b.d.requestFocus();
                    this.b.d.selectAll();
                } else if (errorStatus.getErrorCode() != 70002001) {
                    this.b.a(j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).show());
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("error_prompt", this.b.getString(m.a(this.b, "CS_username_not_exist")));
                    this.b.setResult(1, intent);
                    this.b.finish();
                    this.b.l = true;
                    this.b.c.setText(this.b.getString(m.a(this.b, "CS_retrieve")));
                    this.b.o.removeMessages(2);
                }
            }
        }
        super.onFail(bundle);
    }
}
