package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.deskclock.R;
import com.android.util.TypeFaces;
import com.android.util.Utils;
import java.util.Locale;

public class TimerPanel extends FrameLayout {
    private long currentTime;
    private Context mContext;
    private TextView mTimerPanelTime;
    private OnTimerCountListener onTimeCountListener;
    private long originPickedTime;
    private float sweepAngle;
    private ImageView timerDial;
    private ProgressImageView timerUpperDial;
    private int totalSeconds;

    public interface OnTimerCountListener {
        void onTimeOut();

        void onTimerPause(int i);
    }

    public TimerPanel(Context context) {
        this(context, null);
    }

    public TimerPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    private void init() {
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.view_timer_panel, this, true);
        this.mTimerPanelTime = (TextView) findViewById(R.id.timer_panel_time);
        this.timerUpperDial = (ProgressImageView) findViewById(R.id.clock_timer_upperdial);
        this.timerDial = (ImageView) findViewById(R.id.clock_timer_dial);
        Typeface tf = TypeFaces.get(this.mContext, "sans-serif-thin");
        if (tf != null) {
            this.mTimerPanelTime.setTypeface(tf);
        }
    }

    public void setTimerDialVisibility(boolean show) {
        if (show) {
            this.timerDial.setVisibility(0);
            this.timerUpperDial.setVisibility(0);
            return;
        }
        this.timerDial.setVisibility(4);
        this.timerUpperDial.setVisibility(4);
    }

    public long getCurrentTime() {
        return (long) this.totalSeconds;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
        this.totalSeconds = currentTime == 0 ? 0 : (int) Math.ceil((double) (((float) currentTime) / 1000.0f));
        updateProgress();
        setTime(formatTime(this.totalSeconds));
    }

    public void setOriginPickedTime(long originPickedTime) {
        this.originPickedTime = originPickedTime;
    }

    public void setTimerPanelTime(long totalMiliSeconds) {
        this.totalSeconds = (int) (totalMiliSeconds / 1000);
        setTime(formatTime(this.totalSeconds));
    }

    private void setTime(String time) {
        this.mTimerPanelTime.setText(time);
    }

    private String formatTime(int seconds) {
        int second;
        if (seconds <= 0) {
            seconds = 0;
            this.onTimeCountListener.onTimeOut();
        }
        int hour = 0;
        int minute = 0;
        int totalMinutes = seconds / 60;
        if (totalMinutes == 0) {
            second = seconds;
        } else if (totalMinutes <= 0 || totalMinutes >= 60) {
            hour = totalMinutes / 60;
            minute = totalMinutes - (hour * 60);
            second = seconds - (totalMinutes * 60);
        } else {
            minute = totalMinutes;
            second = seconds % 60;
        }
        String[] timePoint = Utils.getTimePoint(this.mContext);
        return String.format(Locale.getDefault(), "%02d" + timePoint[0] + "%02d" + timePoint[1] + "%02d", new Object[]{Integer.valueOf(hour), Integer.valueOf(minute), Integer.valueOf(second)});
    }

    public void setOnTimerCountListener(OnTimerCountListener onTimeCountListener) {
        this.onTimeCountListener = onTimeCountListener;
    }

    public void stopCountDown() {
        if (this.onTimeCountListener != null) {
            this.onTimeCountListener.onTimerPause(this.totalSeconds);
        }
    }

    public ImageView getDial() {
        return this.timerDial;
    }

    public View getUpperDial() {
        return this.timerUpperDial;
    }

    public TextView getTimerPanelTime() {
        return this.mTimerPanelTime;
    }

    private void updateProgress() {
        this.sweepAngle = 360.0f * (1.0f - ((((float) this.currentTime) * 1.0f) / ((float) this.originPickedTime)));
        this.timerUpperDial.setAngle(this.sweepAngle);
        this.timerUpperDial.invalidate();
    }

    public void recoverProgress() {
        this.timerUpperDial.setAngle((1.0f - ((((float) this.currentTime) * 1.0f) / ((float) this.originPickedTime))) * 360.0f);
        this.timerUpperDial.invalidate();
    }

    public void recoverStopProgress() {
        this.timerUpperDial.setAngle(360.0f);
        this.timerUpperDial.invalidate();
    }

    public void resetTime() {
        this.originPickedTime = 0;
        this.totalSeconds = 0;
        this.timerUpperDial.reset();
    }

    public void setViewVisible() {
        this.timerDial.setVisibility(0);
        this.timerUpperDial.setVisibility(0);
    }
}
