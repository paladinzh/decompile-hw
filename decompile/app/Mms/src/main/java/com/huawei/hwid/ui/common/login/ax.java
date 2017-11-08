package com.huawei.hwid.ui.common.login;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class ax implements OnClickListener {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity a;

    ax(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity) {
        this.a = registerViaPhoneNumVerificationActivity;
    }

    public void onClick(View view) {
        if (this.a.e.isChecked()) {
            this.a.v();
            if (!TextUtils.isEmpty(this.a.l)) {
                this.a.d.setText(this.a.l);
                this.a.d.setSelection(this.a.l.length());
                return;
            }
            return;
        }
        if (this.a.p != null) {
            this.a.getContentResolver().unregisterContentObserver(this.a.p);
        }
        if (this.a.f()) {
            j.a(this.a, m.a(this.a, "CS_read_verify_code_warn"));
        }
    }
}
