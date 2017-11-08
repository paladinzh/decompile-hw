package com.amap.api.mapcore.util;

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
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TextOptions;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.autonavi.amap.mapcore.interfaces.IOverlayImage;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TextDelegateImp */
public class df implements cx {
    private static int a = 0;
    private Paint A = new Paint();
    private boolean B = false;
    private boolean C = false;
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
    private float o = WMElement.CAMERASIZEVALUE1B1;
    private boolean p = true;
    private q q;
    private FloatBuffer r;
    private Object s;
    private String t;
    private int u;
    private int v;
    private int w;
    private Typeface x;
    private float y;
    private Rect z = new Rect();

    private static String a(String str) {
        a++;
        return str + a;
    }

    public void setRotateAngle(float f) {
        this.c = f;
        this.b = (((-f) % 360.0f) + 360.0f) % 360.0f;
        d();
    }

    public void destroy() {
        try {
            this.B = true;
            remove();
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
            fo.b(th, "TextDelegateImp", "destroy");
            th.printStackTrace();
            Log.d("destroy erro", "TextDelegateImp destroy");
        }
    }

    public df(TextOptions textOptions, q qVar) throws RemoteException {
        this.q = qVar;
        if (textOptions.getPosition() != null) {
            this.m = textOptions.getPosition();
        }
        setAlign(textOptions.getAlignX(), textOptions.getAlignY());
        this.p = textOptions.isVisible();
        this.t = textOptions.getText();
        this.u = textOptions.getBackgroundColor();
        this.v = textOptions.getFontColor();
        this.w = textOptions.getFontSize();
        this.s = textOptions.getObject();
        this.y = textOptions.getZIndex();
        this.x = textOptions.getTypeface();
        this.l = getId();
        setRotateAngle(textOptions.getRotate());
        a();
        i();
    }

    private void a() {
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
                this.r = eh.a(new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f});
            } catch (Throwable th) {
                fo.b(th, "TextDelegateImp", "initBitmap");
            }
        }
    }

    private int b() {
        return this.i;
    }

    private int c() {
        return this.j;
    }

    public synchronized boolean remove() {
        d();
        this.p = false;
        return this.q.a((cu) this);
    }

    private void d() {
        if (this.q.a() != null) {
            this.q.a().setRunLowFrame(false);
        }
    }

    public LatLng getPosition() {
        return this.m;
    }

    public String getId() {
        if (this.l == null) {
            this.l = a("Text");
        }
        return this.l;
    }

    public void setPosition(LatLng latLng) {
        this.m = latLng;
        i();
        d();
    }

    public boolean isInfoWindowShown() {
        return false;
    }

    public void setVisible(boolean z) {
        if (this.p != z) {
            this.p = z;
            i();
            d();
        }
    }

    public boolean isVisible() {
        return this.p;
    }

    public void setZIndex(float f) {
        this.y = f;
        this.q.g();
    }

    public float getZIndex() {
        return this.y;
    }

    public void setAnchor(float f, float f2) {
    }

    public float getAnchorU() {
        return this.n;
    }

    public float getAnchorV() {
        return this.o;
    }

    public boolean equalsRemote(IOverlayImage iOverlayImage) throws RemoteException {
        if (equals(iOverlayImage) || iOverlayImage.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() {
        return super.hashCode();
    }

    public boolean i() {
        try {
            if (this.m == null) {
                return false;
            }
            this.q.a().a(this.m.latitude, this.m.longitude, this.f);
            return true;
        } catch (Throwable th) {
            th.printStackTrace();
            return false;
        }
    }

    private void a(l lVar) throws RemoteException {
        float[] a = eh.a(lVar, 0, this.f, this.b, b(), c(), this.n, this.o);
        if (this.k != null) {
            this.k = eh.a(a, this.k);
        } else {
            this.k = eh.a(a);
        }
        if (this.g != 0) {
            a(this.g, this.k, this.r);
        }
    }

    private void a(int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (i != 0 && floatBuffer != null && floatBuffer2 != null) {
            GLES10.glEnable(3042);
            GLES10.glBlendFunc(1, 771);
            GLES10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
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

    public void a(GL10 gl10, l lVar) {
        if (this.p && !this.B && this.m != null && this.h != null) {
            if (!this.C) {
                try {
                    if (!(this.h == null || this.h.isRecycled())) {
                        if (this.g == 0) {
                            this.g = a(gl10);
                        }
                        eh.b(gl10, this.g, this.h, false);
                        this.C = true;
                        this.h.recycle();
                    }
                } catch (Throwable th) {
                    fo.b(th, "TextDelegateImp", "loadtexture");
                    th.printStackTrace();
                    return;
                }
            }
            try {
                a(lVar);
            } catch (Throwable th2) {
                fo.b(th2, "TextDelegateImp", "drawMarker");
            }
        }
    }

    private int a(GL10 gl10) {
        int[] iArr = new int[]{0};
        gl10.glGenTextures(1, iArr, 0);
        return iArr[0];
    }

    public boolean j() {
        return true;
    }

    public void setObject(Object obj) {
        this.s = obj;
    }

    public Object getObject() {
        return this.s;
    }

    public float getRotateAngle() {
        return this.c;
    }

    public Rect h() {
        return null;
    }

    public void setText(String str) throws RemoteException {
        this.t = str;
        e();
    }

    public String getText() throws RemoteException {
        return this.t;
    }

    public void setBackgroundColor(int i) throws RemoteException {
        this.u = i;
        e();
    }

    public int getBackgroundColor() throws RemoteException {
        return this.u;
    }

    public void setFontColor(int i) throws RemoteException {
        this.v = i;
        e();
    }

    public int getFontColor() throws RemoteException {
        return this.v;
    }

    public void setFontSize(int i) throws RemoteException {
        this.w = i;
        e();
    }

    public int getFontSize() throws RemoteException {
        return this.w;
    }

    public void setTypeface(Typeface typeface) throws RemoteException {
        this.x = typeface;
        e();
    }

    public Typeface getTypeface() throws RemoteException {
        return this.x;
    }

    public void setAlign(int i, int i2) throws RemoteException {
        this.d = i;
        switch (i) {
            case 1:
                this.n = 0.0f;
                break;
            case 2:
                this.n = WMElement.CAMERASIZEVALUE1B1;
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
                this.o = WMElement.CAMERASIZEVALUE1B1;
                break;
            case 32:
                this.o = 0.5f;
                break;
            default:
                this.o = 0.5f;
                break;
        }
        d();
    }

    public int getAlignX() throws RemoteException {
        return this.d;
    }

    public int getAlignY() {
        return this.e;
    }

    private synchronized void e() {
        a();
        this.C = false;
        d();
    }

    public boolean k() {
        if (this.f != null && eh.a(this.f, this.q.a().p())) {
            return true;
        }
        return false;
    }

    public void l() {
        this.C = false;
        this.g = 0;
        a();
    }

    public boolean m() {
        return false;
    }

    public IMarkerAction getIMarkerAction() {
        return null;
    }
}
