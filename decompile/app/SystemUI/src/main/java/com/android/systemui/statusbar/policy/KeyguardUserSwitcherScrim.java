package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import com.android.systemui.R;

public class KeyguardUserSwitcherScrim extends Drawable implements OnLayoutChangeListener {
    private int mAlpha = 255;
    private int mDarkColor;
    private int mLayoutWidth;
    private Paint mRadialGradientPaint = new Paint();
    private int mTop;

    public KeyguardUserSwitcherScrim(Context context) {
        this.mDarkColor = context.getColor(R.color.keyguard_user_switcher_background_gradient_color);
    }

    public void draw(Canvas canvas) {
        float f;
        boolean isLtr = getLayoutDirection() == 0;
        Rect bounds = getBounds();
        float width = ((float) bounds.width()) * 2.5f;
        float height = ((float) (this.mTop + bounds.height())) * 2.5f;
        canvas.translate(0.0f, (float) (-this.mTop));
        canvas.scale(1.0f, height / width);
        if (isLtr) {
            f = ((float) bounds.right) - width;
        } else {
            f = 0.0f;
        }
        canvas.drawRect(f, 0.0f, isLtr ? (float) bounds.right : ((float) bounds.left) + width, width, this.mRadialGradientPaint);
    }

    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
        updatePaint();
        invalidateSelf();
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return -3;
    }

    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left == oldLeft && top == oldTop && right == oldRight) {
            if (bottom == oldBottom) {
                return;
            }
        }
        this.mLayoutWidth = right - left;
        this.mTop = top;
        updatePaint();
    }

    private void updatePaint() {
        if (this.mLayoutWidth != 0) {
            int i;
            float radius = ((float) this.mLayoutWidth) * 2.5f;
            boolean isLtr = getLayoutDirection() == 0;
            Paint paint = this.mRadialGradientPaint;
            if (isLtr) {
                i = this.mLayoutWidth;
            } else {
                i = 0;
            }
            paint.setShader(new RadialGradient((float) i, 0.0f, radius, new int[]{Color.argb((int) (((float) (Color.alpha(this.mDarkColor) * this.mAlpha)) / 255.0f), 0, 0, 0), 0}, new float[]{Math.max(0.0f, (((float) this.mLayoutWidth) * 0.75f) / radius), 1.0f}, TileMode.CLAMP));
        }
    }
}
