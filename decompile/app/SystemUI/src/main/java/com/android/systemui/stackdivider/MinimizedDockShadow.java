package com.android.systemui.stackdivider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.R;

public class MinimizedDockShadow extends View {
    private int mDockSide = -1;
    private final Paint mShadowPaint = new Paint();

    public MinimizedDockShadow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updatePaint(int left, int top, int right, int bottom) {
        int startColor = this.mContext.getResources().getColor(R.color.minimize_dock_shadow_start, null);
        int endColor = this.mContext.getResources().getColor(R.color.minimize_dock_shadow_end, null);
        int middleColor = Color.argb((Color.alpha(startColor) + Color.alpha(endColor)) / 2, 0, 0, 0);
        int quarter = Color.argb((int) ((((float) Color.alpha(startColor)) * 0.25f) + (((float) Color.alpha(endColor)) * 0.75f)), 0, 0, 0);
        if (this.mDockSide == 2) {
            this.mShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) (bottom - top), new int[]{startColor, middleColor, quarter, endColor}, new float[]{0.0f, 0.35f, 0.6f, 1.0f}, TileMode.CLAMP));
        } else if (this.mDockSide == 1) {
            this.mShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, (float) (right - left), 0.0f, new int[]{startColor, middleColor, quarter, endColor}, new float[]{0.0f, 0.35f, 0.6f, 1.0f}, TileMode.CLAMP));
        } else if (this.mDockSide == 3) {
            this.mShadowPaint.setShader(new LinearGradient((float) (right - left), 0.0f, 0.0f, 0.0f, new int[]{startColor, middleColor, quarter, endColor}, new float[]{0.0f, 0.35f, 0.6f, 1.0f}, TileMode.CLAMP));
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updatePaint(left, top, right, bottom);
            invalidate();
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mShadowPaint);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
