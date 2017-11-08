package com.android.systemui.volume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;

public class VolumeDialogMotion {
    private static final String TAG = Util.logTag(VolumeDialogMotion.class);
    private boolean ANIMATION_ENABLE = false;
    private boolean mAnimating;
    private final Callback mCallback;
    private final View mChevron;
    private ValueAnimator mChevronPositionAnimator;
    private final ViewGroup mContents;
    private ValueAnimator mContentsPositionAnimator;
    private final Dialog mDialog;
    private final View mDialogView;
    private boolean mDismissing;
    private final Handler mHandler = new Handler();
    private boolean mShowing;

    public interface Callback {
        void onAnimatingChanged(boolean z);
    }

    private static final class LogAccelerateInterpolator implements TimeInterpolator {
        private final int mBase;
        private final int mDrift;
        private final float mLogScale;

        private LogAccelerateInterpolator() {
            this(100, 0);
        }

        private LogAccelerateInterpolator(int base, int drift) {
            this.mBase = base;
            this.mDrift = drift;
            this.mLogScale = 1.0f / computeLog(1.0f, this.mBase, this.mDrift);
        }

        private static float computeLog(float t, int base, int drift) {
            return (((float) (-Math.pow((double) base, (double) (-t)))) + 1.0f) + (((float) drift) * t);
        }

        public float getInterpolation(float t) {
            return 1.0f - (computeLog(1.0f - t, this.mBase, this.mDrift) * this.mLogScale);
        }
    }

    private static final class LogDecelerateInterpolator implements TimeInterpolator {
        private final float mBase;
        private final float mDrift;
        private final float mOutputScale;
        private final float mTimeScale;

        private LogDecelerateInterpolator() {
            this(400.0f, 1.4f, 0.0f);
        }

        private LogDecelerateInterpolator(float base, float timeScale, float drift) {
            this.mBase = base;
            this.mDrift = drift;
            this.mTimeScale = 1.0f / timeScale;
            this.mOutputScale = 1.0f / computeLog(1.0f);
        }

        private float computeLog(float t) {
            return (1.0f - ((float) Math.pow((double) this.mBase, (double) ((-t) * this.mTimeScale)))) + (this.mDrift * t);
        }

        public float getInterpolation(float t) {
            return computeLog(t) * this.mOutputScale;
        }
    }

    public VolumeDialogMotion(Dialog dialog, View dialogView, ViewGroup contents, View chevron, Callback callback) {
        this.mDialog = dialog;
        this.mDialogView = dialogView;
        this.mContents = contents;
        this.mChevron = chevron;
        this.mCallback = callback;
        this.mDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "mDialog.onDismiss");
                }
            }
        });
        this.mDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                if (D.BUG) {
                    Log.d(VolumeDialogMotion.TAG, "mDialog.onShow");
                }
                if (VolumeDialogMotion.this.ANIMATION_ENABLE) {
                    VolumeDialogMotion.this.mDialogView.setTranslationY((float) (-VolumeDialogMotion.this.mDialogView.getHeight()));
                }
                VolumeDialogMotion.this.startShowAnimation();
            }
        });
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

    private void setShowing(boolean showing) {
        if (showing != this.mShowing) {
            this.mShowing = showing;
            if (D.BUG) {
                Log.d(TAG, "mShowing = " + this.mShowing);
            }
            updateAnimating();
        }
    }

    private void setDismissing(boolean dismissing) {
        if (dismissing != this.mDismissing) {
            this.mDismissing = dismissing;
            if (D.BUG) {
                Log.d(TAG, "mDismissing = " + this.mDismissing);
            }
            updateAnimating();
        }
    }

    private void updateAnimating() {
        boolean animating = !this.mShowing ? this.mDismissing : true;
        if (animating != this.mAnimating) {
            this.mAnimating = animating;
            if (D.BUG) {
                Log.d(TAG, "mAnimating = " + this.mAnimating);
            }
            if (this.mCallback != null) {
                this.mCallback.onAnimatingChanged(this.mAnimating);
            }
        }
    }

    public void startShow() {
        if (D.BUG) {
            Log.d(TAG, "startShow");
        }
        if (!this.mShowing) {
            setShowing(true);
            if (this.mDismissing) {
                this.mDialogView.animate().cancel();
                setDismissing(false);
                startShowAnimation();
                return;
            }
            if (D.BUG) {
                Log.d(TAG, "mDialog.show()");
            }
            this.mDialog.show();
        }
    }

    private int chevronDistance() {
        return this.mChevron.getHeight() / 6;
    }

    private int chevronPosY() {
        Object tag = null;
        if (this.mChevron != null) {
            tag = this.mChevron.getTag();
        }
        return tag == null ? 0 : ((Integer) tag).intValue();
    }

    private void startShowAnimation() {
        if (D.BUG) {
            Log.d(TAG, "startShowAnimation");
        }
        if (this.ANIMATION_ENABLE) {
            this.mDialogView.animate().translationY(0.0f).setDuration((long) scaledDuration(300)).setInterpolator(new LogDecelerateInterpolator()).setListener(null).setUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (VolumeDialogMotion.this.mChevronPositionAnimator != null) {
                        VolumeDialogMotion.this.mChevron.setTranslationY((((float) VolumeDialogMotion.this.chevronPosY()) + ((Float) VolumeDialogMotion.this.mChevronPositionAnimator.getAnimatedValue()).floatValue()) + (-VolumeDialogMotion.this.mDialogView.getTranslationY()));
                    }
                }
            }).start();
            this.mContentsPositionAnimator = ValueAnimator.ofFloat(new float[]{(float) (-chevronDistance()), 0.0f}).setDuration((long) scaledDuration(400));
            this.mContentsPositionAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationEnd(Animator animation) {
                    if (!this.mCancelled) {
                        if (D.BUG) {
                            Log.d(VolumeDialogMotion.TAG, "show.onAnimationEnd");
                        }
                        VolumeDialogMotion.this.setShowing(false);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    if (D.BUG) {
                        Log.d(VolumeDialogMotion.TAG, "show.onAnimationCancel");
                    }
                    this.mCancelled = true;
                }
            });
            this.mContentsPositionAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    VolumeDialogMotion.this.mContents.setTranslationY((-VolumeDialogMotion.this.mDialogView.getTranslationY()) + ((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            this.mContentsPositionAnimator.setInterpolator(new LogDecelerateInterpolator());
            this.mContentsPositionAnimator.start();
            this.mContents.setAlpha(0.0f);
            this.mContents.animate().alpha(1.0f).setDuration((long) scaledDuration(150)).setInterpolator(new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f)).start();
            this.mChevronPositionAnimator = ValueAnimator.ofFloat(new float[]{(float) (-chevronDistance()), 0.0f}).setDuration((long) scaledDuration(250));
            this.mChevronPositionAnimator.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f));
            this.mChevronPositionAnimator.start();
            this.mChevron.setAlpha(0.0f);
            this.mChevron.animate().alpha(1.0f).setStartDelay((long) scaledDuration(50)).setDuration((long) scaledDuration(150)).setInterpolator(new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f)).start();
            return;
        }
        setShowing(false);
    }

    public void startDismiss(final Runnable onComplete) {
        if (D.BUG) {
            Log.d(TAG, "startDismiss");
        }
        if (!this.mDismissing) {
            setDismissing(true);
            if (this.mShowing) {
                this.mDialogView.animate().cancel();
                if (this.mContentsPositionAnimator != null) {
                    this.mContentsPositionAnimator.cancel();
                }
                this.mContents.animate().cancel();
                if (this.mChevronPositionAnimator != null) {
                    this.mChevronPositionAnimator.cancel();
                }
                this.mChevron.animate().cancel();
            }
            if (this.ANIMATION_ENABLE) {
                this.mDialogView.animate().translationY((float) (-this.mDialogView.getHeight())).setDuration((long) scaledDuration(250)).setInterpolator(new LogAccelerateInterpolator()).setUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        VolumeDialogMotion.this.mContents.setTranslationY(-VolumeDialogMotion.this.mDialogView.getTranslationY());
                        VolumeDialogMotion.this.mChevron.setTranslationY(((float) VolumeDialogMotion.this.chevronPosY()) + (-VolumeDialogMotion.this.mDialogView.getTranslationY()));
                    }
                }).setListener(new AnimatorListenerAdapter() {
                    private boolean mCancelled;

                    public void onAnimationEnd(Animator animation) {
                        if (!this.mCancelled) {
                            if (D.BUG) {
                                Log.d(VolumeDialogMotion.TAG, "dismiss.onAnimationEnd");
                            }
                            Handler -get7 = VolumeDialogMotion.this.mHandler;
                            final Runnable runnable = onComplete;
                            -get7.postDelayed(new Runnable() {
                                public void run() {
                                    if (D.BUG) {
                                        Log.d(VolumeDialogMotion.TAG, "mDialog.dismiss()");
                                    }
                                    VolumeDialogMotion.this.mDialog.dismiss();
                                    runnable.run();
                                    VolumeDialogMotion.this.setDismissing(false);
                                }
                            }, 50);
                        }
                    }

                    public void onAnimationCancel(Animator animation) {
                        if (D.BUG) {
                            Log.d(VolumeDialogMotion.TAG, "dismiss.onAnimationCancel");
                        }
                        this.mCancelled = true;
                    }
                }).start();
                return;
            }
            this.mDialog.dismiss();
            onComplete.run();
            setDismissing(false);
        }
    }

    private static int scaledDuration(int base) {
        return (int) (((float) base) * 1.0f);
    }
}
