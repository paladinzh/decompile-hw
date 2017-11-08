package com.huawei.keyguard.support;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import com.huawei.keyguard.util.HwLog;

public class HwSensorManager implements SensorEventListener {
    private float mAngleDeltaX;
    private float mAngleDeltaY;
    private float mAngleRevisedY;
    private float mAngleX;
    private float mAngleY;
    private float mAngularSpeedThreshold;
    private float mChangedAngleThreshold;
    private int mDelayUs = 30000;
    private int mDirection = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (HwSensorManager.this.mSensorEventDetector != null) {
                        HwSensorManager.this.mSensorEventDetector.onSwing();
                    }
                    HwSensorManager.this.mAngleX = 0.0f;
                    HwSensorManager.this.mAngleY = 0.0f;
                    HwSensorManager.this.mAngleRevisedY = 0.0f;
                    return;
                case 2:
                    HwSensorManager.this.mAngleX = 0.0f;
                    HwSensorManager.this.mAngleY = 0.0f;
                    HwSensorManager.this.mAngleRevisedY = 0.0f;
                    removeMessages(2);
                    return;
                default:
                    return;
            }
        }
    };
    SensorEventDetector mSensorEventDetector;
    private SensorManager mSensorManager;
    private int mSwingDelay = 600;
    private float mSwingThreshold;
    private long mTimeStamp;
    private int mType;

    public interface SensorEventDetector {
        void onCorrect();

        void onDirectionChanged(int i);

        void onSwing();

        void onTiltToMove(float f, float f2);
    }

    public HwSensorManager(Context context) {
        if (context == null) {
            HwLog.w("HwSensorManager", "SensorDataManager() context is null");
            return;
        }
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        if (this.mSensorManager == null) {
            HwLog.w("HwSensorManager", "Cannot get system service : sensor");
            return;
        }
        this.mChangedAngleThreshold = 0.7853982f;
        this.mAngularSpeedThreshold = 3.1415927f;
        this.mSwingThreshold = 14.0f;
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case 1:
                float[] values = event.values;
                boolean isSwing = false;
                if (Math.abs(values[0]) <= this.mSwingThreshold && Math.abs(values[1]) <= this.mSwingThreshold) {
                    if (Math.abs(values[2]) > this.mSwingThreshold) {
                    }
                    if (isSwing && this.mSensorEventDetector != null && this.mHandler != null) {
                        this.mHandler.removeMessages(1);
                        this.mHandler.sendEmptyMessageDelayed(1, (long) this.mSwingDelay);
                        return;
                    }
                    return;
                }
                isSwing = true;
                if (isSwing) {
                    return;
                }
                return;
            case 4:
                float gyro_wx = event.values[0];
                float gyro_wy = event.values[1];
                float gyro_wz = event.values[2];
                if (this.mTimeStamp != 0) {
                    float dT = (float) (event.timestamp - this.mTimeStamp);
                    this.mAngleDeltaY = (gyro_wy * dT) * 1.0E-9f;
                    this.mAngleDeltaX = (gyro_wx * dT) * 1.0E-9f;
                }
                this.mTimeStamp = event.timestamp;
                if (Math.abs(gyro_wy) >= 0.05f || Math.abs(gyro_wx) >= 0.05f || Math.abs(gyro_wz) >= 0.05f || this.mSensorEventDetector == null) {
                    boolean directionChanged;
                    if (this.mHandler != null) {
                        this.mHandler.removeMessages(2);
                    }
                    this.mAngleY += this.mAngleDeltaY;
                    this.mAngleX += this.mAngleDeltaX;
                    this.mAngleY = getAngleLagerThanPI(this.mAngleY);
                    this.mAngleX = getAngleLagerThanPI(this.mAngleX);
                    float angle_y = reviseAngleY(this.mAngleY, this.mAngleX);
                    if (!((this.mType & 8) == 0 || this.mSensorEventDetector == null)) {
                        this.mSensorEventDetector.onTiltToMove(this.mAngleDeltaY, this.mAngleDeltaX);
                    }
                    if ((this.mType & 4) != 0) {
                        gyro_wx = 0.0f;
                    } else if ((this.mType & 2) != 0) {
                        gyro_wy = 0.0f;
                    }
                    if (this.mHandler == null || this.mHandler.hasMessages(1)) {
                        directionChanged = false;
                    } else {
                        directionChanged = getDirectionChanged(gyro_wy, gyro_wx, angle_y, this.mAngleX);
                    }
                    if (directionChanged && this.mSensorEventDetector != null) {
                        this.mSensorEventDetector.onDirectionChanged(this.mDirection);
                        return;
                    }
                    return;
                }
                this.mSensorEventDetector.onCorrect();
                if (this.mHandler == null) {
                    return;
                }
                if (this.mAngleX != 0.0f || this.mAngleY != 0.0f) {
                    this.mHandler.sendEmptyMessageDelayed(2, 100);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setMoveDirection(int direction) {
        if (direction != this.mDirection) {
            this.mDirection = direction;
        }
    }

    private boolean getDirectionChanged(float wy, float wx, float angley, float anglex) {
        int direction = 0;
        if (Math.abs(wy) > Math.abs(wx)) {
            if (angley > this.mChangedAngleThreshold && wy > this.mAngularSpeedThreshold) {
                direction = 2;
            } else if (angley < (-this.mChangedAngleThreshold) && wy < (-this.mAngularSpeedThreshold)) {
                direction = 1;
            }
        } else if (anglex > this.mChangedAngleThreshold && wx > this.mAngularSpeedThreshold) {
            direction = 4;
        } else if (anglex < (-this.mChangedAngleThreshold) && wx < (-this.mAngularSpeedThreshold)) {
            direction = 3;
        }
        if (direction == 0 || direction == this.mDirection) {
            return false;
        }
        this.mDirection = direction;
        return true;
    }

    private float reviseAngleY(float angley, float anglex) {
        if (Math.abs(Math.abs(anglex) - 1.5707964f) >= 0.17453294f) {
            this.mAngleRevisedY = angley;
        }
        return this.mAngleRevisedY;
    }

    private float getAngleLagerThanPI(float angle) {
        float temp = angle;
        if (((double) angle) > 3.141592653589793d) {
            return 0.0f;
        }
        if (((double) angle) <= -3.141592653589793d) {
            return 0.0f;
        }
        return temp;
    }
}
