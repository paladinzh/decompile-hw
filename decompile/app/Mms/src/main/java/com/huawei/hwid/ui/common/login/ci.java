package com.huawei.hwid.ui.common.login;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.manager.g;
import com.huawei.hwid.simchange.b.b;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class ci extends l {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;

    public ci(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context, a aVar, g gVar) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(setRegisterPhoneNumPasswordActivity, context, gVar);
    }

    public void onSuccess(Bundle bundle) {
        this.b.a(false);
        super.onSuccess(bundle);
        this.b.i.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        if (b.g(this.a)) {
            Account p = d.p(this.a);
            if (p != null) {
                b.a(this.a, p.name, "normal");
            }
        }
        com.huawei.hwid.core.c.b.a.b("RegisterPhoneNumActivity", "log in successfully!");
        if (!a()) {
            return;
        }
        if (this.b.n && d.h(this.b)) {
            com.huawei.hwid.core.c.b.a.b("RegisterPhoneNumActivity", "needActivateVip is true!");
            cj cjVar = new cj(this, bundle);
            Bundle bundle2 = new Bundle();
            bundle2.putString("userID", b().c());
            bundle2.putString("deviceID", d.k(b().h()));
            bundle2.putString("deviceType", b().i());
            bundle2.putString("st", b().f());
            com.huawei.a.a.a.a(this.b, cjVar, bundle2);
            return;
        }
        com.huawei.hwid.core.c.b.a.b("RegisterPhoneNumActivity", "needActivateVip is false!");
        this.b.a(b(), bundle);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            Intent intent = new Intent();
            intent.setClass(this.b, LoginActivity.class);
            intent.putExtra("loginWithUserName", true);
            intent.putExtra("loginWithUserType", "1");
            intent.putExtra("authAccount", this.b.f);
            intent.setFlags(67108864);
            this.b.startActivity(intent);
            this.b.finish();
        }
        this.b.i.a(d.a());
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.i.d(errorStatus.getErrorReason());
        }
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        super.onFail(bundle);
    }
}
