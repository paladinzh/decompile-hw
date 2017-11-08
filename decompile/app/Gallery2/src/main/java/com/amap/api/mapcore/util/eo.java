package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.View;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.InputStream;

/* compiled from: WaterMarkerView */
public class eo extends View {
    private Bitmap a;
    private Bitmap b;
    private Bitmap c;
    private Bitmap d;
    private Paint e = new Paint();
    private boolean f = false;
    private int g = 0;
    private int h = 0;
    private l i;
    private int j = 0;
    private int k = 10;
    private int l = 0;
    private int m = 0;
    private int n = 10;
    private int o = 8;
    private int p = 0;
    private boolean q = false;
    private float r = 0.0f;
    private float s = 0.0f;
    private boolean t = true;

    public eo(Context context, l lVar) {
        Throwable th;
        InputStream inputStream = null;
        super(context);
        this.i = lVar;
        InputStream open;
        try {
            open = ef.a(context).open("ap.data");
            try {
                this.c = BitmapFactory.decodeStream(open);
                this.a = eh.a(this.c, g.a);
                open.close();
                inputStream = ef.a(context).open("ap1.data");
                this.d = BitmapFactory.decodeStream(inputStream);
                this.b = eh.a(this.d, g.a);
                inputStream.close();
                this.h = this.b.getWidth();
                this.g = this.b.getHeight();
                this.e.setAntiAlias(true);
                this.e.setColor(-16777216);
                this.e.setStyle(Style.STROKE);
                if (open != null) {
                    try {
                        open.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th22) {
                        th22.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th22 = th3;
                try {
                    fo.b(th22, "WaterMarkerView", "create");
                    th22.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (Throwable th222) {
                            th222.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th2222) {
                            th2222.printStackTrace();
                        }
                    }
                } catch (Throwable th4) {
                    th2222 = th4;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (Throwable th5) {
                            th5.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th6) {
                            th6.printStackTrace();
                        }
                    }
                    throw th2222;
                }
            }
        } catch (Throwable th7) {
            th2222 = th7;
            open = null;
            if (open != null) {
                open.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            throw th2222;
        }
    }

    public Bitmap a() {
        if (this.f) {
            return this.b;
        }
        return this.a;
    }

    public void a(boolean z) {
        try {
            this.f = z;
            if (z) {
                this.e.setColor(-1);
            } else {
                this.e.setColor(-16777216);
            }
            invalidate();
        } catch (Throwable th) {
            fo.b(th, "WaterMarkerView", "changeBitmap");
            th.printStackTrace();
        }
    }

    public Point b() {
        return new Point(this.k, this.l - 2);
    }

    public void a(int i) {
        this.m = 0;
        this.j = i;
        c();
    }

    public void b(int i) {
        this.m = 1;
        this.o = i;
        c();
    }

    public void c(int i) {
        this.m = 1;
        this.n = i;
        c();
    }

    public float d(int i) {
        switch (i) {
            case 0:
                return this.r;
            case 1:
                return WMElement.CAMERASIZEVALUE1B1 - this.r;
            case 2:
                return WMElement.CAMERASIZEVALUE1B1 - this.s;
            default:
                return 0.0f;
        }
    }

    public void a(int i, float f) {
        this.m = 2;
        this.p = i;
        float max = Math.max(0.0f, Math.min(f, WMElement.CAMERASIZEVALUE1B1));
        switch (i) {
            case 0:
                this.r = max;
                this.t = true;
                break;
            case 1:
                this.r = WMElement.CAMERASIZEVALUE1B1 - max;
                this.t = false;
                break;
            case 2:
                this.s = WMElement.CAMERASIZEVALUE1B1 - max;
                break;
        }
        c();
    }

    public void c() {
        if (getWidth() != 0 && getHeight() != 0) {
            d();
            invalidate();
        }
    }

    public void onDraw(Canvas canvas) {
        try {
            if (this.b != null) {
                if (!this.q) {
                    d();
                    this.q = true;
                }
                canvas.drawBitmap(a(), (float) this.k, (float) this.l, this.e);
            }
        } catch (Throwable th) {
            fo.b(th, "WaterMarkerView", "onDraw");
            th.printStackTrace();
        }
    }

    private void d() {
        switch (this.m) {
            case 0:
                f();
                break;
            case 2:
                e();
                break;
        }
        this.k = this.n;
        this.l = (this.i.getMapHeight() - this.o) - this.g;
        if (this.k < 0) {
            this.k = 0;
        }
        if (this.l < 0) {
            this.l = 0;
        }
    }

    private void e() {
        if (this.t) {
            this.n = (int) (((float) this.i.getMapWidth()) * this.r);
        } else {
            this.n = (int) ((((float) this.i.getMapWidth()) * this.r) - ((float) this.h));
        }
        this.o = (int) (((float) this.i.getMapHeight()) * this.s);
    }

    private void f() {
        if (this.j == 1) {
            this.n = (this.i.getMapWidth() - this.h) / 2;
        } else if (this.j != 2) {
            this.n = 10;
        } else {
            this.n = (this.i.getMapWidth() - this.h) - 10;
        }
        this.o = 8;
    }
}
