package com.huawei.hwid.ui.common.setting;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.ag;

public class ModifyPasswordActivity extends ModifyPasswdBaseActivity {
    private String d;
    private String e;

    public void onCreate(Bundle bundle) {
        if (getIntent() != null) {
            this.e = getIntent().getStringExtra("OldPassWord");
            if (!getIntent().getBooleanExtra("isFromApk", true)) {
                this.a = getIntent().getStringExtra("accountName");
            }
        }
        if (TextUtils.isEmpty(this.a)) {
            this.a = d();
        }
        a(m.a(this, "CS_set_pwd_title"), m.g(this, "cs_actionbar_icon"));
        super.onCreate(bundle);
        if (TextUtils.isEmpty(this.e) || TextUtils.isEmpty(this.a)) {
            finish();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    public void g() {
        Intent intent = new Intent();
        intent.putExtra("password", e.d(getBaseContext(), this.d));
        setResult(-1, intent);
        finish();
    }

    protected void b(String str) {
        Bundle bundle = new Bundle();
        this.d = str;
        a agVar = new ag(this, this.a, this.e, str, bundle);
        i.a((Context) this, agVar, this.a, a(new g(this, this, agVar)));
        a(null);
    }

    protected boolean b(EditText editText, EditText editText2) {
        boolean b = super.b(editText, editText2);
        String obj = editText.getText().toString();
        String e = e.e(this, this.e);
        if (!b || editText.length() < 8 || editText.length() < 8 || !obj.equals(e)) {
            return b;
        }
        editText2.setText("");
        editText2.setError(null);
        editText.setText("");
        editText.requestFocus();
        editText.setError(getString(m.a(this, "CS_new_pwd_invalid")));
        com.huawei.hwid.core.c.b.a.b("ModifyPasswordActivity", "new password is same as old password");
        return false;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
    }

    protected void a(EditText editText, EditText editText2) {
        editText.setHint(m.a(this, "CS_new_pwd"));
        editText2.setHint(m.a(this, "CS_make_sure_pwd"));
    }
}
