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

/* compiled from: SetRegisterEmailPasswordActivity */
class bw extends l {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public bw(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context, a aVar, g gVar) {
        this.b = setRegisterEmailPasswordActivity;
        super(setRegisterEmailPasswordActivity, context, gVar);
    }

    public void onSuccess(Bundle bundle) {
        this.b.a(false);
        super.onSuccess(bundle);
        this.b.g.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        if (b.g(this.a)) {
            Account p = d.p(this.a);
            if (p != null) {
                b.a(this.a, p.name, "normal");
            }
        }
        if (!a()) {
            return;
        }
        if (this.b.n && d.h(this.b)) {
            com.huawei.hwid.core.c.b.a.b("RegisterEmailActivity", "needActivateVip is true");
            bx bxVar = new bx(this, bundle);
            Bundle bundle2 = new Bundle();
            bundle2.putString("userID", b().c());
            bundle2.putString("deviceID", d.k(b().h()));
            bundle2.putString("deviceType", b().i());
            bundle2.putString("st", b().f());
            com.huawei.a.a.a.a(this.b, bxVar, bundle2);
            return;
        }
        com.huawei.hwid.core.c.b.a.b("RegisterEmailActivity", "needActivateVip is false");
        this.b.r = b();
        this.b.a(this.b.r, bundle);
    }

    public void onFail(Bundle bundle) {
        boolean z = bundle.getBoolean("isRequestSuccess", false);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        com.huawei.hwid.core.c.b.a.b("RegisterEmailActivity", "onFail: isRequestSuccess " + z);
        if (errorStatus != null) {
            if (z) {
                this.b.b();
                Intent intent;
                if (70002071 != errorStatus.getErrorCode()) {
                    intent = new Intent();
                    intent.setClass(this.b, LoginActivity.class);
                    intent.putExtra("loginWithUserName", true);
                    intent.putExtra("loginWithUserType", "1");
                    intent.putExtra("authAccount", this.b.f);
                    intent.setFlags(67108864);
                    this.b.startActivity(intent);
                    this.b.finish();
                } else {
                    intent = new Intent(this.b, RegisterResetVerifyEmailActivity.class);
                    intent.putExtra("isFromRegister", true);
                    intent.putExtra("bundle", bundle);
                    intent.putExtra("emailName", this.b.f);
                    intent.putExtras(this.b.getIntent());
                    this.b.startActivityForResult(intent, 1);
                }
            }
            this.b.g.a(d.a());
            this.b.g.c(String.valueOf(errorStatus.getErrorCode()));
            this.b.g.d(errorStatus.getErrorReason());
            com.huawei.hwid.core.a.d.a(this.b.g, this.b);
        }
        super.onFail(bundle);
    }
}
