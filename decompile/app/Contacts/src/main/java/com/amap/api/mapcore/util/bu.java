package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;

/* compiled from: SDKCoordinatorDownload */
public class bu extends Thread implements com.amap.api.mapcore.util.de.a {
    private static String h = "sodownload";
    private static String i = "sofail";
    private de a = new de(this.b);
    private a b;
    private RandomAccessFile c;
    private String d;
    private String e;
    private String f;
    private Context g;

    /* compiled from: SDKCoordinatorDownload */
    private static class a extends dj {
        private String a;

        a(String str) {
            this.a = str;
        }

        public Map<String, String> c() {
            return null;
        }

        public Map<String, String> b() {
            return null;
        }

        public String a() {
            return this.a;
        }
    }

    public bu(Context context, String str, String str2, String str3) {
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
        if (this.b != null && !TextUtils.isEmpty(this.b.a()) && this.b.a().contains("libJni_wgs2gcj.so") && this.b.a().contains(Build.CPU_ABI) && !new File(this.e).exists()) {
            start();
        }
    }

    public void run() {
        try {
            File file = new File(b(this.g, "tempfile"));
            if (file.exists()) {
                bm.a(this.g, new com.amap.api.mapcore.util.bv.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
                file.delete();
            }
            this.a.a(this);
        } catch (Throwable th) {
            cb.a(th, "SDKCoordinatorDownload", "run");
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
            cb.a(e, "SDKCoordinatorDownload", "onDownload");
            b();
        } catch (Throwable e2) {
            b();
            cb.a(e2, "SDKCoordinatorDownload", "onDownload");
            return;
        }
        try {
            this.c.seek(j);
            this.c.write(bArr);
        } catch (Throwable e22) {
            b();
            cb.a(e22, "SDKCoordinatorDownload", "onDownload");
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
            if (!bs.a(this.d).equalsIgnoreCase(this.f)) {
                b();
                bm.a(this.g, new com.amap.api.mapcore.util.bv.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            } else if (new File(this.e).exists()) {
                b();
            } else {
                new File(this.d).renameTo(new File(this.e));
                bm.a(this.g, new com.amap.api.mapcore.util.bv.a(h, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            }
        } catch (Throwable th) {
            b();
            r1 = new File(this.e);
            File file;
            if (file.exists()) {
                file.delete();
            }
            try {
                bm.a(this.g, new com.amap.api.mapcore.util.bv.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            } catch (bk e) {
                e.printStackTrace();
            }
            cb.a(th, "SDKCoordinatorDownload", "onDownload");
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
        } catch (Throwable e) {
            cb.a(e, "SDKCoordinatorDownload", "onException");
        } catch (Throwable e2) {
            cb.a(e2, "SDKCoordinatorDownload", "onException");
        }
    }

    private void b() {
        File file = new File(this.d);
        if (file.exists()) {
            file.delete();
        }
    }
}
