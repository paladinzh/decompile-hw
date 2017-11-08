package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: DummyActivity */
class d implements OnDismissListener {
    final /* synthetic */ DummyActivity a;

    d(DummyActivity dummyActivity) {
        this.a = dummyActivity;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        this.a.h = null;
    }
}
