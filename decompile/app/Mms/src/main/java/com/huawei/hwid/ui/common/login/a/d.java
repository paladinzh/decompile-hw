package com.huawei.hwid.ui.common.login.a;

import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.ui.common.j;

/* compiled from: PwdDialogFragment */
class d implements OnClickListener {
    final /* synthetic */ a a;
    private boolean b = false;

    d(a aVar) {
        this.a = aVar;
    }

    public void onClick(View view) {
        boolean z = false;
        if (this.a.d != null) {
            if (!this.b) {
                z = true;
            }
            this.b = z;
            j.a(this.a.getActivity(), this.a.b, this.a.d, this.b);
            return;
        }
        a.b("PwdDialogFragment", "mDisplayPwd is null");
    }
}
