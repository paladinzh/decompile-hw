package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.ui.common.c.a;

/* compiled from: SetRegisterEmailPasswordActivity */
class bp extends a {
    final /* synthetic */ SetRegisterEmailPasswordActivity a;

    bp(SetRegisterEmailPasswordActivity setRegisterEmailPasswordActivity, Context context, EditText editText) {
        this.a = setRegisterEmailPasswordActivity;
        super(context, editText);
    }

    public void afterTextChanged(Editable editable) {
        g.a(this.a.d, this.a.e, this.a.getApplicationContext());
        g.a(this.a.d, this.a.e, this.a.b);
    }

    public void onFocusChange(View view, boolean z) {
        if (this.a.d.getText().length() > 0 && z && !g.a(this.a.d, this.a.getApplicationContext(), true) && this.a.a) {
            this.a.d.requestFocus();
            this.a.a = false;
        }
        g.a(this.a.d, this.a.e, this.a.b);
    }
}
