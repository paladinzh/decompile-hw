package com.android.server.display;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;

public class HwLightSensorController {
    private static boolean DEBUG = false;
    private static final int MSG_TIMER = 1;
    private static final String TAG = "HwLightSensorController";
    private final LightSensorCallbacks mCallbacks;
    private List<Integer> mDataList;
    private boolean mEnable;
    private long mEnableTime;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int lux = HwLightSensorController.this.getSensorData();
                    if (lux >= 0) {
                        try {
                            HwLightSensorController.this.mCallbacks.processSensorData(SystemClock.elapsedRealtime(), lux);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (HwLightSensorController.this.mEnable) {
                        sendEmptyMessageDelayed(1, (long) HwLightSensorController.this.mRateMillis);
                        return;
                    }
                    return;
                default:
                    Slog.e(HwLightSensorController.TAG, "Invalid message");
                    return;
            }
        }
    };
    private int mLastSensorValue = -1;
    private Sensor mLightSensor;
    private int mRateMillis = 300;
    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (HwLightSensorController.this.mEnable) {
                int lux = (int) event.values[0];
                long timeStamp = event.timestamp / 1000000;
                if (!HwLightSensorController.this.mWarmUpFlg) {
                    HwLightSensorController.this.setSensorData(lux);
                } else if (timeStamp < HwLightSensorController.this.mEnableTime) {
                    if (HwLightSensorController.DEBUG) {
                        Slog.i(HwLightSensorController.TAG, "sensor not ready yet");
                    }
                } else {
                    HwLightSensorController.this.setSensorData(lux);
                    HwLightSensorController.this.mWarmUpFlg = false;
                    HwLightSensorController.this.mHandler.sendEmptyMessage(1);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorManager mSensorManager;
    private boolean mWarmUpFlg = true;

    public interface LightSensorCallbacks {
        void processSensorData(long j, int i);
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwLightSensorController(LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis) {
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mRateMillis = sensorRateMillis;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mDataList = new ArrayList();
    }

    public void enableSensor() {
        if (!this.mEnable) {
            this.mEnable = true;
            this.mWarmUpFlg = true;
            this.mEnableTime = SystemClock.elapsedRealtime();
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mLightSensor, this.mRateMillis * 1000);
        }
    }

    public void disableSensor() {
        if (this.mEnable) {
            this.mEnable = false;
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mHandler.removeMessages(1);
            clearSensorData();
        }
    }

    private void setSensorData(int lux) {
        synchronized (this.mDataList) {
            this.mDataList.add(Integer.valueOf(lux));
        }
    }

    private int getSensorData() {
        synchronized (this.mDataList) {
            if (this.mDataList.isEmpty()) {
                int i = this.mLastSensorValue;
                return i;
            }
            int count = 0;
            int sum = 0;
            for (Integer data : this.mDataList) {
                sum += data.intValue();
                count++;
            }
            if (count != 0) {
                int average = sum / count;
                if (average >= 0) {
                    this.mLastSensorValue = average;
                }
            }
            this.mDataList.clear();
            i = this.mLastSensorValue;
            return i;
        }
    }

    private void clearSensorData() {
        synchronized (this.mDataList) {
            this.mDataList.clear();
            this.mLastSensorValue = -1;
        }
    }
}
