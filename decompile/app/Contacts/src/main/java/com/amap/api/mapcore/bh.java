package com.amap.api.mapcore;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PopupOverlay */
class bh implements ah {
    private boolean a = false;
    private int b = 0;
    private int c = 0;
    private FloatBuffer d = null;
    private String e;
    private FPoint f;
    private BitmapDescriptor g;
    private boolean h = true;
    private FloatBuffer i;
    private Object j;
    private int k;
    private ab l = null;
    private MapProjection m = null;
    private float n = 0.5f;
    private float o = 1.0f;
    private boolean p;
    private boolean q = false;
    private boolean r = true;
    private int s = 20;

    public boolean x() {
        return this.a;
    }

    public void y() {
        if (this.a) {
            try {
                b();
                if (this.g != null) {
                    Bitmap bitmap = this.g.getBitmap();
                    if (bitmap != null) {
                        bitmap.recycle();
                        this.g = null;
                    }
                }
                if (this.i != null) {
                    this.i.clear();
                    this.i = null;
                }
                if (this.d != null) {
                    this.d.clear();
                    this.d = null;
                }
                this.f = null;
                this.j = null;
                this.k = 0;
            } catch (Throwable th) {
                ce.a(th, "PopupOverlay", "realDestroy");
                th.printStackTrace();
                Log.d("destroy erro", "MarkerDelegateImp destroy");
            }
        }
    }

    private void b(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            this.k = 0;
            this.g = bitmapDescriptor;
        }
    }

    public bh(MarkerOptions markerOptions, ab abVar) {
        this.l = abVar;
        this.m = abVar.c();
        b(markerOptions.getIcon());
        this.b = markerOptions.getInfoWindowOffsetX();
        this.c = markerOptions.getInfoWindowOffsetY();
        this.h = markerOptions.isVisible();
        this.e = h();
        r();
    }

    public int K() {
        try {
            return M().getWidth();
        } catch (Throwable th) {
            return 0;
        }
    }

    public int L() {
        try {
            return M().getHeight();
        } catch (Throwable th) {
            return 0;
        }
    }

    public Rect d() {
        return null;
    }

    public boolean b() {
        N();
        if (this.k != 0) {
            this.l.f(this.k);
        }
        return true;
    }

    private void N() {
        if (this.l != null) {
            this.l.f(false);
        }
    }

    public LatLng e() {
        return null;
    }

    public String h() {
        if (this.e == null) {
            this.e = "PopupOverlay";
        }
        return this.e;
    }

    public void a(FPoint fPoint) {
        if (fPoint == null || !fPoint.equals(this.f)) {
            this.f = fPoint;
        }
    }

    public void a(LatLng latLng) {
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

    public void a(ArrayList<BitmapDescriptor> arrayList) {
    }

    public ArrayList<BitmapDescriptor> w() {
        return null;
    }

    public void a(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            this.g = bitmapDescriptor;
            this.q = false;
            if (this.i != null) {
                this.i.clear();
                this.i = null;
            }
            N();
        }
    }

    public BitmapDescriptor M() {
        return this.g;
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
        if (!this.h && z) {
            this.p = true;
        }
        this.h = z;
    }

    public boolean o() {
        return this.h;
    }

    public void a(float f, float f2) {
        if (this.n != f || this.o != f2) {
            this.n = f;
            this.o = f2;
        }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean r() {
        if (this.f == null) {
            return false;
        }
        IPoint iPoint = new IPoint();
        this.l.c().map2Win(this.f.x, this.f.y, iPoint);
        int K = K();
        int L = L();
        int i = (int) (((float) (iPoint.x + this.b)) - (((float) K) * this.n));
        int i2 = (int) (((float) (iPoint.y + this.c)) + (((float) L) * (1.0f - this.o)));
        if (i - K > this.l.l() || i < (-K) * 2 || i2 < (-L) * 2 || i2 - L > this.l.m() || this.g == null) {
            return false;
        }
        K = this.g.getWidth();
        float width = ((float) K) / ((float) this.g.getBitmap().getWidth());
        float height = ((float) this.g.getHeight()) / ((float) this.g.getBitmap().getHeight());
        if (this.i == null) {
            this.i = bj.a(new float[]{0.0f, height, width, height, width, 0.0f, 0.0f, 0.0f});
        }
        float[] fArr = new float[]{(float) i, (float) (this.l.m() - i2), 0.0f, (float) (i + K), (float) (this.l.m() - i2), 0.0f, (float) (K + i), (float) ((this.l.m() - i2) + L), 0.0f, (float) i, (float) ((this.l.m() - i2) + L), 0.0f};
        if (this.d != null) {
            this.d = bj.a(fArr, this.d);
        } else {
            this.d = bj.a(fArr);
        }
        return true;
    }

    private void a(GL10 gl10, int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (floatBuffer != null && floatBuffer2 != null) {
            gl10.glEnable(3042);
            gl10.glBlendFunc(1, 771);
            gl10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            gl10.glEnable(3553);
            gl10.glEnableClientState(32884);
            gl10.glEnableClientState(32888);
            gl10.glBindTexture(3553, i);
            gl10.glVertexPointer(3, 5126, 0, floatBuffer);
            gl10.glTexCoordPointer(2, 5126, 0, floatBuffer2);
            gl10.glDrawArrays(6, 0, 4);
            gl10.glDisableClientState(32884);
            gl10.glDisableClientState(32888);
            gl10.glDisable(3553);
            gl10.glDisable(3042);
        }
    }

    public void a(GL10 gl10) {
        if (this.h && this.f != null && M() != null) {
            if (!this.q) {
                try {
                    if (this.k != 0) {
                        gl10.glDeleteTextures(1, new int[]{this.k}, 0);
                        this.l.f(this.k);
                    }
                    this.k = b(gl10);
                    if (this.g != null) {
                        Bitmap bitmap = this.g.getBitmap();
                        if (!(bitmap == null || bitmap.isRecycled())) {
                            bj.b(gl10, this.k, bitmap, false);
                        }
                        this.q = true;
                    }
                } catch (Throwable th) {
                    ce.a(th, "PopupOverlay", "drawMarker");
                    th.printStackTrace();
                    return;
                }
            }
            if (r()) {
                gl10.glLoadIdentity();
                gl10.glViewport(0, 0, this.l.l(), this.l.m());
                gl10.glMatrixMode(5889);
                gl10.glLoadIdentity();
                gl10.glOrthof(0.0f, (float) this.l.l(), 0.0f, (float) this.l.m(), 1.0f, -1.0f);
                a(gl10, this.k, this.d, this.i);
                if (this.p) {
                    a();
                    this.p = false;
                }
            }
        }
    }

    public void a() {
    }

    private int b(GL10 gl10) {
        int K = this.l.K();
        if (K != 0) {
            return K;
        }
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public boolean c() {
        return this.r;
    }

    public void a(int i) {
        if (i > 1) {
            this.s = i;
        } else {
            this.s = 1;
        }
    }

    public void a(Object obj) {
        this.j = obj;
    }

    public Object s() {
        return this.j;
    }

    public void d(boolean z) {
    }

    public boolean t() {
        return false;
    }

    public int v() {
        return this.s;
    }

    public LatLng g() {
        return null;
    }

    public void z() {
    }

    public void e(boolean z) throws RemoteException {
        N();
    }

    public boolean A() {
        return false;
    }

    public void a(float f) throws RemoteException {
    }

    public void p() {
    }

    public void a(GL10 gl10, ab abVar) {
    }

    public float u() {
        return 0.0f;
    }

    public void b(int i, int i2) throws RemoteException {
        this.b = i;
        this.c = i2;
    }

    public int B() {
        return this.b;
    }

    public int C() {
        return this.c;
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

    public void b(float f) {
    }

    public float G() {
        return 0.0f;
    }

    public boolean H() {
        return false;
    }

    public void b(boolean z) {
    }

    public void a(IPoint iPoint) {
    }

    public IPoint I() {
        return null;
    }

    public void J() {
        this.q = false;
        this.k = 0;
    }
}
