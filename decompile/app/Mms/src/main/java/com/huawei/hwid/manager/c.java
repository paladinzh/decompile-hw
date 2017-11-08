package com.huawei.hwid.manager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.simchange.b.b;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;

/* compiled from: AccountManagerActivity */
class c extends com.huawei.hwid.ui.common.c {
    final /* synthetic */ AccountManagerActivity b;

    public c(AccountManagerActivity accountManagerActivity, Context context) {
        this.b = accountManagerActivity;
        super(accountManagerActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.i.a(d.a());
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        if (d.j(this.b) && d.l(this.b)) {
            String c = this.b.c;
            AccountManager accountManager = AccountManager.get(this.b);
            String peekAuthToken = accountManager.peekAuthToken(this.b.e, "cloud");
            if (!(TextUtils.isEmpty(c) || "cloud".equals(c))) {
                peekAuthToken = d.b(peekAuthToken, c);
            }
            Account[] accountsByType = accountManager.getAccountsByType("com.huawei.hwid");
            if (accountsByType != null && accountsByType.length > 0) {
                Account account = accountsByType[0];
                if (this.b.g && "blocked".equals(b.a(this.b))) {
                    if (!this.b.h) {
                        b.a(this.b, accountManager.getUserData(account, "userId"), 11);
                        return;
                    }
                    return;
                } else if ("noaccount".equals(b.a(this.b)) && b.g(this.b)) {
                    b.c(this.b, account.name, "normal");
                }
            }
            Intent intent = new Intent();
            if (this.b.d != f.FromApp) {
                intent.putExtra("authAccount", this.b.e.name);
                intent.putExtra("accountType", "com.huawei.hwid");
                intent.putExtra("authtoken", peekAuthToken);
                this.b.setResult(-1, intent);
            } else {
                intent.putExtra("bundle", e.a(this.b.getBaseContext(), this.b.e.name, this.b.c));
                this.b.a(this.b.e.name, intent, this.b.c);
            }
        } else {
            this.b.a(this.b.e.name, null, this.b.getPackageName());
        }
        this.b.finish();
    }

    public void onFail(Bundle bundle) {
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (bundle.getBoolean("isRequestSuccess", false)) {
            this.b.i.a(d.a());
            if (70002015 == errorStatus.getErrorCode() || 70002016 == errorStatus.getErrorCode()) {
                this.b.removeDialog(1);
                g a = f.a(this.b);
                if (d.j(this.b) && d.l(this.b)) {
                    a.a(this.b, this.b.e.name, null, new d(this));
                } else {
                    a.a(this.b, this.b.e.name, this.b.c);
                    this.b.a(this.b.e);
                }
                j.a(this.b, this.b.getString(m.a(this.b, "CS_account_change")), 1);
            } else {
                onSuccess(null);
            }
        }
        this.b.i.c(String.valueOf(errorStatus.getErrorCode()));
        this.b.i.d(errorStatus.getErrorReason());
        com.huawei.hwid.core.a.d.a(this.b.i, this.b);
        super.onFail(bundle);
    }
}
