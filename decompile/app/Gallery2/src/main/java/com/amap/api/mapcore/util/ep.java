package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.amap.api.mapcore.util.em.a;

/* compiled from: ZoomControllerView */
class ep extends LinearLayout {
    private Bitmap a;
    private Bitmap b;
    private Bitmap c;
    private Bitmap d;
    private Bitmap e;
    private Bitmap f;
    private Bitmap g;
    private Bitmap h;
    private Bitmap i;
    private Bitmap j;
    private Bitmap k;
    private Bitmap l;
    private ImageView m;
    private ImageView n;
    private l o;

    public ep(Context context, l lVar) {
        super(context);
        this.o = lVar;
        try {
            this.g = eh.a(context, "zoomin_selected.png");
            this.a = eh.a(this.g, g.a);
            this.h = eh.a(context, "zoomin_unselected.png");
            this.b = eh.a(this.h, g.a);
            this.i = eh.a(context, "zoomout_selected.png");
            this.c = eh.a(this.i, g.a);
            this.j = eh.a(context, "zoomout_unselected.png");
            this.d = eh.a(this.j, g.a);
            this.k = eh.a(context, "zoomin_pressed.png");
            this.e = eh.a(this.k, g.a);
            this.l = eh.a(context, "zoomout_pressed.png");
            this.f = eh.a(this.l, g.a);
            this.m = new ImageView(context);
            this.m.setImageBitmap(this.a);
            this.m.setClickable(true);
            this.n = new ImageView(context);
            this.n.setImageBitmap(this.c);
            this.n.setClickable(true);
            this.m.setOnTouchListener(new OnTouchListener(this) {
                final /* synthetic */ ep a;

                {
                    this.a = r1;
                }

                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((this.a.o.o() >= this.a.o.getMaxZoomLevel()) || !this.a.o.isMaploaded()) {
                        return false;
                    }
                    if (motionEvent.getAction() == 0) {
                        this.a.m.setImageBitmap(this.a.e);
                    } else if (motionEvent.getAction() == 1) {
                        this.a.m.setImageBitmap(this.a.a);
                        try {
                            this.a.o.b(ag.a());
                        } catch (Throwable e) {
                            fo.b(e, "ZoomControllerView", "zoomin ontouch");
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
            this.n.setOnTouchListener(new OnTouchListener(this) {
                final /* synthetic */ ep a;

                {
                    this.a = r1;
                }

                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if ((this.a.o.o() <= this.a.o.getMinZoomLevel()) || !this.a.o.isMaploaded()) {
                        return false;
                    }
                    if (motionEvent.getAction() == 0) {
                        this.a.n.setImageBitmap(this.a.f);
                    } else if (motionEvent.getAction() == 1) {
                        this.a.n.setImageBitmap(this.a.c);
                        try {
                            this.a.o.b(ag.b());
                        } catch (Throwable e) {
                            fo.b(e, "ZoomControllerView", "zoomout ontouch");
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
            this.m.setPadding(0, 0, 20, -2);
            this.n.setPadding(0, 0, 20, 20);
            setOrientation(1);
            addView(this.m);
            addView(this.n);
        } catch (Throwable th) {
            fo.b(th, "ZoomControllerView", "create");
            th.printStackTrace();
        }
    }

    public void a(float f) {
        try {
            if (f < this.o.getMaxZoomLevel() && f > this.o.getMinZoomLevel()) {
                this.m.setImageBitmap(this.a);
                this.n.setImageBitmap(this.c);
            } else if (f == this.o.getMinZoomLevel()) {
                this.n.setImageBitmap(this.d);
                this.m.setImageBitmap(this.a);
            } else if (f == this.o.getMaxZoomLevel()) {
                this.m.setImageBitmap(this.b);
                this.n.setImageBitmap(this.c);
            }
        } catch (Throwable th) {
            fo.b(th, "ZoomControllerView", "setZoomBitmap");
            th.printStackTrace();
        }
    }

    public void a(int i) {
        try {
            a aVar = (a) getLayoutParams();
            if (i == 1) {
                aVar.d = 16;
            } else if (i == 2) {
                aVar.d = 80;
            }
            setLayoutParams(aVar);
        } catch (Throwable th) {
            fo.b(th, "ZoomControllerView", "setZoomPosition");
            th.printStackTrace();
        }
    }

    public void a(boolean z) {
        if (z) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }
}
