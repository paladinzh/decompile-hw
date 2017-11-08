package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

/* compiled from: ManageAgreementActivity */
class n implements OnClickListener {
    final /* synthetic */ ManageAgreementActivity a;

    n(ManageAgreementActivity manageAgreementActivity) {
        this.a = manageAgreementActivity;
    }

    public void onClick(View view) {
        boolean z = false;
        CheckBox a = this.a.j;
        if (!this.a.j.isChecked()) {
            z = true;
        }
        a.setChecked(z);
    }
}
