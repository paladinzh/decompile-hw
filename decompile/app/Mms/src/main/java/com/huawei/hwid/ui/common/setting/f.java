package com.huawei.hwid.ui.common.setting;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.core.encrypt.e;

/* compiled from: ModifyPasswdBaseActivity */
class f implements OnClickListener {
    final /* synthetic */ ModifyPasswdBaseActivity a;

    f(ModifyPasswdBaseActivity modifyPasswdBaseActivity) {
        this.a = modifyPasswdBaseActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.b(e.d(this.a, this.a.f.getText().toString()));
    }
}
