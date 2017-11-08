package com.huawei.hwid.ui.common.login.a;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: PwdDialogFragment */
class g implements OnDismissListener {
    final /* synthetic */ a a;

    g(a aVar) {
        this.a = aVar;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        this.a.p = null;
    }
}
