package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.s;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.ai;
import com.huawei.hwid.core.model.http.request.ak;

public class RegisterViaPhoneNumVerificationActivity extends LoginRegisterCommonActivity {
    private Button a = null;
    private Button b = null;
    private Button c = null;
    private EditText d;
    private CheckBox e;
    private c f;
    private String g = "";
    private long h = System.currentTimeMillis();
    private boolean i = false;
    private String j;
    private boolean k = false;
    private String l = "";
    private s p;
    private OnClickListener q = new ay(this);
    private OnClickListener r = new az(this);
    private Handler s = new ba(this);
    private final TextWatcher t = new bb(this);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_register_reset_via_phone_number_verification"));
        } else {
            a(m.a(this, "CS_enter_verification_code"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_register_reset_via_phone_number_verification"));
        }
        Intent intent = getIntent();
        if (intent != null) {
            this.g = intent.getStringExtra("user_phone");
            this.i = intent.getBooleanExtra("is_hottalk_account", false);
            this.j = intent.getStringExtra("country_code");
        }
        h();
        this.s.sendEmptyMessageDelayed(0, 0);
    }

    private void h() {
        this.a = (Button) findViewById(m.e(this, "btn_next"));
        this.b = (Button) findViewById(m.e(this, "btn_back"));
        this.c = (Button) findViewById(m.e(this, "btn_retrieve"));
        this.d = (EditText) findViewById(m.e(this, "verifycode_edittext"));
        this.e = (CheckBox) findViewById(m.e(this, "agree_policy"));
        if (VERSION.SDK_INT > 22) {
            i();
        } else if (d.b() && d.a(getPackageManager(), "android.permission.READ_SMS", getPackageName())) {
            s();
        }
        this.a.setOnClickListener(this.q);
        this.b.setOnClickListener(new aw(this));
        this.c.setOnClickListener(this.r);
        this.d.addTextChangedListener(this.t);
    }

    private void i() {
        if (d.b()) {
            if (checkSelfPermission("android.permission.READ_SMS") == 0) {
                s();
            } else {
                requestPermissions(new String[]{"android.permission.READ_SMS"}, 10001);
            }
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10001 && iArr != null && iArr.length > 0 && iArr[0] == 0) {
            s();
        }
    }

    private void s() {
        LinearLayout linearLayout = (LinearLayout) findViewById(m.e(this, "receive_msg"));
        TextView textView = (TextView) findViewById(m.e(this, "intro_agent"));
        this.e.setChecked(true);
        textView.setText(getString(m.a(this, "CS_read_verify_code")));
        linearLayout.setVisibility(0);
        this.e.setOnClickListener(new ax(this));
    }

    private boolean t() {
        if (this.d == null || this.d.getText() == null) {
            return false;
        }
        if (TextUtils.isEmpty(this.d.getText().toString())) {
            a.e("RegisterViaPhoneNumVerificationActivity", "the verify code is null");
            return false;
        } else if (TextUtils.isEmpty(this.d.getError())) {
            return true;
        } else {
            a.e("RegisterViaPhoneNumVerificationActivity", "error is not null");
            return false;
        }
    }

    public void g() {
        b(true);
        i.a((Context) this, new ai(this, ak.REGISTER_VERIFY, this.g, k(), this.d.getText().toString()), null, a(new bc(this, this)));
        a(null);
    }

    private void b(String str) {
        b(true);
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.m(this, str, str, NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR), null, a(new bd(this, this)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    private void u() {
        this.d.setText("");
        this.k = true;
        this.c.setText(getString(m.a(this, "CS_retrieve")));
        this.s.removeMessages(0);
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (2 == i && i2 == -1) {
            if (intent == null || !intent.getBooleanExtra("authcode_invalid", false) || this.d == null) {
                if (intent != null && intent.hasExtra("secrityPhoneOrsecrityEmail")) {
                    setResult(-1, intent);
                } else {
                    setResult(-1);
                }
                finish();
            } else {
                this.d.setError(getString(m.a(this, "CS_incorrect_verificode")));
                this.d.requestFocus();
                this.d.selectAll();
            }
        }
    }

    protected void onStart() {
        if (d.b() && this.e != null && this.e.isChecked()) {
            v();
        }
        super.onStart();
    }

    private void v() {
        if (d.a(getPackageManager(), "android.permission.READ_SMS", getPackageName())) {
            a.b("RegisterViaPhoneNumVerificationActivity", "has read sms permission, begin to register observer.");
            if (this.p != null) {
                getContentResolver().unregisterContentObserver(this.p);
            }
            this.p = new s(this, this.s);
            getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, this.p);
            return;
        }
        a.b("RegisterViaPhoneNumVerificationActivity", "don't have read sms permission");
    }

    protected void onStop() {
        if (this.p != null) {
            getContentResolver().unregisterContentObserver(this.p);
        }
        super.onStop();
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("RegisterViaPhoneNumVerificationActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
