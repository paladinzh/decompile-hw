package com.huawei.hwid.core.model.http;

import android.os.HandlerThread;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.helper.handler.c;

/* compiled from: HttpRequest */
class b extends HandlerThread {
    private c a = null;
    private c b = null;

    public b(String str, c cVar) {
        super(str);
        this.b = cVar;
    }

    protected void onLooperPrepared() {
        this.a = new c(this.b);
        super.onLooperPrepared();
    }

    public c a() {
        int i = 1000;
        while (this.a == null) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            try {
                sleep(4);
            } catch (Throwable e) {
                a.d("RequestManager", e.toString(), e);
            }
            i = i2;
        }
        return this.a;
    }
}
