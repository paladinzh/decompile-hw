package com.amap.api.mapcore.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.autonavi.amap.mapcore.MapConfig;

/* compiled from: SensorEventHelper */
public class u implements SensorEventListener {
    private SensorManager a;
    private Sensor b;
    private long c = 0;
    private final int d = 100;
    private float e;
    private Context f;
    private l g;
    private Marker h;

    public u(Context context, l lVar) {
        this.f = context.getApplicationContext();
        this.g = lVar;
        this.a = (SensorManager) context.getSystemService("sensor");
        this.b = this.a.getDefaultSensor(3);
    }

    public void a() {
        this.a.registerListener(this, this.b, 3);
    }

    public void b() {
        this.a.unregisterListener(this, this.b);
    }

    public void a(Marker marker) {
        this.h = marker;
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        int i;
        if (System.currentTimeMillis() - this.c >= 100) {
            i = 1;
        } else {
            i = 0;
        }
        if (i != 0) {
            if (this.g.a() == null || this.g.a().getAnimateionsCount() <= 0) {
                switch (sensorEvent.sensor.getType()) {
                    case 3:
                        float a = (sensorEvent.values[0] + ((float) a(this.f))) % 360.0f;
                        if (a > BitmapDescriptorFactory.HUE_CYAN) {
                            a -= 360.0f;
                        } else if (a < -180.0f) {
                            a += 360.0f;
                        }
                        if (Math.abs(this.e - a) >= MapConfig.MIN_ZOOM) {
                            if (Float.isNaN(a)) {
                                a = 0.0f;
                            }
                            this.e = a;
                            if (this.h != null) {
                                try {
                                    this.g.a(ag.d(this.e));
                                    this.h.setRotateAngle(-this.e);
                                } catch (Throwable th) {
                                    th.printStackTrace();
                                }
                            }
                            this.c = System.currentTimeMillis();
                            break;
                        }
                        break;
                }
            }
        }
    }

    public static int a(Context context) {
        switch (((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation()) {
            case 0:
                return 0;
            case 1:
                return 90;
            case 2:
                return 180;
            case 3:
                return -90;
            default:
                return 0;
        }
    }
}
