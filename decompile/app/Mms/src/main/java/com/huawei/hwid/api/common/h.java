package com.huawei.hwid.api.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

/* compiled from: CloudAccountImpl */
class h extends BroadcastReceiver {
    private Context a;
    private CloudRequestHandler b;
    private boolean c = false;

    public h(Context context, CloudRequestHandler cloudRequestHandler) {
        this.a = context;
        this.b = cloudRequestHandler;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onReceive(Context context, Intent intent) {
        if (!this.c) {
            Bundle bundle;
            try {
                bundle = new Bundle();
                bundle.putBoolean("FingerBroadcastReceiver", true);
                a.a(this.a, bundle);
            } catch (Throwable e) {
                a.c("CloudAccountImpl", e.toString(), e);
            }
            this.c = true;
            if (intent != null) {
                String action = intent.getAction();
                a.k(context);
                if ("com.huawei.cloudserive.fingerSuccess".equals(action)) {
                    if (this.b != null) {
                        a.d(context, intent);
                        bundle = intent.getBundleExtra("bundle");
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        this.b.onFinish(bundle);
                    }
                } else if ("com.huawei.cloudserive.fingerCancel".equals(action)) {
                    ErrorStatus errorStatus = new ErrorStatus(3002, "use the sdk: press cancel or back key");
                    a.e("CloudAccountImpl", "error: " + errorStatus.toString());
                    if (this.b != null) {
                        this.b.onError(errorStatus);
                    }
                }
            }
        }
    }
}
