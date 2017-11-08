package com.fyusion.sdk.viewer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import com.fyusion.sdk.common.FyuseSDK;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: Unknown */
public class c {
    private static float[] h = new float[3];
    private static HandlerThread j = new HandlerThread("MotionManager", 10);
    int a;
    boolean b;
    boolean c;
    private SensorManager d;
    private final a e;
    private CopyOnWriteArrayList<b> f;
    private long g;
    private int i;
    private SensorEventListener k;

    /* compiled from: Unknown */
    public static class a {
        public float a = 0.0f;
        public float b = 0.0f;
        public float c = 0.0f;
        public long d = 0;
        public double e = 0.0d;
        public boolean f = false;
    }

    /* compiled from: Unknown */
    private static class b {
        static final c a = new c();
    }

    private c() {
        this.f = new CopyOnWriteArrayList();
        this.g = 0;
        this.i = -1;
        this.a = 0;
        this.b = false;
        this.c = false;
        this.d = (SensorManager) FyuseSDK.getContext().getSystemService("sensor");
        this.e = new a();
        if (!j.isAlive()) {
            j.start();
        }
    }

    public static c a() {
        return b.a;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void a(int i) {
        if (this.i != i) {
            this.i = i;
            SensorEventListener sensorEventListener = this.k;
            this.k = c();
            if (i >= 0) {
                this.d.registerListener(this.k, this.d.getDefaultSensor(4), i, new Handler(j.getLooper()));
            }
            if (sensorEventListener != null) {
                this.d.unregisterListener(sensorEventListener);
            }
        }
    }

    private float[] a(float[] fArr, float[] fArr2) {
        if (fArr2 == null) {
            return fArr;
        }
        for (int i = 0; i < fArr.length; i++) {
            fArr2[i] = fArr2[i] + ((fArr[i] - fArr2[i]) * 0.5f);
        }
        return fArr2;
    }

    private SensorEventListener c() {
        return new SensorEventListener(this) {
            final /* synthetic */ c a;
            private long b = 0;
            private boolean c = true;

            {
                this.a = r3;
            }

            public void onAccuracyChanged(Sensor sensor, int i) {
            }

            public void onSensorChanged(SensorEvent sensorEvent) {
                if (this.b <= 20) {
                    this.a.g = 0;
                    System.arraycopy(sensorEvent.values, 0, c.h, 0, sensorEvent.values.length);
                    this.b++;
                    return;
                }
                this.a.a(sensorEvent.values, c.h);
                this.b++;
                if (this.a.g == 0) {
                    this.a.g = sensorEvent.timestamp;
                    return;
                }
                this.a.e.c = c.h[2];
                this.a.e.b = c.h[0];
                this.a.e.a = c.h[1];
                if (((double) ((Math.abs(c.h[0]) + Math.abs(c.h[1])) + Math.abs(c.h[2]))) > 0.009999999776482582d) {
                    this.a.a = 0;
                    if (this.a.b && !this.a.c) {
                        this.a.b = false;
                        this.a.a(15000);
                    }
                } else {
                    c cVar = this.a;
                    cVar.a++;
                    if (this.a.a > 100) {
                        this.a.a = 0;
                        this.a.b = true;
                        this.a.a(75000);
                    }
                }
                long j = sensorEvent.timestamp;
                this.a.e.d = j;
                this.a.e.e = (((double) j) - ((double) this.a.g)) / 1.0E9d;
                this.a.g = j;
                if (this.c) {
                    this.a.e.f = true;
                    this.c = false;
                }
                Iterator it = this.a.f.iterator();
                while (it.hasNext()) {
                    ((b) it.next()).a(this.a.e);
                }
            }
        };
    }

    public synchronized void a(b bVar) {
        if (!this.f.contains(bVar)) {
            this.f.add(bVar);
        }
        if (this.f.size() == 1) {
            if (j.isAlive()) {
                a(15000);
            }
        }
    }

    public void a(boolean z) {
        if (z != this.c) {
            this.c = z;
            if (!z) {
                this.b = false;
                a(15000);
            } else if (!this.b) {
                this.b = true;
                a(200000);
            }
        }
    }

    public synchronized void b(b bVar) {
        this.f.remove(bVar);
        if (this.f.isEmpty()) {
            this.d.unregisterListener(this.k);
            this.g = 0;
            this.i = -1;
        }
    }
}
