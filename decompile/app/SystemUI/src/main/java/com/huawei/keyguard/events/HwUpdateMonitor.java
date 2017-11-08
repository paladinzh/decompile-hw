package com.huawei.keyguard.events;

import android.content.Context;
import com.google.android.collect.Lists;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.events.weather.LocationInfo;
import com.huawei.keyguard.events.weather.WeatherInfo;
import com.huawei.keyguard.util.HwLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HwUpdateMonitor implements MonitorChangeListener {
    private static HwUpdateMonitor sInstance;
    CallLogInfo mCallLogInfo = null;
    private final ArrayList<WeakReference<HwUpdateCallback>> mCallbacks = Lists.newArrayList();
    private final Context mContext;
    LocationInfo mLocationInfo = null;
    private final ArrayList<MonitorImpl> mMonitors = Lists.newArrayList();
    MessageInfo mMsgInfo = null;
    StepCounterInfo mStepCounterInfo = null;
    WeatherInfo mWeatherInfo = null;
    private boolean weatherRegisterFlag = false;

    public static class HwUpdateCallback {
        public void onNewMessageChange(MessageInfo info) {
        }

        public void onCalllogChange(CallLogInfo info) {
        }

        public void onWeatherChange(WeatherInfo info) {
        }

        public void onStepCounterChange(StepCounterInfo info) {
        }

        public void onClockLocationChange(LocationInfo info) {
        }
    }

    public HwUpdateMonitor(Context context) {
        this.mContext = context;
        this.mMonitors.add(new MessageMonitor(context, this, 0));
        this.mMonitors.add(new CallLogMonitor(context, this, 1));
        this.mMonitors.add(new WeatherMonitor(context, this, 2));
        this.mMonitors.add(new StepCounterMonitor(context, this, 3));
        this.mMonitors.add(new WeatherLocationMonitor(context, this, 4));
    }

    public static HwUpdateMonitor getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HwUpdateMonitor(context);
        }
        return sInstance;
    }

    public void registerCallback(HwUpdateCallback callback) {
        int sz = this.mCallbacks.size();
        HwLog.i("HwUpdateMonitor", "registerCallback " + callback + ", callback size = " + sz);
        for (int i = 0; i < sz; i++) {
            if (((WeakReference) this.mCallbacks.get(i)).get() == callback) {
                HwLog.w("HwUpdateMonitor", "tried to add another callback " + callback);
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(callback));
        clearCallback(null);
        sendUpdates(callback);
        if (sz <= 0) {
            registerMonitor(this.mContext);
        }
        if (this.weatherRegisterFlag) {
            ((MonitorImpl) this.mMonitors.get(2)).register();
        }
    }

    public void unRegisterCallback(HwUpdateCallback callback) {
        int sz = this.mCallbacks.size();
        int i = sz - 1;
        while (i >= 0 && ((WeakReference) this.mCallbacks.get(i)).get() != callback) {
            i--;
        }
        if (i < 0) {
            HwLog.w("HwUpdateMonitor", "unRegisterCallback not found " + callback);
            return;
        }
        this.mCallbacks.remove(i);
        if (sz <= 1) {
            unRegisterMonitor();
        }
    }

    public void clearCallback(HwUpdateCallback callback) {
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            if (((WeakReference) this.mCallbacks.get(i)).get() == callback) {
                this.mCallbacks.remove(i);
            }
        }
    }

    public void clearStepCountInfo() {
        this.mStepCounterInfo = null;
    }

    public void clearNotifications() {
        HwLog.d("HwUpdateMonitor", "clearNotifications");
        if (this.mMsgInfo != null) {
            this.mMsgInfo.setUnReadCount(0);
        }
        if (this.mCallLogInfo != null) {
            this.mCallLogInfo.setmMissedcount(0);
        }
    }

    public void sendUpdates(HwUpdateCallback callback) {
        callback.onNewMessageChange(this.mMsgInfo);
        callback.onCalllogChange(this.mCallLogInfo);
        callback.onWeatherChange(this.mWeatherInfo);
        callback.onStepCounterChange(this.mStepCounterInfo);
    }

    private void registerMonitor(Context context) {
        HwLog.i("HwUpdateMonitor", "registerMonitor");
        for (MonitorImpl impl : this.mMonitors) {
            impl.register();
        }
    }

    private void unRegisterMonitor() {
        for (MonitorImpl impl : this.mMonitors) {
            impl.unRegister();
        }
    }

    public void onMonitorChanged(int monitorId, Object result) {
        if (this.mCallbacks.size() > 0 || !(monitorId == 0 || monitorId == 1)) {
            int i;
            HwUpdateCallback cb;
            switch (monitorId) {
                case 0:
                    if (result instanceof MessageInfo) {
                        this.mMsgInfo = (MessageInfo) result;
                        for (i = 0; i < this.mCallbacks.size(); i++) {
                            if (this.mCallbacks.get(i) != null) {
                                cb = (HwUpdateCallback) ((WeakReference) this.mCallbacks.get(i)).get();
                                if (cb != null) {
                                    cb.onNewMessageChange(this.mMsgInfo);
                                }
                            }
                        }
                        break;
                    }
                    break;
                case 1:
                    if (result instanceof CallLogInfo) {
                        this.mCallLogInfo = (CallLogInfo) result;
                        for (i = 0; i < this.mCallbacks.size(); i++) {
                            if (this.mCallbacks.get(i) != null) {
                                cb = (HwUpdateCallback) ((WeakReference) this.mCallbacks.get(i)).get();
                                if (cb != null) {
                                    cb.onCalllogChange(this.mCallLogInfo);
                                }
                            }
                        }
                        break;
                    }
                    break;
                case 2:
                    if (result instanceof WeatherInfo) {
                        if (this.weatherRegisterFlag) {
                            setWeatherRegisterFlag(false);
                        }
                        this.mWeatherInfo = (WeatherInfo) result;
                        for (i = 0; i < this.mCallbacks.size(); i++) {
                            if (this.mCallbacks.get(i) != null) {
                                cb = (HwUpdateCallback) ((WeakReference) this.mCallbacks.get(i)).get();
                                if (cb != null) {
                                    cb.onWeatherChange(this.mWeatherInfo);
                                }
                            }
                        }
                        break;
                    }
                    break;
                case 3:
                    if (result instanceof StepCounterInfo) {
                        this.mStepCounterInfo = (StepCounterInfo) result;
                        for (i = 0; i < this.mCallbacks.size(); i++) {
                            cb = (HwUpdateCallback) ((WeakReference) this.mCallbacks.get(i)).get();
                            if (cb != null) {
                                cb.onStepCounterChange(this.mStepCounterInfo);
                            }
                        }
                        break;
                    }
                    break;
                case 4:
                    if (result instanceof LocationInfo) {
                        this.mLocationInfo = (LocationInfo) result;
                        for (i = 0; i < this.mCallbacks.size(); i++) {
                            if (this.mCallbacks.get(i) != null) {
                                cb = (HwUpdateCallback) ((WeakReference) this.mCallbacks.get(i)).get();
                                if (cb != null) {
                                    cb.onClockLocationChange(this.mLocationInfo);
                                }
                            }
                        }
                        break;
                    }
                    break;
                default:
                    HwLog.w("HwUpdateMonitor", "onMonitorChanged, unknow monitor id = " + monitorId);
                    break;
            }
            return;
        }
        HwLog.i("HwUpdateMonitor", "Ignore mms or call update because mCallbacks size is 0");
    }

    public void setWeatherRegisterFlag(boolean flag) {
        this.weatherRegisterFlag = flag;
    }
}
