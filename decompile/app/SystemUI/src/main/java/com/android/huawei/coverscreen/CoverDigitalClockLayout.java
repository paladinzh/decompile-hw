package com.android.huawei.coverscreen;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import java.util.Calendar;

public class CoverDigitalClockLayout extends RelativeLayout implements TimeChangeObserver {
    private TextView mAmPmTextView;
    private TextView mDateTimeTextView;
    private final Handler mHandler;
    private TextView mHourTimeTextView;
    private ImageView mImageView;
    private TextView mMinutesTimeTextView;
    private View mMisscallmms;
    private View mSteps;
    private View mWeather;

    public CoverDigitalClockLayout(Context context) {
        this(context, null);
    }

    public CoverDigitalClockLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverDigitalClockLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        CoverDigitalClockLayout.this.updateTextView();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAmPmTextView = (TextView) findViewById(R$id.cover_am_pm_textview);
        this.mAmPmTextView.setLayoutParams((LayoutParams) this.mAmPmTextView.getLayoutParams());
        this.mHourTimeTextView = (TextView) findViewById(R$id.cover_hours_time_textview);
        this.mMinutesTimeTextView = (TextView) findViewById(R$id.cover_minutes_time_textview);
        this.mDateTimeTextView = (TextView) findViewById(R$id.cover_date_time_textview);
        this.mMisscallmms = findViewById(R$id.miss_call_mms);
        this.mWeather = findViewById(R$id.weather_view);
        this.mSteps = findViewById(R$id.cover_steps);
        this.mImageView = new ImageView(getContext());
        LayoutParams imageViewParams = new LayoutParams(-1, -1);
        imageViewParams.addRule(13);
        this.mImageView.setLayoutParams(imageViewParams);
        this.mImageView.setImageResource(R$drawable.slide_guide_mask);
        this.mImageView.setScaleType(ScaleType.FIT_XY);
        this.mImageView.setVisibility(4);
        addView(this.mImageView);
        onTimeChange();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public TextView getAmPmTextView() {
        return this.mAmPmTextView;
    }

    public TextView getmHourTimeTextView() {
        return this.mHourTimeTextView;
    }

    public TextView getmMinutesTimeTextView() {
        return this.mMinutesTimeTextView;
    }

    public void onTimeChange() {
        this.mHandler.sendEmptyMessage(1);
    }

    private void updateTextView() {
        this.mAmPmTextView.setText(getText(1));
        this.mHourTimeTextView.setText(getText(4));
        this.mMinutesTimeTextView.setText(getText(5));
        this.mDateTimeTextView.setText(getText(9));
    }

    private String getText(int format) {
        return new FormatTime(getContext(), Calendar.getInstance()).getTimeString(format);
    }

    public void hideDateTextView(boolean isHide) {
        ValueAnimator alphaAnimator = ObjectAnimator.ofFloat(this.mImageView, "alpha", new float[]{0.0f, 1.0f});
        if (isHide) {
            alphaAnimator.removeAllListeners();
            alphaAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator arg0) {
                    CoverDigitalClockLayout.this.mMisscallmms.setVisibility(4);
                    CoverDigitalClockLayout.this.mWeather.setVisibility(4);
                    CoverDigitalClockLayout.this.mSteps.setVisibility(4);
                    CoverDigitalClockLayout.this.mDateTimeTextView.setVisibility(4);
                }

                public void onAnimationRepeat(Animator arg0) {
                }

                public void onAnimationEnd(Animator arg0) {
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
        } else {
            alphaAnimator.removeAllListeners();
            alphaAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator arg0) {
                }

                public void onAnimationRepeat(Animator arg0) {
                }

                public void onAnimationEnd(Animator arg0) {
                    CoverDigitalClockLayout.this.mMisscallmms.setVisibility(0);
                    CoverDigitalClockLayout.this.mWeather.setVisibility(0);
                    CoverDigitalClockLayout.this.mSteps.setVisibility(0);
                    CoverDigitalClockLayout.this.mDateTimeTextView.setVisibility(0);
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
        }
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.setDuration(400);
        alphaAnimator.start();
    }
}
