package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.CameraPosition;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CompassView */
class r extends LinearLayout {
    Bitmap a;
    Bitmap b;
    Bitmap c;
    ImageView d;
    ab e;

    public void a() {
        try {
            removeAllViews();
            if (this.a != null) {
                this.a.recycle();
            }
            if (this.b != null) {
                this.b.recycle();
            }
            if (this.c != null) {
                this.c.recycle();
            }
            this.c = null;
            this.a = null;
            this.b = null;
        } catch (Throwable th) {
            ce.a(th, "CompassView", "destroy");
            th.printStackTrace();
        }
    }

    public r(Context context) {
        super(context);
    }

    public r(Context context, av avVar, ab abVar) {
        super(context);
        this.e = abVar;
        try {
            this.c = bj.a(context, "maps_dav_compass_needle_large.png");
            this.b = bj.a(this.c, s.a * 0.8f);
            this.c = bj.a(this.c, s.a * 0.7f);
            this.a = Bitmap.createBitmap(this.b.getWidth(), this.b.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(this.a);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            canvas.drawBitmap(this.c, ((float) (this.b.getWidth() - this.c.getWidth())) / 2.0f, ((float) (this.b.getHeight() - this.c.getHeight())) / 2.0f, paint);
            this.d = new ImageView(context);
            this.d.setScaleType(ScaleType.MATRIX);
            this.d.setImageBitmap(this.a);
            this.d.setClickable(true);
            b();
            this.d.setOnTouchListener(new OnTouchListener(this) {
                final /* synthetic */ r a;

                {
                    this.a = r1;
                }

                public boolean onTouch(View view, MotionEvent motionEvent) {
                    try {
                        if (!this.a.e.R()) {
                            return false;
                        }
                        if (motionEvent.getAction() == 0) {
                            this.a.d.setImageBitmap(this.a.b);
                        } else if (motionEvent.getAction() == 1) {
                            this.a.d.setImageBitmap(this.a.a);
                            CameraPosition r = this.a.e.r();
                            this.a.e.b(p.a(new CameraPosition(r.target, r.zoom, 0.0f, 0.0f)));
                        }
                        return false;
                    } catch (Throwable th) {
                        ce.a(th, "CompassView", "onTouch");
                        th.printStackTrace();
                    }
                }
            });
            addView(this.d);
        } catch (Throwable th) {
            ce.a(th, "CompassView", "create");
            th.printStackTrace();
        }
    }

    public void b() {
        try {
            MapProjection c = this.e.c();
            float mapAngle = c.getMapAngle();
            float cameraHeaderAngle = c.getCameraHeaderAngle();
            Matrix matrix = new Matrix();
            matrix.postRotate(-mapAngle, ((float) this.d.getDrawable().getBounds().width()) / 2.0f, ((float) this.d.getDrawable().getBounds().height()) / 2.0f);
            matrix.postScale(1.0f, (float) Math.cos((((double) cameraHeaderAngle) * 3.141592653589793d) / 180.0d), ((float) this.d.getDrawable().getBounds().width()) / 2.0f, ((float) this.d.getDrawable().getBounds().height()) / 2.0f);
            this.d.setImageMatrix(matrix);
        } catch (Throwable th) {
            ce.a(th, "CompassView", "invalidateAngle");
            th.printStackTrace();
        }
    }

    public void a(boolean z) {
        if (z) {
            setVisibility(0);
            b();
            return;
        }
        setVisibility(8);
    }
}
