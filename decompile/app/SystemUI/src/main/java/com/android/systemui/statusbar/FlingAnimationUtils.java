package com.android.systemui.statusbar;

import android.animation.Animator;
import android.content.Context;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;

public class FlingAnimationUtils {
    private AnimatorProperties mAnimatorProperties = new AnimatorProperties();
    private float mHighVelocityPxPerSecond;
    private Interpolator mLinearOutSlowIn;
    private float mMaxLengthSeconds;
    private float mMinVelocityPxPerSecond;

    private static class AnimatorProperties {
        long duration;
        Interpolator interpolator;

        private AnimatorProperties() {
        }
    }

    private static final class InterpolatorInterpolator implements Interpolator {
        private Interpolator mCrossfader;
        private Interpolator mInterpolator1;
        private Interpolator mInterpolator2;

        InterpolatorInterpolator(Interpolator interpolator1, Interpolator interpolator2, Interpolator crossfader) {
            this.mInterpolator1 = interpolator1;
            this.mInterpolator2 = interpolator2;
            this.mCrossfader = crossfader;
        }

        public float getInterpolation(float input) {
            float t = this.mCrossfader.getInterpolation(input);
            return ((1.0f - t) * this.mInterpolator1.getInterpolation(input)) + (this.mInterpolator2.getInterpolation(input) * t);
        }
    }

    private static final class VelocityInterpolator implements Interpolator {
        private float mDiff;
        private float mDurationSeconds;
        private float mVelocity;

        private VelocityInterpolator(float durationSeconds, float velocity, float diff) {
            this.mDurationSeconds = durationSeconds;
            this.mVelocity = velocity;
            this.mDiff = diff;
        }

        public float getInterpolation(float input) {
            return (this.mVelocity * (input * this.mDurationSeconds)) / this.mDiff;
        }
    }

    public FlingAnimationUtils(Context ctx, float maxLengthSeconds) {
        this.mMaxLengthSeconds = maxLengthSeconds;
        this.mLinearOutSlowIn = new PathInterpolator(0.0f, 0.0f, 0.35f, 1.0f);
        this.mMinVelocityPxPerSecond = ctx.getResources().getDisplayMetrics().density * 250.0f;
        this.mHighVelocityPxPerSecond = ctx.getResources().getDisplayMetrics().density * 3000.0f;
    }

    public void apply(Animator animator, float currValue, float endValue, float velocity) {
        apply(animator, currValue, endValue, velocity, Math.abs(endValue - currValue));
    }

    public void apply(ViewPropertyAnimator animator, float currValue, float endValue, float velocity) {
        apply(animator, currValue, endValue, velocity, Math.abs(endValue - currValue));
    }

    public void apply(Animator animator, float currValue, float endValue, float velocity, float maxDistance) {
        AnimatorProperties properties = getProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    public void apply(ViewPropertyAnimator animator, float currValue, float endValue, float velocity, float maxDistance) {
        AnimatorProperties properties = getProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    private AnimatorProperties getProperties(float currValue, float endValue, float velocity, float maxDistance) {
        float maxLengthSeconds = (float) (((double) this.mMaxLengthSeconds) * Math.sqrt((double) (Math.abs(endValue - currValue) / maxDistance)));
        float diff = Math.abs(endValue - currValue);
        float velAbs = Math.abs(velocity);
        float durationSeconds = (2.857143f * diff) / velAbs;
        if (durationSeconds <= maxLengthSeconds) {
            this.mAnimatorProperties.interpolator = this.mLinearOutSlowIn;
        } else if (velAbs >= this.mMinVelocityPxPerSecond) {
            durationSeconds = maxLengthSeconds;
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(maxLengthSeconds, velAbs, diff), this.mLinearOutSlowIn, this.mLinearOutSlowIn);
        } else {
            durationSeconds = maxLengthSeconds;
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        this.mAnimatorProperties.duration = (long) (1000.0f * durationSeconds);
        return this.mAnimatorProperties;
    }

    public void applyDismissing(Animator animator, float currValue, float endValue, float velocity, float maxDistance) {
        AnimatorProperties properties = getDismissingProperties(currValue, endValue, velocity, maxDistance);
        animator.setDuration(properties.duration);
        animator.setInterpolator(properties.interpolator);
    }

    private AnimatorProperties getDismissingProperties(float currValue, float endValue, float velocity, float maxDistance) {
        float maxLengthSeconds = (float) (((double) this.mMaxLengthSeconds) * Math.pow((double) (Math.abs(endValue - currValue) / maxDistance), 0.5d));
        float diff = Math.abs(endValue - currValue);
        float velAbs = Math.abs(velocity);
        float y2 = calculateLinearOutFasterInY2(velAbs);
        float startGradient = y2 / 0.5f;
        Interpolator mLinearOutFasterIn = new PathInterpolator(0.0f, 0.0f, 0.5f, y2);
        float durationSeconds = (startGradient * diff) / velAbs;
        if (durationSeconds <= maxLengthSeconds) {
            this.mAnimatorProperties.interpolator = mLinearOutFasterIn;
        } else if (velAbs >= this.mMinVelocityPxPerSecond) {
            durationSeconds = maxLengthSeconds;
            this.mAnimatorProperties.interpolator = new InterpolatorInterpolator(new VelocityInterpolator(maxLengthSeconds, velAbs, diff), mLinearOutFasterIn, this.mLinearOutSlowIn);
        } else {
            durationSeconds = maxLengthSeconds;
            this.mAnimatorProperties.interpolator = Interpolators.FAST_OUT_LINEAR_IN;
        }
        this.mAnimatorProperties.duration = (long) (1000.0f * durationSeconds);
        return this.mAnimatorProperties;
    }

    private float calculateLinearOutFasterInY2(float velocity) {
        float t = Math.max(0.0f, Math.min(1.0f, (velocity - this.mMinVelocityPxPerSecond) / (this.mHighVelocityPxPerSecond - this.mMinVelocityPxPerSecond)));
        return ((1.0f - t) * 0.4f) + (0.5f * t);
    }

    public float getMinVelocityPxPerSecond() {
        return this.mMinVelocityPxPerSecond;
    }
}
