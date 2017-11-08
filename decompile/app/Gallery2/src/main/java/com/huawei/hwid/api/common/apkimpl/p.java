package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

class p implements OnClickListener {
    final /* synthetic */ OtaDownloadActivity a;

    p(OtaDownloadActivity otaDownloadActivity) {
        this.a = otaDownloadActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.f.a(true);
        this.a.f.dismiss();
        this.a.finish();
    }
}
