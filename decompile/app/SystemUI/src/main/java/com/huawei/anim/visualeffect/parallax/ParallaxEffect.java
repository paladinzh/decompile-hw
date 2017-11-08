package com.huawei.anim.visualeffect.parallax;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Interpolator;
import com.huawei.anim.visualeffect.SensorEffect;
import com.huawei.anim.visualeffect.SensorEffect.Listener;
import com.huawei.anim.visualeffect.SensorEffect.RequestSensorInfo;
import java.util.ArrayList;
import java.util.List;

public class ParallaxEffect implements SensorEffect, SensorEventListener {
    private boolean mEnable;
    private final Interpolator mInterpolatorX;
    private final Interpolator mInterpolatorY;
    private double[] mKalmanGain;
    private double[] mLastAngles;
    private Listener mListener;
    private double[] mP;
    private double[] mResult;
    private final List<RequestSensorInfo> mSensorInfos;
    private final SensorManager mSensorManager;
    private boolean mSensorRegistered;
    private long mTimeStamp;

    public ParallaxEffect(Context context) {
        this(context, 30000);
    }

    public ParallaxEffect(Context context, int sampleRate) {
        this.mSensorRegistered = false;
        this.mEnable = true;
        this.mTimeStamp = 0;
        this.mResult = new double[]{0.0d, 0.0d, 0.0d};
        this.mLastAngles = new double[]{0.0d, 0.0d, 0.0d};
        this.mKalmanGain = new double[]{0.0d, 0.0d, 0.0d};
        this.mP = new double[]{0.001d, 0.001d, 0.001d};
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensorInfos = new ArrayList();
        RequestSensorInfo info = new RequestSensorInfo();
        info.rate = sampleRate;
        info.type = 4;
        this.mSensorInfos.add(info);
        this.mInterpolatorX = new ParallaxInterpolator(0.7853981633974483d, 0.1d);
        this.mInterpolatorY = new ParallaxInterpolator(0.7853981633974483d, 0.1d);
    }

    public void onSensorChanged(SensorEvent event) {
        if (this.mListener != null) {
            switch (event.sensor.getType()) {
                case 4:
                    double dT = 0.0d;
                    if (this.mTimeStamp != 0) {
                        dT = ((double) (event.timestamp - this.mTimeStamp)) * 1.0E-9d;
                    } else {
                        double[] dArr = this.mLastAngles;
                        this.mResult[0] = 0.0d;
                        dArr[0] = 0.0d;
                        dArr = this.mLastAngles;
                        this.mResult[1] = 0.0d;
                        dArr[1] = 0.0d;
                        dArr = this.mLastAngles;
                        this.mResult[2] = 0.0d;
                        dArr[2] = 0.0d;
                    }
                    this.mTimeStamp = event.timestamp;
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];
                    if (Math.sqrt((double) (((axisX * axisX) + (axisY * axisY)) + (axisZ * axisZ))) > 0.13d) {
                        double px = this.mLastAngles[0];
                        double py = this.mLastAngles[1];
                        double pz = this.mLastAngles[2];
                        double pxk = this.mP[0] + 7.225E-7d;
                        double pyk = this.mP[1] + 7.225E-7d;
                        double pzk = this.mP[2] + 7.225E-7d;
                        this.mKalmanGain[0] = pxk / (2.2499999999999996E-8d + pxk);
                        this.mKalmanGain[1] = pyk / (2.2499999999999996E-8d + pyk);
                        this.mKalmanGain[2] = pzk / (2.2499999999999996E-8d + pzk);
                        double my = ((double) axisY) * dT;
                        double mz = ((double) axisZ) * dT;
                        this.mLastAngles[0] = (((((double) axisX) * dT) - px) * this.mKalmanGain[0]) + px;
                        this.mLastAngles[1] = ((my - py) * this.mKalmanGain[1]) + py;
                        this.mLastAngles[2] = ((mz - pz) * this.mKalmanGain[2]) + pz;
                        this.mP[0] = (1.0d - this.mKalmanGain[0]) * this.mP[0];
                        this.mP[1] = (1.0d - this.mKalmanGain[1]) * this.mP[1];
                        this.mP[2] = (1.0d - this.mKalmanGain[2]) * this.mP[2];
                        this.mResult[0] = (this.mResult[0] + this.mLastAngles[0]) % 3.141592653589793d;
                        this.mResult[1] = (this.mResult[1] + this.mLastAngles[1]) % 3.141592653589793d;
                        this.mResult[2] = (this.mResult[2] + this.mLastAngles[2]) % 3.141592653589793d;
                    } else if (this.mResult[0] != 0.0d || this.mResult[1] != 0.0d) {
                        if (this.mResult[0] > 0.0d) {
                            this.mResult[0] = Math.max(0.0d, this.mResult[0] - 0.0013d);
                        } else {
                            this.mResult[0] = Math.min(0.0d, this.mResult[0] + 0.0013d);
                        }
                        if (this.mResult[1] > 0.0d) {
                            this.mResult[1] = Math.max(0.0d, this.mResult[1] - 0.0013d);
                        } else {
                            this.mResult[1] = Math.min(0.0d, this.mResult[1] + 0.0013d);
                        }
                        if (this.mResult[2] > 0.0d) {
                            this.mResult[2] = Math.max(0.0d, this.mResult[2] - 0.0013d);
                        } else {
                            this.mResult[2] = Math.min(0.0d, this.mResult[2] + 0.0013d);
                        }
                    } else {
                        return;
                    }
                    this.mListener.onChanged(500, new float[]{this.mInterpolatorX.getInterpolation((float) this.mResult[1]), this.mInterpolatorY.getInterpolation((float) this.mResult[0])});
                    break;
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
