package com.huawei.hwid.ui.common.login;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.hwid.core.c.m;

/* compiled from: RegisterViaPhoneNumVerificationActivity */
class ba extends Handler {
    final /* synthetic */ RegisterViaPhoneNumVerificationActivity a;

    ba(RegisterViaPhoneNumVerificationActivity registerViaPhoneNumVerificationActivity) {
        this.a = registerViaPhoneNumVerificationActivity;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 0:
                boolean z;
                if (60 - ((System.currentTimeMillis() - this.a.h) / 1000) <= 0) {
                    z = true;
                } else {
                    z = false;
                }
                if (!z) {
                    this.a.k = false;
                    this.a.s.sendEmptyMessageDelayed(0, 200);
                    this.a.c.setText(this.a.getString(m.a(this.a, "CS_count_down"), new Object[]{r4 + ""}));
                    this.a.c.setEnabled(false);
                    break;
                }
                this.a.k = true;
                this.a.c.setText(this.a.getString(m.a(this.a, "CS_retrieve")));
                this.a.c.setEnabled(true);
                break;
            case 1:
                this.a.l = message.getData().getString("verifyCode");
                if (!TextUtils.isEmpty(this.a.l) && this.a.e.isChecked()) {
                    this.a.d.setText(this.a.l);
                    this.a.d.setSelection(this.a.l.length());
                    this.a.d.setError(null);
                    break;
                }
        }
        super.handleMessage(message);
    }
}
