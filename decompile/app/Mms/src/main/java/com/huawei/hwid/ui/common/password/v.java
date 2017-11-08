package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: ResetPwdByPhoneNumberActivity */
class v extends c {
    final /* synthetic */ ResetPwdByPhoneNumberActivity b;

    public v(ResetPwdByPhoneNumberActivity resetPwdByPhoneNumberActivity, Context context, a aVar) {
        this.b = resetPwdByPhoneNumberActivity;
        super(resetPwdByPhoneNumberActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        f.a(this.b).d(this.b, this.b.a);
        j.a(this.b, this.b.getString(m.a(this.b, "CS_reset_pwd_succ_message")), 1);
        this.b.setResult(-1);
        this.b.finish();
    }

    public void onFail(Bundle bundle) {
        if (bundle == null) {
            com.huawei.hwid.core.c.b.a.b("ResetPwdByPhoneNumberActivity", "OnFial bundle is null ");
            bundle = new Bundle();
        } else if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus != null && 70002039 == errorStatus.getErrorCode()) {
                Intent intent = new Intent();
                intent.putExtra("authcode_invalid", true);
                this.b.setResult(-1, intent);
                this.b.finish();
            }
        }
        super.onFail(bundle);
    }
}
