package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.EmailInfo;
import com.huawei.hwid.core.datatype.PhoneNumInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.x;
import com.huawei.hwid.ui.common.BaseActivity;
import java.util.ArrayList;
import java.util.List;

public class FindpwdTypeActivity extends BaseActivity {
    ArrayList a;
    ArrayList b;
    private Button c;
    private Button d;
    private ListView e;
    private String f;
    private int g;
    private String h = "";

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        g();
        h();
    }

    private void g() {
        Intent intent = getIntent();
        if (intent != null) {
            this.f = intent.getStringExtra("hwid");
            this.g = intent.getIntExtra("siteId", 0);
            Bundle bundleExtra = intent.getBundleExtra("securityEmailsAndPhones");
            if (bundleExtra != null) {
                this.a = bundleExtra.getParcelableArrayList("securityEmail");
                this.b = bundleExtra.getParcelableArrayList("securityPhone");
            }
            this.h = intent.getStringExtra("requestTokenType");
            return;
        }
        a.b("FindpwdTypeActivity", "intent is null");
        finish();
    }

    private void h() {
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_reset_pwd_type"));
            TextView textView = (TextView) findViewById(m.e(this, "title_view"));
        } else {
            a(m.a(this, "CS_reset_pwd_choose_type"), m.g(this, "cs_actionbar_icon"));
            setContentView(m.d(this, "cs_reset_pwd_type"));
        }
        String[] strArr = new String[]{getString(m.a(this, "CS_input_emailaddr")), getString(m.a(this, "CS_register_reset_phone_hint"))};
        this.c = (Button) findViewById(m.e(this, "btn_next"));
        this.d = (Button) findViewById(m.e(this, "btn_back"));
        this.e = (ListView) findViewById(m.e(this, "reset_pwd_type_lostview"));
        ListAdapter arrayAdapter = new ArrayAdapter(this, m.d(this, "cs_simple_list_item_single_choice"), strArr);
        this.e.setChoiceMode(1);
        this.e.setAdapter(arrayAdapter);
        this.e.setItemChecked(0, true);
        this.c.setOnClickListener(new h(this));
        this.d.setOnClickListener(new i(this));
    }

    private void d(boolean z) {
        if (z) {
            if (this.a == null || this.a.isEmpty()) {
                if ("1".equals(d.b(this.f))) {
                    b(this.f);
                }
            } else if (1 != this.a.size()) {
                a(this.a);
            } else {
                b(((EmailInfo) this.a.get(0)).a());
            }
        } else if (this.b == null) {
        } else {
            if (1 != this.b.size()) {
                a(this.b);
            } else {
                a((PhoneNumInfo) this.b.get(0));
            }
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 0 && i2 == -1) {
            setResult(-1);
            finish();
        }
        if (2 == i && i2 == -1) {
            setResult(-1);
            finish();
        }
        if (3 == i) {
            setResult(-1);
            finish();
        }
    }

    private void b(String str) {
        i.a((Context) this, new x(this, str, this.f, this.g), null, a(new j(this, this, str)));
        a(getString(m.a(this, "CS_email_reset_pwd_submit")));
    }

    private void a(PhoneNumInfo phoneNumInfo) {
        String b = phoneNumInfo.b();
        if (b != null && b.contains("+")) {
            b = d.d(b);
        }
        i.a((Context) this, new com.huawei.hwid.core.model.http.request.m(this, this.f, b, "1"), null, a(new k(this, this, b)));
        a(getString(m.a(this, "CS_verification_requesting")));
    }

    private void a(List list) {
        Intent intent = new Intent();
        intent.setClass(this, FindpwdbyEmailActivity.class);
        intent.putExtra("hwid", this.f);
        intent.putExtra("siteId", this.g);
        ArrayList arrayList = new ArrayList();
        for (EmailInfo emailInfo : list) {
            a.b("FindpwdTypeActivity", f.a(emailInfo.toString(), true));
            arrayList.add(emailInfo.a());
        }
        intent.putExtra("securityEmails", (String[]) arrayList.toArray(new String[arrayList.size()]));
        startActivityForResult(intent, 0);
    }

    private void a(ArrayList arrayList) {
        Intent intent = new Intent();
        intent.setClass(this, FindpwdbyPhonenumberActivity.class);
        intent.putExtra("hwid", this.f);
        intent.putExtra("siteId", this.g);
        intent.putExtra("requestTokenType", this.h);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("securityPhone", arrayList);
        intent.putExtra("securityPhones", bundle);
        startActivityForResult(intent, 0);
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("FindpwdTypeActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
