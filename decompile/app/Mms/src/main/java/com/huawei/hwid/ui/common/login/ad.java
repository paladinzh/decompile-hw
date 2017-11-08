package com.huawei.hwid.ui.common.login;

import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;

/* compiled from: PrivacyPolicyActivity */
class ad extends Thread {
    final /* synthetic */ ac a;

    ad(ac acVar) {
        this.a = acVar;
    }

    public void run() {
        try {
            if (!TextUtils.isEmpty(this.a.b.n)) {
                this.a.b.m = this.a.b.m + "-" + this.a.b.n;
            }
            if (PrivacyPolicyActivity.c(this.a.b.i + this.a.b.m + ".zip", this.a.b.i, this.a.b.m)) {
                this.a.b.a(this.a.b.i, this.a.b.m, ".zip");
            }
            this.a.b.runOnUiThread(new ae(this));
        } catch (Throwable e) {
            a.d("PrivacyPolicyActivity", e.toString(), e);
        }
    }
}
