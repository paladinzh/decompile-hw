package com.huawei.hwid.ui.common.login.a;

import android.text.TextUtils;
import android.widget.EditText;
import com.huawei.hwid.ui.common.c.b;

/* compiled from: PwdDialogFragment */
class k extends b {
    final /* synthetic */ a a;

    k(a aVar, EditText editText) {
        this.a = aVar;
        super(editText);
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        super.onTextChanged(charSequence, i, i2, i3);
        if (this.a.b == null || TextUtils.isEmpty(this.a.b.getText().toString())) {
            this.a.r.setEnabled(false);
        } else {
            this.a.r.setEnabled(true);
        }
    }
}
