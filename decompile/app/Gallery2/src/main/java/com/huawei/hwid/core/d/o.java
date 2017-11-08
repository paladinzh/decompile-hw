package com.huawei.hwid.core.d;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

class o implements OnCancelListener {
    final /* synthetic */ Context a;

    o(Context context) {
        this.a = context;
    }

    public void onCancel(DialogInterface dialogInterface) {
        ((Activity) this.a).finish();
    }
}
