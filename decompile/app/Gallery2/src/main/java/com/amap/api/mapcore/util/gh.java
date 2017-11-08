package com.amap.api.mapcore.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.amap.api.mapcore.util.gz.a;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/* compiled from: DexDownLoad */
public class gh extends Thread implements a {
    private gi a;
    private gz b;
    private fh c;
    private String d;
    private RandomAccessFile e;
    private Context f;

    public gh(Context context, gi giVar, fh fhVar) {
        try {
            this.f = context.getApplicationContext();
            this.c = fhVar;
            if (giVar != null) {
                this.a = giVar;
                this.b = new gz(new gm(this.a));
                this.d = gn.a(context, this.a.a);
            }
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "DexDownLoad()");
        }
    }

    public void a() {
        try {
            start();
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "startDownload()");
        }
    }

    public void run() {
        try {
            if (c()) {
                hi hiVar = new hi(this.f, this.c.a(), this.c.b(), "O008");
                hiVar.a("{\"param_int_first\":0}");
                hj.a(hiVar, this.f);
                this.b.a(this);
            }
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "run()");
        }
    }

    private boolean a(fu fuVar, gr grVar, gi giVar) {
        String str = giVar.b;
        String str2 = giVar.c;
        String str3 = giVar.d;
        String str4 = giVar.e;
        if ("errorstatus".equals(grVar.f())) {
            b(fuVar);
            return true;
        } else if (!new File(this.d).exists()) {
            return false;
        } else {
            List b = fuVar.b(gr.a(gn.a(this.f, str, str2), str, str2, str3), gr.class);
            if (b != null && b.size() > 0) {
                return true;
            }
            try {
                gn.a(this.f, str, this.c.b());
                gn.a(this.f, fuVar, this.c, this.d, str3);
                gn.a(this.f, this.c);
            } catch (Throwable th) {
                gs.a(th, "dDownLoad", "processDownloadedFile()");
            }
            return true;
        }
    }

    private boolean a(fu fuVar) {
        try {
            List a = gn.a.a(fuVar, this.a.b, "used");
            if (a != null && a.size() > 0 && gs.a(((gr) a.get(0)).e(), this.a.d) > 0) {
                return true;
            }
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "isUsed()");
        }
        return false;
    }

    private boolean f() {
        fu fuVar = new fu(this.f, gq.a());
        if (a(fuVar)) {
            return true;
        }
        gr a = gn.a.a(fuVar, this.a.a);
        if (a == null) {
            return false;
        }
        return a(fuVar, a, this.a);
    }

    boolean b() {
        boolean z = false;
        if (this.c == null) {
            return false;
        }
        if (this.c.a().equals(this.a.b) && this.c.b().equals(this.a.c)) {
            z = true;
        }
        return z;
    }

    private boolean g() {
        return VERSION.SDK_INT >= this.a.g && VERSION.SDK_INT <= this.a.f;
    }

    private boolean a(Context context) {
        return fc.m(context) == 1;
    }

    boolean c() {
        try {
            if (!b() || !g() || !a(this.f) || f()) {
                return false;
            }
            gn.b(this.f, this.c.a());
            return true;
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "isNeedDownload()");
            return false;
        }
    }

    public void a(byte[] bArr, long j) {
        try {
            if (this.e == null) {
                File file = new File(this.d);
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                this.e = new RandomAccessFile(file, "rw");
            }
            this.e.seek(j);
            this.e.write(bArr);
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "onDownload()");
        }
    }

    public void a(Throwable th) {
        try {
            gs.a(this.e);
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
    }

    public void e() {
        try {
            if (this.e != null) {
                gs.a(this.e);
                String b = this.a.b();
                if (gs.b(this.d, b)) {
                    a(b);
                    hi hiVar = new hi(this.f, this.c.a(), this.c.b(), "O008");
                    hiVar.a("{\"param_int_first\":1}");
                    hj.a(hiVar, this.f);
                }
                new File(this.d).delete();
            }
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "onFinish()");
        }
    }

    public void d() {
    }

    private void b(fu fuVar) {
        if (!new File(gn.b(this.f, this.c.a(), this.c.b())).exists() && !TextUtils.isEmpty(gn.a(this.f, fuVar, this.c))) {
            try {
                gn.a(this.f, this.c);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private void a(String str) {
        String c = this.a.c();
        fu fuVar = new fu(this.f, gq.a());
        gn.a.a(fuVar, new gr.a(this.a.a, str, this.a.b, c, this.a.d).a("copy").a(), gr.a(this.a.a, this.a.b, c, this.a.d));
        a(this.f, this.a.b);
        try {
            gn.a(this.f, fuVar, this.c, this.d, this.a.d);
            gn.a(this.f, this.c);
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "onFinish1");
        }
    }

    private void a(Context context, String str) {
        try {
            Editor edit = context.getSharedPreferences(str, 0).edit();
            edit.clear();
            edit.commit();
        } catch (Throwable th) {
            gs.a(th, "dDownLoad", "clearMarker()");
        }
    }
}
