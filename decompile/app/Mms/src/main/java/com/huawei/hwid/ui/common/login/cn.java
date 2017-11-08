package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: StartUpGuideLoginForAPPActivity */
class cn implements OnDismissListener {
    final /* synthetic */ StartUpGuideLoginForAPPActivity a;

    cn(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity) {
        this.a = startUpGuideLoginForAPPActivity;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        this.a.e = null;
    }
}
