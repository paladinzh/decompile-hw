package com.loc;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;

/* compiled from: SDKCoordinatorDownload */
public class u extends Thread implements com.loc.bp.a {
    private static String h = "sodownload";
    private static String i = "sofail";
    private bp a = new bp(this.b);
    private a b;
    private RandomAccessFile c;
    private String d;
    private String e;
    private String f;
    private Context g;

    /* compiled from: SDKCoordinatorDownload */
    private static class a extends bs {
        private String d;

        a(String str) {
            this.d = str;
        }

        public Map<String, String> a() {
            return null;
        }

        public Map<String, String> b() {
            return null;
        }

        public String c() {
            return this.d;
        }
    }

    public u(Context context, String str, String str2, String str3) {
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

    private void d() {
        File file = new File(this.d);
        if (file.exists()) {
            file.delete();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a() {
        if (this.b != null && !TextUtils.isEmpty(this.b.c()) && this.b.c().contains("libJni_wgs2gcj.so") && this.b.c().contains(Build.CPU_ABI) && !new File(this.e).exists()) {
            start();
        }
    }

    public void a(Throwable th) {
        try {
            if (this.c != null) {
                this.c.close();
            }
            d();
            File file = new File(b(this.g, "tempfile"));
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdir();
                }
                file.createNewFile();
            }
        } catch (Throwable e) {
            aa.a(e, "SDKCoordinatorDownload", "onException");
        } catch (Throwable e2) {
            aa.a(e2, "SDKCoordinatorDownload", "onException");
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
            aa.a(e, "SDKCoordinatorDownload", "onDownload");
            d();
        } catch (Throwable e2) {
            d();
            aa.a(e2, "SDKCoordinatorDownload", "onDownload");
            return;
        }
        try {
            this.c.seek(j);
            this.c.write(bArr);
        } catch (Throwable e22) {
            d();
            aa.a(e22, "SDKCoordinatorDownload", "onDownload");
        }
    }

    public void b() {
        d();
    }

    public void c() {
        try {
            if (this.c != null) {
                this.c.close();
            }
            if (!s.a(this.d).equalsIgnoreCase(this.f)) {
                d();
                n.a(this.g, new com.loc.v.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            } else if (new File(this.e).exists()) {
                d();
            } else {
                new File(this.d).renameTo(new File(this.e));
                n.a(this.g, new com.loc.v.a(h, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            }
        } catch (Throwable th) {
            d();
            r1 = new File(this.e);
            File file;
            if (file.exists()) {
                file.delete();
            }
            try {
                n.a(this.g, new com.loc.v.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
            } catch (l e) {
                e.printStackTrace();
            }
            aa.a(th, "SDKCoordinatorDownload", "onDownload");
        }
    }

    public void run() {
        try {
            File file = new File(b(this.g, "tempfile"));
            if (file.exists()) {
                n.a(this.g, new com.loc.v.a(i, "1.0.0", "sodownload_1.0.0").a(new String[0]).a());
                file.delete();
            }
            this.a.a(this);
        } catch (Throwable th) {
            aa.a(th, "SDKCoordinatorDownload", "run");
            d();
        }
    }
}
