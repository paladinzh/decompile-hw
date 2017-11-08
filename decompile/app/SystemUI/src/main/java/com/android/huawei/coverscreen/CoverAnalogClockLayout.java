package com.android.huawei.coverscreen;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
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
import fyusion.vislib.BuildConfig;
import java.util.Calendar;

public class CoverAnalogClockLayout extends RelativeLayout implements TimeChangeObserver {
    private static final String WINDOWS_SIZE = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    public CoverAnalogClock mAnalogClock;
    private TextView mDateTimeTextView;
    private final Handler mHandler;
    private ImageView mImageView;
    private View mMisscallmms;
    private View mSteps;
    private View mWeather;

    public CoverAnalogClockLayout(Context context) {
        this(context, null);
    }

    public CoverAnalogClockLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverAnalogClockLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        CoverAnalogClockLayout.this.updateTextView();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mAnalogClock = new CoverAnalogClock(getContext());
        this.mAnalogClock.setId(10000);
        this.mDateTimeTextView = new TextView(getContext());
        LayoutParams clockLayoutParams = new LayoutParams(500, -2);
        clockLayoutParams.addRule(13);
        this.mAnalogClock.setLayoutParams(clockLayoutParams);
        addView(this.mAnalogClock);
        LayoutParams textViewLayoutParams = new LayoutParams(-2, -2);
        textViewLayoutParams.addRule(14);
        textViewLayoutParams.topMargin = 900;
        this.mDateTimeTextView.setLayoutParams(textViewLayoutParams);
        this.mDateTimeTextView.setTextSize(1, 14.0f);
        this.mDateTimeTextView.setTextColor(Color.parseColor("#ffdcdcdc"));
        addView(this.mDateTimeTextView);
        this.mImageView = new ImageView(getContext());
        LayoutParams imageViewParams = new LayoutParams(-1, -1);
        imageViewParams.addRule(13);
        this.mImageView.setLayoutParams(imageViewParams);
        this.mImageView.setImageResource(R$drawable.slide_guide_mask);
        this.mImageView.setScaleType(ScaleType.FIT_XY);
        addView(this.mImageView);
        onTimeChange();
    }

    public void initClock(int bgDrawableId, int[] fgDrawableIdArray) {
        this.mAnalogClock.init(bgDrawableId, fgDrawableIdArray);
        this.mImageView.setVisibility(4);
        this.mMisscallmms = findViewById(R$id.miss_call_mms);
        this.mWeather = findViewById(R$id.weather_view);
        this.mSteps = findViewById(R$id.cover_steps);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void onTimeChange() {
        this.mHandler.sendEmptyMessage(1);
    }

    private void updateTextView() {
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
                    CoverAnalogClockLayout.this.mMisscallmms.setVisibility(4);
                    CoverAnalogClockLayout.this.mWeather.setVisibility(4);
                    CoverAnalogClockLayout.this.mSteps.setVisibility(4);
                    CoverAnalogClockLayout.this.mDateTimeTextView.setVisibility(4);
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
                    CoverAnalogClockLayout.this.mMisscallmms.setVisibility(0);
                    CoverAnalogClockLayout.this.mWeather.setVisibility(0);
                    CoverAnalogClockLayout.this.mSteps.setVisibility(0);
                    CoverAnalogClockLayout.this.mDateTimeTextView.setVisibility(0);
                }

                public void onAnimationCancel(Animator arg0) {
                }
            });
        }
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.setDuration(370);
        alphaAnimator.start();
    }
}
