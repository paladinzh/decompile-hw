package com.amap.api.mapcore;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.au;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: GroundOverlayDelegateImp */
public class aa implements af {
    private ab a;
    private BitmapDescriptor b;
    private LatLng c;
    private float d;
    private float e;
    private LatLngBounds f;
    private float g;
    private float h;
    private boolean i = true;
    private float j = 0.0f;
    private float k = 0.5f;
    private float l = 0.5f;
    private String m;
    private FloatBuffer n = null;
    private FloatBuffer o;
    private int p;
    private boolean q = false;
    private boolean r = false;

    public aa(ab abVar) {
        this.a = abVar;
        try {
            this.m = c();
        } catch (Throwable e) {
            ce.a(e, "GroundOverlayDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public void b() throws RemoteException {
        this.a.f(this.p);
        this.a.a(c());
        this.a.f(false);
    }

    public String c() throws RemoteException {
        if (this.m == null) {
            this.m = w.a("GroundOverlay");
        }
        return this.m;
    }

    public void a(float f) throws RemoteException {
        this.h = f;
        this.a.M();
        this.a.f(false);
    }

    public float d() throws RemoteException {
        return this.h;
    }

    public void a(boolean z) throws RemoteException {
        this.i = z;
        this.a.f(false);
    }

    public boolean e() throws RemoteException {
        return this.i;
    }

    public boolean a(aj ajVar) throws RemoteException {
        if (equals(ajVar) || ajVar.c().equals(c())) {
            return true;
        }
        return false;
    }

    public int f() throws RemoteException {
        return super.hashCode();
    }

    public void g() throws RemoteException {
        this.r = false;
        if (this.c == null) {
            r();
        } else if (this.f != null) {
            s();
        } else {
            q();
        }
    }

    private void q() {
        if (this.c != null) {
            double cos = ((double) this.d) / ((Math.cos(this.c.latitude * 0.01745329251994329d) * 6371000.79d) * 0.01745329251994329d);
            double d = ((double) this.e) / 111194.94043265979d;
            this.f = new LatLngBounds(new LatLng(this.c.latitude - (((double) (1.0f - this.l)) * d), this.c.longitude - (((double) this.k) * cos)), new LatLng((d * ((double) this.l)) + this.c.latitude, (cos * ((double) (1.0f - this.k))) + this.c.longitude));
            s();
        }
    }

    private void r() {
        if (this.f != null) {
            LatLng latLng = this.f.southwest;
            LatLng latLng2 = this.f.northeast;
            this.c = new LatLng(latLng.latitude + (((double) (1.0f - this.l)) * (latLng2.latitude - latLng.latitude)), latLng.longitude + (((double) this.k) * (latLng2.longitude - latLng.longitude)));
            this.d = (float) (((Math.cos(this.c.latitude * 0.01745329251994329d) * 6371000.79d) * (latLng2.longitude - latLng.longitude)) * 0.01745329251994329d);
            this.e = (float) (((latLng2.latitude - latLng.latitude) * 6371000.79d) * 0.01745329251994329d);
            s();
        }
    }

    private void s() {
        if (this.f != null) {
            float[] fArr = new float[12];
            FPoint fPoint = new FPoint();
            FPoint fPoint2 = new FPoint();
            FPoint fPoint3 = new FPoint();
            FPoint fPoint4 = new FPoint();
            this.a.a(this.f.southwest.latitude, this.f.southwest.longitude, fPoint);
            this.a.a(this.f.southwest.latitude, this.f.northeast.longitude, fPoint2);
            this.a.a(this.f.northeast.latitude, this.f.northeast.longitude, fPoint3);
            this.a.a(this.f.northeast.latitude, this.f.southwest.longitude, fPoint4);
            if (this.g != 0.0f) {
                double d = (double) (fPoint2.x - fPoint.x);
                double d2 = (double) (fPoint2.y - fPoint3.y);
                DPoint dPoint = new DPoint();
                dPoint.x = ((double) fPoint.x) + (((double) this.k) * d);
                dPoint.y = ((double) fPoint.y) - (((double) (1.0f - this.l)) * d2);
                a(dPoint, 0.0d, 0.0d, d, d2, fPoint);
                a(dPoint, d, 0.0d, d, d2, fPoint2);
                a(dPoint, d, d2, d, d2, fPoint3);
                a(dPoint, 0.0d, d2, d, d2, fPoint4);
            }
            fArr[0] = fPoint.x;
            fArr[1] = fPoint.y;
            fArr[2] = 0.0f;
            fArr[3] = fPoint2.x;
            fArr[4] = fPoint2.y;
            fArr[5] = 0.0f;
            fArr[6] = fPoint3.x;
            fArr[7] = fPoint3.y;
            fArr[8] = 0.0f;
            fArr[9] = fPoint4.x;
            fArr[10] = fPoint4.y;
            fArr[11] = 0.0f;
            if (this.n != null) {
                this.n = bj.a(fArr, this.n);
            } else {
                this.n = bj.a(fArr);
            }
        }
    }

    private void a(DPoint dPoint, double d, double d2, double d3, double d4, FPoint fPoint) {
        double d5 = d - (((double) this.k) * d3);
        double d6 = (((double) (1.0f - this.l)) * d4) - d2;
        double d7 = ((double) (-this.g)) * 0.01745329251994329d;
        fPoint.x = (float) (dPoint.x + ((Math.cos(d7) * d5) + (Math.sin(d7) * d6)));
        fPoint.y = (float) (((d6 * Math.cos(d7)) - (d5 * Math.sin(d7))) + dPoint.y);
    }

    private void t() {
        if (this.b != null) {
            int width = this.b.getWidth();
            float width2 = ((float) width) / ((float) this.b.getBitmap().getWidth());
            float height = ((float) this.b.getHeight()) / ((float) this.b.getBitmap().getHeight());
            this.o = bj.a(new float[]{0.0f, height, width2, height, width2, 0.0f, 0.0f, 0.0f});
        }
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.i) {
            if (this.c != null || this.f != null) {
                if (this.b != null) {
                    if (!this.q) {
                        Bitmap bitmap = this.b.getBitmap();
                        if (!(bitmap == null || bitmap.isRecycled())) {
                            if (this.p != 0) {
                                gl10.glDeleteTextures(1, new int[]{this.p}, 0);
                            } else {
                                this.p = this.a.K();
                                if (this.p == 0) {
                                    int[] iArr = new int[]{0};
                                    gl10.glGenTextures(1, iArr, 0);
                                    this.p = iArr[0];
                                }
                            }
                            bj.b(gl10, this.p, bitmap, true);
                        }
                        this.q = true;
                    }
                    if (this.d != 0.0f || this.e != 0.0f) {
                        a(gl10, this.p, this.n, this.o);
                        this.r = true;
                    }
                }
            }
        }
    }

    private void a(GL10 gl10, int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (floatBuffer != null && floatBuffer2 != null) {
            gl10.glEnable(3042);
            gl10.glTexEnvf(8960, 8704, 8448.0f);
            gl10.glBlendFunc(1, 771);
            gl10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f - this.j);
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

    public void j() {
        try {
            b();
            if (this.b != null) {
                Bitmap bitmap = this.b.getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                    this.b = null;
                }
            }
            if (this.o != null) {
                this.o.clear();
                this.o = null;
            }
            if (this.n != null) {
                this.n.clear();
                this.n = null;
            }
            this.c = null;
            this.f = null;
        } catch (Throwable th) {
            ce.a(th, "GroundOverlayDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "GroundOverlayDelegateImp destroy");
        }
    }

    public boolean a() {
        Rect k = this.a.k();
        if (k == null) {
            return true;
        }
        IPoint iPoint = new IPoint();
        if (this.c != null) {
            this.a.b(this.c.latitude, this.c.longitude, iPoint);
        }
        return k.contains(iPoint.x, iPoint.y);
    }

    public void a(LatLng latLng) throws RemoteException {
        this.c = latLng;
        q();
        this.a.f(false);
    }

    public LatLng h() throws RemoteException {
        return this.c;
    }

    public void b(float f) throws RemoteException {
        au.b(f >= 0.0f, "Width must be non-negative");
        if (this.q && this.d != f) {
            this.d = f;
            this.e = f;
            q();
        } else {
            this.d = f;
            this.e = f;
        }
        this.a.f(false);
    }

    public void a(float f, float f2) throws RemoteException {
        boolean z = true;
        au.b(f >= 0.0f, "Width must be non-negative");
        if (f2 < 0.0f) {
            z = false;
        }
        au.b(z, "Height must be non-negative");
        if (!this.q || this.d == f || this.e == f2) {
            this.d = f;
            this.e = f2;
        } else {
            this.d = f;
            this.e = f2;
            q();
        }
        this.a.f(false);
    }

    public float i() throws RemoteException {
        return this.d;
    }

    public float l() throws RemoteException {
        return this.e;
    }

    public void a(LatLngBounds latLngBounds) throws RemoteException {
        this.f = latLngBounds;
        r();
        this.a.f(false);
    }

    public LatLngBounds m() throws RemoteException {
        return this.f;
    }

    public void c(float f) throws RemoteException {
        float f2 = ((f % 360.0f) + 360.0f) % 360.0f;
        if (this.q && ((double) Math.abs(this.g - f2)) > 1.0E-7d) {
            this.g = f2;
            s();
        } else {
            this.g = f2;
        }
        this.a.f(false);
    }

    public float n() throws RemoteException {
        return this.g;
    }

    public void d(float f) throws RemoteException {
        boolean z = f >= 0.0f && f <= 1.0f;
        au.b(z, "Transparency must be in the range [0..1]");
        this.j = f;
        this.a.f(false);
    }

    public float o() throws RemoteException {
        return this.j;
    }

    public void a(BitmapDescriptor bitmapDescriptor) throws RemoteException {
        this.b = bitmapDescriptor;
        t();
        if (this.q) {
            this.q = false;
        }
        this.a.f(false);
    }

    public void b(float f, float f2) throws RemoteException {
        this.k = f;
        this.l = f2;
        this.a.f(false);
    }

    public void p() {
        this.q = false;
        this.p = 0;
    }

    public boolean k() {
        return this.r;
    }
}
