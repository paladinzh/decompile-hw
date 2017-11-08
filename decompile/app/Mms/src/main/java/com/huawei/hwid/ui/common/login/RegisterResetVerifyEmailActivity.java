package com.huawei.hwid.ui.common.login;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.util.ParseMeizuManager;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.a;
import com.huawei.hwid.core.model.http.request.e;
import com.huawei.hwid.core.model.http.request.x;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.j;

public class RegisterResetVerifyEmailActivity extends LoginRegisterCommonActivity {
    private Button a;
    private Button b;
    private LinearLayout c;
    private Button d;
    private ImageView e;
    private TextView f;
    private TextView g;
    private TextView h;
    private TextView i;
    private String j = "";
    private String k = "";
    private boolean l = false;
    private boolean p = false;
    private int q = 0;
    private boolean r = true;
    private boolean s = false;
    private boolean t = false;
    private boolean u = false;
    private HwAccount v;
    private Bundle w;
    private boolean x = true;
    private long y = 0;
    private Handler z = new af(this);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!d.q(this)) {
            setRequestedOrientation(1);
        }
        if (getIntent() == null) {
            finish();
            return;
        }
        this.j = getIntent().getStringExtra("accountName");
        this.k = getIntent().getStringExtra("emailName");
        this.r = getIntent().getBooleanExtra("isFromRegister", true);
        this.p = getIntent().getBooleanExtra("commonloginsuccess", false);
        this.l = getIntent().getBooleanExtra("weixinloginsuccess", false);
        this.s = getIntent().getBooleanExtra("isFromOther", false);
        this.t = getIntent().getBooleanExtra("third_is_weixin_login", false);
        this.q = getIntent().getIntExtra("siteId", 0);
        this.n = getIntent().getBooleanExtra("needActivateVip", false);
        this.u = getIntent().getBooleanExtra("isEmotionIntroduce", false);
        this.v = (HwAccount) getIntent().getParcelableExtra("account");
        this.w = getIntent().getBundleExtra("bundle");
        if (TextUtils.isEmpty(this.j) && TextUtils.isEmpty(this.k)) {
            finish();
            return;
        }
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_register_reset_verify_email"));
            this.i = (TextView) findViewById(m.e(this, "title_view"));
            if (this.r || this.s) {
                this.i.setText(m.a(this, "CS_register_verify_emailaddr"));
            } else {
                this.i.setText(m.a(this, "CS_reset_verify_email_new"));
            }
        } else {
            if (!this.r && !this.s) {
                a(m.a(this, "CS_reset_verify_email_new"), m.g(this, "cs_actionbar_icon"));
                f.a(this).d(this, this.j);
            } else if (this.r) {
                a(m.a(this, "CS_register_verify_emailaddr"), m.g(this, "cs_actionbar_icon"));
            } else {
                a(m.a(this, "CS_from_others_verify_emailaddr"), m.g(this, "cs_actionbar_icon"));
            }
            setContentView(m.d(this, "cs_register_reset_bind_verify_email"));
        }
        g();
    }

    private void g() {
        this.c = (LinearLayout) findViewById(m.e(this, "bottomLinearLayout"));
        this.a = (Button) findViewById(m.e(this, "Btn_submit"));
        this.b = (Button) findViewById(m.e(this, "Btn_send"));
        this.d = (Button) findViewById(m.e(this, "Btn_resendEmail"));
        this.e = (ImageView) findViewById(m.e(this, "bind_image"));
        this.f = (TextView) findViewById(m.e(this, "register_name"));
        this.g = (TextView) findViewById(m.e(this, "show_view1"));
        this.h = (TextView) findViewById(m.e(this, "show_view2"));
        if (this.r || this.s) {
            if (d.q(this) && getResources().getConfiguration().orientation == 2) {
                ((LinearLayout) findViewById(m.e(this, "bottomLinearLayout"))).setOrientation(1);
                this.a.setLayoutParams(new LayoutParams(getResources().getDimensionPixelOffset(m.h(this, "cs_button_width")), getResources().getDimensionPixelOffset(m.h(this, "cs_button_height"))));
            }
            this.d.setVisibility(0);
            this.b.setVisibility(0);
            this.g.setText(m.a(this, "CS_register_verify_email_show1"));
            this.h.setText(m.a(this, "CS_register_verify_emailaddr_show2"));
            this.c.setOrientation(0);
            ViewGroup.LayoutParams layoutParams = new LayoutParams(0, -1);
            layoutParams.weight = ContentUtil.FONT_SIZE_NORMAL;
            this.a.setLayoutParams(layoutParams);
            this.b.setLayoutParams(layoutParams);
            if (this.r) {
                this.b.setText(m.a(this, "CS_register_verify_email_later"));
                this.a.setText(m.a(this, "CS_next"));
            } else {
                this.b.setText(m.a(this, "CS_quit_hwid"));
                this.a.setText(m.a(this, "CS_has_verified_email"));
            }
            this.y = System.currentTimeMillis();
            this.z.sendEmptyMessageDelayed(2, 0);
            this.d.setOnClickListener(new ag(this));
            this.b.setOnClickListener(new ah(this));
        } else {
            this.d.setVisibility(8);
            this.g.setText(m.a(this, "CS_reset_verify_email_show1_new"));
            this.h.setText(m.a(this, "CS_reset_verify_email_show2_new"));
            this.b.setVisibility(0);
            this.y = System.currentTimeMillis();
            this.z.sendEmptyMessageDelayed(2, 0);
            this.b.setOnClickListener(new ai(this));
            this.c.setOrientation(1);
        }
        this.f.setText(this.k);
        this.a.setOnClickListener(new aj(this));
    }

    private void h() {
        i.a((Context) this, new x(this, this.k, this.j, this.q), null, a(new aq(this, this)));
        a(getString(m.a(this, "CS_email_reset_pwd_submit")));
    }

    public void onBackPressed() {
        if (this.r) {
            e(true);
        } else {
            setResult(-1);
        }
        super.onBackPressed();
    }

    public void d(boolean z) {
        if (this.r) {
            e(z);
        } else {
            setResult(-1);
        }
        super.onBackPressed();
    }

    private void e(boolean z) {
        if (this.u) {
            a(z, new Intent().putExtra("register_by_email", true));
        } else if (d.l(this) && d.j((Context) this)) {
            a(z, new Intent().putExtra("bundle", this.w));
        } else {
            Intent intent = new Intent();
            intent.putExtra("hwaccount", this.v);
            if (l()) {
                a(z, intent);
            } else if (com.huawei.hwid.ui.common.f.FromApp != q()) {
                intent.putExtra("completed", true);
                intent.setFlags(67108864);
                intent.setClass(this, LoginActivity.class);
                startActivity(intent);
            } else {
                intent.setAction("com.huawei.cloudserive.loginSuccess");
                if (this.v != null) {
                    intent.putExtra("bundle", this.v.k());
                }
                a(z, intent);
            }
        }
    }

    protected synchronized void onActivityResult(int i, int i2, Intent intent) {
        if (1235 != i) {
            setResult(-1);
            finish();
            super.onActivityResult(i, i2, intent);
        } else {
            onBackPressed();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setContentView(m.d(this, "cs_register_reset_bind_verify_email"));
        g();
    }

    private void a(Bundle bundle) {
        String stringExtra = getIntent().getStringExtra("third_openid");
        String stringExtra2 = getIntent().getStringExtra("third_access_token");
        String string = bundle.getString("userId");
        String string2 = bundle.getString("userName");
        i.a((Context) this, new a(this, ParseMeizuManager.SMS_FLOW_THREE, string2, stringExtra, stringExtra2, string), string2, a(new al(this, this, bundle)));
        a(getString(m.a(this, "CS_logining_message")));
    }

    protected void r() {
    }

    private void f(String str) {
        com.huawei.hwid.core.model.http.a eVar = new e(this, str, str, new Bundle());
        eVar.b(70001104);
        i.a((Context) this, eVar, str, a(new am(this, this, eVar)));
        a("");
    }

    public void b(String str) {
        View inflate;
        com.huawei.hwid.core.c.b.a.b("RegisterResetVerifyEmailActivity", "showNotVeryfiedDialog");
        if (d.r(this)) {
            inflate = View.inflate(this, m.d(this, "cs_verify_email_account_dialog_3"), null);
        } else {
            inflate = View.inflate(this, m.d(this, "cs_verify_email_account_dialog"), null);
        }
        Dialog create = new Builder(this, j.b(this)).setMessage("").setView(inflate).setPositiveButton(m.a(this, "CS_i_known"), null).create();
        a(create);
        create.show();
    }

    public void c(String str) {
        com.huawei.hwid.core.c.b.a.b("RegisterResetVerifyEmailActivity", "showVeryfyLaterDialog");
        Dialog create = new Builder(this, j.b(this)).setMessage(str).setPositiveButton(17039370, new ak(this)).setNegativeButton(17039360, null).create();
        a(create);
        create.show();
    }

    private void a(HwAccount hwAccount, Bundle bundle) {
        b();
        a(hwAccount);
        if (!d.l(this) || !d.j((Context) this)) {
            Intent intent = new Intent();
            intent.putExtra("hwaccount", hwAccount);
            intent.putExtra("isUseSDK", true);
            if (l() || com.huawei.hwid.ui.common.f.FromApp == q()) {
                com.huawei.hwid.core.c.b.a.b("RegisterResetVerifyEmailActivity", "start APK by old way, set result to StartUpGuideLoginActivity or AccountManagerActivity");
                a(true, intent);
            } else {
                intent.setPackage(getPackageName());
                com.huawei.hwid.core.c.e.a(this, intent);
                finish();
            }
        } else if (o()) {
            a(bundle);
        } else {
            a(true, new Intent().putExtra("bundle", bundle));
        }
    }
}
