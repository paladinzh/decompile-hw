package com.android.deskclock.stopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.android.deskclock.R;
import com.android.util.TypeFaces;
import java.lang.ref.WeakReference;

public class StopwatchTimesShow extends View {
    private float dyTextB;
    private String intervalTimeStr;
    private boolean isBigSize;
    public boolean isRun;
    private boolean isfirst;
    private String mBigStr;
    private Context mContext;
    private LocalHandler mHandler;
    private long mIntervalTime;
    private final Paint mIntervalTimePaint;
    private float mMagin;
    private final Paint mPaintBig;
    private float mPaintBigSize;
    private float mPaintIntervalSize;
    private float mTextHeight;
    private float mTextXstart;
    private float mTextYstart;
    private long mTotalTime;
    private OnCircleTimeCallBack onCircleTimeCallBack;
    private float startX;
    private float startY;
    private int x;
    private int y;

    public interface OnCircleTimeCallBack {
        void onTimeBelowHour();

        void onTimeOverHour();
    }

    static class LocalHandler extends Handler {
        private WeakReference<StopwatchTimesShow> mView;

        public LocalHandler(StopwatchTimesShow context) {
            this.mView = new WeakReference(context);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StopwatchTimesShow playStatusView = (StopwatchTimesShow) this.mView.get();
            if (playStatusView != null) {
                switch (msg.what) {
                    case 501:
                        playStatusView.invalidate();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public StopwatchTimesShow(Context context) {
        this(context, null);
    }

    public StopwatchTimesShow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StopwatchTimesShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTotalTime = 0;
        this.x = 0;
        this.y = 0;
        this.dyTextB = 0.0f;
        this.mIntervalTime = 0;
        this.mHandler = new LocalHandler(this);
        this.isBigSize = true;
        this.isfirst = true;
        this.mContext = context;
        setContentDescription(Stopwatches.getTimeText(this.mTotalTime, false));
        this.mMagin = getResources().getDimension(R.dimen.stopwatch_textmagin);
        this.mPaintIntervalSize = getResources().getDimension(R.dimen.paint_interval_size);
        this.mPaintBigSize = getResources().getDimension(R.dimen.stopwatch_text_big);
        this.mPaintBig = new Paint();
        this.mPaintBig.setAntiAlias(true);
        this.mPaintBig.setColor(getResources().getColor(R.color.transparency_100_color));
        this.mPaintBig.setStyle(Style.STROKE);
        this.mPaintBig.setTextAlign(Align.LEFT);
        this.mPaintBig.setTextSize(this.mPaintBigSize);
        this.mTextHeight = this.mPaintBig.getTextSize();
        this.mIntervalTimePaint = new Paint();
        this.mIntervalTimePaint.setAntiAlias(true);
        this.mIntervalTimePaint.setColor(getResources().getColor(R.color.transparency_50_color));
        this.mIntervalTimePaint.setAlpha(127);
        Typeface tfLight = TypeFaces.get(this.mContext, "sans-serif-thin");
        this.mIntervalTimePaint.setTypeface(tfLight);
        this.mPaintBig.setTypeface(tfLight);
        this.mIntervalTimePaint.setStyle(Style.STROKE);
        this.mIntervalTimePaint.setTextAlign(Align.LEFT);
        this.mIntervalTimePaint.setTextSize(this.mPaintIntervalSize);
        this.mBigStr = Stopwatches.getTimeText(this.mTotalTime, false);
        this.intervalTimeStr = Stopwatches.getTimeText(this.mIntervalTime, false);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isfirst) {
            this.mTextXstart = (((float) this.x) - (this.mPaintBig.measureText(this.mBigStr) / 2.0f)) + 4.0f;
            this.mTextHeight = this.mPaintBig.getTextSize();
            this.mTextYstart = (this.dyTextB + (this.mTextHeight / 2.0f)) - (this.mTextHeight * 0.14f);
            this.startX = (((float) this.x) - (this.mIntervalTimePaint.measureText(this.intervalTimeStr) / 2.0f)) + 4.0f;
            this.startY = (this.mTextYstart + (this.mTextHeight / 2.0f)) + this.mMagin;
            this.isfirst = false;
        }
        drawTextDigital(canvas);
        if (this.isRun) {
            if (this.mHandler.hasMessages(501)) {
                this.mHandler.removeMessages(501);
            }
            this.mHandler.sendEmptyMessageDelayed(501, 120);
        }
    }

    private float dp2Px(int dpValue) {
        return TypedValue.applyDimension(1, (float) dpValue, this.mContext.getResources().getDisplayMetrics());
    }

    private void drawTextDigital(Canvas canvas) {
        canvas.drawText(this.mBigStr, this.mTextXstart, this.mTextYstart, this.mPaintBig);
        canvas.drawText(this.intervalTimeStr, this.startX, this.startY, this.mIntervalTimePaint);
    }

    public void updateTotalTime(long time) {
        this.mTotalTime = time;
        this.mBigStr = Stopwatches.getTimeText(this.mTotalTime, false);
        if (this.mTotalTime >= 3600000) {
            if (this.isBigSize) {
                this.mPaintBig.setTextSize(dp2Px(22));
                this.mTextXstart = (((float) this.x) - (this.mPaintBig.measureText(this.mBigStr) / 2.0f)) + 4.0f;
                this.mTextHeight = this.mPaintBig.getTextSize();
                this.mTextYstart = (this.dyTextB + (this.mTextHeight / 2.0f)) - (this.mTextHeight * 0.14f);
                this.onCircleTimeCallBack.onTimeOverHour();
                this.isBigSize = false;
            }
        } else if (!this.isBigSize) {
            this.mPaintBig.setTextSize(dp2Px(30));
            this.mTextXstart = (((float) this.x) - (this.mPaintBig.measureText(this.mBigStr) / 2.0f)) + 4.0f;
            this.mTextHeight = this.mPaintBig.getTextSize();
            this.mTextYstart = (this.dyTextB + (this.mTextHeight / 2.0f)) - (this.mTextHeight * 0.14f);
            this.onCircleTimeCallBack.onTimeBelowHour();
            this.isBigSize = true;
        }
        setContentDescription(this.mBigStr);
    }

    public void updateIntervalTime(long time) {
        this.mIntervalTime = time;
        this.intervalTimeStr = Stopwatches.getTimeText(this.mIntervalTime, false);
    }

    public void setOnCircleTimeCallBack(OnCircleTimeCallBack onCircleTimeCallBack) {
        this.onCircleTimeCallBack = onCircleTimeCallBack;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableHeight = this.mBottom - this.mTop;
        this.x = (this.mRight - this.mLeft) >> 1;
        this.y = availableHeight >> 1;
        this.dyTextB = (float) this.y;
        updateTotalTime(this.mTotalTime);
        updateIntervalTime(this.mIntervalTime);
    }
}
