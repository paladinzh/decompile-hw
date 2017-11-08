package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.j;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.t;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.c;
import java.util.List;

/* compiled from: RegisterResetVerifyEmailActivity */
class an extends c {
    final /* synthetic */ RegisterResetVerifyEmailActivity b;

    public an(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity, Context context) {
        this.b = registerResetVerifyEmailActivity;
        super(registerResetVerifyEmailActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        boolean z;
        super.onSuccess(bundle);
        List<UserAccountInfo> parcelableArrayList = bundle.getParcelableArrayList("accountsInfo");
        if (parcelableArrayList == null) {
            z = false;
        } else {
            for (UserAccountInfo userAccountInfo : parcelableArrayList) {
                if ("1".equals(userAccountInfo.getAccountType())) {
                    z = userAccountInfo.getAccountState().equals("1");
                }
            }
            z = false;
        }
        if (!z) {
            this.b.b();
            this.b.b(this.b.getString(m.a(this.b, "CS_register_email_not_verified_info")));
        } else if (this.b.r) {
            Bundle bundle2 = new Bundle();
            bundle2.putBoolean("needActivateVip", this.b.n);
            if (this.b.p) {
                this.b.onBackPressed();
            } else if (this.b.l) {
                Class a = j.a("com.huawei.third.ui.BindWeixinAccountSuccessActivity");
                if (a != null) {
                    Intent intent = new Intent(this.b, a);
                    intent.putExtras(this.b.getIntent().getExtras());
                    intent.putExtra("open_weixin_from_login_or_register", "register");
                    this.b.startActivityForResult(intent, 1235);
                } else {
                    a.b("RegisterResetVerifyEmailActivity", "cls is null, onSuccess error");
                }
            } else {
                com.huawei.hwid.core.model.http.a tVar = new t(this.b, this.b.k, com.huawei.hwid.a.a().d(), this.b.k(), bundle2);
                i.a(this.b, tVar, null, this.b.a(new ao(this.b, this.b, tVar, f.a(this.b))));
            }
        } else {
            this.b.b();
            Intent intent2 = new Intent();
            intent2.putExtra("isVerified", true);
            this.b.setResult(-1, intent2);
            this.b.finish();
        }
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            Dialog create = com.huawei.hwid.ui.common.j.a(this.a, m.a(this.a, "CS_server_unavailable_message"), m.a(this.a, "CS_server_unavailable_title")).create();
            this.b.a(create);
            create.show();
        }
        super.onFail(bundle);
    }
}
