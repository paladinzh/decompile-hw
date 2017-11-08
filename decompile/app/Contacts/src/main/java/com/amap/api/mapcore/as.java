package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;

/* compiled from: LocationView */
class as extends LinearLayout {
    Bitmap a;
    Bitmap b;
    Bitmap c;
    Bitmap d;
    Bitmap e;
    Bitmap f;
    ImageView g;
    ab h;
    boolean i = false;

    public void a() {
        try {
            removeAllViews();
            if (this.a != null) {
                this.a.recycle();
            }
            if (this.b != null) {
                this.b.recycle();
            }
            if (this.b != null) {
                this.c.recycle();
            }
            this.a = null;
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
            if (this.f != null) {
                this.f.recycle();
                this.f = null;
            }
        } catch (Throwable th) {
            ce.a(th, "LocationView", "destroy");
            th.printStackTrace();
        }
    }

    public as(Context context) {
        super(context);
    }

    public as(Context context, av avVar, ab abVar) {
        super(context);
        this.h = abVar;
        try {
            this.d = bj.a(context, "location_selected.png");
            this.a = bj.a(this.d, s.a);
            this.e = bj.a(context, "location_pressed.png");
            this.b = bj.a(this.e, s.a);
            this.f = bj.a(context, "location_unselected.png");
            this.c = bj.a(this.f, s.a);
        } catch (Throwable th) {
            ce.a(th, "LocationView", "create");
            th.printStackTrace();
        }
        this.g = new ImageView(context);
        this.g.setImageBitmap(this.a);
        this.g.setClickable(true);
        this.g.setPadding(0, 20, 20, 0);
        this.g.setOnTouchListener(new OnTouchListener(this) {
            final /* synthetic */ as a;

            {
                this.a = r1;
            }

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!this.a.i) {
                    return false;
                }
                if (motionEvent.getAction() == 0) {
                    this.a.g.setImageBitmap(this.a.b);
                } else if (motionEvent.getAction() == 1) {
                    try {
                        this.a.g.setImageBitmap(this.a.a);
                        this.a.h.m(true);
                        Location z = this.a.h.z();
                        if (z == null) {
                            return false;
                        }
                        LatLng latLng = new LatLng(z.getLatitude(), z.getLongitude());
                        this.a.h.a(z);
                        this.a.h.a(p.a(latLng, this.a.h.F()));
                    } catch (Throwable th) {
                        ce.a(th, "LocationView", "onTouch");
                        th.printStackTrace();
                    }
                }
                return false;
            }
        });
        addView(this.g);
    }

    public void a(boolean z) {
        this.i = z;
        if (z) {
            this.g.setImageBitmap(this.a);
        } else {
            this.g.setImageBitmap(this.c);
        }
        this.g.invalidate();
    }
}
