package com.huawei.hwid.ui.common.login;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import com.huawei.hwid.core.c.e;

/* compiled from: ManageAgreementActivity */
class r implements AccountManagerCallback {
    final /* synthetic */ q a;

    r(q qVar) {
        this.a = qVar;
    }

    public void run(AccountManagerFuture accountManagerFuture) {
        e.a(this.a.a, this.a.a.s, this.a.a.t);
        this.a.a.a(false, null);
    }
}
