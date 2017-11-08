package com.huawei.hwid.ui.common.login;

import android.text.Editable;
import android.text.TextWatcher;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class bb implements TextWatcher {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity a;

    bb(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity) {
        this.a = registerViaPhoneNumVerificationActivity;
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
