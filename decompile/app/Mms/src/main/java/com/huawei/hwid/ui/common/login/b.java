package com.huawei.hwid.ui.common.login;

import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;

/* compiled from: LoginActivity */
class b extends Handler {
    final /* synthetic */ LoginActivity a;

    b(LoginActivity loginActivity) {
        this.a = loginActivity;
    }

    public void handleMessage(Message message) {
        if (message.what == 100) {
            this.a.i = (ArrayList) message.obj;
            this.a.a(this.a.i);
        }
        super.handleMessage(message);
    }
}
