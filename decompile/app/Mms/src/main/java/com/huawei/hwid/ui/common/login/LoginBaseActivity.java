package com.huawei.hwid.ui.common.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.util.ParseMeizuManager;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.SiteInfo;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.request.p;
import com.huawei.hwid.core.model.http.request.s;
import com.huawei.hwid.core.model.http.request.t;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.manager.g;
import com.huawei.hwid.simchange.b.b;
import com.huawei.hwid.ui.common.h;
import com.huawei.hwid.ui.common.i;
import com.huawei.hwid.ui.common.j;
import com.huawei.hwid.ui.common.password.FindpwdByHwIdActivity;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IActiveMemberCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class LoginBaseActivity extends LoginRegisterCommonActivity {
    protected Button a;
    protected EditText b;
    protected EditText c;
    protected TextView d;
    protected TextView e;
    protected TextView f;
    protected LinearLayout g;
    protected LinearLayout h;
    protected ArrayList i = new ArrayList();
    protected OnClickListener j = new OnClickListener(this) {
        final /* synthetic */ LoginBaseActivity a;

        {
            this.a = r1;
        }

        public void onClick(View view) {
            if (this.a.u()) {
                if (f.a(this.a).c(this.a, this.a.b.getText().toString())) {
                    this.a.b.setError(this.a.getString(m.a(this.a, "CS_username_already_login")));
                    this.a.b.requestFocus();
                    this.a.b.selectAll();
                    this.a.c.setText("");
                    return;
                }
                this.a.r = this.a.b.getText().toString();
                this.a.y = e.d(this.a, this.a.c.getText().toString());
                this.a.p = new c(this.a, "1", this.a.r);
                if (com.huawei.hwid.ui.common.f.FromOOBE == this.a.q()) {
                    this.a.p.a(true);
                }
                this.a.i();
                this.a.a(this.a.r, this.a.y);
                this.a.h();
            }
        }
    };
    protected OnClickListener k = new OnClickListener(this) {
        final /* synthetic */ LoginBaseActivity a;
        private boolean b = false;

        {
            this.a = r2;
        }

        public void onClick(View view) {
            boolean z = false;
            if (!this.b) {
                z = true;
            }
            this.b = z;
            j.a(this.a, this.a.c, this.a.f, this.b);
        }
    };
    protected OnClickListener l = new OnClickListener(this) {
        final /* synthetic */ LoginBaseActivity a;

        {
            this.a = r1;
        }

        public void onClick(View view) {
            if (this.a.i == null || this.a.i.isEmpty()) {
                this.a.h.setVisibility(8);
                return;
            }
            if (this.a.s != null && this.a.s.isShowing()) {
                this.a.s.dismiss();
            }
            final String[] f = this.a.s();
            this.a.s = new Builder(this.a, j.b(this.a)).setSingleChoiceItems(new i(this.a, m.d(this.a, "cs_simple_list_item_single_choice"), 16908308, f), 0, new DialogInterface.OnClickListener(this) {
                final /* synthetic */ AnonymousClass3 b;

                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i != f.length - 1) {
                        this.b.a.a(f, i);
                    } else {
                        this.b.a.t();
                    }
                }
            }).create();
            this.a.s.show();
            this.a.a(this.a.s);
        }
    };
    private c p;
    private boolean q = true;
    private String r;
    private AlertDialog s;
    private boolean t = false;
    private CloudLogincallBack u;
    private Bundle v;
    private Bundle w;
    private AlertDialog x = null;
    private String y = "";

    class AddLoginAcctCallback extends com.huawei.hwid.ui.common.c {
        final /* synthetic */ LoginBaseActivity b;
        private Bundle d;

        public AddLoginAcctCallback(LoginBaseActivity loginBaseActivity, Context context, Bundle bundle) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context);
            this.d = bundle;
        }

        public void onSuccess(Bundle bundle) {
            super.onSuccess(bundle);
            Class a = com.huawei.hwid.core.c.j.a("com.huawei.third.ui.BindWeixinAccountSuccessActivity");
            if (a != null) {
                Intent intent = new Intent(this.a, a);
                intent.putExtras(this.d);
                intent.putExtras(this.b.getIntent().getExtras());
                intent.putExtra("open_weixin_from_login_or_register", "login");
                this.b.startActivityForResult(intent, 201);
                return;
            }
            a.b("LoginBaseActivity", "cls is null, onSuccess error");
        }

        public void onFail(Bundle bundle) {
            this.b.b();
            if (bundle.getBoolean("isRequestSuccess", false)) {
                Dialog create = j.a(this.a, m.a(this.a, "CS_server_unavailable_message"), m.a(this.a, "CS_server_unavailable_title")).create();
                this.b.a(create);
                create.show();
            }
            f.a(this.a).a(this.a, this.b.r, "com.huawei.hwid");
            super.onFail(bundle);
        }
    }

    class CheckAccountcallBack extends com.huawei.hwid.ui.common.c {
        final /* synthetic */ LoginBaseActivity b;

        public CheckAccountcallBack(LoginBaseActivity loginBaseActivity, Context context) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context);
        }

        public void onSuccess(Bundle bundle) {
            this.b.b(true);
            super.onSuccess(bundle);
            ArrayList parcelableArrayList = bundle.getParcelableArrayList("siteInfoList");
            if (parcelableArrayList != null && parcelableArrayList.size() > 0) {
                String[] strArr = new String[parcelableArrayList.size()];
                String[] strArr2 = new String[parcelableArrayList.size()];
                for (int i = 0; i < parcelableArrayList.size(); i++) {
                    strArr[i] = "+" + ((SiteInfo) parcelableArrayList.get(i)).a().replaceFirst("00", "") + " " + this.b.r;
                    strArr2[i] = "00" + ((SiteInfo) parcelableArrayList.get(i)).a().replaceFirst("00", "");
                }
                this.b.a(strArr, strArr2);
            }
        }

        public void onFail(Bundle bundle) {
            if (bundle.getBoolean("isRequestSuccess", false)) {
                this.b.b(true);
                Dialog create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
                this.b.a(create);
                create.show();
            }
            super.onFail(bundle);
        }
    }

    class CloudLogincallBack extends l {
        final /* synthetic */ LoginBaseActivity b;

        public CloudLogincallBack(LoginBaseActivity loginBaseActivity, Context context, g gVar) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context, gVar);
        }

        public void onSuccess(Bundle bundle) {
            String string;
            int i = bundle.getInt("userState", -2);
            a.b("LoginBaseActivity", "userState:" + i);
            if (b.g(this.a)) {
                string = bundle.getString("userName");
                Account[] accountsByType = AccountManager.get(this.b).getAccountsByType("com.huawei.hwid");
                if (accountsByType != null && accountsByType.length > 0 && accountsByType[0].name != null && accountsByType[0].name.equals(string)) {
                    a.b("LoginBaseActivity", "login success startSaveSimChangeInfo");
                    b.a(this.a, this.b.r, "normal");
                }
            }
            string = bundle.getString("agrFlags");
            if (i == 0 && d.l(this.b)) {
                if (d.j(string)) {
                    this.b.t = true;
                } else {
                    this.b.a(this.a);
                }
            }
            if (this.b.o()) {
                a.a("LoginBaseActivity", "weixin login, need to check hwid account");
                i = bundle.getInt("siteId");
                string = bundle.getString("userName");
                com.huawei.hwid.core.helper.handler.c getUserAcctInfocallBack = new GetUserAcctInfocallBack(this.b, this.a, this, bundle);
                com.huawei.hwid.core.model.http.i.a(this.a, new p(this.a, string, i), string, this.b.a(getUserAcctInfocallBack));
                this.b.a(this.b.getString(m.a(this.a, "CS_logining_message")));
            } else if (string != null && d.j(string)) {
                a.a("LoginBaseActivity", "user need to agree new term after logined");
                this.b.v = bundle;
                this.b.u = this;
                this.b.a(301, this.b.v, this.a);
            } else {
                loginSuccessCallback(bundle);
            }
        }

        public void loginSuccessCallback(final Bundle bundle) {
            this.b.b(false);
            super.onSuccess(bundle);
            a.e("LoginBaseActivity", com.huawei.hwid.core.encrypt.f.a(bundle));
            this.b.p.a(d.a());
            com.huawei.hwid.core.a.d.a(this.b.p, this.b);
            if (com.huawei.hwid.core.b.a.a(this.a).a(this.b.d())) {
                com.huawei.hwid.core.b.a.a(this.a).b(this.b.d());
            }
            if (a()) {
                if (this.b.n && d.h(this.b)) {
                    a.b("LoginBaseActivity", "needActivateVip is true!  need to activate!");
                    AnonymousClass1 anonymousClass1 = new IActiveMemberCallback(this) {
                        final /* synthetic */ CloudLogincallBack b;

                        public void callback(String str, String str2, int i) {
                            a.b("LoginBaseActivity", "retCode:" + str + " memLevel:" + i);
                            if ("0".equals(str)) {
                                bundle.putInt("rightsID", i);
                                com.huawei.hwid.c.a.a(this.b.b, bundle);
                                this.b.b.a(this.b.b(), bundle);
                                return;
                            }
                            this.b.b.a(true, new Intent().putExtra("bundle", bundle));
                        }
                    };
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("userID", b().c());
                    bundle2.putString("deviceID", d.k(b().h()));
                    bundle2.putString("deviceType", b().i());
                    bundle2.putString("st", b().f());
                    com.huawei.a.a.a.a(this.b, anonymousClass1, bundle2);
                } else {
                    a.b("LoginBaseActivity", "needActivateVip is false!");
                    this.b.a(b(), bundle);
                }
                return;
            }
            this.b.b();
            if (this.b.b.isFocusableInTouchMode()) {
                this.b.b.setError(this.b.getString(m.a(this.b, "CS_username_already_login")));
                this.b.b.requestFocus();
                this.b.b.selectAll();
            } else {
                this.b.c.setError(this.b.getString(m.a(this.b, "CS_username_already_login")));
            }
            this.b.c.setText("");
        }

        public void onFail(Bundle bundle) {
            boolean z = bundle.getBoolean("isRequestSuccess", false);
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            a.b("LoginBaseActivity", "onFail: isRequestSuccess " + z);
            if (errorStatus != null) {
                if (z) {
                    if (70002067 == errorStatus.getErrorCode()) {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    } else if (70002068 == errorStatus.getErrorCode()) {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    } else if (70002069 == errorStatus.getErrorCode()) {
                        this.b.e(this.b.getString(m.a(this.b, "CS_area_not_support_service_new")));
                    } else if (70002072 == errorStatus.getErrorCode()) {
                        this.b.b(false);
                        this.b.w();
                        this.b.u = this;
                    } else if (70002058 == errorStatus.getErrorCode()) {
                        if (this.b.c != null) {
                            j.a(this.b, this.b.c.getWindowToken());
                        }
                        Dialog a = j.a(this.b, this.b.r, false, new com.huawei.hwid.core.helper.handler.b(this) {
                            final /* synthetic */ CloudLogincallBack a;

                            {
                                this.a = r1;
                            }

                            public void onForget() {
                                Intent intent = new Intent();
                                intent.putExtra("userAccount", this.a.b.r);
                                intent.setClass(this.a.b, FindpwdByHwIdActivity.class);
                                this.a.b.startActivityForResult(intent, 100);
                            }
                        });
                        if (a != null) {
                            this.b.a(a);
                        }
                    } else if (70002071 == errorStatus.getErrorCode()) {
                        this.b.b(false);
                        this.b.b(this.b.r);
                    } else if (70002002 == errorStatus.getErrorCode()) {
                        this.b.b(false);
                        com.huawei.hwid.core.model.http.i.a(this.b, new s(this.b, this.b.r), null, this.b.a(new CheckAccountcallBack(this.b, this.b)));
                    } else if (this.b.b.isFocusableInTouchMode()) {
                        this.b.b.setError(this.b.getString(m.a(this.b, "CS_password_incorrect")));
                        this.b.b.requestFocus();
                        this.b.b.selectAll();
                        this.b.c.setText("");
                    } else {
                        this.b.c.setText("");
                        this.b.c.setError(this.b.getString(m.a(this.b, "CS_password_incorrect")));
                    }
                }
                this.b.p.a(d.a());
                this.b.p.c(String.valueOf(errorStatus.getErrorCode()));
                this.b.p.d(errorStatus.getErrorReason());
            }
            com.huawei.hwid.core.a.d.a(this.b.p, this.b);
            super.onFail(bundle);
        }
    }

    class GetAccInfoCallBack extends com.huawei.hwid.ui.common.c {
        final /* synthetic */ LoginBaseActivity b;

        public GetAccInfoCallBack(LoginBaseActivity loginBaseActivity, Context context) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context);
        }

        public void onSuccess(Bundle bundle) {
            this.b.b(true);
            super.onSuccess(bundle);
            a.a("LoginBaseActivity", "onSuccess");
            this.b.a(bundle.getString("userID"), d.a(bundle.getParcelableArrayList("accountsInfo"), true));
        }

        public void onFail(Bundle bundle) {
            this.b.b(true);
            this.b.b();
            super.onFail(bundle);
        }
    }

    class GetEmailURLcallBack extends com.huawei.hwid.ui.common.c {
        final /* synthetic */ LoginBaseActivity b;

        public GetEmailURLcallBack(LoginBaseActivity loginBaseActivity, Context context, com.huawei.hwid.core.model.http.a aVar) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context);
        }

        public void onSuccess(Bundle bundle) {
            this.b.b(true);
            super.onSuccess(bundle);
            this.b.a(bundle);
        }

        public void onFail(Bundle bundle) {
            this.b.b(true);
            this.b.b();
            if (bundle.getBoolean("isRequestSuccess", false)) {
                ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
                if (errorStatus != null) {
                    if (70002019 == errorStatus.getErrorCode()) {
                        this.b.r = this.b.b.getText().toString();
                        String d = e.d(this.b, this.b.c.getText().toString());
                        this.b.p = new c(this.b, "1", this.b.r);
                        if (com.huawei.hwid.ui.common.f.FromOOBE == this.b.q()) {
                            this.b.p.a(true);
                        }
                        this.b.i();
                        this.b.a(this.b.r, d);
                    } else if (70001104 == errorStatus.getErrorCode() || 70001102 == errorStatus.getErrorCode()) {
                        a.b("LoginBaseActivity", "request over times");
                        this.b.a(bundle);
                    } else {
                        Dialog create = j.a(this.b, m.a(this.b, "CS_server_unavailable_message"), m.a(this.b, "CS_server_unavailable_title")).create();
                        this.b.a(create);
                        create.show();
                    }
                }
            }
            super.onFail(bundle);
        }
    }

    class GetUserAcctInfocallBack extends com.huawei.hwid.ui.common.c {
        final /* synthetic */ LoginBaseActivity b;
        private Bundle d;
        private CloudLogincallBack e;

        public GetUserAcctInfocallBack(LoginBaseActivity loginBaseActivity, Context context, CloudLogincallBack cloudLogincallBack, Bundle bundle) {
            this.b = loginBaseActivity;
            super(loginBaseActivity, context);
            this.d = bundle;
            this.e = cloudLogincallBack;
        }

        public void onSuccess(Bundle bundle) {
            Object obj;
            super.onSuccess(bundle);
            for (UserAccountInfo accountType : bundle.getParcelableArrayList("accountsInfo")) {
                if (ParseMeizuManager.SMS_FLOW_THREE.equals(accountType.getAccountType())) {
                    obj = 1;
                    break;
                }
            }
            obj = null;
            if (obj != null) {
                f.a(this.a).a(this.a, this.b.r, "com.huawei.hwid");
                this.b.b.setError(this.b.getString(m.a(this.b, "CS_hwid_already_binded")));
                this.b.b.requestFocus();
                this.b.b.selectAll();
                this.b.c.setText("");
                return;
            }
            this.e.loginSuccessCallback(this.d);
        }

        public void onFail(Bundle bundle) {
            this.b.b();
            if (bundle.getBoolean("isRequestSuccess", false)) {
                Dialog create = j.a(this.a, m.a(this.a, "CS_server_unavailable_message"), m.a(this.a, "CS_server_unavailable_title")).create();
                this.b.a(create);
                create.show();
            }
            super.onFail(bundle);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("startActivityWay", q().ordinal());
    }

    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        int i = bundle.getInt("startActivityWay", com.huawei.hwid.ui.common.f.FromApp.ordinal());
        if (i >= 0 && i < com.huawei.hwid.ui.common.f.values().length) {
            a(com.huawei.hwid.ui.common.f.values()[i]);
        }
    }

    private String[] s() {
        ArrayList arrayList = new ArrayList();
        Iterator it = this.i.iterator();
        while (it.hasNext()) {
            arrayList.add((String) it.next());
        }
        Collections.reverse(arrayList);
        int size = arrayList.size();
        Object obj = new String[(size + 1)];
        System.arraycopy((String[]) arrayList.toArray(new String[size]), 0, obj, 0, size);
        obj[size] = getString(m.a(this, "CS_clear_all_history"));
        return obj;
    }

    private void t() {
        h.a((Context) this, this.b, this.h);
        this.i.clear();
        h.a(getApplicationContext(), "historyAccounts.xml");
        if (this.s != null && this.s.isShowing()) {
            this.s.dismiss();
        }
    }

    private void a(String[] strArr, int i) {
        Object obj = strArr[i];
        this.b.setText(obj);
        this.b.setSelection(obj.length());
        this.b.setError(null);
        if (this.s != null && this.s.isShowing()) {
            this.s.dismiss();
        }
    }

    private boolean u() {
        boolean z;
        if (this.b == null || this.b.getText() == null || TextUtils.isEmpty(this.b.getText().toString())) {
            z = false;
        } else if (com.huawei.hwid.core.c.p.b(this.b.getText().toString())) {
            z = true;
        } else {
            this.b.setError(getString(m.a(this, "CS_login_username_error")));
            z = false;
        }
        if (this.c == null || this.c.getText() == null || TextUtils.isEmpty(this.c.getText().toString())) {
            return false;
        }
        if (com.huawei.hwid.core.c.p.a(this.c.getText().toString())) {
            return z;
        }
        this.c.setError(getString(m.a(this, "CS_error_have_special_symbol")));
        return false;
    }

    protected void d(boolean z) {
        this.q = z;
    }

    protected boolean g() {
        return this.q;
    }

    private void a(String str, String str2) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("needActivateVip", this.n);
        com.huawei.hwid.a.a().a(str2);
        com.huawei.hwid.core.model.http.i.a((Context) this, new t(this, str, str2, k(), bundle), str, a(new CloudLogincallBack(this, this, f.a(this))));
        a(getString(m.a(this, "CS_logining_message")));
    }

    protected void h() {
    }

    private void a(String[] strArr, final String[] strArr2) {
        if (this.x != null && this.x.isShowing()) {
            this.x.dismiss();
            this.x = null;
        }
        this.x = new Builder(this, j.b(this)).setSingleChoiceItems(strArr, 0, new DialogInterface.OnClickListener(this) {
            final /* synthetic */ LoginBaseActivity b;

            public void onClick(DialogInterface dialogInterface, int i) {
                String str = strArr2[i] + this.b.r;
                if (f.a(this.b).c(this.b, str)) {
                    j.a(this.b, m.a(this.b, "CS_username_already_login"));
                    return;
                }
                this.b.r = str;
                this.b.a(this.b.r, this.b.y);
                this.b.x.dismiss();
            }
        }).create();
        a(this.x);
        this.x.show();
    }

    private void a(Bundle bundle, HwAccount hwAccount, int i) {
        Dialog a = j.a(this, bundle, this.m, i, hwAccount);
        a.setOnKeyListener(new OnKeyListener(this) {
            final /* synthetic */ LoginBaseActivity a;

            {
                this.a = r1;
            }

            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == 4 && keyEvent.getRepeatCount() == 0 && keyEvent.getAction() == 0) {
                    this.a.v();
                }
                return false;
            }
        });
        a.getWindow().setGravity(17);
        a(a);
        a.show();
    }

    private void v() {
        Dialog a = j.a((Activity) this);
        a.setButton(-1, getString(m.a(this, "CS_quit_hwid")), new DialogInterface.OnClickListener(this) {
            final /* synthetic */ LoginBaseActivity a;

            {
                this.a = r1;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                a.b("LoginBaseActivity", "not agree new terms, need to quit account");
                g a = f.a(this.a);
                if (a.c(this.a, this.a.r)) {
                    d.b(this.a, false);
                    a.a("LoginBaseActivity", "account exist, ready to remove it");
                    a.a(this.a, this.a.r, null, new AccountManagerCallback(this) {
                        final /* synthetic */ AnonymousClass6 a;

                        {
                            this.a = r1;
                        }

                        public void run(AccountManagerFuture accountManagerFuture) {
                            this.a.a.a(false, null);
                        }
                    });
                    return;
                }
                this.a.a(false, null);
            }
        });
        a.getWindow().setGravity(17);
        a(a);
        a.show();
    }

    private void a(String str, ArrayList arrayList) {
        this.o = true;
        Intent intent = new Intent();
        intent.setAction("com.huawei.hwid.ACTION_CHECK_IDENTITY");
        intent.setPackage(getPackageName());
        intent.putExtra("isLogin", false);
        intent.putExtra("userName", this.r);
        intent.putExtra("ONCHECKIDENTITY", true);
        intent.putExtra("RequestTokenType", k());
        intent.putExtra("userId", str);
        intent.putExtra("userinfolist", arrayList);
        startActivityForResult(intent, 302);
    }

    private void b(String str) {
        com.huawei.hwid.core.model.http.a eVar = new com.huawei.hwid.core.model.http.request.e(this, str, str, new Bundle());
        eVar.b(70001104);
        com.huawei.hwid.core.model.http.i.a((Context) this, eVar, str, a(new GetEmailURLcallBack(this, this, eVar)));
    }

    private void a(Bundle bundle) {
        Intent intent = new Intent(this, RegisterResetVerifyEmailActivity.class);
        intent.putExtra("isFromRegister", false);
        intent.putExtra("isFromOther", true);
        intent.putExtra("bundle", bundle);
        intent.putExtra("emailName", this.r);
        intent.putExtras(getIntent());
        startActivityForResult(intent, 303);
    }

    private void a(Context context) {
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accountsByType == null || accountsByType.length <= 0 || accountsByType[0].name.equals(this.r)) {
            a.b("LoginBaseActivity", "showNotification");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            CharSequence string = context.getString(m.a(context, "CS_register_email_verified_notify"));
            Class a = com.huawei.hwid.core.c.j.a("com.huawei.hwid.cloudsettings.ui.AccountCenterActivity");
            if (a != null) {
                builder.setTicker(string).setContentText(string).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), m.g(context, "cs_account_icon"))).setSmallIcon(m.g(context, "vip_account_icon_notification")).setDefaults(-1).setWhen(System.currentTimeMillis()).setAutoCancel(true).setDefaults(1).setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, a), 134217728));
                notificationManager.cancel(10012);
                notificationManager.notify(10012, builder.build());
                return;
            }
            a.b("LoginBaseActivity", "cls is null, CenterAct error");
            return;
        }
        a.b("LoginBaseActivity", "AccountManager has diff account,not showNotification");
    }

    public void onBackPressed() {
        if (l() || com.huawei.hwid.ui.common.f.FromOpenSDK == q()) {
            a(false, null);
            return;
        }
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("LoginBaseActivity", "catch Exception throw by FragmentManager!", e);
        }
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (100 == i && i2 == -1) {
            if (intent != null) {
                CharSequence stringExtra = intent.getStringExtra("accountName");
                if (stringExtra != null && this.b.isFocusableInTouchMode()) {
                    this.b.setText(stringExtra);
                    this.c.requestFocus();
                }
            }
        } else if (201 == i) {
            a(true, new Intent().putExtra("bundle", intent.getExtras()));
        } else if (301 == i && i2 == -1) {
            if (this.u != null) {
                this.u.loginSuccessCallback(this.v);
                if (this.t) {
                    a((Context) this);
                    this.t = false;
                }
            }
        } else if (302 == i && -1 == i2) {
            a.b("LoginBaseActivity", "FINISH TWO_STEP_VERIFY_LOGIN");
            if (intent != null) {
                Bundle extras = intent.getExtras();
                this.w = extras;
                if (extras == null) {
                    return;
                }
                if (d.j(this.w.getString("agrFlags"))) {
                    a(304, this.w, (Context) this);
                } else if (this.w != null && this.u != null) {
                    a.b("LoginBaseActivity", "TWO_STEP_UPDATE_AGREEMENT mCloudLogincallBack");
                    if (this.n && d.h((Context) this)) {
                        a(null);
                    }
                    this.u.loginSuccessCallback(this.w);
                }
            }
        } else if (304 == i && i2 == -1) {
            a.b("LoginBaseActivity", "TWO_STEP_UPDATE_AGREEMENT success");
            if (this.u != null && this.w != null) {
                a.b("LoginBaseActivity", "TWO_STEP_UPDATE_AGREEMENT mCloudLogincallBack");
                this.u.loginSuccessCallback(this.w);
            }
        }
    }

    protected void onDestroy() {
        a.b("LoginBaseActivity", "onDestroy");
        super.onDestroy();
    }

    private void b(Bundle bundle) {
        com.huawei.hwid.core.model.http.i.a((Context) this, new com.huawei.hwid.core.model.http.request.a(this, ParseMeizuManager.SMS_FLOW_THREE, this.r, getIntent().getStringExtra("third_openid"), getIntent().getStringExtra("third_access_token"), bundle.getString("userId")), this.r, a(new AddLoginAcctCallback(this, this, bundle)));
        a(getString(m.a(this, "CS_logining_message")));
    }

    private void a(HwAccount hwAccount, Bundle bundle) {
        b();
        if (!g()) {
            Object a = f.a(this).a((Context) this, this.r, null, "userId");
            CharSequence b = com.huawei.hwid.core.c.i.b(this, "bindFingetUserId");
            if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b) && a.equals(b) && d.h((Context) this)) {
                com.huawei.hwid.core.c.i.e(this, this.r);
            }
        }
        a(hwAccount);
        if (!d.l(this) || !d.j((Context) this)) {
            Intent intent = new Intent();
            intent.putExtra("hwaccount", hwAccount);
            intent.putExtra("isUseSDK", true);
            if (l() || com.huawei.hwid.ui.common.f.FromApp == q()) {
                a.b("LoginBaseActivity", "start APK by old way, set result to StartUpGuideLoginActivity or AccountManagerActivity");
                a(true, intent);
            } else {
                intent.setPackage(getPackageName());
                com.huawei.hwid.core.c.e.a(this, intent);
                finish();
            }
        } else if (o()) {
            b(bundle);
        } else {
            a(true, new Intent().putExtra("bundle", bundle));
        }
    }

    protected void i() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService("input_method");
        if (inputMethodManager != null) {
            View currentFocus = getCurrentFocus();
            if (currentFocus != null && currentFocus.getWindowToken() != null) {
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 2);
            }
        }
    }

    private void a(final int i, final Bundle bundle, Context context) {
        a.b("LoginBaseActivity", "user need requestCode = " + i);
        g a = f.a(context);
        String string = bundle.getString(NetUtil.REQ_QUERY_TOEKN);
        String string2 = bundle.getString("userId");
        String string3 = bundle.getString("cookie");
        final HwAccount a2 = d.a(bundle.getString("userName"), bundle.getString("tokenType"), string, string2, bundle.getInt("siteId"), string3, bundle.getString("deviceId"), bundle.getString("deviceType"), bundle.getString("accountType"));
        if (a.c(context, this.r)) {
            d.b(context, false);
            a.a("LoginBaseActivity", "remove account after check agreement update");
            a.a(context, this.r, null, new AccountManagerCallback(this) {
                final /* synthetic */ LoginBaseActivity d;

                public void run(AccountManagerFuture accountManagerFuture) {
                    this.d.a(bundle, a2, i);
                }
            });
            return;
        }
        a(bundle, a2, i);
    }

    private void w() {
        com.huawei.hwid.core.model.http.a pVar = new p(this, this.r);
        pVar.a(false);
        com.huawei.hwid.core.model.http.i.a((Context) this, pVar, this.r, a(new GetAccInfoCallBack(this, this)));
    }
}
