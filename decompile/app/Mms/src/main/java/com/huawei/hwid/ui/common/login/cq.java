package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: StartUpGuideLoginForAPPActivity */
class cq implements OnClickListener {
    final /* synthetic */ StartUpGuideLoginForAPPActivity a;

    cq(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity) {
        this.a = startUpGuideLoginForAPPActivity;
    }

    public void onClick(View view) {
        Intent intent = new Intent(this.a, ManageAgreementActivity.class);
        intent.setPackage("com.huawei.hwid");
        intent.putExtra("typeEnterAgree", "1");
        this.a.startActivityForResult(intent, cs.RequestCode_Agree.ordinal());
    }
}
