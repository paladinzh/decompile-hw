package com.huawei.hwid.ui.common.login;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class az implements OnClickListener {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity a;

    az(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity) {
        this.a = registerViaPhoneNumVerificationActivity;
    }

    public void onClick(View view) {
        if (this.a.k) {
            this.a.h = System.currentTimeMillis();
            if (d.a(this.a)) {
                this.a.s.sendEmptyMessageDelayed(0, 0);
            } else {
                this.a.k = true;
                this.a.c.setText(this.a.getString(m.a(this.a, "CS_retrieve")));
                this.a.s.removeMessages(0);
            }
            if (!TextUtils.isEmpty(this.a.g) && this.a.g.contains("+")) {
                this.a.g = this.a.g.replace("+", "00");
            }
            this.a.b(this.a.g);
            return;
        }
        j.a(this.a, m.a(this.a, "CS_overload_message"));
    }
}
