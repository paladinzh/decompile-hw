package com.huawei.hwid.api.common.apkimpl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Window;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.j;

public class DummyActivity extends Activity {
    final int a = 1;
    private AccountManager b = null;
    private String c = "";
    private String d = "";
    private String e = "";
    private String f = "";
    private boolean g = false;
    private AlertDialog h = null;
    private boolean i = false;
    private Intent j = null;
    private Bundle k = null;
    private boolean l;

    protected void onCreate(Bundle bundle) {
        a.b("DummyActivity", "onCreate");
        super.onCreate(bundle);
        this.j = getIntent();
        if (this.j != null) {
            this.k = this.j.getBundleExtra("bundle");
            if (this.k == null) {
                this.k = new Bundle();
            }
            this.l = this.k.getBoolean("isTransNavigationBar", false);
            requestWindowFeature(1);
            Window window = getWindow();
            window.setFlags(67108864, 67108864);
            if (VERSION.SDK_INT >= 19 && this.l) {
                window.setFlags(134217728, 134217728);
            }
            if (d.a((Activity) this, Boolean.valueOf(true))) {
                d.a((Activity) this, true);
            }
            this.b = AccountManager.get(this);
            this.c = this.j.getStringExtra("requestTokenType");
            if (TextUtils.isEmpty(this.c)) {
                a.b("DummyActivity", "params invalid: tokenType is null");
                finish();
                return;
            }
            LoginHandler a = com.huawei.hwid.api.common.c.a.a();
            if (a == null) {
                a.b("DummyActivity", "params invalid: loginHandler is null");
                finish();
                return;
            } else if (d.b((Context) this, "com.huawei.hwid.GET_AUTH_TOKEN")) {
                com.huawei.hwid.api.common.a.a((Context) this, a, com.huawei.hwid.api.common.c.a.b());
                this.k.putString("ServiceType", this.c);
                if (VERSION.SDK_INT <= 22) {
                    c();
                } else {
                    b();
                }
                return;
            } else {
                Account[] accountsByType = this.b.getAccountsByType("com.huawei.hwid");
                String[] strArr = new String[]{""};
                if (accountsByType != null && accountsByType.length > 0) {
                    this.k.putBoolean("chooseAccount", true);
                    this.b.getAuthToken(accountsByType[0], getPackageName(), this.k, this, new e(this), null);
                } else {
                    this.b.getAuthTokenByFeatures("com.huawei.hwid", "cloud", strArr, this, this.k, this.k, new e(this), null);
                }
                return;
            }
        }
        a.b("DummyActivity", "we got a wrong intent");
        finish();
    }

    private void b() {
        if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            q.c((Context) this);
            q.a((Context) this);
            q.e(this);
            c();
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10003) {
            if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
                q.c((Context) this);
                q.a((Context) this);
                q.e(this);
                c();
                return;
            }
            a(getString(m.a(this, "CS_read_phone_state_permission")));
        }
    }

    public void a(String str) {
        if (TextUtils.isEmpty(str)) {
            a.d("DummyActivity", "READ_PHONE_STATE PermissionName is null!");
            Parcelable errorStatus = new ErrorStatus(28, "READ_PHONE_STATE  Permission is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            e.c(this, intent);
            finish();
            return;
        }
        this.h = d.d(this, str).setNegativeButton(17039360, new c(this)).setPositiveButton(m.a(this, "CS_go_settings"), new b(this)).create();
        this.h.setCancelable(false);
        this.h.setCanceledOnTouchOutside(false);
        this.h.setOnDismissListener(new d(this));
        if (!isFinishing()) {
            this.h.show();
        }
    }

    public void a() {
        if (this.h != null) {
            this.h.dismiss();
            this.h = null;
        }
    }

    private void c() {
        this.j = new Intent("com.huawei.hwid.GET_AUTH_TOKEN");
        this.j.putExtras(this.k);
        this.j.putExtra("isTransNavigationBar", this.l);
        this.j.setPackage("com.huawei.hwid");
        try {
            startActivityForResult(this.j, 1);
        } catch (Exception e) {
            a.d("DummyActivity", "SDK can not start intent for GETTOKEN");
            Parcelable errorStatus = new ErrorStatus(15, "Access is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            e.c(this, intent);
            finish();
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        int i3 = 3002;
        Object obj = null;
        super.onActivityResult(i, i2, intent);
        a.b("DummyActivity", "onActivityResult::requestCode==>" + i);
        try {
            Thread.sleep(50);
        } catch (Throwable e) {
            a.d("DummyActivity", "InterruptedException / " + e.toString(), e);
        }
        if (1 == i) {
            Bundle bundle;
            Object obj2;
            Parcelable errorStatus;
            Intent intent2;
            String str = "";
            a.b("DummyActivity", "onActivityResult::resultCode==>" + i2);
            if (-1 == i2) {
                String str2;
                int i4;
                if (com.huawei.hwid.api.common.c.a.a() == null) {
                    j.a((Context) this, getString(m.a(this, "CS_system_error_tip")), 1);
                    a.b("DummyActivity", "callback is null, please login again!");
                    finish();
                }
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String str3 = (String) extras.get("Exception");
                }
                if (obj != null && "".equals(obj)) {
                    this.e = (String) extras.get("authAccount");
                    this.f = (String) extras.get("accountType");
                    this.d = (String) extras.get("authtoken");
                    this.g = extras.getBoolean("useSelfAccount", false);
                    str2 = str;
                    boolean z = false;
                } else if ("AuthenticatorException".equals(obj)) {
                    i4 = 3003;
                    str2 = "getAuthTokenByFeatures : AuthenticatorException occur";
                    a.b("DummyActivity", "AuthenticatorException");
                } else if ("IOException".equals(obj)) {
                    i4 = CommonStatusCodes.AUTH_TOKEN_ERROR;
                    str2 = "getAuthTokenByFeatures : IOException occur";
                    a.b("DummyActivity", "IOException");
                } else if ("AccessException".equals(obj)) {
                    i4 = 15;
                    str2 = "Access is not allowed";
                    a.b("DummyActivity", "AccessError:appID is not allowed");
                } else if ("AreaNotAllowException".equals(obj)) {
                    i4 = 23;
                    str2 = "AreaNotAllowError: Area is not allowed";
                    a.b("DummyActivity", "AreaNotAllowError: Area is not allowed");
                } else if ("HwIDNotAllowException".equals(obj)) {
                    i4 = 24;
                    str2 = "HwIDNotAllowError: HwID is not allowed";
                    a.b("DummyActivity", "HwIDNotAllowError: HwID is not allowed");
                } else {
                    str2 = "getAuthTokenByFeatures : OperationCanceledException occur";
                    a.b("DummyActivity", "OperationCanceledException");
                    i4 = 3002;
                }
                i3 = i4;
                str = str2;
                bundle = extras;
            } else if (i2 != 0) {
                a.b("DummyActivity", "OperationCanceledException");
                obj2 = "getAuthTokenByFeatures : OperationCanceledException occur";
                bundle = null;
            } else {
                errorStatus = new ErrorStatus(3002, "getAuthTokenByFeatures : OperationCanceledException occur");
                a.b("DummyActivity", "error: " + errorStatus.toString());
                intent2 = new Intent();
                intent2.setPackage(getPackageName());
                intent2.putExtra("isUseSDK", false);
                intent2.putExtra("parce", errorStatus);
                e.c(this, intent2);
                finish();
                return;
            }
            if (!(i3 == 0 || TextUtils.isEmpty(obj2)) || bundle == null) {
                if (bundle != null) {
                    a.b("DummyActivity", "AuthTokenCallBack:error");
                    errorStatus = new ErrorStatus(i3, obj2);
                    a.b("DummyActivity", "error: " + errorStatus.toString());
                } else {
                    a.b("DummyActivity", "AuthTokenCallBack:run bundle is null");
                    errorStatus = new ErrorStatus(i3, "bundle is null");
                    a.b("DummyActivity", "error: " + errorStatus.toString());
                }
                intent2 = new Intent();
                intent2.setPackage(getPackageName());
                intent2.putExtra("isUseSDK", false);
                intent2.putExtra("parce", errorStatus);
                e.c(this, intent2);
                finish();
                return;
            }
            a(this.d, this.e, i3, bundle);
        }
    }

    private void a(String str, String str2, int i, Bundle bundle) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            Parcelable errorStatus = new ErrorStatus(i, "token or accountName is null");
            a.b("DummyActivity", "error: " + errorStatus.toString());
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            e.c(this, intent);
            finish();
        } else if (this.g) {
            if (bundle != null) {
                Bundle bundle2 = bundle.getBundle("bundle");
                if (bundle2 != null) {
                    bundle2.putBundle("envExtra", bundle.getBundle("envExtra"));
                }
                a(bundle2);
            }
            finish();
        } else {
            Account account = new Account(str2, "com.huawei.hwid");
            Bundle bundle3 = new Bundle();
            bundle3.putBoolean("getUserId", true);
            this.b.updateCredentials(account, this.c, bundle3, this, new f(), null);
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("DummyActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    private void a(Bundle bundle) {
        if (bundle != null) {
            String str = (String) bundle.get("userId");
            int i = bundle.getInt("siteId", 0);
            String str2 = (String) bundle.get("deviceId");
            String str3 = (String) bundle.get("deviceType");
            String str4 = (String) bundle.get("Cookie");
            String str5 = (String) bundle.get("accountType");
            if (str2 == null) {
                str2 = q.b((Context) this);
                if (str2 == null) {
                    str2 = "";
                }
            }
            if (str3 == null) {
                str3 = q.a((Context) this, q.b((Context) this));
            }
            if (d.f(str5) && !TextUtils.isEmpty(this.e)) {
                this.e = p.c(this.e, str5);
            }
            if (!(this.e == null || this.d == null)) {
                Parcelable hwAccount = new HwAccount();
                hwAccount.a(this.e);
                hwAccount.g(str2);
                hwAccount.h(str3);
                hwAccount.a(i);
                hwAccount.e(this.d);
                hwAccount.c(str);
                hwAccount.b(this.c);
                hwAccount.d(str4);
                hwAccount.f(str5);
                Intent intent = new Intent();
                intent.setPackage(getPackageName());
                intent.putExtra("hwaccount", hwAccount);
                intent.putExtra("isUseSDK", false);
                intent.putExtra("envExtra", bundle.getBundle("envExtra"));
                e.a(this, intent);
            }
            return;
        }
        a.b("DummyActivity", "bundle is null");
    }

    protected void onDestroy() {
        super.onDestroy();
        a.b("DummyActivity", "onDestroy");
        Bundle bundle = new Bundle();
        bundle.putBoolean("LoginBroadcastReceiver", true);
        com.huawei.hwid.api.common.a.a((Context) this, bundle);
        a();
    }

    protected void onResume() {
        super.onResume();
        a.b("DummyActivity", "onResume");
        if (this.i) {
            this.i = false;
            if (VERSION.SDK_INT <= 22) {
                return;
            }
            if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                q.c((Context) this);
                q.a((Context) this);
                q.e(this);
                c();
                return;
            }
            a.d("DummyActivity", "READ_PHONE_STATE PermissionName is null!");
            Parcelable errorStatus = new ErrorStatus(28, "READ_PHONE_STATE  Permission is not allowed");
            Intent intent = new Intent();
            intent.setPackage(getPackageName());
            intent.putExtra("isUseSDK", false);
            intent.putExtra("parce", errorStatus);
            e.c(this, intent);
            finish();
        }
    }
}
