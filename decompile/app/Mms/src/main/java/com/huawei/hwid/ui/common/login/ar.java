package com.huawei.hwid.ui.common.login;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.ui.common.c.b;

/* compiled from: RegisterViaEmailActivity */
class ar extends b {
    final /* synthetic */ RegisterViaEmailActivity a;

    ar(RegisterViaEmailActivity registerViaEmailActivity, EditText editText) {
        this.a = registerViaEmailActivity;
        super(editText);
    }

    public String a() {
        return this.a.getString(m.a(this.a, "CS_email_address_error"));
    }

    public void a(View view, boolean z) {
        if (!z && this.a.a != null && this.a.a.getText() != null && this.a.a.getText().length() != 0) {
            if (!p.b(this.a.a.getText().toString())) {
                this.a.a.setError(this.a.getString(m.a(this.a, "CS_login_username_error")));
            } else if (!p.a(this.a.a.getText().toString())) {
                this.a.a.setError(this.a.getString(m.a(this.a, "CS_email_address_error")));
            }
        }
    }

    public void afterTextChanged(Editable editable) {
        if (editable != null && editable.toString() != null && editable.toString().startsWith(" ") && editable.toString().trim() != null && editable.toString().trim().length() > 0 && this.a.a != null) {
            this.a.a.setText(editable.toString().trim());
            this.a.a.setSelection(editable.toString().trim().length());
        }
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.onTextChanged(charSequence, i, i2, i3);
        this.a.a.setError(null);
    }
}
