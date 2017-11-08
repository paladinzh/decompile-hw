package com.android.deskclock.timer;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.deskclock.timer.TimerPanel.OnTimerCountListener;
import com.android.deskclock.timer.WhellPicker.OnWhellPickerValueChangedListener;
import com.android.util.Utils;
import java.util.Locale;

public class TimerPicker extends FrameLayout implements OnWhellPickerValueChangedListener, OnTimerCountListener {
    private AnimationSet hideAnimationSet;
    private boolean isInited;
    private PickedTime mPickedTime;
    private AlphaAnimation mPickedTimeHideAnimation;
    private TimerPanel mTimerPanel;
    private RelativeLayout mTimerUnit;
    private AlphaAnimation mTimerUnitHideAnimation;
    private WhellPicker mWhellPicker;
    private AlphaAnimation mWhellPickerHideAnimation;
    private OnTimerListener onTimerListener;
    private AlphaAnimation showAnimation;
    private ScaleAnimation shrinkAnimation;
    private ImageView timerDial;
    private AlphaAnimation timerDialAlphaAnimation;
    private AnimationSet timerDialAnimationSet;
    private ScaleAnimation timerDialScaleAnimation;
    private TextView timerPanelTime;
    private AlphaAnimation timerPanelTimeAlphaAnimation;
    private AnimationSet timerPanelTimeAnimationSet;
    private ScaleAnimation timerPanelTimeScaleAnimation;
    private View timerUpperDial;
    private AlphaAnimation timerUpperDialAlphaAnimation;
    private AnimationSet timerUpperDialAnimationSet;
    private ScaleAnimation timerUpperDialScaleAnimation;

    public interface OnTimerListener {
        void onPause(int i);

        void onTimeOut();

        void onTimerPickScroll(int i);
    }

    public TimerPicker(Context context) {
        this(context, null);
    }

    public TimerPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.timerPanelTimeAnimationSet = new AnimationSet(false);
        this.hideAnimationSet = new AnimationSet(false);
        this.timerDialAnimationSet = new AnimationSet(false);
        this.timerUpperDialAnimationSet = new AnimationSet(false);
        init(context);
    }

    private void init(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.view_timer_picker, this, true);
        this.mWhellPicker = (WhellPicker) findViewById(R.id.timer_whell_picker);
        this.mPickedTime = (PickedTime) findViewById(R.id.timer_picked_time);
        this.mTimerPanel = (TimerPanel) findViewById(R.id.timer_panel);
        this.mTimerUnit = (RelativeLayout) findViewById(R.id.timer_unit);
        this.mTimerPanel.setTimerDialVisibility(false);
        this.mWhellPicker.setOnWhellPickerValueChangedListener(this);
        this.mTimerPanel.setOnTimerCountListener(this);
    }

    public void onHourChanged(int mHour) {
        this.mPickedTime.setHourValue(mHour);
        if (this.onTimerListener != null) {
            this.onTimerListener.onTimerPickScroll(this.mWhellPicker.getTimeSecond());
            sendAccessibilityEvent(32768);
        }
    }

    public void onMinuteChanged(int mMinute) {
        this.mPickedTime.setMinuteValue(mMinute);
        if (this.onTimerListener != null) {
            this.onTimerListener.onTimerPickScroll(this.mWhellPicker.getTimeSecond());
            sendAccessibilityEvent(32768);
        }
    }

    public void onSecondChanged(int mSecond) {
        this.mPickedTime.setSecondValue(mSecond);
        if (this.onTimerListener != null) {
            this.onTimerListener.onTimerPickScroll(this.mWhellPicker.getTimeSecond());
            sendAccessibilityEvent(32768);
        }
    }

    public long getTotalTime() {
        return ((long) this.mWhellPicker.getTimeSecond()) * 1000;
    }

    public long getCurrentTime() {
        return this.mTimerPanel.getCurrentTime();
    }

    public void resetTime() {
        this.mWhellPicker.resetTimeNumber(0, 0, 0);
        this.mTimerPanel.resetTime();
    }

    public void recoverPickedTime(String previousTime) {
        String[] pickedTime = previousTime.split("/");
        this.mWhellPicker.resetTimeNumber(Integer.parseInt(pickedTime[0]), Integer.parseInt(pickedTime[1]), Integer.parseInt(pickedTime[2]));
    }

    public String getPickedTime() {
        return this.mWhellPicker.getHour() + "/" + this.mWhellPicker.getMinute() + "/" + this.mWhellPicker.getSecond();
    }

    private void setTimerPanelTime() {
        this.mTimerPanel.setTimerPanelTime(getTotalTime());
        this.mTimerPanel.setOriginPickedTime(getTotalTime());
    }

    public void performStartAction() {
        this.mTimerPanel.setVisibility(0);
        setTimerPanelTime();
        playStartAnimation();
    }

    public String formatTime(int seconds) {
        if (seconds <= 0) {
            seconds = 0;
        }
        int minute = (seconds / 60) - ((seconds / 3600) * 60);
        int second = seconds % 60;
        String[] timePoint = Utils.getTimePoint(getContext());
        return String.format(Locale.getDefault(), "%02d" + timePoint[0] + "%02d" + timePoint[1] + "%02d", new Object[]{Integer.valueOf(hour), Integer.valueOf(minute), Integer.valueOf(second)});
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        String text = formatTime((int) (getTotalTime() / 1000));
        if (TextUtils.isEmpty(text)) {
            return super.dispatchPopulateAccessibilityEvent(event);
        }
        event.getText().add(text);
        return true;
    }

    public void performPauseAction() {
        this.mTimerPanel.setVisibility(0);
        this.mTimerPanel.stopCountDown();
    }

    public void performResetAction() {
        this.mTimerPanel.setVisibility(8);
        resetTime();
        playResetAnimation();
    }

    public void recoverData(long originPickedTime, long currentTime) {
        this.mTimerPanel.setOriginPickedTime(originPickedTime);
        this.mTimerPanel.setCurrentTime(currentTime);
    }

    public void performSwitchPauseAciton() {
        this.mWhellPicker.setVisibility(4);
        this.mPickedTime.setVisibility(4);
        this.mTimerUnit.setVisibility(4);
        this.mTimerPanel.setViewVisible();
        this.mTimerPanel.recoverProgress();
        this.mTimerPanel.setVisibility(0);
    }

    public void performSwitchRunningAction() {
        this.mWhellPicker.setVisibility(4);
        this.mPickedTime.setVisibility(4);
        this.mTimerUnit.setVisibility(4);
        this.mTimerPanel.setViewVisible();
        this.mTimerPanel.setVisibility(0);
    }

    public void performSwitchStopAction() {
        this.mWhellPicker.setVisibility(4);
        this.mPickedTime.setVisibility(4);
        this.mTimerUnit.setVisibility(4);
        this.mTimerPanel.setViewVisible();
        this.mTimerPanel.recoverStopProgress();
        this.mTimerPanel.setVisibility(0);
    }

    public void updateTimer(long leaveTime) {
        this.mTimerPanel.setCurrentTime(leaveTime);
    }

    private void playStartAnimation() {
        if (!this.isInited) {
            Interpolator interpolator33 = new PathInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
            Interpolator interpolator20 = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
            this.isInited = true;
            this.timerDial = this.mTimerPanel.getDial();
            this.timerUpperDial = this.mTimerPanel.getUpperDial();
            this.timerPanelTime = this.mTimerPanel.getTimerPanelTime();
            this.mWhellPickerHideAnimation = new AlphaAnimation(1.0f, 0.0f);
            this.mWhellPickerHideAnimation.setDuration(100);
            this.mWhellPickerHideAnimation.setInterpolator(interpolator33);
            this.mTimerUnitHideAnimation = new AlphaAnimation(1.0f, 0.0f);
            this.mTimerUnitHideAnimation.setDuration(100);
            this.mTimerUnitHideAnimation.setInterpolator(interpolator33);
            this.mPickedTimeHideAnimation = new AlphaAnimation(1.0f, 0.0f);
            this.mPickedTimeHideAnimation.setDuration(100);
            this.mPickedTimeHideAnimation.setInterpolator(interpolator33);
            this.timerDialAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.timerDialAlphaAnimation.setStartOffset(150);
            this.timerDialAlphaAnimation.setDuration(300);
            this.timerDialAlphaAnimation.setInterpolator(interpolator33);
            this.timerUpperDialAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.timerUpperDialAlphaAnimation.setStartOffset(150);
            this.timerUpperDialAlphaAnimation.setDuration(300);
            this.timerUpperDialAlphaAnimation.setInterpolator(interpolator33);
            this.timerDialScaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, 1, 0.5f, 1, 0.5f);
            this.timerDialScaleAnimation.setDuration(450);
            this.timerDialScaleAnimation.setInterpolator(interpolator20);
            this.timerUpperDialScaleAnimation = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, 1, 0.5f, 1, 0.5f);
            this.timerUpperDialScaleAnimation.setDuration(450);
            this.timerUpperDialScaleAnimation.setInterpolator(interpolator20);
            this.timerDialAnimationSet.addAnimation(this.timerDialAlphaAnimation);
            this.timerDialAnimationSet.addAnimation(this.timerDialScaleAnimation);
            this.timerUpperDialAnimationSet.addAnimation(this.timerUpperDialAlphaAnimation);
            this.timerUpperDialAnimationSet.addAnimation(this.timerUpperDialScaleAnimation);
            this.timerPanelTimeScaleAnimation = new ScaleAnimation(1.52f, 1.0f, 1.52f, 1.0f, 1, 0.5f, 1, 0.5f);
            this.timerPanelTimeScaleAnimation.setDuration(450);
            this.timerPanelTimeScaleAnimation.setInterpolator(interpolator20);
            this.timerPanelTimeAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.timerPanelTimeAlphaAnimation.setStartOffset(50);
            this.timerPanelTimeAlphaAnimation.setDuration(150);
            this.timerPanelTimeAlphaAnimation.setInterpolator(interpolator33);
            this.timerPanelTimeAnimationSet.addAnimation(this.timerPanelTimeAlphaAnimation);
            this.timerPanelTimeAnimationSet.addAnimation(this.timerPanelTimeScaleAnimation);
            this.showAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.showAnimation.setDuration(450);
            this.shrinkAnimation = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f, 1, 0.5f, 1, 0.5f);
            this.shrinkAnimation.setDuration(350);
            this.shrinkAnimation.setInterpolator(interpolator33);
            this.hideAnimationSet.addAnimation(this.shrinkAnimation);
            this.hideAnimationSet.addAnimation(this.mPickedTimeHideAnimation);
        }
        this.timerDial.setVisibility(4);
        this.timerUpperDial.setVisibility(4);
        this.mWhellPicker.startAnimation(this.mWhellPickerHideAnimation);
        this.mWhellPicker.setVisibility(4);
        this.mTimerUnit.startAnimation(this.mTimerUnitHideAnimation);
        this.mTimerUnit.setVisibility(4);
        this.timerDial.startAnimation(this.timerDialAnimationSet);
        this.timerUpperDial.startAnimation(this.timerUpperDialAnimationSet);
        this.timerPanelTime.startAnimation(this.timerPanelTimeAnimationSet);
        this.mTimerPanel.setTimerDialVisibility(true);
        this.mPickedTime.startAnimation(this.hideAnimationSet);
        this.mPickedTime.setVisibility(4);
    }

    private void playResetAnimation() {
        this.mTimerPanel.setTimerDialVisibility(false);
        this.mWhellPicker.setVisibility(0);
        this.mPickedTime.setVisibility(0);
        this.mTimerUnit.setVisibility(0);
    }

    public void onTimeOut() {
        if (this.onTimerListener != null) {
            this.onTimerListener.onTimeOut();
        }
    }

    public void onTimerPause(int currentTime) {
        if (this.onTimerListener != null) {
            this.onTimerListener.onPause(currentTime);
        }
    }

    public void setOnTimerListener(OnTimerListener onTimerListener) {
        this.onTimerListener = onTimerListener;
    }
}
