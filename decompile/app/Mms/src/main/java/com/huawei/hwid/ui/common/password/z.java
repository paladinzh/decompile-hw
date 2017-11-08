package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.ui.common.j;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class z implements OnClickListener {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity a;

    z(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity) {
        this.a = resetPwdByPhoneNumberVerificationActivity;
    }

    public void onClick(View view) {
        if (this.a.l) {
            a.b("ResetPwdByPhoneNumberVerificationActivity", "mPhone: " + f.c(this.a.f));
            this.a.i = System.currentTimeMillis();
            if (d.a(this.a)) {
                this.a.o.sendEmptyMessageDelayed(2, 0);
            } else {
                this.a.l = true;
                this.a.c.setText(this.a.getString(m.a(this.a, "CS_retrieve")));
                this.a.o.removeMessages(2);
            }
            this.a.k();
            return;
        }
        j.a(this.a, m.a(this.a, "CS_overload_message"));
    }
}
