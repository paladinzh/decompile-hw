package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.anima.AnimaRoller;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationPercent;
import java.text.NumberFormat;

public class PowerRollingView extends TextView {
    public static final String TAG = "PowerRollingView";
    private int itemNumResId = R.plurals.power_optimize_others_item_num;
    private int mCurrentNumber = 0;
    private OnNumberChangedListener mOnNumberChangedListener;
    private AnimaRoller mRoller;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            if (PowerRollingView.this.mRoller == null) {
                HwLog.e(PowerRollingView.TAG, "mRunnable mRoller is null!");
                return;
            }
            boolean more = PowerRollingView.this.mRoller.computeRoll();
            PowerRollingView.this.updateNumber((int) PowerRollingView.this.mRoller.value());
            if (more) {
                PowerRollingView.this.post(PowerRollingView.this.mRunnable);
            }
        }
    };

    public interface OnNumberChangedListener {
        void onNumberChanged(int i);
    }

    public PowerRollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PowerRollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNumberImmediately(int target) {
        removeCallbacks(this.mRunnable);
        updateNumber(target);
    }

    public void setNumberByDuration(int target, int duration) {
        if (duration <= 0) {
            HwLog.w(TAG, "setNumberByDuration, duration is nagative:" + duration);
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

    public int getCurrentNumber() {
        return this.mCurrentNumber;
    }

    private final void updateNumber(int number) {
        this.mCurrentNumber = number;
        setText(getNumber(number));
        if (this.mOnNumberChangedListener != null) {
            this.mOnNumberChangedListener.onNumberChanged(this.mCurrentNumber);
        }
    }

    private String getNumber(int number) {
        if (getPowerRollType() != 0) {
            return String.format(GlobalContext.getContext().getResources().getQuantityString(this.itemNumResId, number, new Object[]{Integer.valueOf(number)}), new Object[0]);
        } else if (GlobalContext.getContext().getResources().getBoolean(R.bool.spaceclean_percent_small_mode)) {
            return NumberFormat.getInstance().format((long) number);
        } else {
            return NumberLocationPercent.getPercentage((double) number, 0);
        }
    }

    public void setItemNumResId(int resId) {
        this.itemNumResId = resId;
    }

    public int getPowerRollType() {
        return 0;
    }

    public void setOnNumberChangedListener(OnNumberChangedListener l) {
        this.mOnNumberChangedListener = l;
    }
}
