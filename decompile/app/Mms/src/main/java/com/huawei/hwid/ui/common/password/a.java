package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdByHwIdActivity */
class a implements OnClickListener {
    final /* synthetic */ FindpwdByHwIdActivity a;

    a(FindpwdByHwIdActivity findpwdByHwIdActivity) {
        this.a = findpwdByHwIdActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
