package com.amap.api.mapcore;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES10;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TextOptions;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TextDelegateImp */
class bl implements ao {
    private static int a = 0;
    private Paint A = new Paint();
    private Handler B = new Handler();
    private Runnable C = new bm(this);
    private boolean D = false;
    private boolean E = false;
    private float b = 0.0f;
    private float c = 0.0f;
    private int d = 4;
    private int e = 32;
    private FPoint f = new FPoint();
    private int g;
    private Bitmap h;
    private int i;
    private int j;
    private FloatBuffer k = null;
    private String l;
    private LatLng m;
    private float n = 0.5f;
    private float o = 1.0f;
    private boolean p = true;
    private aw q;
    private FloatBuffer r;
    private Object s;
    private String t;
    private int u;
    private int v;
    private int w;
    private Typeface x;
    private float y;
    private Rect z = new Rect();

    private static String d(String str) {
        a++;
        return str + a;
    }

    public void a(float f) {
        this.c = f;
        this.b = (((-f) % 360.0f) + 360.0f) % 360.0f;
        T();
    }

    public boolean x() {
        return this.D;
    }

    public synchronized void y() {
        if (this.D) {
            try {
                b();
                if (this.h != null) {
                    this.h.recycle();
                    this.h = null;
                }
                if (this.r != null) {
                    this.r.clear();
                    this.r = null;
                }
                if (this.k != null) {
                    this.k.clear();
                    this.k = null;
                }
                this.m = null;
                this.s = null;
            } catch (Throwable th) {
                ce.a(th, "TextDelegateImp", "realdestroy");
                th.printStackTrace();
                Log.d("destroy erro", "TextDelegateImp destroy");
            }
        }
    }

    public void p() {
        try {
            this.D = true;
            if (!(this.q == null || this.q.a == null)) {
                this.q.a.N();
            }
            this.g = 0;
        } catch (Throwable th) {
            ce.a(th, "TextDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "TextDelegateImp destroy");
        }
    }

    public bl(TextOptions textOptions, aw awVar) throws RemoteException {
        this.q = awVar;
        if (textOptions.getPosition() != null) {
            this.m = textOptions.getPosition();
        }
        b(textOptions.getAlignX(), textOptions.getAlignY());
        this.p = textOptions.isVisible();
        this.t = textOptions.getText();
        this.u = textOptions.getBackgroundColor();
        this.v = textOptions.getFontColor();
        this.w = textOptions.getFontSize();
        this.s = textOptions.getObject();
        this.y = textOptions.getZIndex();
        this.x = textOptions.getTypeface();
        this.l = h();
        a(textOptions.getRotate());
        Q();
        r();
    }

    private void Q() {
        if (this.t != null && this.t.trim().length() > 0) {
            try {
                this.A.setTypeface(this.x);
                this.A.setSubpixelText(true);
                this.A.setAntiAlias(true);
                this.A.setStrokeWidth(5.0f);
                this.A.setStrokeCap(Cap.ROUND);
                this.A.setTextSize((float) this.w);
                this.A.setTextAlign(Align.CENTER);
                this.A.setColor(this.v);
                FontMetrics fontMetrics = this.A.getFontMetrics();
                int i = (int) (fontMetrics.descent - fontMetrics.ascent);
                int i2 = (int) (((((float) i) - fontMetrics.bottom) - fontMetrics.top) / 2.0f);
                this.A.getTextBounds(this.t, 0, this.t.length(), this.z);
                Bitmap createBitmap = Bitmap.createBitmap(this.z.width() + 6, i, Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                canvas.drawColor(this.u);
                canvas.drawText(this.t, (float) (this.z.centerX() + 3), (float) i2, this.A);
                this.h = createBitmap;
                this.i = this.h.getWidth();
                this.j = this.h.getHeight();
                this.r = bj.a(new float[]{0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f});
            } catch (Throwable th) {
                ce.a(th, "TextDelegateImp", "initBitmap");
            }
        }
    }

    private int R() {
        return this.i;
    }

    private int S() {
        return this.j;
    }

    public synchronized boolean b() {
        T();
        this.p = false;
        return this.q.b((ah) this);
    }

    private void T() {
        if (this.q.a != null) {
            this.q.a.f(false);
        }
    }

    public LatLng e() {
        return this.m;
    }

    public String h() {
        if (this.l == null) {
            this.l = d("Text");
        }
        return this.l;
    }

    public void a(LatLng latLng) {
        this.m = latLng;
        r();
        T();
    }

    public void a(String str) {
    }

    public String i() {
        return null;
    }

    public void b(String str) {
    }

    public String j() {
        return null;
    }

    public void a(boolean z) {
    }

    public synchronized void a(ArrayList<BitmapDescriptor> arrayList) {
    }

    public synchronized ArrayList<BitmapDescriptor> w() {
        return null;
    }

    public synchronized void a(BitmapDescriptor bitmapDescriptor) {
    }

    public boolean k() {
        return false;
    }

    public void l() {
    }

    public void m() {
    }

    public boolean n() {
        return false;
    }

    public void c(boolean z) {
        if (this.p != z) {
            this.p = z;
            T();
        }
    }

    public boolean o() {
        return this.p;
    }

    public void b(float f) {
        this.y = f;
        this.q.h();
    }

    public float G() {
        return this.y;
    }

    public void a(float f, float f2) {
    }

    public boolean a(ah ahVar) throws RemoteException {
        if (equals(ahVar) || ahVar.h().equals(h())) {
            return true;
        }
        return false;
    }

    public int q() {
        return super.hashCode();
    }

    public boolean r() {
        if (this.m == null) {
            return false;
        }
        this.q.a.a(this.m.latitude, this.m.longitude, this.f);
        return true;
    }

    private void a(ab abVar) throws RemoteException {
        float[] a = bj.a(abVar, 0, this.f, this.b, R(), S(), this.n, this.o);
        if (this.k != null) {
            this.k = bj.a(a, this.k);
        } else {
            this.k = bj.a(a);
        }
        if (this.g != 0) {
            a(this.g, this.k, this.r);
        }
    }

    private void a(int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (i != 0 && floatBuffer != null && floatBuffer2 != null) {
            GLES10.glEnable(3042);
            GLES10.glBlendFunc(1, 771);
            GLES10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GLES10.glEnable(3553);
            GLES10.glEnableClientState(32884);
            GLES10.glEnableClientState(32888);
            GLES10.glBindTexture(3553, i);
            GLES10.glVertexPointer(3, 5126, 0, floatBuffer);
            GLES10.glTexCoordPointer(2, 5126, 0, floatBuffer2);
            GLES10.glDrawArrays(6, 0, 4);
            GLES10.glDisableClientState(32884);
            GLES10.glDisableClientState(32888);
            GLES10.glDisable(3553);
            GLES10.glDisable(3042);
        }
    }

    public void a(GL10 gl10, ab abVar) {
        if (this.p && !this.D && this.m != null && this.h != null) {
            if (!this.E) {
                try {
                    if (!(this.h == null || this.h.isRecycled())) {
                        if (this.g == 0) {
                            this.g = a(gl10);
                        }
                        bj.b(gl10, this.g, this.h, false);
                        this.E = true;
                        this.h.recycle();
                    }
                } catch (Throwable th) {
                    ce.a(th, "TextDelegateImp", "loadtexture");
                    th.printStackTrace();
                    return;
                }
            }
            try {
                a(abVar);
            } catch (Throwable th2) {
                ce.a(th2, "TextDelegateImp", "drawMarker");
            }
        }
    }

    private int a(GL10 gl10) {
        int K = this.q.a.K();
        if (K != 0) {
            return K;
        }
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public boolean c() {
        return true;
    }

    public void a(int i) {
    }

    public void a(Object obj) {
        this.s = obj;
    }

    public Object s() {
        return this.s;
    }

    public void d(boolean z) {
    }

    public boolean t() {
        return false;
    }

    public int v() {
        return 0;
    }

    public LatLng g() {
        return this.m;
    }

    public void z() {
        this.q.c(this);
    }

    public void e(boolean z) throws RemoteException {
    }

    public boolean A() {
        return false;
    }

    public float u() {
        return this.c;
    }

    public int B() {
        return 0;
    }

    public int C() {
        return 0;
    }

    public void a(int i, int i2) {
    }

    public int D() {
        return 0;
    }

    public int E() {
        return 0;
    }

    public FPoint f() {
        return this.f;
    }

    public boolean F() {
        return false;
    }

    public Rect d() {
        return null;
    }

    public void c(String str) throws RemoteException {
        this.t = str;
        U();
    }

    public String a() throws RemoteException {
        return this.t;
    }

    public void b(int i) throws RemoteException {
        this.u = i;
        U();
    }

    public int K() throws RemoteException {
        return this.u;
    }

    public void c(int i) throws RemoteException {
        this.v = i;
        U();
    }

    public int L() throws RemoteException {
        return this.v;
    }

    public void d(int i) throws RemoteException {
        this.w = i;
        U();
    }

    public int M() throws RemoteException {
        return this.w;
    }

    public void a(Typeface typeface) throws RemoteException {
        this.x = typeface;
        U();
    }

    public Typeface N() throws RemoteException {
        return this.x;
    }

    public void b(int i, int i2) throws RemoteException {
        this.d = i;
        switch (i) {
            case 1:
                this.n = 0.0f;
                break;
            case 2:
                this.n = 1.0f;
                break;
            case 4:
                this.n = 0.5f;
                break;
            default:
                this.n = 0.5f;
                break;
        }
        this.e = i2;
        switch (i2) {
            case 8:
                this.o = 0.0f;
                break;
            case 16:
                this.o = 1.0f;
                break;
            case 32:
                this.o = 0.5f;
                break;
            default:
                this.o = 0.5f;
                break;
        }
        T();
    }

    public int O() throws RemoteException {
        return this.d;
    }

    public int P() {
        return this.e;
    }

    private void U() {
        this.B.removeCallbacks(this.C);
        this.B.post(this.C);
    }

    public boolean H() {
        Rect k = this.q.a.k();
        if (k == null) {
            return true;
        }
        IPoint iPoint = new IPoint();
        if (this.m != null) {
            this.q.a.b(this.m.latitude, this.m.longitude, iPoint);
        }
        return k.contains(iPoint.x, iPoint.y);
    }

    public void b(boolean z) {
    }

    public void a(IPoint iPoint) {
    }

    public IPoint I() {
        return null;
    }

    public void J() {
        this.E = false;
        this.g = 0;
        Q();
    }
}
