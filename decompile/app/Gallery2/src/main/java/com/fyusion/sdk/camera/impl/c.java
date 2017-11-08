package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
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
import android.os.SystemClock;
import android.util.Log;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.fyusion.sdk.camera.CameraStatus;
import com.fyusion.sdk.camera.CaptureEventListener;
import com.fyusion.sdk.camera.FyuseCamera.RotationDirection;
import com.fyusion.sdk.camera.FyuseCameraException;
import com.fyusion.sdk.camera.MotionHints;
import com.fyusion.sdk.camera.MotionHints.Hint;
import com.fyusion.sdk.camera.MotionHintsListener;
import com.fyusion.sdk.camera.RecordingProgressListener;
import com.fyusion.sdk.camera.SnapShotCallback;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.ext.Size;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.m;
import com.fyusion.sdk.common.ext.n;
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
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/* compiled from: Unknown */
public class c {
    private static int am = -1;
    private static int an = -1;
    private static int ao = -1;
    private static int ap = -1;
    private static int aq = -1;
    private static boolean ar = false;
    public static final int[] f = new int[]{10, 1};
    public static final int[] g = new int[]{9, 10, 1};
    public static final int[] h = new int[]{2, 14};
    public static final int[] i = new int[]{4, 16};
    private float A;
    private float B;
    private float C;
    private float D;
    private float E;
    private float F;
    private float G;
    private float H;
    private float I;
    private float J;
    private int K;
    private List<l> L;
    private List<m> M;
    private e N;
    private m O;
    private float[] P;
    private float[] Q;
    private float[] R;
    private m S = null;
    private m T;
    private a U;
    private float[] V = null;
    private boolean W = false;
    private int X = -1;
    private int[] Y = new int[5];
    private int Z = -1;
    public int a;
    private boolean aA = true;
    private int aB = 0;
    private YuvImage aC = null;
    private boolean aD = false;
    private int aE = 0;
    private int aF = 0;
    private int aG = 0;
    private int aH = 0;
    private MotionHintsListener aI;
    private MotionHints aJ = null;
    private List<CaptureEventListener> aK;
    private AtomicBoolean aL = new AtomicBoolean(false);
    private CameraStatus aM;
    private n aN;
    private int aO;
    private int aP;
    private int aQ;
    private int aR;
    private boolean aS = false;
    private boolean aT = false;
    private k aU;
    private List<RecordingProgressListener> aV;
    private final SensorEventListener aW = new SensorEventListener(this) {
        final /* synthetic */ c a;

        {
            this.a = r1;
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (this.a.aD) {
                DLog.d("onSensorChanged", "sensor event received from sensor " + sensorEvent.sensor.getName());
            }
            if (this.a.T == null) {
                this.a.T = new m();
            }
            if (!this.a.as) {
                this.a.as = true;
            }
            if (!this.a.W) {
                float a;
                float f;
                float[] fArr;
                float[] fArr2;
                if (this.a.k && sensorEvent.sensor.getType() == c.am) {
                    a = this.a.a(sensorEvent);
                    if (this.a.aD) {
                        DLog.e("progressEstimate", "Current progress: " + a);
                    }
                }
                Sensor sensor = sensorEvent.sensor;
                long j = sensorEvent.timestamp;
                long j2 = sensorEvent.timestamp / 1000000;
                int type = sensor.getType();
                if (type == c.ao) {
                    f = sensorEvent.values[0];
                    float f2 = sensorEvent.values[1];
                    float f3 = sensorEvent.values[2];
                    this.a.ac = this.a.ab;
                    this.a.ab = (float) Math.sqrt((double) (((f * f) + (f2 * f2)) + (f3 * f3)));
                    this.a.aa = (this.a.ab - this.a.ac) + (this.a.aa * 0.9f);
                    this.a.T.c.a = sensorEvent.values[0];
                    this.a.T.c.b = sensorEvent.values[1];
                    this.a.T.c.c = sensorEvent.values[2];
                    this.a.T.d = j2;
                    this.a.T.e = j;
                    this.a.U.a = true;
                }
                if (type == c.ap && this.a.aj) {
                    this.a.al = sensorEvent.values;
                    this.a.T.n = sensorEvent.values;
                    this.a.T.o = j2;
                    this.a.T.p = j;
                    this.a.U.d = true;
                }
                if (type == c.aq) {
                    this.a.T.i.a = sensorEvent.values[0];
                    this.a.T.i.b = sensorEvent.values[1];
                    this.a.T.i.c = sensorEvent.values[2];
                    this.a.U.e = true;
                }
                if (type == c.an) {
                    this.a.ak = sensorEvent.values;
                    this.a.T.f.a = (-sensorEvent.values[0]) / 9.80665f;
                    this.a.T.f.b = (-sensorEvent.values[1]) / 9.80665f;
                    this.a.T.f.c = (-sensorEvent.values[2]) / 9.80665f;
                    this.a.T.g = j2;
                    this.a.T.h = j;
                    this.a.U.b = true;
                }
                if (type == c.am && !c.ar && !this.a.U.c) {
                    if (sensorEvent.values.length <= 4) {
                        SensorManager.getRotationMatrixFromVector(this.a.T.j, sensorEvent.values);
                    } else {
                        if (this.a.V == null) {
                            this.a.V = new float[4];
                        }
                        System.arraycopy(sensorEvent.values, 0, this.a.V, 0, 4);
                        SensorManager.getRotationMatrixFromVector(this.a.T.j, this.a.V);
                    }
                    fArr = new float[3];
                    SensorManager.getOrientation(this.a.T.j, fArr);
                    c.b(this.a.T.j, fArr);
                    this.a.T.l = j2;
                    this.a.T.m = j;
                    this.a.U.c = true;
                } else if (!(!c.ar || this.a.ak == null || this.a.al == null || this.a.U.c)) {
                    if (this.a.aD) {
                        DLog.d("onSensorChanged", "GOT IGNORE_GYRO INFO!");
                    }
                    fArr = new float[9];
                    if (SensorManager.getRotationMatrix(fArr, new float[9], this.a.ak, this.a.al)) {
                        fArr2 = new float[3];
                        SensorManager.getOrientation(fArr, fArr2);
                        this.a.T.j[0] = fArr[0];
                        this.a.T.j[1] = fArr[1];
                        this.a.T.j[2] = fArr[2];
                        this.a.T.j[3] = 0.0f;
                        this.a.T.j[4] = fArr[3];
                        this.a.T.j[5] = fArr[4];
                        this.a.T.j[6] = fArr[5];
                        this.a.T.j[7] = 0.0f;
                        this.a.T.j[8] = fArr[6];
                        this.a.T.j[9] = fArr[7];
                        this.a.T.j[10] = fArr[8];
                        this.a.T.j[11] = 0.0f;
                        this.a.T.j[12] = 0.0f;
                        this.a.T.j[13] = 0.0f;
                        this.a.T.j[14] = 0.0f;
                        this.a.T.j[15] = WMElement.CAMERASIZEVALUE1B1;
                        c.b(this.a.T.j, fArr2);
                        this.a.T.l = j2;
                        this.a.T.m = j;
                        this.a.U.c = true;
                        this.a.ak = null;
                        this.a.al = null;
                    }
                }
                if (this.a.U.c && this.a.U.b && this.a.U.a) {
                    if (!this.a.aj || this.a.U.d) {
                        this.a.W = true;
                        this.a.U.c = false;
                        this.a.U.b = false;
                        this.a.U.a = false;
                        if (this.a.U.d) {
                            this.a.U.d = false;
                        }
                        if (this.a.U.e) {
                            this.a.U.e = false;
                        }
                        this.a.T.a = ((double) this.a.T.l) / 1000.0d;
                        if (!this.a.k) {
                            if (this.a.aD) {
                                DLog.d("onSensorChanged", "imu has NOT stabilized");
                            }
                            FloatVec floatVec = new FloatVec();
                            FloatVec floatVec2 = new FloatVec(16);
                            for (type = 0; type < 16; type++) {
                                floatVec2.set(type, this.a.T.j[type]);
                            }
                            FyuseWrapper.getAngleAxis(floatVec2, floatVec);
                            a = floatVec.get(2);
                            float f4 = floatVec.get(0);
                            f = -floatVec.get(1);
                            if (this.a.O == null) {
                                if (this.a.aD) {
                                    DLog.d("onSensorChanged", "mPrevSampleForStabilization == null");
                                }
                                this.a.O = new m();
                                this.a.O.c = a;
                                this.a.O.b = f4;
                                this.a.O.a = f;
                                this.a.W = false;
                                return;
                            } else if (c.b(f, this.a.O.a, 0.1f) && c.b(f4, this.a.O.b, 0.1f) && c.b(a, this.a.O.c, 0.1f)) {
                                this.a.O.c = a;
                                this.a.O.b = f4;
                                this.a.O.a = f;
                                c cVar = this.a;
                                cVar.b++;
                                if (this.a.b <= 5) {
                                    this.a.W = false;
                                    return;
                                }
                                this.a.k = true;
                                if (this.a.aD) {
                                    DLog.d("onSensorChanged", "IMU Has stabilized!");
                                }
                            } else {
                                this.a.O.c = a;
                                this.a.O.b = f4;
                                this.a.O.a = f;
                                this.a.b = 0;
                                this.a.W = false;
                                return;
                            }
                        }
                        if (this.a.P == null) {
                            this.a.P = (float[]) this.a.T.j.clone();
                            this.a.R = (float[]) this.a.P.clone();
                            Matrix.invertM(this.a.R, 0, this.a.P, 0);
                            this.a.y = Math.abs(this.a.T.f.a) < Math.abs(this.a.T.f.b);
                            this.a.z = true;
                        }
                        fArr = (float[]) this.a.T.j.clone();
                        Matrix.multiplyMM(fArr, 0, (float[]) this.a.T.j.clone(), 0, this.a.R, 0);
                        m mVar = new m();
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
                        mVar2.c = this.a.T.c;
                        mVar2.f = this.a.T.f;
                        if (this.a.S != null) {
                            n nVar;
                            while (((double) Math.abs(this.a.S.k.a - mVar2.k.a)) > 3.141592653589793d) {
                                nVar = mVar2.k;
                                nVar.a = (float) (((double) nVar.a) + (((double) ((this.a.S.k.a > mVar2.k.a ? 1 : -1) * 2)) * 3.141592653589793d));
                            }
                            while (((double) Math.abs(this.a.S.k.b - mVar2.k.b)) > 3.141592653589793d) {
                                nVar = mVar2.k;
                                nVar.b = (float) (((double) nVar.b) + (((double) ((this.a.S.k.b > mVar2.k.b ? 1 : -1) * 2)) * 3.141592653589793d));
                            }
                            while (((double) Math.abs(this.a.S.k.c - mVar2.k.c)) > 3.141592653589793d) {
                                nVar = mVar2.k;
                                nVar.c = (float) (((double) nVar.c) + (((double) ((this.a.S.k.c > mVar2.k.c ? 1 : -1) * 2)) * 3.141592653589793d));
                            }
                        }
                        mVar2.i = this.a.T.i;
                        mVar2.n = this.a.T.n;
                        mVar2.a = this.a.T.a;
                        if (this.a.aU.f == 0) {
                            this.a.aU.f = (long) (this.a.T.a * 1000.0d);
                        }
                        this.a.aU.g = (long) (this.a.T.a * 1000.0d);
                        com.fyusion.sdk.common.ext.m.a aVar = this.a.T.c;
                        if (this.a.ay) {
                            this.a.S = mVar2;
                            if (this.a.M.size() > 10) {
                                this.a.s = mVar2.k.a;
                                this.a.t = mVar2.k.b;
                                f = (float) Math.sqrt((((double) this.a.s) * ((double) this.a.s)) + (((double) this.a.t) * ((double) this.a.t)));
                                this.a.s = this.a.s / f;
                                this.a.t = this.a.t / f;
                                if (Float.isNaN(this.a.s) || Float.isNaN(this.a.t)) {
                                    this.a.s = WMElement.CAMERASIZEVALUE1B1;
                                    this.a.t = 0.0f;
                                }
                                if (this.a.aD) {
                                    DLog.d("onSensorChanged", "mImuDirectionX: " + this.a.s + " mImuDirectionY: " + this.a.t);
                                }
                                if (!this.a.r) {
                                    DLog.d("imuDirection", "mImuDirectionHasSettled");
                                    this.a.r = true;
                                }
                            }
                            if (this.a.Q == null) {
                                this.a.Q = this.a.P;
                            }
                            fArr2 = new float[]{mVar.b, mVar.a, mVar.c};
                            float[] fArr3 = (float[]) this.a.Q.clone();
                            fArr2 = (float[]) fArr.clone();
                            Matrix.invertM(fArr3, 0, this.a.Q, 0);
                            Matrix.multiplyMM(fArr2, 0, fArr, 0, fArr3, 0);
                            floatVec3 = new FloatVec();
                            floatVec4 = new FloatVec(16);
                            for (int i2 = 0; i2 < 16; i2++) {
                                floatVec4.set(i2, fArr2[i2]);
                            }
                            FyuseWrapper.getAngleAxis(floatVec4, floatVec3);
                            l lVar = new l();
                            if (this.a.L.size() < 1) {
                                lVar.a = 0.0f;
                                lVar.b = 0.0f;
                            } else {
                                l lVar2 = (l) this.a.L.get(this.a.L.size() - 1);
                                lVar.a = (lVar2.a - (floatVec3.get(1) * this.a.s)) + (floatVec3.get(0) * this.a.t);
                                lVar.b = (lVar2.b - (floatVec3.get(1) * this.a.t)) - (floatVec3.get(0) * this.a.s);
                            }
                            if (Float.isNaN(lVar.a)) {
                                DLog.w("CameraProcessor", "Hit NaN progress!!!!");
                            }
                            lVar.c = this.a.T.a;
                            this.a.L.add(lVar);
                            this.a.Q = null;
                            this.a.Q = (float[]) fArr.clone();
                        }
                        if (((aVar.a * aVar.a) + (aVar.b * aVar.b)) + (aVar.c * aVar.c) >= 6.25E-6f) {
                            com.fyusion.sdk.common.ext.m.a aVar2 = new com.fyusion.sdk.common.ext.m.a();
                            aVar2.a = ((this.a.T.j[0] * aVar.a) + (this.a.T.j[4] * aVar.b)) + (this.a.T.j[8] * aVar.c);
                            aVar2.b = ((this.a.T.j[1] * aVar.a) + (this.a.T.j[5] * aVar.b)) + (this.a.T.j[9] * aVar.c);
                            aVar2.c = ((this.a.T.j[2] * aVar.a) + (this.a.T.j[6] * aVar.b)) + (this.a.T.j[10] * aVar.c);
                            this.a.M.add(mVar2);
                            k r = this.a.aU;
                            r.h++;
                            this.a.T = null;
                            this.a.W = false;
                        }
                    }
                }
            }
        }
    };
    private long aX = 0;
    private double aY = 0.0d;
    private HandlerThread aZ;
    private float aa;
    private float ab;
    private float ac;
    private SensorManager ad;
    private boolean ae = false;
    private boolean af = false;
    private boolean ag = false;
    private boolean ah = false;
    private boolean ai = false;
    private boolean aj = false;
    private float[] ak;
    private float[] al;
    private boolean as;
    private int at = 0;
    private float[] au;
    private RotationDirection av;
    private int aw;
    private com.fyusion.sdk.camera.a.a ax;
    private boolean ay = false;
    private boolean az = false;
    int b;
    boolean c = false;
    ImuProgressEstimatorWrapper d;
    float[] e = null;
    private int j;
    private boolean k;
    private double l;
    private FyuseFrameInformationVec m;
    private int n;
    private int o;
    private boolean p;
    private int q;
    private boolean r;
    private float s;
    private float t;
    private boolean u;
    private boolean v;
    private float w;
    private OnlineImageStabilizerWrapper x = null;
    private boolean y;
    private boolean z = false;

    /* compiled from: Unknown */
    private class a {
        boolean a;
        boolean b;
        boolean c;
        boolean d;
        boolean e;
        final /* synthetic */ c f;

        private a(c cVar) {
            this.f = cVar;
            this.a = false;
            this.b = false;
            this.c = false;
            this.d = false;
            this.e = false;
        }
    }

    public c(CameraStatus cameraStatus, Size size, Size size2) {
        DLog.i("CameraProcessor", "Capture resolution: " + size.height);
        this.aM = cameraStatus;
        this.aO = size.width;
        this.aP = size.height;
        this.aQ = size2.width;
        this.aR = size2.height;
        q();
        this.aN = new n(size.width, size.height, cameraStatus.isPortraitMode());
        this.L = Collections.synchronizedList(new LinkedList());
        this.M = Collections.synchronizedList(new LinkedList());
        this.N = new e();
        this.U = new a();
        n();
    }

    private float a(SensorEvent sensorEvent) {
        if (this.at <= 0 || this.d == null) {
            return 0.0f;
        }
        if (this.e == null) {
            this.e = new float[4];
        }
        SensorManager.getQuaternionFromVector(this.e, sensorEvent.values);
        double d = 0.0d;
        if (this.d != null) {
            d = this.d.getProgressUsingQuaternions(this.e[0], this.e[1], this.e[2], this.e[3]);
        }
        double d2 = d;
        if (d2 > this.aY + 0.009999999776482582d) {
            this.aY = d2;
            for (RecordingProgressListener onProgress : this.aV) {
                onProgress.onProgress(d2, this.at);
            }
        }
        return ((float) d2) * ((float) this.at);
    }

    private static int a(int i, int i2, int[] iArr) {
        int i3 = 0;
        while (i3 < iArr.length) {
            if (i == iArr[i3] || i2 == iArr[i3]) {
                return iArr[i3];
            }
            i3++;
        }
        return -1;
    }

    private m a(double d) {
        int size = this.M.size();
        if (size > 0) {
            for (int i = size - 1; i >= 0; i--) {
                m mVar = (m) this.M.get(i);
                if (mVar != null && mVar.a <= d) {
                    return mVar;
                }
            }
        }
        return null;
    }

    private void a(int i, float[] fArr, RotationDirection rotationDirection) {
        if (this.d == null) {
            this.d = new ImuProgressEstimatorWrapper();
        }
        this.d.setTargetRotationAmount(((((double) ((float) i)) / 360.0d) * 2.0d) * 3.141592653589793d);
        if (fArr != null && fArr.length == 3) {
            this.d.setAxisOfRotation(fArr[0], fArr[1], fArr[2]);
        }
        ImuProgressEstimatorWrapper.RotationDirection rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.UNSPECIFIED;
        if (rotationDirection == RotationDirection.CLOCKWISE) {
            rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.CLOCKWISE;
        } else if (rotationDirection == RotationDirection.COUNTERCLOCKWISE) {
            rotationDirection2 = ImuProgressEstimatorWrapper.RotationDirection.COUNTERCLOCKWISE;
        }
        this.d.setRotationDirection(rotationDirection2);
        if (i <= 270) {
            this.aw = 300;
        } else {
            this.aw = 1000;
        }
        for (RecordingProgressListener onProgress : this.aV) {
            onProgress.onProgress(0.0d, i);
        }
    }

    private void a(Hint hint) {
        if (this.aI != null) {
            MotionHints motionHints = new MotionHints(hint);
            if (!motionHints.equals(this.aJ)) {
                this.aJ = motionHints;
                this.aI.onMotionDataEvent(motionHints);
            }
        }
    }

    private void a(e eVar) {
        Location a = com.fyusion.sdk.camera.util.c.a(FyuseSDK.getContext()).a();
        if (this.aD) {
            DLog.d("saveLocationToMagic", "location " + a);
        }
        if (a != null) {
            FyusePlacemark fyusePlacemark = new FyusePlacemark();
            fyusePlacemark.setLatitude((float) a.getLatitude());
            fyusePlacemark.setLongitude((float) a.getLongitude());
            eVar.setPlacemark(fyusePlacemark);
        }
    }

    private boolean a(int i) {
        return (i == am || i == ao || i == an) ? true : (i == ap && this.aj) || i == aq;
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
    private boolean a(long j, byte[] bArr, byte[] bArr2) {
        if (!this.ay) {
            return true;
        }
        this.j++;
        if (this.j <= 3 && !this.aS) {
            if (this.aD) {
                DLog.d("dropFrameFromRecording", "Dropped: YES  | mUncroppedFrameCount <= GlobalConstants.g_NUM_FRAMES_TO_CROP");
            }
            return true;
        } else if (this.a >= this.aw) {
            if (this.aD) {
                DLog.d("dropFrameFromRecording", "Dropped: YES | Too many frames were recorded!");
            }
            return true;
        } else if (this.k || this.aS) {
            double d = ((double) j) / 1000.0d;
            if (this.l == 0.0d) {
                this.l = d;
                return true;
            }
            double d2 = d - this.l;
            this.l = d;
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
                fyuseFrameInformation.setHas_imu_direction_settled(this.r);
                fyuseFrameInformation.setImu_direction_x((double) this.s);
                fyuseFrameInformation.setImu_direction_y((double) this.t);
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "mIsBackCamera: " + this.u + " mMirrorVertically: " + this.v);
                }
                long nanoTime = System.nanoTime();
                if (this.x == null) {
                    this.x = new OnlineImageStabilizerWrapper();
                }
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "processYV12Frame : data " + bArr.length);
                }
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "processYV12Frame : w x h  --> " + this.aO + " X " + this.aP);
                }
                if (bArr2 != null) {
                    if (this.aD) {
                        DLog.d("dropFrameFromRecording", "processYV12Frame with depth");
                    }
                    this.x.processYV12FrameWithDepth(bArr, this.aO, this.aP, this.aO, this.v, bArr2, 640, 360, 720);
                } else {
                    this.x.processYV12Frame(bArr, this.aO, this.aP, this.aO, this.v);
                }
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "processYV12Frame took: " + (((float) (System.nanoTime() - nanoTime)) / 1000000.0f));
                }
                s();
                this.A = a.k.a;
                this.B = a.k.b;
                this.C = a.k.c;
                this.D = a.c.a;
                this.E = a.c.b;
                float f3 = this.A - this.F;
                float f4 = this.B - this.G;
                float f5 = this.C - this.H;
                float f6 = this.D - this.I;
                float f7 = this.E - this.J;
                if (this.m.size() != 0) {
                    if ((Math.abs(f3) > 0.01f ? 1 : null) == null) {
                        if ((Math.abs(f4) > 0.01f ? 1 : null) == null) {
                            if ((Math.abs(f5) > 0.01f ? 1 : null) == null) {
                                if ((Math.abs(f6) > 0.01f ? 1 : null) == null && Math.abs(f7) <= 0.01f) {
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
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
                l b = b(d);
                if (b == null) {
                    b = new l();
                    b.a = 0.0f;
                    b.c = d;
                    b.b = 0.0f;
                }
                float f8 = b.a;
                f3 = b.b;
                f3 = this.n <= this.o ? 0.017453292f : 0.008726646f;
                if (this.r) {
                    if (this.w > 0.0f) {
                        f4 = f8 - this.w;
                    }
                    if (this.w <= 0.0f || f8 - this.w >= f3) {
                        f7 = f4;
                        obj = null;
                        int i3 = 1;
                        if ((((double) f7) / d2 > 0.75d ? 1 : null) == null) {
                            this.q = 5;
                            if (!this.p) {
                                a(Hint.MOVING_TOO_FAST);
                            }
                            this.p = true;
                        } else if (this.p) {
                            this.q--;
                            if (this.q <= 0) {
                                this.p = false;
                            }
                        }
                        i = this.u ? 1 : -1;
                        if (f8 > 0.08726646f) {
                            f5 = this.x.computeRotationCompensatedFlow(this.s, ((float) i) * this.t, f8 - this.w);
                            f4 = ((float) (this.n <= this.o ? 1 : -1)) * f5;
                            if (this.aD) {
                                DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                            }
                        } else {
                            if (this.aD) {
                                DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                            }
                            f4 = this.x.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                            f5 = this.x.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                            f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                            f5 = f4;
                        }
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                        }
                        f = (float) ((this.n + 1) / (this.o + 1));
                        f2 = (float) (this.n < this.o ? 6 : 3);
                        if (this.n + this.o >= 5) {
                            if (((double) f) > 0.25d && ((double) f) < 0.75d) {
                            }
                            f6 = f4;
                            if (obj2 == null) {
                                if (this.n >= this.o) {
                                    if ((f6 <= f2 ? 1 : null) == null) {
                                        if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                        }
                                    }
                                }
                                obj3 = null;
                                if (obj3 == null) {
                                    if (this.o > this.n && r7 != null) {
                                    }
                                    obj4 = 1;
                                    if (!this.p) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.aD) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.o++;
                                            } else {
                                                this.n++;
                                            }
                                        }
                                        this.w = f8;
                                        this.F = this.A;
                                        this.G = this.B;
                                        this.H = this.C;
                                        this.I = this.D;
                                        this.J = this.E;
                                        this.a++;
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.m.add(fyuseFrameInformation);
                                        this.x.addProcessedFrame(true);
                                        System.nanoTime();
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    if (obj4 != null && obj3 == null) {
                                        i2 = this.K + 1;
                                        this.K = i2;
                                        if (i2 > 3) {
                                            if (this.o > this.n || !this.u) {
                                                a(Hint.MOVING_BACKWARDS);
                                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                                if (this.K >= 12) {
                                                    if (this.aD) {
                                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                                    }
                                                    DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                                    try {
                                                        this.aT = true;
                                                        this.aM.getFyuseCamera().stopRecording();
                                                    } catch (FyuseCameraException e) {
                                                        DLog.w("CameraProcessor", "stopRecording :: exception : " + e.getMessage());
                                                        e.printStackTrace();
                                                    }
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    }
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                                obj4 = null;
                                if (this.p) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    i2 = this.K + 1;
                                    this.K = i2;
                                    if (i2 > 3) {
                                        if (this.o > this.n) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                        if (this.K >= 12) {
                                            if (this.aD) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                            this.aT = true;
                                            this.aM.getFyuseCamera().stopRecording();
                                            return true;
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    }
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                                if (this.aD) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.n++;
                                    } else {
                                        this.o++;
                                    }
                                }
                                this.w = f8;
                                this.F = this.A;
                                this.G = this.B;
                                this.H = this.C;
                                this.I = this.D;
                                this.J = this.E;
                                this.a++;
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(true);
                                System.nanoTime();
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            obj3 = 1;
                            if (obj3 == null) {
                                if (this.o > this.n) {
                                    obj4 = 1;
                                    if (this.p) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.aD) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.o++;
                                            } else {
                                                this.n++;
                                            }
                                        }
                                        this.w = f8;
                                        this.F = this.A;
                                        this.G = this.B;
                                        this.H = this.C;
                                        this.I = this.D;
                                        this.J = this.E;
                                        this.a++;
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.m.add(fyuseFrameInformation);
                                        this.x.addProcessedFrame(true);
                                        System.nanoTime();
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    i2 = this.K + 1;
                                    this.K = i2;
                                    if (i2 > 3) {
                                        if (this.o > this.n) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                        if (this.K >= 12) {
                                            if (this.aD) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                            this.aT = true;
                                            this.aM.getFyuseCamera().stopRecording();
                                            return true;
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    }
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                            }
                            obj4 = null;
                            if (this.p) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                i2 = this.K + 1;
                                this.K = i2;
                                if (i2 > 3) {
                                    if (this.o > this.n) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                    if (this.K >= 12) {
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                        this.aT = true;
                                        this.aM.getFyuseCamera().stopRecording();
                                        return true;
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(false);
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                }
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                            if (this.aD) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.n++;
                                } else {
                                    this.o++;
                                }
                            }
                            this.w = f8;
                            this.F = this.A;
                            this.G = this.B;
                            this.H = this.C;
                            this.I = this.D;
                            this.J = this.E;
                            this.a++;
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(true);
                            System.nanoTime();
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                        f4 = Math.abs(f5);
                        f6 = f4;
                        if (obj2 == null) {
                            if (this.n >= this.o) {
                                if (f6 <= f2) {
                                }
                                if ((f6 <= f2 ? 1 : null) == null) {
                                    if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                    }
                                }
                            }
                            obj3 = null;
                            if (obj3 == null) {
                                if (this.o > this.n) {
                                    obj4 = 1;
                                    if (this.p) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.aD) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.o++;
                                            } else {
                                                this.n++;
                                            }
                                        }
                                        this.w = f8;
                                        this.F = this.A;
                                        this.G = this.B;
                                        this.H = this.C;
                                        this.I = this.D;
                                        this.J = this.E;
                                        this.a++;
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.m.add(fyuseFrameInformation);
                                        this.x.addProcessedFrame(true);
                                        System.nanoTime();
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    i2 = this.K + 1;
                                    this.K = i2;
                                    if (i2 > 3) {
                                        if (this.o > this.n) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                        if (this.K >= 12) {
                                            if (this.aD) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                            this.aT = true;
                                            this.aM.getFyuseCamera().stopRecording();
                                            return true;
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    }
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                            }
                            obj4 = null;
                            if (this.p) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                i2 = this.K + 1;
                                this.K = i2;
                                if (i2 > 3) {
                                    if (this.o > this.n) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                    if (this.K >= 12) {
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                        this.aT = true;
                                        this.aM.getFyuseCamera().stopRecording();
                                        return true;
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(false);
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                }
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                            if (this.aD) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.n++;
                                } else {
                                    this.o++;
                                }
                            }
                            this.w = f8;
                            this.F = this.A;
                            this.G = this.B;
                            this.H = this.C;
                            this.I = this.D;
                            this.J = this.E;
                            this.a++;
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(true);
                            System.nanoTime();
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                        obj3 = 1;
                        if (obj3 == null) {
                            if (this.o > this.n) {
                                obj4 = 1;
                                if (this.p) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (this.aD) {
                                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                    }
                                    if (f8 > 0.08726646f) {
                                        if (((double) f5) <= -4.0d) {
                                            this.o++;
                                        } else {
                                            this.n++;
                                        }
                                    }
                                    this.w = f8;
                                    this.F = this.A;
                                    this.G = this.B;
                                    this.H = this.C;
                                    this.I = this.D;
                                    this.J = this.E;
                                    this.a++;
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(false);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(true);
                                    System.nanoTime();
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                    }
                                    return false;
                                }
                                i2 = this.K + 1;
                                this.K = i2;
                                if (i2 > 3) {
                                    if (this.o > this.n) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                    if (this.K >= 12) {
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                        this.aT = true;
                                        this.aM.getFyuseCamera().stopRecording();
                                        return true;
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(false);
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                }
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                        }
                        obj4 = null;
                        if (this.p) {
                            a(Hint.MOVING_CORRECTLY);
                        }
                        if (obj3 == null) {
                            i2 = this.K + 1;
                            this.K = i2;
                            if (i2 > 3) {
                                if (this.o > this.n) {
                                }
                                a(Hint.MOVING_BACKWARDS);
                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                if (this.K >= 12) {
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                    }
                                    DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                    this.aT = true;
                                    this.aM.getFyuseCamera().stopRecording();
                                    return true;
                                }
                            }
                            fyuseFrameInformation.setIs_dropped_online(true);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(false);
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                            }
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                            }
                            return false;
                        }
                        if (this.aD) {
                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                        }
                        if (f8 > 0.08726646f) {
                            if (((double) f5) <= -4.0d) {
                                this.n++;
                            } else {
                                this.o++;
                            }
                        }
                        this.w = f8;
                        this.F = this.A;
                        this.G = this.B;
                        this.H = this.C;
                        this.I = this.D;
                        this.J = this.E;
                        this.a++;
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                        }
                        fyuseFrameInformation.setIs_dropped_online(false);
                        this.m.add(fyuseFrameInformation);
                        this.x.addProcessedFrame(true);
                        System.nanoTime();
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                        }
                        return false;
                    }
                    obj5 = null;
                    if (f8 - this.w < -0.08726646f) {
                        f7 = f4;
                        int i4 = 1;
                        obj2 = null;
                        if (((double) f7) / d2 > 0.75d) {
                        }
                        if ((((double) f7) / d2 > 0.75d ? 1 : null) == null) {
                            this.q = 5;
                            if (this.p) {
                                a(Hint.MOVING_TOO_FAST);
                            }
                            this.p = true;
                        } else if (this.p) {
                            this.q--;
                            if (this.q <= 0) {
                                this.p = false;
                            }
                        }
                        if (this.u) {
                        }
                        if (f8 > 0.08726646f) {
                            if (this.aD) {
                                DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                            }
                            f4 = this.x.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                            f5 = this.x.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                            f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                            f5 = f4;
                        } else {
                            f5 = this.x.computeRotationCompensatedFlow(this.s, ((float) i) * this.t, f8 - this.w);
                            if (this.n <= this.o) {
                            }
                            f4 = ((float) (this.n <= this.o ? 1 : -1)) * f5;
                            if (this.aD) {
                                DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                            }
                        }
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                        }
                        f = (float) ((this.n + 1) / (this.o + 1));
                        if (this.n < this.o) {
                        }
                        f2 = (float) (this.n < this.o ? 6 : 3);
                        if (this.n + this.o >= 5) {
                        }
                        f4 = Math.abs(f5);
                        f6 = f4;
                        if (obj2 == null) {
                            if (this.n >= this.o) {
                                if (f6 <= f2) {
                                }
                                if ((f6 <= f2 ? 1 : null) == null) {
                                    if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                                    }
                                }
                            }
                            obj3 = null;
                            if (obj3 == null) {
                                if (this.o > this.n) {
                                    obj4 = 1;
                                    if (this.p) {
                                        a(Hint.MOVING_CORRECTLY);
                                    }
                                    if (obj3 == null) {
                                        if (this.aD) {
                                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                        }
                                        if (f8 > 0.08726646f) {
                                            if (((double) f5) <= -4.0d) {
                                                this.o++;
                                            } else {
                                                this.n++;
                                            }
                                        }
                                        this.w = f8;
                                        this.F = this.A;
                                        this.G = this.B;
                                        this.H = this.C;
                                        this.I = this.D;
                                        this.J = this.E;
                                        this.a++;
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                        }
                                        fyuseFrameInformation.setIs_dropped_online(false);
                                        this.m.add(fyuseFrameInformation);
                                        this.x.addProcessedFrame(true);
                                        System.nanoTime();
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                        }
                                        return false;
                                    }
                                    i2 = this.K + 1;
                                    this.K = i2;
                                    if (i2 > 3) {
                                        if (this.o > this.n) {
                                        }
                                        a(Hint.MOVING_BACKWARDS);
                                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                        if (this.K >= 12) {
                                            if (this.aD) {
                                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                            }
                                            DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                            this.aT = true;
                                            this.aM.getFyuseCamera().stopRecording();
                                            return true;
                                        }
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(true);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(false);
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                    }
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                    }
                                    return false;
                                }
                            }
                            obj4 = null;
                            if (this.p) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                i2 = this.K + 1;
                                this.K = i2;
                                if (i2 > 3) {
                                    if (this.o > this.n) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                    if (this.K >= 12) {
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                        this.aT = true;
                                        this.aM.getFyuseCamera().stopRecording();
                                        return true;
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(false);
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                }
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                            if (this.aD) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.n++;
                                } else {
                                    this.o++;
                                }
                            }
                            this.w = f8;
                            this.F = this.A;
                            this.G = this.B;
                            this.H = this.C;
                            this.I = this.D;
                            this.J = this.E;
                            this.a++;
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(true);
                            System.nanoTime();
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                        obj3 = 1;
                        if (obj3 == null) {
                            if (this.o > this.n) {
                                obj4 = 1;
                                if (this.p) {
                                    a(Hint.MOVING_CORRECTLY);
                                }
                                if (obj3 == null) {
                                    if (this.aD) {
                                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                    }
                                    if (f8 > 0.08726646f) {
                                        if (((double) f5) <= -4.0d) {
                                            this.o++;
                                        } else {
                                            this.n++;
                                        }
                                    }
                                    this.w = f8;
                                    this.F = this.A;
                                    this.G = this.B;
                                    this.H = this.C;
                                    this.I = this.D;
                                    this.J = this.E;
                                    this.a++;
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                    }
                                    fyuseFrameInformation.setIs_dropped_online(false);
                                    this.m.add(fyuseFrameInformation);
                                    this.x.addProcessedFrame(true);
                                    System.nanoTime();
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                    }
                                    return false;
                                }
                                i2 = this.K + 1;
                                this.K = i2;
                                if (i2 > 3) {
                                    if (this.o > this.n) {
                                    }
                                    a(Hint.MOVING_BACKWARDS);
                                    DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                    if (this.K >= 12) {
                                        if (this.aD) {
                                            DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                        }
                                        DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                        this.aT = true;
                                        this.aM.getFyuseCamera().stopRecording();
                                        return true;
                                    }
                                }
                                fyuseFrameInformation.setIs_dropped_online(true);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(false);
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                                }
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                                }
                                return false;
                            }
                        }
                        obj4 = null;
                        if (this.p) {
                            a(Hint.MOVING_CORRECTLY);
                        }
                        if (obj3 == null) {
                            i2 = this.K + 1;
                            this.K = i2;
                            if (i2 > 3) {
                                if (this.o > this.n) {
                                }
                                a(Hint.MOVING_BACKWARDS);
                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                if (this.K >= 12) {
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                    }
                                    DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                    this.aT = true;
                                    this.aM.getFyuseCamera().stopRecording();
                                    return true;
                                }
                            }
                            fyuseFrameInformation.setIs_dropped_online(true);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(false);
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                            }
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                            }
                            return false;
                        }
                        if (this.aD) {
                            DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                        }
                        if (f8 > 0.08726646f) {
                            if (((double) f5) <= -4.0d) {
                                this.n++;
                            } else {
                                this.o++;
                            }
                        }
                        this.w = f8;
                        this.F = this.A;
                        this.G = this.B;
                        this.H = this.C;
                        this.I = this.D;
                        this.J = this.E;
                        this.a++;
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                        }
                        fyuseFrameInformation.setIs_dropped_online(false);
                        this.m.add(fyuseFrameInformation);
                        this.x.addProcessedFrame(true);
                        System.nanoTime();
                        if (this.aD) {
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
                    this.q = 5;
                    if (this.p) {
                        a(Hint.MOVING_TOO_FAST);
                    }
                    this.p = true;
                } else if (this.p) {
                    this.q--;
                    if (this.q <= 0) {
                        this.p = false;
                    }
                }
                if (this.u) {
                }
                if (f8 > 0.08726646f) {
                    f5 = this.x.computeRotationCompensatedFlow(this.s, ((float) i) * this.t, f8 - this.w);
                    if (this.n <= this.o) {
                    }
                    f4 = ((float) (this.n <= this.o ? 1 : -1)) * f5;
                    if (this.aD) {
                        DLog.i("onDropFrameFromRecording", "We have committed to direction: " + f4);
                    }
                } else {
                    if (this.aD) {
                        DLog.i("onDropFrameFromRecording", "We haven't rotated much, flow is in any direction");
                    }
                    f4 = this.x.computeRotationCompensatedFlow(WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
                    f5 = this.x.computeRotationCompensatedFlow(0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f);
                    f4 = (float) Math.sqrt((double) ((f4 * f4) + (f5 * f5)));
                    f5 = f4;
                }
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "Effective flow: " + f4 + " progress: " + f8);
                }
                f = (float) ((this.n + 1) / (this.o + 1));
                if (this.n < this.o) {
                }
                f2 = (float) (this.n < this.o ? 6 : 3);
                if (this.n + this.o >= 5) {
                }
                f4 = Math.abs(f5);
                f6 = f4;
                if (obj2 == null) {
                    if (this.n >= this.o) {
                        if (f6 <= f2) {
                        }
                        if ((f6 <= f2 ? 1 : null) == null) {
                            if (((double) ((f7 / f3) + (f6 / f2))) > WeightedLatLng.DEFAULT_INTENSITY) {
                            }
                        }
                    }
                    obj3 = null;
                    if (obj3 == null) {
                        if (this.o > this.n) {
                            obj4 = 1;
                            if (this.p) {
                                a(Hint.MOVING_CORRECTLY);
                            }
                            if (obj3 == null) {
                                if (this.aD) {
                                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                                }
                                if (f8 > 0.08726646f) {
                                    if (((double) f5) <= -4.0d) {
                                        this.o++;
                                    } else {
                                        this.n++;
                                    }
                                }
                                this.w = f8;
                                this.F = this.A;
                                this.G = this.B;
                                this.H = this.C;
                                this.I = this.D;
                                this.J = this.E;
                                this.a++;
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                                }
                                fyuseFrameInformation.setIs_dropped_online(false);
                                this.m.add(fyuseFrameInformation);
                                this.x.addProcessedFrame(true);
                                System.nanoTime();
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                                }
                                return false;
                            }
                            i2 = this.K + 1;
                            this.K = i2;
                            if (i2 > 3) {
                                if (this.o > this.n) {
                                }
                                a(Hint.MOVING_BACKWARDS);
                                DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                                if (this.K >= 12) {
                                    if (this.aD) {
                                        DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                    }
                                    DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                    this.aT = true;
                                    this.aM.getFyuseCamera().stopRecording();
                                    return true;
                                }
                            }
                            fyuseFrameInformation.setIs_dropped_online(true);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(false);
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                            }
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                            }
                            return false;
                        }
                    }
                    obj4 = null;
                    if (this.p) {
                        a(Hint.MOVING_CORRECTLY);
                    }
                    if (obj3 == null) {
                        i2 = this.K + 1;
                        this.K = i2;
                        if (i2 > 3) {
                            if (this.o > this.n) {
                            }
                            a(Hint.MOVING_BACKWARDS);
                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                            if (this.K >= 12) {
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                }
                                DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                this.aT = true;
                                this.aM.getFyuseCamera().stopRecording();
                                return true;
                            }
                        }
                        fyuseFrameInformation.setIs_dropped_online(true);
                        this.m.add(fyuseFrameInformation);
                        this.x.addProcessedFrame(false);
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                        }
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                        }
                        return false;
                    }
                    if (this.aD) {
                        DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                    }
                    if (f8 > 0.08726646f) {
                        if (((double) f5) <= -4.0d) {
                            this.n++;
                        } else {
                            this.o++;
                        }
                    }
                    this.w = f8;
                    this.F = this.A;
                    this.G = this.B;
                    this.H = this.C;
                    this.I = this.D;
                    this.J = this.E;
                    this.a++;
                    if (this.aD) {
                        DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                    }
                    fyuseFrameInformation.setIs_dropped_online(false);
                    this.m.add(fyuseFrameInformation);
                    this.x.addProcessedFrame(true);
                    System.nanoTime();
                    if (this.aD) {
                        DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                    }
                    return false;
                }
                obj3 = 1;
                if (obj3 == null) {
                    if (this.o > this.n) {
                        obj4 = 1;
                        if (this.p) {
                            a(Hint.MOVING_CORRECTLY);
                        }
                        if (obj3 == null) {
                            if (this.aD) {
                                DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                            }
                            if (f8 > 0.08726646f) {
                                if (((double) f5) <= -4.0d) {
                                    this.o++;
                                } else {
                                    this.n++;
                                }
                            }
                            this.w = f8;
                            this.F = this.A;
                            this.G = this.B;
                            this.H = this.C;
                            this.I = this.D;
                            this.J = this.E;
                            this.a++;
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                            }
                            fyuseFrameInformation.setIs_dropped_online(false);
                            this.m.add(fyuseFrameInformation);
                            this.x.addProcessedFrame(true);
                            System.nanoTime();
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                            }
                            return false;
                        }
                        i2 = this.K + 1;
                        this.K = i2;
                        if (i2 > 3) {
                            if (this.o > this.n) {
                            }
                            a(Hint.MOVING_BACKWARDS);
                            DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                            if (this.K >= 12) {
                                if (this.aD) {
                                    DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                                }
                                DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                                this.aT = true;
                                this.aM.getFyuseCamera().stopRecording();
                                return true;
                            }
                        }
                        fyuseFrameInformation.setIs_dropped_online(true);
                        this.m.add(fyuseFrameInformation);
                        this.x.addProcessedFrame(false);
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                        }
                        if (this.aD) {
                            DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                        }
                        return false;
                    }
                }
                obj4 = null;
                if (this.p) {
                    a(Hint.MOVING_CORRECTLY);
                }
                if (obj3 == null) {
                    i2 = this.K + 1;
                    this.K = i2;
                    if (i2 > 3) {
                        if (this.o > this.n) {
                        }
                        a(Hint.MOVING_BACKWARDS);
                        DLog.d("imuDirection", "MOVING BACKWARDS: " + this.K);
                        if (this.K >= 12) {
                            if (this.aD) {
                                DLog.d("dropFrameFromRecording", "AUTOCUTTING");
                            }
                            DLog.d("imuDirection", "Dropped: YES | Camera stopped recording for moving backwards.");
                            this.aT = true;
                            this.aM.getFyuseCamera().stopRecording();
                            return true;
                        }
                    }
                    fyuseFrameInformation.setIs_dropped_online(true);
                    this.m.add(fyuseFrameInformation);
                    this.x.addProcessedFrame(false);
                    if (this.aD) {
                        DLog.d("dropFrameFromRecording", "--Effective flow--: " + f6);
                    }
                    if (this.aD) {
                        DLog.d("dropFrameFromRecording", "Dropped: YES | !add_frame: not moving forward");
                    }
                    return false;
                }
                if (this.aD) {
                    DLog.d("imuDirection", "progress: " + f8 + " | flow: " + f5 + " | min: " + 0.08726646f);
                }
                if (f8 > 0.08726646f) {
                    if (((double) f5) <= -4.0d) {
                        this.n++;
                    } else {
                        this.o++;
                    }
                }
                this.w = f8;
                this.F = this.A;
                this.G = this.B;
                this.H = this.C;
                this.I = this.D;
                this.J = this.E;
                this.a++;
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "Concave: " + this.n + " Convex: " + this.o);
                }
                fyuseFrameInformation.setIs_dropped_online(false);
                this.m.add(fyuseFrameInformation);
                this.x.addProcessedFrame(true);
                System.nanoTime();
                if (this.aD) {
                    DLog.d("dropFrameFromRecording", "Dropped: NO | moving correctly");
                }
                return false;
            }
            if (this.aD) {
                DLog.w("dropFrameFromRecording", "Dropped: YES | latest_data == null");
            }
            return true;
        } else {
            if (this.aD) {
                DLog.d("dropFrameFromRecording", "Dropped: YES | IMU has NOT stabilized!");
            }
            return true;
        }
    }

    private byte[] a(byte[] bArr, int i, int i2) {
        int i3;
        byte[] bArr2 = new byte[bArr.length];
        int i4 = i * i2;
        int i5 = (i / 2) * (i2 / 2);
        for (i3 = 0; i3 < i4; i3++) {
            bArr2[i3] = (byte) bArr[i3];
        }
        int i6 = i4;
        for (i3 = i4; i3 < i4 + i5; i3++) {
            int i7 = i6 + 1;
            bArr2[i6] = (byte) bArr[i3];
            i6 = i7 + 1;
            bArr2[i7] = (byte) bArr[i5 + i3];
        }
        return bArr2;
    }

    private l b(double d) {
        l lVar;
        int size = this.L.size();
        if (size <= 0) {
            lVar = null;
        } else {
            if (this.aD) {
                DLog.d("CameraProcessor", "getNearestProgressInfo() : sz = " + size + " mProgressCached.get(0) =  " + ((l) this.L.get(0)).c + " AND timestamp = " + d + " AND elapsedRealtime() : " + SystemClock.elapsedRealtime());
            }
            lVar = null;
            for (int size2 = this.L.size() - 1; size2 >= 0; size2--) {
                lVar = (l) this.L.get(size2);
                if (lVar != null && lVar.c <= d) {
                    return lVar;
                }
            }
        }
        return lVar;
    }

    private void b(int i) {
        this.aH = i;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private f c(boolean z) {
        f fVar = new f();
        if (this.M.size() != 0) {
            float f;
            float f2;
            fVar.c = (float) (this.o <= this.n ? 1 : -1);
            if (this.aD) {
                DLog.d("getEstimatedDirection", "convexVotes: " + this.o + " concaveVotes: " + this.n + " Result: " + fVar.c);
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
            int size = (int) (this.N.getFrameTimestamps().size() - 1);
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
                if (this.aD) {
                    DLog.d("CameraProcessor", "mFrameTimestampList.size() , frameNum " + this.m.size() + ", " + i5);
                }
                FyuseFrameInformation fyuseFrameInformation = this.m.get(i5);
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
            fVar.a = 0.0f;
            fVar.b = 0.0f;
            if (Math.abs(f) > Math.abs(f2)) {
                if (f > 0.0f) {
                    fVar.a = WMElement.CAMERASIZEVALUE1B1;
                } else {
                    fVar.a = GroundOverlayOptions.NO_DIMENSION;
                }
            } else if (f2 > 0.0f) {
                fVar.b = WMElement.CAMERASIZEVALUE1B1;
            } else {
                fVar.b = GroundOverlayOptions.NO_DIMENSION;
            }
            i6 = Math.max(Math.max(Math.max(i, i2), i3), i4);
            if (i2 == i6) {
                fVar.d = 0.0f;
                fVar.e = WMElement.CAMERASIZEVALUE1B1;
            } else if (i4 != i6) {
                if (i != i6) {
                    fVar.d = GroundOverlayOptions.NO_DIMENSION;
                } else {
                    fVar.d = WMElement.CAMERASIZEVALUE1B1;
                }
                fVar.e = 0.0f;
            } else {
                fVar.d = 0.0f;
                fVar.e = GroundOverlayOptions.NO_DIMENSION;
            }
        } else {
            if (this.aD) {
                DLog.e("getEstimatedDirection", "[CameraViewController::getEstimatedDirection] Had no motion readings saved; defaulting to portrait mode");
            }
            fVar.a = WMElement.CAMERASIZEVALUE1B1;
            fVar.b = 0.0f;
            fVar.d = 0.0f;
            fVar.e = WMElement.CAMERASIZEVALUE1B1;
        }
        fVar.a = 0.0f;
        fVar.b = 0.0f;
        if (this.aD) {
            DLog.d("motionVotes", "mImuDirectionX:" + this.s + ",mImuDirectionY:" + this.t);
        }
        if (Math.abs(this.s) > Math.abs(this.t)) {
            if (this.s > 0.0f) {
                fVar.a = WMElement.CAMERASIZEVALUE1B1;
            } else {
                fVar.a = GroundOverlayOptions.NO_DIMENSION;
            }
        } else if (this.t > 0.0f) {
            fVar.b = GroundOverlayOptions.NO_DIMENSION;
        } else {
            fVar.b = WMElement.CAMERASIZEVALUE1B1;
        }
        return fVar;
    }

    private static String c(int i) {
        return VERSION.SDK_INT < 19 ? d(i) : e(i);
    }

    private static String d(int i) {
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
    private static String e(int i) {
        return i != 20 ? d(i) : "TYPE_GEOMAGNETIC_ROTATION_VECTOR";
    }

    private void n() {
        this.d = null;
        this.aY = 0.0d;
        if (this.d != null) {
            this.d.reset();
        }
        this.e = null;
        this.F = 0.0f;
        this.G = 0.0f;
        this.H = 0.0f;
        this.A = 0.0f;
        this.B = 0.0f;
        this.C = 0.0f;
        this.D = 0.0f;
        this.E = 0.0f;
        this.I = 0.0f;
        this.J = 0.0f;
        this.s = 0.0f;
        this.t = 0.0f;
        this.j = 0;
        this.a = 0;
        this.o = 0;
        this.n = 0;
        this.w = GroundOverlayOptions.NO_DIMENSION;
        this.O = null;
        this.P = null;
        this.z = false;
        this.P = null;
        this.p = false;
        this.q = 0;
        this.Q = null;
        this.R = null;
        this.l = 0.0d;
        this.K = 0;
        this.T = null;
        this.W = false;
        this.X = -1;
        this.b = 0;
        this.as = false;
        this.k = false;
        this.r = false;
        this.aC = null;
        o();
    }

    private void o() {
        this.ay = false;
        this.aE = 0;
        this.aF = 0;
        this.aG = 0;
        this.aH = 0;
        DLog.d("CameraProcessor", "Stopping recording.");
    }

    private void p() {
        Log.d("ProcessingStats", "Capture Time (s) : " + ((this.aU.e - this.aU.d) / 1000));
        Log.d("ProcessingStats", "preview FPS :" + this.aU.a());
        Log.d("ProcessingStats", "saveAllFrames:" + this.aS + " --> AllFPS:" + this.aU.c() + ",  EffectiveFPS:" + this.aU.b());
        Log.d("ProcessingStats", "saveAllFrames:" + this.aS + " --> SavedFrameCount:" + this.aU.b + ", DroppedFrameCount:" + this.aU.c);
        Log.d("ProcessingStats", "Camera FrameCount  : startTS, endTS --> " + this.aU.a + " : " + this.aU.d + ", " + this.aU.e);
        Log.d("ProcessingStats", "IMU SampleCount : startTS, endTS --> " + this.aU.h + " : " + this.aU.f + ", " + this.aU.g);
    }

    private boolean q() {
        boolean z;
        this.ad = (SensorManager) FyuseSDK.getContext().getSystemService("sensor");
        List<Sensor> sensorList = this.ad.getSensorList(-1);
        int[] x = x();
        for (Sensor type : sensorList) {
            int type2 = type.getType();
            if (a(type2, x)) {
                am = a(type2, am, x);
                this.ae = true;
            }
            if (a(type2, f)) {
                ao = a(type2, ao, f);
                this.af = true;
            }
            if (a(type2, g)) {
                an = a(type2, an, g);
                this.ag = true;
            }
            if (a(type2, h)) {
                ap = a(type2, ap, h);
                this.ah = true;
            }
            if (a(type2, i)) {
                aq = a(type2, aq, i);
                this.ai = true;
            }
        }
        if (this.ae) {
            ar = false;
            z = true;
        } else {
            z = false;
        }
        if (this.aD) {
            DLog.d("CameraRequirements", "mHasRotation:" + this.ae + ",type:" + c(am) + ",hasAccel:" + this.af + ",type:" + c(ao) + ",mHasGravity:" + this.ag + ",type:" + c(an) + ",mHasGyroscope:" + this.ai + ",IGNORE_GYROSCOPE:" + ar + ",mHasMagnetometer:" + this.ah + ",type:" + c(ap) + " (using:" + this.aj + ")");
        }
        return z && this.af && this.ag;
    }

    private Bitmap r() {
        if (this.aC == null) {
            return null;
        }
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.aC.compressToJpeg(new Rect(0, 0, this.aO, this.aP), 100, byteArrayOutputStream);
        return BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size());
    }

    private void s() {
        if (this.aD) {
            DLog.d("updateRecordingKeypointAnimation", "Called.");
        }
        int i = this.n + this.o;
        if (i > 10) {
            if (i >= 11 && !this.c) {
                if (this.aD) {
                    DLog.d("updateRecordingKeypointAnimation", "total_votes > vote_thresh");
                }
                i = !this.u ? -1 : 1;
                if (this.n > this.o) {
                    i *= -1;
                }
                if (this.aD) {
                    DLog.d("updateRecordingKeypointAnimation", "imu_directions: " + this.s + "," + this.t);
                    DLog.d("updateRecordingKeypointAnimation", "sgn: " + i);
                }
                boolean isPortraitMode = this.aM.isPortraitMode();
                int cameraRotation = this.aM.getCameraRotation();
                Hint hint = Math.abs(this.s) > Math.abs(this.t) ? ((float) i) * this.s > 0.0f ? !isPortraitMode ? cameraRotation != 90 ? Hint.MOTION_UP : Hint.MOTION_DOWN : Hint.MOTION_LEFT : !isPortraitMode ? cameraRotation != 90 ? Hint.MOTION_DOWN : Hint.MOTION_UP : Hint.MOTION_RIGHT : ((float) i) * this.t > 0.0f ? !isPortraitMode ? cameraRotation != 90 ? Hint.MOTION_LEFT : Hint.MOTION_RIGHT : Hint.MOTION_DOWN : !isPortraitMode ? cameraRotation != 90 ? Hint.MOTION_RIGHT : Hint.MOTION_LEFT : Hint.MOTION_UP;
                if (this.aD) {
                    DLog.d("updateRecordingKeypointAnimation", "PortraitMode? :" + isPortraitMode + ": Guide Arrows Going : " + hint);
                }
                a(hint);
                this.c = true;
            }
        } else if (this.aD) {
            DLog.d("updateRecordingKeypointAnimation", "total_votes <= vote_thresh :: total_votes = " + i + " <= " + 10);
        }
    }

    private HandlerThread t() {
        if (this.aZ == null) {
            this.aZ = new HandlerThread("CameraSensorThread");
            this.aZ.start();
        }
        return this.aZ;
    }

    private void u() {
        Handler handler = new Handler(t().getLooper());
        if (this.aD) {
            DLog.d("Recording", "REGISTERING SENSORS");
        }
        for (Sensor sensor : this.ad.getSensorList(-1)) {
            if (a(sensor.getType())) {
                this.ad.registerListener(this.aW, sensor, 0, handler);
            }
        }
    }

    private void v() {
        if (this.aD) {
            DLog.d("Recording", "UNREGISTERING SENSORS");
        }
        if (this.ad != null && this.aW != null) {
            this.ad.unregisterListener(this.aW);
            if (this.aZ != null) {
                this.aZ.quitSafely();
                this.aZ = null;
            }
        }
    }

    private void w() {
        this.aC = null;
    }

    private static int[] x() {
        return y();
    }

    private static int[] y() {
        return new int[]{15, 11};
    }

    public List<CaptureEventListener> a() {
        return this.aK;
    }

    public void a(MotionHintsListener motionHintsListener) {
        this.aI = motionHintsListener;
        this.aJ = null;
    }

    public void a(SnapShotCallback snapShotCallback) {
        this.aN.a(snapShotCallback);
        this.aL.set(true);
    }

    public void a(com.fyusion.sdk.camera.a.a aVar, int i, float[] fArr, RotationDirection rotationDirection) {
        this.aU = new k();
        u();
        this.aT = false;
        this.at = i;
        this.au = fArr;
        this.av = rotationDirection;
        a(i, fArr, rotationDirection);
        this.ax = aVar;
        l lVar = new l();
        lVar.b = 0.0f;
        lVar.a = 0.0f;
        lVar.c = 0.0d;
        this.L.add(lVar);
        this.aa = 0.0f;
        this.ab = 9.80665f;
        this.ac = 9.80665f;
        this.x = new OnlineImageStabilizerWrapper();
        this.m = new FyuseFrameInformationVec();
        b((this.aw * 1000) + 3);
        this.N.setCameraOrientation(this.aM.getCameraOrientation());
        if (this.u) {
            this.N.setWhetherRecordedWithFrontCamera(false);
        } else {
            this.N.setWhetherRecordedWithFrontCamera(true);
        }
        this.N.setCameraSize(new FyuseSize((double) this.aO, (double) this.aP));
        if (this.N.getProcessedSize().height == 0.0d && this.N.getProcessedSize().width == 0.0d) {
            this.N.setProcessedSize(new FyuseSize((double) this.aQ, (double) this.aR));
        }
        if (!this.ay) {
            this.ay = true;
            DLog.d("startRecording", "Starting a recording");
            this.az = false;
        }
    }

    public void a(List<CaptureEventListener> list) {
        this.aK = list;
    }

    public void a(boolean z) {
        this.aS = z;
    }

    public boolean a(byte[] bArr, byte[] bArr2, int i, boolean z, long j) {
        boolean z2 = false;
        k kVar = this.aU;
        kVar.a++;
        if (this.aL.getAndSet(false)) {
            this.aN.a(bArr);
        }
        if (this.ay) {
            this.aF++;
            this.aG++;
            if (this.aD) {
                DLog.d("onPreviewFrame", "About to call dropFrameFromRecording");
            }
            this.v = z;
            if (a(j, bArr, bArr2)) {
                if (this.aD) {
                    DLog.d("onPreviewFrame", "framenumber: " + this.aE + " -- dropped");
                }
                kVar = this.aU;
                kVar.c++;
            } else {
                if (this.aD) {
                    DLog.d("onPreviewFrame", "framenumber: " + this.aE + " kept");
                }
                if (!this.az) {
                    this.aA = z;
                    this.aB = i;
                    this.aC = null;
                    this.aC = new YuvImage(this.aM.getImageFormat() == 17 ? bArr : a(bArr, this.aO, this.aP), 17, this.aO, this.aP, null);
                    if (this.aC != null) {
                        this.az = true;
                    } else if (this.aD) {
                        DLog.e("onPreviewFrame", "Thumb is NULL!");
                    }
                }
                k kVar2 = this.aU;
                kVar2.b++;
                if (this.aD) {
                    DLog.d("onPreviewFrame", "Frame saved!");
                }
                z2 = true;
            }
            if (this.aU.d == 0) {
                this.aU.d = j;
            }
            this.aU.e = j;
            this.aE++;
            return z2;
        }
        if (this.aD) {
            DLog.d("onPreviewFrame", "Not recording!");
        }
        return false;
    }

    public void b() {
        this.e = null;
        this.d = null;
        w();
    }

    public void b(List<RecordingProgressListener> list) {
        this.aV = list;
    }

    public void b(boolean z) {
        this.u = z;
    }

    public boolean c() {
        return this.aT;
    }

    void d() {
        if (this.ay) {
            v();
            this.ay = false;
            this.aJ = null;
            DLog.d("AppEvent", com.fyusion.sdk.camera.util.e.FY_FYUSE_RECORDED.a());
            if (this.aD) {
                DLog.d("CameraProcessor", this.M.size() + "");
            }
            this.N.setAppVersionUsedToRecord(FyuseSDK.getVersion());
            this.N.setUniqueDeviceID(com.fyusion.sdk.common.util.a.a());
            this.N.setDeviceID(com.fyusion.sdk.common.util.a.b());
            this.N.setNumberOfMotionFrames(this.M.size());
            this.N.setMotionFile(j.ai);
            f c = c(false);
            this.N.setDirectionX(c.a);
            this.N.setDirectionY(c.b);
            this.N.setCurvature(c.c);
            this.N.setGravityX(c.d);
            this.N.setGravityY(c.e);
            this.N.setMax_number_frames_(this.aw);
            this.N.setMax_ready_for_more_media_data_check_fails_(j.q);
            this.N.setCamera_fps_indicator_frame_window_size_(j.s);
            this.N.setPost_processing_width_(j.C);
            this.N.setStabilization_smoothing_iterations_(j.I);
            this.N.setStabilization_smoothing_range_(j.J);
            this.N.setMax_stabilization_motion_(j.K);
            this.N.setMax_stabilization_angle_(j.L);
            this.N.setMax_low_fps_iso_value_(j.R);
            this.N.setMin_high_fps_iso_value_(j.S);
            this.N.setNum_frames_to_crop_(3);
            this.N.setCurrent_version_number_(j.V);
            this.N.setMjpeg_video_fps_(j.i);
            this.N.setH264_recording_video_fps_(j.j);
            this.N.setH264_upload_video_fps_(j.k);
            this.N.setReady_for_more_media_data_sleep_time_(j.r);
            this.N.setFyuse_quality_(j.Q);
            this.N.setApply_face_detection_for_stabilization_(j.B);
            this.N.setWrite_mjpeg_fyuse_using_gpu_(j.E);
            this.N.setEnable_standard_unsharpen_filter_(j.X);
            this.N.setWasRecordedInSelfiePanoramaMode(false);
            Date date = new Date();
            date.setSeconds_since_1970((double) (System.currentTimeMillis() / 1000));
            this.N.setCreationData(date);
            if (this.aD) {
                DLog.d("resetRecordingInfo", "Number of highResolutionSlices: " + this.N.getNumberOfSlices());
            }
            a(this.N);
            this.ax.a(this.M).a(this.x).a(this.N).e();
            this.c = false;
            if (this.x != null) {
                this.x.releaseData();
                this.x = null;
            }
            if (this.aD) {
                p();
            }
        }
    }

    public void e() {
        this.N.setNumberOfCameraFrames((int) this.m.size());
        this.N.setFrameTimestamps(this.m);
    }

    public Bitmap f() throws FileNotFoundException {
        return com.fyusion.sdk.common.ext.util.a.a(r(), this.N);
    }

    public int g() {
        return this.a;
    }
}
