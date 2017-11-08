package com.huawei.hwid.ui.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

/* compiled from: BaseActivity */
class a extends ProgressDialog {
    final /* synthetic */ BaseActivity a;

    a(BaseActivity baseActivity, Context context, int i) {
        this.a = baseActivity;
        super(context, i);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.a.a(i, keyEvent)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
