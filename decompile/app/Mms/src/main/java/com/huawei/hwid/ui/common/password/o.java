package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdbyEmailActivity */
class o implements OnClickListener {
    final /* synthetic */ FindpwdbyEmailActivity a;

    o(FindpwdbyEmailActivity findpwdbyEmailActivity) {
        this.a = findpwdbyEmailActivity;
    }

    public void onClick(View view) {
        String str = "";
        if (this.a.c.getText() != null) {
            str = this.a.c.getText().toString();
        }
        this.a.b(str);
    }
}
