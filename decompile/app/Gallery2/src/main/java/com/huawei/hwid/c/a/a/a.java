package com.huawei.hwid.c.a.a;

import android.app.AlertDialog;
import android.content.Context;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.m;
import java.security.AccessController;

public class a extends AlertDialog {
    public a(Context context) {
        super(context, m.a(context));
    }

    public void onBackPressed() {
        a(true);
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            e.d("CustomAlertDialog", "catch Exception", e);
        }
    }

    public boolean onSearchRequested() {
        a(true);
        return super.onSearchRequested();
    }

    public void a(boolean z) {
        try {
            AccessController.doPrivileged(new b(this, z));
        } catch (Exception e) {
            e.d("CustomAlertDialog", "Exception: " + e.getMessage());
        }
    }
}
