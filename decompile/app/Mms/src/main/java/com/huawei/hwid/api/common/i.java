package com.huawei.hwid.api.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

/* compiled from: CloudAccountImpl */
class i extends BroadcastReceiver {
    private Context a = null;
    private boolean b = false;
    private LoginHandler c = null;
    private c d;

    public i(Context context, LoginHandler loginHandler, c cVar) {
        this.a = context;
        this.c = loginHandler;
        this.d = cVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        synchronized (a.b) {
            if (this.b) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putBoolean("LoginBroadcastReceiver", true);
            a.a(this.a, bundle);
            this.b = true;
        }
    }

    private void a(Context context, ErrorStatus errorStatus) {
        if (!d.j(context) || !a.b(context)) {
            a.b("CloudAccountImpl", "when report login Log, is sdk. not report");
        } else if (this.d != null) {
            this.d.a(d.a());
            if (errorStatus != null) {
                this.d.c(String.valueOf(errorStatus.getErrorCode()));
                this.d.d(errorStatus.getErrorReason());
            }
            com.huawei.hwid.core.a.d.a(this.d, context);
            com.huawei.hwid.api.common.c.a.a(null);
        } else {
            a.b("CloudAccountImpl", "when reportLog, opLogItem is null");
        }
    }
}
