package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.x;
import com.huawei.hwid.ui.common.BaseActivity;

public class FindpwdbyEmailActivity extends BaseActivity {
    private Button a;
    private Button b;
    private Button c;
    private String[] d = null;
    private String e;
    private int f = 0;
    private OnClickListener g = new o(this);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        if (intent != null) {
            this.e = intent.getStringExtra("hwid");
            this.f = intent.getIntExtra("siteId", 0);
            this.d = intent.getStringArrayExtra("securityEmails");
        }
        g();
    }

    private void g() {
        if (this.d != null) {
            if (f()) {
                requestWindowFeature(1);
                setContentView(m.d(this, "oobe_findpwd_email"));
            } else {
                a(m.a(this, "CS_reset_pwd_label"), m.g(this, "cs_actionbar_icon"));
                setContentView(m.d(this, "cs_findpwd_email"));
            }
            this.c = (Button) findViewById(m.e(this, "countryregion_btn"));
            this.a = (Button) findViewById(m.e(this, "btn_next"));
            this.b = (Button) findViewById(m.e(this, "btn_back"));
            this.a.setOnClickListener(this.g);
            this.b.setOnClickListener(new l(this));
            if (this.d != null && this.d.length > 0) {
                this.c.setText(this.d[0]);
            }
            this.c.setOnClickListener(new m(this));
        }
    }

    private void b(String str) {
        i.a((Context) this, new x(this, str, this.e, this.f), null, a(new p(this, this, str)));
        a(getString(m.a(this, "CS_email_reset_pwd_submit")));
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (1 == i) {
            setResult(-1);
        }
        finish();
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("FindpwdbyEmailActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
