package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.huawei.watermark.controller.callback.PressureValueChangeListener;
import com.huawei.watermark.controller.callback.SensorProcessorListener;

public class SensorProcessor implements SensorProcessorListener {
    protected static final String TAG = ("WM_" + SensorProcessor.class.getSimpleName());
    private Context mContext;
    private boolean mHasPressureSensor = false;
    SensorEventListener mPressureListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            SensorProcessor.this.mPressureValue = event.values[0];
            if (SensorProcessor.this.mPressureValueChangeListener != null) {
                SensorProcessor.this.mPressureValueChangeListener.onPressureValueChanged(SensorProcessor.this.mPressureValue);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private Sensor mPressureSensor;
    private float mPressureValue;
    private PressureValueChangeListener mPressureValueChangeListener;
    private boolean mSensorHasRegister = false;
    private SensorManager mSensorManager = null;

    public SensorProcessor(Context context) {
        this.mContext = context;
        this.mHasPressureSensor = getSensorValid();
    }

    private boolean getSensorValid() {
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        this.mPressureSensor = this.mSensorManager.getDefaultSensor(6);
        if (this.mPressureSensor == null) {
            return false;
        }
        return true;
    }

    public boolean hasRegistered() {
        return this.mSensorHasRegister;
    }

    public void registerSensor() {
        if (this.mHasPressureSensor && this.mSensorManager != null) {
            Log.d(TAG, "Altitude watermark,registerSensor");
            this.mSensorManager.registerListener(this.mPressureListener, this.mPressureSensor, 1000000);
            this.mSensorHasRegister = true;
        }
    }

    public void unRegisterSensor() {
        if (this.mSensorManager != null) {
            Log.d(TAG, "Altitude watermark,unRegisterSensor");
            this.mSensorManager.unregisterListener(this.mPressureListener);
        }
        this.mSensorHasRegister = false;
    }

    public void setPressureValueChangeListener(PressureValueChangeListener listener) {
        this.mPressureValueChangeListener = listener;
    }
}
