package com.huawei.hwid.manager;

import android.accounts.Account;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: AccountManagerActivity */
class a implements OnClickListener {
    final /* synthetic */ int a;
    final /* synthetic */ String[] b;
    final /* synthetic */ AccountManagerActivity c;

    a(AccountManagerActivity accountManagerActivity, int i, String[] strArr) {
        this.c = accountManagerActivity;
        this.a = i;
        this.b = strArr;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        boolean z;
        if (i != this.a - 1) {
            z = false;
            this.c.e = new Account(this.b[i], "com.huawei.hwid");
        } else {
            this.c.e = null;
            z = true;
        }
        this.c.d(z);
    }
}
