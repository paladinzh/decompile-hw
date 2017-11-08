package com.huawei.hwid.ui.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: BaseActivity */
class e extends Handler {
    private c a;

    public e(c cVar) {
        this.a = cVar;
    }

    public void handleMessage(Message message) {
        if (message.what == 0) {
            this.a.disposeRequestMessage((Bundle) message.obj);
        }
        super.handleMessage(message);
    }
}
