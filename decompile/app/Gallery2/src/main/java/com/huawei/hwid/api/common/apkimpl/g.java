package com.huawei.hwid.api.common.apkimpl;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

class g extends ProgressDialog {
    final /* synthetic */ OtaDownloadActivity a;

    g(OtaDownloadActivity otaDownloadActivity, Context context, int i) {
        this.a = otaDownloadActivity;
        super(context, i);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.a.a(i, keyEvent)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }
}
