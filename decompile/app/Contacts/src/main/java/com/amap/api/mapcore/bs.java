package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.amap.api.mapcore.az.a;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;

/* compiled from: ZoomControllerView */
class bs extends LinearLayout {
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
    private ab o;

    public void a() {
        try {
            removeAllViews();
            this.a.recycle();
            this.b.recycle();
            this.c.recycle();
            this.d.recycle();
            this.e.recycle();
            this.f.recycle();
            this.a = null;
            this.b = null;
            this.c = null;
            this.d = null;
            this.e = null;
            this.f = null;
            if (this.g != null) {
                this.g.recycle();
                this.g = null;
            }
            if (this.h != null) {
                this.h.recycle();
                this.h = null;
            }
            if (this.i != null) {
                this.i.recycle();
                this.i = null;
            }
            if (this.j != null) {
                this.j.recycle();
                this.g = null;
            }
            if (this.k != null) {
                this.k.recycle();
                this.k = null;
            }
            if (this.l != null) {
                this.l.recycle();
                this.l = null;
            }
            this.m = null;
            this.n = null;
        } catch (Throwable th) {
            ce.a(th, "ZoomControllerView", "destory");
            th.printStackTrace();
        }
    }

    public bs(Context context) {
        super(context);
    }

    public bs(Context context, ab abVar) {
        super(context);
        this.o = abVar;
        try {
            this.g = bj.a(context, "zoomin_selected.png");
            this.a = bj.a(this.g, s.a);
            this.h = bj.a(context, "zoomin_unselected.png");
            this.b = bj.a(this.h, s.a);
            this.i = bj.a(context, "zoomout_selected.png");
            this.c = bj.a(this.i, s.a);
            this.j = bj.a(context, "zoomout_unselected.png");
            this.d = bj.a(this.j, s.a);
            this.k = bj.a(context, "zoomin_pressed.png");
            this.e = bj.a(this.k, s.a);
            this.l = bj.a(context, "zoomout_pressed.png");
            this.f = bj.a(this.l, s.a);
            this.m = new ImageView(context);
            this.m.setImageBitmap(this.a);
            this.m.setClickable(true);
            this.n = new ImageView(context);
            this.n.setImageBitmap(this.c);
            this.n.setClickable(true);
        } catch (Throwable th) {
            ce.a(th, "ZoomControllerView", "create");
            th.printStackTrace();
        }
        this.m.setOnTouchListener(new OnTouchListener(this) {
            final /* synthetic */ bs a;

            {
                this.a = r1;
            }

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((this.a.o.F() >= this.a.o.s()) || !this.a.o.R()) {
                    return false;
                }
                if (motionEvent.getAction() == 0) {
                    this.a.m.setImageBitmap(this.a.e);
                } else if (motionEvent.getAction() == 1) {
                    this.a.m.setImageBitmap(this.a.a);
                    try {
                        this.a.o.b(p.b());
                    } catch (Throwable e) {
                        ce.a(e, "ZoomControllerView", "zoomin ontouch");
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        this.n.setOnTouchListener(new OnTouchListener(this) {
            final /* synthetic */ bs a;

            {
                this.a = r1;
            }

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((this.a.o.F() <= this.a.o.t()) || !this.a.o.R()) {
                    return false;
                }
                if (motionEvent.getAction() == 0) {
                    this.a.n.setImageBitmap(this.a.f);
                } else if (motionEvent.getAction() == 1) {
                    this.a.n.setImageBitmap(this.a.c);
                    try {
                        this.a.o.b(p.c());
                    } catch (Throwable e) {
                        ce.a(e, "ZoomControllerView", "zoomout ontouch");
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
    }

    public void a(float f) {
        if (f < this.o.s() && f > this.o.t()) {
            this.m.setImageBitmap(this.a);
            this.n.setImageBitmap(this.c);
        } else if (f == this.o.t()) {
            this.n.setImageBitmap(this.d);
            this.m.setImageBitmap(this.a);
        } else if (f == this.o.s()) {
            this.m.setImageBitmap(this.b);
            this.n.setImageBitmap(this.c);
        }
    }

    public void a(int i) {
        a aVar = (a) getLayoutParams();
        if (i == 1) {
            aVar.d = 16;
        } else if (i == 2) {
            aVar.d = 80;
        }
        setLayoutParams(aVar);
    }

    public void a(boolean z) {
        if (z) {
            setVisibility(0);
        } else {
            setVisibility(8);
        }
    }
}
