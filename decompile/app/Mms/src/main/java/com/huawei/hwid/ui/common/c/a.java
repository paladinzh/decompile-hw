package com.huawei.hwid.ui.common.c;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;

/* compiled from: PasswordStyleAdapter */
public class a extends b {
    Context b = null;

    public a(Context context, EditText editText) {
        super(editText);
        this.b = context;
    }

    public void a(View view, boolean z) {
        super.a(view, z);
        if (this.c != null) {
            if (!p.a(this.c.getText().toString())) {
                this.c.setError(this.b.getString(m.a(this.b, "CS_error_have_special_symbol")));
            }
        }
    }

    protected void a(Editable editable) {
        if (editable != null && editable.length() > 32) {
            editable.delete(32, editable.length());
            this.c.setText(editable);
            this.c.setSelection(editable.length());
        }
    }

    public void afterTextChanged(Editable editable) {
        super.afterTextChanged(editable);
        a(editable);
        if (this.c != null) {
            if (!p.a(this.c.getText().toString())) {
                this.c.setError(this.b.getString(m.a(this.b, "CS_error_have_special_symbol")));
                return;
            }
        }
        if (this.c != null) {
            this.c.setError(null);
        }
    }
}
