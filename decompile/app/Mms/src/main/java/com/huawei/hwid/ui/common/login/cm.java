package com.huawei.hwid.ui.common.login;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

/* compiled from: StartUpGuideLoginForAPPActivity */
class cm implements OnClickListener {
    final /* synthetic */ StartUpGuideLoginForAPPActivity a;

    cm(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity) {
        this.a = startUpGuideLoginForAPPActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent = new Intent();
        intent.putExtra("isUseSDK", true);
        intent.putExtra("completed", false);
        intent.setPackage(this.a.getPackageName());
        this.a.a(intent);
        this.a.finish();
    }
}
