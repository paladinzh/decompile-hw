package com.android.systemui.recents.views;

import android.content.Context;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndCancelledEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.model.TaskStack.DockState;

public class SystemBarScrimViews {
    private Context mContext;
    private boolean mHasDockedTasks = Recents.getSystemServices().hasDockedTask();
    private boolean mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
    private boolean mHasTransposedNavBar;
    private int mNavBarScrimEnterDuration;
    private View mNavBarScrimView;
    private boolean mShouldAnimateNavBarScrim;

    public SystemBarScrimViews(RecentsActivity activity) {
        this.mContext = activity;
        this.mNavBarScrimView = activity.findViewById(R.id.nav_bar_scrim);
        this.mNavBarScrimView.forceHasOverlappingRendering(false);
        this.mNavBarScrimEnterDuration = activity.getResources().getInteger(R.integer.recents_nav_bar_scrim_enter_duration);
    }

    public void updateNavBarScrim(boolean animateNavBarScrim, boolean hasStackTasks, AnimationProps animation) {
        prepareEnterRecentsAnimation(isNavBarScrimRequired(hasStackTasks), animateNavBarScrim);
        if (animateNavBarScrim && animation != null) {
            animateNavBarScrimVisibility(true, animation);
        }
    }

    private void prepareEnterRecentsAnimation(boolean hasNavBarScrim, boolean animateNavBarScrim) {
        this.mHasNavBarScrim = hasNavBarScrim;
        this.mShouldAnimateNavBarScrim = animateNavBarScrim;
        View view = this.mNavBarScrimView;
        int i = (!this.mHasNavBarScrim || this.mShouldAnimateNavBarScrim) ? 4 : 0;
        view.setVisibility(i);
    }

    private void animateNavBarScrimVisibility(boolean visible, AnimationProps animation) {
        int toY = 0;
        if (visible) {
            this.mNavBarScrimView.setVisibility(0);
            this.mNavBarScrimView.setTranslationY((float) this.mNavBarScrimView.getMeasuredHeight());
        } else {
            toY = this.mNavBarScrimView.getMeasuredHeight();
        }
        if (animation != AnimationProps.IMMEDIATE) {
            this.mNavBarScrimView.animate().translationY((float) toY).setDuration(animation.getDuration(6)).setInterpolator(animation.getInterpolator(6)).start();
        } else {
            this.mNavBarScrimView.setTranslationY((float) toY);
        }
    }

    private boolean isNavBarScrimRequired(boolean hasStackTasks) {
        return (!hasStackTasks || this.mHasTransposedNavBar || this.mHasDockedTasks) ? false : true;
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        if (this.mHasNavBarScrim) {
            AnimationProps animation;
            if (this.mShouldAnimateNavBarScrim) {
                animation = new AnimationProps().setDuration(6, this.mNavBarScrimEnterDuration).setInterpolator(6, Interpolators.DECELERATE_QUINT);
            } else {
                animation = AnimationProps.IMMEDIATE;
            }
            animateNavBarScrimVisibility(true, animation);
        }
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(DismissAllTaskViewsEvent event) {
        if (this.mHasNavBarScrim) {
            animateNavBarScrimVisibility(false, createBoundsAnimation(200));
        }
    }

    public final void onBusEvent(ConfigurationChangedEvent event) {
        if (event.fromDeviceOrientationChange) {
            this.mHasNavBarScrim = Recents.getSystemServices().hasTransposedNavigationBar();
        }
        animateScrimToCurrentNavBarState(event.hasStackTasks);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent event) {
        boolean z = false;
        this.mHasDockedTasks = event.inMultiWindow;
        if (event.stack.getStackTaskCount() > 0) {
            z = true;
        }
        animateScrimToCurrentNavBarState(z);
    }

    public final void onBusEvent(DragEndEvent event) {
        if (event.dropTarget instanceof DockState) {
            animateScrimToCurrentNavBarState(false);
        }
    }

    public final void onBusEvent(DragEndCancelledEvent event) {
        boolean z = false;
        if (event.stack.getStackTaskCount() > 0) {
            z = true;
        }
        animateScrimToCurrentNavBarState(z);
    }

    private void animateScrimToCurrentNavBarState(boolean hasStackTasks) {
        boolean hasNavBarScrim = isNavBarScrimRequired(hasStackTasks);
        if (this.mHasNavBarScrim != hasNavBarScrim) {
            AnimationProps animation;
            if (hasNavBarScrim) {
                animation = createBoundsAnimation(150);
            } else {
                animation = AnimationProps.IMMEDIATE;
            }
            animateNavBarScrimVisibility(hasNavBarScrim, animation);
        }
        this.mHasNavBarScrim = hasNavBarScrim;
    }

    private AnimationProps createBoundsAnimation(int duration) {
        return new AnimationProps().setDuration(6, duration).setInterpolator(6, Interpolators.FAST_OUT_SLOW_IN);
    }
}
