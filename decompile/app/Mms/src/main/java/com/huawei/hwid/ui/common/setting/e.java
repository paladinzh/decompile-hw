package com.huawei.hwid.ui.common.setting;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: ModifyPasswdBaseActivity */
class e implements OnClickListener {
    final /* synthetic */ ModifyPasswdBaseActivity a;

    e(ModifyPasswdBaseActivity modifyPasswdBaseActivity) {
        this.a = modifyPasswdBaseActivity;
    }

    public void onClick(View view) {
        if (this.a.b(this.a.f, this.a.g)) {
            this.a.j = this.a.h();
            this.a.a(this.a.j);
            this.a.j.show();
            return;
        }
        this.a.d.setEnabled(false);
    }
}
