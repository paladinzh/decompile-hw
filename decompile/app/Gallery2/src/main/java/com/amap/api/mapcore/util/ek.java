package com.amap.api.mapcore.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* compiled from: IndoorFloorSwitchView */
public class ek extends ScrollView {
    public static final String a = ek.class.getSimpleName();
    int b = 1;
    private Context c;
    private LinearLayout d;
    private int e = 0;
    private List<String> f;
    private int g = -1;
    private int h;
    private Bitmap i = null;
    private int j = Color.parseColor("#eeffffff");
    private int k = Color.parseColor("#44383838");
    private int l = 4;
    private int m = 1;
    private int n;
    private int o;
    private Runnable p;
    private int q = 50;
    private a r;

    /* compiled from: IndoorFloorSwitchView */
    public interface a {
        void a(int i);
    }

    public ek(Context context) {
        super(context);
        a(context);
    }

    private void a(Context context) {
        this.c = context;
        setVerticalScrollBarEnabled(false);
        try {
            if (this.i == null) {
                InputStream open = ef.a(context).open("map_indoor_select.png");
                this.i = BitmapFactory.decodeStream(open);
                open.close();
            }
        } catch (Throwable th) {
        }
        this.d = new LinearLayout(context);
        this.d.setOrientation(1);
        addView(this.d);
        this.p = new Runnable(this) {
            final /* synthetic */ ek a;

            {
                this.a = r1;
            }

            public void run() {
                if (this.a.o - this.a.getScrollY() != 0) {
                    this.a.o = this.a.getScrollY();
                    this.a.postDelayed(this.a.p, (long) this.a.q);
                    return;
                }
                final int a = this.a.o % this.a.e;
                final int a2 = this.a.o / this.a.e;
                if (a == 0) {
                    this.a.b = a2 + this.a.m;
                    this.a.f();
                } else if (a <= this.a.e / 2) {
                    this.a.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 c;

                        public void run() {
                            this.c.a.smoothScrollTo(0, this.c.a.o - a);
                            this.c.a.b = a2 + this.c.a.m;
                            this.c.a.f();
                        }
                    });
                } else {
                    this.a.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 c;

                        public void run() {
                            this.c.a.smoothScrollTo(0, (this.c.a.o - a) + this.c.a.e);
                            this.c.a.b = (a2 + this.c.a.m) + 1;
                            this.c.a.f();
                        }
                    });
                }
            }
        };
    }

    public void a() {
        this.o = getScrollY();
        postDelayed(this.p, (long) this.q);
    }

    private void d() {
        if (this.f != null && this.f.size() != 0) {
            this.d.removeAllViews();
            this.n = (this.m * 2) + 1;
            for (int size = this.f.size() - 1; size >= 0; size--) {
                this.d.addView(b((String) this.f.get(size)));
            }
            a(0);
        }
    }

    private TextView b(String str) {
        View textView = new TextView(this.c);
        textView.setLayoutParams(new LayoutParams(-1, -2));
        textView.setSingleLine(true);
        textView.setTextSize(2, 16.0f);
        textView.setText(str);
        textView.setGravity(17);
        textView.getPaint().setFakeBoldText(true);
        int a = a(this.c, 8.0f);
        int a2 = a(this.c, 6.0f);
        textView.setPadding(a, a2, a, a2);
        if (this.e == 0) {
            this.e = a(textView);
            this.d.setLayoutParams(new LayoutParams(-2, this.e * this.n));
            setLayoutParams(new LinearLayout.LayoutParams(-2, this.e * this.n));
        }
        return textView;
    }

    private void a(int i) {
        int i2 = 0;
        int i3 = (i / this.e) + this.m;
        int i4 = i % this.e;
        int i5 = i / this.e;
        if (i4 == 0) {
            i4 = this.m + i5;
        } else if (i4 <= this.e / 2) {
            i4 = i3;
        } else {
            i4 = (this.m + i5) + 1;
        }
        i5 = this.d.getChildCount();
        while (i2 < i5) {
            TextView textView = (TextView) this.d.getChildAt(i2);
            if (textView != null) {
                if (i4 != i2) {
                    textView.setTextColor(Color.parseColor("#bbbbbb"));
                } else {
                    textView.setTextColor(Color.parseColor("#0288ce"));
                }
                i2++;
            } else {
                return;
            }
        }
    }

    public void a(String[] strArr) {
        int i;
        if (this.f == null) {
            this.f = new ArrayList();
        }
        this.f.clear();
        for (Object add : strArr) {
            this.f.add(add);
        }
        for (i = 0; i < this.m; i++) {
            this.f.add(0, "");
            this.f.add("");
        }
        d();
    }

    public void setBackgroundColor(int i) {
        this.j = i;
    }

    public void setBackgroundDrawable(Drawable drawable) {
        if (this.h == 0) {
            this.h = ((Activity) this.c).getWindowManager().getDefaultDisplay().getWidth();
        }
        super.setBackgroundDrawable(new Drawable(this) {
            final /* synthetic */ ek a;

            {
                this.a = r1;
            }

            public void draw(Canvas canvas) {
                try {
                    a(canvas);
                    b(canvas);
                    c(canvas);
                } catch (Throwable th) {
                }
            }

            private void a(Canvas canvas) {
                canvas.drawColor(this.a.j);
            }

            private void b(Canvas canvas) {
                Paint paint = new Paint();
                Rect rect = new Rect();
                Rect rect2 = new Rect();
                rect.left = 0;
                rect.top = 0;
                rect.right = this.a.i.getWidth() + 0;
                rect.bottom = this.a.i.getHeight() + 0;
                rect2.left = 0;
                rect2.top = this.a.e()[0];
                rect2.right = this.a.h + 0;
                rect2.bottom = this.a.e()[1];
                canvas.drawBitmap(this.a.i, rect, rect2, paint);
            }

            private void c(Canvas canvas) {
                Paint paint = new Paint();
                Rect clipBounds = canvas.getClipBounds();
                paint.setColor(this.a.k);
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth((float) this.a.l);
                canvas.drawRect(clipBounds, paint);
            }

            public void setAlpha(int i) {
            }

            public void setColorFilter(ColorFilter colorFilter) {
            }

            public int getOpacity() {
                return 0;
            }
        });
    }

    private int[] e() {
        return new int[]{this.e * this.m, this.e * (this.m + 1)};
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.h = i;
        setBackgroundDrawable(null);
    }

    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        a(i2);
        if (i2 <= i4) {
            this.g = 0;
        } else {
            this.g = 1;
        }
    }

    private void f() {
        if (this.r != null) {
            try {
                this.r.a(b());
            } catch (Throwable th) {
            }
        }
    }

    public void a(String str) {
        if (this.f != null && this.f.size() != 0) {
            final int size = ((this.f.size() - this.m) - 1) - this.f.indexOf(str);
            this.b = this.m + size;
            post(new Runnable(this) {
                final /* synthetic */ ek b;

                public void run() {
                    this.b.smoothScrollTo(0, size * this.b.e);
                }
            });
        }
    }

    public int b() {
        if (this.f == null || this.f.size() == 0) {
            return 0;
        }
        return Math.min(this.f.size() - (this.m * 2), Math.max(0, ((this.f.size() - 1) - this.b) - this.m));
    }

    public void fling(int i) {
        super.fling(i / 3);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1) {
            a();
        }
        return super.onTouchEvent(motionEvent);
    }

    public void a(a aVar) {
        this.r = aVar;
    }

    public static int a(Context context, float f) {
        return (int) ((context.getResources().getDisplayMetrics().density * f) + 0.5f);
    }

    public static int a(View view) {
        b(view);
        return view.getMeasuredHeight();
    }

    public static void b(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(536870911, Target.SIZE_ORIGINAL));
    }

    public void a(boolean z) {
        if (z) {
            if (!c()) {
                setVisibility(0);
            }
        } else if (c()) {
            setVisibility(8);
        }
    }

    public boolean c() {
        return getVisibility() == 0;
    }
}
