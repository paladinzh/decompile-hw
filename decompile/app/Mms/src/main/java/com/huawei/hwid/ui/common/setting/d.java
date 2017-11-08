package com.huawei.hwid.ui.common.setting;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.ui.common.j;

/* compiled from: ModifyPasswdBaseActivity */
class d implements OnClickListener {
    final /* synthetic */ ModifyPasswdBaseActivity a;
    private boolean b = false;

    d(ModifyPasswdBaseActivity modifyPasswdBaseActivity) {
        this.a = modifyPasswdBaseActivity;
    }

    public void onClick(View view) {
        boolean z = false;
        if (!this.b) {
            z = true;
        }
        this.b = z;
        j.a(this.a, this.a.f, this.a.h, this.b);
        j.a(this.a, this.a.g, this.a.h, this.b);
    }
}
