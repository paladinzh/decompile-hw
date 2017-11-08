package com.huawei.hwid.ui.common.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.simchange.b.b;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.j;
import com.huawei.hwid.ui.common.login.a.o;
import com.huawei.hwid.ui.common.password.FindpwdByHwIdActivity;

public class CheckUidPwdActivity extends BaseActivity implements o {
    private String a;
    private String b;
    private String c;
    private String d;
    private int e;
    private int f = 3;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        Intent intent = getIntent();
        if (intent != null) {
            this.a = intent.getStringExtra("userId");
            this.c = intent.getStringExtra("requestTokenType");
            this.d = intent.getStringExtra("accountType");
            this.f = intent.getIntExtra("startway", 3);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.e = extras.getInt("reqClientType", 7);
            }
            a.b("CheckUidPwdActivity", "mStartWay is:" + this.f + ",userid is:" + f.a(this.a) + ",reqClientType is:" + this.e + ", requestTokenType:" + this.c);
            if (TextUtils.isEmpty(this.a)) {
                i();
                return;
            }
            HwAccount c = d.c(this, this.a);
            if (c != null) {
                this.b = c.a();
            }
            if (TextUtils.isEmpty(this.b)) {
                i();
                return;
            }
            if (d.f(this.d)) {
                j.a((Activity) this, true, "CheckUidPwdActivity");
            } else {
                j.a((Activity) this, this.b, this.a, this.c, this.e, "CheckUidPwdActivity");
            }
            return;
        }
        a.b("CheckUidPwdActivity", "init, intent is null");
        i();
    }

    private void i() {
        a.b("CheckUidPwdActivity", "doCancel, mStartWay is:" + this.f);
        if (3 != this.f) {
            setResult(0);
        } else {
            j();
        }
        finish();
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        a.b("CheckUidPwdActivity", "requestCode: " + i + " resultCode: " + i2 + " data: " + f.a(intent));
        if (1 == i && i2 == -1) {
            com.huawei.hwid.ui.common.b.a.a().b();
        }
    }

    public void onBackPressed() {
        a.b("CheckUidPwdActivity", "onBackPressed");
        i();
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("CheckUidPwdActivity", "catch Exception", e);
        }
    }

    public void g() {
        a.b("CheckUidPwdActivity", "onCancel");
        i();
    }

    public void a(Bundle bundle) {
        b(bundle);
    }

    public void h() {
        a.b("CheckUidPwdActivity", "onForgetPwd");
        Intent intent = new Intent();
        intent.putExtra("userAccount", this.b);
        intent.setClass(this, FindpwdByHwIdActivity.class);
        startActivityForResult(intent, 1);
    }

    private void b(Bundle bundle) {
        a.b("CheckUidPwdActivity", "doVerifySuccess, mStartWay is:" + this.f + ",bundle is:" + f.a(bundle));
        if (bundle != null) {
            Intent intent;
            switch (this.f) {
                case 1:
                case 2:
                case 4:
                    intent = new Intent();
                    intent.putExtras(c(bundle));
                    setResult(-1, intent);
                    finish();
                    break;
                case 3:
                    intent = new Intent();
                    intent.setPackage(this.c);
                    intent.putExtra("bundle", c(bundle));
                    e.e(this, intent);
                    finish();
                    break;
                case 5:
                    b.c(this, this.b, "normal");
                    com.huawei.hwid.simchange.b.a.b(this, this.a);
                    d.b((Context) this, 3001);
                    intent = new Intent();
                    intent.putExtras(c(bundle));
                    setResult(-1, intent);
                    finish();
                    break;
                default:
                    finish();
                    break;
            }
            return;
        }
        i();
    }

    private Bundle c(Bundle bundle) {
        Bundle bundle2 = new Bundle();
        bundle2.putString("verifyType", "verifyPwd");
        String string = bundle.getString("serviceToken");
        bundle2.putString("serviceToken", string);
        bundle2.putString("tempST", string);
        bundle2.putString("password", bundle.getString("password"));
        return bundle2;
    }

    private void j() {
        Intent intent = new Intent();
        intent.setPackage(this.c);
        e.d(this, intent);
    }
}
