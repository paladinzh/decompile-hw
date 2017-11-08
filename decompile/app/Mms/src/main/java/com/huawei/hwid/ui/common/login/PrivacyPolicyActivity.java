package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.model.http.request.g;
import com.huawei.hwid.ui.common.BaseActivity;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PrivacyPolicyActivity extends BaseActivity {
    private static OnLongClickListener o = new w();
    Thread a = null;
    OnClickListener b = new x(this);
    OnClickListener c = new y(this);
    private LinearLayout d = null;
    private LinearLayout e = null;
    private WebView f = null;
    private Button g = null;
    private Button h = null;
    private String i = "";
    private WebSettings j;
    private boolean k = false;
    private String l;
    private String m = "";
    private String n = "";

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (!d.q(this)) {
            setRequestedOrientation(1);
        }
        if (getIntent() != null) {
            this.k = getIntent().getBooleanExtra("isEmotionIntroduce", false);
            Object stringExtra = getIntent().getStringExtra("privacyType");
            if (stringExtra == null || TextUtils.isEmpty(stringExtra)) {
                this.l = getIntent().getDataString();
            } else {
                this.l = stringExtra;
            }
            if (!b(this.l)) {
                this.l = "1";
            }
            this.m = i.c(this, this.l);
            this.i = d.i((Context) this);
            c(true);
            if (f()) {
                requestWindowFeature(1);
                setContentView(m.d(this, "oobe_privacy_policy"));
                ((Button) findViewById(m.e(this, "Btn_back"))).setOnClickListener(new v(this));
                TextView textView = (TextView) findViewById(m.e(this, "textview"));
                if ("2".equals(this.l)) {
                    textView.setText(m.a(this, "CS_hwid_policy_new"));
                } else if ("0".equals(this.l)) {
                    textView.setText(m.a(this, "CS_hwid_terms_new"));
                } else if ("6".equals(this.l)) {
                    textView.setText(m.a(this, "vip_member_items_new"));
                } else {
                    textView.setText(m.a(this, "CS_hwid_terms_and_policy_new"));
                }
            } else {
                if ("2".equals(this.l)) {
                    a(m.a(this, "CS_hwid_policy_new"), m.g(this, "cs_actionbar_icon"));
                } else if ("0".equals(this.l)) {
                    a(m.a(this, "CS_hwid_terms_new"), m.g(this, "cs_actionbar_icon"));
                } else if ("6".equals(this.l)) {
                    a(m.a(this, "vip_member_items_new"), m.g(this, "cs_actionbar_icon"));
                } else {
                    a(m.a(this, "CS_hwid_terms_and_policy_new"), m.g(this, "cs_actionbar_icon"));
                }
                setContentView(m.d(this, "cs_privacy_policy"));
            }
            i();
            return;
        }
        a.b("PrivacyPolicyActivity", "intent is null");
        finish();
    }

    private boolean b(String str) {
        if (str != null) {
            if ("2".equals(str) || "0".equals(str) || "1".equals(str) || "6".equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void i() {
        this.e = (LinearLayout) findViewById(m.e(this, "retry_lay"));
        this.d = (LinearLayout) findViewById(m.e(this, "content_lay"));
        this.g = (Button) findViewById(m.e(this, "btn_ok"));
        this.g.setOnClickListener(this.b);
        this.h = (Button) findViewById(m.e(this, "Btn_retry"));
        this.h.setOnClickListener(this.c);
        this.f = (WebView) findViewById(m.e(this, "webview"));
        this.j = this.f.getSettings();
        this.j.setSavePassword(false);
        this.j.setCacheMode(1);
        if (this.k) {
            this.f.setOnLongClickListener(o);
        }
        this.f.setBackgroundColor(0);
        this.d.setVisibility(4);
        this.e.setVisibility(8);
        h();
    }

    void g() {
        Bundle bundle = new Bundle();
        bundle.putString("termsOrPolicy", this.l);
        Object l = l();
        if (!TextUtils.isEmpty(l)) {
            bundle.putString("agreeVersion", l);
        }
        com.huawei.hwid.core.model.http.i.a((Context) this, new g(this, bundle), null, a(new ac(this, this)));
        a(null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean c(String str, String str2, String str3) {
        Throwable e;
        ZipInputStream zipInputStream;
        FileInputStream fileInputStream;
        BufferedOutputStream bufferedOutputStream;
        OutputStream outputStream;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream2;
        ZipInputStream zipInputStream2;
        BufferedOutputStream bufferedOutputStream2;
        try {
            File file = new File(str);
            if (!file.exists()) {
                if (!new File(file.getParent()).mkdirs()) {
                    return false;
                }
            }
            fileInputStream2 = new FileInputStream(str);
            try {
                zipInputStream2 = new ZipInputStream(new BufferedInputStream(fileInputStream2));
                bufferedOutputStream2 = null;
                while (true) {
                    try {
                        ZipEntry nextEntry = zipInputStream2.getNextEntry();
                        if (nextEntry == null) {
                            break;
                        }
                        a.b("Unzip: ", "=" + nextEntry);
                        byte[] bArr = new byte[4096];
                        File file2 = new File(str2 + str3 + ".html");
                        File file3 = new File(file2.getParent());
                        if (!file3.exists()) {
                            if (!file3.mkdirs()) {
                                break;
                            }
                        }
                        OutputStream fileOutputStream2 = new FileOutputStream(file2);
                        try {
                            BufferedOutputStream bufferedOutputStream3 = new BufferedOutputStream(fileOutputStream2, 4096);
                            while (true) {
                                try {
                                    int read = zipInputStream2.read(bArr, 0, 4096);
                                    if (read == -1) {
                                        break;
                                    }
                                    bufferedOutputStream3.write(bArr, 0, read);
                                } catch (IOException e2) {
                                    e = e2;
                                    OutputStream outputStream2 = fileOutputStream2;
                                    zipInputStream = zipInputStream2;
                                    fileInputStream = fileInputStream2;
                                    bufferedOutputStream = bufferedOutputStream3;
                                    fileOutputStream = outputStream2;
                                } catch (Exception e3) {
                                    e = e3;
                                    bufferedOutputStream2 = bufferedOutputStream3;
                                    outputStream = fileOutputStream2;
                                } catch (Throwable th) {
                                    e = th;
                                    bufferedOutputStream2 = bufferedOutputStream3;
                                    outputStream = fileOutputStream2;
                                }
                            }
                            bufferedOutputStream3.flush();
                            fileOutputStream2.close();
                            bufferedOutputStream3.close();
                            bufferedOutputStream2 = bufferedOutputStream3;
                            outputStream = fileOutputStream2;
                        } catch (IOException e4) {
                            e = e4;
                            outputStream = fileOutputStream2;
                            zipInputStream = zipInputStream2;
                            fileInputStream = fileInputStream2;
                            bufferedOutputStream = bufferedOutputStream2;
                        } catch (Exception e5) {
                            e = e5;
                            outputStream = fileOutputStream2;
                        } catch (Throwable th2) {
                            e = th2;
                            outputStream = fileOutputStream2;
                        }
                    } catch (IOException e6) {
                        e = e6;
                        zipInputStream = zipInputStream2;
                        fileInputStream = fileInputStream2;
                        bufferedOutputStream = bufferedOutputStream2;
                    } catch (Exception e7) {
                        e = e7;
                    }
                }
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (Throwable e8) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e8.toString(), e8);
                    }
                }
                if (bufferedOutputStream2 != null) {
                    try {
                        bufferedOutputStream2.close();
                    } catch (Throwable e82) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e82.toString(), e82);
                    }
                }
                if (zipInputStream2 != null) {
                    try {
                        zipInputStream2.close();
                    } catch (Throwable e822) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e822.toString(), e822);
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e8222) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e8222.toString(), e8222);
                    }
                }
                return false;
                if (bufferedOutputStream2 != null) {
                    try {
                        bufferedOutputStream2.close();
                    } catch (Throwable e82222) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e82222.toString(), e82222);
                    }
                }
                if (zipInputStream2 != null) {
                    try {
                        zipInputStream2.close();
                    } catch (Throwable e822222) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e822222.toString(), e822222);
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e8222222) {
                        a.d("PrivacyPolicyActivity", "IOException / " + e8222222.toString(), e8222222);
                    }
                }
                return true;
                if (zipInputStream2 != null) {
                    zipInputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return true;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return true;
                return true;
            } catch (IOException e9) {
                e8222222 = e9;
                zipInputStream = null;
                fileInputStream = fileInputStream2;
                bufferedOutputStream = null;
                try {
                    a.d("PrivacyPolicyActivity", "IOException / " + e8222222.toString(), e8222222);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e82222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e82222222.toString(), e82222222);
                        }
                    }
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (Throwable e822222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e822222222.toString(), e822222222);
                        }
                    }
                    if (zipInputStream != null) {
                        try {
                            zipInputStream.close();
                        } catch (Throwable e8222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e8222222222.toString(), e8222222222);
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e82222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e82222222222.toString(), e82222222222);
                        }
                    }
                    return false;
                } catch (Throwable th3) {
                    e82222222222 = th3;
                    bufferedOutputStream2 = bufferedOutputStream;
                    fileInputStream2 = fileInputStream;
                    zipInputStream2 = zipInputStream;
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (Throwable e10) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e10.toString(), e10);
                        }
                    }
                    if (bufferedOutputStream2 != null) {
                        try {
                            bufferedOutputStream2.close();
                        } catch (Throwable e102) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e102.toString(), e102);
                        }
                    }
                    if (zipInputStream2 != null) {
                        try {
                            zipInputStream2.close();
                        } catch (Throwable e1022) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e1022.toString(), e1022);
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e11) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e11.toString(), e11);
                        }
                    }
                    throw e82222222222;
                }
            } catch (Exception e12) {
                e82222222222 = e12;
                zipInputStream2 = null;
                bufferedOutputStream2 = null;
                try {
                    a.d("PrivacyPolicyActivity", "Exception / " + e82222222222.toString(), e82222222222);
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (Throwable e822222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e822222222222.toString(), e822222222222);
                        }
                    }
                    if (bufferedOutputStream2 != null) {
                        try {
                            bufferedOutputStream2.close();
                        } catch (Throwable e8222222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e8222222222222.toString(), e8222222222222);
                        }
                    }
                    if (zipInputStream2 != null) {
                        try {
                            zipInputStream2.close();
                        } catch (Throwable e82222222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e82222222222222.toString(), e82222222222222);
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e822222222222222) {
                            a.d("PrivacyPolicyActivity", "IOException / " + e822222222222222.toString(), e822222222222222);
                        }
                    }
                    return false;
                } catch (Throwable th4) {
                    e822222222222222 = th4;
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                    if (bufferedOutputStream2 != null) {
                        bufferedOutputStream2.close();
                    }
                    if (zipInputStream2 != null) {
                        zipInputStream2.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw e822222222222222;
                }
            } catch (Throwable th5) {
                e822222222222222 = th5;
                zipInputStream2 = null;
                bufferedOutputStream2 = null;
                if (fileInputStream2 != null) {
                    fileInputStream2.close();
                }
                if (bufferedOutputStream2 != null) {
                    bufferedOutputStream2.close();
                }
                if (zipInputStream2 != null) {
                    zipInputStream2.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw e822222222222222;
            }
        } catch (IOException e13) {
            e822222222222222 = e13;
            zipInputStream = null;
            fileInputStream = null;
            bufferedOutputStream = null;
            a.d("PrivacyPolicyActivity", "IOException / " + e822222222222222.toString(), e822222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (zipInputStream != null) {
                zipInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (Exception e14) {
            e822222222222222 = e14;
            zipInputStream2 = null;
            fileInputStream2 = null;
            bufferedOutputStream2 = null;
            a.d("PrivacyPolicyActivity", "Exception / " + e822222222222222.toString(), e822222222222222);
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (bufferedOutputStream2 != null) {
                bufferedOutputStream2.close();
            }
            if (zipInputStream2 != null) {
                zipInputStream2.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (Throwable th6) {
            e822222222222222 = th6;
            zipInputStream2 = null;
            fileInputStream2 = null;
            bufferedOutputStream2 = null;
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (bufferedOutputStream2 != null) {
                bufferedOutputStream2.close();
            }
            if (zipInputStream2 != null) {
                zipInputStream2.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw e822222222222222;
        }
    }

    protected void onDestroy() {
        k();
        if (this.a != null && this.a.isAlive()) {
            this.a.interrupt();
        }
        super.onDestroy();
    }

    void h() {
        a(this.i, this.m, ".zip");
        g();
        this.e.setVisibility(8);
    }

    private void j() {
        File a = a(this.i, this.m);
        if (a != null) {
            this.d.setVisibility(4);
            if (com.huawei.hwid.core.a.a.a()) {
                a.b("PrivacyPolicyActivity", "this is  oversea OOBE");
                this.f.getSettings().setJavaScriptEnabled(true);
                this.f.loadUrl("file:///" + a.getAbsolutePath() + "?theme=white");
            } else {
                this.f.loadUrl("file:///" + a.getAbsolutePath());
            }
            this.f.setWebViewClient(new z(this));
        }
    }

    private void k() {
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
            if (this.f != null) {
                this.f.stopLoading();
                this.f.freeMemory();
            }
        } catch (Exception e) {
            a.d("PrivacyPolicyActivity", "call clearWebviewData err:" + e.toString());
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 4) {
            finish();
        }
        return super.onKeyDown(i, keyEvent);
    }

    private File a(String str, String str2) {
        File[] listFiles = new File(str).listFiles(new aa(this, str2));
        if (listFiles != null && listFiles.length > 0) {
            return listFiles[0];
        }
        return null;
    }

    private String l() {
        File a = a(this.i, this.m);
        if (a == null) {
            return null;
        }
        String name = a.getName();
        if (!name.endsWith(".html")) {
            return null;
        }
        String[] split = name.substring(0, name.indexOf(".html")).split("-");
        if (split != null && split.length >= 4) {
            return split[3];
        }
        return null;
    }

    public void a(String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || TextUtils.isEmpty(str3)) {
            a.d("PrivacyPolicyActivity", "param error when deleteZipHtmlFile");
            return;
        }
        File[] listFiles = new File(str).listFiles(new ab(this, str2, str3));
        if (listFiles != null && listFiles.length > 0) {
            for (File a : listFiles) {
                i.a(a);
            }
        }
    }

    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable e) {
            a.d("PrivacyPolicyActivity", "catch Exception throw by FragmentManager!", e);
        }
    }
}
