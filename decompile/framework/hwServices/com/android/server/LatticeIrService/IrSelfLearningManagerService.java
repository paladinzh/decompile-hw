package com.android.server.LatticeIrService;

import android.content.Context;
import android.irself.IIrSelfLearningManager.Stub;
import android.media.AudioSystem;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;

public class IrSelfLearningManagerService extends Stub {
    private static final int MAX_XMIT_BUFFER = 4096;
    private static final int MSG_STOP_LEARNING = 1;
    private static final String SECURITY_EXCEPTION = "Requires SELFBUILD_IR_SERVICE permission";
    private static final String TAG = "IrSelfLearningManagerService";
    private static final long TIME_DELAY = 30000;
    private int err;
    private Handler handler;
    private HandlerThread handlerThread;
    private final Context mContext;
    private boolean mDeviceInit_done = false;
    private final long mHal;
    private final Object mHalLock = new Object();
    private String mParameter = AppHibernateCst.INVALID_PKG;

    private static native long Openirslf_hal();

    private static native int halDeviceExit(long j);

    private static native int halDeviceInit(long j);

    private static native int halGetLearningStatus(long j);

    private static native int[] halReadIRCode(long j);

    private static native int halReadIRFrequency(long j);

    private static native int halStartLearning(long j);

    private static native int halStopLearning(long j);

    private static native int halself_learning_support(long j);

    private static native int halsendIR(long j, int i, int[] iArr);

    static {
        try {
            System.loadLibrary("IrLearning_jni");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "IrLearning_jni library not found!");
        }
    }

    public IrSelfLearningManagerService(Context context) {
        this.mContext = context;
        this.mDeviceInit_done = false;
        this.mHal = Openirslf_hal();
        if (this.mHal == 0) {
            Slog.e(TAG, "Lattice IR Service, HAL not loaded");
        }
        this.mParameter = AudioSystem.getParameters("audio_capability=irda_support");
        this.handlerThread = new HandlerThread("LearningStatusTimeOutThread");
        this.handlerThread.start();
        this.handler = new Handler(this.handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    IrSelfLearningManagerService.this.stopLearning();
                }
            }
        };
    }

    private void throwifNoLatticehal() {
        if (this.mHal == 0) {
            throw new UnsupportedOperationException("Lattice IR Service, HAL not loaded");
        } else if (!this.mDeviceInit_done) {
            throw new UnsupportedOperationException("DeviceInit function is not called first or DeviceInit function returned with some failure");
        }
    }

    public boolean hasIrSelfLearning() {
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        boolean DeviceInit_done_temp = this.mDeviceInit_done;
        this.mDeviceInit_done = true;
        throwifNoLatticehal();
        this.mDeviceInit_done = DeviceInit_done_temp;
        if (halself_learning_support(this.mHal) == 1) {
            return true;
        }
        return false;
    }

    public boolean deviceInit() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        this.mDeviceInit_done = true;
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halDeviceInit(this.mHal);
            if (this.err < 0) {
                this.mDeviceInit_done = false;
                Slog.e(TAG, "Error DeviceInit() : " + this.err);
            }
            if (this.err != 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean deviceExit() {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        this.mDeviceInit_done = false;
        synchronized (this.mHalLock) {
            this.err = halDeviceExit(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halDeviceExit() : " + this.err);
            }
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean transmit(int carrierFrequency, int[] pattern) {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        if (pattern == null) {
            return false;
        }
        long totalXmitbyte = 0;
        for (int slice : pattern) {
            if (slice <= 0) {
                throw new IllegalArgumentException("Non-positive IR slice");
            }
            totalXmitbyte += 2;
        }
        if (totalXmitbyte > 4096) {
            throw new IllegalArgumentException("IR pattern too long");
        }
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            turnOnTransmitRoad();
            this.err = halsendIR(this.mHal, carrierFrequency, pattern);
            if (this.err < 0) {
                Slog.e(TAG, "Error transmitting: " + this.err);
            }
            turnOffTransmitRoad();
            if (this.err == 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean startLearning() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            if (isSupportCodecIr()) {
                AudioSystem.setParameters("ir_learn=on");
                if (!isMMIScene()) {
                    this.handler.removeMessages(1);
                    this.handler.sendMessageDelayed(this.handler.obtainMessage(1), TIME_DELAY);
                }
            }
            this.err = halStartLearning(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halStartLearning() : " + this.err);
            }
            if (this.err != 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean stopLearning() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halStopLearning(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halStopLearning() : " + this.err);
            }
            if (isSupportCodecIr()) {
                if (!isMMIScene()) {
                    this.handler.removeMessages(1);
                }
                AudioSystem.setParameters("ir_learn=off");
            }
            if (this.err != 0) {
                z = false;
            }
        }
        return z;
    }

    public boolean getLearningStatus() {
        boolean z = true;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halGetLearningStatus(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error GetLearningStatus() : " + this.err);
            }
            if (this.err != 1) {
                z = false;
            }
        }
        return z;
    }

    public int readIRFrequency() {
        int i;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            this.err = halReadIRFrequency(this.mHal);
            if (this.err < 0) {
                Slog.e(TAG, "Error halReadIRFrequency : " + this.err);
            }
            i = this.err;
        }
        return i;
    }

    public int[] readIRCode() {
        int[] halReadIRCode;
        this.mContext.enforceCallingOrSelfPermission("huawei.android.permission.SELFBUILD_IR_SERVICE", SECURITY_EXCEPTION);
        throwifNoLatticehal();
        synchronized (this.mHalLock) {
            halReadIRCode = halReadIRCode(this.mHal);
        }
        return halReadIRCode;
    }

    private boolean isSupportCodecIr() {
        if (this.mParameter == null || !this.mParameter.equals("true")) {
            return false;
        }
        Slog.i(TAG, "IrSelfLearningManagerService isSupportCodecIr");
        return true;
    }

    private void turnOnTransmitRoad() {
        if (this.mParameter != null && this.mParameter.equals("true")) {
            AudioSystem.setParameters("ir_trans=on");
        }
    }

    private void turnOffTransmitRoad() {
        if (this.mParameter != null && this.mParameter.equals("true")) {
            AudioSystem.setParameters("ir_trans=off");
        }
    }

    private boolean isMMIScene() {
        boolean isMMI = SystemProperties.getBoolean("runtime.mmitest.isrunning", false);
        Slog.i(TAG, "IrSelfLearningManagerService isMMIScene = " + isMMI);
        return isMMI;
    }
}
