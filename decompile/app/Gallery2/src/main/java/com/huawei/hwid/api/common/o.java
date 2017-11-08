package com.huawei.hwid.api.common;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.cloudservice.b;
import com.huawei.cloudservice.c;
import com.huawei.hwid.api.common.apkimpl.a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

public final class o {
    private static final Object[] b = new Object[0];
    private static o c;
    private b a;
    private Context d;
    private String e;
    private Bundle f;
    private LoginHandler g = null;
    private c h = null;
    private ServiceConnection i = new q(this);

    public static o a(Context context, String str, Bundle bundle) {
        o oVar;
        synchronized (b) {
            if (c == null) {
                c = new o(context, str, bundle);
            }
            oVar = c;
        }
        return oVar;
    }

    private o(Context context, String str, Bundle bundle) {
        this.d = context;
        this.e = str;
        this.f = bundle;
    }

    public void a() {
        e.a("CloudAccountServiceHandle", "bindService");
        if (this.g == null) {
            e.d("CloudAccountServiceHandle", "has not set LoginHandle");
        } else if (!a.a(this.d)) {
            this.g.onError(new ErrorStatus(31, "Account hasnot login"));
        } else if (this.h == null) {
            e.d("CloudAccountServiceHandle", "mCallback is null, cannot bind service");
        } else if (this.a != null) {
            a(this.e, this.f);
        } else {
            b();
        }
    }

    private void b() {
        Intent intent = new Intent();
        intent.setAction("com.huawei.hwid.ICloudService");
        intent.setPackage("com.huawei.hwid");
        e.a("CloudAccountServiceHandle", "begin to bindService");
        this.d.getApplicationContext().bindService(intent, this.i, 1);
    }

    public void a(String str, Bundle bundle) {
        e.a("CloudAccountServiceHandle", "login");
        try {
            if (this.h != null && this.a != null) {
                this.a.a(str, bundle, this.h);
            }
        } catch (RemoteException e) {
            e.d("CloudAccountServiceHandle", "Call Remote Exception");
            b();
        }
    }

    public void a(LoginHandler loginHandler) {
        this.g = loginHandler;
        this.h = new p(this, loginHandler);
    }
}
