package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.helper.handler.c;
import java.util.List;

/* compiled from: SetRegisterEmailPasswordActivity */
class bt extends c {
    final /* synthetic */ SetRegisterEmailPasswordActivity b;

    public bt(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context) {
        this.b = setRegisterEmailPasswordActivity;
        super(context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        List parcelableArrayList = bundle.getParcelableArrayList("new_agrs");
        if (parcelableArrayList != null) {
            parcelableArrayList = d.a(parcelableArrayList);
        }
        if (r1 == null || r1.isEmpty()) {
            this.b.a(this.b.f, this.b.k, null);
            return;
        }
        AgreementVersion[] agreementVersionArr = new AgreementVersion[r1.size()];
        String str = this.b.k + "-" + d.f(this.b);
        int i = 0;
        for (AgreementVersion agreementVersion : r1) {
            agreementVersion.b(str);
            int i2 = i + 1;
            agreementVersionArr[i] = agreementVersion;
            i = i2;
        }
        this.b.a(this.b.f, this.b.k, agreementVersionArr);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        this.b.a(this.b.f, this.b.k, null);
    }
}
