package com.huawei.hwid.ui.common.password;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.EditText;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.y;
import com.huawei.hwid.ui.common.setting.ModifyPasswdBaseActivity;

public class ResetPwdByPhoneNumberActivity extends ModifyPasswdBaseActivity {
    private String d;
    private int e = 0;

    protected void onCreate(Bundle bundle) {
        g();
        if (!f()) {
            a(m.a(this, "CS_reset_pwd_label"), m.g(this, "cs_actionbar_icon"));
        }
        super.onCreate(bundle);
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void g() {
        Intent intent = getIntent();
        this.a = intent.getStringExtra("hwid");
        this.e = intent.getIntExtra("siteId", 0);
        this.d = intent.getStringExtra("verifycode");
    }

    protected void b(String str) {
        String str2 = str;
        a yVar = new y(this, this.a, str2, this.d, this.e, new Bundle());
        i.a((Context) this, yVar, null, a(new v(this, this, yVar)));
        a(null);
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("ResetPwdByPhoneNumberActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    protected void a(EditText editText, EditText editText2) {
        editText.setHint(m.a(this, "CS_old_pwd"));
        editText2.setHint(m.a(this, "CS_make_sure_passwd"));
    }
}
