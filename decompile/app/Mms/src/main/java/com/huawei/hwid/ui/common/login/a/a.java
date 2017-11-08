package com.huawei.hwid.ui.common.login.a;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.model.http.i;
import com.huawei.hwid.core.model.http.request.ad;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.b.b;
import com.huawei.hwid.ui.common.j;
import java.lang.reflect.Field;

/* compiled from: PwdDialogFragment */
public class a extends DialogFragment {
    o a;
    public EditText b;
    Dialog c;
    private TextView d;
    private LinearLayout e;
    private FrameLayout f;
    private ScrollView g;
    private TextView h;
    private String i = "";
    private String j = "";
    private String k = "";
    private String l = "com.huawei.hwid";
    private boolean m = false;
    private int n = 7;
    private c o;
    private AlertDialog p = null;
    private boolean q = false;
    private Button r;
    private b s = new b(this);
    private OnClickListener t = new d(this);

    public static a a(String str, String str2, String str3, int i) {
        a aVar = new a();
        Bundle bundle = new Bundle();
        bundle.putString("username", str);
        bundle.putString("userid", str2);
        bundle.putString("appID", str3);
        bundle.putInt("reqclienttype", i);
        aVar.setArguments(bundle);
        return aVar;
    }

    public static a a(boolean z) {
        a aVar = new a();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isThirdAccount", z);
        aVar.setArguments(bundle);
        return aVar;
    }

    public void onAttach(Activity activity) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onAttach");
        try {
            this.a = (o) activity;
        } catch (Exception e) {
            com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", e.toString());
        }
        super.onAttach(activity);
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.m = arguments.getBoolean("isThirdAccount");
            if (!this.m) {
                this.j = arguments.getString("username");
                this.i = arguments.getString("userid");
                this.l = arguments.getString("appID");
                this.n = arguments.getInt("reqclienttype");
            }
        }
        if (VERSION.SDK_INT > 22) {
            c();
        }
        if (!this.m) {
            com.huawei.hwid.ui.common.b.a.a().a(this.s);
        }
        return a(getActivity());
    }

    private void c() {
        if (getActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
            q.c(getActivity());
            q.a(getActivity());
            q.e(getActivity());
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10003) {
            if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
                q.c(getActivity());
                q.a(getActivity());
                q.e(getActivity());
                return;
            }
            a(getString(m.a(getActivity(), "CS_read_phone_state_permission")));
        }
    }

    public void a(String str) {
        if (TextUtils.isEmpty(str)) {
            com.huawei.hwid.core.c.b.a.d("PwdDialogFragment", "PermissionName is null!");
            if (this.a == null) {
                getActivity().finish();
            } else {
                this.a.g();
            }
            return;
        }
        this.p = d.d(getActivity(), str).setNegativeButton(17039360, new f(this)).setPositiveButton(m.a(getActivity(), "CS_go_settings"), new e(this)).create();
        this.p.setCancelable(false);
        this.p.setCanceledOnTouchOutside(false);
        this.p.setOnDismissListener(new g(this));
        if (!getActivity().isFinishing()) {
            this.p.show();
        }
    }

    public void a() {
        if (this.p != null) {
            this.p.dismiss();
            this.p = null;
        }
    }

    private Dialog a(Context context) {
        if (this.m) {
            return b(context);
        }
        View inflate;
        if (d.r(context)) {
            inflate = View.inflate(context, m.d(getActivity(), "cs_check_logined_dialog_3"), null);
        } else {
            inflate = View.inflate(context, m.d(getActivity(), "cs_check_logined_dialog"), null);
        }
        ((TextView) inflate.findViewById(m.e(getActivity(), "forget_pwd"))).setOnClickListener(new h(this));
        this.d = (TextView) inflate.findViewById(m.e(getActivity(), "display_pass"));
        this.e = (LinearLayout) inflate.findViewById(m.e(getActivity(), "display_pass_layout"));
        this.e.setOnClickListener(this.t);
        this.g = (ScrollView) inflate.findViewById(m.e(getActivity(), "disableIncludeLayout"));
        this.f = (FrameLayout) inflate.findViewById(m.e(getActivity(), "password_display_layout"));
        this.h = (TextView) inflate.findViewById(m.e(getActivity(), "user_name"));
        this.h.setText(getString(m.a(getActivity(), "CS_huawei_account"), new Object[]{this.j}));
        this.b = (EditText) inflate.findViewById(m.e(getActivity(), "input_password"));
        this.b.setHint(m.a(getActivity(), "CS_old_pwd"));
        c(context);
        Dialog create = new Builder(context, j.b(context)).setView(inflate).setTitle(m.a(getActivity(), "CS_use_account_pwd")).setNegativeButton(17039360, new j(this)).setPositiveButton(17039370, new i(this, context)).create();
        create.setCanceledOnTouchOutside(false);
        create.show();
        this.r = create.getButton(-1);
        k kVar = new k(this, this.b);
        this.r.setEnabled(false);
        this.c = create;
        b();
        return create;
    }

    public void b() {
        boolean z = false;
        a(this.g, 8);
        a(this.h, 0);
        a(this.f, 0);
        if (this.r != null) {
            this.r.setText(17039370);
            if (this.b != null) {
                Button button = this.r;
                if (!TextUtils.isEmpty(this.b.getText().toString())) {
                    z = true;
                }
                button.setEnabled(z);
            }
        }
        if (getDialog() != null) {
            getDialog().setTitle(m.a(getActivity(), "CS_use_account_pwd"));
        }
    }

    public void a(View view, int i) {
        if (view != null) {
            view.setVisibility(i);
        }
    }

    private void d() {
        if (this.h != null) {
            j.a(getActivity(), this.h.getWindowToken());
        }
        a(this.h, 8);
        a(this.f, 8);
        a(this.g, 0);
        if (this.r != null) {
            this.r.setEnabled(false);
            this.r.setText(m.a(getActivity(), "CS_verify_account"));
        }
        if (getDialog() != null) {
            getDialog().setTitle(m.a(getActivity(), "CS_pwd_disable_verify_login_pwd"));
        }
    }

    private Dialog b(Context context) {
        Dialog create = new Builder(context, j.b(context)).setMessage(m.a(getActivity(), "CS_no_hwid")).setTitle(m.a(getActivity(), "CS_title_tips")).setPositiveButton(17039370, new l(this)).create();
        create.setCanceledOnTouchOutside(false);
        create.show();
        this.c = create;
        return create;
    }

    private boolean e() {
        if (this.b == null || this.b.getText() == null || TextUtils.isEmpty(this.b.getText().toString())) {
            return false;
        }
        if (!p.a(this.b.getText().toString())) {
            this.b.setError(getString(m.a(getActivity(), "CS_error_have_special_symbol")));
            return false;
        } else if (TextUtils.isEmpty(this.b.getError())) {
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.e("PwdDialogFragment", "the password has error");
            return false;
        }
    }

    private void c(Context context) {
        if (this.h == null || this.b == null) {
            com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "setEditTextListener error, editText is null");
            return;
        }
        this.b.requestFocus();
        this.h.setEnabled(false);
        c cVar = new c(this, context, this.b);
    }

    protected void a(String str, String str2) {
        ((BaseActivity) getActivity()).a(getString(m.a(getActivity(), "CS_verify_waiting_progress_message")));
        i.a(getActivity(), new ad(getActivity(), this.i, this.l, str2, "1", this.n), this.j, ((BaseActivity) getActivity()).a(new n(this, getActivity())));
    }

    private Bundle a(Bundle bundle) {
        Bundle bundle2 = new Bundle();
        bundle2.putAll(bundle);
        bundle2.putString("password", this.k);
        return bundle2;
    }

    public void a(Dialog dialog, boolean z) {
        if (dialog != null) {
            try {
                Field declaredField = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                declaredField.setAccessible(true);
                declaredField.set(dialog, Boolean.valueOf(z));
            } catch (Throwable e) {
                com.huawei.hwid.core.c.b.a.d("PwdDialogFragment", "Exception: " + e, e);
            }
            return;
        }
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "cleanupDialog, is null");
    }

    public void onDismiss(DialogInterface dialogInterface) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onDismiss");
        super.onDismiss(dialogInterface);
    }

    public void onStart() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onStart");
        try {
            super.onStart();
        } catch (IllegalStateException e) {
            com.huawei.hwid.core.c.b.a.d("PwdDialogFragment", e.toString());
        } catch (Exception e2) {
            com.huawei.hwid.core.c.b.a.d("PwdDialogFragment", e2.toString());
        }
    }

    public void onResume() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onResume");
        super.onResume();
        if (this.q) {
            this.q = false;
            if (VERSION.SDK_INT <= 22) {
                return;
            }
            if (getActivity().checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                q.c(getActivity());
                q.a(getActivity());
                q.e(getActivity());
            } else if (this.a == null) {
                getActivity().finish();
            } else {
                this.a.g();
            }
        }
    }

    public void onPause() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onPause");
        a(this.c, true);
        super.onPause();
    }

    public void onCancel(DialogInterface dialogInterface) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onCancel");
        if (this.a != null) {
            this.a.g();
        }
        com.huawei.hwid.ui.common.b.a.a().b(this.s);
        super.onCancel(dialogInterface);
    }

    public void onStop() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onStop");
        super.onStop();
    }

    public void onDestroyView() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onDestroyView");
        a(this.c, true);
        if (this.c != null) {
            this.c.dismiss();
        }
        a();
        super.onDestroyView();
    }

    public void onDestroy() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onDestroy");
        super.onDestroy();
    }

    public void onDetach() {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onDetach");
        super.onDetach();
    }

    public void onCreate(Bundle bundle) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onCreate");
        super.onCreate(bundle);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onCreateView");
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public void onActivityCreated(Bundle bundle) {
        com.huawei.hwid.core.c.b.a.b("PwdDialogFragment", "onActivityCreated");
        super.onActivityCreated(bundle);
    }
}
