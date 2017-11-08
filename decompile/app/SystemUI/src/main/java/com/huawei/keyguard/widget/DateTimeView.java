package com.huawei.keyguard.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.widget.GridLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.weather.LocationInfo;
import com.huawei.keyguard.events.weather.WeatherHelper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.view.KgViewUtils;
import com.huawei.keyguard.view.widget.ClockView;
import java.util.Calendar;
import java.util.TimeZone;

public class DateTimeView extends GridLayout implements IEventListener, IContentListener {
    private ClockView mClockView;
    private boolean mConfigurationChanged;
    private ClockView mCurrentClockView;
    private TextView mDateView;
    private View mDivideLine;
    private RefreshTask mFreshTask;
    private final Handler mHandler;
    private ClockView mHomeClockView;
    HwUpdateCallback mLocationUpdateCallback;
    private Handler mUIhandler;
    private KeyguardUpdateMonitorCallback mUpdateCallback;
    private Runnable mUpdateClockView;

    private class RefreshTask implements Runnable {
        private Runnable mUiCallback;
        private boolean needDualClock;

        private RefreshTask() {
            this.mUiCallback = new Runnable() {
                public void run() {
                    DateTimeView.this.refreshDualClock(RefreshTask.this.needDualClock);
                }
            };
        }

        public void run() {
            WeatherHelper.getInstance().queryLocation();
            this.needDualClock = DateTimeView.this.needDualClock();
            DateTimeView.this.mUIhandler.post(this.mUiCallback);
        }
    }

    public void onContentChange(boolean selfChange) {
        this.mUIhandler.removeCallbacks(this.mUpdateClockView);
        this.mUIhandler.postDelayed(this.mUpdateClockView, 100);
    }

    public boolean onReceive(Context context, Intent intent) {
        onContentChange(false);
        return false;
    }

    public DateTimeView(Context context) {
        this(context, null, 0);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mUIhandler = GlobalContext.getUIHandler();
        this.mHandler = GlobalContext.getUIHandler();
        this.mConfigurationChanged = false;
        this.mFreshTask = new RefreshTask();
        this.mUpdateClockView = new Runnable() {
            public void run() {
                DateTimeView.this.refreshDualClock(DateTimeView.this.needDualClock());
            }
        };
        this.mLocationUpdateCallback = new HwUpdateCallback() {
            public void onClockLocationChange(LocationInfo locationInfo) {
                HwLog.d("DateTimeView", "onClockLocationChange");
                if (locationInfo == null) {
                    HwLog.w("DateTimeView", "onWeatherChange info is null - no change happened");
                    return;
                }
                WeatherHelper.getInstance().setLocationInfo(locationInfo);
                DateTimeView.this.mUIhandler.post(DateTimeView.this.mUpdateClockView);
            }
        };
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() {
            public void onStartedGoingToSleep(int why) {
                DateTimeView.this.freshData(500);
            }
        };
    }

    protected void onFinishInflate() {
        this.mDateView = (TextView) findViewById(R$id.date_view);
        this.mClockView = (ClockView) findViewById(R$id.hw_clock_view);
        HwUpdateMonitor.getInstance(this.mContext).registerCallback(this.mLocationUpdateCallback);
        refresh();
        freshData(0);
        super.onFinishInflate();
    }

    protected void refresh() {
        if (this.mClockView.getVisibility() == 0) {
            this.mClockView.updateTime();
            return;
        }
        if (this.mCurrentClockView != null) {
            this.mCurrentClockView.updateTime();
        }
        if (this.mHomeClockView != null) {
            this.mHomeClockView.updateTime();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwLog.d("DateTimeView", "KeyguardStatusViewEx onAttachedToWindow ");
        EventCenter.getInst().listen(1, this);
        EventCenter.getInst().listenContent(1, this);
        HwKeyguardUpdateMonitor.getInstance().registerCallback(this.mUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(this.mContext).unRegisterCallback(this.mLocationUpdateCallback);
        HwLog.d("DateTimeView", "KeyguardStatusViewEx onDetachedFromWindow");
        EventCenter.getInst().stopListen(this);
        EventCenter.getInst().stopListenContent(this);
        HwKeyguardUpdateMonitor.getInstance().removeCallback(this.mUpdateCallback);
    }

    private boolean needDualClock() {
        return (WeatherHelper.getInstance().getLocationInfo() == null || WeatherHelper.getInstance().isShowOneClock()) ? false : true;
    }

    private void setLocationText() {
        if (needDualClock() && this.mCurrentClockView != null && this.mCurrentClockView.findViewById(R$id.description) != null) {
            ((TextView) this.mCurrentClockView.findViewById(R$id.description)).setText(R$string.kg_clock_current_location);
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mConfigurationChanged && KgViewUtils.isViewVisible(this)) {
            this.mConfigurationChanged = false;
            freshData(500);
        }
    }

    private void freshData(long delay) {
        GlobalContext.getBackgroundHandler().removeCallbacks(this.mFreshTask);
        if (delay == 0) {
            GlobalContext.getBackgroundHandler().post(this.mFreshTask);
        } else if (KgViewUtils.isViewVisible(this)) {
            GlobalContext.getBackgroundHandler().postDelayed(this.mFreshTask, delay);
        } else {
            this.mConfigurationChanged = true;
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        setLocationText();
        freshData(500);
        super.onConfigurationChanged(newConfig);
    }

    private static int convertDpToPx(Context context, int dpValue) {
        return (int) ((((float) dpValue) * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void refreshDualClock(boolean neeDualClock) {
        View view = findViewById(R$id.dual_clock_view);
        this.mDivideLine = getRootView().findViewById(R$id.divider_simple);
        LayoutParams dividerLayoutParams;
        if (!neeDualClock || view == null) {
            HwLog.d("DateTimeView", "Change to single clock mode!");
            if (this.mDivideLine != null) {
                dividerLayoutParams = (LayoutParams) this.mDivideLine.getLayoutParams();
                if (HwUnlockUtils.isTablet()) {
                    dividerLayoutParams.width = convertDpToPx(this.mContext, 165);
                } else {
                    dividerLayoutParams.width = convertDpToPx(this.mContext, 180);
                }
                this.mDivideLine.setLayoutParams(dividerLayoutParams);
            }
            if (this.mClockView != null) {
                this.mClockView.updateTime();
                this.mClockView.setVisibility(0);
            }
            if (this.mDateView != null) {
                this.mDateView.setVisibility(0);
            }
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        if (this.mDivideLine != null) {
            dividerLayoutParams = (LayoutParams) this.mDivideLine.getLayoutParams();
            if (HwUnlockUtils.isTablet()) {
                dividerLayoutParams.width = convertDpToPx(this.mContext, 150);
            } else {
                dividerLayoutParams.width = convertDpToPx(this.mContext, 154);
            }
            this.mDivideLine.setLayoutParams(dividerLayoutParams);
        }
        HwLog.d("DateTimeView", "Change to double clock mode!");
        if (view instanceof ViewStub) {
            view = ((ViewStub) view).inflate();
        }
        this.mCurrentClockView = (ClockView) findViewById(R$id.clock_view1);
        this.mHomeClockView = (ClockView) findViewById(R$id.clock_view2);
        if (this.mCurrentClockView != null && this.mHomeClockView != null) {
            view.setVisibility(0);
            if (this.mClockView != null) {
                this.mClockView.setVisibility(8);
            }
            if (this.mDateView != null) {
                this.mDateView.setVisibility(8);
            }
            revertTimeParent(this.mCurrentClockView);
            revertTimeParent(this.mHomeClockView);
            LocationInfo locationInfo = WeatherHelper.getInstance().getLocationInfo();
            if (locationInfo != null) {
                TimeZone homeTimeZone = locationInfo.getHomeTimeZone();
                if (homeTimeZone != null) {
                    this.mHomeClockView.updateTime(Calendar.getInstance(homeTimeZone));
                    this.mHomeClockView.setFixedTimeZone(true);
                }
                this.mCurrentClockView.updateTime(Calendar.getInstance());
                ((TextView) this.mHomeClockView.findViewById(R$id.description)).setText(locationInfo.getHomeLocation());
            }
        }
    }

    private void revertTimeParent(ClockView clockView) {
        View timeParent = clockView.findViewById(R$id.clock_parent);
        timeParent.setLayoutParams((MarginLayoutParams) timeParent.getLayoutParams());
    }
}
