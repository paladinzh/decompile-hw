package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class StackStateAnimator {
    private AnimationFilter mAnimationFilter = new AnimationFilter();
    private Stack<AnimatorListenerAdapter> mAnimationListenerPool = new Stack();
    private HashSet<Animator> mAnimatorSet = new HashSet();
    private ValueAnimator mBottomOverScrollAnimator;
    private ArrayList<View> mChildrenToClearFromOverlay = new ArrayList();
    private long mCurrentAdditionalDelay;
    private int mCurrentLastNotAddedIndex;
    private long mCurrentLength;
    private final int mGoToFullShadeAppearingTranslation;
    private HashSet<View> mHeadsUpAppearChildren = new HashSet();
    private int mHeadsUpAppearHeightBottom;
    private final Interpolator mHeadsUpAppearInterpolator;
    private HashSet<View> mHeadsUpDisappearChildren = new HashSet();
    public NotificationStackScrollLayout mHostLayout;
    private ArrayList<View> mNewAddChildren = new ArrayList();
    private ArrayList<AnimationEvent> mNewEvents = new ArrayList();
    private boolean mShadeExpanded;
    private final StackViewState mTmpState = new StackViewState();
    private ValueAnimator mTopOverScrollAnimator;

    public StackStateAnimator(NotificationStackScrollLayout hostLayout) {
        this.mHostLayout = hostLayout;
        this.mGoToFullShadeAppearingTranslation = hostLayout.getContext().getResources().getDimensionPixelSize(R.dimen.go_to_full_shade_appearing_translation);
        this.mHeadsUpAppearInterpolator = new HeadsUpAppearInterpolator();
    }

    public boolean isRunning() {
        return !this.mAnimatorSet.isEmpty();
    }

    public void startAnimationForEvents(ArrayList<AnimationEvent> mAnimationEvents, StackScrollState finalState, long additionalDelay) {
        processAnimationEvents(mAnimationEvents, finalState);
        int childCount = this.mHostLayout.getChildCount();
        this.mAnimationFilter.applyCombination(this.mNewEvents);
        this.mCurrentAdditionalDelay = additionalDelay;
        this.mCurrentLength = AnimationEvent.combineLength(this.mNewEvents);
        this.mCurrentLastNotAddedIndex = findLastNotAddedIndex(finalState);
        for (int i = 0; i < childCount; i++) {
            ExpandableView child = (ExpandableView) this.mHostLayout.getChildAt(i);
            StackViewState viewState = finalState.getViewStateForView(child);
            if (!(viewState == null || child.getVisibility() == 8 || applyWithoutAnimation(child, viewState, finalState))) {
                startStackAnimations(child, viewState, finalState, i, -1);
            }
        }
        if (!isRunning()) {
            onAnimationFinished();
        }
        this.mHeadsUpAppearChildren.clear();
        this.mHeadsUpDisappearChildren.clear();
        this.mNewEvents.clear();
        this.mNewAddChildren.clear();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean applyWithoutAnimation(ExpandableView child, StackViewState viewState, StackScrollState finalState) {
        if (this.mShadeExpanded || getChildTag(child, R.id.translation_y_animator_tag) != null || this.mHeadsUpDisappearChildren.contains(child) || this.mHeadsUpAppearChildren.contains(child) || NotificationStackScrollLayout.isPinnedHeadsUp(child)) {
            return false;
        }
        finalState.applyState(child, viewState);
        return true;
    }

    private int findLastNotAddedIndex(StackScrollState finalState) {
        for (int i = this.mHostLayout.getChildCount() - 1; i >= 0; i--) {
            ExpandableView child = (ExpandableView) this.mHostLayout.getChildAt(i);
            StackViewState viewState = finalState.getViewStateForView(child);
            if (viewState != null && child.getVisibility() != 8 && !this.mNewAddChildren.contains(child)) {
                return viewState.notGoneIndex;
            }
        }
        return -1;
    }

    public void startStackAnimations(ExpandableView child, StackViewState viewState, StackScrollState finalState, int i, long fixedDelay) {
        boolean wasAdded = this.mNewAddChildren.contains(child);
        long duration = this.mCurrentLength;
        if (wasAdded && this.mAnimationFilter.hasGoToFullShadeEvent) {
            child.setTranslationY(child.getTranslationY() + ((float) this.mGoToFullShadeAppearingTranslation));
            duration = 514 + ((long) (100.0f * ((float) Math.pow((double) ((float) (viewState.notGoneIndex - this.mCurrentLastNotAddedIndex)), 0.699999988079071d))));
        }
        boolean yTranslationChanging = child.getTranslationY() != viewState.yTranslation;
        boolean zTranslationChanging = child.getTranslationZ() != viewState.zTranslation;
        boolean alphaChanging = viewState.alpha != child.getAlpha();
        boolean heightChanging = viewState.height != child.getActualHeight();
        boolean shadowAlphaChanging = viewState.shadowAlpha != child.getShadowAlpha();
        boolean darkChanging = viewState.dark != child.isDark();
        boolean topInsetChanging = viewState.clipTopAmount != child.getClipTopAmount();
        boolean hasDelays = this.mAnimationFilter.hasDelays;
        boolean isDelayRelevant;
        if (yTranslationChanging || zTranslationChanging || alphaChanging || heightChanging || topInsetChanging || darkChanging) {
            isDelayRelevant = true;
        } else {
            isDelayRelevant = shadowAlphaChanging;
        }
        long delay = 0;
        if (fixedDelay != -1) {
            delay = fixedDelay;
        } else if ((hasDelays && r22) || wasAdded) {
            delay = this.mCurrentAdditionalDelay + calculateChildAnimationDelay(viewState, finalState);
        }
        startViewAnimations(child, viewState, delay, duration);
        if (heightChanging) {
            startHeightAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.height_animator_tag);
        }
        if (shadowAlphaChanging) {
            startShadowAlphaAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.shadow_alpha_animator_tag);
        }
        if (topInsetChanging) {
            startInsetAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.top_inset_animator_tag);
        }
        child.setDimmed(viewState.dimmed, this.mAnimationFilter.animateDimmed);
        child.setBelowSpeedBump(viewState.belowSpeedBump);
        child.setHideSensitive(viewState.hideSensitive, this.mAnimationFilter.animateHideSensitive, delay, duration);
        child.setDark(viewState.dark, this.mAnimationFilter.animateDark, delay);
        child.setOverlap(viewState.overlap);
        if (wasAdded) {
            child.performAddAnimation(delay, this.mCurrentLength);
        }
        if (child instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) child).startChildAnimation(finalState, this, delay, duration);
        }
    }

    public void startViewAnimations(View child, ViewState viewState, long delay, long duration) {
        boolean wasVisible = child.getVisibility() == 0;
        float alpha = viewState.alpha;
        if (!(wasVisible || ((alpha == 0.0f && child.getAlpha() == 0.0f) || viewState.gone || viewState.hidden))) {
            child.setVisibility(0);
        }
        boolean yTranslationChanging = child.getTranslationY() != viewState.yTranslation;
        boolean zTranslationChanging = child.getTranslationZ() != viewState.zTranslation;
        boolean alphaChanging = viewState.alpha != child.getAlpha();
        if (child instanceof ExpandableView) {
            alphaChanging &= ((ExpandableView) child).willBeGone() ? 0 : 1;
        }
        if (yTranslationChanging) {
            startYTranslationAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.translation_y_animator_tag);
        }
        if (zTranslationChanging) {
            startZTranslationAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.translation_z_animator_tag);
        }
        if (alphaChanging && child.getTranslationX() == 0.0f) {
            startAlphaAnimation(child, viewState, duration, delay);
        } else {
            abortAnimation(child, R.id.alpha_animator_tag);
        }
    }

    private void abortAnimation(View child, int animatorTag) {
        Animator previousAnimator = (Animator) getChildTag(child, animatorTag);
        if (previousAnimator != null) {
            previousAnimator.cancel();
        }
    }

    private long calculateChildAnimationDelay(StackViewState viewState, StackScrollState finalState) {
        if (this.mAnimationFilter.hasDarkEvent) {
            return calculateDelayDark(viewState);
        }
        if (this.mAnimationFilter.hasGoToFullShadeEvent) {
            return calculateDelayGoToFullShade(viewState);
        }
        if (this.mAnimationFilter.hasHeadsUpDisappearClickEvent) {
            return 120;
        }
        long minDelay = 0;
        for (AnimationEvent event : this.mNewEvents) {
            View viewAfterChangingView;
            long delayPerElement = 80;
            switch (event.animationType) {
                case 0:
                    minDelay = Math.max(((long) (2 - Math.max(0, Math.min(2, Math.abs(viewState.notGoneIndex - finalState.getViewStateForView(event.changingView).notGoneIndex) - 1)))) * 80, minDelay);
                    continue;
                case 1:
                    break;
                case 2:
                    delayPerElement = 32;
                    break;
                default:
                    continue;
            }
            int ownIndex = viewState.notGoneIndex;
            if (event.viewAfterChangingView == null) {
                viewAfterChangingView = this.mHostLayout.getLastChildNotGone();
            } else {
                viewAfterChangingView = event.viewAfterChangingView;
            }
            int nextIndex = finalState.getViewStateForView(viewAfterChangingView).notGoneIndex;
            if (ownIndex >= nextIndex) {
                ownIndex++;
            }
            minDelay = Math.max(((long) Math.max(0, Math.min(2, Math.abs(ownIndex - nextIndex) - 1))) * delayPerElement, minDelay);
        }
        return minDelay;
    }

    private long calculateDelayDark(StackViewState viewState) {
        int referenceIndex;
        if (this.mAnimationFilter.darkAnimationOriginIndex == -1) {
            referenceIndex = 0;
        } else if (this.mAnimationFilter.darkAnimationOriginIndex == -2) {
            referenceIndex = this.mHostLayout.getNotGoneChildCount() - 1;
        } else {
            referenceIndex = this.mAnimationFilter.darkAnimationOriginIndex;
        }
        return (long) (Math.abs(referenceIndex - viewState.notGoneIndex) * 24);
    }

    private long calculateDelayGoToFullShade(StackViewState viewState) {
        return (long) (48.0f * ((float) Math.pow((double) ((float) viewState.notGoneIndex), 0.699999988079071d)));
    }

    private void startShadowAlphaAnimation(ExpandableView child, StackViewState viewState, long duration, long delay) {
        Float previousStartValue = (Float) getChildTag(child, R.id.shadow_alpha_animator_start_value_tag);
        Float previousEndValue = (Float) getChildTag(child, R.id.shadow_alpha_animator_end_value_tag);
        float newEndValue = viewState.shadowAlpha;
        if (previousEndValue == null || previousEndValue.floatValue() != newEndValue) {
            ValueAnimator previousAnimator = (ValueAnimator) getChildTag(child, R.id.shadow_alpha_animator_tag);
            if (this.mAnimationFilter.animateShadowAlpha) {
                ValueAnimator animator = ValueAnimator.ofFloat(new float[]{child.getShadowAlpha(), newEndValue});
                final ExpandableView expandableView = child;
                animator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        expandableView.setShadowAlpha(((Float) animation.getAnimatedValue()).floatValue());
                    }
                });
                animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
                if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                    animator.setStartDelay(delay);
                }
                animator.addListener(getGlobalAnimationFinishedListener());
                expandableView = child;
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        expandableView.setTag(R.id.shadow_alpha_animator_tag, null);
                        expandableView.setTag(R.id.shadow_alpha_animator_start_value_tag, null);
                        expandableView.setTag(R.id.shadow_alpha_animator_end_value_tag, null);
                    }
                });
                startAnimator(animator);
                child.setTag(R.id.shadow_alpha_animator_tag, animator);
                child.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(child.getShadowAlpha()));
                child.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(newEndValue));
            } else if (previousAnimator != null) {
                float newStartValue = previousStartValue.floatValue() + (newEndValue - previousEndValue.floatValue());
                previousAnimator.getValues()[0].setFloatValues(new float[]{newStartValue, newEndValue});
                child.setTag(R.id.shadow_alpha_animator_start_value_tag, Float.valueOf(newStartValue));
                child.setTag(R.id.shadow_alpha_animator_end_value_tag, Float.valueOf(newEndValue));
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
            } else {
                child.setShadowAlpha(newEndValue);
            }
        }
    }

    private void startHeightAnimation(ExpandableView child, StackViewState viewState, long duration, long delay) {
        Integer previousStartValue = (Integer) getChildTag(child, R.id.height_animator_start_value_tag);
        Integer previousEndValue = (Integer) getChildTag(child, R.id.height_animator_end_value_tag);
        int newEndValue = viewState.height;
        if (previousEndValue == null || previousEndValue.intValue() != newEndValue) {
            ValueAnimator previousAnimator = (ValueAnimator) getChildTag(child, R.id.height_animator_tag);
            if (this.mAnimationFilter.animateHeight) {
                ValueAnimator animator = ValueAnimator.ofInt(new int[]{child.getActualHeight(), newEndValue});
                final ExpandableView expandableView = child;
                animator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        expandableView.setActualHeight(((Integer) animation.getAnimatedValue()).intValue(), false);
                    }
                });
                animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
                if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                    animator.setStartDelay(delay);
                }
                animator.addListener(getGlobalAnimationFinishedListener());
                expandableView = child;
                animator.addListener(new AnimatorListenerAdapter() {
                    boolean mWasCancelled;

                    public void onAnimationEnd(Animator animation) {
                        expandableView.setTag(R.id.height_animator_tag, null);
                        expandableView.setTag(R.id.height_animator_start_value_tag, null);
                        expandableView.setTag(R.id.height_animator_end_value_tag, null);
                        expandableView.setActualHeightAnimating(false);
                        if (!this.mWasCancelled && (expandableView instanceof ExpandableNotificationRow)) {
                            ((ExpandableNotificationRow) expandableView).setGroupExpansionChanging(false);
                        }
                    }

                    public void onAnimationStart(Animator animation) {
                        this.mWasCancelled = false;
                    }

                    public void onAnimationCancel(Animator animation) {
                        this.mWasCancelled = true;
                    }
                });
                startAnimator(animator);
                child.setTag(R.id.height_animator_tag, animator);
                child.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(child.getActualHeight()));
                child.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(newEndValue));
                child.setActualHeightAnimating(true);
            } else if (previousAnimator != null) {
                int newStartValue = previousStartValue.intValue() + (newEndValue - previousEndValue.intValue());
                previousAnimator.getValues()[0].setIntValues(new int[]{newStartValue, newEndValue});
                child.setTag(R.id.height_animator_start_value_tag, Integer.valueOf(newStartValue));
                child.setTag(R.id.height_animator_end_value_tag, Integer.valueOf(newEndValue));
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
            } else {
                child.setActualHeight(newEndValue, false);
            }
        }
    }

    private void startInsetAnimation(ExpandableView child, StackViewState viewState, long duration, long delay) {
        Integer previousStartValue = (Integer) getChildTag(child, R.id.top_inset_animator_start_value_tag);
        Integer previousEndValue = (Integer) getChildTag(child, R.id.top_inset_animator_end_value_tag);
        int newEndValue = viewState.clipTopAmount;
        if (previousEndValue == null || previousEndValue.intValue() != newEndValue) {
            ValueAnimator previousAnimator = (ValueAnimator) getChildTag(child, R.id.top_inset_animator_tag);
            if (this.mAnimationFilter.animateTopInset) {
                ValueAnimator animator = ValueAnimator.ofInt(new int[]{child.getClipTopAmount(), newEndValue});
                final ExpandableView expandableView = child;
                animator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        expandableView.setClipTopAmount(((Integer) animation.getAnimatedValue()).intValue());
                    }
                });
                animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
                if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                    animator.setStartDelay(delay);
                }
                animator.addListener(getGlobalAnimationFinishedListener());
                expandableView = child;
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        expandableView.setTag(R.id.top_inset_animator_tag, null);
                        expandableView.setTag(R.id.top_inset_animator_start_value_tag, null);
                        expandableView.setTag(R.id.top_inset_animator_end_value_tag, null);
                    }
                });
                startAnimator(animator);
                child.setTag(R.id.top_inset_animator_tag, animator);
                child.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(child.getClipTopAmount()));
                child.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(newEndValue));
            } else if (previousAnimator != null) {
                int newStartValue = previousStartValue.intValue() + (newEndValue - previousEndValue.intValue());
                previousAnimator.getValues()[0].setIntValues(new int[]{newStartValue, newEndValue});
                child.setTag(R.id.top_inset_animator_start_value_tag, Integer.valueOf(newStartValue));
                child.setTag(R.id.top_inset_animator_end_value_tag, Integer.valueOf(newEndValue));
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
            } else {
                child.setClipTopAmount(newEndValue);
            }
        }
    }

    private void startAlphaAnimation(View child, ViewState viewState, long duration, long delay) {
        Float previousStartValue = (Float) getChildTag(child, R.id.alpha_animator_start_value_tag);
        Float previousEndValue = (Float) getChildTag(child, R.id.alpha_animator_end_value_tag);
        final float newEndValue = viewState.alpha;
        if (previousEndValue == null || previousEndValue.floatValue() != newEndValue) {
            ObjectAnimator previousAnimator = (ObjectAnimator) getChildTag(child, R.id.alpha_animator_tag);
            if (!this.mAnimationFilter.animateAlpha) {
                if (previousAnimator != null) {
                    float newStartValue = previousStartValue.floatValue() + (newEndValue - previousEndValue.floatValue());
                    previousAnimator.getValues()[0].setFloatValues(new float[]{newStartValue, newEndValue});
                    child.setTag(R.id.alpha_animator_start_value_tag, Float.valueOf(newStartValue));
                    child.setTag(R.id.alpha_animator_end_value_tag, Float.valueOf(newEndValue));
                    previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
                    return;
                }
                child.setAlpha(newEndValue);
                if (newEndValue == 0.0f) {
                    child.setVisibility(4);
                }
            }
            ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.ALPHA, new float[]{child.getAlpha(), newEndValue});
            animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            child.setLayerType(2, null);
            final View view = child;
            animator.addListener(new AnimatorListenerAdapter() {
                public boolean mWasCancelled;

                public void onAnimationEnd(Animator animation) {
                    view.setLayerType(0, null);
                    if (newEndValue == 0.0f && !this.mWasCancelled) {
                        view.setVisibility(4);
                    }
                    view.setTag(R.id.alpha_animator_tag, null);
                    view.setTag(R.id.alpha_animator_start_value_tag, null);
                    view.setTag(R.id.alpha_animator_end_value_tag, null);
                }

                public void onAnimationCancel(Animator animation) {
                    this.mWasCancelled = true;
                }

                public void onAnimationStart(Animator animation) {
                    this.mWasCancelled = false;
                }
            });
            animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
            if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                animator.setStartDelay(delay);
            }
            animator.addListener(getGlobalAnimationFinishedListener());
            startAnimator(animator);
            child.setTag(R.id.alpha_animator_tag, animator);
            child.setTag(R.id.alpha_animator_start_value_tag, Float.valueOf(child.getAlpha()));
            child.setTag(R.id.alpha_animator_end_value_tag, Float.valueOf(newEndValue));
        }
    }

    private void startZTranslationAnimation(View child, ViewState viewState, long duration, long delay) {
        Float previousStartValue = (Float) getChildTag(child, R.id.translation_z_animator_start_value_tag);
        Float previousEndValue = (Float) getChildTag(child, R.id.translation_z_animator_end_value_tag);
        float newEndValue = viewState.zTranslation;
        if (previousEndValue == null || previousEndValue.floatValue() != newEndValue) {
            ObjectAnimator previousAnimator = (ObjectAnimator) getChildTag(child, R.id.translation_z_animator_tag);
            if (!this.mAnimationFilter.animateZ) {
                if (previousAnimator != null) {
                    float newStartValue = previousStartValue.floatValue() + (newEndValue - previousEndValue.floatValue());
                    previousAnimator.getValues()[0].setFloatValues(new float[]{newStartValue, newEndValue});
                    child.setTag(R.id.translation_z_animator_start_value_tag, Float.valueOf(newStartValue));
                    child.setTag(R.id.translation_z_animator_end_value_tag, Float.valueOf(newEndValue));
                    previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
                    return;
                }
                child.setTranslationZ(newEndValue);
            }
            ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Z, new float[]{child.getTranslationZ(), newEndValue});
            animator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
            if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                animator.setStartDelay(delay);
            }
            animator.addListener(getGlobalAnimationFinishedListener());
            final View view = child;
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    view.setTag(R.id.translation_z_animator_tag, null);
                    view.setTag(R.id.translation_z_animator_start_value_tag, null);
                    view.setTag(R.id.translation_z_animator_end_value_tag, null);
                }
            });
            startAnimator(animator);
            child.setTag(R.id.translation_z_animator_tag, animator);
            child.setTag(R.id.translation_z_animator_start_value_tag, Float.valueOf(child.getTranslationZ()));
            child.setTag(R.id.translation_z_animator_end_value_tag, Float.valueOf(newEndValue));
        }
    }

    private void startYTranslationAnimation(View child, ViewState viewState, long duration, long delay) {
        Float previousStartValue = (Float) getChildTag(child, R.id.translation_y_animator_start_value_tag);
        Float previousEndValue = (Float) getChildTag(child, R.id.translation_y_animator_end_value_tag);
        float newEndValue = viewState.yTranslation;
        if (previousEndValue == null || previousEndValue.floatValue() != newEndValue) {
            ObjectAnimator previousAnimator = (ObjectAnimator) getChildTag(child, R.id.translation_y_animator_tag);
            if (this.mAnimationFilter.animateY) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, new float[]{child.getTranslationY(), newEndValue});
                animator.setInterpolator(this.mHeadsUpAppearChildren.contains(child) ? this.mHeadsUpAppearInterpolator : Interpolators.FAST_OUT_SLOW_IN);
                animator.setDuration(cancelAnimatorAndGetNewDuration(duration, previousAnimator));
                if (delay > 0 && (previousAnimator == null || previousAnimator.getAnimatedFraction() == 0.0f)) {
                    animator.setStartDelay(delay);
                }
                animator.addListener(getGlobalAnimationFinishedListener());
                final boolean isHeadsUpDisappear = this.mHeadsUpDisappearChildren.contains(child);
                final View view = child;
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        HeadsUpManager.setIsClickedNotification(view, false);
                        view.setTag(R.id.translation_y_animator_tag, null);
                        view.setTag(R.id.translation_y_animator_start_value_tag, null);
                        view.setTag(R.id.translation_y_animator_end_value_tag, null);
                        if (isHeadsUpDisappear) {
                            ((ExpandableNotificationRow) view).setHeadsupDisappearRunning(false);
                        }
                    }
                });
                startAnimator(animator);
                child.setTag(R.id.translation_y_animator_tag, animator);
                child.setTag(R.id.translation_y_animator_start_value_tag, Float.valueOf(child.getTranslationY()));
                child.setTag(R.id.translation_y_animator_end_value_tag, Float.valueOf(newEndValue));
            } else if (previousAnimator != null) {
                previousAnimator.getValues()[0].setFloatValues(new float[]{previousStartValue.floatValue() + (newEndValue - previousEndValue.floatValue()), newEndValue});
                child.setTag(R.id.translation_y_animator_start_value_tag, Float.valueOf(newStartValue));
                child.setTag(R.id.translation_y_animator_end_value_tag, Float.valueOf(newEndValue));
                previousAnimator.setCurrentPlayTime(previousAnimator.getCurrentPlayTime());
            } else {
                child.setTranslationY(newEndValue);
            }
        }
    }

    private void startAnimator(ValueAnimator animator) {
        this.mAnimatorSet.add(animator);
        animator.start();
    }

    private AnimatorListenerAdapter getGlobalAnimationFinishedListener() {
        if (this.mAnimationListenerPool.empty()) {
            return new AnimatorListenerAdapter() {
                private boolean mWasCancelled;

                public void onAnimationEnd(Animator animation) {
                    StackStateAnimator.this.mAnimatorSet.remove(animation);
                    if (StackStateAnimator.this.mAnimatorSet.isEmpty() && !this.mWasCancelled) {
                        StackStateAnimator.this.onAnimationFinished();
                    }
                    StackStateAnimator.this.mAnimationListenerPool.push(this);
                }

                public void onAnimationCancel(Animator animation) {
                    this.mWasCancelled = true;
                }

                public void onAnimationStart(Animator animation) {
                    this.mWasCancelled = false;
                }
            };
        }
        return (AnimatorListenerAdapter) this.mAnimationListenerPool.pop();
    }

    public static <T> T getChildTag(View child, int tag) {
        return child.getTag(tag);
    }

    private long cancelAnimatorAndGetNewDuration(long duration, ValueAnimator previousAnimator) {
        long newDuration = duration;
        if (previousAnimator == null) {
            return newDuration;
        }
        newDuration = Math.max(previousAnimator.getDuration() - previousAnimator.getCurrentPlayTime(), duration);
        previousAnimator.cancel();
        return newDuration;
    }

    private void onAnimationFinished() {
        this.mHostLayout.onChildAnimationFinished();
        for (View v : this.mChildrenToClearFromOverlay) {
            removeFromOverlay(v);
        }
        this.mChildrenToClearFromOverlay.clear();
    }

    private void processAnimationEvents(ArrayList<AnimationEvent> animationEvents, StackScrollState finalState) {
        for (AnimationEvent event : animationEvents) {
            final ExpandableView changingView = event.changingView;
            StackViewState viewState;
            if (event.animationType == 0) {
                viewState = finalState.getViewStateForView(changingView);
                if (viewState != null) {
                    finalState.applyState(changingView, viewState);
                    this.mNewAddChildren.add(changingView);
                }
            } else if (event.animationType == 1) {
                if (changingView.getVisibility() == 8) {
                    removeFromOverlay(changingView);
                } else {
                    viewState = finalState.getViewStateForView(event.viewAfterChangingView);
                    int actualHeight = changingView.getActualHeight();
                    float translationDirection = -1.0f;
                    if (viewState != null) {
                        translationDirection = Math.max(Math.min(((viewState.yTranslation - (changingView.getTranslationY() + (((float) actualHeight) / 2.0f))) * 2.0f) / ((float) actualHeight), 1.0f), -1.0f);
                    }
                    changingView.performRemoveAnimation(464, translationDirection, new Runnable() {
                        public void run() {
                            StackStateAnimator.removeFromOverlay(changingView);
                        }
                    });
                }
            } else if (event.animationType == 2) {
                this.mHostLayout.getOverlay().remove(changingView);
                if (Math.abs(changingView.getTranslation()) == ((float) changingView.getWidth()) && changingView.getTransientContainer() != null) {
                    changingView.getTransientContainer().removeTransientView(changingView);
                }
            } else if (event.animationType == 13) {
                event.changingView.prepareExpansionChanged(finalState);
            } else if (event.animationType == 14) {
                this.mTmpState.copyFrom(finalState.getViewStateForView(changingView));
                if (event.headsUpFromBottom) {
                    this.mTmpState.yTranslation = (float) this.mHeadsUpAppearHeightBottom;
                } else {
                    this.mTmpState.yTranslation = (float) (-this.mTmpState.height);
                }
                this.mHeadsUpAppearChildren.add(changingView);
                finalState.applyState(changingView, this.mTmpState);
            } else if (event.animationType == 15 || event.animationType == 16) {
                this.mHeadsUpDisappearChildren.add(changingView);
                if (changingView.getParent() == null) {
                    int i;
                    this.mHostLayout.getOverlay().add(changingView);
                    this.mTmpState.initFrom(changingView);
                    this.mTmpState.yTranslation = (float) (-changingView.getActualHeight());
                    this.mAnimationFilter.animateY = true;
                    ViewState viewState2 = this.mTmpState;
                    if (event.animationType == 16) {
                        i = 120;
                    } else {
                        i = 0;
                    }
                    startViewAnimations(changingView, viewState2, (long) i, 230);
                    this.mChildrenToClearFromOverlay.add(changingView);
                }
            }
            this.mNewEvents.add(event);
        }
    }

    public static void removeFromOverlay(View changingView) {
        ViewGroup parent = (ViewGroup) changingView.getParent();
        if (parent != null) {
            parent.removeView(changingView);
        }
    }

    public void animateOverScrollToAmount(float targetAmount, final boolean onTop, final boolean isRubberbanded) {
        if (targetAmount != this.mHostLayout.getCurrentOverScrollAmount(onTop)) {
            cancelOverScrollAnimators(onTop);
            ValueAnimator overScrollAnimator = ValueAnimator.ofFloat(new float[]{startOverScrollAmount, targetAmount});
            overScrollAnimator.setDuration(360);
            overScrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    StackStateAnimator.this.mHostLayout.setOverScrollAmount(((Float) animation.getAnimatedValue()).floatValue(), onTop, false, false, isRubberbanded);
                }
            });
            overScrollAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            overScrollAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (onTop) {
                        StackStateAnimator.this.mTopOverScrollAnimator = null;
                    } else {
                        StackStateAnimator.this.mBottomOverScrollAnimator = null;
                    }
                }
            });
            overScrollAnimator.start();
            if (onTop) {
                this.mTopOverScrollAnimator = overScrollAnimator;
            } else {
                this.mBottomOverScrollAnimator = overScrollAnimator;
            }
        }
    }

    public void cancelOverScrollAnimators(boolean onTop) {
        ValueAnimator currentAnimator = onTop ? this.mTopOverScrollAnimator : this.mBottomOverScrollAnimator;
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
    }

    public static int getFinalActualHeight(ExpandableView view) {
        if (view == null) {
            return 0;
        }
        if (((ValueAnimator) getChildTag(view, R.id.height_animator_tag)) == null) {
            return view.getActualHeight();
        }
        return ((Integer) getChildTag(view, R.id.height_animator_end_value_tag)).intValue();
    }

    public static float getFinalTranslationY(View view) {
        if (view == null) {
            return 0.0f;
        }
        if (((ValueAnimator) getChildTag(view, R.id.translation_y_animator_tag)) == null) {
            return view.getTranslationY();
        }
        return ((Float) getChildTag(view, R.id.translation_y_animator_end_value_tag)).floatValue();
    }

    public void setHeadsUpAppearHeightBottom(int headsUpAppearHeightBottom) {
        this.mHeadsUpAppearHeightBottom = headsUpAppearHeightBottom;
    }

    public void setShadeExpanded(boolean shadeExpanded) {
        this.mShadeExpanded = shadeExpanded;
    }
}
