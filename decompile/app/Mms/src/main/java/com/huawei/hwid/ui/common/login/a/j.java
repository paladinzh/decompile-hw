package com.huawei.hwid.ui.common.login.a;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: PwdDialogFragment */
class j implements OnClickListener {
    final /* synthetic */ a a;

    j(a aVar) {
        this.a = aVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (this.a.a == null) {
            this.a.getActivity().finish();
        } else {
            this.a.a.g();
        }
    }
}
