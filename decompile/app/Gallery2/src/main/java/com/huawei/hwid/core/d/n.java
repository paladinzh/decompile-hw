package com.huawei.hwid.core.d;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

class n implements OnClickListener {
    final /* synthetic */ Context a;

    n(Context context) {
        this.a = context;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        ((Activity) this.a).finish();
    }
}
