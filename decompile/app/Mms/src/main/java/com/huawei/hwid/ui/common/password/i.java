package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdTypeActivity */
class i implements OnClickListener {
    final /* synthetic */ FindpwdTypeActivity a;

    i(FindpwdTypeActivity findpwdTypeActivity) {
        this.a = findpwdTypeActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
