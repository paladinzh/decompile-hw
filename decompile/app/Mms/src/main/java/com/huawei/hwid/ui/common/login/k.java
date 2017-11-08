package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import com.huawei.hwid.a;

/* compiled from: LoginRegisterCommonActivity */
class k implements OnDismissListener {
    final /* synthetic */ LoginRegisterCommonActivity a;

    k(LoginRegisterCommonActivity loginRegisterCommonActivity) {
        this.a = loginRegisterCommonActivity;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        a.a().b("AreaNotAllowException");
        this.a.a(false, null);
        this.a.finish();
    }
}
