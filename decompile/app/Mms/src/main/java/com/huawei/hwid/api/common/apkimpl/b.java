package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.ui.common.j;

/* compiled from: DummyActivity */
class b implements OnClickListener {
    final /* synthetic */ DummyActivity a;

    b(DummyActivity dummyActivity) {
        this.a = dummyActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.i = true;
        j.d(this.a);
    }
}
