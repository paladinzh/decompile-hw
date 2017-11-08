package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.amap.api.maps.model.LatLng;

/* compiled from: LocationView */
public class el extends LinearLayout {
    Bitmap a;
    Bitmap b;
    Bitmap c;
    Bitmap d;
    Bitmap e;
    Bitmap f;
    ImageView g;
    l h;
    boolean i = false;

    public el(Context context, l lVar) {
        super(context);
        this.h = lVar;
        try {
            this.d = eh.a(context, "location_selected.png");
            this.a = eh.a(this.d, g.a);
            this.e = eh.a(context, "location_pressed.png");
            this.b = eh.a(this.e, g.a);
            this.f = eh.a(context, "location_unselected.png");
            this.c = eh.a(this.f, g.a);
            this.g = new ImageView(context);
            this.g.setImageBitmap(this.a);
            this.g.setClickable(true);
            this.g.setPadding(0, 20, 20, 0);
            this.g.setOnTouchListener(new OnTouchListener(this) {
                final /* synthetic */ el a;

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
                            this.a.h.setMyLocationEnabled(true);
                            Location myLocation = this.a.h.getMyLocation();
                            if (myLocation == null) {
                                return false;
                            }
                            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                            this.a.h.a(myLocation);
                            this.a.h.a(ag.a(latLng, this.a.h.o()));
                        } catch (Throwable th) {
                            fo.b(th, "LocationView", "onTouch");
                            th.printStackTrace();
                        }
                    }
                    return false;
                }
            });
            addView(this.g);
        } catch (Throwable th) {
            fo.b(th, "LocationView", "create");
            th.printStackTrace();
        }
    }

    public void a(boolean z) {
        this.i = z;
        if (z) {
            this.g.setImageBitmap(this.a);
        } else {
            try {
                this.g.setImageBitmap(this.c);
            } catch (Throwable th) {
                fo.b(th, "LocationView", "showSelect");
                th.printStackTrace();
                return;
            }
        }
        this.g.invalidate();
    }
}
