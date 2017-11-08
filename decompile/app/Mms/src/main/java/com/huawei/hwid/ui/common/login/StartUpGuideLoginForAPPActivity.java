package com.huawei.hwid.ui.common.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.e;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.f;
import com.huawei.hwid.ui.common.j;

public class StartUpGuideLoginForAPPActivity extends BaseActivity {
    private Button a = null;
    private Button b = null;
    private String c = "";
    private boolean d = false;
    private AlertDialog e = null;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!d.q(this)) {
            setRequestedOrientation(1);
        }
        this.c = getIntent().getStringExtra("requestTokenType");
        c(true);
        a.b("StartUpGuideLoginForAPPActivity", "startActivityWay is: " + f.FromApp);
        a(m.a(this, "CS_app_name"), m.g(this, "cs_account_icon"));
        setContentView(m.d(this, "cs_welcome_view_for_app"));
        h();
        i();
        if (VERSION.SDK_INT > 22) {
            g();
        }
    }

    private void g() {
        if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            q.c((Context) this);
            q.a((Context) this);
            q.e(this);
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10003) {
            if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
                q.c((Context) this);
                q.a((Context) this);
                q.e(this);
                return;
            }
            b(getString(m.a(this, "CS_read_phone_state_permission")));
        }
    }

    public void b(String str) {
        if (TextUtils.isEmpty(str)) {
            a.d("StartUpGuideLoginForAPPActivity", "PermissionName is null!");
            Intent intent = new Intent();
            intent.putExtra("isUseSDK", true);
            intent.putExtra("completed", false);
            intent.setPackage(getPackageName());
            a(intent);
            finish();
            return;
        }
        this.e = d.d(this, str).setNegativeButton(17039360, new cm(this)).setPositiveButton(m.a(this, "CS_go_settings"), new cl(this)).create();
        this.e.setCancelable(false);
        this.e.setCanceledOnTouchOutside(false);
        a(this.e);
        this.e.setOnDismissListener(new cn(this));
        if (!isFinishing()) {
            this.e.show();
        }
    }

    private void h() {
        TextView textView = (TextView) findViewById(m.e(this, "welcome_textview_value"));
        TextView textView2 = (TextView) findViewById(m.e(this, "welcome_textview_continue_use"));
        if (d.h()) {
            textView2.setText(getString(m.a(this, "CS_welcome_view_continue_use")).trim());
            textView.setText(getString(m.a(this, "CS_welcome_view_inner_common"), (Object[]) new String[]{getString(m.a(this, "CS_welcome_view_start")), getString(m.a(this, "CS_more_service")), getString(m.a(this, "CS_welcome_view_end"))}));
            j.a(textView, getString(m.a(this, "CS_more_service")), new co(this, this, textView));
            return;
        }
        textView2.setText(getString(m.a(this, "CS_welcome_view_continue_use")).trim());
        textView.setText(getString(m.a(this, "CS_welcome_view_universal"), (Object[]) new String[]{getString(m.a(this, "CS_welcome_view_start")), getString(m.a(this, "CS_welcome_view_end"))}));
    }

    private void i() {
        try {
            this.a = (Button) findViewById(m.e(this, "btn_login"));
            this.a.setOnClickListener(new cp(this));
            this.b = (Button) findViewById(m.e(this, "btn_register"));
            this.b.setOnClickListener(new cq(this));
        } catch (Throwable e) {
            a.d("StartUpGuideLoginForAPPActivity", e.toString(), e);
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        a.a("StartUpGuideLoginForAPPActivity", "enter onActivityResult(requestCode:" + i + " resultCode:" + i2 + " data:" + com.huawei.hwid.core.encrypt.f.a(intent));
        super.onActivityResult(i, i2, intent);
        cs csVar = cs.RequestCode_Default;
        if (i >= 0) {
            if (i < cs.values().length) {
                csVar = cs.values()[i];
            }
        }
        switch (cr.a[csVar.ordinal()]) {
            case 1:
            case 2:
                a.a("StartUpGuideLoginForAPPActivity", "" + csVar + " Return, resultCode:" + i2 + "requestCode:" + i);
                if (-1 == i2) {
                    a(intent);
                    finish();
                    break;
                }
                break;
            case 3:
                if (-1 == i2) {
                    j.a((Context) this, cs.RequestCode_RegistActivity.ordinal(), f.FromApp, true, this.c, new Bundle());
                    break;
                }
                break;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        a.a("StartUpGuideLoginForAPPActivity", "enter onNewIntent:" + com.huawei.hwid.core.encrypt.f.a(intent));
        a(intent);
        finish();
    }

    private void a(Intent intent) {
        Intent intent2 = new Intent();
        intent2.setPackage(this.c);
        if (intent.getBooleanExtra("completed", false)) {
            intent2.putExtra("isUseSDK", true);
            intent2.putExtra("isChangeAccount", false);
            intent2.putExtra("currAccount", intent.getStringExtra("accountName"));
            intent2.putExtras(intent);
            intent2.putExtra("bundle", j.a(intent.getBundleExtra("bundle"), this.c));
            e.a(this, intent2);
            return;
        }
        intent2.putExtra("isUseSDK", false);
        Bundle bundle = new Bundle();
        CharSequence e = com.huawei.hwid.a.a().e();
        com.huawei.hwid.a.a().b(null);
        if (TextUtils.isEmpty(e)) {
            bundle.putInt("errorcode", 3002);
            intent2.putExtra("bundle", bundle);
            e.b(this, intent2);
            return;
        }
        intent2.putExtra("parce", new ErrorStatus(23, "AreaNotAllowError: Area is not allowed"));
        e.c(this, intent2);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        a.a("StartUpGuideLoginForAPPActivity", "onResume");
        if (this.d) {
            this.d = false;
            if (VERSION.SDK_INT <= 22) {
                return;
            }
            if (checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                q.c((Context) this);
                q.a((Context) this);
                q.e(this);
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("isUseSDK", true);
            intent.putExtra("completed", false);
            intent.setPackage(getPackageName());
            a(intent);
            finish();
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("isUseSDK", true);
        intent.putExtra("completed", false);
        intent.setPackage(getPackageName());
        a(intent);
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("StartUpGuideLoginForAPPActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
