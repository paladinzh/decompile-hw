package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: LoginRegisterCommonActivity */
class j implements OnDismissListener {
    final /* synthetic */ LoginRegisterCommonActivity a;

    j(LoginRegisterCommonActivity loginRegisterCommonActivity) {
        this.a = loginRegisterCommonActivity;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        this.a.a(false, null);
        this.a.finish();
    }
}
