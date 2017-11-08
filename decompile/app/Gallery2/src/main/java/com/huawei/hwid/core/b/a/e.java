package com.huawei.hwid.core.b.a;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

class e extends Thread {
    private a a;
    private Handler b;
    private String c;
    private Context d;

    public e(Context context, a aVar, Handler handler, String str) {
        this.a = aVar;
        this.c = str;
        this.d = context;
        this.b = handler;
    }

    public void run() {
        try {
            if (this.b != null) {
                Bundle a = d.a(this.d, this.a, this.c);
                Message obtainMessage = this.b.obtainMessage(0);
                if ((this.d instanceof Activity) && ((Activity) this.d).isFinishing()) {
                    com.huawei.hwid.core.d.b.e.d("RequestManager", "context is finished !!", null);
                    return;
                }
                obtainMessage.obj = a;
                this.b.sendMessage(obtainMessage);
            }
        } catch (Throwable e) {
            com.huawei.hwid.core.d.b.e.d("RequestManager", e.getMessage(), e);
        }
    }
}
