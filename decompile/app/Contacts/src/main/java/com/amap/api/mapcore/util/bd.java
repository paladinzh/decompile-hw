package com.amap.api.mapcore.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import java.lang.ref.WeakReference;

/* compiled from: ImageWorker */
public abstract class bd {
    private ba a;
    private com.amap.api.mapcore.util.ba.a b;
    protected boolean c = false;
    protected Resources d;
    private boolean e = false;
    private final Object f = new Object();

    /* compiled from: ImageWorker */
    public class a extends av<Boolean, Void, Bitmap> {
        final /* synthetic */ bd a;
        private final WeakReference<com.amap.api.mapcore.bn.a> e;

        public a(bd bdVar, com.amap.api.mapcore.bn.a aVar) {
            this.a = bdVar;
            this.e = new WeakReference(aVar);
        }

        protected Bitmap a(Boolean... boolArr) {
            Bitmap bitmap = null;
            boolean booleanValue = boolArr[0].booleanValue();
            Object obj = (com.amap.api.mapcore.bn.a) this.e.get();
            if (obj == null) {
                return null;
            }
            Bitmap bitmap2;
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
                    try {
                        this.a.f.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!(this.a.a == null || d() || e() == null || this.a.e)) {
                bitmap = this.a.a.b(stringBuilder2);
            }
            if (!booleanValue || bitmap != null || d()) {
                bitmap2 = bitmap;
            } else if (e() == null || this.a.e) {
                bitmap2 = bitmap;
            } else {
                bitmap2 = this.a.a(obj);
            }
            if (!(bitmap2 == null || this.a.a == null)) {
                this.a.a.a(stringBuilder2, bitmap2);
            }
            return bitmap2;
        }

        protected void a(Bitmap bitmap) {
            if (d() || this.a.e) {
                bitmap = null;
            }
            com.amap.api.mapcore.bn.a e = e();
            if (bitmap != null && !bitmap.isRecycled() && e != null) {
                e.a(bitmap);
            }
        }

        protected void b(Bitmap bitmap) {
            super.b((Object) bitmap);
            synchronized (this.a.f) {
                this.a.f.notifyAll();
            }
        }

        private com.amap.api.mapcore.bn.a e() {
            com.amap.api.mapcore.bn.a aVar = (com.amap.api.mapcore.bn.a) this.e.get();
            if (this != bd.c(aVar)) {
                return null;
            }
            return aVar;
        }
    }

    /* compiled from: ImageWorker */
    protected class b extends av<Object, Void, Void> {
        final /* synthetic */ bd a;

        protected b(bd bdVar) {
            this.a = bdVar;
        }

        protected /* synthetic */ Object a(Object[] objArr) {
            return d(objArr);
        }

        protected Void d(Object... objArr) {
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
            return null;
        }
    }

    protected abstract Bitmap a(Object obj);

    protected bd(Context context) {
        this.d = context.getResources();
    }

    public void a(boolean z, com.amap.api.mapcore.bn.a aVar) {
        Bitmap bitmap = null;
        if (aVar != null) {
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
                aVar2.a(av.d, (Object[]) new Boolean[]{Boolean.valueOf(z)});
            } else {
                aVar.a(bitmap);
            }
        }
    }

    public void a(com.amap.api.mapcore.util.ba.a aVar) {
        this.b = aVar;
        this.a = ba.a(this.b);
        new b(this).c(Integer.valueOf(1));
    }

    public void a(boolean z) {
        this.e = z;
        b(false);
    }

    protected ba a() {
        return this.a;
    }

    public static void a(com.amap.api.mapcore.bn.a aVar) {
        a c = c(aVar);
        if (c != null) {
            c.a(true);
        }
    }

    private static a c(com.amap.api.mapcore.bn.a aVar) {
        if (aVar == null) {
            return null;
        }
        return aVar.j;
    }

    public void b(boolean z) {
        synchronized (this.f) {
            this.c = z;
            if (!this.c) {
                this.f.notifyAll();
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
