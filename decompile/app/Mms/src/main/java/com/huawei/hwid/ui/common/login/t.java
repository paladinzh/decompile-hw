package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.ui.common.c;
import java.util.List;

/* compiled from: ManageAgreementActivity */
class t extends c {
    final /* synthetic */ ManageAgreementActivity b;

    public t(ManageAgreementActivity manageAgreementActivity, Context context) {
        this.b = manageAgreementActivity;
        super(manageAgreementActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        List parcelableArrayList = bundle.getParcelableArrayList("new_agrs");
        if (parcelableArrayList != null) {
            List<AgreementVersion> a = d.a(parcelableArrayList);
            AgreementVersion[] agreementVersionArr = new AgreementVersion[a.size()];
            String str = this.b.u + "-" + d.f(this.b);
            int i = 0;
            for (AgreementVersion agreementVersion : a) {
                agreementVersion.b(str);
                int i2 = i + 1;
                agreementVersionArr[i] = agreementVersion;
                i = i2;
            }
            this.b.a(agreementVersionArr);
        }
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        this.b.b();
    }
}
