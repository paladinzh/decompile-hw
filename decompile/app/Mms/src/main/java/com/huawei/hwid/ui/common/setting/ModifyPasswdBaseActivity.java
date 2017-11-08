package com.huawei.hwid.ui.common.setting;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.g;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.BaseActivity;
import com.huawei.hwid.ui.common.j;

public abstract class ModifyPasswdBaseActivity extends BaseActivity {
    protected String a;
    protected boolean b = true;
    OnClickListener c = new f(this);
    private Button d;
    private Button e;
    private EditText f;
    private EditText g;
    private TextView h;
    private LinearLayout i;
    private AlertDialog j;
    private String k = "";
    private View.OnClickListener l = new d(this);
    private View.OnClickListener m = new e(this);

    protected abstract void a(EditText editText, EditText editText2);

    protected abstract void b(String str);

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        g();
    }

    private void g() {
        if (f()) {
            requestWindowFeature(1);
            setContentView(m.d(this, "oobe_set_password"));
            this.d = (Button) findViewById(m.e(this, "btn_next"));
            this.d.setText(m.a(this, "CS_done"));
        } else {
            setContentView(m.d(this, "cs_set_password"));
            this.d = (Button) findViewById(m.e(this, "btn_submit"));
        }
        this.f = (EditText) findViewById(m.e(this, "input_password"));
        this.g = (EditText) findViewById(m.e(this, "confirm_password"));
        a(this.f, this.g);
        this.e = (Button) findViewById(m.e(this, "btn_back"));
        this.e.setOnClickListener(new a(this));
        this.f.requestFocus();
        g.a(this.f, this.g, this.d);
        b bVar = new b(this, this, this.f);
        c cVar = new c(this, this, this.g);
        this.d.setOnClickListener(this.m);
        this.h = (TextView) findViewById(m.e(this, "display_pass"));
        this.i = (LinearLayout) findViewById(m.e(this, "display_pass_layout"));
        this.i.setOnClickListener(this.l);
    }

    private AlertDialog h() {
        Builder a = j.a((Context) this, m.a(this, "CS_title_tips"), getString(m.a(this, "CS_modify_nickname_notice")), false);
        a.setPositiveButton(17039370, this.c);
        a.setNegativeButton(17039360, null);
        return a.create();
    }

    protected boolean b(EditText editText, EditText editText2) {
        return g.a(this.a, this.f, this.g, getApplicationContext());
    }

    protected void onPause() {
        super.onPause();
        if (this.j != null) {
            this.j.dismiss();
            this.j = null;
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("ModifyPasswdBaseActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
