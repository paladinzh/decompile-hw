package com.huawei.hwid.ui.common.setting;

import android.content.Context;
import android.text.Editable;
import android.widget.EditText;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.ui.common.c.a;

/* compiled from: ModifyPasswdBaseActivity */
class b extends a {
    final /* synthetic */ ModifyPasswdBaseActivity a;

    b(ModifyPasswdBaseActivity modifyPasswdBaseActivity, Context context, EditText editText) {
        this.a = modifyPasswdBaseActivity;
        super(context, editText);
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.beforeTextChanged(charSequence, i, i2, i3);
        this.a.k = charSequence.toString();
    }

    public void afterTextChanged(Editable editable) {
        if (!this.a.k.equals(editable.toString())) {
            g.a(this.a.f, this.a.getApplicationContext(), false);
        }
        g.a(this.a.f, this.a.g, this.a.getApplicationContext());
        g.a(this.a.f, this.a.g, this.a.d);
    }
}
