package com.huawei.hwid.ui.common.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.a.d;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.h;
import com.huawei.hwid.ui.common.j;
import java.util.ArrayList;

public class LoginActivity extends LoginBaseActivity {
    private Button p;
    private boolean q = true;
    private Handler r = new b(this);

    public static void a(Activity activity, f fVar, String str, boolean z, String str2, int i, Bundle bundle) {
        if (fVar != null) {
            Intent putExtra = new Intent(activity, LoginActivity.class).putExtra("startActivityWay", fVar.ordinal()).putExtra("isFromGuide", z).putExtra("requestTokenType", str2).putExtra("topActivity", str);
            if (bundle != null) {
                putExtra.putExtras(bundle);
            }
            activity.startActivityForResult(putExtra, i);
            return;
        }
        a.d("LoginActivity", "StartActivityWay is null!");
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        a.a("LoginActivity", "onCreate");
        c(true);
        s();
        c cVar = new c(this, "8");
        if (f.FromOOBE == q()) {
            cVar.a(true);
        }
        d.a(cVar, this);
    }

    private void s() {
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_login_activity"));
            TextView textView = (TextView) findViewById(m.e(this, "tv_login"));
            this.a = (Button) findViewById(m.e(this, "btn_next"));
            this.p = (Button) findViewById(m.e(this, "btn_back"));
            this.a.setText(m.a(this, "CS_log_in"));
        } else {
            a(m.a(this, "CS_log_in"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_login_activity"));
            this.p = (Button) findViewById(m.e(this, "btn_back"));
            this.a = (Button) findViewById(m.e(this, "btn_email_login"));
        }
        this.p = (Button) findViewById(m.e(this, "btn_back"));
        this.p.setOnClickListener(new c(this));
        this.a.setOnClickListener(this.j);
        this.a.setEnabled(false);
        this.b = (EditText) findViewById(m.e(this, "email_name"));
        if (k.f(this) || k.e(this) || q.a((Context) this, (int) MsgUrlService.RESULT_NOT_IMPL).startsWith("460")) {
            this.b.setHint(m.a(this, "CS_reset_name_hint"));
        } else {
            this.b.setHint(m.a(this, "CS_input_emailaddr"));
        }
        d dVar = new d(this, this.b);
        this.c = (EditText) findViewById(m.e(this, "input_password"));
        e eVar = new e(this, this, this.c);
        this.d = (TextView) findViewById(m.e(this, "forget_pwd"));
        this.e = (TextView) findViewById(m.e(this, "register_hwid"));
        if (l()) {
            this.e.setVisibility(0);
        }
        CharSequence stringExtra = getIntent().getStringExtra("authAccount");
        if (!TextUtils.isEmpty(stringExtra) && getIntent().hasExtra("allowChangeAccount")) {
            d(getIntent().getBooleanExtra("allowChangeAccount", true));
            if (!g()) {
                this.b.setText(stringExtra);
                this.b.setFocusableInTouchMode(false);
                this.b.setEnabled(false);
                this.q = false;
            }
        }
        if (f.FromOpenSDK == q()) {
            stringExtra = getIntent().getStringExtra("openSDKPhoneNumber");
            if (TextUtils.isEmpty(stringExtra)) {
                a.b("LoginActivity", "from openSDK, phonenumber is empty, return to StartUpGuideLoginActivity");
                a(false, null);
                return;
            }
            this.b.setText(stringExtra);
            this.b.setFocusableInTouchMode(false);
            this.b.setEnabled(false);
            this.q = false;
        }
        this.h = (LinearLayout) findViewById(m.e(this, "select_layout"));
        if (h.a((Context) this, l(), this.q)) {
            new Thread(new f(this)).start();
        }
        this.d.setOnClickListener(new g(this));
        this.e.setOnClickListener(new h(this));
        this.f = (TextView) findViewById(m.e(this, "display_pass"));
        this.g = (LinearLayout) findViewById(m.e(this, "display_pass_layout"));
        this.g.setOnClickListener(this.k);
    }

    private void a(ArrayList arrayList) {
        this.i = arrayList;
        if (!this.i.isEmpty()) {
            h.b(this, this.b, this.h);
            this.h.setOnClickListener(this.l);
            String str = (String) this.i.get(this.i.size() - 1);
            if (!TextUtils.isEmpty(str)) {
                this.b.setText(str);
                this.b.setSelection(str.length());
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        a.e("LoginActivity", "LoginActiviy, onNewIntent");
        if (intent != null && intent.hasExtra("completed")) {
            Intent intent2;
            if (com.huawei.hwid.core.c.d.l(this) && com.huawei.hwid.core.c.d.j((Context) this)) {
                if (q() == f.FromApp) {
                    a.e("LoginActivity", "startActivityWay is FromApp and don't use SDK");
                    intent2 = new Intent();
                    intent2.putExtra("isUseSDK", false);
                    intent2.setPackage(k());
                    if (intent.getBooleanExtra("completed", false)) {
                        intent2.putExtra("isChangeAccount", false);
                        intent2.putExtra("currAccount", intent.getStringExtra("accountName"));
                        intent2.putExtras(intent);
                        intent2.putExtra("bundle", j.a(intent.getBundleExtra("bundle"), k()));
                        a(intent2, true);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("errorcode", 3002);
                        bundle.putString("errorreason", "getAuthTokenByFeatures : OperationCanceledException occur");
                        intent2.putExtra("bundle", bundle);
                        a(intent2, false);
                    }
                    finish();
                } else {
                    a.e("LoginActivity", "startActivityWay is not FromApp and don't use SDK");
                    setIntent(intent);
                    if (intent.getBooleanExtra("completed", false)) {
                        setResult(-1, getIntent());
                    } else {
                        setResult(0, getIntent());
                    }
                }
                super.onNewIntent(intent);
            } else if (!l() && intent.getBooleanExtra("completed", false)) {
                intent2 = new Intent();
                intent2.putExtra("isChangeAccount", false);
                intent2.putExtra("isUseSDK", true);
                intent2.putExtra("currAccount", intent.getStringExtra("accountName"));
                intent2.putExtras(intent);
                intent2.setPackage(getPackageName());
                a(intent2, true);
            }
            finish();
        } else if (intent != null && intent.hasExtra("tokenInvalidate")) {
            this.c.setText("");
        } else if (intent != null && intent.hasExtra("loginWithUserName")) {
            CharSequence string = intent.getExtras().getString("authAccount");
            if (string != null && !TextUtils.isEmpty(string)) {
                this.b.setText(string);
            }
        } else {
            finish();
        }
    }

    private void a(Intent intent, boolean z) {
        if (p()) {
            a.b("LoginActivity", "start APK by old way, set result to StartUpGuideLoginActivity");
            setResult(-1, intent);
        } else if (z) {
            e.a(this, intent);
        } else {
            e.b(this, intent);
        }
    }

    protected void onDestroy() {
        a.b("LoginActivity", "onDestroy");
        super.onDestroy();
    }
}
