package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.a.d;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.n;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.s;
import com.huawei.hwid.ui.common.g;
import com.huawei.hwid.ui.common.j;

public class RegisterViaEmailActivity extends LoginRegisterCommonActivity {
    private EditText a = null;
    private Button b = null;
    private Button c = null;
    private String d = null;
    private c e = null;
    private boolean f = false;
    private TextView g;
    private CheckBox h;
    private TextView i;
    private OnClickListener j = new au(this);

    protected void onCreate(Bundle bundle) {
        a.b("RegisterViaEmailActivity", "onCreate");
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_register_email"));
            TextView textView = (TextView) findViewById(m.e(this, "title_view"));
            if (m()) {
                textView.setText(m.a(this, "CS_bind_new_email"));
            } else if (this.n) {
                textView.setText(m.a(this, "CS_register_email"));
            }
        } else {
            if (m()) {
                a(m.a(this, "CS_bind_new_email"), m.g(this, "cs_actionbar_icon"));
            } else {
                a(m.a(this, "CS_register_email"), m.g(this, "cs_actionbar_icon"));
            }
            setContentView(m.d(this, "cs_register_email"));
        }
        this.f = intent.getBooleanExtra("isEmotionIntroduce", false);
        g();
        h();
    }

    protected void onDestroy() {
        a.b("RegisterViaEmailActivity", "onDestroy");
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void g() {
        this.a = (EditText) findViewById(m.e(this, "eMail_name"));
        if (m()) {
            this.a.setHint(m.a(this, "CS_input_email_for_bind"));
        }
        ar arVar = new ar(this, this.a);
        this.b = (Button) findViewById(m.e(this, "btn_next"));
        this.c = (Button) findViewById(m.e(this, "btn_back"));
        this.b.setOnClickListener(this.j);
        this.c.setOnClickListener(new as(this));
        this.g = (TextView) findViewById(m.e(this, "intro_agent"));
        this.g.setText(getString(m.a(this, "CS_agree_huawei_policy_new")));
        this.g.setVisibility(0);
        i();
        this.i = (TextView) findViewById(m.e(this, "forget_pwd"));
        if (k.f(this) || k.e(this) || q.a((Context) this, (int) MsgUrlService.RESULT_NOT_IMPL).startsWith("460")) {
            String str;
            if (m()) {
                str = "CS_bind_new_phone";
            } else {
                str = "CS_phone_register";
            }
            if (this.n) {
                String str2 = "vip_phone_register";
                if (10011 != n.h()) {
                    str = str2;
                } else {
                    TextView textView = (TextView) findViewById(m.e(this, "activate_notice"));
                    textView.setVisibility(0);
                    textView.setText(getString(m.a(this, "vip_email_activate_notice")));
                    str = str2;
                }
            }
            this.i.setText(getString(m.a(this, str)));
            this.i.setOnClickListener(new at(this));
        } else {
            this.i.setVisibility(8);
        }
        this.h = (CheckBox) findViewById(m.e(this, "agree_policy"));
        this.h.setVisibility(0);
        if (!this.n) {
            if (!m() && !o()) {
                if (l()) {
                    return;
                }
            }
            return;
        }
        ((LinearLayout) findViewById(m.e(this, "agree_bar"))).setVisibility(8);
        this.h.setChecked(true);
    }

    private void h() {
        c cVar = new c(this, "7");
        cVar.a(this.f);
        cVar.d("stdRigister");
        d.a(cVar, this);
    }

    private void i() {
        this.g.setText(getString(m.a(this, "CS_agree_huawei_policy_new"), (Object[]) new String[]{getString(m.a(this, "CS_hwid_terms_new")), getString(m.a(this, "CS_hwid_policy_new"))}));
        j.a(this.g, getString(m.a(this, "CS_hwid_terms_new")), new g(this, 0, this.f));
        j.a(this.g, getString(m.a(this, "CS_hwid_policy_new")), new g(this, 2, this.f));
    }

    private boolean s() {
        if (this.a == null || this.a.getText() == null || this.a.getText().length() == 0) {
            return false;
        }
        if (!p.b(this.a.getText().toString())) {
            this.a.setError(getString(m.a(this, "CS_login_username_error")));
            return false;
        } else if (!p.a(this.a.getText().toString())) {
            this.a.setError(getString(m.a(this, "CS_email_address_error")));
            return false;
        } else if (!TextUtils.isEmpty(this.a.getError())) {
            a.e("RegisterViaEmailActivity", "the email has error");
            return false;
        } else if (this.h.isChecked()) {
            return true;
        } else {
            j.a((Context) this, m.a(this, "CS_agree_policy_toast_new"));
            return false;
        }
    }

    private void t() {
        i.a((Context) this, new s(this, this.d), null, a(new av(this, this)));
        a(null);
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (m() && 1 == i && -1 == i2) {
            if (o()) {
                setResult(-1);
            }
            finish();
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("RegisterViaEmailActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
