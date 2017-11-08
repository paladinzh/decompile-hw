package com.huawei.systemmanager.power.batterychart;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.batterychart.BatterHistoryUtils.UpdateCallBack;
import com.huawei.systemmanager.util.HwLog;

public class BatteryDraggingBar extends LinearLayout {
    private static final int HALF_HOUR_IN_MILLI_SECONDS = 1800000;
    private static final int INIT_LEFT = -10000;
    private static final int MSG_UPDATE_TIME = 1;
    private static final int MSG_VIEW_IDLE = 2;
    private static final int STEP_TIME = 1800000;
    private static final String TAG = "BatteryDraggingBar";
    private static final int TWO_DAY_IN_HALF_HOUR = 96;
    private static final int TWO_DAY_IN_MILLI_SECONDS = 172800000;
    private final int CHART_AREA_RIGHT_PADDING;
    private final int LEFT_PADDING;
    private final int LINE_WIDTH;
    private final int RIGHT_PADDING;
    private UpdateCallBack mCallBack;
    private long mChartEndTime;
    private long mChartStartTime;
    private BatteryBarView mDragBarImg;
    private View mDragBarImgWrap;
    private int mDragBarWidth;
    private ViewDragHelper mDragHelper;
    private Handler mHandler;
    private int mLastLeft;
    private int mLeftBound;
    private int mMaxCount;
    private int mRightBound;
    private double mStepWidth;

    private class DragHelperCallback extends Callback {
        private DragHelperCallback() {
        }

        public boolean tryCaptureView(View child, int pointerId) {
            return child == BatteryDraggingBar.this.mDragBarImgWrap;
        }

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left > BatteryDraggingBar.this.mRightBound) {
                return BatteryDraggingBar.this.mRightBound;
            }
            if (left < BatteryDraggingBar.this.mLeftBound) {
                return BatteryDraggingBar.this.mLeftBound;
            }
            return left;
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            BatteryDraggingBar.this.mHandler.removeMessages(1);
            Message msg = BatteryDraggingBar.this.mHandler.obtainMessage(1);
            int t = (((BatteryDraggingBar.this.mDragBarImgWrap.getWidth() - BatteryDraggingBar.this.mDragBarWidth) / 2) + left) + BatteryDraggingBar.this.LINE_WIDTH;
            msg.arg1 = t;
            BatteryDraggingBar.this.mHandler.sendMessage(msg);
            BatteryDraggingBar.this.mLastLeft = t;
        }

        public void onViewDragStateChanged(int state) {
            switch (state) {
                case 0:
                    HwLog.i(BatteryDraggingBar.TAG, "onViewDragStateChanged , state is idle");
                    if (BatteryDraggingBar.this.mLastLeft != -10000) {
                        BatteryDraggingBar.this.mHandler.removeMessages(2);
                        Message msg = BatteryDraggingBar.this.mHandler.obtainMessage(2);
                        msg.arg1 = BatteryDraggingBar.this.mLastLeft;
                        BatteryDraggingBar.this.mHandler.sendMessage(msg);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class DragOnGlobalLayoutListener implements OnGlobalLayoutListener {
        private DragOnGlobalLayoutListener() {
        }

        public void onGlobalLayout() {
            BatteryDraggingBar.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            BatteryDraggingBar.this.checkInitDragBar();
        }
    }

    private class TimeUpDateHandler extends Handler {
        private TimeUpDateHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                int left = msg.arg1;
                int delta = left - BatteryDraggingBar.this.LEFT_PADDING;
                int count = (int) ((((double) delta) / BatteryDraggingBar.this.mStepWidth) + 0.5d);
                HwLog.i(BatteryDraggingBar.TAG, "handleMessage, left is:" + left + "  delta is:" + delta + "  count is:" + count);
                if (count > BatteryDraggingBar.this.mMaxCount) {
                    HwLog.i(BatteryDraggingBar.TAG, "handleMessage , count > mMaxCount");
                    count = BatteryDraggingBar.this.mMaxCount;
                }
                if (count < 0) {
                    HwLog.i(BatteryDraggingBar.TAG, "handleMessage , count < 0");
                    count = 0;
                }
                long start = BatteryDraggingBar.this.mChartStartTime + ((((long) count) * 1) * 1800000);
                switch (msg.what) {
                    case 1:
                        BatteryDraggingBar.this.mCallBack.updateTime(start, delta);
                        break;
                    case 2:
                        BatteryDraggingBar.this.mCallBack.onDragBarIdle(start);
                        break;
                }
            }
        }
    }

    public BatteryDraggingBar(Context paramContext) {
        super(paramContext);
        this.LINE_WIDTH = (int) getResources().getDimension(R.dimen.battery_history_chart_linewidth);
        this.LEFT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_left_padding);
        this.RIGHT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_right_margin);
        this.CHART_AREA_RIGHT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_area_right_padding);
        this.mLastLeft = -10000;
    }

    public int getLastLeft() {
        return this.mLastLeft;
    }

    public long getLastLeftTime() {
        return this.mChartStartTime + ((((long) this.mMaxCount) * 1) * 1800000);
    }

    public void setChartStartTime(long time) {
        this.mChartStartTime = time;
        HwLog.i(TAG, "setChartStartTime , mChartStartTime is: " + this.mChartStartTime);
    }

    public void setChartEndTime(long time) {
        this.mChartEndTime = time;
        HwLog.i(TAG, "setChartEndTime , mChartEndTime is: " + this.mChartEndTime);
    }

    public void setCallBack(UpdateCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public BatteryDraggingBar(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        this.LINE_WIDTH = (int) getResources().getDimension(R.dimen.battery_history_chart_linewidth);
        this.LEFT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_left_padding);
        this.RIGHT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_right_margin);
        this.CHART_AREA_RIGHT_PADDING = (int) getResources().getDimension(R.dimen.battery_history_chart_area_right_padding);
        this.mLastLeft = -10000;
        this.mDragHelper = ViewDragHelper.create(this, 0.6f, new DragHelperCallback());
        HwLog.i(TAG, "BatteryDraggingBar constructor");
        initDragBar();
        this.mHandler = new TimeUpDateHandler();
    }

    public void initDragBar() {
        getViewTreeObserver().addOnGlobalLayoutListener(new DragOnGlobalLayoutListener());
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (action != 3 && action != 1) {
            return this.mDragHelper.shouldInterceptTouchEvent(ev);
        }
        this.mDragHelper.cancel();
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mDragHelper.processTouchEvent(ev);
        return true;
    }

    public void checkInitDragBar() {
        HwLog.i(TAG, "BatteryDraggingBar checkInitDragBar");
        if (this.mDragBarImg == null) {
            this.mDragBarImgWrap = findViewById(R.id.wrap);
            this.mDragBarImg = (BatteryBarView) findViewById(R.id.dragging_bar_image);
        }
        LayoutParams params = (LayoutParams) this.mDragBarImg.getLayoutParams();
        this.mDragBarWidth = (((getWidth() - this.LEFT_PADDING) - this.RIGHT_PADDING) - this.CHART_AREA_RIGHT_PADDING) / 48;
        this.mStepWidth = ((double) (((getWidth() - this.LEFT_PADDING) - this.RIGHT_PADDING) - this.CHART_AREA_RIGHT_PADDING)) / 96.0d;
        params.width = this.mDragBarWidth;
        this.mDragBarImg.setLayoutParams(params);
        initBound();
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) this.mDragBarImgWrap.getLayoutParams();
        marginLayoutParams.leftMargin = this.mRightBound;
        this.mDragBarImgWrap.setLayoutParams(marginLayoutParams);
        this.mLastLeft = (this.mRightBound + ((this.mDragBarImgWrap.getWidth() - this.mDragBarWidth) / 2)) + (this.LINE_WIDTH / 2);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initDragBar();
    }

    private void initBound() {
        this.mLeftBound = (this.LEFT_PADDING - ((this.mDragBarImgWrap.getWidth() - this.mDragBarWidth) / 2)) - (this.LINE_WIDTH / 2);
        this.mMaxCount = (((int) (this.mChartEndTime - this.mChartStartTime)) / 1800000) - 2;
        if (this.mMaxCount < 0) {
            HwLog.i(TAG, "handleMessage, mMaxCount is: " + this.mMaxCount);
            this.mMaxCount = 0;
        }
        this.mRightBound = this.mLeftBound + ((int) (((double) this.mMaxCount) * this.mStepWidth));
    }
}
