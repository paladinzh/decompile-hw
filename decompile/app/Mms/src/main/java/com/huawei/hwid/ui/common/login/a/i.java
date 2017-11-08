package com.huawei.hwid.ui.common.login.a;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.encrypt.e;

/* compiled from: PwdDialogFragment */
class i implements OnClickListener {
    final /* synthetic */ Context a;
    final /* synthetic */ a b;

    i(a aVar, Context context) {
        this.b = aVar;
        this.a = context;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.b.a(this.b.c, false);
        if (this.b.e()) {
            this.b.o = new c(this.a, "6", this.b.j);
            this.b.k = e.d(this.b.getActivity(), this.b.b.getText().toString());
            this.b.a(this.b.j, this.b.k);
        }
    }
}
