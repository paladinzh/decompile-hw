package com.huawei.gallery.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import com.android.gallery3d.R;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ImmersionUtils;

public class HorizontalIndicatorView extends ImageView implements AnimationListener {
    private int CURSOR_HEIGHT;
    private int CURSOR_WIDTH;
    private int mCurrentIndex = 1;
    private Drawable mDrawable;
    private boolean mIsAnimating = false;
    private int mLeftPadding = 0;
    private int[] mPositionArray;

    public HorizontalIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initInternal();
    }

    public HorizontalIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initInternal();
    }

    public HorizontalIndicatorView(Context context) {
        super(context);
        initInternal();
    }

    private void initInternal() {
        int darkDrawable = ColorfulUtils.getHwExtDrawable(getResources(), "tab_selected_point");
        if (darkDrawable == 0) {
            darkDrawable = R.drawable.tab_selected_point;
        }
        int lightDrawable = ColorfulUtils.getHwExtDrawable(getResources(), "tab_selected_point_light");
        if (ImmersionUtils.getControlColor(getContext()) != 0) {
            int colorfulDrawable = ColorfulUtils.getHwExtDrawable(getResources(), "tab_selected_point_colorful_light");
            if (colorfulDrawable != 0) {
                lightDrawable = colorfulDrawable;
            }
        }
        if (lightDrawable == 0) {
            lightDrawable = R.drawable.tab_selected_point_light;
        }
        Resources resources = getResources();
        if (ImmersionUtils.getImmersionStyle(getContext()) != 0) {
            lightDrawable = darkDrawable;
        }
        this.mDrawable = resources.getDrawable(lightDrawable);
        setImageDrawable(this.mDrawable);
        this.CURSOR_WIDTH = this.mDrawable.getIntrinsicWidth();
        this.CURSOR_HEIGHT = this.mDrawable.getIntrinsicHeight();
        setFocusable(false);
    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate((float) this.mLeftPadding, 0.0f);
        this.mDrawable.setBounds(0, 0, this.CURSOR_WIDTH, this.CURSOR_HEIGHT);
        this.mDrawable.draw(canvas);
        canvas.restore();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        forceIndicatorTo(this.mCurrentIndex);
    }

    public void forceIndicatorTo(int index) {
        this.mCurrentIndex = index;
        clearAnimation();
        this.mLeftPadding = getPaddingForIndex(this.mCurrentIndex);
    }

    private int getPaddingForIndex(int index) {
        return this.mPositionArray[Math.max(0, index)] - (this.CURSOR_WIDTH / 2);
    }

    public void onAnimationStart(Animation animation) {
        this.mIsAnimating = true;
    }

    public void onAnimationEnd(Animation animation) {
        this.mIsAnimating = false;
        forceIndicatorTo(this.mCurrentIndex);
    }

    public void onAnimationRepeat(Animation animation) {
    }
}
