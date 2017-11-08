package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.ui.common.j;

/* compiled from: StartUpGuideLoginForAPPActivity */
class cl implements OnClickListener {
    final /* synthetic */ StartUpGuideLoginForAPPActivity a;

    cl(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity) {
        this.a = startUpGuideLoginForAPPActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.d = true;
        j.d(this.a);
    }
}
