package com.huawei.hwid.ui.common.password;

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
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.s;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.ai;
import com.huawei.hwid.core.model.http.request.ak;
import com.huawei.hwid.ui.common.BaseActivity;

public class ResetPwdByPhoneNumberVerificationActivity extends BaseActivity {
    private Button a;
    private Button b;
    private Button c;
    private EditText d;
    private CheckBox e;
    private String f = "";
    private String g = "";
    private final int h = 2;
    private long i = System.currentTimeMillis();
    private String j;
    private int k = 0;
    private boolean l = false;
    private String m = "";
    private s n;
    private Handler o = new y(this);
    private OnClickListener p = new z(this);
    private OnClickListener q = new aa(this);
    private final TextWatcher r = new ab(this);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_register_reset_via_phone_number_verification"));
        } else {
            a(m.a(this, "CS_enter_verification_code"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_register_reset_via_phone_number_verification"));
        }
        h();
    }

    private void h() {
        if (getIntent() != null) {
            this.o.sendEmptyMessageDelayed(2, 0);
            Intent intent = getIntent();
            this.g = intent.getStringExtra("requestTokenType");
            this.f = intent.getStringExtra("phoneNumber");
            this.j = intent.getStringExtra("hwid");
            this.k = intent.getIntExtra("siteId", 0);
            this.d = (EditText) findViewById(m.e(this, "verifycode_edittext"));
            this.c = (Button) findViewById(m.e(this, "btn_retrieve"));
            this.c.setOnClickListener(this.p);
            this.a = (Button) findViewById(m.e(this, "btn_next"));
            this.b = (Button) findViewById(m.e(this, "btn_back"));
            this.a.setOnClickListener(this.q);
            this.b.setOnClickListener(new w(this));
            this.e = (CheckBox) findViewById(m.e(this, "agree_policy"));
            if (VERSION.SDK_INT > 22) {
                i();
            } else if (d.b() && d.a(getPackageManager(), "android.permission.READ_SMS", getPackageName())) {
                j();
            }
            this.d.addTextChangedListener(this.r);
            return;
        }
        a.b("ResetPwdByPhoneNumberVerificationActivity", "intent is null");
        finish();
    }

    private void i() {
        if (d.b()) {
            if (checkSelfPermission("android.permission.READ_SMS") == 0) {
                j();
            } else {
                requestPermissions(new String[]{"android.permission.READ_SMS"}, 10001);
            }
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10001 && iArr != null && iArr.length > 0 && iArr[0] == 0) {
            j();
        }
    }

    private void j() {
        LinearLayout linearLayout = (LinearLayout) findViewById(m.e(this, "receive_msg"));
        TextView textView = (TextView) findViewById(m.e(this, "intro_agent"));
        this.e.setChecked(true);
        textView.setText(getString(m.a(this, "CS_read_verify_code")));
        linearLayout.setVisibility(0);
        this.e.setOnClickListener(new x(this));
    }

    private void k() {
        com.huawei.hwid.core.model.http.a mVar = new com.huawei.hwid.core.model.http.request.m(this, this.j, this.f, "1");
        i.a((Context) this, mVar, null, a(new ad(this, this, mVar)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    private boolean l() {
        if (this.d == null || this.d.getText() == null) {
            return false;
        }
        if (TextUtils.isEmpty(this.d.getText().toString())) {
            a.e("ResetPwdByPhoneNumberVerificationActivity", "the verify code is null");
            return false;
        } else if (TextUtils.isEmpty(this.d.getError())) {
            return true;
        } else {
            a.e("ResetPwdByPhoneNumberVerificationActivity", "error is not null");
            return false;
        }
    }

    public void g() {
        b(true);
        i.a((Context) this, new ai(this, ak.FINDPASSWORD_VERIFY, this.j, this.g, this.d.getText().toString()), null, a(new ac(this, this)));
        a(null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (2 == i && i2 == -1) {
            if (intent != null) {
                if (intent.getBooleanExtra("authcode_invalid", false) && this.d != null) {
                    this.d.setError(getString(m.a(this, "CS_incorrect_verificode")));
                    this.d.requestFocus();
                    this.d.selectAll();
                    return;
                }
            }
            setResult(-1);
            finish();
        }
    }

    protected void onStart() {
        if (d.b() && this.e != null && this.e.isChecked()) {
            m();
        }
        super.onStart();
    }

    private void m() {
        if (d.a(getPackageManager(), "android.permission.READ_SMS", getPackageName())) {
            a.b("ResetPwdByPhoneNumberVerificationActivity", "has read sms permission, begin to register observer.");
            if (this.n != null) {
                getContentResolver().unregisterContentObserver(this.n);
            }
            this.n = new s(this, this.o);
            getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, this.n);
            return;
        }
        a.b("ResetPwdByPhoneNumberVerificationActivity", "don't have read sms permission");
    }

    protected void onStop() {
        if (this.n != null) {
            getContentResolver().unregisterContentObserver(this.n);
        }
        super.onStop();
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("ResetPwdByPhoneNumberVerificationActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
