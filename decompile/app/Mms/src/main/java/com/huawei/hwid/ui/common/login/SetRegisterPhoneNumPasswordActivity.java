package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.util.ParseMeizuManager;
import com.huawei.hwid.a;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.constants.HwAccountConstants;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.b;
import com.huawei.hwid.core.model.http.request.f;
import com.huawei.hwid.core.model.http.request.t;
import com.huawei.hwid.core.model.http.request.w;

public class SetRegisterPhoneNumPasswordActivity extends LoginRegisterCommonActivity {
    protected boolean a = true;
    private Button b = null;
    private Button c = null;
    private EditText d;
    private EditText e;
    private String f;
    private String g;
    private String h;
    private c i;
    private boolean j = false;
    private boolean k = false;
    private int l;
    private TextView p;
    private LinearLayout q;
    private int r = 0;
    private String s = "";
    private OnClickListener t = new cc(this);
    private OnClickListener u = new cd(this);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (intent != null) {
            this.f = intent.getStringExtra("user_phone");
            this.j = intent.getBooleanExtra("is_hottalk_account", false);
            this.g = intent.getStringExtra("verifycode");
            this.h = intent.getStringExtra("country_code");
            this.k = intent.getBooleanExtra("isEmotionIntroduce", false);
            this.r = intent.getIntExtra("onlyBindPhoneForThird", 0);
        }
        g();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void g() {
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_set_password"));
            this.b = (Button) findViewById(m.e(this, "btn_next"));
            this.b.setText(m.a(this, "CS_done"));
        } else {
            a(m.a(this, "CS_set_password"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_set_password"));
            this.b = (Button) findViewById(m.e(this, "btn_submit"));
        }
        this.c = (Button) findViewById(m.e(this, "btn_back"));
        this.c.setOnClickListener(new bz(this));
        this.d = (EditText) findViewById(m.e(this, "input_password"));
        this.e = (EditText) findViewById(m.e(this, "confirm_password"));
        this.d.setHint(m.a(this, "CS_old_pwd"));
        this.d.requestFocus();
        this.b.setOnClickListener(this.u);
        g.a(this.d, this.e, this.b);
        ca caVar = new ca(this, this, this.d);
        cb cbVar = new cb(this, this, this.e);
        this.p = (TextView) findViewById(m.e(this, "display_pass"));
        this.q = (LinearLayout) findViewById(m.e(this, "display_pass_layout"));
        this.q.setOnClickListener(this.t);
    }

    private void h() {
        b(false);
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.i(this, new Bundle()), null, a(new ch(this, this)));
        a(getString(m.a(this, "CS_registering_message")));
    }

    private void i() {
        i.a((Context) this, new f(this, HwAccountConstants.a()), null, a(new cf(this, this)));
    }

    private void a(String str, int i, AgreementVersion[] agreementVersionArr) {
        String replace;
        if (!TextUtils.isEmpty(str) && str.contains("+")) {
            replace = str.replace("+", "00");
        } else {
            replace = str;
        }
        new UserInfo().setLanguageCode(d.e((Context) this));
        Bundle bundle = new Bundle();
        String d = e.d(this, this.d.getText().toString());
        a.a().a(d);
        com.huawei.hwid.core.model.http.a wVar = new w(this, replace, d, i, k(), this.g, bundle);
        wVar.a(agreementVersionArr);
        i.a((Context) this, wVar, null, a(new ck(this, this)));
    }

    private String a(String str, String str2) {
        if (!TextUtils.isEmpty(str2) && str2.contains("+")) {
            str2 = str2.replace("+", "00");
        }
        if (!TextUtils.isEmpty(str) && str.contains("+")) {
            str = str.replace("+", "00");
        }
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || !str2.startsWith(str)) {
            return str2;
        }
        return str2.replaceFirst(str, "");
    }

    private void s() {
        this.i = new c(this, "1", this.f);
        if (com.huawei.hwid.ui.common.f.FromOOBE == q()) {
            this.i.a(true);
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("needActivateVip", false);
        com.huawei.hwid.core.model.http.a tVar = new t(this, a(this.h, this.f), e.d(this, this.d.getText().toString()), k(), bundle);
        i.a((Context) this, tVar, e.d(this, this.d.getText().toString()), a(new ci(this, this, tVar, com.huawei.hwid.manager.f.a(this))));
    }

    private void t() {
        this.d.setText("");
        this.e.setText("");
    }

    private void a(String str, int i) {
        Bundle bundle = new Bundle();
        HwAccount hwAccount = (HwAccount) getIntent().getParcelableExtra("hwaccount");
        if (hwAccount != null) {
            String str2 = str;
            int i2 = i;
            i.a((Context) this, new b(this, str2, e.d(this, this.d.getText().toString()), i2, k(), this.g, hwAccount.c(), bundle), null, a(new cg(this, this)));
        }
    }

    private void a(Bundle bundle) {
        String stringExtra = getIntent().getStringExtra("third_openid");
        String stringExtra2 = getIntent().getStringExtra("third_access_token");
        String string = bundle.getString("userId");
        String string2 = bundle.getString("userName");
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.a(this, ParseMeizuManager.SMS_FLOW_THREE, string2, stringExtra, stringExtra2, string), string2, a(new ce(this, this, bundle)));
        a(getString(m.a(this, "CS_logining_message")));
    }

    private void a(HwAccount hwAccount, Bundle bundle) {
        b();
        a(hwAccount);
        if (this.k) {
            a(true, null);
        } else if (!d.l(this) || !d.j((Context) this)) {
            Intent intent = new Intent();
            intent.putExtra("hwaccount", hwAccount);
            if (l()) {
                a(true, intent);
            } else if (com.huawei.hwid.ui.common.f.FromApp != q()) {
                intent.putExtra("completed", true);
                intent.setFlags(67108864);
                intent.setClass(this, LoginActivity.class);
                startActivity(intent);
            } else {
                intent.setAction("com.huawei.cloudserive.loginSuccess");
                intent.putExtra("bundle", hwAccount.k());
                a(true, intent);
            }
        } else if (o()) {
            a(bundle);
        } else {
            a(true, new Intent().putExtra("bundle", bundle));
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (m() && 1 == i) {
            setResult(-1);
            finish();
        } else if (2 == i) {
            a(true, new Intent().putExtra("bundle", intent.getExtras()));
        } else if (100 == i) {
            com.huawei.hwid.core.c.b.a.b("RegisterPhoneNumActivity", "return frome UpgradeSuccessActivity");
            setResult(-1, intent);
            finish();
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("RegisterPhoneNumActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
