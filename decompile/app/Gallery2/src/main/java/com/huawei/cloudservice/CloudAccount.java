package com.huawei.cloudservice;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.api.common.d;
import com.huawei.hwid.core.datatype.HwAccount;

public class CloudAccount {
    private d a = null;

    public CloudAccount(d dVar) {
        this.a = dVar;
    }

    public HwAccount getAccountData() {
        if (this.a == null) {
            this.a = new d();
        }
        return this.a.a();
    }

    public String getAccountName() {
        return getAccountData().b();
    }

    public String getLoginUserName() {
        Object m = getAccountData().m();
        if (TextUtils.isEmpty(m)) {
            return getAccountName();
        }
        return m;
    }

    public static void getAccountsByType(Context context, String str, Bundle bundle, LoginHandler loginHandler) {
        d.a(context, str, bundle, loginHandler);
    }

    public static boolean hasLoginAccount(Context context) {
        return d.d(context);
    }

    public void getUserInfo(Context context, String str, CloudRequestHandler cloudRequestHandler) {
        this.a.a(context, str, cloudRequestHandler);
    }
}
