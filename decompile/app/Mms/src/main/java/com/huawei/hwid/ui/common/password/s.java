package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdbyPhonenumberActivity */
class s implements OnClickListener {
    final /* synthetic */ FindpwdbyPhonenumberActivity a;

    s(FindpwdbyPhonenumberActivity findpwdbyPhonenumberActivity) {
        this.a = findpwdbyPhonenumberActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
