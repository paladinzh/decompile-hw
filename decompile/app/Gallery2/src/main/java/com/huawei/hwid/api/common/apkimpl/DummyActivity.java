package com.huawei.hwid.api.common.apkimpl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Window;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.api.common.d;
import com.huawei.hwid.api.common.v;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.c;
import com.huawei.hwid.core.d.j;
import com.huawei.hwid.core.d.k;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.core.d.m;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import java.io.IOException;

public class DummyActivity extends Activity {
    private AccountManager a = null;
    private String b = "";
    private String c = "";
    private String d = "";
    private String e = "";
    private String f = "";
    private String g = "";
    private boolean h = false;
    private AlertDialog i = null;
    private boolean j = false;
    private Intent k = null;
    private Bundle l = null;
    private Bundle m = null;
    private boolean n;

    class a implements AccountManagerCallback<Bundle> {
        final /* synthetic */ DummyActivity a;

        public a(DummyActivity dummyActivity) {
            this.a = dummyActivity;
        }

        public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
            Bundle bundle;
            int i;
            String str;
            int i2;
            Bundle bundle2;
            String str2;
            Parcelable errorStatus;
            Intent intent;
            String str3 = "";
            e.b("AuthTokenCallBack", "AuthTokenCallBack::run==>");
            if (accountManagerFuture == null) {
                bundle = null;
            } else {
                try {
                    bundle = (Bundle) accountManagerFuture.getResult();
                } catch (Throwable e) {
                    i = 3002;
                    str3 = "getAuthTokenByFeatures : OperationCanceledException occur";
                    e.d("AuthTokenCallBack", "AuthTokenCallBack OperationCanceledException:" + e.getMessage(), e);
                    str = str3;
                    i2 = i;
                    bundle2 = null;
                    str2 = str;
                    if (i2 == 0) {
                        if (bundle2 != null) {
                            e.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                            errorStatus = new ErrorStatus(i2, "bundle is null");
                            e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                        } else {
                            e.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                            errorStatus = new ErrorStatus(i2, str2);
                            e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                        }
                        intent = new Intent();
                        intent.setPackage(this.a.getPackageName());
                        intent.putExtra("isUseSDK", false);
                        intent.putExtra("parce", errorStatus);
                        c.b(this.a, intent);
                        this.a.finish();
                        return;
                    }
                    if (bundle2 != null) {
                        e.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                        errorStatus = new ErrorStatus(i2, str2);
                        e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    } else {
                        e.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                        errorStatus = new ErrorStatus(i2, "bundle is null");
                        e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    }
                    intent = new Intent();
                    intent.setPackage(this.a.getPackageName());
                    intent.putExtra("isUseSDK", false);
                    intent.putExtra("parce", errorStatus);
                    c.b(this.a, intent);
                    this.a.finish();
                    return;
                } catch (Throwable e2) {
                    i = 3003;
                    str3 = "getAuthTokenByFeatures : AuthenticatorException occur";
                    e.d("AuthTokenCallBack", "AuthTokenCallBack AuthenticatorException:" + e2.getMessage(), e2);
                    str = str3;
                    i2 = i;
                    bundle2 = null;
                    str2 = str;
                    if (i2 == 0) {
                        if (bundle2 != null) {
                            e.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                            errorStatus = new ErrorStatus(i2, "bundle is null");
                            e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                        } else {
                            e.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                            errorStatus = new ErrorStatus(i2, str2);
                            e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                        }
                        intent = new Intent();
                        intent.setPackage(this.a.getPackageName());
                        intent.putExtra("isUseSDK", false);
                        intent.putExtra("parce", errorStatus);
                        c.b(this.a, intent);
                        this.a.finish();
                        return;
                    }
                    if (bundle2 != null) {
                        e.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                        errorStatus = new ErrorStatus(i2, str2);
                        e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    } else {
                        e.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                        errorStatus = new ErrorStatus(i2, "bundle is null");
                        e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                    }
                    intent = new Intent();
                    intent.setPackage(this.a.getPackageName());
                    intent.putExtra("isUseSDK", false);
                    intent.putExtra("parce", errorStatus);
                    c.b(this.a, intent);
                    this.a.finish();
                    return;
                } catch (Throwable e22) {
                    e.d("AuthTokenCallBack", "AuthTokenCallBack IOException:" + e22.getMessage(), e22);
                    i2 = 3004;
                    bundle2 = null;
                    str2 = "getAuthTokenByFeatures : IOException occur";
                }
            }
            str2 = str3;
            bundle2 = bundle;
            i2 = 0;
            if (!(i2 == 0 || TextUtils.isEmpty(str2)) || bundle2 == null) {
                if (bundle2 != null) {
                    e.b("AuthTokenCallBack", "AuthTokenCallBack:error:" + str2);
                    errorStatus = new ErrorStatus(i2, str2);
                    e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                } else {
                    e.b("AuthTokenCallBack", "AuthTokenCallBack:run bundle is null");
                    errorStatus = new ErrorStatus(i2, "bundle is null");
                    e.b("AuthTokenCallBack", "error: " + errorStatus.toString());
                }
                intent = new Intent();
                intent.setPackage(this.a.getPackageName());
                intent.putExtra("isUseSDK", false);
                intent.putExtra("parce", errorStatus);
                c.b(this.a, intent);
                this.a.finish();
                return;
            }
            this.a.d = (String) bundle2.get("authAccount");
            this.a.e = (String) bundle2.get("accountType");
            this.a.c = (String) bundle2.get("authtoken");
            e.b("AuthTokenCallBack", "AuthTokenCallBack: accountName=" + f.c(this.a.d) + " accountType=" + this.a.e);
            this.a.a(this.a.c, this.a.d, i2, bundle2);
        }
    }

    private class b implements AccountManagerCallback<Bundle> {
        final /* synthetic */ DummyActivity a;

        private b(DummyActivity dummyActivity) {
            this.a = dummyActivity;
        }

        public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
            if (accountManagerFuture != null) {
                try {
                    this.a.m = (Bundle) accountManagerFuture.getResult();
                    if (this.a.a(this.a.m)) {
                        if (VERSION.SDK_INT > 22) {
                            this.a.b();
                            return;
                        }
                    }
                    this.a.b(this.a.m);
                    this.a.finish();
                } catch (OperationCanceledException e) {
                    this.a.finish();
                    e.d("DummyActivity", "OperationCanceledException / " + e.getMessage());
                } catch (AuthenticatorException e2) {
                    this.a.finish();
                    e.d("DummyActivity", "AuthenticatorException / " + e2.getMessage());
                } catch (IOException e3) {
                    this.a.finish();
                    e.d("DummyActivity", "IOException / " + e3.getMessage());
                }
            }
        }
    }

    protected void onCreate(Bundle bundle) {
        e.b("DummyActivity", "onCreate");
        super.onCreate(bundle);
        this.k = getIntent();
        if (this.k != null) {
            this.l = this.k.getBundleExtra("bundle");
            if (this.l == null) {
                this.l = new Bundle();
            }
            this.n = this.l.getBoolean("isTransNavigationBar", false);
            requestWindowFeature(1);
            Window window = getWindow();
            window.setFlags(67108864, 67108864);
            if (VERSION.SDK_INT >= 19 && this.n) {
                window.setFlags(134217728, 134217728);
            }
            if (com.huawei.hwid.core.d.b.a((Activity) this, Boolean.valueOf(true))) {
                com.huawei.hwid.core.d.b.a((Activity) this, true);
            }
            this.a = AccountManager.get(this);
            this.b = this.k.getStringExtra("requestTokenType");
            if (TextUtils.isEmpty(this.b)) {
                e.b("DummyActivity", "params invalid: tokenType is null");
                finish();
                return;
            }
            LoginHandler a = v.a();
            if (a == null) {
                e.b("DummyActivity", "params invalid: loginHandler is null");
                finish();
                return;
            } else if (com.huawei.hwid.core.d.b.b(this, "com.huawei.hwid.GET_AUTH_TOKEN")) {
                d.a((Context) this, a, v.b());
                this.l.putString("ServiceType", this.b);
                c();
                return;
            } else {
                Account[] accountsByType = this.a.getAccountsByType("com.huawei.hwid");
                String[] strArr = new String[]{""};
                if (accountsByType != null && accountsByType.length > 0) {
                    this.l.putBoolean("chooseAccount", true);
                    this.a.getAuthToken(accountsByType[0], getPackageName(), this.l, this, new a(this), null);
                } else {
                    this.a.getAuthTokenByFeatures("com.huawei.hwid", "com.huawei.hwid", strArr, this, this.l, this.l, new a(this), null);
                }
                return;
            }
        }
        e.b("DummyActivity", "we got a wrong intent");
        finish();
    }

    @TargetApi(23)
    private void b() {
        if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            l.c((Context) this);
            l.a((Context) this);
            l.e(this);
            b(this.m);
            finish();
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10003) {
            if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
                l.c((Context) this);
                l.a((Context) this);
                l.e(this);
                b(this.m);
                finish();
                return;
            }
            a(getString(j.a(this, "CS_read_phone_state_permission")));
        }
    }

    public void a(String str) {
        if (TextUtils.isEmpty(str)) {
            e.d("DummyActivity", "READ_PHONE_STATE PermissionName is null!");
            Parcelable errorStatus = new ErrorStatus(28, "READ_PHONE_STATE  Permission is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            c.b(this, intent);
            finish();
            return;
        }
        this.i = com.huawei.hwid.core.d.b.e(this, str).setNegativeButton(17039360, new c(this)).setPositiveButton(j.a(this, "CS_go_settings"), new b(this)).create();
        this.i.setCancelable(false);
        this.i.setCanceledOnTouchOutside(false);
        this.i.setOnDismissListener(new d(this));
        if (!isFinishing()) {
            this.i.show();
        }
    }

    public void a() {
        if (this.i != null) {
            this.i.dismiss();
            this.i = null;
        }
    }

    private void c() {
        this.k = new Intent("com.huawei.hwid.GET_AUTH_TOKEN");
        this.k.putExtras(this.l);
        this.k.putExtra("isTransNavigationBar", this.n);
        this.k.setPackage("com.huawei.hwid");
        try {
            startActivityForResult(this.k, 1);
        } catch (Exception e) {
            e.d("DummyActivity", "SDK can not start intent for GETTOKEN");
            Parcelable errorStatus = new ErrorStatus(15, "Access is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            c.b(this, intent);
            finish();
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        e.b("DummyActivity", "onActivityResult::requestCode==>" + i);
        try {
            Thread.sleep(50);
        } catch (Throwable e) {
            e.d("DummyActivity", "InterruptedException / " + e.getMessage(), e);
        }
        if (1 == i) {
            Bundle bundle;
            int i3;
            Object obj;
            Parcelable errorStatus;
            Intent intent2;
            String str = "";
            e.b("DummyActivity", "onActivityResult::resultCode==>" + i2);
            if (-1 == i2) {
                Bundle extras = intent.getExtras();
                ErrorStatus a = a(i, i2, intent, extras);
                int errorCode = a.getErrorCode();
                String errorReason = a.getErrorReason();
                bundle = extras;
                i3 = errorCode;
                String str2 = errorReason;
            } else if (i2 != 0) {
                e.b("DummyActivity", "OperationCanceledException");
                bundle = null;
                i3 = 3002;
                obj = "getAuthTokenByFeatures : OperationCanceledException occur";
            } else {
                errorStatus = new ErrorStatus(3002, "getAuthTokenByFeatures : OperationCanceledException occur");
                e.b("DummyActivity", "error: " + errorStatus.toString());
                intent2 = new Intent();
                intent2.setPackage(getPackageName());
                intent2.putExtra("isUseSDK", false);
                intent2.putExtra("parce", errorStatus);
                c.b(this, intent2);
                finish();
                return;
            }
            if (!(i3 == 0 || TextUtils.isEmpty(obj)) || bundle == null) {
                if (bundle != null) {
                    e.b("DummyActivity", "AuthTokenCallBack:error");
                    errorStatus = new ErrorStatus(i3, obj);
                    e.b("DummyActivity", "error: " + errorStatus.toString());
                } else {
                    e.b("DummyActivity", "AuthTokenCallBack:run bundle is null");
                    errorStatus = new ErrorStatus(i3, "bundle is null");
                    e.b("DummyActivity", "error: " + errorStatus.toString());
                }
                intent2 = new Intent();
                intent2.setPackage(getPackageName());
                intent2.putExtra("isUseSDK", false);
                intent2.putExtra("parce", errorStatus);
                c.b(this, intent2);
                finish();
                return;
            }
            a(this.c, this.d, i3, bundle);
        }
    }

    private ErrorStatus b(String str) {
        int i;
        String str2;
        String str3 = "";
        if ("AuthenticatorException".equals(str)) {
            i = 3003;
            str2 = "getAuthTokenByFeatures : AuthenticatorException occur";
            e.b("DummyActivity", "AuthenticatorException");
        } else if ("IOException".equals(str)) {
            i = 3004;
            str2 = "getAuthTokenByFeatures : IOException occur";
            e.b("DummyActivity", "IOException");
        } else if ("AccessException".equals(str)) {
            i = 15;
            str2 = "Access is not allowed";
            e.b("DummyActivity", "AccessError:appID is not allowed");
        } else if ("AreaNotAllowException".equals(str)) {
            i = 23;
            str2 = "AreaNotAllowError: Area is not allowed";
            e.b("DummyActivity", "AreaNotAllowError: Area is not allowed");
        } else if ("HwIDNotAllowException".equals(str)) {
            i = 24;
            str2 = "HwIDNotAllowError: HwID is not allowed";
            e.b("DummyActivity", "HwIDNotAllowError: HwID is not allowed");
        } else {
            i = 3002;
            str2 = "getAuthTokenByFeatures : OperationCanceledException occur";
            e.b("DummyActivity", "OperationCanceledException");
        }
        return new ErrorStatus(i, str2);
    }

    private ErrorStatus a(int i, int i2, Intent intent, Bundle bundle) {
        String str = null;
        if (v.a() == null) {
            m.a(this, getString(j.a(this, "CS_system_error_tip")), 1);
            e.b("DummyActivity", "callback is null, please login again!");
            finish();
        }
        if (bundle != null) {
            str = (String) bundle.get("Exception");
        }
        if (str == null || !"".equals(str)) {
            return b(str);
        }
        this.d = (String) bundle.get("authAccount");
        this.e = (String) bundle.get("accountType");
        this.c = (String) bundle.get("authtoken");
        this.h = bundle.getBoolean("useSelfAccount", false);
        if (bundle.containsKey("loginUserName")) {
            this.f = bundle.getString("loginUserName");
        }
        if (bundle.containsKey("countryIsoCode")) {
            this.g = bundle.getString("countryIsoCode");
        }
        return new ErrorStatus(0, "");
    }

    private void a(String str, String str2, int i, Bundle bundle) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            Parcelable errorStatus = new ErrorStatus(i, "token or accountName is null");
            e.b("DummyActivity", "error: " + errorStatus.toString());
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            c.b(this, intent);
            finish();
        } else if (this.h) {
            if (bundle != null) {
                Bundle bundle2 = bundle.getBundle("bundle");
                if (bundle2 != null) {
                    bundle2.putBundle("envExtra", bundle.getBundle("envExtra"));
                }
                this.m = bundle2;
                if (a(this.m) && VERSION.SDK_INT > 22) {
                    b();
                } else {
                    b(this.m);
                    finish();
                }
            }
        } else {
            Account account = new Account(str2, "com.huawei.hwid");
            Bundle bundle3 = new Bundle();
            bundle3.putBoolean("getUserId", true);
            this.a.updateCredentials(account, this.b, bundle3, this, new b(), null);
        }
    }

    private boolean a(Bundle bundle) {
        if (bundle != null) {
            String str = (String) bundle.get("deviceId");
            String str2 = (String) bundle.get("deviceType");
            if (TextUtils.isEmpty(str) || "null".equalsIgnoreCase(str) || TextUtils.isEmpty(str2) || "null".equalsIgnoreCase(str2)) {
                return true;
            }
            return false;
        }
        e.b("DummyActivity", "bundle is null isNeedcheckPermission false");
        return false;
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            e.d("DummyActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    private void b(Bundle bundle) {
        if (bundle != null) {
            String str = (String) bundle.get("userId");
            int i = bundle.getInt("siteId", 0);
            String str2 = (String) bundle.get("deviceId");
            String str3 = (String) bundle.get("deviceType");
            String str4 = (String) bundle.get("Cookie");
            String str5 = (String) bundle.get("accountType");
            if (TextUtils.isEmpty(str2) || "null".equalsIgnoreCase(str2)) {
                str2 = l.b((Context) this);
                if (str2 == null) {
                    str2 = "";
                }
            }
            if (TextUtils.isEmpty(str3) || "null".equalsIgnoreCase(str3)) {
                str3 = l.a((Context) this, l.b((Context) this));
            }
            e.b("DummyActivity", "sendSuccess is" + str3);
            if (com.huawei.hwid.core.d.b.d(str5) && !TextUtils.isEmpty(this.d)) {
                this.d = k.c(this.d, str5);
            }
            if (!(this.d == null || this.c == null)) {
                Parcelable hwAccount = new HwAccount();
                hwAccount.b(this.d);
                hwAccount.h(str2);
                hwAccount.i(str3);
                hwAccount.a(i);
                hwAccount.f(this.c);
                hwAccount.d(str);
                hwAccount.c(this.b);
                hwAccount.e(str4);
                hwAccount.g(str5);
                hwAccount.j(this.f);
                hwAccount.a(this.g);
                Intent intent = new Intent();
                intent.setPackage(getPackageName());
                intent.putExtra("hwaccount", hwAccount);
                intent.putExtra("isUseSDK", false);
                intent.putExtra("envExtra", bundle.getBundle("envExtra"));
                c.a(this, intent);
            }
            return;
        }
        e.b("DummyActivity", "bundle is null");
    }

    protected void onDestroy() {
        super.onDestroy();
        e.b("DummyActivity", "onDestroy");
        Bundle bundle = new Bundle();
        bundle.putBoolean("LoginBroadcastReceiver", true);
        d.a((Context) this, bundle);
        a();
    }

    protected void onResume() {
        super.onResume();
        e.b("DummyActivity", "onResume");
        if (this.j) {
            this.j = false;
            if (VERSION.SDK_INT <= 22) {
                return;
            }
            if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                l.c((Context) this);
                l.a((Context) this);
                l.e(this);
                b(this.m);
                finish();
                return;
            }
            e.d("DummyActivity", "READ_PHONE_STATE PermissionName is null!");
            Parcelable errorStatus = new ErrorStatus(28, "READ_PHONE_STATE  Permission is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            c.b(this, intent);
            finish();
        }
    }
}
