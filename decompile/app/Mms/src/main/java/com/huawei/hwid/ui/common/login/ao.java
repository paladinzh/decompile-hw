package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.manager.g;

/* compiled from: RegisterResetVerifyEmailActivity */
class ao extends l {
    final /* synthetic */ RegisterResetVerifyEmailActivity b;

    public ao(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity, Context context, a aVar, g gVar) {
        this.b = registerResetVerifyEmailActivity;
        super(registerResetVerifyEmailActivity, context, gVar);
    }

    public void onSuccess(Bundle bundle) {
        this.b.b(false);
        super.onSuccess(bundle);
        if (!a()) {
            return;
        }
        if (this.b.n && d.h(this.b)) {
            com.huawei.hwid.core.c.b.a.b("RegisterResetVerifyEmailActivity", "needActivateVip is true!  need to activate!");
            ap apVar = new ap(this, bundle);
            Bundle bundle2 = new Bundle();
            bundle2.putString("userID", b().c());
            bundle2.putString("deviceID", d.k(b().h()));
            bundle2.putString("deviceType", b().i());
            bundle2.putString("st", b().f());
            com.huawei.a.a.a.a(this.b, apVar, bundle2);
            return;
        }
        com.huawei.hwid.core.c.b.a.b("RegisterResetVerifyEmailActivity", "needActivateVip is false!");
        this.b.a(b(), bundle);
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.b();
            Intent intent = new Intent();
            intent.setClass(this.b, LoginActivity.class);
            intent.putExtra("loginWithUserName", true);
            intent.putExtra("loginWithUserType", "1");
            intent.putExtra("authAccount", this.b.k);
            intent.setFlags(67108864);
            this.b.startActivity(intent);
            this.b.finish();
        }
        super.onFail(bundle);
    }
}
