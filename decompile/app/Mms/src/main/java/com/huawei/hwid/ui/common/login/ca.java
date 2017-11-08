package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.text.Editable;
import android.widget.EditText;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.ui.common.c.a;

/* compiled from: SetRegisterPhoneNumPasswordActivity */
class ca extends a {
    final /* synthetic */ SetRegisterPhoneNumPasswordActivity a;

    ca(SetRegisterPhoneNumPasswordActivity setRegisterPhoneNumPasswordActivity, Context context, EditText editText) {
        this.a = setRegisterPhoneNumPasswordActivity;
        super(context, editText);
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.beforeTextChanged(charSequence, i, i2, i3);
        this.a.s = charSequence.toString();
    }

    public void afterTextChanged(Editable editable) {
        if (!this.a.s.equals(editable.toString())) {
            g.a(this.a.d, this.a.getApplicationContext(), false);
        }
        g.a(this.a.d, this.a.e, this.a.getApplicationContext());
        g.a(this.a.d, this.a.e, this.a.b);
    }
}
