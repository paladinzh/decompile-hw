package com.huawei.hwid.ui.common.login;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

/* compiled from: RegisterViaPhoneNumberActivity */
class be implements OnClickListener {
    final /* synthetic */ RegisterViaPhoneNumberActivity a;

    be(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity) {
        this.a = registerViaPhoneNumberActivity;
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
