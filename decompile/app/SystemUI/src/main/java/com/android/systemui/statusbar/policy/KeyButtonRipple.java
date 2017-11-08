package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.DisplayListCanvas;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.tint.TintManager;
import java.util.ArrayList;
import java.util.HashSet;

public class KeyButtonRipple extends Drawable {
    private final AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            KeyButtonRipple.this.mRunningAnimations.remove(animation);
            if (KeyButtonRipple.this.mRunningAnimations.isEmpty() && !KeyButtonRipple.this.mPressed) {
                KeyButtonRipple.this.mDrawingHardwareGlow = false;
                KeyButtonRipple.this.invalidateSelf();
            }
        }
    };
    private Paint mBlackePaint;
    private CanvasProperty<Float> mBottomProp;
    private Context mContext;
    private boolean mDrawingHardwareGlow;
    private float mGlowAlpha = 0.0f;
    private float mGlowScale = 1.0f;
    private final Interpolator mInterpolator = new LogInterpolator();
    private Paint mLauncherPaint;
    private CanvasProperty<Float> mLeftProp;
    private int mMaxWidth;
    private CanvasProperty<Paint> mPaintProp;
    private boolean mPressed;
    private CanvasProperty<Float> mRightProp;
    private final HashSet<Animator> mRunningAnimations = new HashSet();
    private CanvasProperty<Float> mRxProp;
    private CanvasProperty<Float> mRyProp;
    private boolean mSupportHardware;
    private final View mTargetView;
    private final ArrayList<Animator> mTmpArray = new ArrayList();
    private CanvasProperty<Float> mTopProp;
    private Paint mWhitePaint;

    private static final class LogInterpolator implements Interpolator {
        private LogInterpolator() {
        }

        public float getInterpolation(float input) {
            return 1.0f - ((float) Math.pow(400.0d, ((double) (-input)) * 1.4d));
        }
    }

    public KeyButtonRipple(Context ctx, View targetView) {
        this.mContext = ctx;
        this.mMaxWidth = ctx.getResources().getDimensionPixelSize(R.dimen.key_button_ripple_max_width);
        this.mTargetView = targetView;
    }

    private Paint getRipplePaint() {
        if (this.mBlackePaint == null) {
            this.mBlackePaint = newColorPaint(R.color.navigationbar_black_bg);
        }
        if (this.mWhitePaint == null) {
            this.mWhitePaint = newColorPaint(R.color.navigationbar_default_bg);
        }
        if (this.mLauncherPaint == null) {
            this.mLauncherPaint = newColorPaint(R.color.navigationbar_launcher_bg);
        }
        if (-1275068417 != TintManager.getInstance().getIconColorByType("navigationBarType")) {
            return this.mBlackePaint;
        }
        if (TintManager.getInstance().isLuncherStyle()) {
            return this.mLauncherPaint;
        }
        if (TintManager.getInstance().isEmuiLightStyle()) {
            return this.mBlackePaint;
        }
        return this.mWhitePaint;
    }

    private Paint newColorPaint(int colorId) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        if (this.mContext == null) {
            p.setColor(-1);
        } else {
            p.setColor(this.mContext.getResources().getColor(colorId));
        }
        return p;
    }

    private void drawSoftware(Canvas canvas) {
        if (this.mGlowAlpha > 0.0f) {
            float corner;
            Paint p = getRipplePaint();
            p.setAlpha((int) (this.mGlowAlpha * 255.0f));
            float w = (float) getBounds().width();
            float h = (float) getBounds().height();
            boolean horizontal = w > h;
            float radius = (((float) getRippleSize()) * this.mGlowScale) * 0.5f;
            float cx = w * 0.5f;
            float cy = h * 0.5f;
            float rx = horizontal ? radius : cx;
            float ry = horizontal ? cy : radius;
            if (horizontal) {
                corner = cy;
            } else {
                corner = cx;
            }
            canvas.drawRoundRect(cx - rx, cy - ry, cx + rx, cy + ry, corner, corner, p);
        }
    }

    public void draw(Canvas canvas) {
        this.mSupportHardware = canvas.isHardwareAccelerated();
        if (this.mSupportHardware) {
            drawHardware((DisplayListCanvas) canvas);
        } else {
            drawSoftware(canvas);
        }
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return -3;
    }

    private boolean isHorizontal() {
        return getBounds().width() > getBounds().height();
    }

    private void drawHardware(DisplayListCanvas c) {
        if (this.mDrawingHardwareGlow) {
            c.drawRoundRect(this.mLeftProp, this.mTopProp, this.mRightProp, this.mBottomProp, this.mRxProp, this.mRyProp, this.mPaintProp);
        }
    }

    public float getGlowAlpha() {
        return this.mGlowAlpha;
    }

    public void setGlowAlpha(float x) {
        this.mGlowAlpha = x;
        invalidateSelf();
    }

    public float getGlowScale() {
        return this.mGlowScale;
    }

    public void setGlowScale(float x) {
        this.mGlowScale = x;
        invalidateSelf();
    }

    protected boolean onStateChange(int[] state) {
        boolean pressed = false;
        for (int i : state) {
            if (i == 16842919) {
                pressed = true;
                break;
            }
        }
        if (pressed == this.mPressed) {
            return false;
        }
        setPressed(pressed);
        this.mPressed = pressed;
        return true;
    }

    public void jumpToCurrentState() {
        cancelAnimations();
    }

    public boolean isStateful() {
        return true;
    }

    public void setPressed(boolean pressed) {
        if (this.mSupportHardware) {
            setPressedHardware(pressed);
        } else {
            setPressedSoftware(pressed);
        }
    }

    private void cancelAnimations() {
        this.mTmpArray.addAll(this.mRunningAnimations);
        int size = this.mTmpArray.size();
        for (int i = 0; i < size; i++) {
            ((Animator) this.mTmpArray.get(i)).cancel();
        }
        this.mTmpArray.clear();
        this.mRunningAnimations.clear();
    }

    private void setPressedSoftware(boolean pressed) {
        if (pressed) {
            enterSoftware();
        } else {
            exitSoftware();
        }
    }

    private void enterSoftware() {
        cancelAnimations();
        this.mGlowAlpha = 0.2f;
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "glowScale", new float[]{0.0f, 1.35f});
        scaleAnimator.setInterpolator(this.mInterpolator);
        scaleAnimator.setDuration(350);
        scaleAnimator.addListener(this.mAnimatorListener);
        scaleAnimator.start();
        this.mRunningAnimations.add(scaleAnimator);
    }

    private void exitSoftware() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "glowAlpha", new float[]{this.mGlowAlpha, 0.0f});
        alphaAnimator.setInterpolator(Interpolators.ALPHA_OUT);
        alphaAnimator.setDuration(450);
        alphaAnimator.addListener(this.mAnimatorListener);
        alphaAnimator.start();
        this.mRunningAnimations.add(alphaAnimator);
    }

    private void setPressedHardware(boolean pressed) {
        if (pressed) {
            enterHardware();
        } else {
            exitHardware();
        }
    }

    private void setExtendStart(CanvasProperty<Float> prop) {
        if (isHorizontal()) {
            this.mLeftProp = prop;
        } else {
            this.mTopProp = prop;
        }
    }

    private CanvasProperty<Float> getExtendStart() {
        return isHorizontal() ? this.mLeftProp : this.mTopProp;
    }

    private void setExtendEnd(CanvasProperty<Float> prop) {
        if (isHorizontal()) {
            this.mRightProp = prop;
        } else {
            this.mBottomProp = prop;
        }
    }

    private CanvasProperty<Float> getExtendEnd() {
        return isHorizontal() ? this.mRightProp : this.mBottomProp;
    }

    private int getExtendSize() {
        return isHorizontal() ? getBounds().width() : getBounds().height();
    }

    private int getRippleSize() {
        return Math.min(isHorizontal() ? getBounds().width() : getBounds().height(), this.mMaxWidth);
    }

    private void enterHardware() {
        cancelAnimations();
        this.mDrawingHardwareGlow = true;
        setExtendStart(CanvasProperty.createFloat((float) (getExtendSize() / 2)));
        RenderNodeAnimator startAnim = new RenderNodeAnimator(getExtendStart(), ((float) (getExtendSize() / 2)) - ((((float) getRippleSize()) * 1.35f) / 2.0f));
        startAnim.setDuration(350);
        startAnim.setInterpolator(this.mInterpolator);
        startAnim.addListener(this.mAnimatorListener);
        startAnim.setTarget(this.mTargetView);
        setExtendEnd(CanvasProperty.createFloat((float) (getExtendSize() / 2)));
        RenderNodeAnimator endAnim = new RenderNodeAnimator(getExtendEnd(), ((float) (getExtendSize() / 2)) + ((((float) getRippleSize()) * 1.35f) / 2.0f));
        endAnim.setDuration(350);
        endAnim.setInterpolator(this.mInterpolator);
        endAnim.addListener(this.mAnimatorListener);
        endAnim.setTarget(this.mTargetView);
        if (isHorizontal()) {
            this.mTopProp = CanvasProperty.createFloat(0.0f);
            this.mBottomProp = CanvasProperty.createFloat((float) getBounds().height());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().height() / 2));
        } else {
            this.mLeftProp = CanvasProperty.createFloat(0.0f);
            this.mRightProp = CanvasProperty.createFloat((float) getBounds().width());
            this.mRxProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
            this.mRyProp = CanvasProperty.createFloat((float) (getBounds().width() / 2));
        }
        this.mGlowScale = 1.35f;
        this.mGlowAlpha = 0.2f;
        Paint ripplePaint = getRipplePaint();
        ripplePaint.setAlpha((int) (this.mGlowAlpha * 255.0f));
        this.mPaintProp = CanvasProperty.createPaint(ripplePaint);
        startAnim.start();
        endAnim.start();
        this.mRunningAnimations.add(startAnim);
        this.mRunningAnimations.add(endAnim);
        invalidateSelf();
    }

    private void exitHardware() {
        this.mPaintProp = CanvasProperty.createPaint(getRipplePaint());
        RenderNodeAnimator opacityAnim = new RenderNodeAnimator(this.mPaintProp, 1, 0.0f);
        opacityAnim.setDuration(450);
        opacityAnim.setInterpolator(Interpolators.ALPHA_OUT);
        opacityAnim.addListener(this.mAnimatorListener);
        opacityAnim.setTarget(this.mTargetView);
        opacityAnim.start();
        this.mRunningAnimations.add(opacityAnim);
        invalidateSelf();
    }
}
