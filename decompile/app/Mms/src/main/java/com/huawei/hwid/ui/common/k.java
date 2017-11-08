package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: UIUtil */
final class k implements OnClickListener {
    final /* synthetic */ Context a;

    k(Context context) {
        this.a = context;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        ((Activity) this.a).finish();
    }
}
