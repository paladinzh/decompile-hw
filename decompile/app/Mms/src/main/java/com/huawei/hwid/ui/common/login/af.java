package com.huawei.hwid.ui.common.login;

import android.os.Handler;
import android.os.Message;
import com.huawei.hwid.core.c.m;

/* compiled from: RegisterResetVerifyEmailActivity */
class af extends Handler {
    final /* synthetic */ RegisterResetVerifyEmailActivity a;

    af(RegisterResetVerifyEmailActivity registerResetVerifyEmailActivity) {
        this.a = registerResetVerifyEmailActivity;
    }

    public void handleMessage(Message message) {
        if (message.what == 2) {
            if (60 - ((System.currentTimeMillis() - this.a.y) / 1000) <= 0) {
                this.a.x = true;
                if (this.a.r || this.a.s) {
                    this.a.d.setEnabled(true);
                    this.a.d.setText(this.a.getString(m.a(this.a, "CS_register_resend_email")));
                } else {
                    this.a.b.setText(this.a.getString(m.a(this.a, "CS_reset_verify_email_btn")));
                    this.a.b.setEnabled(true);
                }
            } else {
                this.a.x = false;
                this.a.z.sendEmptyMessageDelayed(2, 200);
                if (this.a.r || this.a.s) {
                    this.a.d.setEnabled(false);
                    this.a.d.setText(this.a.getString(m.a(this.a, "CS_resend_email_count_down"), new Object[]{r4 + ""}));
                } else {
                    this.a.b.setEnabled(false);
                    this.a.b.setText(this.a.getString(m.a(this.a, "CS_reset_verify_email_btn_count_down"), new Object[]{r4 + ""}));
                }
            }
        }
        super.handleMessage(message);
    }
}
