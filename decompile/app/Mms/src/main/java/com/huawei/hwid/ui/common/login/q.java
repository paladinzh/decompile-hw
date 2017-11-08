package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.manager.g;

/* compiled from: ManageAgreementActivity */
class q implements OnClickListener {
    final /* synthetic */ ManageAgreementActivity a;

    q(ManageAgreementActivity manageAgreementActivity) {
        this.a = manageAgreementActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        a.b("ManageAgreementActivity", "not agree new terms, need to quit account");
        g a = f.a(this.a);
        if (a.c(this.a, this.a.t)) {
            d.b(this.a, false);
            a.a(this.a, this.a.t, null, new r(this));
            return;
        }
        this.a.a(false, null);
    }
}
