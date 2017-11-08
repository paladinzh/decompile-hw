package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: UIUtil */
final class m implements OnClickListener {
    final /* synthetic */ boolean a;
    final /* synthetic */ Activity b;

    m(boolean z, Activity activity) {
        this.a = z;
        this.b = activity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (this.a && this.b != null) {
            this.b.finish();
        }
    }
}
