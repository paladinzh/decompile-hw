package com.huawei.hwid.ui.common;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.helper.handler.c;
import com.huawei.hwid.ui.common.login.LoginActivity;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseActivity extends Activity {
    private static HashMap a = new HashMap();
    private int b = 0;
    private int c = 0;
    private boolean d = false;
    private boolean e = true;
    private ProgressDialog f;
    private ArrayList g = new ArrayList();
    private boolean h = false;
    private boolean i = true;

    protected void a() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        intent.putExtra("topActivity", LoginActivity.class.getName());
        if (!TextUtils.isEmpty(d())) {
            intent.putExtra("authAccount", d());
            intent.putExtra("allowChangeAccount", false);
        }
        intent.setFlags(67108864);
        startActivity(intent);
        finish();
    }

    public e a(c cVar) {
        return new e(cVar);
    }

    public void a(Dialog dialog) {
        if (dialog != null) {
            synchronized (this.g) {
                this.g.add(dialog);
            }
        }
    }

    protected void onDestroy() {
        g();
        super.onDestroy();
    }

    private void g() {
        synchronized (this.g) {
            int size = this.g.size();
            for (int i = 0; i < size; i++) {
                Dialog dialog = (Dialog) this.g.get(i);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    a.b("BaseActivity", "dimiss dialog = " + dialog.toString());
                }
            }
        }
    }

    protected void a(boolean z) {
        this.d = z;
    }

    protected void b(boolean z) {
        this.e = z;
    }

    public synchronized void a(String str) {
        if (p.e(str)) {
            str = getString(m.a(this, "CS_waiting_progress_message"));
        }
        int c = j.c(this);
        a.a("BaseActivity", "oobe Login, showRequestProgressDialog theme id is " + c);
        if (this.f == null) {
            if (c != 0) {
                if (com.huawei.hwid.core.a.a.a()) {
                    this.f = new a(this, this, c);
                    this.f.setCanceledOnTouchOutside(false);
                    this.f.setMessage(str);
                    a(this.f);
                }
            }
            this.f = new b(this, this);
            this.f.setCanceledOnTouchOutside(false);
            this.f.setMessage(str);
            a(this.f);
        }
        a.a("BaseActivity", "this.isFinishing():" + isFinishing());
        if (!(this.f.isShowing() || isFinishing())) {
            this.f.setMessage(str);
            this.f.show();
        }
    }

    private boolean a(int i, KeyEvent keyEvent) {
        if ((4 == i && !this.d) || 84 == i) {
            return true;
        }
        if (this.d) {
            finish();
        }
        return false;
    }

    public synchronized void b() {
        a.b("BaseActivity", "dismissRequestProgressDialog");
        if (this.f != null) {
            if (this.f.isShowing()) {
                this.f.dismiss();
            }
        }
    }

    public void setContentView(int i) {
        int c = j.c(this);
        if (c != 0) {
            setTheme(c);
        }
        if (j.a || this.b == 0 || this.c == 0) {
            try {
                super.setContentView(i);
            } catch (IllegalStateException e) {
                a.d("BaseActivity", e.toString());
            } catch (Exception e2) {
                a.d("BaseActivity", e2.toString());
            }
            if (j.a && this.i) {
                try {
                    ActionBar actionBar = getActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                        if (this.b != 0) {
                            actionBar.setTitle(this.b);
                            return;
                        }
                        return;
                    }
                    return;
                } catch (Throwable e3) {
                    a.c("BaseActivity", e3.toString(), e3);
                    return;
                }
            }
            return;
        }
        super.setContentView(i);
    }

    protected void c(boolean z) {
        this.i = z;
    }

    protected void a(int i, int i2) {
        this.b = i;
        this.c = i2;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        if (this.i) {
            onBackPressed();
        }
        return true;
    }

    public String c() {
        String str = "";
        if (a.containsKey("userId")) {
            return (String) a.get("userId");
        }
        return str;
    }

    public String d() {
        String str = "";
        if (a.containsKey("accountName")) {
            return (String) a.get("accountName");
        }
        return str;
    }

    public String e() {
        String str = "";
        if (a.containsKey("authToken")) {
            return (String) a.get("authToken");
        }
        return str;
    }

    public void a(HwAccount hwAccount) {
        Object c = hwAccount.c();
        String a = hwAccount.a();
        String f = hwAccount.f();
        String b = hwAccount.b();
        if (p.e(c)) {
            c = "";
        }
        a.put("userId", c);
        if (p.e(a)) {
            c = "";
        } else {
            c = a;
        }
        a.put("accountName", c);
        if (d.h((Context) this) && !"com.huawei.hwid".equals(b)) {
            a.put("authToken", d.b(f, b));
            return;
        }
        a.put("authToken", f);
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        synchronized (this) {
            this.h = false;
            super.onActivityResult(i, i2, intent);
        }
    }

    public synchronized void startActivityForResult(Intent intent, int i) {
        if ((intent.getFlags() & 268435456) == 0) {
            if (!this.h) {
                this.h = true;
            } else {
                return;
            }
        }
        super.startActivityForResult(intent, i);
    }

    public void startActivity(Intent intent) {
        startActivityForResult(intent, 0);
    }

    protected boolean f() {
        return com.huawei.hwid.core.a.a.b();
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("BaseActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
