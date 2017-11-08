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

public class SetRegisterEmailPasswordActivity extends LoginRegisterCommonActivity {
    protected boolean a = true;
    private Button b = null;
    private Button c = null;
    private EditText d;
    private EditText e;
    private String f;
    private c g;
    private boolean h = false;
    private boolean i = false;
    private boolean j = true;
    private int k = 1;
    private String l = "";
    private TextView p;
    private LinearLayout q;
    private HwAccount r;
    private OnClickListener s = new bq(this);
    private OnClickListener t = new br(this);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (intent != null) {
            this.f = intent.getStringExtra("accountName");
            this.i = intent.getBooleanExtra("isEmotionIntroduce", false);
        }
        if (TextUtils.isEmpty(this.f)) {
            finish();
        } else {
            g();
        }
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
        this.c.setOnClickListener(new bn(this));
        this.d = (EditText) findViewById(m.e(this, "input_password"));
        this.e = (EditText) findViewById(m.e(this, "confirm_password"));
        this.d.setHint(m.a(this, "CS_old_pwd"));
        this.d.requestFocus();
        this.b.setOnClickListener(this.t);
        g.a(this.d, this.e, this.b);
        bo boVar = new bo(this, this, this.d);
        bp bpVar = new bp(this, this, this.e);
        this.p = (TextView) findViewById(m.e(this, "display_pass"));
        this.q = (LinearLayout) findViewById(m.e(this, "display_pass_layout"));
        this.q.setOnClickListener(this.s);
    }

    private void h() {
        b(false);
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.i(this, new Bundle()), null, a(new bv(this, this)));
        a(getString(m.a(this, "CS_registering_message")));
    }

    private void i() {
        i.a((Context) this, new f(this, HwAccountConstants.a()), null, a(new bt(this, this)));
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
        com.huawei.hwid.core.model.http.a wVar = new w(this, replace, d, i, k(), null, bundle);
        wVar.a(agreementVersionArr);
        i.a((Context) this, wVar, null, a(new by(this, this)));
    }

    private void s() {
        this.g = new c(this, "1", this.f);
        if (com.huawei.hwid.ui.common.f.FromOOBE == q() || this.i) {
            this.g.a(true);
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("needActivateVip", this.n);
        com.huawei.hwid.core.model.http.a tVar = new t(this, this.f, e.d(this, this.d.getText().toString()), k(), bundle);
        i.a((Context) this, tVar, e.d(this, this.d.getText().toString()), a(new bw(this, this, tVar, com.huawei.hwid.manager.f.a(this))));
    }

    private void t() {
        this.d.setText("");
        this.e.setText("");
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (m() && 2 == i) {
            setResult(-1);
            finish();
        }
    }

    private void a(String str, String str2, int i) {
        Bundle bundle = new Bundle();
        HwAccount hwAccount = (HwAccount) getIntent().getParcelableExtra("hwaccount");
        if (hwAccount == null) {
            com.huawei.hwid.core.c.b.a.d("RegisterEmailActivity", "bindLoginEmail fail");
            b();
            return;
        }
        String str3 = str;
        String str4 = str2;
        int i2 = i;
        i.a((Context) this, new b(this, str3, str4, i2, k(), null, hwAccount.c(), bundle), null, a(new bu(this, this)));
    }

    private void a(Bundle bundle) {
        String stringExtra = getIntent().getStringExtra("third_openid");
        String stringExtra2 = getIntent().getStringExtra("third_access_token");
        String string = bundle.getString("userId");
        String string2 = bundle.getString("userName");
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.a(this, ParseMeizuManager.SMS_FLOW_THREE, string2, stringExtra, stringExtra2, string), string2, a(new bs(this, this, bundle)));
        a(getString(m.a(this, "CS_logining_message")));
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("RegisterEmailActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    protected void d(String str) {
        if (this.j && !m() && !l() && "com.huawei.hwid".equals(getPackageName()) && d.n(this)) {
            super.d(str);
        }
    }

    private void a(HwAccount hwAccount, Bundle bundle) {
        b();
        a(hwAccount);
        if (o()) {
            a(bundle);
            return;
        }
        Intent intent = new Intent(this, RegisterResetVerifyEmailActivity.class);
        intent.putExtra("isFromRegister", true);
        intent.putExtra("commonloginsuccess", true);
        intent.putExtra("bundle", bundle);
        intent.putExtra("account", hwAccount);
        intent.putExtra("emailName", this.f);
        intent.putExtras(getIntent());
        startActivityForResult(intent, 1);
        this.j = false;
    }
}
