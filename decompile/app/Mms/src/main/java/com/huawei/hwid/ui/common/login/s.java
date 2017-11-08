package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: ManageAgreementActivity */
class s implements OnClickListener {
    final /* synthetic */ ManageAgreementActivity a;
    private String b;

    private s(ManageAgreementActivity manageAgreementActivity, String str) {
        this.a = manageAgreementActivity;
        this.b = str;
    }

    public void onClick(View view) {
        Intent intent = new Intent(this.a, PrivacyPolicyActivity.class);
        intent.putExtra("isEmotionIntroduce", this.a.r);
        intent.putExtra("privacyType", this.b);
        this.a.startActivity(intent);
    }
}
