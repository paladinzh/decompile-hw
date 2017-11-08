package com.huawei.hwid.api.common.apkimpl;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.huawei.hwid.api.common.v;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.i;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.core.d.m;
import com.huawei.hwid.update.d;
import com.huawei.hwid.update.h;
import com.huawei.hwid.update.j;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class OtaDownloadActivity extends Activity {
    TextView a;
    ProgressBar b;
    ImageView c;
    private com.huawei.hwid.c.a.a.a d;
    private com.huawei.hwid.c.a.a.a e;
    private com.huawei.hwid.c.a.a.a f;
    private int g = -1;
    private boolean h = false;
    private boolean i = false;
    private ProgressDialog j;
    private boolean k = false;
    private ArrayList<Dialog> l = new ArrayList();

    public class a extends com.huawei.hwid.update.a {
        final /* synthetic */ OtaDownloadActivity a;

        public a(OtaDownloadActivity otaDownloadActivity) {
            this.a = otaDownloadActivity;
        }

        public void a(int i, Map<Integer, com.huawei.hwid.update.a.b> map) {
            e.b("OtaDownloadActivity", "handleCheckFailedstausCode = " + i);
            this.a.b();
            com.huawei.hwid.update.e.a().a(false);
            com.huawei.hwid.update.e.a().c();
            this.a.k();
        }

        public void a(Map<Integer, com.huawei.hwid.update.a.b> map) {
            this.a.b();
            com.huawei.hwid.update.e.a().a(false);
            com.huawei.hwid.update.e.a().a((Map) map);
            com.huawei.hwid.update.a.b b = com.huawei.hwid.update.e.a().b(this.a.g);
            if (b != null) {
                if (this.a.i) {
                    if (!TextUtils.isEmpty(b.a())) {
                        int intValue;
                        try {
                            intValue = Integer.valueOf(b.a()).intValue();
                        } catch (Exception e) {
                            e.b("OtaDownloadActivity", "Exception = " + e.getMessage());
                            intValue = 0;
                        }
                        if (v.d(this.a) >= intValue) {
                            e.b("OtaDownloadActivity", "newest version");
                            this.a.finish();
                            return;
                        }
                    }
                    this.a.d();
                } else {
                    this.a.m();
                }
                return;
            }
            e.b("OtaDownloadActivity", "vInfo is null");
            this.a.finish();
        }
    }

    public class b extends d {
        final /* synthetic */ OtaDownloadActivity a;

        public b(OtaDownloadActivity otaDownloadActivity) {
            this.a = otaDownloadActivity;
        }

        public void a(int i, int i2) {
            this.a.a(i, i2);
        }

        public void a(int i) {
            this.a.g();
        }

        public void a() {
            this.a.h();
            if (VERSION.SDK_INT > 23 && j.b(this.a) && this.a.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                this.a.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 10002);
                return;
            }
            this.a.i();
            this.a.l();
        }
    }

    public enum c {
        NoNewVersion,
        NoStoragePermission,
        NewVersionIsReady
    }

    @TargetApi(23)
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        e.b("OtaDownloadActivity", "onCreate");
        requestWindowFeature(1);
        if (com.huawei.hwid.core.d.b.g()) {
            com.huawei.hwid.core.d.b.a((Activity) this);
        }
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                this.h = extras.getBoolean("updateApk");
                this.i = extras.getBoolean("updateHighApk");
                e.b("OtaDownloadActivity", "mIsUpdateHighApk = " + this.i);
            }
            if (!this.i) {
                d();
            } else if (VERSION.SDK_INT > 22 && checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
                e.b("OtaDownloadActivity", "have not permission READ_PHONE_STATE");
                requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 10003);
            } else {
                a(new a(this));
            }
            return;
        }
        finish();
    }

    private void b(String str) {
        this.d.setButton(-1, str, new e(this));
    }

    private void d() {
        String string;
        this.d = new com.huawei.hwid.c.a.a.a(this);
        this.d.setCanceledOnTouchOutside(false);
        if (this.h) {
            this.d.setTitle(com.huawei.hwid.core.d.j.a(this, "CS_update_hwid"));
            this.d.setMessage(getString(com.huawei.hwid.core.d.j.a(this, "CS_update_old_hwid_notes")));
            string = getString(com.huawei.hwid.core.d.j.a(this, "CS_update"));
        } else {
            this.d.setTitle(com.huawei.hwid.core.d.j.a(this, "CS_install_hwid"));
            this.d.setMessage(getString(com.huawei.hwid.core.d.j.a(this, "CS_update_notes")));
            string = getString(com.huawei.hwid.core.d.j.a(this, "CS_install"));
        }
        b(string);
        this.d.setButton(-2, getString(17039360), new j(this));
        this.d.setOnKeyListener(new k(this));
        if (!isFinishing() && !this.d.isShowing()) {
            this.d.show();
        }
    }

    protected void onDestroy() {
        e.b("OtaDownloadActivity", "onDestroy");
        c();
        super.onDestroy();
        if (this.d != null) {
            this.d.a(true);
            this.d.dismiss();
            this.d = null;
        }
        if (this.e != null) {
            this.e.dismiss();
            this.e = null;
        }
        if (this.f != null) {
            this.f.dismiss();
            this.f = null;
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 10003) {
            b(iArr);
        } else if (i == 10002) {
            a(iArr);
        } else if (i == 10007) {
            if (iArr != null && iArr.length > 0 && iArr[0] == 0 && c.NewVersionIsReady == a()) {
                l();
            } else {
                e.b("OtaDownloadActivity", "MY_PERMISSIONS_REQUEST_OTA_NEW_VERSION_READY_STORAGE failed");
                finish();
            }
        }
    }

    private void a(int[] iArr) {
        if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
            e.b("OtaDownloadActivity", "startInstallVersion");
            i();
            l();
            return;
        }
        e.b("OtaDownloadActivity", "MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE failed");
        finish();
    }

    private void b(int[] iArr) {
        if (iArr != null && iArr.length > 0 && iArr[0] == 0) {
            e.b("OtaDownloadActivity", "startCheckVersion");
            a(new a(this));
            return;
        }
        finish();
    }

    public void a(a aVar) {
        e.b("OtaDownloadActivity", "startCheckVersion");
        if (!com.huawei.hwid.core.d.b.a((Context) this)) {
            a(m.a((Context) this, com.huawei.hwid.core.d.j.a(this, "CS_network_connect_error"), com.huawei.hwid.core.d.j.a(this, "CS_server_unavailable_title"), false).show());
        } else if (com.huawei.hwid.update.e.a().b()) {
            e.b("OtaDownloadActivity", "OtaDownloadManager.getInstance().isMutiDownloading()");
        } else {
            e.b("OtaDownloadActivity", "mIsUpdateApk = " + this.h);
            if (this.h) {
                String toUpperCase = v.c(this).toUpperCase(Locale.ENGLISH);
                if (toUpperCase.endsWith("OVE") || toUpperCase.endsWith("EU")) {
                    this.g = 49846;
                } else if (!v.a((Context) this, 20101302)) {
                    this.g = 49827;
                } else if (e()) {
                    this.g = 49827;
                } else {
                    this.g = 49846;
                }
            } else if (e()) {
                this.g = 49827;
            } else {
                this.g = 49846;
            }
            a(null);
            com.huawei.hwid.update.e.a().a((Context) this, this.g, (Handler) aVar);
        }
    }

    private boolean e() {
        return i.a() || "cn".equalsIgnoreCase(com.huawei.hwid.core.d.b.f((Context) this)) || l.a((Context) this, -999).startsWith("460");
    }

    public void a(b bVar) {
        e.b("OtaDownloadActivity", "entry startDownload");
        com.huawei.hwid.update.a.b b = com.huawei.hwid.update.e.a().b(this.g);
        if (b == null) {
            e.b("OtaDownloadActivity", "versionInfo == null");
            finish();
        } else if (this.i && !com.huawei.hwid.core.d.b.a((Context) this)) {
            a(m.a((Context) this, com.huawei.hwid.core.d.j.a(this, "CS_network_connect_error"), com.huawei.hwid.core.d.j.a(this, "CS_server_unavailable_title"), false).show());
        } else {
            if (this.h) {
                Object a = b.a();
                if (!TextUtils.isEmpty(a)) {
                    try {
                        if (v.d(this) >= Integer.valueOf(a).intValue()) {
                            e.b("OtaDownloadActivity", "local version is newest");
                            finish();
                            return;
                        }
                    } catch (Exception e) {
                        e.b("versionCode", "e = " + e.getMessage());
                    }
                }
            }
            if (j.a((Context) this, (long) b.c())) {
                e.b("OtaDownloadActivity", "start startDownloadVersion");
                com.huawei.hwid.update.e.a().a((Context) this, (Handler) bVar, this.g);
                return;
            }
            e.b("OtaDownloadActivity", "!OtaUtils.isEnoughSpaceToDown");
            Toast.makeText(this, getString(com.huawei.hwid.core.d.j.a(this, "CS_download_no_space")), 0).show();
        }
    }

    private void a(int i, int i2) {
        if (this.e == null) {
            View inflate;
            this.e = new com.huawei.hwid.c.a.a.a(this);
            this.e.setCanceledOnTouchOutside(false);
            if (com.huawei.hwid.core.d.b.o(this)) {
                inflate = View.inflate(this, com.huawei.hwid.core.d.j.d(this, "cs_download_progress_dialog_3"), null);
            } else {
                inflate = View.inflate(this, com.huawei.hwid.core.d.j.d(this, "cs_download_progress_dialog"), null);
            }
            this.a = (TextView) inflate.findViewById(com.huawei.hwid.core.d.j.e(this, "information"));
            this.b = (ProgressBar) inflate.findViewById(com.huawei.hwid.core.d.j.e(this, "progressbar"));
            this.c = (ImageView) inflate.findViewById(com.huawei.hwid.core.d.j.e(this, "cancel_download"));
            this.e.setView(inflate);
            if (this.c != null) {
                this.c.setOnClickListener(new l(this));
            }
            this.e.setOnKeyListener(new m(this));
        }
        if (!(isFinishing() || this.e.isShowing())) {
            this.d.a(true);
            this.d.dismiss();
            this.d = null;
            this.e.show();
        }
        b(i, i2);
    }

    private void f() {
        Dialog create = new Builder(this, m.a(this)).setMessage(com.huawei.hwid.core.d.j.a(this, "CS_update_stop")).setPositiveButton(com.huawei.hwid.core.d.j.a(this, "CS_terminate"), new n(this)).setNegativeButton(17039360, null).create();
        if (!isFinishing() && !create.isShowing()) {
            a(create);
            create.show();
        }
    }

    private void g() {
        this.f = new com.huawei.hwid.c.a.a.a(this);
        this.f.setMessage(getString(com.huawei.hwid.core.d.j.a(this, "CS_download_failed_notes")));
        this.f.setButton(-1, getString(com.huawei.hwid.core.d.j.a(this, "CS_retry")), new o(this));
        this.f.setButton(-2, getString(17039360), new p(this));
        this.f.setOnKeyListener(new q(this));
        if (!isFinishing() && !this.f.isShowing()) {
            this.f.show();
        }
    }

    private void b(int i, int i2) {
        int i3 = (i * 100) / i2;
        e.b("OtaDownloadActivity", "progress: " + i3);
        if (this.a != null) {
            this.a.setText(getString(com.huawei.hwid.core.d.j.a(this, "CS_downloading_new"), new Object[]{String.valueOf(i3)}));
        }
        if (this.b != null) {
            this.b.setProgress(i3);
        }
    }

    private void h() {
        this.e.dismiss();
        this.e = null;
    }

    private void i() {
        com.huawei.hwid.update.a.b b = com.huawei.hwid.update.e.a().b(this.g);
        if (b == null) {
            e.b("OtaDownloadActivity", "versionInfo is null");
        } else {
            h.a().a(this, b.f());
        }
    }

    private void j() {
        com.huawei.hwid.update.e.a().e();
        a(com.huawei.hwid.update.e.a().b(this.g));
        com.huawei.hwid.update.e.a().a(this.g);
        finish();
    }

    public c a() {
        com.huawei.hwid.update.a.b b = com.huawei.hwid.update.e.a().b(this.g);
        if (b == null) {
            return c.NoNewVersion;
        }
        String b2 = com.huawei.hwid.update.i.a(this).b(this);
        String c = com.huawei.hwid.update.i.a(this).c(this);
        if ("".equals(c)) {
            return c.NoNewVersion;
        }
        File file = new File(c);
        if (!file.exists()) {
            return c.NoNewVersion;
        }
        if (!b2.equals(b.b())) {
            try {
                if (!file.delete()) {
                    e.d("OtaDownloadActivity", "delete old apk error");
                }
            } catch (Exception e) {
                e.d("OtaDownloadActivity", "delete old apk error,error is " + e.getMessage());
            }
            return c.NoNewVersion;
        } else if (VERSION.SDK_INT > 23 && j.b(this) && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            e.b("OtaDownloadActivity", "have not permission WRITE_EXTERNAL_STORAGE");
            return c.NoStoragePermission;
        } else {
            e.b("OtaDownloadActivity", "startInstallVersion");
            h.a().a(this, c);
            return c.NewVersionIsReady;
        }
    }

    public void a(com.huawei.hwid.update.a.b bVar) {
        if (bVar != null) {
            String c = com.huawei.hwid.update.i.a(this).c(this);
            if (!"".equals(c)) {
                File file = new File(c);
                if (file.exists()) {
                    try {
                        if (!file.delete()) {
                            e.d("OtaDownloadActivity", "delete uninstallApk error");
                        }
                    } catch (Exception e) {
                        e.d("OtaDownloadActivity", "delete uninstallApk error, error is " + e.getMessage());
                    }
                }
            }
        }
    }

    private void k() {
        a(m.a((Context) this, com.huawei.hwid.core.d.j.a(this, "CS_ERR_for_unable_get_data"), com.huawei.hwid.core.d.j.a(this, "CS_server_unavailable_title"), true).show());
    }

    private void l() {
        new Handler().postDelayed(new f(this), 200);
    }

    public synchronized void a(String str) {
        if (TextUtils.isEmpty(str)) {
            str = getString(com.huawei.hwid.core.d.j.a(this, "CS_waiting_progress_message"));
        }
        int b = m.b(this);
        e.a("OtaDownloadActivity", "oobe Login, showRequestProgressDialog theme id is " + b);
        if (this.j != null) {
            e.a("OtaDownloadActivity", "mProgressDialog != null");
        } else {
            if (b != 0) {
                if (com.huawei.hwid.core.d.d.a()) {
                    this.j = new g(this, this, b);
                    this.j.setCanceledOnTouchOutside(false);
                    this.j.setMessage(str);
                    a(this.j);
                }
            }
            this.j = new h(this, this);
            this.j.setCanceledOnTouchOutside(false);
            this.j.setMessage(str);
            a(this.j);
        }
        e.a("OtaDownloadActivity", "this.isFinishing():" + isFinishing());
        if (!(this.j.isShowing() || isFinishing())) {
            this.j.setMessage(str);
            this.j.show();
        }
    }

    public synchronized void b() {
        e.b("OtaDownloadActivity", "dismissRequestProgressDialog");
        if (this.j != null) {
            if (this.j.isShowing()) {
                this.j.dismiss();
                this.j = null;
            }
        }
    }

    private boolean a(int i, KeyEvent keyEvent) {
        if ((4 == i && !this.k) || 84 == i) {
            return true;
        }
        if (this.k) {
            finish();
        }
        return false;
    }

    public void a(Dialog dialog) {
        if (dialog != null) {
            synchronized (this.l) {
                e.b("OtaDownloadActivity", "mManagedDialogList.size = " + this.l.size());
                this.l.add(dialog);
            }
        }
    }

    public void c() {
        synchronized (this.l) {
            int size = this.l.size();
            for (int i = 0; i < size; i++) {
                Dialog dialog = (Dialog) this.l.get(i);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            this.l.clear();
        }
    }

    private void m() {
        switch (i.a[a().ordinal()]) {
            case 1:
                a(new b(this));
                return;
            case 2:
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 10007);
                return;
            case 3:
                l();
                return;
            default:
                finish();
                return;
        }
    }
}
