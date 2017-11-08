package com.huawei.hwid.ui.common.login.a;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.ui.common.j;

/* compiled from: PwdDialogFragment */
class e implements OnClickListener {
    final /* synthetic */ a a;

    e(a aVar) {
        this.a = aVar;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.q = true;
        j.d(this.a.getActivity());
    }
}
