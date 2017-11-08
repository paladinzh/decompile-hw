package com.huawei.hwid.api.common;

import android.view.View;
import android.view.View.OnClickListener;

class c implements OnClickListener {
    final /* synthetic */ CloudAccountCenterActivity a;

    c(CloudAccountCenterActivity cloudAccountCenterActivity) {
        this.a = cloudAccountCenterActivity;
    }

    public void onClick(View view) {
        this.a.finish();
    }
}
