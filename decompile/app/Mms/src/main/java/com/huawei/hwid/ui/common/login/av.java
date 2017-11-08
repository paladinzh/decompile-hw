package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;

/* compiled from: RegisterViaEmailActivity */
class av extends c {
    final /* synthetic */ RegisterViaEmailActivity b;

    public av(RegisterViaEmailActivity registerViaEmailActivity, Context context) {
        this.b = registerViaEmailActivity;
        super(registerViaEmailActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        if ("0".equals(bundle.getString("isAccountExist"))) {
            Intent intent = new Intent(this.b, SetRegisterEmailPasswordActivity.class);
            intent.putExtras(this.b.getIntent());
            intent.putExtra("accountName", this.b.d);
            this.b.startActivityForResult(intent, 1);
        } else {
            this.b.a.setError(this.b.getString(m.a(this.b, "CS_email_already_exist")));
            this.b.a.requestFocus();
            this.b.a.selectAll();
        }
        this.b.e.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.e, this.b);
    }

    public void onFail(Bundle bundle) {
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (bundle.getBoolean("isRequestSuccess", false) && errorStatus != null && 70002001 == errorStatus.getErrorCode()) {
            Intent intent = new Intent(this.b, SetRegisterEmailPasswordActivity.class);
            intent.putExtras(this.b.getIntent());
            intent.putExtra("accountName", this.b.d);
            this.b.startActivityForResult(intent, 1);
        }
        this.b.e.a(d.a());
        if (errorStatus != null) {
            this.b.e.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.e.d(errorStatus.getErrorReason());
        }
        com.huawei.hwid.core.a.d.a(this.b.e, this.b);
        this.b.b();
        super.onFail(bundle);
    }
}
