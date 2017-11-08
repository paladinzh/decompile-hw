package com.huawei.hwid.manager;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;

/* compiled from: AccountManagerActivity */
class d implements AccountManagerCallback {
    final /* synthetic */ c a;

    d(c cVar) {
        this.a = cVar;
    }

    public void run(AccountManagerFuture accountManagerFuture) {
        this.a.b.a(this.a.b.e);
    }
}
