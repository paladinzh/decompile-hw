package com.android.settings.sdencryption.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import java.text.NumberFormat;

public class RollingView extends TextView {
    private int mCurrentNumber = 0;
    public boolean mIncludePercent = false;
    private OnNumberChangedListener mOnNumberChangedListener;
    private AnimaRoller mRoller;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            if (RollingView.this.mRoller != null) {
                boolean more = RollingView.this.mRoller.computeRoll();
                float value = RollingView.this.mRoller.value();
                if (RollingView.this.mIncludePercent) {
                    RollingView.this.updateNumber((int) value, true);
                } else {
                    RollingView.this.updateNumber((int) value);
                }
                if (more) {
                    RollingView.this.post(RollingView.this.mRunnable);
                }
            }
        }
    };

    public interface OnNumberChangedListener {
        void onNumberChanged(int i);
    }

    public RollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNumberImmediately(int target) {
        removeCallbacks(this.mRunnable);
        updateNumber(target);
    }

    public void setNumberByDuration(int target, int duration) {
        if (duration <= 0) {
            setNumberImmediately(target);
            return;
        }
        removeCallbacks(this.mRunnable);
        if (this.mRoller == null) {
            this.mRoller = new AnimaRoller();
            this.mRoller.startRoll((float) this.mCurrentNumber, (float) target, duration);
        } else {
            this.mRoller.continueRoll((float) target, duration);
        }
        post(this.mRunnable);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mRunnable);
    }

    private final void updateNumber(int number, boolean type) {
        this.mCurrentNumber = number;
        if (type) {
            setText(getNumber(number));
        } else {
            setText(number);
        }
        if (this.mOnNumberChangedListener != null) {
            this.mOnNumberChangedListener.onNumberChanged(this.mCurrentNumber);
        }
    }

    private final void updateNumber(int number) {
        this.mCurrentNumber = number;
        setText(getNumber(number));
        if (this.mOnNumberChangedListener != null) {
            this.mOnNumberChangedListener.onNumberChanged(this.mCurrentNumber);
        }
    }

    private String getNumber(int number) {
        if (!this.mIncludePercent) {
            return NumberFormat.getInstance().format((long) number);
        }
        NumberFormat pnf = NumberFormat.getPercentInstance();
        double pValue = ((double) number) / 100.0d;
        pnf.setMinimumFractionDigits(0);
        return pnf.format(pValue);
    }
}
