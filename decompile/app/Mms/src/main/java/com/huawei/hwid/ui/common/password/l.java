package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdbyEmailActivity */
class l implements OnClickListener {
    final /* synthetic */ FindpwdbyEmailActivity a;

    l(FindpwdbyEmailActivity findpwdbyEmailActivity) {
        this.a = findpwdbyEmailActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
