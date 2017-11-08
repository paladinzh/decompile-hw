package com.huawei.hwid.ui.common.password;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: FindpwdbyEmailActivity */
class n implements OnClickListener {
    final /* synthetic */ m a;

    n(m mVar) {
        this.a = mVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.a.c.setText(this.a.a.d[i]);
    }
}
