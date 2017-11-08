package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.view.View;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: Unknown */
class g extends View {
    Paint a = new Paint();
    Paint b = new Paint();
    ArrayList<a> c = null;

    /* compiled from: Unknown */
    private static class a {
        float a;
        float b;
        float c;
        float d;

        public a(float f, float f2, float f3, float f4) {
            this.a = f;
            this.b = f2;
            this.c = f3;
            this.d = f4;
        }
    }

    public g(Context context) {
        super(context);
        b();
    }

    private void b() {
        this.b.setAntiAlias(true);
        this.b.setStrokeWidth(6.0f);
        this.b.setColor(-1);
        this.b.setStyle(Style.STROKE);
        this.b.setStrokeJoin(Join.ROUND);
        this.b.setStrokeCap(Cap.ROUND);
        this.b.setAlpha(75);
        this.a.setAntiAlias(true);
        this.a.setStrokeWidth(BitmapDescriptorFactory.HUE_ORANGE);
        this.a.setColor(Color.parseColor("#ff3143"));
        this.a.setStyle(Style.FILL);
        this.a.setStrokeJoin(Join.ROUND);
        this.a.setStrokeCap(Cap.ROUND);
        this.a.setMaskFilter(new BlurMaskFilter(8.0f, Blur.NORMAL));
        this.a.setAlpha(75);
        this.c = new ArrayList();
    }

    public void a() {
        this.c.clear();
    }

    public void a(float f, float f2, float f3, float f4) {
        this.c.add(new a(f, f2, f3, f4));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.c != null) {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                canvas.drawLine(aVar.a, aVar.b, aVar.c, aVar.d, this.b);
                canvas.drawCircle(aVar.a, aVar.b, 10.0f, this.a);
                canvas.drawCircle(aVar.c, aVar.d, 10.0f, this.a);
            }
        }
    }
}
