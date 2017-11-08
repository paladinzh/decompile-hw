package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.FloatProperty;
import android.util.Property;
import android.view.ViewDebug.ExportedProperty;
import android.widget.OverScroller;
import com.android.systemui.R;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.utils.PerfAdjust;
import java.io.PrintWriter;

public class TaskStackViewScroller {
    private static final Property<TaskStackViewScroller, Float> STACK_SCROLL = new FloatProperty<TaskStackViewScroller>("stackScroll") {
        public void setValue(TaskStackViewScroller object, float value) {
            object.setStackScroll(value);
        }

        public Float get(TaskStackViewScroller object) {
            return Float.valueOf(object.getStackScroll());
        }
    };
    TaskStackViewScrollerCallbacks mCb;
    Context mContext;
    float mFinalAnimatedScroll;
    float mFlingDownScrollP;
    int mFlingDownY;
    @ExportedProperty(category = "recents")
    float mLastDeltaP = 0.0f;
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    ObjectAnimator mScrollAnimator;
    OverScroller mScroller;
    @ExportedProperty(category = "recents")
    float mStackScrollP;

    public interface TaskStackViewScrollerCallbacks {
        void onStackScrollChanged(float f, float f2, AnimationProps animationProps);
    }

    public TaskStackViewScroller(Context context, TaskStackViewScrollerCallbacks cb, TaskStackLayoutAlgorithm layoutAlgorithm) {
        this.mContext = context;
        this.mCb = cb;
        this.mScroller = new OverScroller(context);
        this.mLayoutAlgorithm = layoutAlgorithm;
    }

    void reset() {
        this.mStackScrollP = 0.0f;
        this.mLastDeltaP = 0.0f;
    }

    void resetDeltaScroll() {
        this.mLastDeltaP = 0.0f;
    }

    public float getStackScroll() {
        return this.mStackScrollP;
    }

    public void setStackScroll(float s) {
        setStackScroll(s, AnimationProps.IMMEDIATE);
    }

    public float setDeltaStackScroll(float downP, float deltaP) {
        float targetScroll = downP + deltaP;
        float newScroll = this.mLayoutAlgorithm.updateFocusStateOnScroll(this.mLastDeltaP + downP, targetScroll, this.mStackScrollP);
        setStackScroll(newScroll, AnimationProps.IMMEDIATE);
        this.mLastDeltaP = deltaP;
        return newScroll - targetScroll;
    }

    public void setStackScroll(float newScroll, AnimationProps animation) {
        float prevScroll = this.mStackScrollP;
        this.mStackScrollP = newScroll;
        if (this.mCb != null) {
            this.mCb.onStackScrollChanged(prevScroll, this.mStackScrollP, animation);
        }
    }

    public boolean setStackScrollToInitialState() {
        float prevScroll = this.mStackScrollP;
        setStackScroll(this.mLayoutAlgorithm.mInitialScrollP);
        if (Float.compare(prevScroll, this.mStackScrollP) != 0) {
            return true;
        }
        return false;
    }

    public void fling(float downScrollP, int downY, int y, int velY, int minY, int maxY, int overscroll) {
        this.mFlingDownScrollP = downScrollP;
        this.mFlingDownY = downY;
        this.mScroller.fling(0, y, 0, velY, 0, 0, minY, maxY, 0, overscroll);
    }

    public boolean boundScroll() {
        float curScroll = getStackScroll();
        float newScroll = getBoundedStackScroll(curScroll);
        if (Float.compare(newScroll, curScroll) == 0) {
            return false;
        }
        setStackScroll(newScroll);
        return true;
    }

    float getBoundedStackScroll(float scroll) {
        return Utilities.clamp(scroll, this.mLayoutAlgorithm.mMinScrollP, this.mLayoutAlgorithm.mMaxScrollP);
    }

    float getScrollAmountOutOfBounds(float scroll) {
        if (scroll < this.mLayoutAlgorithm.mMinScrollP) {
            return Math.abs(scroll - this.mLayoutAlgorithm.mMinScrollP);
        }
        if (scroll > this.mLayoutAlgorithm.mMaxScrollP) {
            return Math.abs(scroll - this.mLayoutAlgorithm.mMaxScrollP);
        }
        return 0.0f;
    }

    boolean isScrollOutOfBounds() {
        return Float.compare(getScrollAmountOutOfBounds(this.mStackScrollP), 0.0f) != 0;
    }

    ObjectAnimator animateBoundScroll() {
        float curScroll = getStackScroll();
        float newScroll = getBoundedStackScroll(curScroll);
        if (Float.compare(newScroll, curScroll) != 0) {
            animateScroll(newScroll, null);
        }
        return this.mScrollAnimator;
    }

    void animateScroll(float newScroll, Runnable postRunnable) {
        animateScroll(newScroll, this.mContext.getResources().getInteger(R.integer.recents_animate_task_stack_scroll_duration), postRunnable);
    }

    void animateScroll(float newScroll, int duration, final Runnable postRunnable) {
        if (this.mScrollAnimator != null && this.mScrollAnimator.isRunning()) {
            setStackScroll(this.mFinalAnimatedScroll);
            this.mScroller.forceFinished(true);
        }
        stopScroller();
        stopBoundScrollAnimation();
        if (Float.compare(this.mStackScrollP, newScroll) != 0) {
            this.mFinalAnimatedScroll = newScroll;
            this.mScrollAnimator = ObjectAnimator.ofFloat(this, STACK_SCROLL, new float[]{getStackScroll(), newScroll});
            this.mScrollAnimator.setDuration((long) PerfAdjust.getScrollTaskViewAnimationDuration(duration));
            this.mScrollAnimator.setInterpolator(PerfAdjust.getScrollTaskViewAnimationInterpolator());
            this.mScrollAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (postRunnable != null) {
                        postRunnable.run();
                    }
                    TaskStackViewScroller.this.mScrollAnimator.removeAllListeners();
                }
            });
            this.mScrollAnimator.start();
        } else if (postRunnable != null) {
            postRunnable.run();
        }
    }

    void stopBoundScrollAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollAnimator);
    }

    boolean computeScroll() {
        if (!this.mScroller.computeScrollOffset()) {
            return false;
        }
        this.mFlingDownScrollP += setDeltaStackScroll(this.mFlingDownScrollP, this.mLayoutAlgorithm.getDeltaPForY(this.mFlingDownY, this.mScroller.getCurrY()));
        return true;
    }

    void stopScroller() {
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
    }

    public void dump(String prefix, PrintWriter writer) {
        writer.print(prefix);
        writer.print("TaskStackViewScroller");
        writer.print(" stackScroll:");
        writer.print(this.mStackScrollP);
        writer.println();
    }
}
