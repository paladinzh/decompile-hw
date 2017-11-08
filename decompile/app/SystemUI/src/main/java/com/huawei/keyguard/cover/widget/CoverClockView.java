package com.huawei.keyguard.cover.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$color;
import com.android.keyguard.R$id;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.cover.AnalogClockResourceUtils;
import com.huawei.keyguard.cover.CoverCfg;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.events.TimeZoneManager;
import com.huawei.keyguard.events.weather.LocationInfo;
import com.huawei.keyguard.events.weather.WeatherHelper;
import com.huawei.keyguard.util.EventViewHelper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.Typefaces;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.TimeZone;

public class CoverClockView extends RelativeLayout implements Callback {
    private boolean enableAnaclock;
    private NewCoverDigitalClock mAnalogClockView;
    private TextView mCallCount;
    private TextView mChargingView;
    private NewCoverDigitalClock mClockView;
    private NewCoverDigitalClock mClockView1;
    private NewCoverDigitalClock mClockView2;
    private Handler mHandler;
    private TextView mHomeLocationView;
    private View mMissCallView;
    private TextView mMmsCount;
    private int mStepCounterColorBright;
    private int mStepCounterColorGray;
    private TextView mTextViewSteps;
    HwUpdateCallback mUpdateCallback;
    private boolean unShownBottominfoInDualAnalog;

    private class RefreshTask extends AsyncTask<Void, Void, Boolean> {
        private RefreshTask() {
        }

        protected Boolean doInBackground(Void... voids) {
            CoverClockView.this.queryWeatherInfo();
            return Boolean.valueOf(CoverClockView.this.needDualClock());
        }

        protected void onPostExecute(Boolean needDualClock) {
            CoverClockView.this.refreshDualClock(needDualClock.booleanValue());
        }
    }

    public CoverClockView(Context context) {
        this(context, null);
    }

    public CoverClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.unShownBottominfoInDualAnalog = false;
        this.enableAnaclock = false;
        this.mUpdateCallback = new HwUpdateCallback() {
            public void onClockLocationChange(final LocationInfo locationInfo) {
                HwLog.w("CoverClockView", "onClockLocationChange");
                if (locationInfo == null) {
                    HwLog.w("CoverClockView", "onWeatherChange info is null - no change happened");
                } else {
                    CoverClockView.this.mHandler.post(new Runnable() {
                        public void run() {
                            WeatherHelper.getInstance().setLocationInfo(locationInfo);
                            CoverClockView.this.refreshDualClock(CoverClockView.this.needDualClock());
                        }
                    });
                }
            }

            public void onNewMessageChange(MessageInfo info) {
                if (info == null) {
                    HwLog.i("CoverClockView", "onNewMessageChange info is null - no change happened");
                    return;
                }
                HwLog.i("CoverClockView", "onNewMessageChange missedCount=" + info.getUnReadCount());
                Message message = CoverClockView.this.mHandler.obtainMessage(1000);
                message.arg1 = info.getUnReadCount();
                CoverClockView.this.mHandler.sendMessage(message);
            }

            public void onCalllogChange(CallLogInfo info) {
                if (info == null) {
                    HwLog.i("CoverClockView", "onCalllogChange info is null - no change happened");
                    return;
                }
                HwLog.i("CoverClockView", "onCalllogChange missedCount=" + info.getMissedcount());
                Message message = CoverClockView.this.mHandler.obtainMessage(1001);
                message.arg1 = info.getMissedcount();
                CoverClockView.this.mHandler.sendMessage(message);
            }

            public void onStepCounterChange(StepCounterInfo info) {
                int i = 0;
                if (info == null) {
                    HwLog.i("CoverClockView", "onStepCounterChange info is null - no change happened!");
                } else if (info.getStepsCount() < 0 || info.getStepsCount() == 0) {
                    HwLog.i("CoverClockView", "onStepCounterChange info is invaild");
                } else {
                    if (CoverClockView.this.mTextViewSteps == null) {
                        HwLog.w("CoverClockView", "onStepCounterChange, mTextViewSteps = " + CoverClockView.this.mTextViewSteps);
                    } else {
                        boolean bStepEnable = HwUnlockUtils.isStepCounterEnabled(CoverClockView.this.getContext()).booleanValue();
                        TextView -get1 = CoverClockView.this.mTextViewSteps;
                        if (!bStepEnable) {
                            i = 8;
                        }
                        -get1.setVisibility(i);
                    }
                    Message message = CoverClockView.this.mHandler.obtainMessage(1002);
                    message.obj = info;
                    CoverClockView.this.mHandler.sendMessage(message);
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        CoverClockView.this.onMmsChange(msg.arg1);
                        break;
                    case 1001:
                        CoverClockView.this.onCallLogChange(msg.arg1);
                        break;
                    case 1002:
                        if (msg.obj instanceof StepCounterInfo) {
                            StepCounterInfo si = msg.obj;
                            if (!si.getEnableCounterChanged()) {
                                CoverClockView.this.onStepsChange(si.getStepsCount());
                                break;
                            }
                            CoverClockView.this.onStepCounterStateChange(si.getEnableCounter());
                            CoverClockView.this.onStepsChange(si.getStepsCount());
                            break;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        HwLog.i("CoverClockView", " onFinishInflate");
        this.mCallCount = (TextView) findViewById(R$id.cover_miss_calllog_count);
        this.mMmsCount = (TextView) findViewById(R$id.cover_new_mms_count);
        this.mChargingView = (TextView) findViewById(R$id.charging);
        this.mMissCallView = findViewById(R$id.miss_call_mms);
        new RefreshTask().executeOnExecutor(GlobalContext.getSerialExecutor(), new Void[0]);
        View v = findViewById(R$id.cover_steps_textview);
        if (v instanceof TextView) {
            this.mTextViewSteps = (TextView) v;
        }
        this.mStepCounterColorBright = getResources().getColor(R$color.coverscreen_step_counter_bright);
        this.mStepCounterColorGray = getResources().getColor(R$color.coverscreen_step_counter_gray);
        if (this.mCallCount == null || this.mMmsCount == null) {
            HwLog.w("CoverClockView", "onFinishInflate, mCallCount=" + this.mCallCount + ", mMmsCount=" + this.mMmsCount);
            return;
        }
        Typeface t = Typefaces.get(getContext(), "/system/fonts/Roboto-Regular.ttf");
        if (t != null) {
            this.mCallCount.setTypeface(t);
            this.mMmsCount.setTypeface(t);
        }
        this.mCallCount.setVisibility(8);
        this.mMmsCount.setVisibility(8);
        this.mTextViewSteps.setVisibility(8);
        this.mClockView = (NewCoverDigitalClock) findViewById(R$id.clock_view);
        boolean needDualClock = needDualClock();
        HwLog.i("CoverClockView", "needDualClock is " + needDualClock);
        this.enableAnaclock = getResources().getBoolean(R$bool.coverscreen_analogclock_enable);
        if (this.enableAnaclock) {
            this.mAnalogClockView = (NewCoverDigitalClock) findViewById(R$id.analog_clock_view);
        }
        if (BatteryStateInfo.getInst().showBatteryInfo()) {
            this.mChargingView.setVisibility(0);
            this.mChargingView.setText(BatteryStateInfo.getInst().getBatteryInfo2(this.mContext));
        } else {
            this.mChargingView.setVisibility(8);
        }
        if (!this.enableAnaclock || !AnalogClockResourceUtils.isThemeAnalogClockType()) {
            if (this.mClockView != null) {
                this.mClockView.setVisibility(0);
            }
            if (this.mAnalogClockView != null) {
                this.mAnalogClockView.setVisibility(8);
            }
            refreshDualClock(needDualClock);
        } else if (this.mAnalogClockView != null) {
            this.mClockView.setVisibility(8);
            if (needDualClock) {
                this.mAnalogClockView.setVisibility(8);
                refreshDualAnalogClock(needDualClock);
            } else {
                this.mAnalogClockView.setVisibility(0);
            }
        }
    }

    private void refreshDualClock(boolean needDualClock) {
        boolean z;
        View view = findViewById(R$id.dual_clock_view);
        String str = "CoverClockView";
        StringBuilder append = new StringBuilder().append("In refreshDualClock,null != view is ");
        if (view != null) {
            z = true;
        } else {
            z = false;
        }
        HwLog.i(str, append.append(z).toString());
        LocationInfo mLocationInfo = WeatherHelper.getInstance().getLocationInfo();
        if (!needDualClock || view == null) {
            if (this.mClockView != null) {
                this.mClockView.setVisibility(0);
                if (mLocationInfo != null) {
                    this.mClockView.setWeatherInfo(mLocationInfo.getCurrentWeatherInfo());
                }
            }
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        HwLog.i("CoverClockView", "In refreshDualClock,needDualClock is " + needDualClock);
        if (view instanceof ViewStub) {
            view = ((ViewStub) view).inflate();
        }
        this.mClockView1 = (NewCoverDigitalClock) findViewById(R$id.clock_view1);
        this.mClockView2 = (NewCoverDigitalClock) findViewById(R$id.clock_view2);
        this.mHomeLocationView = (TextView) findViewById(R$id.home_location_view);
        if (this.mClockView1 != null && this.mClockView2 != null) {
            view.setVisibility(0);
            if (this.mClockView != null) {
                this.mClockView.setVisibility(8);
            }
            if (mLocationInfo != null) {
                TimeZone homeTimeZone = mLocationInfo.getHomeTimeZone();
                if (homeTimeZone != null) {
                    Calendar calendar = Calendar.getInstance(homeTimeZone);
                    this.mClockView1.setWeatherInfo(mLocationInfo.getHomeWeatherInfo());
                    this.mClockView1.setCalendar(calendar);
                }
                this.mClockView2.setWeatherInfo(mLocationInfo.getCurrentWeatherInfo());
                this.mHomeLocationView.setText(mLocationInfo.getHomeLocation());
            }
        }
    }

    public void refreshDualAnalogClock(boolean needDualClock) {
        View view = findViewById(R$id.analog_dualclock_view);
        if (!needDualClock || view == null) {
            if (this.mAnalogClockView != null) {
                this.mAnalogClockView.setVisibility(0);
            } else if (this.mClockView != null) {
                this.mClockView.setVisibility(0);
            }
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        if (view instanceof ViewStub) {
            view = ((ViewStub) view).inflate();
        }
        NewCoverDigitalClock clockView1 = (NewCoverDigitalClock) findViewById(R$id.analog_clock_view1);
        NewCoverDigitalClock clockView2 = (NewCoverDigitalClock) findViewById(R$id.analog_clock_view2);
        if (clockView1 != null && clockView2 != null) {
            view.setVisibility(0);
            if (this.mAnalogClockView != null) {
                this.mAnalogClockView.setVisibility(8);
            }
            TimeZone deftimeZone = TimeZoneManager.getInstance().getDefaultTimeZone(this.mContext);
            if (deftimeZone != null) {
                clockView1.setCalendar(Calendar.getInstance(deftimeZone));
            }
            this.unShownBottominfoInDualAnalog = getResources().getBoolean(R$bool.coverscreen_dualanalogclock_bottominfo_unshown);
            if (this.unShownBottominfoInDualAnalog) {
                if (this.mMissCallView != null) {
                    this.mMissCallView.setVisibility(8);
                }
                if (this.mChargingView != null) {
                    this.mChargingView.setVisibility(8);
                }
            }
        }
    }

    private boolean needDualClock() {
        return (WeatherHelper.getInstance().getLocationInfo() == null || WeatherHelper.getInstance().isShowOneClock()) ? false : true;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
        AppHandler.addListener(this);
        if (!CoverCfg.isUseThemeOnlineFonts()) {
            return;
        }
        if (needDualClock()) {
            if (this.mClockView1 == null || this.mClockView2 == null) {
                this.mClockView1 = (NewCoverDigitalClock) findViewById(R$id.clock_view1);
                this.mClockView2 = (NewCoverDigitalClock) findViewById(R$id.clock_view2);
            }
            if (this.mClockView1 != null && this.mClockView2 != null) {
                this.mClockView1.setDigitalTimeFontSize(getContext(), 1);
                this.mClockView2.setDigitalTimeFontSize(getContext(), 1);
                return;
            }
            return;
        }
        if (this.mClockView == null) {
            this.mClockView = (NewCoverDigitalClock) findViewById(R$id.clock_view);
        }
        if (this.mClockView != null) {
            this.mClockView.setDigitalTimeFontSize(getContext(), 0);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
        AppHandler.removeListener(this);
    }

    public void onCallLogChange(int count) {
        if (this.mCallCount == null) {
            HwLog.w("CoverClockView", "onCallLogChange view is null");
        } else if (count <= 0) {
            HwLog.w("CoverClockView", "hide miss call view count = " + count);
            this.mCallCount.setVisibility(8);
        } else {
            CharSequence numText = BuildConfig.FLAVOR + count;
            if (count > 99) {
                numText = "99+";
            }
            this.mCallCount.setText(numText);
            this.mCallCount.setContentDescription(EventViewHelper.getMissCallAccessibilityDescription(getContext(), count, false));
            if (!KeyguardUpdateMonitor.getInstance(getContext()).isSimPinSecure()) {
                this.mCallCount.setVisibility(0);
            }
        }
    }

    public void onMmsChange(int count) {
        if (this.mMmsCount == null) {
            HwLog.w("CoverClockView", "onCallLogChange view is null");
        } else if (count <= 0) {
            HwLog.w("CoverClockView", "hide miss mms view count = " + count);
            this.mMmsCount.setVisibility(8);
        } else {
            CharSequence numText = BuildConfig.FLAVOR + count;
            if (count > 99) {
                numText = "99+";
            }
            this.mMmsCount.setText(numText);
            this.mMmsCount.setContentDescription(EventViewHelper.getNewMmsAccessibilityDescription(getContext(), count, false));
            if (!KeyguardUpdateMonitor.getInstance(getContext()).isSimPinSecure()) {
                this.mMmsCount.setVisibility(0);
            }
        }
    }

    private void queryWeatherInfo() {
        WeatherHelper.getInstance().queryLocation();
    }

    public void onStepsChange(int step) {
        if (this.mTextViewSteps == null) {
            HwLog.w("CoverClockView", "onStepNumChange, mSteps is null!");
        } else {
            this.mTextViewSteps.setText(BuildConfig.FLAVOR + step);
        }
    }

    public void onStepCounterStateChange(boolean enable) {
        if (this.mTextViewSteps == null) {
            HwLog.w("CoverClockView", "onStepCounterStateChange, mTextViewSteps = " + this.mTextViewSteps);
            return;
        }
        int i;
        TextView textView = this.mTextViewSteps;
        if (enable) {
            i = this.mStepCounterColorBright;
        } else {
            i = this.mStepCounterColorGray;
        }
        textView.setTextColor(i);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 100) {
            if (this.mChargingView == null) {
                HwLog.w("CoverClockView", "onRefreshBatteryInfo, mChargingView is null!");
                return false;
            } else if (BatteryStateInfo.getInst().showBatteryInfo()) {
                this.mChargingView.setVisibility(0);
                this.mChargingView.setText(BatteryStateInfo.getInst().getBatteryInfo2(this.mContext));
            } else {
                this.mChargingView.setVisibility(8);
            }
        }
        return false;
    }
}
