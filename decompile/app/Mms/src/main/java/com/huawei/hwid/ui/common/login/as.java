package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

/* compiled from: RegisterViaEmailActivity */
class as implements OnClickListener {
    final /* synthetic */ RegisterViaEmailActivity a;

    as(RegisterViaEmailActivity registerViaEmailActivity) {
        this.a = registerViaEmailActivity;
    }

    public void onClick(View view) {
        if (this.a.f()) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.a.getSystemService("input_method");
            if (inputMethodManager != null) {
                View currentFocus = this.a.getCurrentFocus();
                if (!(currentFocus == null || currentFocus.getWindowToken() == null)) {
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 2);
                }
            }
        }
        this.a.onBackPressed();
    }
}
