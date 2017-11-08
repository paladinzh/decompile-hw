package com.huawei.hwid.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import com.huawei.hwid.core.d.b.e;
import java.io.File;

public final class h {
    private static h a;

    private static class a implements Runnable {
        private String a = null;
        private Context b = null;

        a(Context context, String str) {
            this.b = context;
            this.a = str;
        }

        public void run() {
            if (this.b != null) {
                try {
                    File file = new File(this.a);
                    if (VERSION.SDK_INT > 23) {
                        if (j.b(this.b)) {
                            a(file);
                            Thread.sleep(500);
                            return;
                        }
                    }
                    b(file);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.d("InstallProcessor", "Thread.sleep error");
                } catch (Exception e2) {
                    e.d("InstallProcessor", "install exception: " + e2.getMessage());
                }
                return;
            }
            e.b("InstallProcessor", "mContext is null");
        }

        private void a(File file) {
            e.b("InstallProcessor", "installFromProvider");
            Intent data = new Intent("android.intent.action.INSTALL_PACKAGE").setData(OtaFileProvider.a(this.b, this.b.getPackageName() + ".hwid.sdk.otafileprovider", file));
            data.setFlags(1);
            this.b.startActivity(data);
        }

        private void b(File file) {
            e.b("InstallProcessor", "installFromFile");
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(268435456);
            this.b.startActivity(intent);
        }
    }

    private h() {
    }

    public static synchronized h a() {
        h hVar;
        synchronized (h.class) {
            if (a == null) {
                a = new h();
            }
            hVar = a;
        }
        return hVar;
    }

    public a a(Context context, String str) {
        Object aVar = new a(context, str);
        new Thread(aVar, "InstallUpdateThread").start();
        return aVar;
    }
}
