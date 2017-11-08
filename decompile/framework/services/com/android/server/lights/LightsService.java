package com.android.server.lights;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.Settings.Secure;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.Flog;
import android.util.Slog;
import android.view.ViewRootImpl;
import com.android.server.SystemService;
import com.android.server.usb.UsbAudioDevice;
import com.android.server.vr.VrManagerService;
import com.huawei.pgmng.common.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LightsService extends SystemService {
    static final boolean DEBUG = false;
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final String MAX_BRIGHTNESS_PATH = "/sys/class/leds/lcd_backlight0/max_brightness";
    static final String TAG = "LightsService";
    private static boolean inMirrorLinkBrightnessMode = false;
    private static long mAmountTime = 0;
    private static boolean mIsAutoAdjust = false;
    private static int mLcdBrightness = 100;
    public static int mMaxBrightnessFromKernel = 255;
    private static double mRatio = 1.0d;
    private int mCurBrightness = 100;
    private Handler mH = new Handler() {
        public void handleMessage(Message msg) {
            msg.obj.stopFlashing();
        }
    };
    protected boolean mIsHighPrecision = false;
    final LightImpl[] mLights = new LightImpl[14];
    protected long mNativePointer = init_native();
    private final LightsManager mService = new LightsManager() {
        public Light getLight(int id) {
            if (id < 14) {
                return LightsService.this.mLights[id];
            }
            return null;
        }
    };
    private boolean mVrModeEnabled;
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(boolean enabled) throws RemoteException {
            LightImpl l = LightsService.this.mLights[0];
            int vrDisplayMode = LightsService.this.getVrDisplayMode();
            if (enabled && vrDisplayMode == 0) {
                if (!LightsService.this.mVrModeEnabled) {
                    l.enableLowPersistence();
                    LightsService.this.mVrModeEnabled = true;
                }
            } else if (LightsService.this.mVrModeEnabled) {
                l.disableLowPersistence();
                LightsService.this.mVrModeEnabled = false;
            }
        }
    };
    protected boolean mWriteAutoBrightnessDbEnable = true;

    private final class LightImpl extends Light {
        private int mBrightnessMode;
        private int mColor;
        private int mCurrentBrightness;
        private boolean mFlashing;
        private int mId;
        private int mLastBrightnessMode;
        private int mLastColor;
        private boolean mLocked;
        private int mMode;
        private int mOffMS;
        private int mOnMS;

        private LightImpl(int id) {
            this.mId = id;
        }

        public void setLcdRatio(int ratio, boolean autoAdjust) {
            LightsService.mIsAutoAdjust = autoAdjust;
            if (ratio > 100 || ratio < 1) {
                LightsService.mRatio = 1.0d;
            } else {
                LightsService.mRatio = ((double) ratio) / 100.0d;
            }
            Slog.i(LightsService.TAG, "setLcdRatio ratio:" + ratio + " autoAdjust:" + autoAdjust);
            setLightGradualChange(LightsService.mLcdBrightness, 0, true);
        }

        public void configBrightnessRange(int ratioMin, int ratioMax, int autoLimit) {
            Utils.configBrightnessRange(ratioMin, ratioMax, autoLimit);
        }

        public void sendSmartBackLightWithRefreshFrames(int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
            LightsService.this.sendSmartBackLightWithRefreshFramesImpl(this.mId, enable, level, value, frames, setAfterRefresh, enable2, value2);
        }

        public void writeAutoBrightnessDbEnable(boolean enable) {
            LightsService.this.mWriteAutoBrightnessDbEnable = enable;
            if (enable) {
                LightsService.this.sendUpdateaAutoBrightnessDbMsg();
            }
        }

        public void updateUserId(int userId) {
            LightsService.this.updateCurrentUserId(userId);
        }

        public boolean isHighPrecision() {
            return true;
        }

        public int getMaxBrightnessFromKernel() {
            return LightsService.mMaxBrightnessFromKernel;
        }

        public void updateBrightnessAdjustMode(boolean mode) {
            LightsService.this.updateBrightnessMode(mode);
        }

        public void sendSmartBackLight(int enable, int level, int value) {
            synchronized (this) {
                if (value > 65535) {
                    value = 65535;
                }
                int lightValue = (((enable & 1) << 24) | ((level & 255) << 16)) | (value & 65535);
                Flog.i(NetdResponseCode.BandwidthControl, "set smart backlight. enable is " + enable + ",level is " + level + ",value is " + value + ",lightValue is " + lightValue);
                LightsService.setLight_native(LightsService.this.mNativePointer, this.mId, lightValue, 0, 0, 0, 0);
                ViewRootImpl.setEnablePartialUpdate(enable == 0);
            }
        }

        public void sendCustomBackLight(int backlight) {
            if (!LightsService.inMirrorLinkBrightnessMode) {
                synchronized (this) {
                    LightsService.setLight_native(LightsService.this.mNativePointer, this.mId, backlight, 0, 0, 0, 0);
                }
            }
        }

        public void sendAmbientLight(int ambientLight) {
            synchronized (this) {
                LightsService.setLight_native(LightsService.this.mNativePointer, this.mId, ambientLight, 0, 0, 0, 0);
            }
        }

        public void sendSREWithRefreshFrames(int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
            LightsService.this.sendSREWithRefreshFramesImpl(this.mId, enable, ambientLightThreshold, ambientLight, frames, setAfterRefresh, enable2, ambientLight2);
        }

        public void setBrightness(int brightness) {
            if (!LightsService.inMirrorLinkBrightnessMode) {
                setBrightness(brightness, 0);
            }
        }

        public void setMirrorLinkBrightness(int target) {
            synchronized (this) {
                Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  brightness is " + target);
                int brightness = LightsService.this.mapIntoRealBacklightLevel((target * 10000) / 255);
                if (LightsService.this.mIsHighPrecision) {
                    setLightLocked_10000stage(brightness & 65535, 0, 0, 0, 0);
                } else {
                    int color = brightness & 255;
                    setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, 0);
                }
            }
        }

        public void setMirrorLinkBrightnessStatus(boolean status) {
            Slog.i(LightsService.TAG, "setMirrorLinkBrightnessStatus  status is " + status);
            LightsService.inMirrorLinkBrightnessMode = status;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setBrightness(int brightness, int brightnessMode) {
            this.mCurrentBrightness = brightness;
            if (!LightsService.inMirrorLinkBrightnessMode) {
                if (this.mId == 0) {
                    LightsService.mLcdBrightness = brightness;
                    LightsService.this.sendUpdateaAutoBrightnessDbMsg();
                    if (brightness == 0 || (!LightsService.mIsAutoAdjust && LightsService.mRatio >= 1.0d)) {
                        LightsService.this.mCurBrightness = brightness;
                    } else {
                        setLightGradualChange(brightness, brightnessMode, false);
                        return;
                    }
                }
                synchronized (this) {
                    brightness = LightsService.this.mapIntoRealBacklightLevel(brightness);
                    int color;
                    if (LightsService.this.mIsHighPrecision) {
                        color = brightness & 65535;
                        if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                            color = (color * 255) / 10000;
                            setLightLocked(brightness & 255, 0, 0, 0, brightnessMode);
                            return;
                        }
                        setLightLocked_10000stage(color, 0, 0, 0, brightnessMode);
                    } else {
                        color = brightness & 255;
                        if (this.mId == 2 && LightsService.FRONT_FINGERPRINT_NAVIGATION && LightsService.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                            Slog.i(LightsService.TAG, "Set button brihtness:" + color);
                            setLightLocked(color, 0, 0, 0, brightnessMode);
                            return;
                        }
                        setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, brightnessMode);
                    }
                }
            }
        }

        public int getCurrentBrightness() {
            return this.mCurrentBrightness;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void setLightGradualChange(int brightness, int brightnessMode, boolean isPGset) {
            int tarBrightness = brightness;
            if (LightsService.mRatio < 1.0d) {
                tarBrightness = Utils.getRatioBright(brightness, LightsService.mRatio);
            }
            if (LightsService.mIsAutoAdjust) {
                tarBrightness = Utils.getAutoAdjustBright(tarBrightness);
            }
            if (!isPGset) {
                if (LightsService.this.mCurBrightness == 0 && tarBrightness > 0) {
                    LightsService.mAmountTime = SystemClock.elapsedRealtime();
                }
                if (SystemClock.elapsedRealtime() - LightsService.mAmountTime < 1000) {
                    LightsService.this.mCurBrightness = tarBrightness;
                }
            }
            int amount = Math.max(100, Math.abs(LightsService.this.mCurBrightness - tarBrightness) / 18);
            synchronized (this) {
                while (true) {
                    int setValue = Utils.getAnimatedValue(tarBrightness, LightsService.this.mCurBrightness, amount);
                    LightsService.this.mCurBrightness = setValue;
                    setValue = LightsService.this.mapIntoRealBacklightLevel(setValue);
                    if (LightsService.this.mIsHighPrecision) {
                        setLightLocked_10000stage(setValue & 65535, 0, 0, 0, brightnessMode);
                    } else {
                        int color = setValue & 255;
                        setLightLocked(color | (((color << 16) | UsbAudioDevice.kAudioDeviceMetaMask) | (color << 8)), 0, 0, 0, brightnessMode);
                    }
                    if (LightsService.mLcdBrightness != 0) {
                        if (LightsService.this.mCurBrightness != tarBrightness) {
                            SystemClock.sleep(16);
                        }
                        if (LightsService.this.mCurBrightness == tarBrightness) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        public void setColor(int color) {
            synchronized (this) {
                setLightLocked(color, 0, 0, 0, 0);
            }
        }

        public void setFlashing(int color, int mode, int onMS, int offMS) {
            synchronized (this) {
                setLightLocked(color, mode, onMS, offMS, 0);
            }
        }

        public void pulse() {
            pulse(UsbAudioDevice.kAudioDeviceClassMask, 7);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void pulse(int color, int onMS) {
            synchronized (this) {
                if (this.mBrightnessMode == 2) {
                } else if (this.mColor == 0 && !this.mFlashing) {
                    setLightLocked(color, 2, onMS, 1000, 0);
                    this.mColor = 0;
                    LightsService.this.mH.sendMessageDelayed(Message.obtain(LightsService.this.mH, 1, this), (long) onMS);
                }
            }
        }

        public void turnOff() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 0);
            }
        }

        void enableLowPersistence() {
            synchronized (this) {
                setLightLocked(0, 0, 0, 0, 2);
                this.mLocked = true;
            }
        }

        void disableLowPersistence() {
            synchronized (this) {
                this.mLocked = false;
                setLightLocked(this.mLastColor, 0, 0, 0, this.mLastBrightnessMode);
            }
        }

        private void stopFlashing() {
            synchronized (this) {
                setLightLocked(this.mColor, 0, 0, 0, 0);
            }
        }

        private void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (!this.mLocked) {
                if (color == this.mColor && mode == this.mMode && onMS == this.mOnMS && offMS == this.mOffMS) {
                    if (this.mBrightnessMode == brightnessMode) {
                        return;
                    }
                }
                this.mLastColor = this.mColor;
                this.mColor = color;
                this.mMode = mode;
                this.mOnMS = onMS;
                this.mOffMS = offMS;
                this.mLastBrightnessMode = this.mBrightnessMode;
                this.mBrightnessMode = brightnessMode;
                Trace.traceBegin(131072, "setLight(" + this.mId + ", 0x" + Integer.toHexString(color) + ")");
                try {
                    LightsService.setLight_native(LightsService.this.mNativePointer, this.mId, color, mode, onMS, offMS, brightnessMode);
                } finally {
                    Trace.traceEnd(131072);
                }
            }
        }

        private void setLightLocked_10000stage(int color, int mode, int onMS, int offMS, int brightnessMode) {
            if (color == this.mColor && mode == this.mMode && onMS == this.mOnMS) {
                if (offMS == this.mOffMS) {
                    return;
                }
            }
            this.mColor = color;
            this.mMode = mode;
            this.mOnMS = onMS;
            this.mOffMS = offMS;
            Trace.traceBegin(131072, "setLight(" + this.mId + ", " + color + ")");
            try {
                LightsService.setLight_native(LightsService.this.mNativePointer, 11, color, mode, onMS, offMS, brightnessMode);
            } finally {
                Trace.traceEnd(131072);
            }
        }

        public int getDeviceActualBrightnessLevel() {
            return LightsService.this.getDeviceActualBrightnessLevelImpl();
        }

        public int getDeviceActualBrightnessNit() {
            return LightsService.this.getDeviceActualBrightnessNitImpl();
        }

        public int getDeviceStandardBrightnessNit() {
            return LightsService.this.getDeviceStandardBrightnessNitImpl();
        }
    }

    private static native void finalize_native(long j);

    private static native long init_native();

    protected static native void refreshFrames_native();

    static native void setBackLightMaxLevel_native(int i);

    static native void setHighPrecisionFlag_native(long j, int i);

    static native void setLight_native(long j, int i, int i2, int i3, int i4, int i5, int i6);

    public LightsService(Context context) {
        super(context);
        for (int i = 0; i < 14; i++) {
            this.mLights[i] = new LightImpl(i);
        }
        getMaxBrightnessFromKerenl();
    }

    public void onStart() {
        publishLocalService(LightsManager.class, this.mService);
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            try {
                ((IVrManager) getBinderService(VrManagerService.VR_MANAGER_BINDER_SERVICE)).registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e);
            }
        }
    }

    private int getVrDisplayMode() {
        return Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser());
    }

    protected void finalize() throws Throwable {
        finalize_native(this.mNativePointer);
        super.finalize();
    }

    public void getMaxBrightnessFromKerenl() {
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(MAX_BRIGHTNESS_PATH)));
            try {
                String tempString = reader.readLine();
                if (tempString != null) {
                    mMaxBrightnessFromKernel = Integer.parseInt(tempString);
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e2) {
                bufferedReader = reader;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                bufferedReader = reader;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e8) {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (Throwable th3) {
            th = th3;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            throw th;
        }
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
    }

    public void sendSREWithRefreshFramesImpl(int id, int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
    }

    protected void sendUpdateaAutoBrightnessDbMsg() {
    }

    protected void updateBrightnessMode(boolean mode) {
    }

    protected int getLcdBrightnessMode() {
        return mLcdBrightness;
    }

    protected int mapIntoRealBacklightLevel(int level) {
        return level;
    }

    protected void updateCurrentUserId(int userId) {
    }

    public int getDeviceActualBrightnessLevelImpl() {
        return 0;
    }

    public int getDeviceActualBrightnessNitImpl() {
        return 0;
    }

    public int getDeviceStandardBrightnessNitImpl() {
        return 0;
    }
}
