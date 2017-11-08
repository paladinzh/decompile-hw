package com.huawei.hwid.ui.common.login;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.p;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterResetVerifyEmailActivity */
class aj implements OnClickListener {
    final /* synthetic */ RegisterResetVerifyEmailActivity a;

    aj(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity) {
        this.a = registerResetVerifyEmailActivity;
    }

    public void onClick(View view) {
        if (!this.a.r && !this.a.s) {
            this.a.onBackPressed();
        } else if (d.a(this.a)) {
            this.a.a(null);
            this.a.a(true);
            i.a(this.a, new p(this.a, this.a.k, this.a.q), this.a.k, this.a.a(new an(this.a, this.a)));
        } else {
            Dialog create = j.a(this.a, m.a(this.a, "CS_network_connect_error"), m.a(this.a, "CS_notification")).create();
            this.a.a(create);
            create.show();
        }
    }
}
