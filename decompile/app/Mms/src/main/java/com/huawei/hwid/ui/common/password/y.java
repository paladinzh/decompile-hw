package com.huawei.hwid.ui.common.password;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.hwid.core.c.m;

/* compiled from: ResetPwdByPhoneNumberVerificationActivity */
class y extends Handler {
    final /* synthetic */ ResetPwdByPhoneNumberVerificationActivity a;

    y(ResetPwdByPhoneNumberVerificationActivity resetPwdByPhoneNumberVerificationActivity) {
        this.a = resetPwdByPhoneNumberVerificationActivity;
    }

    public void handleMessage(Message message) {
        if (message != null) {
            switch (message.what) {
                case 1:
                    this.a.m = message.getData().getString("verifyCode");
                    if (!(this.a.m == null || TextUtils.isEmpty(this.a.m) || !this.a.e.isChecked())) {
                        this.a.d.setText(this.a.m);
                        this.a.d.setSelection(this.a.m.length());
                        this.a.d.setError(null);
                        break;
                    }
                case 2:
                    boolean z;
                    if (60 - ((System.currentTimeMillis() - this.a.i) / 1000) <= 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    if (!z) {
                        this.a.l = false;
                        this.a.o.sendEmptyMessageDelayed(2, 200);
                        this.a.c.setText(this.a.getString(m.a(this.a, "CS_count_down"), new Object[]{r4 + ""}));
                        this.a.c.setEnabled(false);
                        break;
                    }
                    this.a.l = true;
                    this.a.c.setText(this.a.getString(m.a(this.a, "CS_retrieve")));
                    this.a.c.setEnabled(true);
                    break;
            }
            super.handleMessage(message);
        }
    }
}
