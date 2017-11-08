package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class w implements OnClickListener {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity a;

    w(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity) {
        this.a = resetPwdByPhoneNumberVerificationActivity;
    }

    public void onClick(View view) {
        this.a.onBackPressed();
    }
}
