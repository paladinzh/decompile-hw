package com.huawei.hwid.manager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.z;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;
import com.huawei.hwid.ui.common.login.LoginActivity;
import java.util.List;

public class AccountManagerActivity extends BaseActivity {
    private AlertDialog a;
    private String b = "";
    private String c = "";
    private f d = f.Default;
    private Account e;
    private boolean f = true;
    private boolean g = false;
    private boolean h = false;
    private c i;
    private boolean j = false;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        a.b("AccountManagerActivity", "onCreate, savedInstanceState:" + bundle);
        requestWindowFeature(1);
        this.b = getIntent().getStringExtra("topActivity");
        this.c = getIntent().getStringExtra("requestTokenType");
        this.j = getIntent().getBooleanExtra("onlyRegisterPhone", false);
        this.g = getIntent().getBooleanExtra("check_sim_status", false);
        this.h = getIntent().getBooleanExtra("IS_FROM_NO_AUTH", false);
        a.b("AccountManagerActivity", "AccountManagerActivity onCreate() mReqeustTokenType=" + this.c);
        int intExtra = getIntent().getIntExtra("startActivityWay", f.Default.ordinal());
        if (intExtra > 0 && intExtra < f.values().length) {
            this.d = f.values()[intExtra];
        }
        g();
    }

    protected void onDestroy() {
        a.e("AccountManagerActivity", "onDestroy");
        if (this.a != null && this.a.isShowing()) {
            this.a.dismiss();
        }
        super.onDestroy();
    }

    protected void onResume() {
        a.b("AccountManagerActivity", "onResume");
        if (this.f) {
            showDialog(1);
        }
        super.onResume();
    }

    protected void onStop() {
        a.e("AccountManagerActivity", "onStop");
        removeDialog(1);
        super.onStop();
    }

    protected void onRestoreInstanceState(Bundle bundle) {
        a.e("AccountManagerActivity", "onRestoreInstanceState");
        super.onRestoreInstanceState(bundle);
        finish();
    }

    private void g() {
        this.f = b.d(this, this.c);
        if (!this.f) {
            a(true, b.e(this, this.c));
        }
    }

    private void a(boolean z, String str) {
        String peekAuthToken;
        String str2 = "";
        if (d.j((Context) this) && d.l(this)) {
            AccountManager accountManager = AccountManager.get(this);
            Account[] accountsByType = accountManager.getAccountsByType("com.huawei.hwid");
            if (z && !TextUtils.isEmpty(str) && accountsByType != null && accountsByType.length > 0) {
                for (Account account : accountsByType) {
                    if (account.name.equals(str)) {
                        this.e = account;
                        break;
                    }
                }
            }
            if (this.e == null) {
                if (accountsByType != null && accountsByType.length > 0) {
                    this.e = accountsByType[0];
                    str = this.e.name;
                } else {
                    finish();
                    return;
                }
            }
            try {
                peekAuthToken = accountManager.peekAuthToken(this.e, "cloud");
            } catch (Throwable e) {
                a.d("AccountManagerActivity", e.toString(), e);
                peekAuthToken = str2;
            }
            str2 = com.huawei.hwid.simchange.b.b.a(this);
            if (this.h && "blocked".equals(str2)) {
                this.i = new c(this, "5", str);
                com.huawei.hwid.simchange.b.b.a((Activity) this, accountManager.getUserData(this.e, "userId"), 11);
                a(str, peekAuthToken, this.c);
                return;
            }
        }
        HwAccount c = f.a(this).c(this, str, this.c);
        if (c == null) {
            a.a("AccountManagerActivity", "SDK has no account");
            finish();
            return;
        }
        peekAuthToken = c.f();
        this.e = new Account(c.a(), "com.huawei.hwid");
        this.i = new c(this, "5", str);
        a(str, peekAuthToken, this.c);
    }

    protected Dialog onCreateDialog(int i) {
        if (i != 1) {
            return null;
        }
        String[] j = j();
        Object k = k();
        Object obj = new String[(k.length + 1)];
        System.arraycopy(k, 0, obj, 0, k.length);
        obj[obj.length - 1] = getString(m.a(this, "CS_choose_another_account"));
        int length = obj.length;
        this.a = new Builder(this, j.b(this)).setOnCancelListener(new b(this)).setAdapter(new ArrayAdapter(this, m.d(this, "cs_listview_item"), m.e(this, "id_txt"), obj), new a(this, length, j)).create();
        this.a.setCanceledOnTouchOutside(false);
        return this.a;
    }

    private void d(boolean z) {
        if (z) {
            Intent intent = new Intent();
            a(intent);
            intent.putExtra("requestTokenType", this.c);
            intent.putExtra("isFromManager", true);
            intent.putExtra("startActivityWay", this.d.ordinal());
            intent.putExtra("topActivity", getClass().getName());
            intent.putExtra("onlyRegisterPhone", this.j);
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
        } else if (this.e == null) {
            finish();
        } else {
            a(false, this.e.name);
            this.a.dismiss();
        }
    }

    private void h() {
        if (!d.j((Context) this) || !d.l(this)) {
            i();
        } else if (this.d == f.FromApp) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt("errorcode", 3002);
            bundle.putString("errorreason", "getAuthTokenByFeatures : OperationCanceledException occur");
            intent.putExtra("bundle", bundle);
            intent.putExtra("isUseSDK", false);
            intent.setPackage(this.c);
            a((Context) this, intent, 150);
        }
    }

    private void a(Context context, Intent intent, long j) {
        AlarmManager alarmManager;
        try {
            alarmManager = (AlarmManager) context.getSystemService("alarm");
        } catch (Throwable e) {
            a.d("AccountManagerActivity", "catch Exception throw by AlarmManager!", e);
            alarmManager = null;
        }
        if (alarmManager == null) {
            e.b(this, intent);
            return;
        }
        alarmManager.set(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(context, 0, intent, 134217728));
    }

    protected void a(String str, Intent intent, String str2) {
        boolean z;
        if (intent == null) {
            intent = new Intent();
        }
        intent.setPackage(str2);
        String str3 = "isUseSDK";
        if (this.d == f.FromApp) {
            z = false;
        } else {
            z = true;
        }
        intent.putExtra(str3, z);
        intent.putExtra("isChangeAccount", true);
        intent.putExtra("currAccount", str);
        e.a(this, intent);
        finish();
    }

    private void i() {
        Intent intent = new Intent();
        intent.putExtra("isUseSDK", true);
        intent.putExtra("completed", false);
        intent.setPackage(getPackageName());
        e.b(this, intent);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        a.e("AccountManagerActivity", "AccountManagerActiviy onNewIntentcontent:" + com.huawei.hwid.core.encrypt.f.a(intent));
        if (intent.hasExtra("completed")) {
            Intent intent2;
            if (d.l(this) && d.j((Context) this)) {
                a.b("AccountManagerActivity", "callingPkg:" + this.c);
                if (this.d != f.FromApp) {
                    a.e("AccountManagerActivity", "PARA_COMPLETED, info:" + com.huawei.hwid.core.encrypt.f.a(intent));
                    setIntent(intent);
                    setResult(-1, getIntent());
                } else {
                    intent2 = new Intent();
                    intent2.putExtra("isUseSDK", false);
                    intent2.setPackage(this.c);
                    if (intent.getBooleanExtra("completed", false)) {
                        intent2.putExtra("isChangeAccount", false);
                        intent2.putExtra("currAccount", intent.getStringExtra("accountName"));
                        intent2.putExtras(intent);
                        intent2.putExtra("bundle", j.a(intent.getBundleExtra("bundle"), this.c));
                        e.a(this, intent2);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("errorcode", 3002);
                        bundle.putString("errorreason", "getAuthTokenByFeatures : OperationCanceledException occur");
                        intent2.putExtra("bundle", bundle);
                        e.b(this, intent2);
                    }
                }
            } else {
                intent2 = new Intent();
                if (intent.getBooleanExtra("completed", false)) {
                    intent2.setPackage(getPackageName());
                    intent2.putExtra("isUseSDK", true);
                    intent2.putExtra("isChangeAccount", false);
                    intent2.putExtra("currAccount", intent.getStringExtra("accountName"));
                    intent2.putExtras(intent);
                    e.a(this, intent2);
                } else {
                    intent2.putExtra("isUseSDK", true);
                    intent2.setPackage(getPackageName());
                    e.b(this, intent2);
                }
            }
            finish();
        } else if (intent.hasExtra("tokenInvalidate")) {
            a.e("AccountManagerActivity", "PARA_TOKEN_INVALIDATED");
        } else if (intent.hasExtra("loginWithUserName")) {
            a.e("AccountManagerActivity", "PARA_LOGIN_WITH_USERNAME");
        } else {
            a.e("AccountManagerActivity", "finish~");
            finish();
        }
    }

    private String[] j() {
        List a;
        if (d.j((Context) this) && d.l(this)) {
            a = f.a(this).a((Context) this, "com.huawei.hwid");
        } else {
            a = f.a(this).a((Context) this, this.c);
        }
        if (r1 == null || r1.isEmpty()) {
            return new String[0];
        }
        String[] strArr = new String[r1.size()];
        int i = 0;
        for (HwAccount a2 : r1) {
            strArr[i] = a2.a();
            i++;
        }
        return strArr;
    }

    private String[] k() {
        List a;
        if (d.j((Context) this) && d.l(this)) {
            a = f.a(this).a((Context) this, "com.huawei.hwid");
        } else {
            a = f.a(this).a((Context) this, this.c);
        }
        if (r0 == null || r0.isEmpty()) {
            return new String[0];
        }
        String[] strArr = new String[r0.size()];
        int i = 0;
        for (HwAccount a2 : r0) {
            String a3 = a2.a();
            strArr[i] = getString(m.a(this, "CS_choose_current_account"), new Object[]{a3});
            i++;
        }
        return strArr;
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("AccountManagerActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    private void a(String str, String str2, String str3) {
        a.e("AccountManagerActivity", "sendServiceTokenAuthRequest");
        if (!TextUtils.isEmpty(str2)) {
            a(null);
            i.a((Context) this, new z(this, str3, str2, d.a((Context) this, str), null), str, a(new c(this, this)));
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        a.e("AccountManagerActivity", "onActivityResult:" + i + "/" + i2);
        if (intent == null) {
            intent = new Intent();
        }
        a.a("AccountManagerActivity", "intent:" + com.huawei.hwid.core.encrypt.f.a(intent.getExtras()));
        super.onActivityResult(i, i2, intent);
        String peekAuthToken;
        Intent intent2;
        if (10 != i) {
            if (11 == i) {
                if (-1 == i2) {
                    String str = this.c;
                    peekAuthToken = AccountManager.get(this).peekAuthToken(this.e, "cloud");
                    if (!(TextUtils.isEmpty(str) || "cloud".equals(str))) {
                        peekAuthToken = d.b(peekAuthToken, str);
                    }
                    intent2 = new Intent();
                    if (this.d != f.FromApp) {
                        intent2.putExtra("authAccount", this.e.name);
                        intent2.putExtra("accountType", "com.huawei.hwid");
                        intent2.putExtra("authtoken", peekAuthToken);
                        setResult(-1, intent2);
                    } else {
                        intent2.putExtra("bundle", e.a(getBaseContext(), this.e.name, this.c));
                        a(this.e.name, intent2, this.c);
                    }
                }
                finish();
            }
        } else if (-1 == i2) {
            peekAuthToken = intent.getStringExtra("authAccount");
            if (d.j((Context) this) && d.l(this)) {
                intent2 = new Intent();
                if (this.d == f.FromApp) {
                    intent2.setPackage(this.c);
                    intent2.putExtra("bundle", e.a((Context) this, peekAuthToken, this.c));
                    e.a(this, intent2);
                } else {
                    intent2.putExtras(e.a((Context) this, peekAuthToken, this.c));
                    setResult(-1, intent2);
                }
            } else {
                a(peekAuthToken, null, getPackageName());
            }
            finish();
        }
    }

    private void a(Account account) {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        intent.putExtra("topActivity", AccountManagerActivity.class.getName());
        intent.putExtra("requestTokenType", this.c);
        intent.putExtra("isFromManager", true);
        intent.putExtra("startActivityWay", this.d.ordinal());
        if (!(account == null || TextUtils.isEmpty(account.name))) {
            intent.putExtra("authAccount", account.name);
            intent.putExtra("allowChangeAccount", false);
        }
        intent.setFlags(67108864);
        startActivityForResult(intent, 10);
    }

    private void a(Intent intent) {
        if (TextUtils.isEmpty(this.b)) {
            intent.putExtra("topActivity", AccountManagerActivity.class.getName());
        } else {
            intent.putExtra("topActivity", this.b);
        }
        if (!TextUtils.isEmpty(this.c)) {
            intent.putExtra("requestTokenType", this.c);
        }
    }

    protected void onSaveInstanceState(Bundle bundle) {
        a.b("AccountManagerActivity", "onSaveInstanceState");
        super.onSaveInstanceState(bundle);
    }
}
