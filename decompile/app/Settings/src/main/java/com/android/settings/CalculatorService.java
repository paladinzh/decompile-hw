package com.android.settings;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class CalculatorService extends Service {
    private CalculatorModel mCalculatorModel;
    private boolean mRegisted = false;
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 19) {
                CalculatorService.this.mCalculatorModel.onSensorChanged((long) event.values[0]);
            }
        }
    };
    private SensorManager mSensorManager = null;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d("CalculatorService", "onCreate()");
        this.mCalculatorModel = CalculatorModel.getInstance(this);
        if (this.mCalculatorModel.getCalculatorEnable()) {
            this.mSensorManager = (SensorManager) getSystemService("sensor");
            registerReceiver();
            this.mRegisted = true;
            return;
        }
        Log.w("CalculatorService", "CalculatorEnable set to disable after service killed!");
        onDestroy();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("CalculatorService", "onDestroy()");
        if (this.mRegisted) {
            unRegisterReceiver();
            this.mRegisted = false;
        }
    }

    private void registerReceiver() {
        this.mSensorManager.registerListener(this.mSensorListener, this.mSensorManager.getDefaultSensor(19), 0);
    }

    private void unRegisterReceiver() {
        this.mSensorManager.unregisterListener(this.mSensorListener, this.mSensorManager.getDefaultSensor(19));
    }
}
