package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;

public class BarTransitions {
    public static final boolean HIGH_END = ActivityManager.isHighEndGfx();
    private boolean mAlwaysOpaque = false;
    protected final BarBackgroundDrawable mBarBackground;
    private boolean mIsNavigationBar;
    protected int mMode;
    private final String mTag;
    private final View mView;

    public static class BarBackgroundDrawable extends Drawable {
        private boolean mAnimating;
        public int mColor;
        private int mColorStart;
        private long mEndTime;
        private final Drawable mGradient;
        private int mGradientAlpha;
        private int mGradientAlphaStart;
        private final Drawable mGradientLand;
        private boolean mIsLandScape;
        private boolean mIsNavigationBar;
        private int mMode = -1;
        private final int mOpaque;
        private Paint mPaint = new Paint();
        private final int mSemiTransparent;
        private long mStartTime;
        private PorterDuffColorFilter mTintFilter;
        private final int mTransparent;
        private final int mWarning;

        public BarBackgroundDrawable(Context context, int gradientResourceId, boolean isNavigationBar, View view) {
            Resources res = context.getResources();
            this.mIsNavigationBar = isNavigationBar;
            this.mSemiTransparent = context.getColor(17170544);
            this.mOpaque = context.getColor(R.color.system_bar_background_opaque);
            this.mTransparent = context.getColor(R.color.system_bar_background_transparent);
            this.mWarning = context.getColor(17170520);
            this.mGradient = context.getDrawable(gradientResourceId);
            this.mGradientLand = context.getDrawable(R.drawable.nav_background_land);
            this.mIsLandScape = SystemUiUtil.isLandscape();
            this.mGradient.setBounds(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            this.mGradientLand.setBounds(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public void setTint(int color) {
            if (this.mTintFilter == null) {
                this.mTintFilter = new PorterDuffColorFilter(color, Mode.SRC_IN);
            } else {
                this.mTintFilter.setColor(color);
            }
            invalidateSelf();
        }

        public void setTintMode(Mode tintMode) {
            if (this.mTintFilter == null) {
                this.mTintFilter = new PorterDuffColorFilter(0, tintMode);
            } else {
                this.mTintFilter.setMode(tintMode);
            }
            invalidateSelf();
        }

        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            this.mGradient.setBounds(bounds);
            this.mGradientLand.setBounds(bounds);
            this.mIsLandScape = SystemUiUtil.isLandscape();
        }

        public void applyModeBackground(int oldMode, int newMode, boolean animate) {
            if (this.mMode != newMode) {
                this.mMode = newMode;
                this.mAnimating = animate;
                if (animate) {
                    long now = SystemClock.elapsedRealtime();
                    this.mStartTime = now;
                    this.mEndTime = 200 + now;
                    this.mGradientAlphaStart = this.mGradientAlpha;
                    this.mColorStart = this.mColor;
                }
                invalidateSelf();
            }
        }

        public int getOpacity() {
            return -3;
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        public void draw(Canvas canvas) {
            int targetColor;
            if (this.mMode == 5) {
                targetColor = this.mWarning;
            } else if (this.mMode == 2) {
                if (this.mIsNavigationBar) {
                    targetColor = this.mTransparent;
                } else {
                    targetColor = this.mSemiTransparent;
                }
            } else if (this.mMode == 1) {
                targetColor = this.mSemiTransparent;
            } else if (this.mMode == 4 || this.mMode == 6) {
                targetColor = this.mTransparent;
            } else {
                targetColor = this.mOpaque;
            }
            if (this.mAnimating) {
                long now = SystemClock.elapsedRealtime();
                if (now >= this.mEndTime) {
                    this.mAnimating = false;
                    this.mColor = targetColor;
                    this.mGradientAlpha = 0;
                } else {
                    float v = Math.max(0.0f, Math.min(Interpolators.LINEAR.getInterpolation(((float) (now - this.mStartTime)) / ((float) (this.mEndTime - this.mStartTime))), 1.0f));
                    this.mGradientAlpha = (int) ((v * 0.0f) + (((float) this.mGradientAlphaStart) * (1.0f - v)));
                    this.mColor = Color.argb((int) ((((float) Color.alpha(targetColor)) * v) + (((float) Color.alpha(this.mColorStart)) * (1.0f - v))), (int) ((((float) Color.red(targetColor)) * v) + (((float) Color.red(this.mColorStart)) * (1.0f - v))), (int) ((((float) Color.green(targetColor)) * v) + (((float) Color.green(this.mColorStart)) * (1.0f - v))), (int) ((((float) Color.blue(targetColor)) * v) + (((float) Color.blue(this.mColorStart)) * (1.0f - v))));
                }
            } else {
                this.mColor = targetColor;
                this.mGradientAlpha = 0;
            }
            if (this.mIsNavigationBar && 2 == this.mMode) {
                if (!this.mIsLandScape || SystemUiUtil.isDefaultLandOrientationProduct()) {
                    this.mGradient.draw(canvas);
                } else {
                    this.mGradientLand.draw(canvas);
                }
            } else if (this.mGradientAlpha > 0) {
                this.mGradient.setAlpha(this.mGradientAlpha);
                this.mGradient.draw(canvas);
            }
            if (Color.alpha(this.mColor) > 0) {
                this.mPaint.setColor(this.mColor);
                if (this.mTintFilter != null) {
                    this.mPaint.setColorFilter(this.mTintFilter);
                }
                canvas.drawPaint(this.mPaint);
            }
            if (this.mAnimating) {
                invalidateSelf();
            }
        }
    }

    public BarTransitions(View view, int gradientResourceId, boolean isNavigationBar) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        this.mIsNavigationBar = isNavigationBar;
        this.mBarBackground = new BarBackgroundDrawable(this.mView.getContext(), gradientResourceId, isNavigationBar, view);
        if (HIGH_END) {
            this.mView.setBackground(this.mBarBackground);
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public boolean isAlwaysOpaque() {
        return HIGH_END ? this.mAlwaysOpaque : true;
    }

    public void transitionTo(int mode, boolean animate) {
        HwLog.i(this.mTag, "transitionTo: mode=" + modeToString(mode) + ", animate=" + animate);
        if (isAlwaysOpaque()) {
            if (!(mode == 1 || mode == 2)) {
                if (mode == 4) {
                }
            }
            mode = 0;
        }
        if (isAlwaysOpaque() && mode == 6) {
            mode = 3;
        }
        if (!this.mIsNavigationBar && ((TintManager.getInstance().isLuncherStyle() || TintManager.getInstance().isEmuiStyle()) && mode != 1)) {
            mode = 4;
        }
        if (this.mMode != mode) {
            int oldMode = this.mMode;
            this.mMode = mode;
            Log.d(this.mTag, String.format("%s -> %s animate=%s", new Object[]{modeToString(oldMode), modeToString(mode), Boolean.valueOf(animate)}));
            onTransition(oldMode, this.mMode, animate);
        }
    }

    protected void onTransition(int oldMode, int newMode, boolean animate) {
        if (HIGH_END) {
            applyModeBackground(oldMode, newMode, animate);
        }
    }

    protected void applyModeBackground(int oldMode, int newMode, boolean animate) {
        Log.d(this.mTag, String.format("applyModeBackground oldMode=%s newMode=%s animate=%s", new Object[]{modeToString(oldMode), modeToString(newMode), Boolean.valueOf(animate)}));
        this.mBarBackground.applyModeBackground(oldMode, newMode, animate);
    }

    public static String modeToString(int mode) {
        if (mode == 0) {
            return "MODE_OPAQUE";
        }
        if (mode == 1) {
            return "MODE_SEMI_TRANSPARENT";
        }
        if (mode == 2) {
            return "MODE_TRANSLUCENT";
        }
        if (mode == 3) {
            return "MODE_LIGHTS_OUT";
        }
        if (mode == 4) {
            return "MODE_TRANSPARENT";
        }
        if (mode == 5) {
            return "MODE_WARNING";
        }
        if (mode == 6) {
            return "MODE_LIGHTS_OUT_TRANSPARENT";
        }
        return "Unknown mode " + mode;
    }

    public void finishAnimations() {
        this.mBarBackground.finishAnimation();
    }

    protected boolean isLightsOut(int mode) {
        return mode == 3 || mode == 6;
    }
}
