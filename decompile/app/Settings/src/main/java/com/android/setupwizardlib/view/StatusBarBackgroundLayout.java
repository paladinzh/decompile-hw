package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.setupwizardlib.R$styleable;

public class StatusBarBackgroundLayout extends FrameLayout {
    private Object mLastInsets;
    private Drawable mStatusBarBackground;

    public StatusBarBackgroundLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public StatusBarBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @TargetApi(11)
    public StatusBarBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SuwStatusBarBackgroundLayout, defStyleAttr, 0);
        setStatusBarBackground(a.getDrawable(R$styleable.SuwStatusBarBackgroundLayout_suwStatusBarBackground));
        a.recycle();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (VERSION.SDK_INT >= 21 && this.mLastInsets == null) {
            requestApplyInsets();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (VERSION.SDK_INT >= 21 && this.mLastInsets != null) {
            int insetTop = ((WindowInsets) this.mLastInsets).getSystemWindowInsetTop();
            if (insetTop > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), insetTop);
                this.mStatusBarBackground.draw(canvas);
            }
        }
    }

    public void setStatusBarBackground(Drawable background) {
        boolean z = true;
        this.mStatusBarBackground = background;
        if (VERSION.SDK_INT >= 21) {
            boolean z2;
            if (background == null) {
                z2 = true;
            } else {
                z2 = false;
            }
            setWillNotDraw(z2);
            if (background == null) {
                z = false;
            }
            setFitsSystemWindows(z);
            invalidate();
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mLastInsets = insets;
        return super.onApplyWindowInsets(insets);
    }
}
