package com.android.mms.attachment.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.google.android.gms.R;

public class UiUtils {
    public static final Interpolator DEFAULT_INTERPOLATOR = new CubicBezierInterpolator(0.4f, 0.0f, 0.2f, ContentUtil.FONT_SIZE_NORMAL);
    public static final Interpolator EASE_IN_INTERPOLATOR = new CubicBezierInterpolator(0.4f, 0.0f, 0.8f, 0.5f);
    public static final Interpolator EASE_OUT_INTERPOLATOR = new CubicBezierInterpolator(0.0f, 0.0f, 0.2f, ContentUtil.FONT_SIZE_NORMAL);
    public static final int MEDIAPICKER_TRANSITION_DURATION = getApplicationContext().getResources().getInteger(R.integer.mediapicker_transition_duration);
    public static final int REVEAL_ANIMATION_DURATION = getApplicationContext().getResources().getInteger(R.integer.reveal_view_animation_duration);

    public static Context getApplicationContext() {
        return MmsApp.getApplication().getApplicationContext();
    }

    public static boolean isInLandscape() {
        return getApplicationContext().getResources().getConfiguration().orientation == 2;
    }

    public static Rect getMeasuredBoundsOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void revealOrHideViewWithAnimation(boolean isFullScreen, View view, int desiredVisibility, final Runnable onFinishRunnable) {
        boolean needAnimation = false;
        if (view.getVisibility() != desiredVisibility) {
            needAnimation = true;
        }
        if (needAnimation) {
            float fromScale = desiredVisibility == 0 ? 0.0f : ContentUtil.FONT_SIZE_NORMAL;
            float toScale = desiredVisibility == 0 ? ContentUtil.FONT_SIZE_NORMAL : 0.0f;
            ScaleAnimation showHideAnimation = new ScaleAnimation(fromScale, toScale, fromScale, toScale, 1, 0.5f, 1, 0.5f);
            showHideAnimation.setDuration((long) REVEAL_ANIMATION_DURATION);
            showHideAnimation.setInterpolator(DEFAULT_INTERPOLATOR);
            showHideAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (onFinishRunnable != null) {
                        ThreadUtil.getMainThreadHandler().post(onFinishRunnable);
                    }
                }
            });
            view.clearAnimation();
            if (isFullScreen) {
                ThreadUtil.getMainThreadHandler().post(onFinishRunnable);
                return;
            }
            view.startAnimation(showHideAnimation);
            view.setVisibility(desiredVisibility);
        } else if (onFinishRunnable != null) {
            ThreadUtil.getMainThreadHandler().post(onFinishRunnable);
        }
    }
}
