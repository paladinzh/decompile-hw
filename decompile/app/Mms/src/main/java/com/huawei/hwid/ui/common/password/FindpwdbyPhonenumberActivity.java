package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.PhoneNumInfo;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.j;
import java.util.ArrayList;
import java.util.List;

public class FindpwdbyPhonenumberActivity extends BaseActivity {
    private Button a = null;
    private Button b = null;
    private Button c;
    private String d;
    private int e = 0;
    private ArrayList f = null;
    private int g = 0;
    private String h = "";
    private OnClickListener i = new t(this);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        g();
        h();
    }

    private void g() {
        Intent intent = getIntent();
        if (intent != null) {
            this.h = intent.getStringExtra("requestTokenType");
            String stringExtra = intent.getStringExtra("hwid");
            if ("2".equals(d.b(stringExtra))) {
                this.d = new PhoneNumInfo(this, d.d(stringExtra), null).b();
            }
            if (TextUtils.isEmpty(this.d)) {
                this.d = stringExtra;
            }
            this.e = intent.getIntExtra("siteId", 0);
            Bundle bundleExtra = intent.getBundleExtra("securityPhones");
            this.f = new ArrayList();
            if (bundleExtra != null) {
                int i;
                List parcelableArrayList = bundleExtra.getParcelableArrayList("securityPhone");
                if (parcelableArrayList == null) {
                    i = 0;
                } else {
                    i = parcelableArrayList.size();
                }
                for (int i2 = 0; i2 < i; i2++) {
                    Object b = ((PhoneNumInfo) parcelableArrayList.get(i2)).b();
                    String a = ((PhoneNumInfo) parcelableArrayList.get(i2)).a();
                    if (!(TextUtils.isEmpty(b) || this.d.equals(b))) {
                        this.f.add(new StringBuffer(d.c(a)).append(b).toString());
                    }
                }
            }
        }
    }

    private void h() {
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_findpwd_phonenumber"));
        } else {
            a(m.a(this, "CS_reset_pwd_label"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_findpwd_phonenumber"));
        }
        this.c = (Button) findViewById(m.e(this, "countryregion_btn"));
        if (!(this.f == null || this.f.isEmpty())) {
            this.c.setText((CharSequence) this.f.get(0));
        }
        this.c.setOnClickListener(new q(this));
        this.a = (Button) findViewById(m.e(this, "btn_next"));
        this.b = (Button) findViewById(m.e(this, "btn_back"));
        this.a.setOnClickListener(this.i);
        this.b.setOnClickListener(new s(this));
    }

    private void b(String str) {
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.m(this, this.d, str, "1"), null, a(new u(this, this)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (2 == i && i2 == -1) {
            setResult(-1);
            finish();
        }
        if (2 == i && i2 == 1) {
            String stringExtra = intent.getStringExtra("error_prompt");
            if (!TextUtils.isEmpty(stringExtra)) {
                j.a((Context) this, stringExtra, 1);
            }
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("FindpwdbyPhonenumberActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
