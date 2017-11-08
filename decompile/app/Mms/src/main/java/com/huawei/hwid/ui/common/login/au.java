package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.ui.common.f;

/* compiled from: RegisterViaEmailActivity */
class au implements OnClickListener {
    final /* synthetic */ RegisterViaEmailActivity a;

    au(RegisterViaEmailActivity registerViaEmailActivity) {
        this.a = registerViaEmailActivity;
    }

    public void onClick(View view) {
        if (this.a.s()) {
            this.a.d = this.a.a.getText().toString();
            this.a.e = new c(this.a, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, this.a.d);
            if (f.FromOOBE == this.a.q() || this.a.f) {
                this.a.e.a(true);
            }
            this.a.t();
        }
    }
}
