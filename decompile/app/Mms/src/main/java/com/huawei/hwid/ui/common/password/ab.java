package com.huawei.hwid.ui.common.password;

import android.text.Editable;
import android.text.TextWatcher;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class ab implements TextWatcher {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity a;

    ab(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity) {
        this.a = resetPwdByPhoneNumberVerificationActivity;
    }

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.a.d != null) {
            this.a.d.setError(null);
        }
    }
}
