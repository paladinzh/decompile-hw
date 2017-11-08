package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.widget.EditText;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.ui.common.c.b;

/* compiled from: LoginActivity */
class d extends b {
    final /* synthetic */ LoginActivity a;

    d(LoginActivity loginActivity, EditText editText) {
        this.a = loginActivity;
        super(editText);
    }

    public void a(View view, boolean z) {
        if (!z && this.a.b.getText().length() != 0) {
            if (p.b(this.a.b.getText().toString())) {
                this.a.b.setError(null);
            } else {
                this.a.b.setError(this.a.getString(m.a(this.a, "CS_login_username_error")));
            }
        }
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        this.a.b.setError(null);
        super.onTextChanged(charSequence, i, i2, i3);
    }
}
