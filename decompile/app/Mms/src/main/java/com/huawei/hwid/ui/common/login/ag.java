package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.d;

/* compiled from: RegisterResetVerifyEmailActivity */
class ag implements OnClickListener {
    final /* synthetic */ RegisterResetVerifyEmailActivity a;

    ag(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity) {
        this.a = registerResetVerifyEmailActivity;
    }

    public void onClick(View view) {
        this.a.y = System.currentTimeMillis();
        if (d.a(this.a)) {
            this.a.z.sendEmptyMessageDelayed(2, 0);
        } else {
            this.a.x = true;
            this.a.z.removeMessages(2);
        }
        this.a.f(this.a.k);
    }
}
