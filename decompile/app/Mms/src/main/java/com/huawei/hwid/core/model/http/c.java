package com.huawei.hwid.core.model.http;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/* compiled from: HttpRequest */
class c extends Handler {
    private com.huawei.hwid.core.helper.handler.c a;

    public c(com.huawei.hwid.core.helper.handler.c cVar) {
        this.a = cVar;
    }

    public void a() {
        getLooper().quit();
    }

    public void handleMessage(Message message) {
        if (message.what == 0) {
            this.a.disposeRequestMessage((Bundle) message.obj);
            a();
        }
        super.handleMessage(message);
    }
}
