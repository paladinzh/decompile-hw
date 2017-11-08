package com.huawei.hwid.ui.common.login;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.ui.common.f;

/* compiled from: StartUpGuideLoginForAPPActivity */
class cp implements OnClickListener {
    final /* synthetic */ StartUpGuideLoginForAPPActivity a;

    cp(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity) {
        this.a = startUpGuideLoginForAPPActivity;
    }

    public void onClick(View view) {
        LoginActivity.a(this.a, f.FromApp, StartUpGuideLoginForAPPActivity.class.getName(), true, this.a.c, cs.RequestCode_LoginActivity.ordinal(), new Bundle());
    }
}
