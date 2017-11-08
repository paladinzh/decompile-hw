package com.android.contacts.dialpad.gravity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class SingleHandAdapter implements AnimatorListener {
    private int mCurrentMode = -1;
    private GravityView mKeyNumPadView;
    private View mLeftModeButton;
    private AnimatorListener mListener;
    private FrameLayout mParentView;
    private View mRightModeButton;
    private boolean mUpdateParent;
    private ObjectAnimator pin_anim;

    public interface AnimatorListener {
        void onAnimatorEnd(ObjectAnimator objectAnimator);

        void onAnimatorStart(ObjectAnimator objectAnimator);
    }

    public SingleHandAdapter(Context context) {
    }

    public void initAnimatorSet(float w, float h, float transX) {
        if (this.mLeftModeButton == null || this.mRightModeButton == null || this.mKeyNumPadView == null) {
            HwLog.w("singlegravity", "initAnimatorSet mLeftModeButton = " + this.mLeftModeButton + ", " + this.mRightModeButton + ", " + this.mKeyNumPadView);
            return;
        }
        if (this.mParentView != null) {
            this.mKeyNumPadView.setParentView(this.mParentView);
        }
        int lHeight = this.mKeyNumPadView.getLayoutParams().height;
        if (lHeight < 0) {
            lHeight = this.mKeyNumPadView.getHeight();
        }
        if (HwLog.HWDBG) {
            HwLog.d("singlegravity", "initAnimatorSet lHeight:" + lHeight + " toW:" + w + " toH:" + h + " transX:" + transX);
            HwLog.d("singlegravity", "initAnimatorSet lwidth:" + this.mKeyNumPadView.getWidth());
            HwLog.d("singlegravity", "initAnimatorSet params width:" + this.mKeyNumPadView.getLayoutParams().width);
        }
        this.pin_anim = this.mKeyNumPadView.getViewAnimator((float) this.mKeyNumPadView.getWidth(), w, (float) lHeight, h, this.mKeyNumPadView.getTranslationX(), transX, this.mUpdateParent);
        this.mKeyNumPadView.setAnimatorListener(this);
    }

    public void setAnimatedViews(View lView, View rView, GravityView keyNumView, FrameLayout parent) {
        this.mLeftModeButton = lView;
        this.mRightModeButton = rView;
        this.mKeyNumPadView = keyNumView;
        this.mParentView = parent;
    }

    public void startAnimatorUpdateView(boolean parentUpdate, int curMode, boolean playAnimation) {
        this.mUpdateParent = parentUpdate;
        if (this.pin_anim != null) {
            if (HwLog.HWDBG) {
                HwLog.d("singlegravity", "startAnimatorUpdateView playAnimation:" + playAnimation);
            }
            this.mCurrentMode = curMode;
            if (playAnimation) {
                this.pin_anim.setDuration(360);
                this.pin_anim.start();
                if (this.mListener != null) {
                    this.mListener.onAnimatorStart(this.pin_anim);
                }
            } else {
                processFinalLayout();
            }
        }
    }

    private void processFinalLayout() {
        if (this.mKeyNumPadView != null) {
            if (this.mListener != null) {
                this.mListener.onAnimatorStart(null);
            }
            if (this.mCurrentMode == 0) {
                if (this.mRightModeButton != null && this.mRightModeButton.getVisibility() == 0) {
                    this.mRightModeButton.setVisibility(8);
                }
                if (this.mLeftModeButton != null && this.mLeftModeButton.getVisibility() == 0) {
                    this.mLeftModeButton.setVisibility(8);
                }
                this.mKeyNumPadView.setTranslationX(0.0f);
            } else if (this.mCurrentMode == 1) {
                this.mKeyNumPadView.setTranslationX(0.0f);
            } else if (this.mCurrentMode == 2) {
                this.mKeyNumPadView.setTranslationX(getTranslationX());
            }
            this.mKeyNumPadView.processEndLayout(this.mUpdateParent, null);
            if (this.mListener != null) {
                this.mListener.onAnimatorEnd(null);
            }
        }
    }

    public void onAnimatorEnd(ObjectAnimator animator) {
        if (HwLog.HWDBG) {
            HwLog.d("singlegravity", "startAnimatorUpdateView onAnimatorEnd");
        }
        if (this.mKeyNumPadView != null) {
            if (this.mCurrentMode == 0) {
                if (animator == null || this.pin_anim.equals(animator)) {
                    if (this.mRightModeButton != null && this.mRightModeButton.getVisibility() == 0) {
                        this.mRightModeButton.setVisibility(8);
                    }
                    if (this.mLeftModeButton != null && this.mLeftModeButton.getVisibility() == 0) {
                        this.mLeftModeButton.setVisibility(8);
                    }
                }
                this.mKeyNumPadView.setTranslationX(0.0f);
            } else if (this.mCurrentMode == 1) {
                this.mKeyNumPadView.setTranslationX(0.0f);
            } else if (this.mCurrentMode == 2) {
                this.mKeyNumPadView.setTranslationX(getTranslationX());
            }
        }
        if (this.mListener != null) {
            this.mListener.onAnimatorEnd(animator);
        }
    }

    public void onAnimatorStart(ObjectAnimator animator) {
        if (HwLog.HWDBG) {
            HwLog.d("singlegravity", "startAnimatorUpdateView onAnimatorStart");
        }
    }

    public void setAnimatorListener(AnimatorListener listener) {
        this.mListener = listener;
    }

    private float getTranslationX() {
        if (this.mKeyNumPadView == null) {
            return 0.0f;
        }
        if (ContactDpiAdapter.NOT_SRC_DPI) {
            return (float) Math.floor((((double) this.mKeyNumPadView.getResources().getDimensionPixelSize(R.dimen.contact_dialpad_single_hand_width)) * ((double) ContactDpiAdapter.SRC_DPI)) / ((double) ContactDpiAdapter.REAL_Dpi));
        }
        return this.mKeyNumPadView.getResources().getDimension(R.dimen.contact_dialpad_single_hand_width);
    }
}
