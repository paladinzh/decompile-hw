package com.huawei.keyguard.view.effect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Point;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;

public class AnimUtils {

    public static class SimpleAnimListener extends AnimatorListenerAdapter {
    }

    public static final TimeInterpolator getInterpolator_20_90() {
        return new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    }

    public static void adjustViewPivot(View target) {
        Point p = HwUnlockUtils.getPoint(target.getContext());
        target.setPivotY((float) (target.getHeight() - (Math.max(p.x, p.y) >> 1)));
    }

    public static boolean startEnterSecurityViewAnimation(final View target, final Runnable endRunnable) {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        adjustViewPivot(target);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float param = ((Float) animation.getAnimatedValue()).floatValue();
                float scale = ((1.0f - param) * 0.1f) + 1.0f;
                target.setScaleX(scale);
                target.setScaleY(scale);
                target.setAlpha(param);
            }
        });
        anim.setInterpolator(getInterpolator_20_90());
        anim.setDuration(350);
        if (endRunnable != null) {
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    endRunnable.run();
                }
            });
        }
        anim.start();
        return true;
    }

    public static boolean startExitSecurityViewAnimation(final View target, long duration) {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float param = ((Float) animation.getAnimatedValue()).floatValue();
                float scale = 1.0f - (0.15f * param);
                target.setScaleX(scale);
                target.setScaleY(scale);
                target.setAlpha(1.0f - param);
                HwLog.e("AnimUtils", "startExitSecurityViewAnimation with scale : " + scale + " alpha : " + (1.0f - param));
            }
        });
        anim.setInterpolator(getInterpolator_20_90());
        anim.setDuration(duration);
        anim.start();
        return true;
    }

    public static boolean startExitSecurityViewAnimation(View target, Runnable endRunnable) {
        HwLog.w("AnimUtils", "startExitSecurityViewAnimation");
        if (target != null) {
            target.setScaleX(0.85f);
            target.setScaleY(0.85f);
            target.setAlpha(0.0f);
        }
        if (endRunnable != null) {
            endRunnable.run();
        }
        return true;
    }

    public static boolean startRevertSecurityViewAnimation(final View target, final Runnable endRunnable) {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float param = ((Float) animation.getAnimatedValue()).floatValue();
                float scale = 1.0f + (0.1f * param);
                target.setScaleX(scale);
                target.setScaleY(scale);
                target.setAlpha(1.0f - param);
            }
        });
        anim.setInterpolator(getInterpolator_20_90());
        anim.setDuration(200);
        if (endRunnable != null) {
            anim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    endRunnable.run();
                }
            });
        }
        anim.start();
        return true;
    }
}
