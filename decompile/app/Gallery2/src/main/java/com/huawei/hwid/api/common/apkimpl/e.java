package com.huawei.hwid.api.common.apkimpl;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION;
import com.huawei.hwid.api.common.apkimpl.OtaDownloadActivity.a;

class e implements OnClickListener {
    final /* synthetic */ OtaDownloadActivity a;

    e(OtaDownloadActivity otaDownloadActivity) {
        this.a = otaDownloadActivity;
    }

    @TargetApi(23)
    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.d.a(false);
        if (VERSION.SDK_INT > 22 && this.a.checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            com.huawei.hwid.core.d.b.e.b("OtaDownloadActivity", "have not permission READ_PHONE_STATE");
            this.a.requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
        } else if (this.a.i) {
            this.a.m();
        } else {
            com.huawei.hwid.core.d.b.e.b("OtaDownloadActivity", "startCheckVersion");
            this.a.a(new a(this.a));
        }
    }
}
