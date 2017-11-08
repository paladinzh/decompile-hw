package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.ui.common.c;

/* compiled from: ManageAgreementActivity */
class u extends c {
    final /* synthetic */ ManageAgreementActivity b;

    public u(ManageAgreementActivity manageAgreementActivity, Context context) {
        this.b = manageAgreementActivity;
        super(manageAgreementActivity, context);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        this.b.b();
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        this.b.b();
        this.b.setResult(-1);
        this.b.finish();
    }
}
