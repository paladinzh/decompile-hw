package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.comm.misc.Utility;

@Deprecated
public class NumberRollingView extends SimpleTextView {
    private static final int PROGRESS_MAX = 100;
    private static final long QUICK_DURING_TIME = 200;
    private static final long SPEED_PER_NUM = 100;
    public static final String TAG = NumberRollingView.class.getSimpleName();
    private RollingCommand mCounterRunable = new MyRollingCommand(this.mHandler, "NumberRollingView");
    private int mCurrentNumber = 0;
    private int mCurrentNumberLenth = 1;
    private Handler mHandler = new Handler();
    private OnNumberChangedListener mOnNumberChangedListener;
    private Interpolator mTimerInterpolator = new AccelerateInterpolator();

    private class MyRollingCommand extends RollingCommand {
        public MyRollingCommand(Handler handler, String type) {
            super(handler, type);
        }

        protected void onNumberUpate(int count) {
            NumberRollingView.this.updateCurrentNumber(count);
        }
    }

    public interface OnNumberChangedListener {
        void onNumberChanged(int i);
    }

    public NumberRollingView(Context context) {
        super(context);
    }

    public NumberRollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText(String.valueOf(this.mCurrentNumber));
    }

    protected final void updateCurrentNumber(int number) {
        if (this.mCurrentNumber != number) {
            this.mCurrentNumber = number;
            this.mText = String.valueOf(this.mCurrentNumber);
            int lenth = getNumberLenth(number);
            if (this.mCurrentNumberLenth != lenth) {
                this.mCurrentNumberLenth = lenth;
                resetBounds();
                requestLayout();
            }
            invalidate();
            if (this.mOnNumberChangedListener != null) {
                this.mOnNumberChangedListener.onNumberChanged(this.mCurrentNumber);
            }
        }
    }

    public void setNumberImmediately(int target) {
        removeCallbacks(this.mCounterRunable);
        updateCurrentNumber(target);
    }

    public void stopRefresh() {
        this.mCounterRunable.stop();
    }

    public void setNumber(int target, boolean quick) {
        setNumberByDuration(target, quick ? 200 : getDuration(target));
    }

    public void setNumberByProgress(int target, int progress) {
        setNumberByProgress(target, progress, getDuration(target));
    }

    public void setNumberByProgress(int progress) {
        setNumberByProgress(progress, progress, getDuration(progress));
    }

    public void setNumberByProgress(int target, int progress, long duration) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("progress only between (0,100)");
        }
        setNumberByDuration(target, (long) ((Utility.ALPHA_MAX - this.mTimerInterpolator.getInterpolation(((float) progress) / 100.0f)) * ((float) duration)));
    }

    public void setNumberByDuration(int target, long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Could not set duration negative number!");
        }
        this.mCounterRunable.setNewTarget(this.mCurrentNumber, target, duration);
    }

    public int getCurrentNumber() {
        return this.mCurrentNumber;
    }

    protected void onResetBounds(Rect r) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.mCurrentNumberLenth; i++) {
            builder.append("4");
        }
        getTextPaint().getTextBounds(builder.toString(), 0, builder.length(), r);
    }

    private long getDuration(int target) {
        return ((long) Math.abs(this.mCurrentNumber - target)) * SPEED_PER_NUM;
    }

    public static int getNumberLenth(int number) {
        return String.valueOf(number).length();
    }

    public void setOnNumberChangedListener(OnNumberChangedListener l) {
        this.mOnNumberChangedListener = l;
    }
}
