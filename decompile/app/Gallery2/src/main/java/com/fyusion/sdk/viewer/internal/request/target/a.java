package com.fyusion.sdk.viewer.internal.request.target;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.internal.request.b;
import com.fyusion.sdk.viewer.view.FyuseView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class a implements Target<com.fyusion.sdk.viewer.internal.b.c.a> {
    protected FyuseView a;
    private final a b;
    private int c = 0;
    private boolean d = false;

    /* compiled from: Unknown */
    private static class a {
        private final View a;
        private final List<b> b = new ArrayList();
        @Nullable
        private a c;
        @Nullable
        private Point d;

        /* compiled from: Unknown */
        private static class a implements OnPreDrawListener {
            private final WeakReference<a> a;

            public a(a aVar) {
                this.a = new WeakReference(aVar);
            }

            public boolean onPreDraw() {
                a aVar = (a) this.a.get();
                if (aVar != null) {
                    aVar.b();
                }
                return true;
            }
        }

        public a(View view) {
            this.a = view;
        }

        private int a(int i, boolean z) {
            if (i != -2) {
                return i;
            }
            Point e = e();
            return !z ? e.x : e.y;
        }

        private void a(int i, int i2) {
            for (b a : this.b) {
                a.a(i, i2);
            }
        }

        private boolean a(int i) {
            return i > 0 || i == -2;
        }

        private void b() {
            if (!this.b.isEmpty()) {
                int d = d();
                int c = c();
                if (a(d) && a(c)) {
                    a(d, c);
                    a();
                }
            }
        }

        private int c() {
            LayoutParams layoutParams = this.a.getLayoutParams();
            return !a(this.a.getHeight()) ? layoutParams == null ? 0 : a(layoutParams.height, true) : this.a.getHeight();
        }

        private int d() {
            LayoutParams layoutParams = this.a.getLayoutParams();
            return !a(this.a.getWidth()) ? layoutParams == null ? 0 : a(layoutParams.width, false) : this.a.getWidth();
        }

        @TargetApi(13)
        private Point e() {
            if (this.d != null) {
                return this.d;
            }
            Display defaultDisplay = ((WindowManager) this.a.getContext().getSystemService("window")).getDefaultDisplay();
            if (VERSION.SDK_INT < 13) {
                this.d = new Point(defaultDisplay.getWidth(), defaultDisplay.getHeight());
            } else {
                this.d = new Point();
                defaultDisplay.getSize(this.d);
            }
            return this.d;
        }

        void a() {
            ViewTreeObserver viewTreeObserver = this.a.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnPreDrawListener(this.c);
            }
            this.c = null;
            this.b.clear();
        }

        void a(b bVar) {
            if (!this.b.contains(bVar)) {
                this.b.add(bVar);
            }
            if (this.c == null) {
                ViewTreeObserver viewTreeObserver = this.a.getViewTreeObserver();
                this.c = new a(this);
                viewTreeObserver.addOnPreDrawListener(this.c);
            }
        }
    }

    public a(FyuseView fyuseView) {
        this.a = fyuseView;
        this.b = new a(fyuseView);
    }

    public void a(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        getSize(this.a.getSizeReadyCallback());
        this.a.setFyuseData(aVar);
        this.d = true;
    }

    public void b(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        if (!this.d) {
            DLog.d("FyuseViewTarget", "onResourceReady: " + aVar.d());
            a(aVar);
        }
    }

    @Nullable
    public b getRequest() {
        Object tag = this.a.getTag();
        if (tag == null) {
            return null;
        }
        if (tag instanceof b) {
            return (b) tag;
        }
        throw new IllegalArgumentException("You must not call setTag() on a view FyuseViewer is targeting");
    }

    public void getSize(b bVar) {
        this.b.a(bVar);
    }

    public Object getWrappedObject() {
        return this.a;
    }

    public void onDestroy() {
        DLog.d("FyuseViewTarget", "onDestroy: " + this);
    }

    public void onLoadCleared(@Nullable Drawable drawable) {
        DLog.d("FyuseViewTarget", "onLoadCleared ");
        if (this.a != null) {
            this.a.clear(drawable);
        }
        this.b.a();
    }

    public void onLoadFailed(@Nullable Drawable drawable) {
        DLog.d("FyuseViewTarget", "onLoadFailed: ");
    }

    public void onLoadStarted(@Nullable Drawable drawable) {
        DLog.d("FyuseViewTarget", "onLoadStarted: ");
    }

    public /* synthetic */ void onMetadataReady(Object obj) {
        a((com.fyusion.sdk.viewer.internal.b.c.a) obj);
    }

    public void onProcessingSliceProgress(int i, int i2, Object obj) {
        this.c++;
        if (this.a != null) {
            this.a.setProgress(i, (this.c * 100) / i2, obj);
        }
    }

    public /* synthetic */ void onResourceReady(Object obj) {
        b((com.fyusion.sdk.viewer.internal.b.c.a) obj);
    }

    public void onStart() {
        DLog.d("FyuseViewTarget", "onStart: " + this);
        this.a.onStart();
    }

    public void onStop() {
        DLog.d("FyuseViewTarget", "onStop: " + this);
        this.a.onStop();
    }

    public void setRequest(@Nullable b bVar) {
        this.a.setTag(bVar);
    }
}
