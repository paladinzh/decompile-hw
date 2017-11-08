package com.huawei.hwid.manager;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import com.huawei.hwid.core.c.b.a;

/* compiled from: AccountManagerActivity */
class b implements OnCancelListener {
    final /* synthetic */ AccountManagerActivity a;

    b(AccountManagerActivity accountManagerActivity) {
        this.a = accountManagerActivity;
    }

    public void onCancel(DialogInterface dialogInterface) {
        a.b("AccountManagerActivity", "OnCancel");
        this.a.h();
        this.a.finish();
    }
}
