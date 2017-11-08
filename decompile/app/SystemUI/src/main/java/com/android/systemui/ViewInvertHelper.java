package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import java.util.ArrayList;

public class ViewInvertHelper {
    private final Paint mDarkPaint;
    private final long mFadeDuration;
    private final ColorMatrix mGrayscaleMatrix;
    private final ColorMatrix mMatrix;
    private final ArrayList<View> mTargets;

    public ViewInvertHelper(View v, long fadeDuration) {
        this(v.getContext(), fadeDuration);
        addTarget(v);
    }

    public ViewInvertHelper(Context context, long fadeDuration) {
        this.mDarkPaint = new Paint();
        this.mMatrix = new ColorMatrix();
        this.mGrayscaleMatrix = new ColorMatrix();
        this.mTargets = new ArrayList();
        this.mFadeDuration = fadeDuration;
    }

    public void clearTargets() {
        this.mTargets.clear();
    }

    public void addTarget(View target) {
        this.mTargets.add(target);
    }

    public void fade(final boolean invert, long delay) {
        float startIntensity = invert ? 0.0f : 1.0f;
        float endIntensity = invert ? 1.0f : 0.0f;
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{startIntensity, endIntensity});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewInvertHelper.this.updateInvertPaint(((Float) animation.getAnimatedValue()).floatValue());
                for (int i = 0; i < ViewInvertHelper.this.mTargets.size(); i++) {
                    ((View) ViewInvertHelper.this.mTargets.get(i)).setLayerType(2, ViewInvertHelper.this.mDarkPaint);
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!invert) {
                    for (int i = 0; i < ViewInvertHelper.this.mTargets.size(); i++) {
                        ((View) ViewInvertHelper.this.mTargets.get(i)).setLayerType(0, null);
                    }
                }
            }
        });
        animator.setDuration(this.mFadeDuration);
        animator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        animator.setStartDelay(delay);
        animator.start();
    }

    public void update(boolean invert) {
        int i;
        if (invert) {
            updateInvertPaint(1.0f);
            for (i = 0; i < this.mTargets.size(); i++) {
                ((View) this.mTargets.get(i)).setLayerType(2, this.mDarkPaint);
            }
            return;
        }
        for (i = 0; i < this.mTargets.size(); i++) {
            ((View) this.mTargets.get(i)).setLayerType(0, null);
        }
    }

    private void updateInvertPaint(float intensity) {
        float components = 1.0f - (2.0f * intensity);
        this.mMatrix.set(new float[]{components, 0.0f, 0.0f, 0.0f, 255.0f * intensity, 0.0f, components, 0.0f, 0.0f, 255.0f * intensity, 0.0f, 0.0f, components, 0.0f, 255.0f * intensity, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        this.mGrayscaleMatrix.setSaturation(1.0f - intensity);
        this.mMatrix.preConcat(this.mGrayscaleMatrix);
        this.mDarkPaint.setColorFilter(new ColorMatrixColorFilter(this.mMatrix));
    }

    public void setInverted(boolean invert, boolean fade, long delay) {
        if (fade) {
            fade(invert, delay);
        } else {
            update(invert);
        }
    }
}
