package com.huawei.hwid.api.common.apkimpl;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

class h extends ProgressDialog {
    final /* synthetic */ OtaDownloadActivity a;

    h(OtaDownloadActivity otaDownloadActivity, Context context) {
        this.a = otaDownloadActivity;
        super(context);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.a.a(i, keyEvent)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
