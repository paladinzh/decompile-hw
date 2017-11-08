package com.huawei.hwid.ui.common.login;

import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.ui.common.j;
import java.util.ArrayList;

/* compiled from: RegisterViaPhoneNumberActivity */
class bk extends Thread {
    final /* synthetic */ RegisterViaPhoneNumberActivity a;

    bk(RegisterViaPhoneNumberActivity registerViaPhoneNumberActivity) {
        this.a = registerViaPhoneNumberActivity;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        int i = 0;
        ArrayList a = j.a(this.a);
        if (!a.isEmpty()) {
            this.a.l = (String[]) a.get(0);
            this.a.p = (String[]) a.get(1);
            if (this.a.p != null && this.a.p.length > 0) {
                this.a.g = this.a.p[0];
            } else {
                if (this.a.l == null || this.a.l.length == 0) {
                    if (this.a.p == null || this.a.p.length == 0) {
                    }
                }
                if (!q.a(this.a, (int) MsgUrlService.RESULT_NOT_IMPL).startsWith("460")) {
                }
                this.a.l = new String[]{"中国"};
                this.a.p = new String[]{StringUtils.MPLUG86};
                this.a.g = StringUtils.MPLUG86;
            }
            if (this.a.p != null && this.a.p.length > 0) {
                String d = k.d(this.a);
                if (this.a.l != null && this.a.l.length > 0) {
                    for (int i2 = 0; i2 < this.a.p.length; i2++) {
                        if (this.a.p[i2].equals(d)) {
                            i = i2;
                            break;
                        }
                    }
                    this.a.e.setText(this.a.l[i]);
                    this.a.g = this.a.p[i];
                }
                this.a.e.setOnClickListener(new bl(this));
            }
        }
    }
}
