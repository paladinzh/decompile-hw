package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.View;
import com.amap.api.mapcore.s.a;
import com.amap.api.mapcore.util.bh;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import java.io.InputStream;

/* compiled from: WaterMarkerView */
class br extends View {
    int a = 10;
    private Bitmap b;
    private Bitmap c;
    private Bitmap d;
    private Bitmap e;
    private Paint f = new Paint();
    private boolean g = false;
    private int h = 0;
    private AMapDelegateImp i;
    private int j = 0;

    public void a() {
        try {
            if (this.b != null) {
                this.b.recycle();
            }
            if (this.c != null) {
                this.c.recycle();
            }
            this.b = null;
            this.c = null;
            if (this.d != null) {
                this.d.recycle();
                this.d = null;
            }
            if (this.e != null) {
                this.e.recycle();
                this.e = null;
            }
            this.f = null;
        } catch (Throwable th) {
            ce.a(th, "WaterMarkerView", "destory");
            th.printStackTrace();
        }
    }

    public br(Context context) {
        super(context);
    }

    public br(Context context, AMapDelegateImp aMapDelegateImp) {
        super(context);
        this.i = aMapDelegateImp;
        try {
            InputStream open;
            if (s.g != a.ALIBABA) {
                open = bh.a(context).open("ap.data");
            } else {
                open = bh.a(context).open("apl.data");
            }
            this.d = BitmapFactory.decodeStream(open);
            this.b = bj.a(this.d, s.a);
            open.close();
            if (s.g != a.ALIBABA) {
                open = bh.a(context).open("ap1.data");
            } else {
                open = bh.a(context).open("apl1.data");
            }
            this.e = BitmapFactory.decodeStream(open);
            this.c = bj.a(this.e, s.a);
            open.close();
            this.h = this.c.getHeight();
        } catch (Throwable th) {
            ce.a(th, "WaterMarkerView", "create");
            th.printStackTrace();
        }
        this.f.setAntiAlias(true);
        this.f.setColor(-16777216);
        this.f.setStyle(Style.STROKE);
    }

    public Bitmap b() {
        if (this.g) {
            return this.c;
        }
        return this.b;
    }

    public void a(boolean z) {
        this.g = z;
        if (z) {
            this.f.setColor(-1);
        } else {
            this.f.setColor(-16777216);
        }
        invalidate();
    }

    public Point c() {
        return new Point(this.a, (getHeight() - this.h) - 10);
    }

    public void a(int i) {
        this.j = i;
    }

    public void onDraw(Canvas canvas) {
        try {
            if (this.c != null) {
                int width = this.c.getWidth();
                if (this.j == 1) {
                    this.a = (this.i.n() - width) / 2;
                } else if (this.j != 2) {
                    this.a = 10;
                } else {
                    this.a = (this.i.n() - width) - 10;
                }
                if (s.g != a.ALIBABA) {
                    canvas.drawBitmap(b(), (float) this.a, (float) ((getHeight() - this.h) - 8), this.f);
                } else {
                    canvas.drawBitmap(b(), (float) (this.a + 15), (float) ((getHeight() - this.h) - 8), this.f);
                }
            }
        } catch (Throwable th) {
            ce.a(th, "WaterMarkerView", "onDraw");
            th.printStackTrace();
        }
    }
}
