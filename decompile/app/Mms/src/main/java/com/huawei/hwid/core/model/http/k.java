package com.huawei.hwid.core.model.http;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.datatype.HwAccount;

/* compiled from: RequestManager */
class k extends Thread {
    private a a;
    private Handler b;
    private String c;
    private Context d;
    private HwAccount e;

    public k(Context context, a aVar, Handler handler, String str, HwAccount hwAccount) {
        this.a = aVar;
        this.c = str;
        this.d = context;
        this.b = handler;
        this.e = hwAccount;
    }

    public void run() {
        try {
            if (this.b != null) {
                Bundle a = i.a(this.d, this.a, this.c, this.e);
                Message obtainMessage = this.b.obtainMessage(0);
                if ((this.d instanceof Activity) && ((Activity) this.d).isFinishing()) {
                    a.d("RequestManager", "context is finished !!", null);
                    return;
                }
                obtainMessage.obj = a;
                this.b.sendMessage(obtainMessage);
            }
        } catch (Throwable e) {
            a.d("RequestManager", e.toString(), e);
        }
    }
}
