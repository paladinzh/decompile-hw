package com.amap.api.mapcore.util;

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
import com.amap.api.maps.model.CameraPosition;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: CompassView */
public class ej extends LinearLayout {
    Bitmap a;
    Bitmap b;
    Bitmap c;
    ImageView d;
    l e;
    Matrix f = new Matrix();

    public ej(Context context, l lVar) {
        super(context);
        this.e = lVar;
        try {
            this.c = eh.a(context, "maps_dav_compass_needle_large.png");
            this.b = eh.a(this.c, g.a * 0.8f);
            this.c = eh.a(this.c, g.a * 0.7f);
            if (this.b != null || this.c != null) {
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
                a();
                this.d.setOnTouchListener(new OnTouchListener(this) {
                    final /* synthetic */ ej a;

                    {
                        this.a = r1;
                    }

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        try {
                            if (!this.a.e.isMaploaded()) {
                                return false;
                            }
                            if (motionEvent.getAction() == 0) {
                                this.a.d.setImageBitmap(this.a.b);
                            } else if (motionEvent.getAction() == 1) {
                                this.a.d.setImageBitmap(this.a.a);
                                CameraPosition cameraPosition = this.a.e.getCameraPosition();
                                this.a.e.b(ag.a(new CameraPosition(cameraPosition.target, cameraPosition.zoom, 0.0f, 0.0f)));
                            }
                            return false;
                        } catch (Throwable th) {
                            fo.b(th, "CompassView", "onTouch");
                            th.printStackTrace();
                        }
                    }
                });
                addView(this.d);
            }
        } catch (Throwable th) {
            fo.b(th, "CompassView", "create");
            th.printStackTrace();
        }
    }

    public void a() {
        try {
            MapProjection c = this.e.c();
            if (c != null && this.d != null) {
                float mapAngle = c.getMapAngle();
                float cameraHeaderAngle = c.getCameraHeaderAngle();
                if (this.f == null) {
                    this.f = new Matrix();
                }
                this.f.reset();
                this.f.postRotate(-mapAngle, ((float) this.d.getDrawable().getBounds().width()) / 2.0f, ((float) this.d.getDrawable().getBounds().height()) / 2.0f);
                this.f.postScale(WMElement.CAMERASIZEVALUE1B1, (float) Math.cos((((double) cameraHeaderAngle) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude), ((float) this.d.getDrawable().getBounds().width()) / 2.0f, ((float) this.d.getDrawable().getBounds().height()) / 2.0f);
                this.d.setImageMatrix(this.f);
            }
        } catch (Throwable th) {
            fo.b(th, "CompassView", "invalidateAngle");
            th.printStackTrace();
        }
    }

    public void a(boolean z) {
        if (z) {
            setVisibility(0);
            a();
            return;
        }
        setVisibility(8);
    }
}
