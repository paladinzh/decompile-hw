package com.huawei.hwid.ui.common;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;

/* compiled from: BaseActivity */
class d implements AccountManagerCallback {
    final /* synthetic */ c a;

    d(c cVar) {
        this.a = cVar;
    }

    public void run(AccountManagerFuture accountManagerFuture) {
        this.a.c.a();
    }
}
