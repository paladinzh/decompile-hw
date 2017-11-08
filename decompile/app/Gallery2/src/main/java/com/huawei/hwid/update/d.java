package com.huawei.hwid.update;

import android.os.Handler;
import android.os.Message;
import com.huawei.hwid.core.d.b.e;

public abstract class d extends Handler {
    public abstract void a();

    public abstract void a(int i);

    public abstract void a(int i, int i2);

    public void handleMessage(Message message) {
        super.handleMessage(message);
        switch (message.what) {
            case 3:
                e.b("OtaDownloadHandler", "entry DOWNLOAD_SHOW_PROGRESS");
                a(message.arg1, message.arg2);
                return;
            case 4:
                e.b("OtaDownloadHandler", "entry DOWNLOAD_VERSION_FAILURE");
                a(2);
                return;
            case 5:
                e.b("OtaDownloadHandler", "entry APK_DOWNLOAD_COMPLETED");
                a();
                return;
            case 11:
                return;
            default:
                e.b("OtaDownloadHandler", "entry download default");
                return;
        }
    }
}
