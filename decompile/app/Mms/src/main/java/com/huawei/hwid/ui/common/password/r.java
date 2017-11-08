package com.huawei.hwid.ui.common.password;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: FindpwdbyPhonenumberActivity */
class r implements OnClickListener {
    final /* synthetic */ q a;

    r(q qVar) {
        this.a = qVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.a.c.setText((CharSequence) this.a.a.f.get(i));
        this.a.a.g = i;
    }
}
