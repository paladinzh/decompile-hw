package com.amap.api.mapcore.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import java.lang.ref.WeakReference;

/* compiled from: ImageWorker */
public abstract class dz {
    private dw a;
    private com.amap.api.mapcore.util.dw.a b;
    protected boolean c = false;
    protected Resources d;
    private boolean e = false;
    private final Object f = new Object();

    /* compiled from: ImageWorker */
    public class a extends dq<Boolean, Void, Bitmap> {
        final /* synthetic */ dz a;
        private final WeakReference<com.amap.api.mapcore.util.dg.a> e;

        public a(dz dzVar, com.amap.api.mapcore.util.dg.a aVar) {
            this.a = dzVar;
            this.e = new WeakReference(aVar);
        }

        protected Bitmap a(Boolean... boolArr) {
            try {
                boolean booleanValue = boolArr[0].booleanValue();
                Object obj = (com.amap.api.mapcore.util.dg.a) this.e.get();
                if (obj == null) {
                    return null;
                }
                Bitmap b;
                Bitmap bitmap;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(obj.a);
                stringBuilder.append("-");
                stringBuilder.append(obj.b);
                stringBuilder.append("-");
                stringBuilder.append(obj.c);
                String stringBuilder2 = stringBuilder.toString();
                synchronized (this.a.f) {
                    while (this.a.c) {
                        if (d()) {
                            break;
                        }
                        this.a.f.wait();
                    }
                }
                if (this.a.a != null) {
                    if (!d()) {
                        b = e() == null ? null : this.a.e ? null : this.a.a.b(stringBuilder2);
                        if (!booleanValue || b != null || d()) {
                            bitmap = b;
                        } else if (e() == null) {
                            bitmap = b;
                        } else if (this.a.e) {
                            bitmap = this.a.a(obj);
                        } else {
                            bitmap = b;
                        }
                        if (!(bitmap == null || this.a.a == null)) {
                            this.a.a.a(stringBuilder2, bitmap);
                        }
                        return bitmap;
                    }
                }
                b = null;
                if (!booleanValue) {
                    if (e() == null) {
                        bitmap = b;
                    } else if (this.a.e) {
                        bitmap = this.a.a(obj);
                    } else {
                        bitmap = b;
                    }
                    this.a.a.a(stringBuilder2, bitmap);
                    return bitmap;
                }
                bitmap = b;
                this.a.a.a(stringBuilder2, bitmap);
                return bitmap;
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }

        protected void a(Bitmap bitmap) {
            try {
                if (d() || this.a.e) {
                    bitmap = null;
                }
                com.amap.api.mapcore.util.dg.a e = e();
                if (bitmap != null && !bitmap.isRecycled() && e != null) {
                    e.a(bitmap);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        protected void b(Bitmap bitmap) {
            super.b((Object) bitmap);
            synchronized (this.a.f) {
                try {
                    this.a.f.notifyAll();
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }

        private com.amap.api.mapcore.util.dg.a e() {
            com.amap.api.mapcore.util.dg.a aVar = (com.amap.api.mapcore.util.dg.a) this.e.get();
            if (this != dz.c(aVar)) {
                return null;
            }
            return aVar;
        }
    }

    /* compiled from: ImageWorker */
    protected class b extends dq<Object, Void, Void> {
        final /* synthetic */ dz a;

        protected b(dz dzVar) {
            this.a = dzVar;
        }

        protected /* synthetic */ Object a(Object[] objArr) {
            return d(objArr);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected Void d(Object... objArr) {
            try {
                switch (((Integer) objArr[0]).intValue()) {
                    case 0:
                        this.a.c();
                        break;
                    case 1:
                        this.a.b();
                        break;
                    case 2:
                        this.a.d();
                        break;
                    case 3:
                        this.a.e();
                        break;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return null;
        }
    }

    protected abstract Bitmap a(Object obj);

    protected dz(Context context) {
        this.d = context.getResources();
    }

    public void a(boolean z, com.amap.api.mapcore.util.dg.a aVar) {
        Bitmap bitmap = null;
        if (aVar != null) {
            try {
                if (this.a != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(aVar.a);
                    stringBuilder.append("-");
                    stringBuilder.append(aVar.b);
                    stringBuilder.append("-");
                    stringBuilder.append(aVar.c);
                    bitmap = this.a.a(stringBuilder.toString());
                }
                if (bitmap == null) {
                    a aVar2 = new a(this, aVar);
                    aVar.j = aVar2;
                    aVar2.a(dq.d, (Object[]) new Boolean[]{Boolean.valueOf(z)});
                } else {
                    aVar.a(bitmap);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public void a(com.amap.api.mapcore.util.dw.a aVar) {
        this.b = aVar;
        this.a = dw.a(this.b);
        new b(this).c(Integer.valueOf(1));
    }

    public void a(boolean z) {
        this.e = z;
        b(false);
    }

    protected dw a() {
        return this.a;
    }

    public static void a(com.amap.api.mapcore.util.dg.a aVar) {
        a c = c(aVar);
        if (c != null) {
            c.a(true);
        }
    }

    private static a c(com.amap.api.mapcore.util.dg.a aVar) {
        if (aVar == null) {
            return null;
        }
        return aVar.j;
    }

    public void b(boolean z) {
        synchronized (this.f) {
            this.c = z;
            if (!this.c) {
                try {
                    this.f.notifyAll();
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
    }

    protected void b() {
        if (this.a != null) {
            this.a.a();
        }
    }

    protected void c() {
        if (this.a != null) {
            this.a.b();
        }
    }

    protected void d() {
        if (this.a != null) {
            this.a.c();
        }
    }

    protected void e() {
        if (this.a != null) {
            this.a.d();
            this.a = null;
        }
    }

    public void f() {
        new b(this).c(Integer.valueOf(0));
    }

    public void g() {
        new b(this).c(Integer.valueOf(3));
    }
}
