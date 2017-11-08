package com.huawei.hwid.ui.common.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.constants.HwAccountConstants;
import com.huawei.hwid.core.datatype.Agreement;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.af;
import com.huawei.hwid.core.model.http.request.f;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.j;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageAgreementActivity extends BaseActivity {
    OnClickListener a = new p(this);
    private TextView b;
    private TextView c;
    private TextView d;
    private TextView e;
    private LinearLayout f;
    private LinearLayout g;
    private LinearLayout h;
    private LinearLayout i;
    private CheckBox j;
    private CheckBox k;
    private Button l;
    private LinearLayout m;
    private LinearLayout n;
    private ImageView o;
    private ImageView p;
    private String q = NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR;
    private boolean r = false;
    private String s;
    private String t;
    private String u;
    private AlertDialog v;
    private String w;
    private List x = new ArrayList();

    protected void onCreate(Bundle bundle) {
        String str;
        super.onCreate(bundle);
        a.b("ManageAgreementActivity", "enter ManageAgreementActivity onCreate");
        if (!d.q(this)) {
            setRequestedOrientation(1);
        }
        if (f()) {
            requestWindowFeature(1);
            str = !com.huawei.hwid.a.a.a((Context) this) ? "oobe_cs_manage_agreement" : "oobe_social_manage_agreement";
        } else {
            str = !com.huawei.hwid.a.a.a((Context) this) ? "cs_manage_agreement" : "social_manage_agreement";
        }
        setContentView(m.d(this, str));
        this.r = getIntent().getBooleanExtra("isEmotionIntroduce", false);
        this.q = getIntent().getStringExtra("typeEnterAgree");
        if (TextUtils.isEmpty(this.q)) {
            this.q = NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR;
        }
        this.w = getIntent().getStringExtra("topActivity");
        g();
    }

    private void g() {
        int i = 0;
        this.b = (TextView) findViewById(m.e(this, "user_terms_detail"));
        this.c = (TextView) findViewById(m.e(this, "privacy_policy_detail"));
        this.d = (TextView) findViewById(m.e(this, "privacy_policy_signing_time"));
        this.e = (TextView) findViewById(m.e(this, "user_terms_signing_time"));
        this.f = (LinearLayout) findViewById(m.e(this, "privacy_policy_agree"));
        this.g = (LinearLayout) findViewById(m.e(this, "user_terms_agree"));
        this.h = (LinearLayout) findViewById(m.e(this, "privacy_layout"));
        this.i = (LinearLayout) findViewById(m.e(this, "user_term_layout"));
        this.j = (CheckBox) findViewById(m.e(this, "privacy_policy_check"));
        this.k = (CheckBox) findViewById(m.e(this, "user_terms_check"));
        this.l = (Button) findViewById(m.e(this, "btn_next"));
        this.o = (ImageView) findViewById(m.e(this, "line1"));
        this.p = (ImageView) findViewById(m.e(this, "line2"));
        this.m = (LinearLayout) findViewById(m.e(this, "privacy_policy_agree_layout"));
        this.n = (LinearLayout) findViewById(m.e(this, "user_terms_agree_layout"));
        this.b.setOnClickListener(new s(this, "0", null));
        this.c.setOnClickListener(new s(this, "2", null));
        if (NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR.equals(this.q)) {
            this.d.setVisibility(0);
            this.e.setVisibility(0);
            this.f.setVisibility(8);
            this.g.setVisibility(8);
            this.l.setVisibility(8);
            List parcelableArrayListExtra = getIntent().getParcelableArrayListExtra("useragrs");
            a(parcelableArrayListExtra, this.d, "2");
            a(parcelableArrayListExtra, this.e, "0");
            return;
        }
        this.d.setVisibility(8);
        this.e.setVisibility(8);
        this.f.setVisibility(0);
        this.g.setVisibility(0);
        this.l.setVisibility(0);
        this.l.setOnClickListener(this.a);
        this.m.setOnClickListener(new n(this));
        this.n.setOnClickListener(new o(this));
        if ("2".equals(this.q)) {
            this.x = b(getIntent().getStringExtra("argFlags"));
            this.s = getIntent().getStringExtra("userId");
            if (TextUtils.isEmpty(this.s)) {
                this.s = c();
            }
            this.t = getIntent().getStringExtra("accountName");
            if (TextUtils.isEmpty(this.t)) {
                this.t = d();
            }
            this.u = getIntent().getStringExtra("siteId");
            if (TextUtils.isEmpty(this.u) && !TextUtils.isEmpty(this.t)) {
                this.u = String.valueOf(d.a((Context) this, this.t));
            }
            int i2 = 0;
            for (String str : this.x) {
                int i3;
                if ("2".equals(str)) {
                    i3 = i;
                    boolean z = true;
                } else if ("0".equals(str)) {
                    boolean z2 = true;
                    i = i2;
                } else {
                    i3 = i;
                    i = i2;
                }
                i2 = i;
                i = i3;
            }
            if (i2 == 0) {
                this.h.setVisibility(8);
                this.o.setVisibility(8);
                this.j.setChecked(true);
            }
            if (i == 0) {
                this.i.setVisibility(8);
                this.p.setVisibility(8);
                this.k.setChecked(true);
            }
        }
    }

    private List b(String str) {
        List arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            a.d("ManageAgreementActivity", "argFlag in getAgreeList is invalid");
            return arrayList;
        }
        String[] a = HwAccountConstants.a();
        int length = str.length();
        if (length > a.length) {
            length = a.length;
        }
        String str2 = "1";
        for (int i = 0; i < length; i++) {
            if (str2.equals(String.valueOf(str.charAt(i)))) {
                arrayList.add(a[i]);
            }
        }
        return arrayList;
    }

    private void a(List list, TextView textView, String str) {
        if (list != null && !list.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            for (Agreement agreement : list) {
                if (str.equals(agreement.a())) {
                    Object b = agreement.b();
                    if (!TextUtils.isEmpty(b)) {
                        try {
                            Date parse = simpleDateFormat.parse(b);
                            Calendar instance = Calendar.getInstance();
                            instance.setTime(parse);
                            int i = instance.get(1);
                            int i2 = instance.get(2) + 1;
                            int i3 = instance.get(5);
                            textView.setText(getString(m.a(this, "CS_agreement_signing_time"), (Object[]) new Integer[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)}));
                        } catch (ParseException e) {
                            a.d("ManageAgreementActivity", "parse date error");
                        }
                    }
                }
            }
        }
    }

    private void h() {
        a(null);
        b(false);
        String[] a = HwAccountConstants.a();
        if (!(this.x == null || this.x.isEmpty())) {
            a = (String[]) this.x.toArray(new String[this.x.size()]);
        }
        i.a((Context) this, new f(this, a), null, a(new t(this, this)));
    }

    private void a(AgreementVersion[] agreementVersionArr) {
        HwAccount hwAccount = null;
        if (TextUtils.isEmpty(this.s) || TextUtils.isEmpty(this.t)) {
            a.d("ManageAgreementActivity", "userId or userName is null when updateUserAgreement");
            finish();
            return;
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("cacheAccount")) {
            hwAccount = (HwAccount) extras.getParcelable("cacheAccount");
        }
        i.a((Context) this, new af(this, this.s, getIntent().getStringExtra("reqClientType"), agreementVersionArr), this.t, hwAccount, a(new u(this, this)));
    }

    public void onBackPressed() {
        if ("2".equals(this.q)) {
            i();
        } else {
            super.onBackPressed();
        }
    }

    private void i() {
        this.v = j.a((Activity) this);
        this.v.setButton(-1, getString(m.a(this, "CS_quit_hwid")), new q(this));
        this.v.getWindow().setGravity(17);
        a(this.v);
        this.v.show();
    }

    private void a(boolean z, Intent intent) {
        if (TextUtils.isEmpty(this.w)) {
            a.b("ManageAgreementActivity", "ManageAgreementActivity mTopActivity is null");
            setResult(-1);
            finish();
            return;
        }
        if (intent == null) {
            intent = new Intent();
        }
        intent.setClassName(this, this.w);
        intent.setFlags(67108864);
        a.b("ManageAgreementActivity", "onLoginComplete :" + this.w);
        super.startActivityForResult(intent, -1);
    }

    protected void onDestroy() {
        a.b("ManageAgreementActivity", "enter ManageAgreementActivity onDestroy");
        super.onDestroy();
    }
}
