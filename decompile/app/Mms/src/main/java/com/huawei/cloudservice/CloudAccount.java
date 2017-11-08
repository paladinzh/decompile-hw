package com.huawei.cloudservice;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.api.common.a;
import com.huawei.hwid.core.datatype.HwAccount;

public class CloudAccount {
    private a a = null;

    public CloudAccount(a aVar) {
        this.a = aVar;
    }

    public HwAccount getAccountData() {
        if (this.a == null) {
            this.a = new a();
        }
        return this.a.a();
    }

    public String getAccountName() {
        return getAccountData().a();
    }

    public String getUserId() {
        return getAccountData().c();
    }

    public String getServiceToken() {
        return getAccountData().f();
    }

    public String getAccountType() {
        return getAccountData().g();
    }

    public static void getAccountsByType(Context context, String str, Bundle bundle, LoginHandler loginHandler) {
        a.a(context, str, bundle, loginHandler);
    }

    public static boolean hasLoginAccount(Context context) {
        return a.e(context);
    }

    public static void checkPassWord(Context context, String str, String str2, String str3, String str4, CloudRequestHandler cloudRequestHandler, Bundle bundle) {
        a.a(context, str, str2, str3, str4, cloudRequestHandler, bundle);
    }
}
