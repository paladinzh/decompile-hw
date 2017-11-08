package com.huawei.thermal.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.thermal.TContext;
import com.huawei.thermal.adapter.BroadcastAdapter;
import com.huawei.thermal.adapter.LogPowerAdapter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PolicyDispatcher {
    private static final boolean DEBUG = true;
    private static final long MIN_INTERVAL = SystemProperties.getLong("hwthermal.appctrl.period", 1800000);
    private static int mCurrRatio = 100;
    private static int mLcdBrightness = 0;
    private static PolicyDispatcher sInstance;
    private ContentResolver mContentResolver = null;
    private final Context mContext;
    private int mCurChargingLevel = 0;
    private int mCurWlanLevel = 0;
    private FileOutputStream mFosBatt = null;
    private FileOutputStream mFosBattAux = null;
    private FileOutputStream mFosDirectChr = null;
    private FileOutputStream mFosIc = null;
    private FileOutputStream mFosIcAux = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private boolean mIsSafetyRequire = false;
    private long mLastUpdateTime = 0;
    private boolean mLimitFlashLowBattery = false;
    private boolean mLimitFlashThermal = false;
    private final TContext mTContext;
    private int mTemperature = 0;
    private int mThermalRatio = 100;
    private WifiManager mWifiManager = null;
    private Runnable myRunnable = new Runnable() {
        public void run() {
            if (PolicyDispatcher.mCurrRatio > PolicyDispatcher.this.mThermalRatio) {
                PolicyDispatcher.mLcdBrightness = PolicyDispatcher.this.getLCDBrightnessAuto();
                if (!PolicyDispatcher.this.mIsSafetyRequire) {
                    if (PolicyDispatcher.mLcdBrightness >= 208) {
                        if (PolicyDispatcher.this.isManualBrightnessMode()) {
                        }
                    }
                    if (PolicyDispatcher.this.isManualBrightnessMode()) {
                        Log.i("HwPolicyDispatcher", "manual mode, not allow to adjust brightness");
                        return;
                    } else {
                        Log.i("HwPolicyDispatcher", "lcd db backlight: " + PolicyDispatcher.mLcdBrightness + " less than 208, not allow to adjust brightness");
                        return;
                    }
                }
                PolicyDispatcher.access$010();
                Log.d("HwPolicyDispatcher", "decrease brightness, mCurrRatio: " + PolicyDispatcher.mCurrRatio);
                PolicyDispatcher.this.sendPolicyToPG(PolicyDispatcher.this.mTemperature, "lcd", String.valueOf(PolicyDispatcher.mCurrRatio));
                PolicyDispatcher.this.mHandler.postDelayed(this, 1000);
            } else if (PolicyDispatcher.mCurrRatio >= PolicyDispatcher.this.mThermalRatio) {
                PolicyDispatcher.this.sendPolicyToPG(PolicyDispatcher.this.mTemperature, "lcd", String.valueOf(PolicyDispatcher.mCurrRatio));
                Log.d("HwPolicyDispatcher", "end lcd gradual adjust");
            } else {
                PolicyDispatcher.access$008();
                Log.d("HwPolicyDispatcher", "increase brightness, mCurrRatio: " + PolicyDispatcher.mCurrRatio);
                PolicyDispatcher.this.sendPolicyToPG(PolicyDispatcher.this.mTemperature, "lcd", String.valueOf(PolicyDispatcher.mCurrRatio));
                PolicyDispatcher.this.mHandler.postDelayed(this, 1000);
            }
        }
    };

    static /* synthetic */ int access$008() {
        int i = mCurrRatio;
        mCurrRatio = i + 1;
        return i;
    }

    static /* synthetic */ int access$010() {
        int i = mCurrRatio;
        mCurrRatio = i - 1;
        return i;
    }

    static {
        if (Log.isLoggable("HwPolicyDispatcher", 2)) {
        }
    }

    public static PolicyDispatcher getInstance(TContext tcontext) {
        PolicyDispatcher policyDispatcher;
        synchronized (PolicyDispatcher.class) {
            if (sInstance == null) {
                sInstance = new PolicyDispatcher(tcontext);
            }
            policyDispatcher = sInstance;
        }
        return policyDispatcher;
    }

    private PolicyDispatcher(TContext tcontext) {
        this.mContext = tcontext.getContext();
        this.mTContext = tcontext;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mContentResolver = this.mContext.getContentResolver();
        if (DEBUG) {
            Log.i("HwPolicyDispatcher", "MIN_INTERVAL:" + MIN_INTERVAL);
        }
    }

    public void thermalCrashInitialize() {
        Log.i("HwPolicyDispatcher", "crash, thermal action initialize");
        String path = this.mTContext.getThermalInterface("battery");
        if (path == null) {
            Log.w("HwPolicyDispatcher", "not config battery path");
        } else {
            setChargingLimit(0, path);
            this.mCurChargingLevel = 0;
        }
        this.mLimitFlashThermal = false;
        this.mLimitFlashLowBattery = false;
        setFlashSwitcher(false, true);
        setFlashSwitcher(true, true);
        setCameraFps(0);
        setChargeHotLimit(1, 0);
        setChargeHotLimit(2, 0);
        setChargeHotLimit(3, 0);
        setChargeHotLimit(4, 0);
        setChargeHotLimit(5, 0);
    }

    public void dispatchPolicy(Action action) {
        if (action instanceof ThermalAction) {
            dispatchThermalPolicy((ThermalAction) action);
        } else if (action instanceof BatteryAction) {
            dispatchBatteryPolicy((BatteryAction) action);
        }
    }

    public void dispatchBatteryPolicy(BatteryAction batteryAction) {
        Log.i("HwPolicyDispatcher", "dispatch battery " + batteryAction.toString());
        String action = batteryAction.getAction();
        String svalue = batteryAction.getValue();
        int batteryLevel = batteryAction.getBatteryLevel();
        if (action != null && svalue != null) {
            int value = Integer.parseInt(batteryAction.getValue());
            if (!"battery_critical".equals(action)) {
                Log.w("HwPolicyDispatcher", "do nothing for battery action:" + action + " value:" + svalue);
            } else if (value != 1) {
                Log.i("HwPolicyDispatcher", "not limit backgroud flash");
                setFlashSwitcher(false, true);
                this.mLimitFlashLowBattery = false;
            } else {
                Log.i("HwPolicyDispatcher", "limit backgroud flash");
                setFlashSwitcher(false, false);
                this.mLimitFlashLowBattery = true;
            }
        }
    }

    public void dispatchThermalPolicy(ThermalAction thermalAction) {
        Log.i("HwPolicyDispatcher", "dispatch thermal " + thermalAction.toString());
        String action = thermalAction.getAction();
        String svalue = thermalAction.getValue();
        int temperature = thermalAction.getTemperature();
        int sensorType = thermalAction.getSensorType();
        if (action != null && svalue != null) {
            boolean enable;
            int value = Integer.parseInt(thermalAction.getValue());
            if (value == 0) {
                enable = false;
            } else {
                enable = true;
            }
            if ("ucurrent".equals(action)) {
                setChargeHotLimit(1, value);
            } else if ("bcurrent".equals(action)) {
                setChargeHotLimit(2, value);
            } else if ("ucurrent_aux".equals(action)) {
                setChargeHotLimit(3, value);
            } else if ("bcurrent_aux".equals(action)) {
                setChargeHotLimit(4, value);
            } else if ("direct_charger".equals(action)) {
                setChargeHotLimit(5, value);
            } else if ("uvoltage".equals(action)) {
                setChargeVoltage(value);
            } else if ("battery".equals(action) || "call_battery".equals(action)) {
                path = this.mTContext.getThermalInterface("battery");
                if (path == null || value == this.mCurChargingLevel) {
                    Log.w("HwPolicyDispatcher", "not config " + action + " path");
                } else {
                    setChargingLimit(value, path);
                    this.mCurChargingLevel = value;
                }
            } else if ("wlan".equals(action)) {
                path = this.mTContext.getThermalInterface(action);
                if (path == null || value == this.mCurWlanLevel) {
                    Log.w("HwPolicyDispatcher", "not config " + action + " path");
                } else {
                    setWlanLimit(value, path);
                    this.mCurWlanLevel = value;
                }
            } else if ("camera_fps".equals(action)) {
                setCameraFps(value);
            } else if ("key_thread_sched".equals(action)) {
                sendKeyThreadSchedEvent(action, value);
            } else if ("vr_warning_level".equals(action)) {
                sendVRWarningLevel(action, value);
            } else if ("flash".equals(action)) {
                boolean z;
                if (enable) {
                    z = false;
                } else {
                    z = true;
                }
                setFlashSwitcher(false, z);
                this.mLimitFlashThermal = enable;
            } else if ("flash_front".equals(action)) {
                setFlashLimit(true, !enable);
            } else if ("paback".equals(action)) {
                setPAFallback(enable);
            } else if ("isp".equals(action)) {
                setIspLimit(value);
            } else if (enable && "camera_warning".equals(action)) {
                sendNotifyToCamera("camera_ui", "warning");
            } else if (enable && "camera_stop".equals(action)) {
                sendNotifyToCamera("camera_ui", "stop");
            } else if ("shutdown".equals(action)) {
                sendThermalUIEvent(this.mContext, action);
                shutdownPhone(value);
            } else if ("app_action".equals(action)) {
                sendPolicyToPG(temperature, action, svalue, sensorType);
            } else if ("app_ctrl".equals(action)) {
                if (value <= 0) {
                    sendPolicyToPG(temperature, action, svalue);
                } else {
                    long now = SystemClock.elapsedRealtime();
                    if ((now - this.mLastUpdateTime < MIN_INTERVAL ? 1 : null) == null) {
                        this.mLastUpdateTime = now;
                        sendPolicyToPG(temperature, action, svalue);
                    } else {
                        Log.d("HwPolicyDispatcher", "app_ctrl action period is " + ((now - this.mLastUpdateTime) / 1000) + "s from last action, don't send to PG");
                    }
                }
            } else if ("cpu".equals(action) || "cpu1".equals(action) || "cpu2".equals(action) || "cpu3".equals(action) || "cpu_a15".equals(action) || "gpu".equals(action) || "ipa_power".equals(action) || "ipa_temp".equals(action) || "ipa_switch".equals(action) || "fork_on_big".equals(action) || "boost".equals(action) || "threshold_up".equals(action) || "threshold_down".equals(action)) {
                sendPolicyToPG(temperature, action, svalue);
            } else if ("lcd".equals(action)) {
                switch (value) {
                    case NativeAdapter.PLATFORM_QCOM /*0*/:
                        this.mIsSafetyRequire = false;
                        this.mThermalRatio = 100;
                        break;
                    case NativeAdapter.PLATFORM_MTK /*1*/:
                        this.mIsSafetyRequire = false;
                        this.mThermalRatio = 80;
                        break;
                    case NativeAdapter.PLATFORM_HI /*2*/:
                        this.mIsSafetyRequire = false;
                        this.mThermalRatio = 50;
                        break;
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                        this.mIsSafetyRequire = false;
                        this.mThermalRatio = 15;
                        break;
                    case 4:
                        this.mIsSafetyRequire = true;
                        this.mThermalRatio = 50;
                        break;
                    case 5:
                        this.mIsSafetyRequire = true;
                        this.mThermalRatio = 15;
                        break;
                    case 6:
                        this.mIsSafetyRequire = true;
                        this.mThermalRatio = 80;
                        break;
                    default:
                        this.mIsSafetyRequire = false;
                        this.mThermalRatio = 100;
                        break;
                }
                Log.d("HwPolicyDispatcher", "start lcd gradual adjust, lcd: " + value);
                this.mTemperature = temperature;
                this.mHandler.removeCallbacks(this.myRunnable);
                this.mHandler.post(this.myRunnable);
            } else {
                Log.w("HwPolicyDispatcher", "do nothing for thermal action:" + action + " value:" + svalue);
            }
        }
    }

    public int getLCDBrightnessAuto() {
        return System.getInt(this.mContentResolver, "screen_auto_brightness", 207);
    }

    private boolean isManualBrightnessMode() {
        if (System.getInt(this.mContentResolver, "screen_brightness_mode", 0) != 0) {
            return false;
        }
        return true;
    }

    private void sendPolicyToPG(int temperature, String action, String policy) {
        LogPowerAdapter.push(temperature, action, policy);
    }

    private void sendPolicyToPG(int temperature, String action, String policy, int sensorType) {
        LogPowerAdapter.push(temperature, action, policy, sensorType);
    }

    private void sendNotifyToCamera(String event, String flag) {
        BroadcastAdapter.sendThermalComUIEvent(this.mContext, event, flag);
    }

    private void shutdownPhone(int delay) {
        if (delay <= 100) {
            delay = 0;
        }
        this.mTContext.shutdownPhone(delay);
    }

    private FileOutputStream getFreqStream(String path) {
        try {
            return new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private boolean writeFreq(int freq, FileOutputStream fos) {
        if (fos != null) {
            try {
                byte[] byFreq = Integer.toString(freq).getBytes();
                fos.write(byFreq, 0, byFreq.length);
                return true;
            } catch (IOException e) {
                Log.w("HwPolicyDispatcher", "IOException : write fail freq = " + freq);
                return false;
            }
        }
        Log.w("HwPolicyDispatcher", "write freq file stream is null !");
        return false;
    }

    public boolean setChargeHotLimit(int mode, int value) {
        if (mode == 1) {
            if (this.mFosIc == null) {
                this.mFosIc = getFreqStream("/sys/class/hw_power/charger/charge_data/iin_thermal");
            }
            return writeFreq(value, this.mFosIc);
        } else if (mode == 2) {
            if (this.mFosBatt == null) {
                this.mFosBatt = getFreqStream("/sys/class/hw_power/charger/charge_data/ichg_thermal");
            }
            return writeFreq(value, this.mFosBatt);
        } else if (mode == 3) {
            if (this.mFosIcAux == null) {
                this.mFosIcAux = getFreqStream("/sys/class/hw_power/charger/charge_data/iin_thermal_aux");
            }
            return writeFreq(value, this.mFosIcAux);
        } else if (mode == 4) {
            if (this.mFosBattAux == null) {
                this.mFosBattAux = getFreqStream("/sys/class/hw_power/charger/charge_data/ichg_thermal_aux");
            }
            return writeFreq(value, this.mFosBattAux);
        } else if (mode != 5) {
            return false;
        } else {
            if (this.mFosDirectChr == null) {
                this.mFosDirectChr = getFreqStream("/sys/class/hw_power/charger/direct_charger/iin_thermal");
            }
            return writeFreq(value, this.mFosDirectChr);
        }
    }

    public boolean setChargingLimit(int limitCurrent, String filePath) {
        Exception e;
        Throwable th;
        Log.i("HwPolicyDispatcher", "set charging limit: " + limitCurrent);
        FileOutputStream fileOutputStream = null;
        boolean result = false;
        byte[] byCurrent = Integer.toString(limitCurrent).getBytes();
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            try {
                fos.write(byCurrent, 0, byCurrent.length);
                result = true;
                if (fos == null) {
                    fileOutputStream = fos;
                } else {
                    try {
                        fos.close();
                    } catch (Exception e2) {
                        Log.d("HwPolicyDispatcher", "close failed " + e2);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fileOutputStream = fos;
                try {
                    Log.e("HwPolicyDispatcher", "set charging limit error", e2);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e22) {
                            Log.d("HwPolicyDispatcher", "close failed " + e22);
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e222) {
                            Log.d("HwPolicyDispatcher", "close failed " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fos;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e222 = e4;
            Log.e("HwPolicyDispatcher", "set charging limit error", e222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return result;
        }
        return result;
    }

    public boolean setFlashLimit(boolean isFront, boolean limit) {
        return com.huawei.thermal.adapter.NativeAdapter.setFlashLimit(isFront, limit);
    }

    public boolean setPAFallback(boolean fallback) {
        return false;
    }

    public boolean setIspLimit(int value) {
        return false;
    }

    public boolean setWlanLimit(int level, String filePath) {
        Log.i("HwPolicyDispatcher", "set wlan limit: " + level + ", path: " + filePath);
        return false;
    }

    public boolean setCameraFps(int value) {
        FileNotFoundException fe;
        SecurityException se;
        IOException e;
        try {
            FileOutputStream out = new FileOutputStream("/sys/bus/platform/drivers/huawei,camcfgdev/guard_thermal");
            if (value >= 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(value));
                sb.append("\n");
                byte[] byteFps = sb.toString().getBytes();
                out.write(byteFps, 0, byteFps.length);
                Log.i("HwPolicyDispatcher", "writeFpsNode success value = " + value);
            }
            try {
                out.close();
                return true;
            } catch (FileNotFoundException e2) {
                fe = e2;
                FileOutputStream fileOutputStream = out;
                Log.w("HwPolicyDispatcher", "FileNotFoundException : writeFpsNode fail :" + fe);
                return false;
            } catch (SecurityException e3) {
                se = e3;
                Log.w("HwPolicyDispatcher", "SecurityException : writeFpsNode fail :" + se);
                return false;
            } catch (IOException e4) {
                e = e4;
                Log.w("HwPolicyDispatcher", "IOException : writeFpsNode fail :" + e);
                return false;
            }
        } catch (FileNotFoundException e5) {
            fe = e5;
            Log.w("HwPolicyDispatcher", "FileNotFoundException : writeFpsNode fail :" + fe);
            return false;
        } catch (SecurityException e6) {
            se = e6;
            Log.w("HwPolicyDispatcher", "SecurityException : writeFpsNode fail :" + se);
            return false;
        } catch (IOException e7) {
            e = e7;
            Log.w("HwPolicyDispatcher", "IOException : writeFpsNode fail :" + e);
            return false;
        }
    }

    public boolean setChargeVoltage(int value) {
        if (value >= 0) {
            try {
                FileOutputStream out = new FileOutputStream("/sys/class/hw_power/charger/charge_data/adaptor_voltage");
                StringBuilder sb = new StringBuilder();
                sb.append(String.valueOf(value));
                sb.append("\n");
                byte[] byteUsbVoltage = sb.toString().getBytes();
                out.write(byteUsbVoltage, 0, byteUsbVoltage.length);
                Log.i("HwPolicyDispatcher", "writeUsbVoltageNode success value = " + value);
                out.close();
                return true;
            } catch (FileNotFoundException fe) {
                Log.w("HwPolicyDispatcher", "FileNotFoundException : writeUsbVoltageNode fail :" + fe);
            } catch (SecurityException se) {
                Log.w("HwPolicyDispatcher", "SecurityException : writeUsbVoltageNode fail :" + se);
            } catch (IOException e) {
                Log.w("HwPolicyDispatcher", "IOException : writeUsbVoltageNode fail :" + e);
            } catch (Exception e2) {
                Log.w("HwPolicyDispatcher", "Exception : writeUsbVoltageNode fail :" + e2);
            }
        }
        return false;
    }

    public void sendThermalUIEvent(Context context, String event) {
        BroadcastAdapter.sendThermalUIEvent(context, event);
    }

    public void sendVRWarningLevel(String key, int value) {
        BroadcastAdapter.sendVRWarningLevel(this.mContext, key, String.valueOf(value));
    }

    public void sendKeyThreadSchedEvent(String key, int value) {
        BroadcastAdapter.sendKeyThreadSchedEvent(this.mContext, key, String.valueOf(value));
    }

    private void setFlashSwitcher(boolean isFront, boolean enable) {
        boolean z = true;
        boolean z2 = false;
        if (isFront) {
            if (!enable) {
                z2 = true;
            }
            setFlashLimit(true, z2);
        } else {
            if (enable) {
                if (this.mLimitFlashLowBattery && this.mLimitFlashThermal) {
                    Log.i("HwPolicyDispatcher", "not limit flash, do nothing, flash limit by thermal and low battery");
                    return;
                }
            } else if (this.mLimitFlashLowBattery || this.mLimitFlashThermal) {
                Log.i("HwPolicyDispatcher", "limit flash, do nothing, flash limit by thermal or low battery");
                return;
            }
            if (this.mTContext.isHisiPlatform() || this.mTContext.isQcommPlatform()) {
                if (enable) {
                    z = false;
                }
                setFlashLimit(false, z);
            } else {
                Log.e("HwPolicyDispatcher", "IHardwareService->setFlashlightEnabled not implements for platform");
            }
        }
    }
}
