package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.a.c;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class ay implements OnClickListener {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity a;

    ay(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity) {
        this.a = registerViaPhoneNumVerificationActivity;
    }

    public void onClick(View view) {
        if (this.a.t()) {
            this.a.f = new c(this.a, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.a.g);
            this.a.g();
        }
    }
}
