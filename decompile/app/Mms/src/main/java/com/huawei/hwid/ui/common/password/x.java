package com.huawei.hwid.ui.common.password;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class x implements OnClickListener {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity a;

    x(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity) {
        this.a = resetPwdByPhoneNumberVerificationActivity;
    }

    public void onClick(View view) {
        if (this.a.e.isChecked()) {
            this.a.m();
            if (!TextUtils.isEmpty(this.a.m)) {
                this.a.d.setText(this.a.m);
                this.a.d.setSelection(this.a.m.length());
                return;
            }
            return;
        }
        if (this.a.n != null) {
            this.a.getContentResolver().unregisterContentObserver(this.a.n);
        }
        if (this.a.f()) {
            j.a(this.a, m.a(this.a, "CS_read_verify_code_warn"));
        }
    }
}
