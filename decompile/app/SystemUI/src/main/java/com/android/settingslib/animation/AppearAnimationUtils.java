package com.android.settingslib.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settingslib.R$dimen;

public class AppearAnimationUtils implements AppearAnimationCreator<View> {
    protected boolean mAppearing;
    protected final float mDelayScale;
    private final long mDuration;
    private final Interpolator mInterpolator;
    private final AppearAnimationProperties mProperties;
    protected RowTranslationScaler mRowTranslationScaler;
    private final float mStartTranslation;

    public class AppearAnimationProperties {
        public long[][] delays;
        public int maxDelayColIndex;
        public int maxDelayRowIndex;
    }

    public interface RowTranslationScaler {
        float getRowTranslationScale(int i, int i2);
    }

    public AppearAnimationUtils(Context ctx) {
        this(ctx, 220, 1.0f, 1.0f, AnimationUtils.loadInterpolator(ctx, 17563662));
    }

    public AppearAnimationUtils(Context ctx, long duration, float translationScaleFactor, float delayScaleFactor, Interpolator interpolator) {
        this.mProperties = new AppearAnimationProperties();
        this.mInterpolator = interpolator;
        this.mStartTranslation = ((float) ctx.getResources().getDimensionPixelOffset(R$dimen.appear_y_translation_start)) * translationScaleFactor;
        this.mDelayScale = delayScaleFactor;
        this.mDuration = duration;
        this.mAppearing = true;
    }

    public void startAnimation2d(View[][] objects, Runnable finishListener) {
        startAnimation2d(objects, finishListener, this);
    }

    public void startAnimation(View[] objects, Runnable finishListener) {
        startAnimation(objects, finishListener, this);
    }

    public <T> void startAnimation2d(T[][] objects, Runnable finishListener, AppearAnimationCreator<T> creator) {
        startAnimations(getDelays((Object[][]) objects), (Object[][]) objects, finishListener, (AppearAnimationCreator) creator);
    }

    public <T> void startAnimation(T[] objects, Runnable finishListener, AppearAnimationCreator<T> creator) {
        startAnimations(getDelays((Object[]) objects), (Object[]) objects, finishListener, (AppearAnimationCreator) creator);
    }

    private <T> void startAnimations(AppearAnimationProperties properties, T[] objects, Runnable finishListener, AppearAnimationCreator<T> creator) {
        if (properties.maxDelayRowIndex == -1 || properties.maxDelayColIndex == -1) {
            finishListener.run();
            return;
        }
        for (int row = 0; row < properties.delays.length; row++) {
            float translationScale;
            long delay = properties.delays[row][0];
            Runnable endRunnable = null;
            if (properties.maxDelayRowIndex == row && properties.maxDelayColIndex == 0) {
                endRunnable = finishListener;
            }
            if (this.mRowTranslationScaler != null) {
                translationScale = this.mRowTranslationScaler.getRowTranslationScale(row, properties.delays.length);
            } else {
                translationScale = 1.0f;
            }
            float translation = translationScale * this.mStartTranslation;
            creator.createAnimation(objects[row], delay, this.mDuration, this.mAppearing ? translation : -translation, this.mAppearing, this.mInterpolator, endRunnable);
        }
    }

    private <T> void startAnimations(AppearAnimationProperties properties, T[][] objects, Runnable finishListener, AppearAnimationCreator<T> creator) {
        if (properties.maxDelayRowIndex == -1 || properties.maxDelayColIndex == -1) {
            finishListener.run();
            return;
        }
        for (int row = 0; row < properties.delays.length; row++) {
            float translationScale;
            long[] columns = properties.delays[row];
            if (this.mRowTranslationScaler != null) {
                translationScale = this.mRowTranslationScaler.getRowTranslationScale(row, properties.delays.length);
            } else {
                translationScale = 1.0f;
            }
            float translation = translationScale * this.mStartTranslation;
            int col = 0;
            while (col < columns.length) {
                long delay = columns[col];
                Runnable endRunnable = null;
                if (properties.maxDelayRowIndex == row && properties.maxDelayColIndex == col) {
                    endRunnable = finishListener;
                }
                creator.createAnimation(objects[row][col], delay, this.mDuration, this.mAppearing ? translation : -translation, this.mAppearing, this.mInterpolator, endRunnable);
                col++;
            }
        }
    }

    private <T> AppearAnimationProperties getDelays(T[] items) {
        long maxDelay = -1;
        this.mProperties.maxDelayColIndex = -1;
        this.mProperties.maxDelayRowIndex = -1;
        this.mProperties.delays = new long[items.length][];
        for (int row = 0; row < items.length; row++) {
            this.mProperties.delays[row] = new long[1];
            long delay = calculateDelay(row, 0);
            this.mProperties.delays[row][0] = delay;
            if (items[row] != null && delay > maxDelay) {
                maxDelay = delay;
                this.mProperties.maxDelayColIndex = 0;
                this.mProperties.maxDelayRowIndex = row;
            }
        }
        return this.mProperties;
    }

    private <T> AppearAnimationProperties getDelays(T[][] items) {
        long maxDelay = -1;
        this.mProperties.maxDelayColIndex = -1;
        this.mProperties.maxDelayRowIndex = -1;
        this.mProperties.delays = new long[items.length][];
        for (int row = 0; row < items.length; row++) {
            T[] columns = items[row];
            this.mProperties.delays[row] = new long[columns.length];
            for (int col = 0; col < columns.length; col++) {
                long delay = calculateDelay(row, col);
                this.mProperties.delays[row][col] = delay;
                if (items[row][col] != null && delay > maxDelay) {
                    maxDelay = delay;
                    this.mProperties.maxDelayColIndex = col;
                    this.mProperties.maxDelayRowIndex = row;
                }
            }
        }
        return this.mProperties;
    }

    protected long calculateDelay(int row, int col) {
        return (long) ((((double) (row * 40)) + ((((double) col) * (Math.pow((double) row, 0.4d) + 0.4d)) * 20.0d)) * ((double) this.mDelayScale));
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public float getStartTranslation() {
        return this.mStartTranslation;
    }

    public void createAnimation(final View view, long delay, long duration, float translationY, boolean appearing, Interpolator interpolator, Runnable endRunnable) {
        if (view != null) {
            Animator alphaAnim;
            float f;
            view.setAlpha(appearing ? 0.0f : 1.0f);
            view.setTranslationY(appearing ? translationY : 0.0f);
            float targetAlpha = appearing ? 1.0f : 0.0f;
            if (view.isHardwareAccelerated()) {
                Animator alphaAnimRt = new RenderNodeAnimator(11, targetAlpha);
                alphaAnimRt.setTarget(view);
                alphaAnim = alphaAnimRt;
            } else {
                alphaAnim = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{view.getAlpha(), targetAlpha});
            }
            alphaAnim.setInterpolator(interpolator);
            alphaAnim.setDuration(duration);
            alphaAnim.setStartDelay(delay);
            if (view.hasOverlappingRendering()) {
                view.setLayerType(2, null);
                alphaAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        view.setLayerType(0, null);
                    }
                });
            }
            if (endRunnable != null) {
                final Runnable runnable = endRunnable;
                alphaAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        runnable.run();
                    }
                });
            }
            alphaAnim.start();
            if (appearing) {
                f = 0.0f;
            } else {
                f = translationY;
            }
            startTranslationYAnimation(view, delay, duration, f, interpolator);
        }
    }

    public static void startTranslationYAnimation(View view, long delay, long duration, float endTranslationY, Interpolator interpolator) {
        Animator translationAnim;
        if (view.isHardwareAccelerated()) {
            Animator translationAnimRt = new RenderNodeAnimator(1, endTranslationY);
            translationAnimRt.setTarget(view);
            translationAnim = translationAnimRt;
        } else {
            translationAnim = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, new float[]{view.getTranslationY(), endTranslationY});
        }
        translationAnim.setInterpolator(interpolator);
        translationAnim.setDuration(duration);
        translationAnim.setStartDelay(delay);
        translationAnim.start();
    }
}
