package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.android.setupwizardlib.R$styleable;

public class Illustration extends FrameLayout {
    private float mAspectRatio = 0.0f;
    private Drawable mBackground;
    private float mBaselineGridSize;
    private Drawable mIllustration;
    private final Rect mIllustrationBounds = new Rect();
    private float mScale = 1.0f;
    private final Rect mViewBounds = new Rect();

    public Illustration(Context context) {
        super(context);
        init(null, 0);
    }

    public Illustration(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    @TargetApi(11)
    public Illustration(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.SuwIllustration, defStyleAttr, 0);
            this.mAspectRatio = a.getFloat(R$styleable.SuwIllustration_suwAspectRatio, 0.0f);
            a.recycle();
        }
        this.mBaselineGridSize = getResources().getDisplayMetrics().density * 8.0f;
        setWillNotDraw(false);
    }

    public void setBackgroundDrawable(Drawable background) {
        if (background != this.mBackground) {
            this.mBackground = background;
            invalidate();
            requestLayout();
        }
    }

    public void setIllustration(Drawable illustration) {
        if (illustration != this.mIllustration) {
            this.mIllustration = illustration;
            invalidate();
            requestLayout();
        }
    }

    public void setAspectRatio(float aspectRatio) {
        this.mAspectRatio = aspectRatio;
        invalidate();
        requestLayout();
    }

    @Deprecated
    public void setForeground(Drawable d) {
        setIllustration(d);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mAspectRatio != 0.0f) {
            int illustrationHeight = (int) (((float) MeasureSpec.getSize(widthMeasureSpec)) / this.mAspectRatio);
            setPadding(0, (int) (((float) illustrationHeight) - (((float) illustrationHeight) % this.mBaselineGridSize)), 0, 0);
        }
        if (VERSION.SDK_INT >= 21) {
            setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int layoutWidth = right - left;
        int layoutHeight = bottom - top;
        if (this.mIllustration != null) {
            int intrinsicWidth = this.mIllustration.getIntrinsicWidth();
            int intrinsicHeight = this.mIllustration.getIntrinsicHeight();
            this.mViewBounds.set(0, 0, layoutWidth, layoutHeight);
            if (this.mAspectRatio != 0.0f) {
                this.mScale = ((float) layoutWidth) / ((float) intrinsicWidth);
                intrinsicWidth = layoutWidth;
                intrinsicHeight = (int) (((float) intrinsicHeight) * this.mScale);
            }
            Gravity.apply(55, intrinsicWidth, intrinsicHeight, this.mViewBounds, this.mIllustrationBounds);
            this.mIllustration.setBounds(this.mIllustrationBounds);
        }
        if (this.mBackground != null) {
            this.mBackground.setBounds(0, 0, (int) Math.ceil((double) (((float) layoutWidth) / this.mScale)), (int) Math.ceil((double) (((float) (layoutHeight - this.mIllustrationBounds.height())) / this.mScale)));
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void onDraw(Canvas canvas) {
        if (this.mBackground != null) {
            canvas.save();
            canvas.translate(0.0f, (float) this.mIllustrationBounds.height());
            canvas.scale(this.mScale, this.mScale, 0.0f, 0.0f);
            if (VERSION.SDK_INT > 17 && shouldMirrorDrawable(this.mBackground, getLayoutDirection())) {
                canvas.scale(-1.0f, 1.0f);
                canvas.translate((float) (-this.mBackground.getBounds().width()), 0.0f);
            }
            this.mBackground.draw(canvas);
            canvas.restore();
        }
        if (this.mIllustration != null) {
            canvas.save();
            if (VERSION.SDK_INT > 17 && shouldMirrorDrawable(this.mIllustration, getLayoutDirection())) {
                canvas.scale(-1.0f, 1.0f);
                canvas.translate((float) (-this.mIllustrationBounds.width()), 0.0f);
            }
            this.mIllustration.draw(canvas);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

    private boolean shouldMirrorDrawable(Drawable drawable, int layoutDirection) {
        boolean z = true;
        if (layoutDirection == 1) {
            if (VERSION.SDK_INT >= 19) {
                return drawable.isAutoMirrored();
            }
            if (VERSION.SDK_INT >= 17) {
                if ((4194304 & getContext().getApplicationInfo().flags) == 0) {
                    z = false;
                }
                return z;
            }
        }
        return false;
    }
}
