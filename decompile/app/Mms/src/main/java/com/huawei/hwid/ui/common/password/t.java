package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;

/* compiled from: FindpwdbyPhonenumberActivity */
class t implements OnClickListener {
    final /* synthetic */ FindpwdbyPhonenumberActivity a;

    t(FindpwdbyPhonenumberActivity findpwdbyPhonenumberActivity) {
        this.a = findpwdbyPhonenumberActivity;
    }

    public void onClick(View view) {
        try {
            if (this.a.g >= 0 && this.a.g < this.a.f.size()) {
                String str = "";
                if (this.a.c.getText() != null) {
                    str = this.a.c.getText().toString();
                }
                if (str.contains("+")) {
                    str = d.d(str);
                }
                this.a.b(str);
                return;
            }
            a.d("FindpwdbyPhonenumberActivity", "click Err, maybe not init failed");
            this.a.finish();
        } catch (Throwable th) {
            a.d("FindpwdbyPhonenumberActivity", th.toString(), th);
        }
    }
}
