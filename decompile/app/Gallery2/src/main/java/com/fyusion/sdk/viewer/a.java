package com.fyusion.sdk.viewer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.fyusion.sdk.viewer.internal.d;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* compiled from: Unknown */
public class a {
    private static ExecutorService i = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "GestureManager");
        }
    });
    b a = b.INVALID;
    float b;
    float c;
    Runnable d = new Runnable(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public void run() {
            if (this.a.f != null) {
                this.a.f.a(this.a.b, this.a.c, this.a.a);
            }
        }
    };
    private View e = null;
    private a f;
    private GestureDetector g = null;
    private d h = null;
    private SimpleOnGestureListener j = new SimpleOnGestureListener(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            this.a.b = f;
            this.a.c = f2;
            a.i.submit(this.a.d);
            return true;
        }

        public boolean onSingleTapUp(MotionEvent motionEvent) {
            ((View) this.a.e.getParent()).performClick();
            return false;
        }
    };
    private com.fyusion.sdk.viewer.internal.d.a k = new com.fyusion.sdk.viewer.internal.d.a(this) {
        final /* synthetic */ a a;

        {
            this.a = r1;
        }

        public void a(float f, float f2) {
            if (this.a.f != null) {
                this.a.f.a(f, f2);
            }
        }

        public void a(float f, float f2, float f3) {
            if (this.a.f != null) {
                this.a.f.a(f, f2, f3);
            }
        }
    };

    /* compiled from: Unknown */
    public interface a {
        void a(float f, float f2);

        void a(float f, float f2, float f3);

        void a(float f, float f2, b bVar);
    }

    /* compiled from: Unknown */
    public enum b {
        HAS_STARTED,
        HAS_ENDED,
        INVALID
    }

    public a(Context context) {
        this.g = new GestureDetector(context, this.j);
        this.h = new d(context, this.k);
    }

    public synchronized void a() {
        if (this.e != null) {
            this.e.setOnTouchListener(null);
        }
        this.e = null;
        this.f = null;
    }

    public synchronized void a(View view, a aVar) {
        this.e = view;
        this.f = aVar;
        view.setOnTouchListener(new OnTouchListener(this) {
            final /* synthetic */ a a;

            {
                this.a = r1;
            }

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case 0:
                        this.a.a = b.HAS_STARTED;
                        break;
                    case 1:
                    case 3:
                        this.a.b = 0.0f;
                        this.a.c = 0.0f;
                        this.a.a = b.HAS_ENDED;
                        a.i.submit(this.a.d);
                        break;
                }
                boolean a = this.a.h.a(motionEvent);
                if (!a) {
                    a = this.a.g.onTouchEvent(motionEvent);
                }
                view.getParent().requestDisallowInterceptTouchEvent(a);
                return false;
            }
        });
    }
}
