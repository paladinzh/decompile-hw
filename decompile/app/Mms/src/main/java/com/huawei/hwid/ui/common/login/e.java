package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

/* compiled from: LoginActivity */
class e extends i {
    final /* synthetic */ LoginActivity a;

    e(LoginActivity loginActivity, Context context, EditText editText) {
        this.a = loginActivity;
        super(context, editText);
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.onTextChanged(charSequence, i, i2, i3);
        if (this.a.c == null || TextUtils.isEmpty(this.a.c.getText().toString())) {
            this.a.a.setEnabled(false);
        } else {
            this.a.a.setEnabled(true);
        }
    }
}
