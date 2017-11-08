package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: FindpwdTypeActivity */
class h implements OnClickListener {
    final /* synthetic */ FindpwdTypeActivity a;

    h(FindpwdTypeActivity findpwdTypeActivity) {
        this.a = findpwdTypeActivity;
    }

    public void onClick(View view) {
        boolean z = false;
        FindpwdTypeActivity findpwdTypeActivity = this.a;
        if (this.a.e.getCheckedItemPosition() == 0) {
            z = true;
        }
        findpwdTypeActivity.d(z);
    }
}
