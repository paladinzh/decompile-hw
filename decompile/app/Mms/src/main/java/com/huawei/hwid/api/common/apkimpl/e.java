package com.huawei.hwid.api.common.apkimpl;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

/* compiled from: DummyActivity */
class e implements AccountManagerCallback {
    final /* synthetic */ DummyActivity a;

    public e(DummyActivity dummyActivity) {
        this.a = dummyActivity;
    }

    public void run(AccountManagerFuture accountManagerFuture) {
        Bundle bundle;
        int i;
        String str;
        int i2;
        Bundle bundle2;
        String str2;
        Parcelable errorStatus;
        Intent intent;
        String str3 = "";
        a.b("AuthTokenCallBack", "AuthTokenCallBack::run==>");
        if (accountManagerFuture == null) {
            bundle = null;
        } else {
            try {
                bundle = (Bundle) accountManagerFuture.getResult();
            } catch (Throwable e) {
                i = 3002;
                str3 = "getAuthTokenByFeatures : OperationCanceledException occur";
                a.d("AuthTokenCallBack", "AuthTokenCallBack OperationCanceledException:" + e.toString(), e);
                str = str3;
                i2 = i;
                bundle2 = null;
                str2 = str;
                if (i2 == 0) {
                    if (bundle2 != null) {
                        a.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                        errorStatus = new ErrorStatus(i2, "bundle is null");
                        a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    } else {
                        a.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                        errorStatus = new ErrorStatus(i2, str2);
                        a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    }
                    intent = new Intent();
                    intent.setPackage(this.a.getPackageName());
                    intent.putExtra("isUseSDK", false);
                    intent.putExtra("parce", errorStatus);
                    com.huawei.hwid.core.c.e.c(this.a, intent);
                    this.a.finish();
                    return;
                }
                if (bundle2 != null) {
                    a.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                    errorStatus = new ErrorStatus(i2, str2);
                    a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                } else {
                    a.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                    errorStatus = new ErrorStatus(i2, "bundle is null");
                    a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                }
                intent = new Intent();
                intent.setPackage(this.a.getPackageName());
                intent.putExtra("isUseSDK", false);
                intent.putExtra("parce", errorStatus);
                com.huawei.hwid.core.c.e.c(this.a, intent);
                this.a.finish();
                return;
            } catch (Throwable e2) {
                i = 3003;
                str3 = "getAuthTokenByFeatures : AuthenticatorException occur";
                a.d("AuthTokenCallBack", "AuthTokenCallBack AuthenticatorException:" + e2.toString(), e2);
                str = str3;
                i2 = i;
                bundle2 = null;
                str2 = str;
                if (i2 == 0) {
                    if (bundle2 != null) {
                        a.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                        errorStatus = new ErrorStatus(i2, "bundle is null");
                        a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    } else {
                        a.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                        errorStatus = new ErrorStatus(i2, str2);
                        a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    }
                    intent = new Intent();
                    intent.setPackage(this.a.getPackageName());
                    intent.putExtra("isUseSDK", false);
                    intent.putExtra("parce", errorStatus);
                    com.huawei.hwid.core.c.e.c(this.a, intent);
                    this.a.finish();
                    return;
                }
                if (bundle2 != null) {
                    a.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                    errorStatus = new ErrorStatus(i2, str2);
                    a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                } else {
                    a.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                    errorStatus = new ErrorStatus(i2, "bundle is null");
                    a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                }
                intent = new Intent();
                intent.setPackage(this.a.getPackageName());
                intent.putExtra("isUseSDK", false);
                intent.putExtra("parce", errorStatus);
                com.huawei.hwid.core.c.e.c(this.a, intent);
                this.a.finish();
                return;
            } catch (Throwable e22) {
                a.d("AuthTokenCallBack", "AuthTokenCallBack IOException:" + e22.toString(), e22);
                i2 = CommonStatusCodes.AUTH_TOKEN_ERROR;
                bundle2 = null;
                str2 = "getAuthTokenByFeatures : IOException occur";
            }
        }
        str2 = str3;
        bundle2 = bundle;
        i2 = 0;
        if (!(i2 == 0 || TextUtils.isEmpty(str2)) || bundle2 == null) {
            if (bundle2 != null) {
                a.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                errorStatus = new ErrorStatus(i2, str2);
                a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
            } else {
                a.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                errorStatus = new ErrorStatus(i2, "bundle is null");
                a.b("AuthTokenCallBack", "error: " + errorStatus.toString());
            }
            intent = new Intent();
            intent.setPackage(this.a.getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            com.huawei.hwid.core.c.e.c(this.a, intent);
            this.a.finish();
            return;
        }
        this.a.e = (String) bundle2.get("authAccount");
        this.a.f = (String) bundle2.get("accountType");
        this.a.d = (String) bundle2.get("authtoken");
        a.b("AuthTokenCallBack", "AuthTokenCallBack: accountName=" + f.c(this.a.e) + " accountType=" + this.a.f);
        this.a.a(this.a.d, this.a.e, i2, bundle2);
    }
}
