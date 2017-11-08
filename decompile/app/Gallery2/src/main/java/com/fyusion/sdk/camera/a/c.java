package com.fyusion.sdk.camera.a;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.WindowManager;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.fyusion.sdk.camera.FyuseCamera.RotationDirection;
import com.fyusion.sdk.camera.MotionHints;
import com.fyusion.sdk.camera.MotionHints.Hint;
import com.fyusion.sdk.camera.MotionHintsListener;
import com.fyusion.sdk.camera.RecordingProgressListener;
import com.fyusion.sdk.camera.impl.n;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.m;
import com.huawei.watermark.manager.parse.WMElement;
import fyusion.vislib.Date;
import fyusion.vislib.FloatVec;
import fyusion.vislib.FyuseFrameInformation;
import fyusion.vislib.FyuseFrameInformationVec;
import fyusion.vislib.FyusePlacemark;
import fyusion.vislib.FyuseSize;
import fyusion.vislib.FyuseWrapper;
import fyusion.vislib.ImuProgressEstimatorWrapper;
import fyusion.vislib.OnlineImageStabilizerWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/* compiled from: Unknown */
public class c {
    private static final int[] aP = new int[]{10, 1};
    private static final int[] aQ = new int[]{9, 10, 1};
    private static final int[] aR = new int[]{2, 14};
    private static final int[] aS = new int[]{4, 16};
    int A = 0;
    int B = 0;
    int C = 0;
    int D = 0;
    long E;
    float[] F = null;
    private boolean G;
    private int H;
    private float I;
    private float J;
    private AtomicBoolean K = new AtomicBoolean(false);
    private float L;
    private float M;
    private float N;
    private float O;
    private float P;
    private float Q;
    private float R;
    private float S;
    private float T;
    private float U;
    private int V;
    private List<e> W;
    private List<m> X;
    private com.fyusion.sdk.common.ext.e Y;
    private com.fyusion.sdk.camera.impl.m Z;
    int a;
    private int aA;
    private int aB;
    private boolean aC;
    private int aD;
    private boolean aE;
    private boolean aF;
    private int aG;
    private boolean aH = false;
    private boolean aI = false;
    private n aJ;
    private d aK;
    private final SensorEventListener aL = new SensorEventListener(this) {
        final /* synthetic */ c a;

        {
            this.a = r1;
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (this.a.x) {
                if (this.a.ae == null) {
                    this.a.ae = new m();
                }
                if (!this.a.w) {
                    this.a.w = true;
                }
                if (!this.a.ah) {
                    float a;
                    float f;
                    float[] fArr;
                    float[] fArr2;
                    if (this.a.c && sensorEvent.sensor.getType() == this.a.am) {
                        a = this.a.a(sensorEvent);
                        if (this.a.av) {
                            DLog.e("progressEstimate", "Current progress: " + a);
                        }
                    }
                    Sensor sensor = sensorEvent.sensor;
                    long j = sensorEvent.timestamp;
                    long j2 = sensorEvent.timestamp / 1000000;
                    int type = sensor.getType();
                    if (type == this.a.ao) {
                        f = sensorEvent.values[0];
                        float f2 = sensorEvent.values[1];
                        float f3 = sensorEvent.values[2];
                        this.a.ak = this.a.aj;
                        this.a.aj = (float) Math.sqrt((double) (((f * f) + (f2 * f2)) + (f3 * f3)));
                        this.a.ai = (this.a.aj - this.a.ak) + (this.a.ai * 0.9f);
                        this.a.ae.c.a = sensorEvent.values[0];
                        this.a.ae.c.b = sensorEvent.values[1];
                        this.a.ae.c.c = sensorEvent.values[2];
                        this.a.ae.d = j2;
                        this.a.ae.e = j;
                        this.a.af.a = true;
                    }
                    if (type == this.a.ap && this.a.t) {
                        this.a.v = sensorEvent.values;
                        this.a.ae.n = sensorEvent.values;
                        this.a.ae.o = j2;
                        this.a.ae.p = j;
                        this.a.af.d = true;
                    }
                    if (type == this.a.aq) {
                        this.a.ae.i.a = sensorEvent.values[0];
                        this.a.ae.i.b = sensorEvent.values[1];
                        this.a.ae.i.c = sensorEvent.values[2];
                        this.a.af.e = true;
                    }
                    if (type == this.a.an) {
                        this.a.u = sensorEvent.values;
                        this.a.ae.f.a = (-sensorEvent.values[0]) / 9.80665f;
                        this.a.ae.f.b = (-sensorEvent.values[1]) / 9.80665f;
                        this.a.ae.f.c = (-sensorEvent.values[2]) / 9.80665f;
                        this.a.ae.g = j2;
                        this.a.ae.h = j;
                        this.a.af.b = true;
                    }
                    if (type == this.a.am && !this.a.ar && !this.a.af.c) {
                        if (sensorEvent.values.length <= 4) {
                            SensorManager.getRotationMatrixFromVector(this.a.ae.j, sensorEvent.values);
                        } else {
                            if (this.a.ag == null) {
                                this.a.ag = new float[4];
                            }
                            System.arraycopy(sensorEvent.values, 0, this.a.ag, 0, 4);
                            SensorManager.getRotationMatrixFromVector(this.a.ae.j, this.a.ag);
                        }
                        fArr = new float[3];
                        SensorManager.getOrientation(this.a.ae.j, fArr);
                        c.b(this.a.ae.j, fArr);
                        this.a.ae.l = j2;
                        this.a.ae.m = j;
                        this.a.af.c = true;
                    } else if (!(!this.a.ar || this.a.u == null || this.a.v == null || this.a.af.c)) {
                        if (this.a.av) {
                            DLog.d("onSensorChanged", "GOT IGNORE_GYRO INFO!");
                        }
                        fArr = new float[9];
                        if (SensorManager.getRotationMatrix(fArr, new float[9], this.a.u, this.a.v)) {
                            fArr2 = new float[3];
                            SensorManager.getOrientation(fArr, fArr2);
                            this.a.ae.j[0] = fArr[0];
                            this.a.ae.j[1] = fArr[1];
                            this.a.ae.j[2] = fArr[2];
                            this.a.ae.j[3] = 0.0f;
                            this.a.ae.j[4] = fArr[3];
                            this.a.ae.j[5] = fArr[4];
                            this.a.ae.j[6] = fArr[5];
                            this.a.ae.j[7] = 0.0f;
                            this.a.ae.j[8] = fArr[6];
                            this.a.ae.j[9] = fArr[7];
                            this.a.ae.j[10] = fArr[8];
                            this.a.ae.j[11] = 0.0f;
                            this.a.ae.j[12] = 0.0f;
                            this.a.ae.j[13] = 0.0f;
                            this.a.ae.j[14] = 0.0f;
                            this.a.ae.j[15] = WMElement.CAMERASIZEVALUE1B1;
                            c.b(this.a.ae.j, fArr2);
                            this.a.ae.l = j2;
                            this.a.ae.m = j;
                            this.a.af.c = true;
                            this.a.u = null;
                            this.a.v = null;
                        }
                    }
                    if (this.a.af.c && this.a.af.b && this.a.af.a) {
                        if (!this.a.t || this.a.af.d) {
                            this.a.ah = true;
                            this.a.af.c = false;
                            this.a.af.b = false;
                            this.a.af.a = false;
                            if (this.a.af.d) {
                                this.a.af.d = false;
                            }
                            if (this.a.af.e) {
                                this.a.af.e = false;
                            }
                            this.a.ae.a = ((double) this.a.ae.l) / 1000.0d;
                            if (!this.a.c) {
                                if (this.a.av) {
                                    DLog.d("onSensorChanged", "imu has NOT stabilized");
                                }
                                FloatVec floatVec = new FloatVec();
                                FloatVec floatVec2 = new FloatVec(16);
                                for (type = 0; type < 16; type++) {
                                    floatVec2.set(type, this.a.ae.j[type]);
                                }
                                FyuseWrapper.getAngleAxis(floatVec2, floatVec);
                                a = floatVec.get(2);
                                float f4 = floatVec.get(0);
                                f = -floatVec.get(1);
                                if (this.a.Z == null) {
                                    if (this.a.av) {
                                        DLog.d("onSensorChanged", "mPrevSampleForStabilization == null");
                                    }
                                    this.a.Z = new com.fyusion.sdk.camera.impl.m();
                                    this.a.Z.c = a;
                                    this.a.Z.b = f4;
                                    this.a.Z.a = f;
                                    this.a.ah = false;
                                    return;
                                } else if (c.b(f, this.a.Z.a, 0.1f) && c.b(f4, this.a.Z.b, 0.1f) && c.b(a, this.a.Z.c, 0.1f)) {
                                    this.a.Z.c = a;
                                    this.a.Z.b = f4;
                                    this.a.Z.a = f;
                                    c cVar = this.a;
                                    cVar.m++;
                                    if (this.a.m <= 5) {
                                        this.a.ah = false;
                                        return;
                                    }
                                    this.a.c = true;
                                    if (this.a.av) {
                                        DLog.d("onSensorChanged", "IMU Has stabilized!");
                                    }
                                } else {
                                    this.a.Z.c = a;
                                    this.a.Z.b = f4;
                                    this.a.Z.a = f;
                                    this.a.m = 0;
                                    this.a.ah = false;
                                    return;
                                }
                            }
                            if (this.a.aa == null) {
                                this.a.aa = (float[]) this.a.ae.j.clone();
                                this.a.ac = (float[]) this.a.aa.clone();
                                Matrix.invertM(this.a.ac, 0, this.a.aa, 0);
                                this.a.k = Math.abs(this.a.ae.f.a) < Math.abs(this.a.ae.f.b);
                                this.a.l = true;
                            }
                            fArr = (float[]) this.a.ae.j.clone();
                            Matrix.multiplyMM(fArr, 0, (float[]) this.a.ae.j.clone(), 0, this.a.ac, 0);
                            com.fyusion.sdk.camera.impl.m mVar = new com.fyusion.sdk.camera.impl.m();
                            Matrix.setIdentityM(new float[fArr.length], 0);
                            FloatVec floatVec3 = new FloatVec();
                            FloatVec floatVec4 = new FloatVec(16);
                            for (int i = 0; i < 16; i++) {
                                floatVec4.set(i, fArr[i]);
                            }
                            FyuseWrapper.getAngleAxis(floatVec4, floatVec3);
                            mVar.c = floatVec3.get(2);
                            mVar.b = floatVec3.get(0);
                            mVar.a = -floatVec3.get(1);
                            m mVar2 = new m();
                            mVar2.j = fArr;
                            mVar2.k.c = mVar.c;
                            mVar2.k.b = mVar.b;
                            mVar2.k.a = mVar.a;
                            mVar2.c = this.a.ae.c;
                            mVar2.f = this.a.ae.f;
                            if (this.a.ad != null) {
                                com.fyusion.sdk.common.ext.n nVar;
                                while (((double) Math.abs(this.a.ad.k.a - mVar2.k.a)) > 3.141592653589793d) {
                                    nVar = mVar2.k;
                                    nVar.a = (float) (((double) nVar.a) + (((double) ((this.a.ad.k.a > mVar2.k.a ? 1 : -1) * 2)) * 3.141592653589793d));
                                }
                                while (((double) Math.abs(this.a.ad.k.b - mVar2.k.b)) > 3.141592653589793d) {
                                    nVar = mVar2.k;
                                    nVar.b = (float) (((double) nVar.b) + (((double) ((this.a.ad.k.b > mVar2.k.b ? 1 : -1) * 2)) * 3.141592653589793d));
                                }
                                while (((double) Math.abs(this.a.ad.k.c - mVar2.k.c)) > 3.141592653589793d) {
                                    nVar = mVar2.k;
                                    nVar.c = (float) (((double) nVar.c) + (((double) ((this.a.ad.k.c > mVar2.k.c ? 1 : -1) * 2)) * 3.141592653589793d));
                                }
                            }
                            mVar2.i = this.a.ae.i;
                            mVar2.n = this.a.ae.n;
                            mVar2.a = this.a.ae.a;
                            if (this.a.aK.f == 0) {
                                this.a.aK.f = (long) (this.a.ae.a * 1000.0d);
                            }
                            this.a.aK.g = (long) (this.a.ae.a * 1000.0d);
                            com.fyusion.sdk.common.ext.m.a aVar = this.a.ae.c;
                            if (this.a.x) {
                                this.a.ad = mVar2;
                                if (this.a.X.size() > 10) {
                                    this.a.I = mVar2.k.a;
                                    this.a.i = mVar2.k.b;
                                    f = (float) Math.sqrt((((double) this.a.I) * ((double) this.a.I)) + (((double) this.a.i) * ((double) this.a.i)));
                                    this.a.I = this.a.I / f;
                                    c cVar2 = this.a;
                                    cVar2.i /= f;
                                    if (Float.isNaN(this.a.I) || Float.isNaN(this.a.i)) {
                                        this.a.I = WMElement.CAMERASIZEVALUE1B1;
                                        this.a.i = 0.0f;
                                    }
                                    if (this.a.av) {
                                        DLog.d("onSensorChanged", "mImuDirectionX: " + this.a.I + " mImuDirectionY: " + this.a.i);
                                    }
                                    if (!this.a.h) {
                                        DLog.d("imuDirection", "mImuDirectionHasSettled");
                                        this.a.h = true;
                                    }
                                }
                                if (this.a.ab == null) {
                                    this.a.ab = this.a.aa;
                                }
                                float[] fArr3 = (float[]) this.a.ab.clone();
                                fArr2 = (float[]) fArr.clone();
                                Matrix.invertM(fArr3, 0, this.a.ab, 0);
                                Matrix.multiplyMM(fArr2, 0, fArr, 0, fArr3, 0);
                                floatVec3 = new FloatVec();
                                floatVec4 = new FloatVec(16);
                                for (int i2 = 0; i2 < 16; i2++) {
                                    floatVec4.set(i2, fArr2[i2]);
                                }
                                FyuseWrapper.getAngleAxis(floatVec4, floatVec3);
                                e eVar = new e();
                                if (this.a.W.size() < 1) {
                                    eVar.a = 0.0f;
                                    eVar.b = 0.0f;
                                } else {
                                    e eVar2 = (e) this.a.W.get(this.a.W.size() - 1);
                                    eVar.a = (eVar2.a - (floatVec3.get(1) * this.a.I)) + (floatVec3.get(0) * this.a.i);
                                    eVar.b = (eVar2.b - (floatVec3.get(1) * this.a.i)) - (floatVec3.get(0) * this.a.I);
                                }
                                if (Float.isNaN(eVar.a)) {
                                    DLog.w("InternalProcessor", "Hit NaN progress!!!!");
                                }
                                eVar.c = this.a.ae.a;
                                this.a.W.add(eVar);
                                this.a.ab = null;
                                this.a.ab = (float[]) fArr.clone();
                            }
                            if (((aVar.a * aVar.a) + (aVar.b * aVar.b)) + (aVar.c * aVar.c) >= 6.25E-6f) {
                                com.fyusion.sdk.common.ext.m.a aVar2 = new com.fyusion.sdk.common.ext.m.a();
                                aVar2.a = ((this.a.ae.j[0] * aVar.a) + (this.a.ae.j[4] * aVar.b)) + (this.a.ae.j[8] * aVar.c);
                                aVar2.b = ((this.a.ae.j[1] * aVar.a) + (this.a.ae.j[5] * aVar.b)) + (this.a.ae.j[9] * aVar.c);
                                aVar2.c = ((this.a.ae.j[2] * aVar.a) + (this.a.ae.j[6] * aVar.b)) + (this.a.ae.j[10] * aVar.c);
                                this.a.X.add(mVar2);
                                d s = this.a.aK;
                                s.h++;
                                this.a.ae = null;
                                this.a.ah = false;
                            }
                        }
                    }
                }
            }
        }
    };
    private ImuProgressEstimatorWrapper aM;
    private double aN = 0.0d;
    private HandlerThread aO;
    private float[] aa;
    private float[] ab;
    private float[] ac;
    private m ad = null;
    private m ae;
    private c af;
    private float[] ag = null;
    private boolean ah = false;
    private float ai;
    private float aj;
    private float ak;
    private SensorManager al;
    private int am = -1;
    private int an = -1;
    private int ao = -1;
    private int ap = -1;
    private int aq = -1;
    private boolean ar = false;
    private int as = 0;
    private int at;
    private a au;
    private boolean av = false;
    private b aw;
    private RecordingProgressListener ax;
    private MotionHintsListener ay;
    private MotionHints az = null;
    public int b;
    boolean c;
    double d;
    FyuseFrameInformationVec e;
    int f;
    int g;
    boolean h;
    float i;
    OnlineImageStabilizerWrapper j = null;
    boolean k;
    boolean l = false;
    int m;
    boolean n = false;
    boolean o = false;
    boolean p = false;
    boolean q = false;
    boolean r = false;
    boolean s = true;
    boolean t = false;
    float[] u;
    float[] v;
    boolean w;
    boolean x = false;
    boolean y = false;
    YuvImage z = null;

    /* compiled from: Unknown */
    public interface b {
        void onMovingBackwards();

        void onTerminatedPrematurely();
    }

    /* compiled from: Unknown */
    static class a {
        float a;
        float b;
        float c;
        float d;
        float e;

        a() {
        }
    }

    /* compiled from: Unknown */
    private static class c {
        boolean a;
        boolean b;
        boolean c;
        boolean d;
        boolean e;

        private c() {
            this.a = false;
            this.b = false;
            this.c = false;
            this.d = false;
            this.e = false;
        }
    }

    /* compiled from: Unknown */
    private static class d {
        int a;
        int b;
        int c;
        long d;
        long e;
        long f;
        long g;
        long h;

        private d() {
        }
    }

    /* compiled from: Unknown */
    static class e {
        float a;
        float b;
        double c;

        e() {
        }
    }

    static {
        System.loadLibrary("vislib_jni");
    }

    public c(int i, int i2, boolean z, int i3, b bVar) {
        boolean z2 = false;
        this.aA = i;
        this.aB = i2;
        this.aD = i3;
        this.aC = z;
        this.aw = bVar;
        this.aE = i3 == 270;
        Context context = FyuseSDK.getContext();
        if (context.getResources().getConfiguration().orientation == 1) {
            z2 = true;
        }
        this.aF = z2;
        this.aG = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation() * 90;
        h();
        this.aJ = new n(i, i2, this.aF);
        this.W = Collections.synchronizedList(new LinkedList());
        this.X = Collections.synchronizedList(new LinkedList());
        f();
    }

    private float a(SensorEvent sensorEvent) {
        if (this.as <= 0 || this.aM == null) {
            return 0.0f;
        }
        if (this.F == null) {
            this.F = new float[4];
        }
        SensorManager.getQuaternionFromVector(this.F, sensorEvent.values);
        double d = 0.0d;
        if (this.aM != null) {
            d = this.aM.getProgressUsingQuaternions(this.F[0], this.F[1], this.F[2], this.F[3]);
        }
        if (d > this.aN + 0.009999999776482582d) {
            this.aN = d;
            if (this.ax != null) {
                this.ax.onProgress(d, this.as);
            }
        }
        return ((float) d) * ((float) this.as);
    }

    private static int a(int i, int i2, int[] iArr) {
        for (int i3 : iArr) {
            if (i == i3 || i2 == i3) {
                return i3;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private a a(boolean z) {
        a aVar = new a();
        if (this.X.size() != 0) {
            float f;
            float f2;
            aVar.c = (float) (this.g <= this.f ? 1 : -1);
            if (this.av) {
                DLog.d("getEstimatedDirection", "convexVotes: " + this.g + " concaveVotes: " + this.f + " Result: " + aVar.c);
            }
            float f3 = 0.0f;
            float f4 = 0.0f;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            float f5 = 0.0f;
            float f6 = 0.0f;
            float f7 = 0.0f;
            float f8 = 0.0f;
            int size = (int) (this.Y.getFrameTimestamps().size() - 1);
            int i5 = 0;
            int i6 = 0;
            int i7 = 0;
            int i8 = 0;
            int i9 = 0;
            while (i5 < size) {
                int i10;
                int i11;
                int i12;
                int i13;
                FyuseFrameInformation fyuseFrameInformation = this.e.get(i5);
                m a = a(fyuseFrameInformation.getTimestamp_in_seconds());
                if (i5 == 0) {
                    f5 = a.k.a;
                    f6 = a.k.b;
                } else if (i5 == size) {
                    f7 = a.k.a;
                    f8 = a.k.b;
                }
                float f9 = f5;
                f5 = f6;
                f6 = f7;
                f7 = f8;
                if (!z && fyuseFrameInformation.getIs_dropped_online()) {
                    i10 = i4;
                    i4 = i6;
                    i6 = i10;
                    i11 = i2;
                    i2 = i8;
                    i8 = i11;
                    i12 = i3;
                    i3 = i7;
                    i7 = i12;
                    i13 = i;
                    i = i9;
                    i9 = i13;
                } else {
                    if (i5 <= 0) {
                        f3 = a.k.a;
                        f4 = a.k.b;
                    } else {
                        float f10 = a.k.a - f3;
                        float f11 = a.k.b - f4;
                        if ((((double) Math.abs(f10)) > 0.008726646259971648d ? 1 : null) == null) {
                            if ((((double) Math.abs(f11)) > 0.008726646259971648d ? 1 : null) == null) {
                            }
                        }
                        if (Math.abs(f10) > Math.abs(f11)) {
                            if (f10 > 0.0f) {
                                i9++;
                            } else {
                                i8++;
                            }
                        } else if (f11 > 0.0f) {
                            i7++;
                        } else {
                            i6++;
                        }
                        f3 = a.k.a;
                        f4 = a.k.b;
                    }
                    float f12 = f4;
                    int i14 = i8;
                    i8 = i6;
                    f = f12;
                    i11 = i7;
                    f2 = f3;
                    int i15 = i9;
                    i9 = i11;
                    float f13;
                    if (Math.abs(a.f.a) > Math.abs(a.f.b)) {
                        if (a.f.a > 0.0f) {
                            i10 = i4;
                            i4 = i8;
                            i8 = i2;
                            i2 = i14;
                            f4 = f;
                            i6 = i10;
                            f13 = f2;
                            i7 = i3;
                            i3 = i9;
                            i9 = i + 1;
                            i = i15;
                            f3 = f13;
                        } else {
                            i10 = i4;
                            i4 = i8;
                            i8 = i2;
                            i2 = i14;
                            f4 = f;
                            i6 = i10;
                            f13 = f2;
                            i7 = i3 + 1;
                            i3 = i9;
                            i9 = i;
                            i = i15;
                            f3 = f13;
                        }
                    } else if (a.f.b > 0.0f) {
                        i10 = i4;
                        i4 = i8;
                        i8 = i2 + 1;
                        i2 = i14;
                        f4 = f;
                        i6 = i10;
                        f13 = f2;
                        i7 = i3;
                        i3 = i9;
                        i9 = i;
                        i = i15;
                        f3 = f13;
                    } else {
                        i10 = i4 + 1;
                        i4 = i8;
                        i8 = i2;
                        i2 = i14;
                        f4 = f;
                        i6 = i10;
                        f13 = f2;
                        i7 = i3;
                        i3 = i9;
                        i9 = i;
                        i = i15;
                        f3 = f13;
                    }
                }
                i5++;
                f8 = f7;
                f7 = f6;
                f6 = f5;
                f5 = f9;
                i10 = i6;
                i6 = i4;
                i4 = i10;
                i11 = i8;
                i8 = i2;
                i2 = i11;
                i12 = i7;
                i7 = i3;
                i3 = i12;
                i13 = i9;
                i9 = i;
                i = i13;
            }
            f = f7 - f5;
            f2 = f8 - f6;
            aVar.a = 0.0f;
            aVar.b = 0.0f;
            if (Math.abs(f) > Math.abs(f2)) {
                if (f > 0.0f) {
                    aVar.a = WMElement.CAMERASIZEVALUE1B1;
                } else {
                    aVar.a = GroundOverlayOptions.NO_DIMENSION;
                }
            } else if (f2 > 0.0f) {
                aVar.b = WMElement.CAMERASIZEVALUE1B1;
            } else {
                aVar.b = GroundOverlayOptions.NO_DIMENSION;
            }
            i6 = Math.max(Math.max(Math.max(i, i2), i3), i4);
            if (i2 == i6) {
                aVar.d = 0.0f;
                aVar.e = WMElement.CAMERASIZEVALUE1B1;
            } else if (i4 != i6) {
                if (i != i6) {
                    aVar.d = GroundOverlayOptions.NO_DIMENSION;
                } else {
                    aVar.d = WMElement.CAMERASIZEVALUE1B1;
                }
                aVar.e = 0.0f;
            } else {
                aVar.d = 0.0f;
                aVar.e = GroundOverlayOptions.NO_DIMENSION;
            }
        } else {
            if (this.av) {
                DLog.e("getEstimatedDirection", "[CameraViewController::getEstimatedDirection] Had no motion readings saved; defaulting to portrait mode");
            }
            aVar.a = WMElement.CAMERASIZEVALUE1B1;
            aVar.b = 0.0f;
            aVar.d = 0.0f;
            aVar.e = WMElement.CAMERASIZEVALUE1B1;
        }
        aVar.a = 0.0f;
        aVar.b = 0.0f;
        if (this.av) {
            DLog.d("motionVotes", "mImuDirectionX:" + this.I + ",mImuDirectionY:" + this.i);
        }
        if (Math.abs(this.I) > Math.abs(this.i)) {
            if (this.I > 0.0f) {
                aVar.a = WMElement.CAMERASIZEVALUE1B1;
            } else {
                aVar.a = GroundOverlayOptions.NO_DIMENSION;
            }
        } else if (this.i > 0.0f) {
            aVar.b = GroundOverlayOptions.NO_DIMENSION;
        } else {
            aVar.b = WMElement.CAMERASIZEVALUE1B1;
        }
        return aVar;
    }

    private m a(double d) {
        int size = this.X.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                m mVar = (m) this.X.get(i);
                if (mVar != null && mVar.a <= d) {
                    return mVar;
                }
            }
        }
        return null;
    }

    private void a(int i, float[] fArr, RotationDirection rotationDirection) {
        this.at = i <= 270 ? 300 : 1000;
        if (this.aM == null) {
            this.aM = new ImuProgressEstimatorWrapper();
        }
        this.aM.setTargetRotationAmount(((((double) ((float) i)) / 360.0d) * 2.0d) * 3.141592653589793d);
        if (fArr != null && fArr.length == 3) {
            this.aM.setAxisOfRotation(fArr[0], fArr[1], fArr[2]);
        }
        ImuProgressEstimatorWrapper.RotationDirection rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.UNSPECIFIED;
        if (rotationDirection == RotationDirection.CLOCKWISE) {
            rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.CLOCKWISE;
        } else if (rotationDirection == RotationDirection.COUNTERCLOCKWISE) {
            rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.COUNTERCLOCKWISE;
        }
        this.aM.setRotationDirection(rotationDirection2);
        if (this.ax != null) {
            this.ax.onProgress(0.0d, i);
        }
    }

    private void a(Hint hint) {
        if (this.ay != null) {
            MotionHints motionHints = new MotionHints(hint);
            if (!motionHints.equals(this.az)) {
                this.az = motionHints;
                this.ay.onMotionDataEvent(motionHints);
            }
        }
    }

    private void a(a aVar, int i, float[] fArr, RotationDirection rotationDirection) {
        this.aK = new d();
        l();
        this.au = aVar;
        this.as = i;
        a(i, fArr, rotationDirection);
        if (this.Y == null) {
            this.Y = new com.fyusion.sdk.common.ext.e();
        }
        this.W.clear();
        e eVar = new e();
        eVar.b = 0.0f;
        eVar.a = 0.0f;
        eVar.c = 0.0d;
        this.W.add(eVar);
        this.ai = 0.0f;
        this.aj = 9.80665f;
        this.ak = 9.80665f;
        this.j = new OnlineImageStabilizerWrapper();
        this.af = new c();
        this.X.clear();
        this.e = null;
        this.e = new FyuseFrameInformationVec();
        a((this.at * 1000) + 3);
        FyuseSize fyuseSize = new FyuseSize(0.0d, 0.0d);
        fyuseSize.width = (double) this.aA;
        fyuseSize.height = (double) this.aB;
        this.Y.setCameraOrientation(this.aD);
        this.Y.setWhetherRecordedWithFrontCamera(!this.aC);
        this.Y.setCameraSize(fyuseSize);
        if (this.Y.getProcessedSize().height == 0.0d && this.Y.getProcessedSize().width == 0.0d) {
            com.fyusion.sdk.common.e eVar2 = this.aA < 1080 ? j.g : j.e;
            fyuseSize = new FyuseSize(eVar2.a, eVar2.b);
            if (this.av) {
                DLog.d("InternalProcessor", "processedSize width: " + fyuseSize.width + " height: " + fyuseSize.height);
            }
            this.Y.setProcessedSize(fyuseSize);
        }
        if (!this.x) {
            this.x = true;
            DLog.d("startRecording", "Starting a recording");
            this.y = false;
        }
    }

    private void a(com.fyusion.sdk.common.ext.e eVar) {
        Location a = com.fyusion.sdk.camera.util.c.a(FyuseSDK.getContext()).a();
        if (this.av) {
            DLog.d("saveLocationToMagic", "location " + a);
        }
        if (a != null) {
            FyusePlacemark fyusePlacemark = new FyusePlacemark();
            fyusePlacemark.setLatitude((float) a.getLatitude());
            fyusePlacemark.setLongitude((float) a.getLongitude());
            eVar.setPlacemark(fyusePlacemark);
        }
    }

    private static boolean a(int i, int[] iArr) {
        for (int i2 : iArr) {
            if (i == i2) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @TargetApi(21)
    private boolean a(long j, byte[] bArr, byte[] bArr2) {
        if (this.x) {
            this.a++;
            if (this.a <= 3 && !this.aH) {
                if (this.av) {
                    DLog.d("dropFrameFromRecording", "Dropped: YES  | mUncroppedFrameCount <= GlobalConstants.g_NUM_FRAMES_TO_CROP");
                }
                return true;
            } else if (this.b >= this.at) {
                if (this.av) {
                    DLog.d("dropFrameFromRecording", "Too many frames were recorded!");
                }
                return true;
            } else if (this.c || this.aH) {
                double d = ((double) j) / 1000.0d;
                if (this.d == 0.0d) {
                    this.d = d;
                    return true;
                }
                double d2 = d - this.d;
                this.d = d;
                m a = a(((double) j) / 1000.0d);
                if (a != null) {
                    Object obj;
                    int i;
                    float f;
                    float f2;
                    Object obj2;
                    Object obj3;
                    Object obj4;
                    int i2;
                    FyuseFrameInformation fyuseFrameInformation = new FyuseFrameInformation();
                    fyuseFrameInformation.setTimestamp_in_seconds(((double) j) / 1000.0d);
                    fyuseFrameInformation.setExposure_value(0.0d);
                    fyuseFrameInformation.setBrightness_value(0.0d);
                    fyuseFrameInformation.setIso_value(0);
                    fyuseFrameInformation.setHas_imu_direction_settled(this.h);
                    fyuseFrameInformation.setImu_direction_x((double) this.I);
                    fyuseFrameInformation.setImu_direction_y((double) this.i);
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "isBackCamera: " + this.aC + " isMirrorVertically: " + this.aE);
                    }
                    long nanoTime = System.nanoTime();
                    if (this.j == null) {
                        this.j = new OnlineImageStabilizerWrapper();
                    }
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "processYV12Frame : data " + bArr.length);
                        DLog.d("dropFrameFromRecording", "processYV12Frame : w x h  --> " + this.aA + " X " + this.aB);
                    }
                    if (bArr2 != null) {
                        if (this.av) {
                            DLog.d("dropFrameFromRecording", "processYV12Frame with depth");
                        }
                        this.j.processYV12FrameWithDepth(bArr, this.aA, this.aB, this.aA, this.aE, bArr2, 640, 360, 720);
                    } else {
                        this.j.processYV12Frame(bArr, this.aA, this.aB, this.aA, this.aE);
                    }
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "processYV12Frame took: " + (((float) (System.nanoTime() - nanoTime)) / 1000000.0f));
                    }
                    j();
                    this.L = a.k.a;
                    this.M = a.k.b;
                    this.N = a.k.c;
                    this.O = a.c.a;
                    this.P = a.c.b;
                    float f3 = this.L - this.Q;
                    float f4 = this.M - this.R;
                    float f5 = this.N - this.S;
                    float f6 = this.O - this.T;
                    float f7 = this.P - this.U;
                    if (this.e.size() != 0) {
                        if ((Math.abs(f3) > 0.01f ? 1 : null) == null) {
                            if ((Math.abs(f4) > 0.01f ? 1 : null) == null) {
                                if ((Math.abs(f5) > 0.01f ? 1 : null) == null) {
                                    if ((Math.abs(f6) > 0.01f ? 1 : null) == null && Math.abs(f7) <= 0.01f) {
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | Insufficient movement between frames.");
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    Object obj5 = null;
                    f4 = 0.0f;
                    e b = b((double) ((float) d));
                    if (b == null) {
                        b = new e();
                        b.a = 0.0f;
                        b.c = d;
                        b.b = 0.0f;
                    }
                    float f8 = b.a;
                    f3 = this.f <= this.g ? 0.017453292f : 0.008726646f;
                    if (this.h) {
                        if (this.J > 0.0f) {
                            f4 = f8 - this.J;
                        }
                        if (this.J <= 0.0f || f8 - this.J >= f3) {
                            f7 = f4;
                            obj = null;
                            int i3 = 1;
                            if ((((double) f7) / d2 > 0.75d ? 1 : null) == null) {
                                this.H = 5;
                                if (!this.G) {
                                    a(Hint.MOVING_TOO_FAST);
                                }
                                this.G = true;
                            } else if (this.G) {
                                this.H--;
                                if (this.H <= 0) {
                                    this.G = false;
                                }
                            }
                            i = this.aC ? 1 : -1;
                            if (f8 > 0.08726646f) {
                                f5 = this.j.computeRotationCompensatedFlow(this.I, ((float) i) * this.i, f8 - this.J);
                                f4 = ((float) (this.f <= this.g ? 1 : -1)) * f5;
                                if (this.av) {
                                    DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                                }
                            } else {
                                if (this.av) {
                                    DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                                }
                                f4 = this.j.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                                f5 = this.j.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                                f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                                f5 = f4;
                            }
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                            }
                            f = (float) ((this.f + 1) / (this.g + 1));
                            f2 = (float) (this.f < this.g ? 6 : 3);
                            if (this.f + this.g >= 5) {
                                if (((double) f) > 0.25d && ((double) f) < 0.75d) {
                                }
                                f6 = f4;
                                if (obj2 == null) {
                                    if (this.f >= this.g) {
                                        if ((f6 <= f2 ? 1 : null) == null) {
                                            if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                            }
                                        }
                                    }
                                    obj3 = null;
                                    if (obj3 == null) {
                                        if (this.g > this.f && r7 != null) {
                                        }
                                        obj4 = 1;
                                        if (!this.G) {
                                            a(Hint.MOVING_CORRECTLY);
                                        }
                                        if (obj3 == null) {
                                            if (this.av) {
                                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                            }
                                            if (f8 > 0.08726646f) {
                                                if (((double) f5) <= -4.0d) {
                                                    this.g++;
                                                } else {
                                                    this.f++;
                                                }
                                            }
                                            this.J = f8;
                                            this.Q = this.L;
                                            this.R = this.M;
                                            this.S = this.N;
                                            this.T = this.O;
                                            this.U = this.P;
                                            this.b++;
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                            }
                                            fyuseFrameInformation.setIs_dropped_online(false);
                                            this.e.add(fyuseFrameInformation);
                                            this.j.addProcessedFrame(true);
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                            }
                                            return false;
                                        }
                                        if (obj4 != null) {
                                            i2 = this.V + 1;
                                            this.V = i2;
                                            if (i2 > 3) {
                                                if (this.g > this.f || !this.aC) {
                                                    a(Hint.MOVING_BACKWARDS);
                                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                                    if (this.V >= 12) {
                                                        if (this.av) {
                                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                        }
                                                        if (!(this.aw == null || this.n)) {
                                                            this.aw.onMovingBackwards();
                                                            this.n = true;
                                                        }
                                                        DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                        }
                                        return false;
                                    }
                                    obj4 = null;
                                    if (this.G) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (obj4 != null) {
                                            i2 = this.V + 1;
                                            this.V = i2;
                                            if (i2 > 3) {
                                                if (this.g > this.f) {
                                                }
                                                a(Hint.MOVING_BACKWARDS);
                                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                                if (this.V >= 12) {
                                                    if (this.av) {
                                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                    }
                                                    this.aw.onMovingBackwards();
                                                    this.n = true;
                                                    DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                    return true;
                                                }
                                            }
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                        }
                                        return false;
                                    }
                                    if (this.av) {
                                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                    }
                                    if (f8 > 0.08726646f) {
                                        if (((double) f5) <= -4.0d) {
                                            this.f++;
                                        } else {
                                            this.g++;
                                        }
                                    }
                                    this.J = f8;
                                    this.Q = this.L;
                                    this.R = this.M;
                                    this.S = this.N;
                                    this.T = this.O;
                                    this.U = this.P;
                                    this.b++;
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(false);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(true);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                    }
                                    return false;
                                }
                                obj3 = 1;
                                if (obj3 == null) {
                                    if (this.g > this.f) {
                                        obj4 = 1;
                                        if (this.G) {
                                            a(Hint.MOVING_CORRECTLY);
                                        }
                                        if (obj3 == null) {
                                            if (this.av) {
                                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                            }
                                            if (f8 > 0.08726646f) {
                                                if (((double) f5) <= -4.0d) {
                                                    this.g++;
                                                } else {
                                                    this.f++;
                                                }
                                            }
                                            this.J = f8;
                                            this.Q = this.L;
                                            this.R = this.M;
                                            this.S = this.N;
                                            this.T = this.O;
                                            this.U = this.P;
                                            this.b++;
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                            }
                                            fyuseFrameInformation.setIs_dropped_online(false);
                                            this.e.add(fyuseFrameInformation);
                                            this.j.addProcessedFrame(true);
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                            }
                                            return false;
                                        }
                                        if (obj4 != null) {
                                            i2 = this.V + 1;
                                            this.V = i2;
                                            if (i2 > 3) {
                                                if (this.g > this.f) {
                                                }
                                                a(Hint.MOVING_BACKWARDS);
                                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                                if (this.V >= 12) {
                                                    if (this.av) {
                                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                    }
                                                    this.aw.onMovingBackwards();
                                                    this.n = true;
                                                    DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                    return true;
                                                }
                                            }
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                        }
                                        return false;
                                    }
                                }
                                obj4 = null;
                                if (this.G) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (obj4 != null) {
                                        i2 = this.V + 1;
                                        this.V = i2;
                                        if (i2 > 3) {
                                            if (this.g > this.f) {
                                            }
                                            a(Hint.MOVING_BACKWARDS);
                                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                            if (this.V >= 12) {
                                                if (this.av) {
                                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                }
                                                this.aw.onMovingBackwards();
                                                this.n = true;
                                                DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                return true;
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(false);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                                if (this.av) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.f++;
                                    } else {
                                        this.g++;
                                    }
                                }
                                this.J = f8;
                                this.Q = this.L;
                                this.R = this.M;
                                this.S = this.N;
                                this.T = this.O;
                                this.U = this.P;
                                this.b++;
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(true);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            f4 = Math.abs(f5);
                            f6 = f4;
                            if (obj2 == null) {
                                if (this.f >= this.g) {
                                    if (f6 <= f2) {
                                    }
                                    if ((f6 <= f2 ? 1 : null) == null) {
                                        if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                        }
                                    }
                                }
                                obj3 = null;
                                if (obj3 == null) {
                                    if (this.g > this.f) {
                                        obj4 = 1;
                                        if (this.G) {
                                            a(Hint.MOVING_CORRECTLY);
                                        }
                                        if (obj3 == null) {
                                            if (this.av) {
                                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                            }
                                            if (f8 > 0.08726646f) {
                                                if (((double) f5) <= -4.0d) {
                                                    this.g++;
                                                } else {
                                                    this.f++;
                                                }
                                            }
                                            this.J = f8;
                                            this.Q = this.L;
                                            this.R = this.M;
                                            this.S = this.N;
                                            this.T = this.O;
                                            this.U = this.P;
                                            this.b++;
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                            }
                                            fyuseFrameInformation.setIs_dropped_online(false);
                                            this.e.add(fyuseFrameInformation);
                                            this.j.addProcessedFrame(true);
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                            }
                                            return false;
                                        }
                                        if (obj4 != null) {
                                            i2 = this.V + 1;
                                            this.V = i2;
                                            if (i2 > 3) {
                                                if (this.g > this.f) {
                                                }
                                                a(Hint.MOVING_BACKWARDS);
                                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                                if (this.V >= 12) {
                                                    if (this.av) {
                                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                    }
                                                    this.aw.onMovingBackwards();
                                                    this.n = true;
                                                    DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                    return true;
                                                }
                                            }
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                        }
                                        return false;
                                    }
                                }
                                obj4 = null;
                                if (this.G) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (obj4 != null) {
                                        i2 = this.V + 1;
                                        this.V = i2;
                                        if (i2 > 3) {
                                            if (this.g > this.f) {
                                            }
                                            a(Hint.MOVING_BACKWARDS);
                                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                            if (this.V >= 12) {
                                                if (this.av) {
                                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                }
                                                this.aw.onMovingBackwards();
                                                this.n = true;
                                                DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                return true;
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(false);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                                if (this.av) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.f++;
                                    } else {
                                        this.g++;
                                    }
                                }
                                this.J = f8;
                                this.Q = this.L;
                                this.R = this.M;
                                this.S = this.N;
                                this.T = this.O;
                                this.U = this.P;
                                this.b++;
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(true);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            obj3 = 1;
                            if (obj3 == null) {
                                if (this.g > this.f) {
                                    obj4 = 1;
                                    if (this.G) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.av) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.g++;
                                            } else {
                                                this.f++;
                                            }
                                        }
                                        this.J = f8;
                                        this.Q = this.L;
                                        this.R = this.M;
                                        this.S = this.N;
                                        this.T = this.O;
                                        this.U = this.P;
                                        this.b++;
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(true);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    if (obj4 != null) {
                                        i2 = this.V + 1;
                                        this.V = i2;
                                        if (i2 > 3) {
                                            if (this.g > this.f) {
                                            }
                                            a(Hint.MOVING_BACKWARDS);
                                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                            if (this.V >= 12) {
                                                if (this.av) {
                                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                }
                                                this.aw.onMovingBackwards();
                                                this.n = true;
                                                DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                return true;
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(false);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                            }
                            obj4 = null;
                            if (this.G) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                if (obj4 != null) {
                                    i2 = this.V + 1;
                                    this.V = i2;
                                    if (i2 > 3) {
                                        if (this.g > this.f) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                        if (this.V >= 12) {
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            this.aw.onMovingBackwards();
                                            this.n = true;
                                            DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                            return true;
                                        }
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(false);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                            if (this.av) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.f++;
                                } else {
                                    this.g++;
                                }
                            }
                            this.J = f8;
                            this.Q = this.L;
                            this.R = this.M;
                            this.S = this.N;
                            this.T = this.O;
                            this.U = this.P;
                            this.b++;
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.e.add(fyuseFrameInformation);
                            this.j.addProcessedFrame(true);
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                        obj5 = null;
                        if (f8 - this.J < -0.08726646f) {
                            f7 = f4;
                            int i4 = 1;
                            obj2 = null;
                            if (((double) f7) / d2 > 0.75d) {
                            }
                            if ((((double) f7) / d2 > 0.75d ? 1 : null) == null) {
                                this.H = 5;
                                if (this.G) {
                                    a(Hint.MOVING_TOO_FAST);
                                }
                                this.G = true;
                            } else if (this.G) {
                                this.H--;
                                if (this.H <= 0) {
                                    this.G = false;
                                }
                            }
                            if (this.aC) {
                            }
                            if (f8 > 0.08726646f) {
                                if (this.av) {
                                    DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                                }
                                f4 = this.j.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                                f5 = this.j.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                                f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                                f5 = f4;
                            } else {
                                f5 = this.j.computeRotationCompensatedFlow(this.I, ((float) i) * this.i, f8 - this.J);
                                if (this.f <= this.g) {
                                }
                                f4 = ((float) (this.f <= this.g ? 1 : -1)) * f5;
                                if (this.av) {
                                    DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                                }
                            }
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                            }
                            f = (float) ((this.f + 1) / (this.g + 1));
                            if (this.f < this.g) {
                            }
                            f2 = (float) (this.f < this.g ? 6 : 3);
                            if (this.f + this.g >= 5) {
                            }
                            f4 = Math.abs(f5);
                            f6 = f4;
                            if (obj2 == null) {
                                if (this.f >= this.g) {
                                    if (f6 <= f2) {
                                    }
                                    if ((f6 <= f2 ? 1 : null) == null) {
                                        if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                        }
                                    }
                                }
                                obj3 = null;
                                if (obj3 == null) {
                                    if (this.g > this.f) {
                                        obj4 = 1;
                                        if (this.G) {
                                            a(Hint.MOVING_CORRECTLY);
                                        }
                                        if (obj3 == null) {
                                            if (this.av) {
                                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                            }
                                            if (f8 > 0.08726646f) {
                                                if (((double) f5) <= -4.0d) {
                                                    this.g++;
                                                } else {
                                                    this.f++;
                                                }
                                            }
                                            this.J = f8;
                                            this.Q = this.L;
                                            this.R = this.M;
                                            this.S = this.N;
                                            this.T = this.O;
                                            this.U = this.P;
                                            this.b++;
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                            }
                                            fyuseFrameInformation.setIs_dropped_online(false);
                                            this.e.add(fyuseFrameInformation);
                                            this.j.addProcessedFrame(true);
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                            }
                                            return false;
                                        }
                                        if (obj4 != null) {
                                            i2 = this.V + 1;
                                            this.V = i2;
                                            if (i2 > 3) {
                                                if (this.g > this.f) {
                                                }
                                                a(Hint.MOVING_BACKWARDS);
                                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                                if (this.V >= 12) {
                                                    if (this.av) {
                                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                    }
                                                    this.aw.onMovingBackwards();
                                                    this.n = true;
                                                    DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                    return true;
                                                }
                                            }
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(true);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(false);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                        }
                                        return false;
                                    }
                                }
                                obj4 = null;
                                if (this.G) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (obj4 != null) {
                                        i2 = this.V + 1;
                                        this.V = i2;
                                        if (i2 > 3) {
                                            if (this.g > this.f) {
                                            }
                                            a(Hint.MOVING_BACKWARDS);
                                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                            if (this.V >= 12) {
                                                if (this.av) {
                                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                }
                                                this.aw.onMovingBackwards();
                                                this.n = true;
                                                DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                return true;
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(false);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                                if (this.av) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.f++;
                                    } else {
                                        this.g++;
                                    }
                                }
                                this.J = f8;
                                this.Q = this.L;
                                this.R = this.M;
                                this.S = this.N;
                                this.T = this.O;
                                this.U = this.P;
                                this.b++;
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(true);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            obj3 = 1;
                            if (obj3 == null) {
                                if (this.g > this.f) {
                                    obj4 = 1;
                                    if (this.G) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.av) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.g++;
                                            } else {
                                                this.f++;
                                            }
                                        }
                                        this.J = f8;
                                        this.Q = this.L;
                                        this.R = this.M;
                                        this.S = this.N;
                                        this.T = this.O;
                                        this.U = this.P;
                                        this.b++;
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.e.add(fyuseFrameInformation);
                                        this.j.addProcessedFrame(true);
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    if (obj4 != null) {
                                        i2 = this.V + 1;
                                        this.V = i2;
                                        if (i2 > 3) {
                                            if (this.g > this.f) {
                                            }
                                            a(Hint.MOVING_BACKWARDS);
                                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                            if (this.V >= 12) {
                                                if (this.av) {
                                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                }
                                                this.aw.onMovingBackwards();
                                                this.n = true;
                                                DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                                return true;
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(false);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                            }
                            obj4 = null;
                            if (this.G) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                if (obj4 != null) {
                                    i2 = this.V + 1;
                                    this.V = i2;
                                    if (i2 > 3) {
                                        if (this.g > this.f) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                        if (this.V >= 12) {
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            this.aw.onMovingBackwards();
                                            this.n = true;
                                            DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                            return true;
                                        }
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(false);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                            if (this.av) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.f++;
                                } else {
                                    this.g++;
                                }
                            }
                            this.J = f8;
                            this.Q = this.L;
                            this.R = this.M;
                            this.S = this.N;
                            this.T = this.O;
                            this.U = this.P;
                            this.b++;
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.e.add(fyuseFrameInformation);
                            this.j.addProcessedFrame(true);
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                    }
                    f7 = f4;
                    obj = null;
                    obj2 = obj5;
                    if (((double) f7) / d2 > 0.75d) {
                    }
                    if ((((double) f7) / d2 > 0.75d ? 1 : null) == null) {
                        this.H = 5;
                        if (this.G) {
                            a(Hint.MOVING_TOO_FAST);
                        }
                        this.G = true;
                    } else if (this.G) {
                        this.H--;
                        if (this.H <= 0) {
                            this.G = false;
                        }
                    }
                    if (this.aC) {
                    }
                    if (f8 > 0.08726646f) {
                        f5 = this.j.computeRotationCompensatedFlow(this.I, ((float) i) * this.i, f8 - this.J);
                        if (this.f <= this.g) {
                        }
                        f4 = ((float) (this.f <= this.g ? 1 : -1)) * f5;
                        if (this.av) {
                            DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                        }
                    } else {
                        if (this.av) {
                            DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                        }
                        f4 = this.j.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                        f5 = this.j.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                        f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                        f5 = f4;
                    }
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                    }
                    f = (float) ((this.f + 1) / (this.g + 1));
                    if (this.f < this.g) {
                    }
                    f2 = (float) (this.f < this.g ? 6 : 3);
                    if (this.f + this.g >= 5) {
                    }
                    f4 = Math.abs(f5);
                    f6 = f4;
                    if (obj2 == null) {
                        if (this.f >= this.g) {
                            if (f6 <= f2) {
                            }
                            if ((f6 <= f2 ? 1 : null) == null) {
                                if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                }
                            }
                        }
                        obj3 = null;
                        if (obj3 == null) {
                            if (this.g > this.f) {
                                obj4 = 1;
                                if (this.G) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (this.av) {
                                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                    }
                                    if (f8 > 0.08726646f) {
                                        if (((double) f5) <= -4.0d) {
                                            this.g++;
                                        } else {
                                            this.f++;
                                        }
                                    }
                                    this.J = f8;
                                    this.Q = this.L;
                                    this.R = this.M;
                                    this.S = this.N;
                                    this.T = this.O;
                                    this.U = this.P;
                                    this.b++;
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(false);
                                    this.e.add(fyuseFrameInformation);
                                    this.j.addProcessedFrame(true);
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                    }
                                    return false;
                                }
                                if (obj4 != null) {
                                    i2 = this.V + 1;
                                    this.V = i2;
                                    if (i2 > 3) {
                                        if (this.g > this.f) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                        if (this.V >= 12) {
                                            if (this.av) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            this.aw.onMovingBackwards();
                                            this.n = true;
                                            DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                            return true;
                                        }
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(false);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                        }
                        obj4 = null;
                        if (this.G) {
                            a(Hint.MOVING_CORRECTLY);
                        }
                        if (obj3 == null) {
                            if (obj4 != null) {
                                i2 = this.V + 1;
                                this.V = i2;
                                if (i2 > 3) {
                                    if (this.g > this.f) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                    if (this.V >= 12) {
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        this.aw.onMovingBackwards();
                                        this.n = true;
                                        DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                        return true;
                                    }
                                }
                            }
                            fyuseFrameInformation.setIs_dropped_online(true);
                            this.e.add(fyuseFrameInformation);
                            this.j.addProcessedFrame(false);
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                            }
                            return false;
                        }
                        if (this.av) {
                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                        }
                        if (f8 > 0.08726646f) {
                            if (((double) f5) <= -4.0d) {
                                this.f++;
                            } else {
                                this.g++;
                            }
                        }
                        this.J = f8;
                        this.Q = this.L;
                        this.R = this.M;
                        this.S = this.N;
                        this.T = this.O;
                        this.U = this.P;
                        this.b++;
                        if (this.av) {
                            DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                        }
                        fyuseFrameInformation.setIs_dropped_online(false);
                        this.e.add(fyuseFrameInformation);
                        this.j.addProcessedFrame(true);
                        if (this.av) {
                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                        }
                        return false;
                    }
                    obj3 = 1;
                    if (obj3 == null) {
                        if (this.g > this.f) {
                            obj4 = 1;
                            if (this.G) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                if (this.av) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.g++;
                                    } else {
                                        this.f++;
                                    }
                                }
                                this.J = f8;
                                this.Q = this.L;
                                this.R = this.M;
                                this.S = this.N;
                                this.T = this.O;
                                this.U = this.P;
                                this.b++;
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.e.add(fyuseFrameInformation);
                                this.j.addProcessedFrame(true);
                                if (this.av) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            if (obj4 != null) {
                                i2 = this.V + 1;
                                this.V = i2;
                                if (i2 > 3) {
                                    if (this.g > this.f) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                    if (this.V >= 12) {
                                        if (this.av) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        this.aw.onMovingBackwards();
                                        this.n = true;
                                        DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                        return true;
                                    }
                                }
                            }
                            fyuseFrameInformation.setIs_dropped_online(true);
                            this.e.add(fyuseFrameInformation);
                            this.j.addProcessedFrame(false);
                            if (this.av) {
                                DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                            }
                            return false;
                        }
                    }
                    obj4 = null;
                    if (this.G) {
                        a(Hint.MOVING_CORRECTLY);
                    }
                    if (obj3 == null) {
                        if (obj4 != null) {
                            i2 = this.V + 1;
                            this.V = i2;
                            if (i2 > 3) {
                                if (this.g > this.f) {
                                }
                                a(Hint.MOVING_BACKWARDS);
                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.V);
                                if (this.V >= 12) {
                                    if (this.av) {
                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                    }
                                    this.aw.onMovingBackwards();
                                    this.n = true;
                                    DLog.d("imuDirection", "Should stop recording for moving backwards.");
                                    return true;
                                }
                            }
                        }
                        fyuseFrameInformation.setIs_dropped_online(true);
                        this.e.add(fyuseFrameInformation);
                        this.j.addProcessedFrame(false);
                        if (this.av) {
                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                        }
                        return false;
                    }
                    if (this.av) {
                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                    }
                    if (f8 > 0.08726646f) {
                        if (((double) f5) <= -4.0d) {
                            this.f++;
                        } else {
                            this.g++;
                        }
                    }
                    this.J = f8;
                    this.Q = this.L;
                    this.R = this.M;
                    this.S = this.N;
                    this.T = this.O;
                    this.U = this.P;
                    this.b++;
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "Concave: " + this.f + " Convex: " + this.g);
                    }
                    fyuseFrameInformation.setIs_dropped_online(false);
                    this.e.add(fyuseFrameInformation);
                    this.j.addProcessedFrame(true);
                    if (this.av) {
                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                    }
                    return false;
                }
                if (this.av) {
                    DLog.w("dropFrameFromRecording", "Dropped: YES | latest_data == null");
                }
                return true;
            } else {
                if (this.av) {
                    DLog.d("dropFrameFromRecording", "IMU has NOT stabilized!");
                }
                return true;
            }
        }
        Log.d("onPreviewFrame", "return true because isrecording is false");
        return true;
    }

    private e b(double d) {
        e eVar = null;
        int size = this.W.size();
        if (size > 0) {
            while (true) {
                size--;
                if (size < 0) {
                    break;
                }
                eVar = (e) this.W.get(size);
                if (eVar != null && eVar.c <= d) {
                    return eVar;
                }
            }
        }
        return eVar;
    }

    private static String b(int i) {
        return VERSION.SDK_INT < 19 ? c(i) : d(i);
    }

    private static void b(float[] fArr, float[] fArr2) {
        if (fArr2 != null) {
            fArr2[0] = fArr2[0] * GroundOverlayOptions.NO_DIMENSION;
            fArr2[1] = fArr2[1] * GroundOverlayOptions.NO_DIMENSION;
        }
        if (fArr != null) {
            fArr[0] = fArr[0] * GroundOverlayOptions.NO_DIMENSION;
            fArr[4] = fArr[4] * GroundOverlayOptions.NO_DIMENSION;
            fArr[8] = fArr[8] * GroundOverlayOptions.NO_DIMENSION;
            fArr[1] = fArr[1] * GroundOverlayOptions.NO_DIMENSION;
            fArr[5] = fArr[5] * GroundOverlayOptions.NO_DIMENSION;
            fArr[9] = fArr[9] * GroundOverlayOptions.NO_DIMENSION;
        }
    }

    private static boolean b(float f, float f2, float f3) {
        float abs = Math.abs(f - f2);
        while (((double) abs) > 3.141592653589793d) {
            abs = (float) (((double) abs) - 6.283185307179586d);
        }
        return Math.abs(abs) < f3;
    }

    private static String c(int i) {
        switch (i) {
            case 1:
                return "TYPE_ACCELEROMETER";
            case 2:
                return "TYPE_MAGNETIC_FIELD";
            case 9:
                return "TYPE_GRAVITY";
            case 10:
                return "TYPE_LINEAR_ACCELERATION";
            case 11:
                return "TYPE_ROTATION_VECTOR";
            case 14:
                return "TYPE_MAGNETIC_FIELD_UNCALIBRATED";
            case 15:
                return "TYPE_GAME_ROTATION_VECTOR";
            default:
                return "UNKNOWN";
        }
    }

    @TargetApi(19)
    private static String d(int i) {
        return i != 20 ? c(i) : "TYPE_GEOMAGNETIC_ROTATION_VECTOR";
    }

    private void f() {
        this.Q = 0.0f;
        this.R = 0.0f;
        this.S = 0.0f;
        this.L = 0.0f;
        this.M = 0.0f;
        this.N = 0.0f;
        this.O = 0.0f;
        this.P = 0.0f;
        this.T = 0.0f;
        this.U = 0.0f;
        this.I = 0.0f;
        this.i = 0.0f;
        this.a = 0;
        this.b = 0;
        this.g = 0;
        this.f = 0;
        this.J = GroundOverlayOptions.NO_DIMENSION;
        this.Y = null;
        this.Z = null;
        this.aa = null;
        this.l = false;
        this.aa = null;
        this.G = false;
        this.H = 0;
        this.ab = null;
        this.ac = null;
        this.d = -1.0d;
        this.V = 0;
        this.ad = null;
        this.ae = null;
        this.af = null;
        this.ah = false;
        this.m = 0;
        this.w = false;
        this.c = false;
        this.h = false;
        this.z = null;
        g();
    }

    private void g() {
        if (this.x) {
            this.x = false;
            this.A = 0;
            this.B = 0;
            this.C = 0;
            this.D = 0;
            DLog.d("InternalProcessor", "Stopping recording.");
        }
    }

    private void h() {
        this.al = (SensorManager) FyuseSDK.getContext().getSystemService("sensor");
        List<Sensor> sensorList = this.al.getSensorList(-1);
        int[] n = n();
        for (Sensor type : sensorList) {
            int type2 = type.getType();
            if (a(type2, n)) {
                this.am = a(type2, this.am, n);
                this.o = true;
            }
            if (a(type2, aP)) {
                this.ao = a(type2, this.ao, aP);
                this.p = true;
            }
            if (a(type2, aQ)) {
                this.an = a(type2, this.an, aQ);
                this.q = true;
            }
            if (a(type2, aR)) {
                this.ap = a(type2, this.ap, aR);
                this.r = true;
            }
            if (a(type2, aS)) {
                this.aq = a(type2, this.aq, aS);
                this.s = true;
            }
        }
        if (this.o) {
            this.ar = false;
        }
        if (this.av) {
            DLog.d("CameraRequirements", "mHasRotation:" + this.o + ",type:" + b(this.am) + ",hasAccel:" + this.p + ",type:" + b(this.ao) + ",mHasGravity:" + this.q + ",type:" + b(this.an) + ",mHasMagnetometer:" + this.r + ",type:" + b(this.ap) + ", mHasGyroscope:" + this.s + ",type:" + b(this.aq) + ", ignoreGyroscope:" + this.ar + " (using:" + this.t + ")");
        }
    }

    private Bitmap i() throws IOException {
        Throwable th;
        Throwable th2 = null;
        if (this.z == null) {
            return null;
        }
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.z.compressToJpeg(new Rect(0, 0, this.aA, this.aB), 100, byteArrayOutputStream);
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return decodeByteArray;
        } catch (Throwable th22) {
            Throwable th3 = th22;
            th22 = th;
            th = th3;
        }
        if (byteArrayOutputStream != null) {
            if (th22 == null) {
                byteArrayOutputStream.close();
            } else {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th4) {
                    th22.addSuppressed(th4);
                }
            }
        }
        throw th;
        throw th;
    }

    private void j() {
        if (this.av) {
            DLog.d("updateRecordingKeypointAnimation", "Called.");
        }
        int i = this.f + this.g;
        if (i > 10) {
            if (i >= 11 && !this.aI) {
                if (this.av) {
                    DLog.d("updateRecordingKeypointAnimation", "total_votes > vote_thresh");
                }
                i = !this.aC ? -1 : 1;
                if (this.f > this.g) {
                    i *= -1;
                }
                if (this.av) {
                    DLog.d("updateRecordingKeypointAnimation", "imu_directions: " + this.I + "," + this.i);
                }
                Hint hint = Math.abs(this.I) > Math.abs(this.i) ? ((float) i) * this.I > 0.0f ? !this.aF ? this.aG != 90 ? Hint.MOTION_UP : Hint.MOTION_DOWN : Hint.MOTION_LEFT : !this.aF ? this.aG != 90 ? Hint.MOTION_DOWN : Hint.MOTION_UP : Hint.MOTION_RIGHT : ((float) i) * this.i > 0.0f ? !this.aF ? this.aG != 90 ? Hint.MOTION_LEFT : Hint.MOTION_RIGHT : Hint.MOTION_DOWN : !this.aF ? this.aG != 90 ? Hint.MOTION_RIGHT : Hint.MOTION_LEFT : Hint.MOTION_UP;
                if (this.av) {
                    DLog.d("updateRecordingKeypointAnimation", "PortraitMode? :" + this.aF + ": Hint : " + hint);
                }
                a(hint);
                this.aI = true;
            }
        } else if (this.av) {
            DLog.d("updateRecordingKeypointAnimation", "total_votes <= vote_thresh");
        }
    }

    private HandlerThread k() {
        if (this.aO == null) {
            this.aO = new HandlerThread("CameraSensorThread");
            this.aO.start();
        }
        return this.aO;
    }

    private void l() {
        Handler handler = new Handler(k().getLooper());
        if (this.av) {
            DLog.d("Recording", "REGISTERING SENSORS");
        }
        for (Sensor sensor : this.al.getSensorList(-1)) {
            int type = sensor.getType();
            if (!(type == this.am || type == this.ao || type == this.an)) {
                if (type != this.ap || !this.t) {
                    if (type != this.aq) {
                    }
                }
            }
            this.al.registerListener(this.aL, sensor, 0, handler);
        }
    }

    private void m() {
        if (this.av) {
            DLog.d("Recording", "UNREGISTERING SENSORS");
        }
        if (this.al != null && this.aL != null) {
            this.al.unregisterListener(this.aL);
            if (this.aO != null) {
                this.aO.quitSafely();
                this.aO = null;
            }
        }
    }

    private static int[] n() {
        return o();
    }

    private static int[] o() {
        return new int[]{15, 11};
    }

    public b a() {
        return this.aw;
    }

    public void a(int i) {
        this.D = i;
    }

    public void a(MotionHintsListener motionHintsListener) {
        this.ay = motionHintsListener;
        this.az = null;
    }

    public void a(RecordingProgressListener recordingProgressListener) {
        this.ax = recordingProgressListener;
    }

    public void a(a aVar, int i) {
        a(aVar, i, null, null);
    }

    public boolean a(byte[] bArr, byte[] bArr2, long j) {
        boolean z = false;
        d dVar = this.aK;
        dVar.a++;
        if (this.x) {
            this.B++;
            this.C++;
            this.E = j;
            if (this.av) {
                DLog.d("onPreviewFrame", "About to call dropFrameFromRecording");
            }
            if (a(j, bArr, bArr2)) {
                if (this.av) {
                    DLog.d("onPreviewFrame", "framenumber: " + this.A + " -- dropped");
                }
                dVar = this.aK;
                dVar.c++;
            } else {
                if (this.av) {
                    DLog.d("onPreviewFrame", "framenumber: " + this.A + " kept");
                }
                if (!this.y) {
                    this.z = null;
                    this.z = new YuvImage(bArr, 17, this.aA, this.aB, null);
                    this.y = true;
                }
                d dVar2 = this.aK;
                dVar2.b++;
                if (this.av) {
                    DLog.d("onPreviewFrame", "Frame saved!");
                }
                z = true;
            }
            if (this.aK.d == 0) {
                this.aK.d = j;
            }
            this.aK.e = j;
            this.A++;
            return z;
        }
        if (this.av) {
            DLog.d("onPreviewFrame", "Not recording!");
        }
        return false;
    }

    void b() {
        if (this.x) {
            m();
            this.x = false;
            this.az = null;
            this.ay = null;
            DLog.d("AppEvent", com.fyusion.sdk.camera.util.e.FY_FYUSE_RECORDED.a());
            if (this.av) {
                DLog.d("InternalProcessor", this.X.size() + "");
            }
            this.Y.setAppVersionUsedToRecord(FyuseSDK.getVersion());
            this.Y.setUniqueDeviceID(com.fyusion.sdk.common.util.a.a());
            this.Y.setDeviceID(com.fyusion.sdk.common.util.a.b());
            this.Y.setNumberOfMotionFrames(this.X.size());
            this.Y.setMotionFile(j.ai);
            a a = a(false);
            this.Y.setDirectionX(a.a);
            this.Y.setDirectionY(a.b);
            this.Y.setCurvature(a.c);
            this.Y.setGravityX(a.d);
            this.Y.setGravityY(a.e);
            this.Y.setMax_number_frames_(this.at);
            this.Y.setMax_ready_for_more_media_data_check_fails_(j.q);
            this.Y.setCamera_fps_indicator_frame_window_size_(j.s);
            this.Y.setPost_processing_width_(j.C);
            this.Y.setStabilization_smoothing_iterations_(j.I);
            this.Y.setStabilization_smoothing_range_(j.J);
            this.Y.setMax_stabilization_motion_(j.K);
            this.Y.setMax_stabilization_angle_(j.L);
            this.Y.setMax_low_fps_iso_value_(j.R);
            this.Y.setMin_high_fps_iso_value_(j.S);
            this.Y.setNum_frames_to_crop_(3);
            this.Y.setCurrent_version_number_(j.V);
            this.Y.setMjpeg_video_fps_(j.i);
            this.Y.setH264_recording_video_fps_(j.j);
            this.Y.setH264_upload_video_fps_(j.k);
            this.Y.setReady_for_more_media_data_sleep_time_(j.r);
            this.Y.setFyuse_quality_(j.Q);
            this.Y.setApply_face_detection_for_stabilization_(j.B);
            this.Y.setWrite_mjpeg_fyuse_using_gpu_(j.E);
            this.Y.setEnable_standard_unsharpen_filter_(j.X);
            this.Y.setWasRecordedInSelfiePanoramaMode(false);
            Date date = new Date();
            date.setSeconds_since_1970((double) (System.currentTimeMillis() / 1000));
            this.Y.setCreationData(date);
            if (this.av) {
                DLog.d("resetRecordingInfo", "Number of highResolutionSlices: " + this.Y.getNumberOfSlices());
            }
            a(this.Y);
            this.au.a(this.X).a(this.j).a(this.Y).e();
            this.j.releaseData();
            this.j = null;
        }
    }

    public void c() {
        this.Y.setNumberOfCameraFrames((int) this.e.size());
        this.Y.setFrameTimestamps(this.e);
    }

    public Bitmap d() throws IOException {
        return com.fyusion.sdk.common.ext.util.a.a(i(), this.Y);
    }

    public int e() {
        return this.b;
    }
}
