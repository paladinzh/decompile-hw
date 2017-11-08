package com.loc;

import android.content.Context;
import android.os.Build.VERSION;
import com.loc.bp.a;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/* compiled from: DexDownLoad */
public class aw extends Thread implements a {
    private ax a;
    private bp b;
    private v c;
    private String d;
    private RandomAccessFile e;
    private String f;
    private Context g;
    private String h;
    private String i;
    private String j;
    private String k;
    private int l;
    private int m;

    public aw(Context context, ax axVar, v vVar) {
        try {
            this.g = context.getApplicationContext();
            this.c = vVar;
            if (axVar != null) {
                this.a = axVar;
                this.b = new bp(new az(this.a));
                String[] split = this.a.a().split("/");
                this.f = split[split.length - 1];
                split = this.f.split("_");
                this.h = split[0];
                this.i = split[2];
                this.j = split[1];
                this.l = Integer.parseInt(split[3]);
                this.m = Integer.parseInt(split[4].split("\\.")[0]);
                this.k = axVar.b();
                this.d = ba.a(context, this.f);
            }
        } catch (Throwable th) {
            aa.a(th, "DexDownLoad", "DexDownLoad");
        }
    }

    private void a(String str) {
        aj ajVar = new aj(this.g, bd.c());
        List c = ajVar.c(be.b(str, "copy"), new be());
        ba.a(c);
        if (c != null && c.size() > 1) {
            int size = c.size();
            for (int i = 1; i < size; i++) {
                ba.a(this.g, ajVar, ((bf) c.get(i)).a());
            }
        }
    }

    private boolean a(Context context) {
        return q.m(context) == 1;
    }

    private boolean a(aj ajVar, bf bfVar, String str, String str2, String str3, String str4) {
        if ("errorstatus".equals(bfVar.f())) {
            if (!new File(ba.a(this.g, this.c.a(), this.c.b())).exists()) {
                bb.a(this.g, this.c, ba.a(this.g, this.h, this.c.b()), ba.a(this.g), null, this.g.getClassLoader(), ba.a(this.g, ajVar, this.c));
            }
            return true;
        } else if (!new File(this.d).exists()) {
            return false;
        } else {
            List c = ajVar.c(be.a(ba.b(str, str2), str, str2, str3), new be());
            if (c != null && c.size() > 0) {
                return true;
            }
            try {
                ba.a(this.g, ajVar, this.c, new a(ba.b(str, this.c.b()), str4, str, str2, str3).a("usedex").a(), this.d);
                bb.a(this.g, this.c, ba.a(this.g, this.h, this.c.b()), ba.a(this.g), null, this.g.getClassLoader(), str3);
            } catch (Throwable e) {
                aa.a(e, "DexDownLoad", "processDownloadedFile()");
            } catch (Throwable e2) {
                aa.a(e2, "DexDownLoad", "processDownloadedFile()");
            } catch (Throwable e22) {
                aa.a(e22, "DexDownLoad", "processDownloadedFile()");
            }
            return true;
        }
    }

    private boolean a(String str, String str2) {
        boolean z = false;
        if (this.c == null) {
            return false;
        }
        if (this.c.a().equals(str) && this.c.b().equals(str2)) {
            z = true;
        }
        return z;
    }

    private boolean a(String str, String str2, String str3, String str4, String str5) {
        aj ajVar = new aj(this.g, bd.c());
        try {
            List c = ajVar.c(be.b(str3, "usedex"), new be());
            if (c != null) {
                if (c.size() > 0 && bh.a(((bf) c.get(0)).e(), this.j) > 0) {
                    return true;
                }
            }
        } catch (Throwable th) {
            aa.a(th, "DexDownLoad", "isDownloaded()");
        }
        bf a = a.a(ajVar, str);
        return a == null ? false : a(ajVar, a, str3, str4, str2, str5);
    }

    private boolean d() {
        return VERSION.SDK_INT >= this.m && VERSION.SDK_INT <= this.l;
    }

    private boolean e() {
        try {
            if (!a(this.h, this.i) || a(this.f, this.j, this.h, this.i, this.k) || !a(this.g) || !d()) {
                return false;
            }
            a(this.c.a());
            return true;
        } catch (Throwable th) {
            aa.a(th, "DexDownLoad", "isNeedDownload()");
            return false;
        }
    }

    public void a() {
        try {
            start();
        } catch (Throwable th) {
            aa.a(th, "DexDownLoad", "startDownload");
        }
    }

    public void a(Throwable th) {
        try {
            if (this.e != null) {
                this.e.close();
            }
        } catch (Throwable e) {
            aa.a(e, "DexDownLoad", "onException()");
        } catch (Throwable e2) {
            aa.a(e2, "DexDownLoad", "onException()");
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
        } catch (Throwable e) {
            aa.a(e, "DexDownLoad", "onDownload()");
        } catch (Throwable e2) {
            aa.a(e2, "DexDownLoad", "onDownload()");
            return;
        }
        try {
            this.e.seek(j);
            this.e.write(bArr);
        } catch (Throwable e22) {
            aa.a(e22, "DexDownLoad", "onDownload()");
        }
    }

    public void b() {
    }

    public void c() {
        try {
            if (this.e != null) {
                this.e.close();
                String b = this.a.b();
                if (ba.a(this.d, b)) {
                    String c = this.a.c();
                    aj ajVar = new aj(this.g, bd.c());
                    a.a(ajVar, new a(this.f, b, this.h, c, this.j).a("copy").a(), be.a(this.f, this.h, c, this.j));
                    try {
                        ba.a(this.g, ajVar, this.c, new a(ba.b(this.h, this.c.b()), b, this.h, c, this.j).a("usedex").a(), this.d);
                        bb.a(this.g, this.c, ba.a(this.g, this.h, this.c.b()), ba.a(this.g), null, this.g.getClassLoader(), this.j);
                    } catch (Throwable e) {
                        aa.a(e, "DexDownLoad", "onFinish()");
                    } catch (Throwable e2) {
                        aa.a(e2, "DexDownLoad", "onFinish()");
                    } catch (Throwable e22) {
                        aa.a(e22, "DexDownLoad", "onFinish()");
                    }
                }
                try {
                    new File(this.d).delete();
                } catch (Throwable e222) {
                    aa.a(e222, "DexDownLoad", "onFinish()");
                }
            }
        } catch (Throwable e2222) {
            aa.a(e2222, "DexDownLoad", "onFinish()");
        } catch (Throwable e22222) {
            aa.a(e22222, "DexDownLoad", "onFinish()");
        }
    }

    public void run() {
        try {
            if (e()) {
                this.b.a(this);
            }
        } catch (Throwable th) {
            aa.a(th, "DexDownLoad", "run");
        }
    }
}
