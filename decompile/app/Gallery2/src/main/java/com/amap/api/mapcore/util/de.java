package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.amap.api.maps.AMap.MultiPositionInfoWindowAdapter;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.Animation.AnimationListener;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.interfaces.IInfoWindowManager;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: PopupOverlay */
public class de implements cq, ct, IInfoWindowManager {
    private Bitmap A = null;
    private Bitmap B = null;
    private boolean C = false;
    private di D;
    private di E;
    private boolean F = false;
    private boolean G = true;
    l a = null;
    private Context b;
    private MultiPositionInfoWindowAdapter c;
    private cr d;
    private boolean e = false;
    private int f = 0;
    private int g = 0;
    private int h = 0;
    private int i = 0;
    private FPoint j;
    private FloatBuffer k = null;
    private String l;
    private boolean m = true;
    private FloatBuffer n;
    private float o = 0.5f;
    private float p = WMElement.CAMERASIZEVALUE1B1;
    private boolean q;
    private Bitmap r;
    private Bitmap s;
    private Rect t = new Rect();
    private float u = 0.0f;
    private float v = 0.0f;
    private int w;
    private boolean x = true;
    private Bitmap y = null;
    private Bitmap z = null;

    public boolean f() {
        return this.x;
    }

    public void a(boolean z) {
        this.x = z;
    }

    public void a(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            c(this.y);
            this.y = bitmap;
        }
    }

    private synchronized void c(Bitmap bitmap) {
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private void d(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            c(this.z);
            this.z = bitmap;
        }
    }

    private void e(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            c(this.A);
            this.A = bitmap;
        }
    }

    private void f(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            c(this.B);
            this.B = bitmap;
        }
    }

    private Bitmap j() {
        return this.y;
    }

    private Bitmap k() {
        return this.A;
    }

    public de(l lVar, Context context) {
        this.b = context;
        this.a = lVar;
        this.l = getId();
    }

    public int g() {
        try {
            return this.r.getWidth();
        } catch (Throwable th) {
            return 0;
        }
    }

    public int h() {
        try {
            return this.r.getHeight();
        } catch (Throwable th) {
            return 0;
        }
    }

    public String getId() {
        if (this.l == null) {
            this.l = "PopupOverlay";
        }
        return this.l;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void b(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                if (!bitmap.isRecycled()) {
                    if (this.r != null) {
                        if (this.r.hashCode() == bitmap.hashCode()) {
                            return;
                        }
                    }
                    if (this.r != null) {
                        if (this.y == null && this.z == null && this.A == null && this.B == null) {
                            c(this.s);
                            this.s = this.r;
                        } else if (!g(this.r)) {
                            c(this.s);
                            this.s = this.r;
                        }
                    }
                    this.C = false;
                    this.r = bitmap;
                }
            } catch (Throwable th) {
            }
        }
    }

    private boolean g(Bitmap bitmap) {
        if (this.y != null && bitmap.hashCode() == this.y.hashCode()) {
            return true;
        }
        if (this.A != null && bitmap.hashCode() == this.A.hashCode()) {
            return true;
        }
        if (this.z != null && bitmap.hashCode() == this.z.hashCode()) {
            return true;
        }
        if (this.B != null && bitmap.hashCode() == this.B.hashCode()) {
            return true;
        }
        return false;
    }

    public void setVisible(boolean z) {
        if (!this.m && z) {
            this.q = true;
        }
        this.m = z;
    }

    public boolean isVisible() {
        return this.m;
    }

    public boolean equalsRemote(IOverlay iOverlay) throws RemoteException {
        if (equals(iOverlay) || iOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() {
        return super.hashCode();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(MapProjection mapProjection, int i, int i2) {
        if (this.j == null) {
            return false;
        }
        IPoint iPoint = new IPoint();
        mapProjection.map2Win(this.j.x, this.j.y, iPoint);
        int g = g();
        int h = h();
        int i3 = (int) (((float) (iPoint.x + this.f)) - (((float) g) * this.o));
        int i4 = (int) (((float) (iPoint.y + this.g)) + (((float) h) * (WMElement.CAMERASIZEVALUE1B1 - this.p)));
        if (i3 - g > i || i3 < (-g) * 2 || i4 < (-h) * 2 || i4 - h > i2 || this.r == null) {
            return false;
        }
        g = this.r.getWidth();
        h = this.r.getHeight();
        if (this.n == null) {
            this.n = eh.a(new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f});
        }
        int i5 = (int) ((((double) (WMElement.CAMERASIZEVALUE1B1 - this.u)) * 0.5d) * ((double) g));
        float[] fArr = new float[12];
        fArr[0] = (float) (i3 + i5);
        this.t.left = i3 + i5;
        fArr[1] = (float) (i2 - i4);
        fArr[2] = 0.0f;
        fArr[3] = (float) ((i3 + g) - i5);
        fArr[4] = (float) (i2 - i4);
        this.t.top = i4 - h;
        fArr[5] = 0.0f;
        fArr[6] = (float) ((i3 + g) - i5);
        this.t.right = g + i3;
        fArr[7] = (float) ((i2 - i4) + h);
        this.t.bottom = i4;
        fArr[8] = 0.0f;
        fArr[9] = (float) (i3 + i5);
        fArr[10] = (float) ((i2 - i4) + h);
        fArr[11] = 0.0f;
        if (this.k != null) {
            this.k = eh.a(fArr, this.k);
        } else {
            this.k = eh.a(fArr);
        }
        return true;
    }

    private void a(GL10 gl10, int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (floatBuffer != null && floatBuffer2 != null && i != 0) {
            gl10.glEnable(3042);
            gl10.glBlendFunc(1, 771);
            gl10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
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

    public void a(GL10 gl10, MapProjection mapProjection, int i, int i2) {
        if (this.m && this.j != null && this.r != null) {
            if (this.r.isRecycled()) {
            }
            if (!(this.C || this.r.isRecycled())) {
                try {
                    if (this.w == 0) {
                        this.w = b(gl10);
                    } else {
                        gl10.glDeleteTextures(1, new int[]{this.w}, 0);
                    }
                    if (!(this.r == null || this.r.isRecycled())) {
                        eh.b(gl10, this.w, this.r, false);
                        this.C = true;
                    }
                } catch (Throwable th) {
                    fo.b(th, "PopupOverlay", "drawMarker");
                    th.printStackTrace();
                    return;
                }
            }
            l();
            if (a(mapProjection, i, i2)) {
                gl10.glLoadIdentity();
                gl10.glViewport(0, 0, i, i2);
                gl10.glMatrixMode(5889);
                gl10.glLoadIdentity();
                gl10.glOrthof(0.0f, (float) i, 0.0f, (float) i2, WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                a(gl10, this.w, this.k, this.n);
                if (this.q) {
                    this.q = false;
                    p();
                }
            }
        }
    }

    private void l() {
        dn dnVar;
        if (!this.G && this.E != null && !this.E.l()) {
            this.F = true;
            dnVar = new dn();
            this.E.a(AnimationUtils.currentAnimationTimeMillis(), dnVar);
            if (dnVar != null && !Double.isNaN(dnVar.e) && !Double.isNaN(dnVar.f)) {
                this.u = (float) dnVar.e;
                this.v = (float) dnVar.f;
            }
        } else if (this.D == null || this.D.l()) {
            this.u = WMElement.CAMERASIZEVALUE1B1;
            this.v = WMElement.CAMERASIZEVALUE1B1;
            this.F = false;
        } else {
            this.G = false;
            this.F = true;
            this.f = this.h;
            this.g = this.i;
            dnVar = new dn();
            this.D.a(AnimationUtils.currentAnimationTimeMillis(), dnVar);
            if (dnVar != null && !Double.isNaN(dnVar.e) && !Double.isNaN(dnVar.f)) {
                this.u = (float) dnVar.e;
                this.v = (float) dnVar.f;
            }
        }
    }

    public void setInfoWindowAnimation(Animation animation, AnimationListener animationListener) {
    }

    public void setInfoWindowAppearAnimation(Animation animation) {
        if (this.E != null && this.E.equals(animation.glAnimation)) {
            try {
                this.D = animation.glAnimation.a();
                return;
            } catch (Throwable th) {
                fo.b(th, "PopupOverlay", "setInfoWindowDisappearAnimation");
                return;
            }
        }
        this.D = animation.glAnimation;
    }

    public void setInfoWindowBackColor(int i) {
    }

    public void setInfoWindowBackEnable(boolean z) {
    }

    public void setInfoWindowBackScale(float f, float f2) {
    }

    public void setInfoWindowDisappearAnimation(Animation animation) {
        if (this.D != null && this.D.equals(animation.glAnimation)) {
            try {
                this.E = animation.glAnimation.a();
                return;
            } catch (Throwable th) {
                fo.b(th, "PopupOverlay", "setInfoWindowDisappearAnimation");
                return;
            }
        }
        this.E = animation.glAnimation;
    }

    public void setInfoWindowMovingAnimation(Animation animation) {
    }

    public void startAnimation() {
    }

    private int b(GL10 gl10) {
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public void a(int i, int i2) throws RemoteException {
        if (this.F) {
            this.h = i;
            this.i = i2;
            return;
        }
        this.f = i;
        this.g = i2;
        this.h = i;
        this.i = i2;
    }

    public void setZIndex(float f) {
    }

    public float getZIndex() {
        return 0.0f;
    }

    public boolean a() {
        return false;
    }

    public void remove() throws RemoteException {
    }

    private void b(boolean z) {
        if (z) {
            b(j());
        } else {
            b(k());
        }
    }

    private void c(final boolean z) {
        if (this.E != null) {
            this.G = false;
            this.F = true;
            this.E.d();
            this.E.a(new AnimationListener(this) {
                final /* synthetic */ de b;

                public void onAnimationStart() {
                }

                public void onAnimationEnd() {
                    if (this.b.D != null) {
                        this.b.F = true;
                        this.b.D.d();
                        this.b.b(z);
                    }
                }
            });
        } else if (this.D == null) {
            b(z);
        } else {
            this.F = true;
            this.D.d();
            b(z);
        }
    }

    private void m() {
        if (this.x && this.r != null) {
            c(false);
        } else {
            b(k());
        }
        a(false);
    }

    private void n() {
        if (this.x || this.r == null) {
            b(j());
        } else {
            c(true);
        }
        a(true);
    }

    public void destroy() {
        if (this.e) {
            try {
                remove();
                o();
                if (this.n != null) {
                    this.n.clear();
                    this.n = null;
                }
                if (this.k != null) {
                    this.k.clear();
                    this.k = null;
                }
                this.j = null;
                this.w = 0;
            } catch (Throwable th) {
                fo.b(th, "PopupOverlay", "realDestroy");
                th.printStackTrace();
            }
        }
    }

    private void o() {
        if (this.r != null) {
            Bitmap bitmap = this.r;
            if (bitmap != null) {
                bitmap.recycle();
                this.r = null;
            }
        }
        if (!(this.s == null || this.s.isRecycled())) {
            this.s.recycle();
            this.s = null;
        }
        if (!(this.y == null || this.y.isRecycled())) {
            this.y.recycle();
        }
        if (!(this.z == null || this.z.isRecycled())) {
            this.z.recycle();
        }
        if (!(this.A == null || this.A.isRecycled())) {
            this.A.recycle();
        }
        if (this.B != null && !this.B.isRecycled()) {
            this.B.recycle();
        }
    }

    public boolean c() {
        return false;
    }

    public boolean b() throws RemoteException {
        return false;
    }

    public void a(GL10 gl10) throws RemoteException {
    }

    public void a(FPoint fPoint) {
        this.j = fPoint;
    }

    private void p() {
    }

    public boolean i() {
        return this.F;
    }

    public synchronized void a(cr crVar) throws RemoteException {
        if (crVar != null) {
            if (crVar.getTitle() == null) {
                if (crVar.getSnippet() == null) {
                    return;
                }
            }
            if (this.d != null) {
                if (!this.d.getId().equals(crVar.getId())) {
                    d();
                }
            }
            if (this.c != null) {
                this.d = crVar;
                crVar.a(true);
                setVisible(true);
                try {
                    a(a(this.c.getInfoWindow(new Marker(this.d))));
                    d(a(this.c.getInfoWindowClick(new Marker(this.d))));
                    e(a(this.c.getOverturnInfoWindow(new Marker(this.d))));
                    f(a(this.c.getOverturnInfoWindowClick(new Marker(this.d))));
                } catch (Throwable th) {
                    fo.b(th, "PopupOverlay", "getInfoWindow");
                    th.printStackTrace();
                }
            }
        }
    }

    private Bitmap a(View view) {
        if (view == null) {
            return null;
        }
        if ((view instanceof RelativeLayout) && this.b != null) {
            View linearLayout = new LinearLayout(this.b);
            linearLayout.setOrientation(1);
            linearLayout.addView(view);
            view = linearLayout;
        }
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(0);
        return eh.a(view);
    }

    private Rect q() {
        return new Rect(this.t.left, this.t.top, this.t.right, this.t.top + s());
    }

    private Rect r() {
        return new Rect(this.t.left, this.t.top, this.t.right, this.t.top + t());
    }

    private int s() {
        if (this.y == null || this.y.isRecycled()) {
            return 0;
        }
        return this.y.getHeight();
    }

    private int t() {
        if (this.A == null || this.A.isRecycled()) {
            return 0;
        }
        return this.A.getHeight();
    }

    public void e() {
        try {
            if (this.d != null && this.d.k()) {
                setVisible(true);
                Rect h = this.d.h();
                int c = this.d.c() + this.d.e();
                int f = (this.d.f() + this.d.d()) + 2;
                if (i()) {
                    if (this.r == null) {
                        if (this.y == null && this.A == null) {
                        }
                    }
                    return;
                }
                IMarkerAction iMarkerAction = this.d.getIMarkerAction();
                if (iMarkerAction == null || iMarkerAction.isInfoWindowEnable()) {
                    setVisible(true);
                    if (iMarkerAction != null && iMarkerAction.isInfoWindowAutoOverturn()) {
                        Rect q = q();
                        Rect r = r();
                        if (f()) {
                            r.offset(0, (h.height() + q.height()) + 2);
                        } else {
                            q.offset(0, -((h.height() + q.height()) + 2));
                        }
                        int a = this.a.a(iMarkerAction, q);
                        int a2 = this.a.a(iMarkerAction, r);
                        if (a > 0) {
                            if (a2 != 0) {
                                if (a2 > 0) {
                                    if (a >= a2) {
                                    }
                                }
                            }
                            f = (((this.d.f() + this.d.d()) + 2) + h.height()) + r.height();
                            m();
                            a(this.d.a());
                            a(c, f);
                            return;
                        }
                        n();
                        a(this.d.a());
                        a(c, f);
                        return;
                    }
                    a(this.d.a());
                    a(c, f);
                    n();
                    return;
                }
                setVisible(false);
                return;
            }
            setVisible(false);
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(MotionEvent motionEvent) {
        if (this.m && this.d != null && eh.a(this.t, (int) motionEvent.getX(), (int) motionEvent.getY())) {
            return true;
        }
        return false;
    }

    public synchronized void d() {
        setVisible(false);
        o();
    }

    public void a(MultiPositionInfoWindowAdapter multiPositionInfoWindowAdapter) throws RemoteException {
        this.c = multiPositionInfoWindowAdapter;
    }

    public boolean isAboveMaskLayer() {
        return false;
    }

    public void setAboveMaskLayer(boolean z) {
    }
}
