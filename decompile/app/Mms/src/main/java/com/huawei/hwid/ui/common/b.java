package com.huawei.hwid.ui.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

/* compiled from: BaseActivity */
class b extends ProgressDialog {
    final /* synthetic */ BaseActivity a;

    b(BaseActivity baseActivity, Context context) {
        this.a = baseActivity;
        super(context);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.a.a(i, keyEvent)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
