package cn.com.xy.sms.sdk.ui.anim;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.popupview.BubblePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;

public class CardAnimUtil {
    public static void viewChangeAnim(final View hiddenView, final View showView, final BubblePopupView bubblePopupView, final AnimatorListener listener) {
        ContentUtil.setVisibilityAndAlpha(showView, 0, 0.0f);
        try {
            new Handler() {
                public void handleMessage(Message msg) {
                    try {
                        super.handleMessage(msg);
                        int showViewHeight = showView.getHeight();
                        int showViewWidth = showView.getWidth();
                        int hiddenViewHeight = hiddenView.getHeight();
                        int hiddenViewWidth = hiddenView.getWidth();
                        hiddenView.setPivotX(0.0f);
                        hiddenView.setPivotY(0.0f);
                        showView.setPivotX(0.0f);
                        showView.setPivotY(0.0f);
                        float hiddenViewScaleX = ((float) showViewWidth) / ((float) hiddenViewWidth);
                        float hiddenViewScaleY = ((float) showViewHeight) / ((float) hiddenViewHeight);
                        ObjectAnimator hiddenViewScaleXAnim = CardAnimUtil.setViewAnimator(hiddenView, "scaleX", 200, null, CardAnimUtil.onAnimationEndSetScaleX(hiddenView, ContentUtil.FONT_SIZE_NORMAL), ContentUtil.FONT_SIZE_NORMAL, hiddenViewScaleX);
                        Animator hiddenViewScaleYAnim = CardAnimUtil.setViewAnimator(hiddenView, "scaleY", 200, null, CardAnimUtil.onAnimationEndSetScaleY(hiddenView, ContentUtil.FONT_SIZE_NORMAL), ContentUtil.FONT_SIZE_NORMAL, hiddenViewScaleY);
                        float showViewScaleY = ((float) hiddenViewHeight) / ((float) showViewHeight);
                        showView.setScaleX(((float) hiddenViewWidth) / ((float) showViewWidth));
                        showView.setScaleY(showViewScaleY);
                        Animator showViewScaleXAnim = CardAnimUtil.setViewAnimator(showView, "scaleX", 200, null, CardAnimUtil.onAnimationEndSetScaleX(showView, ContentUtil.FONT_SIZE_NORMAL), showViewScaleX, ContentUtil.FONT_SIZE_NORMAL);
                        Animator showViewScaleYAnim = CardAnimUtil.setViewAnimator(showView, "scaleY", 200, null, CardAnimUtil.onAnimationEndSetScaleY(showView, ContentUtil.FONT_SIZE_NORMAL), showViewScaleY, ContentUtil.FONT_SIZE_NORMAL);
                        ObjectAnimator hiddenViewAlphaOut = CardAnimUtil.setViewAnimator(hiddenView, "alpha", 200, null, CardAnimUtil.onAnimationEndSetVisibilityAndAlpha(hiddenView, 8, ContentUtil.FONT_SIZE_NORMAL), ContentUtil.FONT_SIZE_NORMAL, 0.0f);
                        Animator showViewAlphaIn = CardAnimUtil.setViewAnimator(showView, "alpha", 200, null, null, 0.0f, ContentUtil.FONT_SIZE_NORMAL);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.play(hiddenViewScaleXAnim).with(hiddenViewScaleYAnim).with(showViewScaleXAnim).with(showViewScaleYAnim).with(hiddenViewAlphaOut).with(showViewAlphaIn);
                        if (listener != null) {
                            animatorSet.addListener(listener);
                        }
                        animatorSet.start();
                        if (bubblePopupView != null) {
                            bubblePopupView.runAnimation();
                        }
                    } catch (Throwable e) {
                        ContentUtil.setVisibilityAndAlpha(hiddenView, 8, (float) ContentUtil.FONT_SIZE_NORMAL);
                        ContentUtil.setVisibilityAndAlpha(showView, 0, (float) ContentUtil.FONT_SIZE_NORMAL);
                        CardAnimUtil.callAnimationEndListener(null, listener);
                        SmartSmsSdkUtil.smartSdkExceptionLog("CardAnimUtil viewChangeAnim handler error:" + e.getMessage(), e);
                    }
                }
            }.sendEmptyMessageDelayed(1, 50);
        } catch (Throwable e) {
            ContentUtil.setVisibilityAndAlpha(hiddenView, 8, (float) ContentUtil.FONT_SIZE_NORMAL);
            ContentUtil.setVisibilityAndAlpha(showView, 0, (float) ContentUtil.FONT_SIZE_NORMAL);
            callAnimationEndListener(null, listener);
            SmartSmsSdkUtil.smartSdkExceptionLog("CardAnimUtil viewChangeAnim error:" + e.getMessage(), e);
        }
    }

    private static ObjectAnimator setViewAnimator(View view, String propertyName, long duration, TimeInterpolator interpolator, AnimatorListener listener, float... ofFloatValues) {
        if (view == null) {
            return null;
        }
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, propertyName, ofFloatValues);
        alphaAnimator.setDuration(duration);
        if (interpolator != null) {
            alphaAnimator.setInterpolator(interpolator);
        }
        if (listener != null) {
            alphaAnimator.addListener(listener);
        }
        return alphaAnimator;
    }

    private static void callAnimationEndListener(Animator animator, AnimatorListener listener) {
        if (listener != null) {
            listener.onAnimationEnd(animator);
        }
    }

    private static AnimatorListener onAnimationEndSetScaleX(final View view, final float scaleX) {
        return view == null ? null : new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                view.setScaleX(scaleX);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        };
    }

    private static AnimatorListener onAnimationEndSetScaleY(final View view, final float scaleY) {
        return view == null ? null : new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                view.setScaleY(scaleY);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        };
    }

    private static AnimatorListener onAnimationEndSetVisibilityAndAlpha(final View view, final int visibility, final float alpha) {
        return view == null ? null : new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ContentUtil.setVisibilityAndAlpha(view, visibility, alpha);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        };
    }
}
