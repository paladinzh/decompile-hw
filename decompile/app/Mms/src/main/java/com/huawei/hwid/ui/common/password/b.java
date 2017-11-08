package com.huawei.hwid.ui.common.password;

import android.text.Editable;
import android.text.TextWatcher;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;

/* compiled from: FindpwdByHwIdActivity */
class b implements TextWatcher {
    final /* synthetic */ FindpwdByHwIdActivity a;

    b(FindpwdByHwIdActivity findpwdByHwIdActivity) {
        this.a = findpwdByHwIdActivity;
    }

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (this.a.a == null || p.b(this.a.a.getText().toString())) {
            this.a.a.setError(null);
        } else {
            this.a.a.setError(this.a.getString(m.a(this.a, "CS_login_username_error")));
        }
    }
}
