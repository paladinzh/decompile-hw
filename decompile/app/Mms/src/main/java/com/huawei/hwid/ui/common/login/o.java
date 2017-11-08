package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

/* compiled from: ManageAgreementActivity */
class o implements OnClickListener {
    final /* synthetic */ ManageAgreementActivity a;

    o(ManageAgreementActivity manageAgreementActivity) {
        this.a = manageAgreementActivity;
    }

    public void onClick(View view) {
        boolean z = false;
        CheckBox b = this.a.k;
        if (!this.a.k.isChecked()) {
            z = true;
        }
        b.setChecked(z);
    }
}
