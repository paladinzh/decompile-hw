package com.huawei.hwid.ui.common.c;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

/* compiled from: TextEditStyleAdapter */
public class b implements TextWatcher, OnFocusChangeListener {
    protected EditText c = null;
    int d = 0;

    public b(EditText editText) {
        this.c = editText;
        if (this.c != null) {
            this.c.addTextChangedListener(this);
            this.c.setOnFocusChangeListener(this);
        }
    }

    public String a() {
        return null;
    }

    public void a(View view, boolean z) {
    }

    public void onFocusChange(View view, boolean z) {
        if (!z && this.d > 0 && this.c != null && this.c.length() == 0) {
            this.c.setError(a());
        } else {
            a(view, z);
        }
    }

    public void afterTextChanged(Editable editable) {
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        this.d++;
    }
}
