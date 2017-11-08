package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.EmailInfo;
import com.huawei.hwid.core.datatype.PhoneNumInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.x;
import com.huawei.hwid.ui.common.BaseActivity;
import java.util.ArrayList;
import java.util.List;

public class FindpwdByHwIdActivity extends BaseActivity {
    private EditText a = null;
    private Button b = null;
    private Button c = null;
    private String d = "";
    private String e = "";
    private int f;
    private int g = 0;
    private final TextWatcher h = new b(this);
    private OnClickListener i = new c(this);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!d.q(this)) {
            setRequestedOrientation(1);
        }
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_reset_pwd_step1"));
        } else {
            a(m.a(this, "CS_reset_pwd_label"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_reset_pwd_step1"));
        }
        g();
    }

    private void g() {
        this.a = (EditText) findViewById(m.e(this, "edit_hwid"));
        this.a.addTextChangedListener(this.h);
        this.b = (Button) findViewById(m.e(this, "btn_next"));
        this.c = (Button) findViewById(m.e(this, "btn_back"));
        this.b.setOnClickListener(this.i);
        this.c.setOnClickListener(new a(this));
        Intent intent = getIntent();
        if (intent != null) {
            Object stringExtra = intent.getStringExtra("userAccount");
            if (!(stringExtra == null || TextUtils.isEmpty(stringExtra))) {
                this.a.setText(stringExtra);
                this.a.setSelection(stringExtra.length());
            }
            this.d = getIntent().getStringExtra("requestTokenType");
            if (TextUtils.isEmpty(this.d)) {
                this.d = "cloud";
            }
        }
    }

    private boolean h() {
        if (this.a == null || TextUtils.isEmpty(this.a.getText())) {
            return false;
        }
        if (!p.b(this.a.getText().toString())) {
            this.a.setError(getString(m.a(this, "CS_login_username_error")));
            a.e("FindpwdByHwIdActivity", "the phone number is has error");
            return false;
        } else if (TextUtils.isEmpty(this.a.getError())) {
            return true;
        } else {
            a.e("FindpwdByHwIdActivity", "the username has error");
            return false;
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        Intent intent2;
        super.onActivityResult(i, i2, intent);
        if (i == 0 && i2 == -1) {
            intent2 = new Intent();
            if (!TextUtils.isEmpty(this.e)) {
                intent2.putExtra("accountName", this.e);
            }
            setResult(-1, intent2);
            finish();
        }
        if (2 == i && i2 == -1) {
            intent2 = new Intent();
            if (!TextUtils.isEmpty(this.e)) {
                intent2.putExtra("accountName", this.e);
            }
            setResult(-1, intent2);
            finish();
        }
        if (4 == i) {
            intent2 = new Intent();
            if (!TextUtils.isEmpty(this.e)) {
                intent2.putExtra("accountName", this.e);
            }
            setResult(-1, intent2);
            finish();
        }
    }

    private void a(Bundle bundle) {
        this.f = bundle.getInt("siteID");
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.p(this, this.e, this.f), this.e, a(new e(this, this)));
    }

    private void a(ArrayList arrayList, ArrayList arrayList2) {
        Intent intent = new Intent();
        intent.setClass(this, FindpwdTypeActivity.class);
        intent.putExtra("hwid", this.e);
        intent.putExtra("siteId", this.f);
        intent.putExtra("requestTokenType", this.d);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("securityEmail", arrayList);
        bundle.putParcelableArrayList("securityPhone", arrayList2);
        intent.putExtra("securityEmailsAndPhones", bundle);
        startActivityForResult(intent, 2);
    }

    private void a(List list) {
        Intent intent = new Intent();
        intent.setClass(this, FindpwdbyEmailActivity.class);
        intent.putExtra("hwid", this.e);
        intent.putExtra("siteId", this.f);
        ArrayList arrayList = new ArrayList();
        for (EmailInfo emailInfo : list) {
            a.b("FindpwdByHwIdActivity", f.a(emailInfo.toString(), true));
            arrayList.add(emailInfo.a());
        }
        intent.putExtra("securityEmails", (String[]) arrayList.toArray(new String[arrayList.size()]));
        startActivityForResult(intent, 0);
    }

    private void a(ArrayList arrayList) {
        Intent intent = new Intent();
        intent.setClass(this, FindpwdbyPhonenumberActivity.class);
        intent.putExtra("hwid", this.e);
        intent.putExtra("siteId", this.f);
        intent.putExtra("requestTokenType", this.d);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("securityPhone", arrayList);
        intent.putExtra("securityPhones", bundle);
        startActivityForResult(intent, 0);
    }

    private void b(String str) {
        a.b("FindpwdByHwIdActivity", "sendEmail===");
        i.a((Context) this, new x(this, str, this.e, this.f), null, a(new f(this, this, str)));
        a(getString(m.a(this, "CS_email_reset_pwd_submit")));
    }

    private void a(PhoneNumInfo phoneNumInfo) {
        String b = phoneNumInfo.b();
        if (b != null && b.contains("+")) {
            b = d.d(b);
        }
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.m(this, this.e, b, "1"), null, a(new g(this, this, b)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("FindpwdByHwIdActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
