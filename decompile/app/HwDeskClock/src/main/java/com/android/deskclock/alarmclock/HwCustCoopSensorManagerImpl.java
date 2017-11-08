package com.android.deskclock.alarmclock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import com.android.deskclock.MotionManager.MotionListener;

public class HwCustCoopSensorManagerImpl extends HwCustCoopSensorManager {
    private boolean mIsRegister;
    private float mLastZ;
    private SensorManager mManager;
    private MotionListener mMotionListener;
    private Sensor mSensor;
    private SensorEventListener sensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (HwCustCoopSensorManagerImpl.this.isTurn(event.values[0], event.values[1], event.values[2])) {
                HwCustCoopSensorManagerImpl.this.doTurnSlenit();
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }
    };

    public boolean isCoop() {
        return SystemProperties.getBoolean("ro.config.hw_coop.gesture", false);
    }

    public void startListener(Context context, MotionListener listener) {
        if (SystemProperties.getBoolean("persist.sys.flip.clock.enabled", false)) {
            this.mManager = (SensorManager) context.getSystemService("sensor");
            this.mSensor = this.mManager.getDefaultSensor(1);
            this.mMotionListener = listener;
            this.mManager.registerListener(this.sensorListener, this.mSensor, 3);
            this.mIsRegister = true;
        }
    }

    public boolean isRegister() {
        return this.mIsRegister;
    }

    public void clear() {
        if (this.mManager != null) {
            this.mManager.unregisterListener(this.sensorListener);
        }
        this.mLastZ = 0.0f;
        this.mIsRegister = false;
    }

    private void doTurnSlenit() {
        if (this.mMotionListener != null) {
            this.mMotionListener.flipMute();
        }
    }

    private boolean isTurn(float x, float y, float z) {
        if (this.mLastZ == 0.0f) {
            this.mLastZ = z;
        }
        if (Float.compare(Math.abs(z) + Math.abs(this.mLastZ), Math.abs(this.mLastZ + z)) == 0 || Float.compare(Math.abs(x), 4.0f) >= 0) {
            return false;
        }
        return true;
    }
}
