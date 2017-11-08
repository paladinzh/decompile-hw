package com.huawei.hwid.update;

import android.os.Handler;
import android.os.Message;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.update.a.b;
import java.util.Map;

public abstract class a extends Handler {
    public abstract void a(int i, Map<Integer, b> map);

    public abstract void a(Map<Integer, b> map);

    public void handleMessage(Message message) {
        super.handleMessage(message);
        Map map = (Map) message.obj;
        switch (message.what) {
            case 1:
                e.b("OtaCheckVersionHandler", "entry CHECK_VERSION_FINISHED");
                a(map);
                return;
            case 2:
                e.b("OtaCheckVersionHandler", "entry not hasNewVersion");
                a(2, map);
                return;
            case 7:
                e.b("OtaCheckVersionHandler", "entry check version error");
                a(7, map);
                return;
            default:
                e.b("OtaCheckVersionHandler", "entry check version default");
                return;
        }
    }
}
