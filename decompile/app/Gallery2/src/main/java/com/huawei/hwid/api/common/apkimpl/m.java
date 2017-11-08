package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;
import com.huawei.hwid.core.d.b.e;

class m implements OnKeyListener {
    final /* synthetic */ OtaDownloadActivity a;

    m(OtaDownloadActivity otaDownloadActivity) {
        this.a = otaDownloadActivity;
    }

    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
        if (i == 4 && keyEvent.getRepeatCount() == 0 && keyEvent.getAction() == 0) {
            e.b("OtaDownloadActivity", "onKey keycode_back");
            this.a.f();
        }
        return false;
    }
}
