package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: ScaleView */
public class en extends View {
    private String a = "";
    private int b = 0;
    private l c;
    private Paint d;
    private Paint e;
    private Rect f;
    private final int[] g = new int[]{10000000, 5000000, 2000000, 1000000, 500000, 200000, 100000, 50000, 30000, 20000, 10000, 5000, 2000, 1000, 500, SmsCheckResult.ESCT_200, 100, 50, 25, 10, 5};

    public en(Context context, l lVar) {
        super(context);
        this.c = lVar;
        this.d = new Paint();
        this.f = new Rect();
        this.d.setAntiAlias(true);
        this.d.setColor(-16777216);
        this.d.setStrokeWidth(g.a * 2.0f);
        this.d.setStyle(Style.STROKE);
        this.e = new Paint();
        this.e.setAntiAlias(true);
        this.e.setColor(-16777216);
        this.e.setTextSize(g.a * MapConfig.MAX_ZOOM_INDOOR);
    }

    protected void onDraw(Canvas canvas) {
        if (this.a != null && !this.a.equals("") && this.b != 0) {
            Point q = this.c.q();
            if (q != null) {
                this.e.getTextBounds(this.a, 0, this.a.length(), this.f);
                int i = q.x;
                int height = (q.y - this.f.height()) + 5;
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
            a();
            return;
        }
        a("");
        a(0);
        setVisibility(8);
    }

    public void a() {
        if (this.c != null) {
            try {
                CameraPosition cameraPosition = this.c.getCameraPosition();
                if (cameraPosition != null) {
                    LatLng latLng = cameraPosition.target;
                    float o = this.c.o();
                    double cos = (double) ((float) ((((Math.cos((latLng.latitude * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * 2.0d) * 3.141592653589793d) * 6378137.0d) / (Math.pow(2.0d, (double) o) * 256.0d)));
                    int u = (int) (((double) this.g[(int) o]) / (((double) this.c.u()) * cos));
                    String b = eh.b(this.g[(int) o]);
                    a(u);
                    a(b);
                    invalidate();
                }
            } catch (Throwable th) {
                fo.b(th, "AMapDelegateImpGLSurfaceView", "changeScaleState");
                th.printStackTrace();
            }
        }
    }
}
