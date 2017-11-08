package com.fyusion.sdk.viewer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.viewer.internal.b.c.a;
import com.fyusion.sdk.viewer.internal.request.target.b;

/* compiled from: Unknown */
public class FyuseView extends a {
    protected l a;
    protected boolean b;

    public FyuseView(Context context) {
        super(context);
        a(context, null);
    }

    public FyuseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        a(context, attributeSet);
    }

    private void a(Context context, AttributeSet attributeSet) {
        LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        this.b = true;
        this.a = createMainView(context, attributeSet);
        this.a.setClickable(true);
        this.a.setLayoutParams(layoutParams);
        this.a.setVisibility(8);
        this.a.setListener(new a(this) {
            final /* synthetic */ FyuseView a;

            {
                this.a = r1;
            }

            public void a() {
                this.a.post(new Runnable(this) {
                    final /* synthetic */ AnonymousClass1 a;

                    {
                        this.a = r1;
                    }

                    public void run() {
                        this.a.a.showFyuse();
                    }
                });
            }
        });
        addView(this.a);
        doInit(context, layoutParams);
    }

    public void clear(Drawable drawable) {
        this.a.i();
    }

    protected l createMainView(Context context, AttributeSet attributeSet) {
        return new l(context, attributeSet);
    }

    @UiThread
    public void destroySurface() {
        this.a.k();
    }

    protected void doInit(Context context, FrameLayout.LayoutParams layoutParams) {
    }

    public void enableGesture(boolean z) {
        this.a.b(z);
    }

    public void enableMotion(boolean z) {
        this.a.a(z);
    }

    public b getSizeReadyCallback() {
        return this.a.getSizeReadyCallback();
    }

    public f getView() {
        return this.a;
    }

    protected void onContentSizeAvailable(int i, int i2) {
        setAspectRatio((double) (((float) i) / ((float) i2)));
    }

    protected void onFyuseShown() {
    }

    protected void onProgress(int i, int i2) {
    }

    public void onStart() {
        this.a.j();
    }

    public void onStop() {
        this.a.k();
    }

    protected void onThumbnailReady(@Nullable Bitmap bitmap) {
    }

    public void setFyuseData(a aVar) {
        if (aVar == null || aVar.m() == null) {
            Log.w("FyuseView", "Unexpected call. Trying to set null FyuseData");
            return;
        }
        int width;
        int height;
        p m = aVar.m();
        if (this.b && !m.isPortrait()) {
            width = m.getWidth();
            height = m.getHeight();
        } else {
            width = m.getHeight();
            height = m.getWidth();
        }
        this.a.setData(aVar);
        onContentSizeAvailable(width, height);
        onThumbnailReady(aVar.n().getBlurImage());
    }

    public void setProgress(int i, int i2, Object obj) {
        onProgress(i, i2);
    }

    public void setRotateWithGravity(boolean z) {
        this.b = z;
        this.a.setRotateWithGravity(z);
    }

    protected void showFyuse() {
        if (this.a.getVisibility() != 0) {
            this.a.setVisibility(0);
        }
        onFyuseShown();
    }
}
