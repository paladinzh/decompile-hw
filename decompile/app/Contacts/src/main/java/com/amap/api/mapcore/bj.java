package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.autonavi.amap.mapcore.VTMCDataCache;

/* compiled from: ScaleView */
class bj extends View {
    private String a = "";
    private int b = 0;
    private ab c;
    private Paint d;
    private Paint e;
    private Rect f;
    private final int[] g = new int[]{10000000, 5000000, 2000000, 1000000, 500000, 200000, 100000, 50000, 30000, 20000, 10000, 5000, AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, 1000, VTMCDataCache.MAXSIZE, 200, 100, 50, 25, 10, 5};

    public void a() {
        this.d = null;
        this.e = null;
        this.f = null;
        this.a = null;
    }

    public bj(Context context) {
        super(context);
    }

    public bj(Context context, ab abVar) {
        super(context);
        this.c = abVar;
        this.d = new Paint();
        this.f = new Rect();
        this.d.setAntiAlias(true);
        this.d.setColor(-16777216);
        this.d.setStrokeWidth(s.a * 2.0f);
        this.d.setStyle(Style.STROKE);
        this.e = new Paint();
        this.e.setAntiAlias(true);
        this.e.setColor(-16777216);
        this.e.setTextSize(s.a * 20.0f);
    }

    protected void onDraw(Canvas canvas) {
        if (this.a != null && !this.a.equals("") && this.b != 0) {
            Point I = this.c.I();
            if (I != null) {
                this.e.getTextBounds(this.a, 0, this.a.length(), this.f);
                int i = I.x;
                int height = (I.y - this.f.height()) + 5;
                canvas.drawText(this.a, (float) i, (float) height, this.e);
                int height2 = height + (this.f.height() - 5);
                canvas.drawLine((float) i, (float) (height2 - 2), (float) i, (float) (height2 + 2), this.d);
                canvas.drawLine((float) i, (float) height2, (float) (this.b + i), (float) height2, this.d);
                canvas.drawLine((float) (this.b + i), (float) (height2 - 2), (float) (this.b + i), (float) (height2 + 2), this.d);
            }
        }
    }

    public void a(String str) {
        this.a = str;
    }

    public void a(int i) {
        this.b = i;
    }

    public void a(boolean z) {
        if (z) {
            setVisibility(0);
            b();
            return;
        }
        a("");
        a(0);
        setVisibility(8);
    }

    void b() {
        if (this.c != null) {
            try {
                CameraPosition r = this.c.r();
                if (r != null) {
                    LatLng latLng = r.target;
                    float F = this.c.F();
                    double cos = (double) ((float) ((((Math.cos((latLng.latitude * 3.141592653589793d) / 180.0d) * 2.0d) * 3.141592653589793d) * 6378137.0d) / (Math.pow(2.0d, (double) F) * 256.0d)));
                    int W = (int) (((double) this.g[(int) F]) / (((double) this.c.W()) * cos));
                    String b = com.amap.api.mapcore.util.bj.b(this.g[(int) F]);
                    a(W);
                    a(b);
                    invalidate();
                }
            } catch (Throwable th) {
                ce.a(th, "AMapDelegateImpGLSurfaceView", "changeScaleState");
                th.printStackTrace();
            }
        }
    }
}
