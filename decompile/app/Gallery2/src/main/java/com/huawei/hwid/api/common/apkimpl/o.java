package com.huawei.hwid.api.common.apkimpl;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.j;
import com.huawei.hwid.core.d.m;

class o implements OnClickListener {
    final /* synthetic */ OtaDownloadActivity a;

    o(OtaDownloadActivity otaDownloadActivity) {
        this.a = otaDownloadActivity;
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.a.f.a(false);
        if (b.a(this.a)) {
            this.a.a(new OtaDownloadActivity.b(this.a));
            this.a.f.a(true);
            this.a.f.dismiss();
            return;
        }
        this.a.a(m.a(this.a, j.a(this.a, "CS_network_connect_error"), j.a(this.a, "CS_server_unavailable_title"), false).show());
    }
}
