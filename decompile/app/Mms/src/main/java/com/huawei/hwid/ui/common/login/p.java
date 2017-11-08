package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: ManageAgreementActivity */
class p implements OnClickListener {
    final /* synthetic */ ManageAgreementActivity a;

    p(ManageAgreementActivity manageAgreementActivity) {
        this.a = manageAgreementActivity;
    }

    public void onClick(View view) {
        if (!this.a.j.isChecked() || !this.a.k.isChecked()) {
            j.a(this.a, m.a(this.a, "CS_agree_policy_toast_new"));
        } else if ("1".equals(this.a.q)) {
            this.a.setResult(-1);
            this.a.finish();
        } else if ("2".equals(this.a.q)) {
            this.a.h();
        } else {
            a.d("ManageAgreementActivity", "type is unknown, finish activity");
            this.a.finish();
        }
    }
}
