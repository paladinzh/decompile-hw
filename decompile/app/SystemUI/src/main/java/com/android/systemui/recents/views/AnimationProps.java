package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.SparseArray;
import android.util.SparseLongArray;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import java.util.List;

public class AnimationProps {
    public static final AnimationProps IMMEDIATE = new AnimationProps(0, Interpolators.LINEAR);
    private AnimatorListener mListener;
    private SparseLongArray mPropDuration;
    private SparseLongArray mPropInitialPlayTime;
    private SparseArray<Interpolator> mPropInterpolators;
    private SparseLongArray mPropStartDelay;

    public AnimationProps(int duration, Interpolator interpolator) {
        this(0, duration, interpolator, null);
    }

    public AnimationProps(int duration, Interpolator interpolator, AnimatorListener listener) {
        this(0, duration, interpolator, listener);
    }

    public AnimationProps(int startDelay, int duration, Interpolator interpolator) {
        this(startDelay, duration, interpolator, null);
    }

    public AnimationProps(int startDelay, int duration, Interpolator interpolator, AnimatorListener listener) {
        setStartDelay(0, startDelay);
        setDuration(0, duration);
        setInterpolator(0, interpolator);
        setListener(listener);
    }

    public AnimatorSet createAnimator(List<Animator> animators) {
        AnimatorSet anim = new AnimatorSet();
        if (this.mListener != null) {
            anim.addListener(this.mListener);
        }
        anim.playTogether(animators);
        return anim;
    }

    public <T extends ValueAnimator> T apply(int propertyType, T animator) {
        animator.setStartDelay(getStartDelay(propertyType));
        animator.setDuration(getDuration(propertyType));
        animator.setInterpolator(getInterpolator(propertyType));
        long initialPlayTime = getInitialPlayTime(propertyType);
        if (initialPlayTime != 0) {
            animator.setCurrentPlayTime(initialPlayTime);
        }
        return animator;
    }

    public AnimationProps setStartDelay(int propertyType, int startDelay) {
        if (this.mPropStartDelay == null) {
            this.mPropStartDelay = new SparseLongArray();
        }
        this.mPropStartDelay.append(propertyType, (long) startDelay);
        return this;
    }

    public AnimationProps setInitialPlayTime(int propertyType, int initialPlayTime) {
        if (this.mPropInitialPlayTime == null) {
            this.mPropInitialPlayTime = new SparseLongArray();
        }
        this.mPropInitialPlayTime.append(propertyType, (long) initialPlayTime);
        return this;
    }

    public long getStartDelay(int propertyType) {
        if (this.mPropStartDelay == null) {
            return 0;
        }
        long startDelay = this.mPropStartDelay.get(propertyType, -1);
        if (startDelay != -1) {
            return startDelay;
        }
        return this.mPropStartDelay.get(0, 0);
    }

    public AnimationProps setDuration(int propertyType, int duration) {
        if (this.mPropDuration == null) {
            this.mPropDuration = new SparseLongArray();
        }
        this.mPropDuration.append(propertyType, (long) duration);
        return this;
    }

    public long getDuration(int propertyType) {
        if (this.mPropDuration == null) {
            return 0;
        }
        long duration = this.mPropDuration.get(propertyType, -1);
        if (duration != -1) {
            return duration;
        }
        return this.mPropDuration.get(0, 0);
    }

    public AnimationProps setInterpolator(int propertyType, Interpolator interpolator) {
        if (this.mPropInterpolators == null) {
            this.mPropInterpolators = new SparseArray();
        }
        this.mPropInterpolators.append(propertyType, interpolator);
        return this;
    }

    public Interpolator getInterpolator(int propertyType) {
        if (this.mPropInterpolators == null) {
            return Interpolators.LINEAR;
        }
        Interpolator interp = (Interpolator) this.mPropInterpolators.get(propertyType);
        if (interp != null) {
            return interp;
        }
        return (Interpolator) this.mPropInterpolators.get(0, Interpolators.LINEAR);
    }

    public long getInitialPlayTime(int propertyType) {
        if (this.mPropInitialPlayTime == null) {
            return 0;
        }
        if (this.mPropInitialPlayTime.indexOfKey(propertyType) != -1) {
            return this.mPropInitialPlayTime.get(propertyType);
        }
        return this.mPropInitialPlayTime.get(0, 0);
    }

    public AnimationProps setListener(AnimatorListener listener) {
        this.mListener = listener;
        return this;
    }

    public AnimatorListener getListener() {
        return this.mListener;
    }

    public boolean isImmediate() {
        int count = this.mPropDuration.size();
        for (int i = 0; i < count; i++) {
            if (this.mPropDuration.valueAt(i) > 0) {
                return false;
            }
        }
        return true;
    }
}
