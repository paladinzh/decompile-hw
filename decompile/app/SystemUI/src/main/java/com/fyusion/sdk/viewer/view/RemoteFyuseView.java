package com.fyusion.sdk.viewer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

/* compiled from: Unknown */
public class RemoteFyuseView extends FyuseView {
    private static int c = Color.parseColor("#ff3143");
    private ImageView d;
    private View e;
    private ProgressBar f;

    /* compiled from: Unknown */
    static class a extends View {
        public a(Context context) {
            super(context);
            setBackgroundColor(RemoteFyuseView.c);
        }

        public void a(int i) {
            getLayoutParams().width = ((View) getParent()).getWidth();
            LayoutParams layoutParams = getLayoutParams();
            getLayoutParams().width = (int) (((float) ((View) getParent()).getWidth()) * (((float) i) / 100.0f));
            setLayoutParams(layoutParams);
            postInvalidate();
        }
    }

    public RemoteFyuseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected View a(Context context) {
        LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 15);
        layoutParams.gravity = 1;
        View aVar = new a(context);
        aVar.setLayoutParams(layoutParams);
        return aVar;
    }

    protected ProgressBar b(Context context) {
        return new ProgressBar(context, null, 16842874);
    }

    public void clear(Drawable drawable) {
        super.clear(drawable);
        this.d.clearAnimation();
        this.d.setImageBitmap(null);
        this.d.setImageDrawable(drawable);
        if (this.e != null) {
            this.e.setVisibility(8);
        }
        if (this.f != null) {
            this.f.setVisibility(0);
        }
    }

    protected void doInit(Context context, FrameLayout.LayoutParams layoutParams) {
        this.d = new ImageView(context);
        this.d.setLayoutParams(layoutParams);
        this.d.setFocusable(false);
        setBackgroundColor(Color.rgb(0, 0, 0));
        addView(this.d);
        this.e = a(context);
        if (this.e != null) {
            this.e.setVisibility(8);
            addView(this.e);
        }
        this.f = b(context);
        if (this.f != null) {
            this.f.setIndeterminate(true);
            this.f.setVisibility(0);
            LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2);
            layoutParams2.gravity = 17;
            this.f.setLayoutParams(layoutParams2);
            addView(this.f);
        }
    }

    protected void onFyuseShown() {
        if (this.f != null) {
            this.f.setVisibility(8);
        }
        this.d.clearAnimation();
        this.d.animate().alpha(0.0f).setDuration(200).withEndAction(new Runnable(this) {
            final /* synthetic */ RemoteFyuseView a;

            {
                this.a = r1;
            }

            public void run() {
                this.a.d.setVisibility(8);
            }
        });
    }

    protected void onProgress(int i, int i2) {
        if (this.e != null && i2 > 0) {
            if (i2 < 100) {
                if (this.e.getVisibility() == 8) {
                    this.e.setVisibility(0);
                }
                if (this.e instanceof a) {
                    ((a) this.e).a(i2);
                } else if (this.e instanceof ProgressBar) {
                    ((ProgressBar) this.e).setProgress(i2);
                }
            } else {
                this.e.setVisibility(8);
            }
        }
    }

    protected void onThumbnailReady(Bitmap bitmap) {
        if (bitmap == null) {
            this.d.setVisibility(8);
            return;
        }
        if (this.d.getVisibility() != 0) {
            this.d.setVisibility(0);
        }
        this.d.setImageBitmap(bitmap);
        this.d.setScaleType(ScaleType.FIT_XY);
        this.d.clearAnimation();
        this.d.setAlpha(0.0f);
        this.d.animate().alpha(1.0f).setDuration(200);
        if (this.f != null) {
            this.f.setVisibility(8);
        }
    }
}
