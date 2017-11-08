package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.helper.handler.c;
import java.util.List;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class cf extends c {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity b;

    public cf(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context) {
        this.b = setRegisterPhoneNumPasswordActivity;
        super(context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        List parcelableArrayList = bundle.getParcelableArrayList("new_agrs");
        if (parcelableArrayList != null) {
            parcelableArrayList = d.a(parcelableArrayList);
        }
        if (r1 == null || r1.isEmpty()) {
            a.c("RegisterPhoneNumActivity", "curVers is null error");
            this.b.a(this.b.f, this.b.l, null);
            return;
        }
        AgreementVersion[] agreementVersionArr = new AgreementVersion[r1.size()];
        String str = this.b.l + "-" + d.f(this.b);
        int i = 0;
        for (AgreementVersion agreementVersion : r1) {
            agreementVersion.b(str);
            int i2 = i + 1;
            agreementVersionArr[i] = agreementVersion;
            i = i2;
        }
        this.b.a(this.b.f, this.b.l, agreementVersionArr);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        this.b.a(this.b.f, this.b.l, null);
    }
}
