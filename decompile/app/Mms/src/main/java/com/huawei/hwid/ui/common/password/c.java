package com.huawei.hwid.ui.common.password;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.s;

/* compiled from: FindpwdByHwIdActivity */
class c implements OnClickListener {
    final /* synthetic */ FindpwdByHwIdActivity a;

    c(FindpwdByHwIdActivity findpwdByHwIdActivity) {
        this.a = findpwdByHwIdActivity;
    }

    public void onClick(View view) {
        if (this.a.h()) {
            this.a.b(false);
            this.a.e = this.a.a.getText().toString().trim();
            i.a(this.a, new s(this.a, this.a.e), null, this.a.a(new d(this.a, this.a)));
            this.a.a(null);
        }
    }
}
