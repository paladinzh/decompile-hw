package com.android.contacts.dialpad;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class DialpadAnimateHelper {
    private View action_bar_container;
    private boolean canPlay = true;
    private DialpadAnimatorListener dialpadAnimatorListener;
    private View lTabContainer;
    private Activity mActivity;
    private ValueAnimator mAnimator;
    private View mDigistHeader;
    private EditText mDigits;
    private View mFreqListContainer;
    private int mTranslateHeight;

    public interface DialpadAnimatorListener {
        void onAnimationEnd();

        void onAnimationStart();
    }

    public void setListener(DialpadAnimatorListener listener) {
        this.dialpadAnimatorListener = listener;
    }

    public void initView(Activity activity, View digistHeader, EditText digits, View freqListContainer) {
        boolean z = true;
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadAnimateHelper", "initView");
        }
        this.mActivity = activity;
        this.mDigistHeader = digistHeader;
        this.mDigits = digits;
        this.mFreqListContainer = freqListContainer;
        if (this.mActivity != null) {
            this.canPlay = true;
            this.action_bar_container = this.mActivity.getWindow().getDecorView().findViewById(16909290);
            if ((this.action_bar_container instanceof ViewGroup) && ((ViewGroup) this.action_bar_container).getChildCount() > 2) {
                this.lTabContainer = ((ViewGroup) this.action_bar_container).getChildAt(2);
            }
            initAnimator();
        } else {
            this.canPlay = false;
            if (HwLog.HWFLOW) {
                String str = "DialpadAnimateHelper";
                StringBuilder append = new StringBuilder().append("mActivity!= null:");
                if (this.mActivity == null) {
                    z = false;
                }
                HwLog.i(str, append.append(z).toString());
            }
        }
        if (!(this.action_bar_container == null || this.lTabContainer == null || this.mFreqListContainer == null)) {
            if (this.mDigistHeader == null) {
            }
            if (HwLog.HWFLOW) {
                HwLog.i("DialpadAnimateHelper", "initView end!");
            }
        }
        this.canPlay = false;
        if (HwLog.HWFLOW) {
            HwLog.i("DialpadAnimateHelper", "initView end!");
        }
    }

    public boolean isCanPlay() {
        if (HwLog.HWFLOW) {
            HwLog.d("DialpadAnimateHelper", "isCanPlay:" + this.canPlay);
        }
        return this.canPlay;
    }

    public void setTranslateHeight(int translateHeight) {
        this.mTranslateHeight = translateHeight;
    }

    private void initAnimator() {
        if (this.mActivity == null) {
            HwLog.w("DialpadAnimateHelper", "initAnimator mActivity == null:");
            return;
        }
        final float lActbarTranslate = ((float) this.mTranslateHeight) - 50.0f;
        this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mTranslateHeight});
        this.mAnimator.setTarget(this.mFreqListContainer);
        this.mAnimator.setDuration(150);
        this.mAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mActivity.getApplicationContext(), R.interpolator.cubic_bezier_interpolator_type_10_90));
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (DialpadAnimateHelper.this.mDigistHeader != null && DialpadAnimateHelper.this.lTabContainer != null && DialpadAnimateHelper.this.mFreqListContainer != null && DialpadAnimateHelper.this.action_bar_container != null) {
                    DialpadAnimateHelper.this.mFreqListContainer.setTranslationY(value.floatValue());
                    DialpadAnimateHelper.this.lTabContainer.setAlpha(1.0f - animation.getAnimatedFraction());
                    DialpadAnimateHelper.this.mDigistHeader.setAlpha(animation.getAnimatedFraction());
                    if (value.floatValue() < lActbarTranslate) {
                        DialpadAnimateHelper.this.action_bar_container.setTranslationY(value.floatValue());
                    } else {
                        DialpadAnimateHelper.this.mDigistHeader.setVisibility(0);
                        DialpadAnimateHelper.this.mDigits.setVisibility(0);
                        DialpadAnimateHelper.this.mDigits.setSelection(DialpadAnimateHelper.this.mDigits.getText().length());
                    }
                    DialpadAnimateHelper.this.mDigistHeader.setTranslationY(((float) (-DialpadAnimateHelper.this.mTranslateHeight)) + value.floatValue());
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListener() {
            boolean hasCanceled = false;

            public void onAnimationStart(Animator arg0) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadAnimateHelper", "onAnimationStart");
                }
                this.hasCanceled = false;
                if (DialpadAnimateHelper.this.dialpadAnimatorListener != null) {
                    DialpadAnimateHelper.this.dialpadAnimatorListener.onAnimationStart();
                }
                if (DialpadAnimateHelper.this.mDigistHeader != null) {
                    DialpadAnimateHelper.this.mDigistHeader.setVisibility(8);
                    DialpadAnimateHelper.this.mDigits.setVisibility(8);
                }
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                if (DialpadAnimateHelper.this.mDigistHeader != null) {
                    if (this.hasCanceled) {
                        DialpadAnimateHelper.this.mDigistHeader.setVisibility(8);
                        DialpadAnimateHelper.this.mDigits.setVisibility(8);
                    } else {
                        DialpadAnimateHelper.this.mDigistHeader.setVisibility(0);
                        DialpadAnimateHelper.this.mDigits.setVisibility(0);
                        DialpadAnimateHelper.this.mDigits.setSelection(DialpadAnimateHelper.this.mDigits.getText().length());
                    }
                    if (DialpadAnimateHelper.this.action_bar_container != null) {
                        DialpadAnimateHelper.this.action_bar_container.setTranslationY(0.0f);
                    }
                    if (DialpadAnimateHelper.this.lTabContainer != null) {
                        DialpadAnimateHelper.this.lTabContainer.setAlpha(1.0f);
                    }
                    if (HwLog.HWDBG) {
                        HwLog.d("DialpadAnimateHelper", "onAnimationEnd end and reset TranslationY");
                    }
                    if (DialpadAnimateHelper.this.mFreqListContainer != null) {
                        DialpadAnimateHelper.this.mFreqListContainer.setTranslationY(0.0f);
                    }
                }
                if (DialpadAnimateHelper.this.dialpadAnimatorListener != null) {
                    DialpadAnimateHelper.this.dialpadAnimatorListener.onAnimationEnd();
                }
            }

            public void onAnimationCancel(Animator arg0) {
                if (HwLog.HWDBG) {
                    HwLog.d("DialpadAnimateHelper", "onAnimationEnd ,hasCanceled:" + this.hasCanceled);
                }
                this.hasCanceled = true;
            }
        });
    }

    private void playAnimator() {
        if (this.mAnimator != null) {
            this.mAnimator.start();
        }
    }

    public void reset() {
        if (this.mAnimator != null && this.mAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
        if (this.action_bar_container != null) {
            this.action_bar_container.setTranslationY(0.0f);
        }
        if (this.mFreqListContainer != null) {
            this.mFreqListContainer.setTranslationY(0.0f);
        }
        if (this.lTabContainer != null) {
            this.lTabContainer.setAlpha(1.0f);
        }
        if (this.mDigistHeader != null) {
            this.mDigistHeader.setAlpha(1.0f);
            this.mDigistHeader.setTranslationY(0.0f);
        }
    }

    public void play(boolean play) {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadAnimateHelper", "play:" + play);
        }
        if (!play) {
            reset();
        } else if (isAnimating()) {
            if (HwLog.HWDBG) {
                HwLog.d("DialpadAnimateHelper", "animator has already running!");
            }
        } else if (this.mActivity != null) {
            playAnimator();
        } else {
            HwLog.w("DialpadAnimateHelper", "animator has already running!");
            if (this.dialpadAnimatorListener != null) {
                this.dialpadAnimatorListener.onAnimationEnd();
            }
        }
    }

    public boolean isAnimating() {
        if (this.mAnimator != null) {
            return this.mAnimator.isRunning();
        }
        return false;
    }

    public void cancel() {
        if (HwLog.HWDBG) {
            HwLog.d("DialpadAnimateHelper", "cancel!");
        }
        if (this.mAnimator != null && isAnimating()) {
            this.mAnimator.cancel();
        }
    }
}
