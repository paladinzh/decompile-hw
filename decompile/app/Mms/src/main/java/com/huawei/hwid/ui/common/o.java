package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.ui.common.login.ManageAgreementActivity;

/* compiled from: UIUtil */
final class o implements OnClickListener {
    final /* synthetic */ Bundle a;
    final /* synthetic */ Activity b;
    final /* synthetic */ String c;
    final /* synthetic */ HwAccount d;
    final /* synthetic */ int e;

    o(Bundle bundle, Activity activity, String str, HwAccount hwAccount, int i) {
        this.a = bundle;
        this.b = activity;
        this.c = str;
        this.d = hwAccount;
        this.e = i;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        String string = this.a.getString("agrFlags");
        String string2 = this.a.getString("userId");
        String string3 = this.a.getString("userName");
        String valueOf = String.valueOf(this.a.getInt("siteId"));
        Intent intent = new Intent();
        intent.setClass(this.b, ManageAgreementActivity.class);
        intent.putExtra("typeEnterAgree", "2");
        intent.putExtra("argFlags", string);
        intent.putExtra("userId", string2);
        intent.putExtra("accountName", string3);
        intent.putExtra("siteId", valueOf);
        intent.putExtra("topActivity", this.c);
        if (this.d != null) {
            intent.putExtra("cacheAccount", this.d);
        }
        this.b.startActivityForResult(intent, this.e);
    }
}
