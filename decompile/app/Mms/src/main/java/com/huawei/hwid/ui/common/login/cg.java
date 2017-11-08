package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.c;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class cg extends c {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;

    public cg(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(setRegisterPhoneNumPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        Intent intent = new Intent("com.huawei.third.ACTION_UPGRADE_SUCCESS");
        intent.putExtra("requestTokenType", this.b.k());
        intent.putExtras(this.b.getIntent());
        intent.putExtras(bundle);
        intent.setPackage("com.huawei.hwid");
        if (1 != this.b.r) {
            this.b.startActivityForResult(intent, 1);
            return;
        }
        intent.putExtra("displayName", d.h(this.b.f));
        this.b.startActivityForResult(intent, 100);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            this.b.b();
            this.b.i.a(d.a());
            if (errorStatus != null) {
                if (70002002 == errorStatus.getErrorCode()) {
                    if (this.b.j) {
                        this.b.t();
                    } else {
                        this.b.s();
                        this.b.a(this.b.getString(m.a(this.b, "CS_logining_message")));
                    }
                    this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
                } else if (70002039 != errorStatus.getErrorCode()) {
                    this.b.t();
                    this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("authcode_invalid", true);
                    this.b.setResult(-1, intent);
                    this.b.finish();
                }
            }
            com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        }
        super.onFail(bundle);
    }
}
