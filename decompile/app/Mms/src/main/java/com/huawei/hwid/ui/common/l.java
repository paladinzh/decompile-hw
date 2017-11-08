package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

/* compiled from: UIUtil */
final class l implements OnCancelListener {
    final /* synthetic */ Context a;

    l(Context context) {
        this.a = context;
    }

    public void onCancel(DialogInterface dialogInterface) {
        ((Activity) this.a).finish();
    }
}
