package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;

public class PipControlButtonView extends RelativeLayout {
    private Animator mButtonFocusGainAnimator;
    private Animator mButtonFocusLossAnimator;
    ImageView mButtonImageView;
    private TextView mDescriptionTextView;
    private OnFocusChangeListener mFocusChangeListener;
    private ImageView mIconImageView;
    private final OnFocusChangeListener mInternalFocusChangeListener;
    private Animator mTextFocusGainAnimator;
    private Animator mTextFocusLossAnimator;

    public PipControlButtonView(Context context) {
        this(context, null, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mInternalFocusChangeListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    PipControlButtonView.this.startFocusGainAnimation();
                } else {
                    PipControlButtonView.this.startFocusLossAnimation();
                }
                if (PipControlButtonView.this.mFocusChangeListener != null) {
                    PipControlButtonView.this.mFocusChangeListener.onFocusChange(PipControlButtonView.this, hasFocus);
                }
            }
        };
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.tv_pip_control_button, this);
        this.mIconImageView = (ImageView) findViewById(R.id.icon);
        this.mButtonImageView = (ImageView) findViewById(R.id.button);
        this.mDescriptionTextView = (TextView) findViewById(R.id.desc);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{16843033, 16843087}, defStyleAttr, defStyleRes);
        setImageResource(typedArray.getResourceId(0, 0));
        setText(typedArray.getResourceId(1, 0));
        typedArray.recycle();
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mButtonImageView.setOnFocusChangeListener(this.mInternalFocusChangeListener);
        this.mTextFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_gain_animation);
        this.mTextFocusGainAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_gain_animation);
        this.mButtonFocusGainAnimator.setTarget(this.mButtonImageView);
        this.mTextFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_loss_animation);
        this.mTextFocusLossAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_loss_animation);
        this.mButtonFocusLossAnimator.setTarget(this.mButtonImageView);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mButtonImageView.setOnClickListener(listener);
    }

    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        this.mFocusChangeListener = listener;
    }

    public void setImageResource(int resId) {
        this.mIconImageView.setImageResource(resId);
    }

    public void setText(int resId) {
        this.mButtonImageView.setContentDescription(getContext().getString(resId));
        this.mDescriptionTextView.setText(resId);
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    public void startFocusGainAnimation() {
        cancelAnimator(this.mButtonFocusLossAnimator);
        cancelAnimator(this.mTextFocusLossAnimator);
        this.mTextFocusGainAnimator.start();
        if (this.mButtonImageView.getAlpha() < 1.0f) {
            this.mButtonFocusGainAnimator.start();
        }
    }

    public void startFocusLossAnimation() {
        cancelAnimator(this.mButtonFocusGainAnimator);
        cancelAnimator(this.mTextFocusGainAnimator);
        this.mTextFocusLossAnimator.start();
        if (this.mButtonImageView.hasFocus()) {
            this.mButtonFocusLossAnimator.start();
        }
    }

    public void reset() {
        float f = 1.0f;
        cancelAnimator(this.mButtonFocusGainAnimator);
        cancelAnimator(this.mTextFocusGainAnimator);
        cancelAnimator(this.mButtonFocusLossAnimator);
        cancelAnimator(this.mTextFocusLossAnimator);
        this.mButtonImageView.setAlpha(1.0f);
        TextView textView = this.mDescriptionTextView;
        if (!this.mButtonImageView.hasFocus()) {
            f = 0.0f;
        }
        textView.setAlpha(f);
    }
}
