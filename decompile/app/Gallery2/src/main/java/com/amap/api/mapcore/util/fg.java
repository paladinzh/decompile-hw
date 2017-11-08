package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;

/* compiled from: SDKCoordinatorDownload */
public class fg extends Thread implements com.amap.api.mapcore.util.gz.a {
    private static String h = "sodownload";
    private static String i = "sofail";
    private gz a = new gz(this.b);
    private a b;
    private RandomAccessFile c;
    private String d;
    private String e;
    private String f;
    private Context g;

    /* compiled from: SDKCoordinatorDownload */
    private static class a extends hd {
        private String a;

        a(String str) {
            this.a = str;
        }

        public Map<String, String> a() {
            return null;
        }

        public Map<String, String> b() {
            return null;
        }

        public String c() {
            return this.a;
        }
    }

    public fg(Context context, String str, String str2, String str3) {
        this.g = context;
        this.f = str3;
        this.d = a(context, str + "temp.so");
        this.e = a(context, "libwgs2gcj.so");
        this.b = new a(str2);
    }

    public static String a(Context context, String str) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "libso" + File.separator + str;
    }

    private static String b(Context context, String str) {
        return a(context, str);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a() {
        if (this.b != null && !TextUtils.isEmpty(this.b.c()) && this.b.c().contains("libJni_wgs2gcj.so") && this.b.c().contains(Build.CPU_ABI) && !new File(this.e).exists()) {
            start();
        }
    }

    public void run() {
        try {
            File file = new File(b(this.g, "tempfile"));
            if (file.exists()) {
                file.delete();
            }
            this.a.a(this);
        } catch (Throwable th) {
            fl.a(th, "SDKCoordinatorDownload", "run");
            b();
        }
    }

    public void a(byte[] bArr, long j) {
        try {
            if (this.c == null) {
                File file = new File(this.d);
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                this.c = new RandomAccessFile(file, "rw");
            }
        } catch (Throwable e) {
            fl.a(e, "SDKCoordinatorDownload", "onDownload");
            b();
        } catch (Throwable e2) {
            b();
            fl.a(e2, "SDKCoordinatorDownload", "onDownload");
        }
        if (this.c != null) {
            try {
                this.c.seek(j);
                this.c.write(bArr);
            } catch (Throwable e22) {
                b();
                fl.a(e22, "SDKCoordinatorDownload", "onDownload");
            }
        }
    }

    public void d() {
        b();
    }

    public void e() {
        try {
            if (this.c != null) {
                this.c.close();
            }
            String a = fe.a(this.d);
            if (a != null) {
                if (a.equalsIgnoreCase(this.f)) {
                    if (new File(this.e).exists()) {
                        b();
                        return;
                    } else {
                        new File(this.d).renameTo(new File(this.e));
                    }
                }
            }
            b();
        } catch (Throwable th) {
            b();
            r1 = new File(this.e);
            File file;
            if (file.exists()) {
                file.delete();
            }
            fl.a(th, "SDKCoordinatorDownload", "onFinish");
        }
    }

    public void a(Throwable th) {
        try {
            if (this.c != null) {
                this.c.close();
            }
            b();
            File file = new File(b(this.g, "tempfile"));
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdir();
                }
                file.createNewFile();
            }
        } catch (Throwable th2) {
            fl.a(th2, "SDKCoordinatorDownload", "onException");
        }
    }

    private void b() {
        File file = new File(this.d);
        if (file.exists()) {
            file.delete();
        }
    }
}
