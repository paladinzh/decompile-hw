package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NotificationBackgroundView extends View {
    private int mActualHeight;
    private Drawable mBackground;
    private int mClipTopAmount;
    private int mTintColor = 0;

    public NotificationBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        draw(canvas, this.mBackground);
    }

    private void draw(Canvas canvas, Drawable drawable) {
        if (drawable != null && this.mActualHeight > this.mClipTopAmount) {
            drawable.setBounds(0, 0, getWidth(), this.mActualHeight);
            canvas.clipRect(0, this.mClipTopAmount, getWidth(), this.mActualHeight);
            drawable.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    protected void drawableStateChanged() {
        drawableStateChanged(this.mBackground);
    }

    private void drawableStateChanged(Drawable d) {
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    public void drawableHotspotChanged(float x, float y) {
        if (this.mBackground != null) {
            this.mBackground.setHotspot(x, y);
        }
    }

    public void setCustomBackground(Drawable background) {
        if (this.mBackground != null) {
            this.mBackground.setCallback(null);
            unscheduleDrawable(this.mBackground);
        }
        this.mBackground = background;
        if (this.mBackground != null) {
            if (this.mTintColor != 0) {
                this.mBackground.setColorFilter(this.mTintColor, Mode.SRC_ATOP);
            }
            this.mBackground.setCallback(this);
        }
        if (this.mBackground instanceof RippleDrawable) {
            ((RippleDrawable) this.mBackground).setForceSoftware(true);
        }
        invalidate();
    }

    public void setCustomBackground(int drawableResId) {
        setCustomBackground(this.mContext.getDrawable(drawableResId));
    }

    public void setTint(int tintColor) {
        this.mTintColor = tintColor;
        if (tintColor != 0) {
            this.mBackground.setColorFilter(tintColor, Mode.SRC_ATOP);
        } else {
            this.mBackground.clearColorFilter();
        }
        invalidate();
    }

    public void setActualHeight(int actualHeight) {
        this.mActualHeight = actualHeight;
        invalidate();
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public void setClipTopAmount(int clipTopAmount) {
        this.mClipTopAmount = clipTopAmount;
        invalidate();
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setState(int[] drawableState) {
        this.mBackground.setState(drawableState);
    }

    public void setRippleColor(int color) {
        if (this.mBackground instanceof RippleDrawable) {
            this.mBackground.setColor(ColorStateList.valueOf(color));
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mClipTopAmount=" + this.mClipTopAmount + ", mActualHeight=" + this.mActualHeight);
    }
}
