package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.c.a;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.a.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.n;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.s;
import com.huawei.hwid.ui.common.g;
import com.huawei.hwid.ui.common.j;

public class RegisterViaPhoneNumberActivity extends LoginRegisterCommonActivity {
    Thread a = null;
    private EditText b = null;
    private Button c = null;
    private Button d = null;
    private Button e = null;
    private boolean f = false;
    private String g = null;
    private boolean h = false;
    private TextView i;
    private TextView j;
    private CheckBox k;
    private String[] l;
    private String[] p;
    private int q = 0;
    private boolean r = false;
    private final TextWatcher s = new bg(this);
    private OnClickListener t = new bh(this);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_register_phone_number"));
            TextView textView = (TextView) findViewById(m.e(this, "title_view"));
            if (m()) {
                textView.setText(m.a(this, "CS_bind_new_phone"));
            } else if (this.n) {
                textView.setText(m.a(this, "vip_phone_register"));
            }
        } else {
            if (m()) {
                a(m.a(this, "CS_bind_new_phone"), m.g(this, "cs_actionbar_icon"));
            } else if (this.n) {
                a(m.a(this, "vip_phone_register"), m.g(this, "cs_actionbar_icon"));
            } else {
                a(m.a(this, "CS_register_via_phone_number"), m.g(this, "cs_actionbar_icon"));
            }
            setContentView(m.d(this, "cs_register_phone_number"));
        }
        this.h = intent.getBooleanExtra("isEmotionIntroduce", false);
        this.q = intent.getIntExtra("onlyBindPhoneForThird", 0);
        this.r = intent.getBooleanExtra("onlyRegisterPhone", false);
        h();
        s();
        g();
    }

    private void g() {
        c cVar = new c(this, "7");
        cVar.a(this.h);
        cVar.d("stdRigister");
        d.a(cVar, this);
    }

    private void h() {
        String str;
        this.b = (EditText) findViewById(m.e(this, "phone_number"));
        if (m()) {
            this.b.setHint(m.a(this, "CS_register_reset_phone_number_for_bind"));
        }
        this.c = (Button) findViewById(m.e(this, "btn_next"));
        this.d = (Button) findViewById(m.e(this, "btn_back"));
        this.c.setOnClickListener(this.t);
        this.d.setOnClickListener(new be(this));
        this.c.setText(m.a(this, "CS_next"));
        this.e = (Button) findViewById(m.e(this, "countryregion_btn"));
        this.a = new bk(this);
        this.a.start();
        ((LinearLayout) findViewById(m.e(this, "policy_view"))).setVisibility(0);
        this.i = (TextView) findViewById(m.e(this, "intro_agent"));
        this.i.setText(getString(m.a(this, "CS_agree_huawei_policy_new")));
        i();
        this.j = (TextView) findViewById(m.e(this, "forget_pwd"));
        if (m()) {
            str = "CS_bind_new_email";
        } else {
            str = "CS_email_register";
        }
        if (this.n) {
            String str2 = "CS_register_email";
            if (10011 != n.h()) {
                str = str2;
            } else {
                TextView textView = (TextView) findViewById(m.e(this, "activate_notice"));
                textView.setVisibility(0);
                textView.setText(getString(m.a(this, "vip_phone_activate_notice")));
                str = str2;
            }
        }
        if (1 == this.q) {
            this.j.setVisibility(8);
        }
        if (!m() && a.a() && this.r) {
            this.j.setVisibility(8);
        }
        this.j.setText(getString(m.a(this, str)));
        this.j.setOnClickListener(new bf(this));
        this.k = (CheckBox) findViewById(m.e(this, "agree_policy"));
        if (!this.n) {
            if (!(m() || o())) {
                if (l()) {
                }
            }
            this.b.addTextChangedListener(this.s);
        }
        ((LinearLayout) findViewById(m.e(this, "agree_bar"))).setVisibility(8);
        this.k.setChecked(true);
        this.b.addTextChangedListener(this.s);
    }

    private void i() {
        this.i.setText(getString(m.a(this, "CS_agree_huawei_policy_new"), (Object[]) new String[]{getString(m.a(this, "CS_hwid_terms_new")), getString(m.a(this, "CS_hwid_policy_new"))}));
        j.a(this.i, getString(m.a(this, "CS_hwid_terms_new")), new g(this, 0, this.h));
        j.a(this.i, getString(m.a(this, "CS_hwid_policy_new")), new g(this, 2, this.h));
    }

    private void s() {
        Object a = com.huawei.hwid.core.c.n.a(this);
        if (a != null && !TextUtils.isEmpty(a)) {
            this.b.setText(a);
            this.b.setSelection(a.length());
        }
    }

    private String t() {
        String obj = this.b.getText().toString();
        Object obj2 = this.g;
        if (!TextUtils.isEmpty(obj2) && obj2.contains("+")) {
            obj2 = obj2.replace("+", "00");
        }
        if (TextUtils.isEmpty(obj) || TextUtils.isEmpty(obj2) || !obj.startsWith(obj2)) {
            return obj2 + obj;
        }
        return obj;
    }

    private boolean u() {
        if (this.b.getText().length() < 4) {
            com.huawei.hwid.core.c.b.a.e("RegisterViaPhoneNumberActivity", "the phone number is not long enough");
            return false;
        } else if (TextUtils.isEmpty(this.g)) {
            com.huawei.hwid.core.c.b.a.e("RegisterViaPhoneNumberActivity", "the country code is empty");
            return false;
        } else if (!TextUtils.isEmpty(this.b.getError())) {
            com.huawei.hwid.core.c.b.a.e("RegisterViaPhoneNumberActivity", "the phone number has error");
            return false;
        } else if (this.k.isChecked()) {
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.e("RegisterViaPhoneNumberActivity", "the policy is not agree");
            j.a((Context) this, m.a(this, "CS_agree_policy_toast_new"));
            return false;
        }
    }

    private void b(String str) {
        b(false);
        i.a((Context) this, new s(this, str), null, a(new bi(this, this)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    private void c(String str) {
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.m(this, str, str, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR), null, a(new bj(this, this)));
    }

    private void v() {
        Intent intent = new Intent(this, RegisterViaPhoneNumVerificationActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("user_phone", com.huawei.hwid.core.c.d.c(t()));
        intent.putExtra("country_code", this.g);
        intent.putExtra("is_hottalk_account", this.f);
        if (1 != this.q) {
            startActivityForResult(intent, 101);
        } else {
            startActivityForResult(intent, 102);
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (101 == i && i2 == -1) {
            if (m()) {
                if (o()) {
                    setResult(-1);
                }
                finish();
            } else {
                this.b.setError(intent.getStringExtra("countryCalling_code"));
                this.b.requestFocus();
                this.b.selectAll();
            }
        } else if (102 == i && i2 == -1) {
            setResult(-1, intent);
            finish();
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("RegisterViaPhoneNumberActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
