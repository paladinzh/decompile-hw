package com.huawei.hwid.c;

import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

/* compiled from: VipCommonUtils */
class b implements CloudRequestHandler {
    boolean a = true;
    private Object b = new Object();

    b() {
    }

    public void onFinish(Bundle bundle) {
        synchronized (this.b) {
            this.a = false;
            this.b.notifyAll();
        }
    }

    public void onError(ErrorStatus errorStatus) {
        synchronized (this.b) {
            this.a = false;
            this.b.notifyAll();
        }
    }

    public void a() {
        synchronized (this.b) {
            if (this.a) {
                try {
                    a.a("VipCommonUtils", "need wait for vip queryResouce, begin to wait it");
                    this.b.wait(10000);
                    a.a("VipCommonUtils", "wait for vip queryResouce over!");
                } catch (Throwable e) {
                    a.d("VipCommonUtils", e.toString(), e);
                }
                return;
            }
        }
    }
}
