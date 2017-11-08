package com.huawei.hwid.ui.common.setting;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.ui.common.c.a;

/* compiled from: ModifyPasswdBaseActivity */
class c extends a {
    final /* synthetic */ ModifyPasswdBaseActivity a;

    c(ModifyPasswdBaseActivity modifyPasswdBaseActivity, Context context, EditText editText) {
        this.a = modifyPasswdBaseActivity;
        super(context, editText);
    }

    public void afterTextChanged(Editable editable) {
        g.a(this.a.f, this.a.g, this.a.getApplicationContext());
        g.a(this.a.f, this.a.g, this.a.d);
    }

    public void onFocusChange(View view, boolean z) {
        if (this.a.f.getText().length() > 0 && z && !g.a(this.a.f, this.a.getApplicationContext(), true) && this.a.b) {
            this.a.f.requestFocus();
            this.a.b = false;
        }
        g.a(this.a.f, this.a.g, this.a.d);
    }
}
