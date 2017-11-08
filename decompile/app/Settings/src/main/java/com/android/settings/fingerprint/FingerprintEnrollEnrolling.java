package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener;

public class FingerprintEnrollEnrolling extends FingerprintEnrollBase implements Listener {
    private boolean mAnimationCancelled;
    private final Runnable mDelayedFinishRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.launchFinish(FingerprintEnrollEnrolling.this.mToken);
        }
    };
    private TextView mErrorText;
    private Interpolator mFastOutLinearInInterpolator;
    private Interpolator mFastOutSlowInInterpolator;
    private ImageView mFingerprintAnimator;
    private final AnimationCallback mIconAnimationCallback = new AnimationCallback() {
        public void onAnimationEnd(Drawable d) {
            if (!FingerprintEnrollEnrolling.this.mAnimationCancelled) {
                FingerprintEnrollEnrolling.this.mFingerprintAnimator.post(new Runnable() {
                    public void run() {
                        FingerprintEnrollEnrolling.this.startIconAnimation();
                    }
                });
            }
        }
    };
    private AnimatedVectorDrawable mIconAnimationDrawable;
    private int mIconTouchCount;
    private int mIndicatorBackgroundActivatedColor;
    private int mIndicatorBackgroundRestingColor;
    private Interpolator mLinearOutSlowInInterpolator;
    private ObjectAnimator mProgressAnim;
    private final AnimatorListener mProgressAnimationListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (FingerprintEnrollEnrolling.this.mProgressBar.getProgress() >= 10000) {
                FingerprintEnrollEnrolling.this.mProgressBar.postDelayed(FingerprintEnrollEnrolling.this.mDelayedFinishRunnable, 250);
            }
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private ProgressBar mProgressBar;
    private TextView mRepeatMessage;
    private boolean mRestoring;
    private final Runnable mShowDialogRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.showIconTouchDialog();
        }
    };
    private FingerprintEnrollSidecar mSidecar;
    private TextView mStartMessage;
    private final Runnable mTouchAgainRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.showError(FingerprintEnrollEnrolling.this.getString(2131624685));
        }
    };

    public static class ErrorDialog extends DialogFragment {
        static ErrorDialog newInstance(CharSequence msg, int msgId) {
            ErrorDialog dlg = new ErrorDialog();
            Bundle args = new Bundle();
            args.putCharSequence("error_msg", msg);
            args.putInt("error_id", msgId);
            dlg.setArguments(args);
            return dlg;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            CharSequence errorString = getArguments().getCharSequence("error_msg");
            final int errMsgId = getArguments().getInt("error_id");
            builder.setTitle(2131624678).setMessage(errorString).setCancelable(false).setPositiveButton(2131624662, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int i = 3;
                    dialog.dismiss();
                    boolean wasTimeout = errMsgId == 3;
                    Activity activity = ErrorDialog.this.getActivity();
                    if (!wasTimeout) {
                        i = 1;
                    }
                    activity.setResult(i);
                    activity.finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

    public static class IconTouchDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(2131624676).setMessage(2131624677).setPositiveButton(2131624662, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        setContentView(2130968785);
        setHeaderText(2131624664);
        this.mStartMessage = (TextView) findViewById(2131886617);
        this.mRepeatMessage = (TextView) findViewById(2131886618);
        this.mErrorText = (TextView) findViewById(2131886619);
        this.mProgressBar = (ProgressBar) findViewById(2131886621);
        this.mFingerprintAnimator = (ImageView) findViewById(2131886620);
        this.mIconAnimationDrawable = (AnimatedVectorDrawable) this.mFingerprintAnimator.getDrawable();
        this.mIconAnimationDrawable.registerAnimationCallback(this.mIconAnimationCallback);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this, 17563661);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this, 17563663);
        this.mFingerprintAnimator.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == 0) {
                    FingerprintEnrollEnrolling fingerprintEnrollEnrolling = FingerprintEnrollEnrolling.this;
                    fingerprintEnrollEnrolling.mIconTouchCount = fingerprintEnrollEnrolling.mIconTouchCount + 1;
                    if (FingerprintEnrollEnrolling.this.mIconTouchCount == 3) {
                        FingerprintEnrollEnrolling.this.showIconTouchDialog();
                    } else {
                        FingerprintEnrollEnrolling.this.mFingerprintAnimator.postDelayed(FingerprintEnrollEnrolling.this.mShowDialogRunnable, 500);
                    }
                } else if (event.getActionMasked() == 3 || event.getActionMasked() == 1) {
                    FingerprintEnrollEnrolling.this.mFingerprintAnimator.removeCallbacks(FingerprintEnrollEnrolling.this.mShowDialogRunnable);
                }
                return true;
            }
        });
        this.mIndicatorBackgroundRestingColor = getColor(2131427452);
        this.mIndicatorBackgroundActivatedColor = getColor(2131427599);
        if (savedInstanceState != null) {
            z = true;
        } else {
            z = false;
        }
        this.mRestoring = z;
    }

    protected void onStart() {
        super.onStart();
        this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag("sidecar");
        if (this.mSidecar == null) {
            this.mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(this.mSidecar, "sidecar").commit();
        }
        this.mSidecar.setListener(this);
        updateProgress(false);
        updateDescription();
        if (this.mRestoring) {
            startIconAnimation();
        }
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        this.mAnimationCancelled = false;
        startIconAnimation();
    }

    private void startIconAnimation() {
        this.mIconAnimationDrawable.start();
    }

    private void stopIconAnimation() {
        this.mAnimationCancelled = true;
        this.mIconAnimationDrawable.stop();
    }

    protected void onStop() {
        super.onStop();
        if (this.mSidecar != null) {
            this.mSidecar.setListener(null);
        }
        stopIconAnimation();
        if (!isChangingConfigurations()) {
            if (this.mSidecar != null) {
                this.mSidecar.cancelEnrollment();
                getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
            }
            finish();
        }
    }

    public void onBackPressed() {
        if (this.mSidecar != null) {
            this.mSidecar.setListener(null);
            this.mSidecar.cancelEnrollment();
            getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
            this.mSidecar = null;
        }
        super.onBackPressed();
    }

    private void animateProgress(int progress) {
        if (this.mProgressAnim != null) {
            this.mProgressAnim.cancel();
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(this.mProgressBar, "progress", new int[]{this.mProgressBar.getProgress(), progress});
        anim.addListener(this.mProgressAnimationListener);
        anim.setInterpolator(this.mFastOutSlowInInterpolator);
        anim.setDuration(250);
        anim.start();
        this.mProgressAnim = anim;
    }

    private void animateFlash() {
        ValueAnimator anim = ValueAnimator.ofArgb(new int[]{this.mIndicatorBackgroundRestingColor, this.mIndicatorBackgroundActivatedColor});
        final AnimatorUpdateListener listener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintEnrollEnrolling.this.mFingerprintAnimator.setBackgroundTintList(ColorStateList.valueOf(((Integer) animation.getAnimatedValue()).intValue()));
            }
        };
        anim.addUpdateListener(listener);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ValueAnimator anim = ValueAnimator.ofArgb(new int[]{FingerprintEnrollEnrolling.this.mIndicatorBackgroundActivatedColor, FingerprintEnrollEnrolling.this.mIndicatorBackgroundRestingColor});
                anim.addUpdateListener(listener);
                anim.setDuration(300);
                anim.setInterpolator(FingerprintEnrollEnrolling.this.mLinearOutSlowInInterpolator);
                anim.start();
            }
        });
        anim.setInterpolator(this.mFastOutSlowInInterpolator);
        anim.setDuration(300);
        anim.start();
    }

    private void launchFinish(byte[] token) {
        Intent intent = getFinishIntent();
        intent.addFlags(33554432);
        intent.putExtra("hw_auth_token", token);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivity(intent);
        finish();
    }

    protected Intent getFinishIntent() {
        return new Intent(this, FingerprintEnrollFinish.class);
    }

    private void updateDescription() {
        if (this.mSidecar.getEnrollmentSteps() == -1) {
            setHeaderText(2131624664);
            this.mStartMessage.setVisibility(0);
            this.mRepeatMessage.setVisibility(4);
            return;
        }
        setHeaderText(2131624666, true);
        this.mStartMessage.setVisibility(4);
        this.mRepeatMessage.setVisibility(0);
    }

    public void onEnrollmentHelp(CharSequence helpString) {
        this.mErrorText.setText(helpString);
    }

    public void onEnrollmentError(int errMsgId, CharSequence errString) {
        int msgId;
        switch (errMsgId) {
            case 3:
                msgId = 2131624679;
                break;
            default:
                msgId = 2131624680;
                break;
        }
        showErrorDialog(getText(msgId), errMsgId);
        stopIconAnimation();
        this.mErrorText.removeCallbacks(this.mTouchAgainRunnable);
    }

    public void onEnrollmentProgressChange(int steps, int remaining) {
        updateProgress(true);
        updateDescription();
        clearError();
        animateFlash();
        this.mErrorText.removeCallbacks(this.mTouchAgainRunnable);
        this.mErrorText.postDelayed(this.mTouchAgainRunnable, 2500);
    }

    private void updateProgress(boolean animate) {
        int progress = getProgress(this.mSidecar.getEnrollmentSteps(), this.mSidecar.getEnrollmentRemaining());
        if (animate) {
            animateProgress(progress);
        } else {
            this.mProgressBar.setProgress(progress);
        }
    }

    private int getProgress(int steps, int remaining) {
        if (steps == -1) {
            return 0;
        }
        return (Math.max(0, (steps + 1) - remaining) * 10000) / (steps + 1);
    }

    private void showErrorDialog(CharSequence msg, int msgId) {
        ErrorDialog.newInstance(msg, msgId).show(getFragmentManager(), ErrorDialog.class.getName());
    }

    private void showIconTouchDialog() {
        this.mIconTouchCount = 0;
        new IconTouchDialog().show(getFragmentManager(), null);
    }

    private void showError(CharSequence error) {
        this.mErrorText.setText(error);
        if (this.mErrorText.getVisibility() == 4) {
            this.mErrorText.setVisibility(0);
            this.mErrorText.setTranslationY((float) getResources().getDimensionPixelSize(2131558697));
            this.mErrorText.setAlpha(0.0f);
            this.mErrorText.animate().alpha(1.0f).translationY(0.0f).setDuration(200).setInterpolator(this.mLinearOutSlowInInterpolator).start();
            return;
        }
        this.mErrorText.animate().cancel();
        this.mErrorText.setAlpha(1.0f);
        this.mErrorText.setTranslationY(0.0f);
    }

    private void clearError() {
        if (this.mErrorText.getVisibility() == 0) {
            this.mErrorText.animate().alpha(0.0f).translationY((float) getResources().getDimensionPixelSize(2131558698)).setDuration(100).setInterpolator(this.mFastOutLinearInInterpolator).withEndAction(new Runnable() {
                public void run() {
                    FingerprintEnrollEnrolling.this.mErrorText.setVisibility(4);
                }
            }).start();
        }
    }

    protected int getMetricsCategory() {
        return 240;
    }
}
