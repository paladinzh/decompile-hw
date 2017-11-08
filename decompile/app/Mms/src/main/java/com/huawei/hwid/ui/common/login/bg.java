package com.huawei.hwid.ui.common.login;

import android.text.Editable;
import android.text.TextWatcher;

/* compiled from: RegisterViaPhoneNumberActivity */
class bg implements TextWatcher {
    final /* synthetic */ RegisterViaPhoneNumberActivity a;

    bg(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity) {
        this.a = registerViaPhoneNumberActivity;
    }

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.a.b != null) {
            this.a.b.setError(null);
        }
    }
}
