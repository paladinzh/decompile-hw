package com.huawei.hwid.ui.common.login;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.constants.c;
import com.huawei.hwid.manager.AccountManagerActivity;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;

public class LoginRegisterCommonActivity extends BaseActivity {
    private static boolean g = false;
    private String a = "";
    private boolean b = false;
    private boolean c = false;
    private boolean d = false;
    private boolean e = false;
    private f f = f.Default;
    protected String m = "";
    protected boolean n = false;
    protected boolean o = false;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent() != null) {
            this.m = getIntent().getStringExtra("topActivity");
            if (!getPackageName().equals(k()) && this.m == null) {
                this.m = getClass().getName();
            }
            this.a = getIntent().getStringExtra("requestTokenType");
            this.b = getIntent().getBooleanExtra("isFromManager", false);
            this.c = getIntent().getBooleanExtra("isFromGuide", false);
            this.d = getIntent().getBooleanExtra("ONCHECKIDENTITY", false);
            int intExtra = getIntent().getIntExtra("startActivityWay", f.Default.ordinal());
            if (intExtra > 0 && intExtra < f.values().length) {
                this.f = f.values()[intExtra];
            }
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                this.n = extras.getBoolean("needActivateVip", false);
                a.b("LoginRegisterCommonActivity", "needActivateVip:" + this.n);
            }
            this.e = getIntent().getBooleanExtra("BindNewHwAccount", false);
            return;
        }
        a.d("LoginRegisterCommonActivity", "getIntent is null, finish LoginRegisterCommonActivity");
        finish();
    }

    protected void a(boolean z, Intent intent) {
        a.b("LoginRegisterCommonActivity", "It is not from Notification!");
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClassName(this, j());
        if (z) {
            intent.putExtras(new m(true, d(), "com.huawei.hwid", e()).a());
        } else {
            intent.putExtras(new m().a());
        }
        intent.setFlags(67108864);
        g = true;
        a.b("LoginRegisterCommonActivity", "onLoginComplete");
        e.a(this);
        super.startActivityForResult(intent, -1);
    }

    protected String j() {
        a.e("LoginRegisterCommonActivity", "mTopActivity:" + this.m);
        if (p.e(this.m)) {
            return AccountManagerActivity.class.getName();
        }
        return this.m;
    }

    protected String k() {
        if (TextUtils.isEmpty(this.a)) {
            return "cloud";
        }
        return this.a;
    }

    protected boolean l() {
        return this.b;
    }

    protected boolean m() {
        return this.e;
    }

    protected boolean n() {
        return this.d;
    }

    protected boolean o() {
        Intent intent = getIntent();
        if (intent.hasExtra("third_account_type") && c.WEIXIN == ((c) intent.getSerializableExtra("third_account_type"))) {
            return true;
        }
        return false;
    }

    protected boolean p() {
        return this.c;
    }

    private void a(Intent intent) {
        if (!TextUtils.isEmpty(this.m)) {
            intent.putExtra("topActivity", this.m);
        }
        intent.putExtra("requestTokenType", k());
        intent.putExtra("startActivityWay", this.f.ordinal());
        intent.putExtra("needActivateVip", this.n);
    }

    public synchronized void startActivityForResult(Intent intent, int i) {
        a(intent);
        super.startActivityForResult(intent, i);
    }

    protected f q() {
        return this.f;
    }

    protected void a(f fVar) {
        this.f = fVar;
    }

    protected void onResume() {
        super.onResume();
        r();
    }

    protected void r() {
        if ("com.huawei.hwid".equals(getPackageName()) && !l() && d.n(this) && !m() && !o() && !n() && !this.o) {
            a.b("LoginRegisterCommonActivity", "arealdy has one account, can't login.");
            if ("com.huawei.android.ds".equals(this.a)) {
                a(false, null);
                finish();
            } else if (!g) {
                d(getString(m.a(this, "CS_account_exists")));
            }
        }
    }

    protected void d(String str) {
        a.b("LoginRegisterCommonActivity", "hwid has logined account");
        Dialog create = j.a((Context) this, m.a(this, "CS_title_tips"), str, false).create();
        create.setOnDismissListener(new j(this));
        a(create);
        create.show();
    }

    public void e(String str) {
        a.b("LoginRegisterCommonActivity", "show area not allow dialog");
        Dialog create = new Builder(this, j.b(this)).setMessage(str).setTitle(getString(m.a(this, "CS_title_tips"))).setPositiveButton(17039370, null).create();
        create.setOnDismissListener(new k(this));
        a(create);
        create.show();
    }
}
