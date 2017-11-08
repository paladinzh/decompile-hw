package com.amap.api.mapcore.util;

import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FPointBounds;
import com.autonavi.amap.mapcore.FPointBounds.Builder;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: GroundOverlayDelegateImp */
public class cm implements cp {
    FPointBounds a = null;
    private l b;
    private BitmapDescriptor c;
    private LatLng d;
    private float e;
    private float f;
    private LatLngBounds g;
    private float h;
    private float i;
    private boolean j = true;
    private float k = 0.0f;
    private float l = 0.5f;
    private float m = 0.5f;
    private String n;
    private FloatBuffer o = null;
    private FloatBuffer p;
    private int q;
    private boolean r = false;
    private boolean s = false;
    private List<Float> t = new ArrayList();
    private List<Float> u = new ArrayList();

    public cm(l lVar) {
        this.b = lVar;
        try {
            this.n = getId();
        } catch (Throwable e) {
            fo.b(e, "GroundOverlayDelegateImp", "create");
            e.printStackTrace();
        }
    }

    public void remove() throws RemoteException {
        this.b.a(getId());
        this.b.setRunLowFrame(false);
    }

    public String getId() throws RemoteException {
        if (this.n == null) {
            this.n = j.a("GroundOverlay");
        }
        return this.n;
    }

    public void setZIndex(float f) throws RemoteException {
        this.i = f;
        this.b.r();
        this.b.setRunLowFrame(false);
    }

    public float getZIndex() throws RemoteException {
        return this.i;
    }

    public void setVisible(boolean z) throws RemoteException {
        this.j = z;
        this.b.setRunLowFrame(false);
    }

    public boolean isVisible() throws RemoteException {
        return this.j;
    }

    public boolean equalsRemote(IOverlay iOverlay) throws RemoteException {
        if (equals(iOverlay) || iOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() throws RemoteException {
        return super.hashCode();
    }

    public boolean b() throws RemoteException {
        this.s = false;
        if (this.d == null) {
            f();
        } else if (this.g != null) {
            g();
        } else {
            e();
        }
        return true;
    }

    private void e() {
        if (this.d != null) {
            double cos = ((double) this.e) / ((Math.cos(this.d.latitude * 0.01745329251994329d) * 6371000.79d) * 0.01745329251994329d);
            double d = ((double) this.f) / 111194.94043265979d;
            this.g = new LatLngBounds(new LatLng(this.d.latitude - (((double) (WMElement.CAMERASIZEVALUE1B1 - this.m)) * d), this.d.longitude - (((double) this.l) * cos)), new LatLng((d * ((double) this.m)) + this.d.latitude, (cos * ((double) (WMElement.CAMERASIZEVALUE1B1 - this.l))) + this.d.longitude));
            g();
        }
    }

    private void f() {
        if (this.g != null) {
            LatLng latLng = this.g.southwest;
            LatLng latLng2 = this.g.northeast;
            this.d = new LatLng(latLng.latitude + (((double) (WMElement.CAMERASIZEVALUE1B1 - this.m)) * (latLng2.latitude - latLng.latitude)), latLng.longitude + (((double) this.l) * (latLng2.longitude - latLng.longitude)));
            this.e = (float) (((Math.cos(this.d.latitude * 0.01745329251994329d) * 6371000.79d) * (latLng2.longitude - latLng.longitude)) * 0.01745329251994329d);
            this.f = (float) (((latLng2.latitude - latLng.latitude) * 6371000.79d) * 0.01745329251994329d);
            g();
        }
    }

    private void g() {
        if (this.g != null) {
            float[] fArr = new float[12];
            FPoint fPoint = new FPoint();
            FPoint fPoint2 = new FPoint();
            FPoint fPoint3 = new FPoint();
            FPoint fPoint4 = new FPoint();
            this.b.a(this.g.southwest.latitude, this.g.southwest.longitude, fPoint);
            this.b.a(this.g.southwest.latitude, this.g.northeast.longitude, fPoint2);
            this.b.a(this.g.northeast.latitude, this.g.northeast.longitude, fPoint3);
            this.b.a(this.g.northeast.latitude, this.g.southwest.longitude, fPoint4);
            Builder builder = new Builder();
            builder.include(fPoint);
            builder.include(fPoint4);
            builder.include(fPoint2);
            builder.include(fPoint3);
            this.a = builder.build();
            if (this.h != 0.0f) {
                double d = (double) (fPoint2.x - fPoint.x);
                double d2 = (double) (fPoint2.y - fPoint3.y);
                DPoint dPoint = new DPoint();
                dPoint.x = ((double) fPoint.x) + (((double) this.l) * d);
                dPoint.y = ((double) fPoint.y) - (((double) (WMElement.CAMERASIZEVALUE1B1 - this.m)) * d2);
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
            if (this.o != null) {
                this.o = eh.a(fArr, this.o);
            } else {
                this.o = eh.a(fArr);
            }
        }
    }

    private void a(DPoint dPoint, double d, double d2, double d3, double d4, FPoint fPoint) {
        double d5 = d - (((double) this.l) * d3);
        double d6 = (((double) (WMElement.CAMERASIZEVALUE1B1 - this.m)) * d4) - d2;
        double d7 = ((double) (-this.h)) * 0.01745329251994329d;
        fPoint.x = (float) (dPoint.x + ((Math.cos(d7) * d5) + (Math.sin(d7) * d6)));
        fPoint.y = (float) (((d6 * Math.cos(d7)) - (d5 * Math.sin(d7))) + dPoint.y);
    }

    private void h() {
        if (this.c != null) {
            int width = this.c.getWidth();
            float width2 = ((float) width) / ((float) this.c.getBitmap().getWidth());
            float height = ((float) this.c.getHeight()) / ((float) this.c.getBitmap().getHeight());
            this.p = eh.a(new float[]{0.0f, height, width2, height, width2, 0.0f, 0.0f, 0.0f});
        }
    }

    public void a(GL10 gl10) throws RemoteException {
        if (this.j) {
            if (this.d != null || this.g != null) {
                if (this.c != null) {
                    if (!this.r) {
                        Bitmap bitmap = this.c.getBitmap();
                        if (!(bitmap == null || bitmap.isRecycled())) {
                            if (this.q != 0) {
                                gl10.glDeleteTextures(1, new int[]{this.q}, 0);
                            } else {
                                int[] iArr = new int[]{0};
                                gl10.glGenTextures(1, iArr, 0);
                                this.q = iArr[0];
                            }
                            eh.b(gl10, this.q, bitmap, true);
                        }
                        this.r = true;
                    }
                    if (this.e != 0.0f || this.f != 0.0f) {
                        a(gl10, this.q, this.o, this.p);
                        this.s = true;
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
            gl10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1 - this.k);
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

    public void destroy() {
        try {
            remove();
            if (this.c != null) {
                Bitmap bitmap = this.c.getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                    this.c = null;
                }
            }
            if (this.p != null) {
                this.p.clear();
                this.p = null;
            }
            if (this.o != null) {
                this.o.clear();
                this.o = null;
            }
            this.d = null;
            this.g = null;
        } catch (Throwable th) {
            fo.b(th, "GroundOverlayDelegateImp", "destroy");
            th.printStackTrace();
        }
    }

    public boolean a() {
        if (this.a == null) {
            return false;
        }
        FPoint[] p = this.b.p();
        this.t.clear();
        this.u.clear();
        for (int i = 0; i < p.length; i++) {
            FPoint fPoint = p[i];
            this.t.add(i, Float.valueOf(fPoint.x));
            this.u.add(i, Float.valueOf(fPoint.y));
        }
        if (eh.a(new ed(this.a.southwest.x, this.a.northeast.x, this.a.southwest.y, this.a.northeast.y), new ed(((Float) Collections.min(this.t)).floatValue(), ((Float) Collections.max(this.t)).floatValue(), ((Float) Collections.min(this.u)).floatValue(), ((Float) Collections.max(this.u)).floatValue()))) {
            return true;
        }
        return false;
    }

    public void setPosition(LatLng latLng) throws RemoteException {
        this.d = latLng;
        e();
        this.b.setRunLowFrame(false);
    }

    public LatLng getPosition() throws RemoteException {
        return this.d;
    }

    public void setDimensions(float f) throws RemoteException {
        if (f <= 0.0f) {
            Log.w("GroundOverlayDelegateImp", "Width must be non-negative");
        }
        if (this.r && this.e != f) {
            this.e = f;
            this.f = f;
            e();
        } else {
            this.e = f;
            this.f = f;
        }
        this.b.setRunLowFrame(false);
    }

    public void setDimensions(float f, float f2) throws RemoteException {
        if ((f <= 0.0f) || f2 <= 0.0f) {
            Log.w("GroundOverlayDelegateImp", "Width and Height must be non-negative");
        }
        if (!this.r || this.e == f || this.f == f2) {
            this.e = f;
            this.f = f2;
        } else {
            this.e = f;
            this.f = f2;
            e();
        }
        this.b.setRunLowFrame(false);
    }

    public float getWidth() throws RemoteException {
        return this.e;
    }

    public float getHeight() throws RemoteException {
        return this.f;
    }

    public void setPositionFromBounds(LatLngBounds latLngBounds) throws RemoteException {
        this.g = latLngBounds;
        f();
        this.b.setRunLowFrame(false);
    }

    public LatLngBounds getBounds() throws RemoteException {
        return this.g;
    }

    public void setBearing(float f) throws RemoteException {
        float f2 = ((f % 360.0f) + 360.0f) % 360.0f;
        if (this.r && ((double) Math.abs(this.h - f2)) > 1.0E-7d) {
            this.h = f2;
            g();
        } else {
            this.h = f2;
        }
        this.b.setRunLowFrame(false);
    }

    public float getBearing() throws RemoteException {
        return this.h;
    }

    public void setTransparency(float f) throws RemoteException {
        if (f < 0.0f) {
            Log.w("GroundOverlayDelegateImp", "Transparency must be in the range [0..1]");
        }
        this.k = f;
        this.b.setRunLowFrame(false);
    }

    public float getTransparency() throws RemoteException {
        return this.k;
    }

    public void setImage(BitmapDescriptor bitmapDescriptor) throws RemoteException {
        this.c = bitmapDescriptor;
        h();
        if (this.r) {
            this.r = false;
        }
        this.b.setRunLowFrame(false);
    }

    public void a(float f, float f2) throws RemoteException {
        this.l = f;
        this.m = f2;
        this.b.setRunLowFrame(false);
    }

    public void d() {
        this.r = false;
        this.q = 0;
    }

    public boolean c() {
        return this.s;
    }

    public boolean isAboveMaskLayer() {
        return false;
    }

    public void setAboveMaskLayer(boolean z) {
    }
}
