package com.huawei.hwid.ui.common.setting;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;

/* compiled from: ModifyPasswordActivity */
class g extends c {
    final /* synthetic */ ModifyPasswordActivity b;

    public g(ModifyPasswordActivity modifyPasswordActivity, Context context, a aVar) {
        this.b = modifyPasswordActivity;
        super(modifyPasswordActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        AccountManager accountManager = AccountManager.get(this.b);
        if (!TextUtils.isEmpty(this.b.a)) {
            f.a(this.b).d(this.b, this.b.a);
            if (d.l(this.b) && d.j(this.b)) {
                Account account = new Account(this.b.a, "com.huawei.hwid");
                accountManager.setAuthToken(account, "cloud", accountManager.peekAuthToken(account, "cloud"));
            }
        }
        j.a(this.b, m.a(this.b, "CS_modify_pwd_succ_new"));
        this.b.g();
    }

    public void onFail(Bundle bundle) {
        if (bundle.getBoolean("isRequestSuccess", false)) {
            j.a(this.b, m.a(this.b, "CS_error_old_pwd_message"));
            Intent intent = new Intent();
            intent.putExtra("old_pass_error", true);
            this.b.setResult(-1, intent);
            this.b.finish();
        }
        super.onFail(bundle);
    }
}
