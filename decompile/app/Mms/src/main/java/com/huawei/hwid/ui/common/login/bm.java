package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: RegisterViaPhoneNumberActivity */
class bm implements OnClickListener {
    final /* synthetic */ bl a;

    bm(bl blVar) {
        this.a = blVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.a.a.e.setText(this.a.a.a.l[i]);
        this.a.a.a.g = this.a.a.a.p[i];
    }
}
