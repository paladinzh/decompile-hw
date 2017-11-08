package com.huawei.hwid.api.common;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.cloudservice.c.a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

class p extends a {
    final /* synthetic */ LoginHandler a;
    final /* synthetic */ o b;

    p(o oVar, LoginHandler loginHandler) {
        this.b = oVar;
        this.a = loginHandler;
    }

    public void a(int i, Bundle bundle) throws RemoteException {
        e.b("CloudAccountServiceHandle", "loginResult:retCode=" + i);
        if (i == -1) {
            String b;
            HwAccount a = new HwAccount().a(bundle);
            Object i2 = a.i();
            if (TextUtils.isEmpty(i2) || "null".equalsIgnoreCase(i2)) {
                b = l.b(this.b.d);
                if (b == null) {
                    b = "";
                }
                a.h(b);
            }
            i2 = a.j();
            if (TextUtils.isEmpty(i2) || "null".equalsIgnoreCase(i2)) {
                a.i(l.a(this.b.d, l.b(this.b.d)));
            }
            com.huawei.hwid.b.a.a(this.b.d).a(a);
            CloudAccount[] a2 = d.a(this.b.d);
            b = "";
            if (!TextUtils.isEmpty(a.b())) {
                b = a.b();
            }
            e.b("CloudAccountServiceHandle", "loginResult");
            v.a(this.b.d, b);
            this.a.onLogin(a2, d.a(a2, b));
        } else if (i == 0) {
            this.a.onError(new ErrorStatus(31, "Account hasnot login"));
        } else if (i == 1) {
            this.a.onError(new ErrorStatus(29, "Signature invalid"));
        } else if (i != 2) {
            e.b("CloudAccountServiceHandle", "DONT KNOW RET_CODE:" + i);
        } else {
            this.a.onError(new ErrorStatus(30, "serviceToken invalid"));
        }
    }
}
