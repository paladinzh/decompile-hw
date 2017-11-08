package com.android.server.display;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.DisplayEffectMonitor.ParamLogPrinter;

public class BackLightBrightnessMonitor {
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW;
    private static final boolean HWLOGWE = true;
    private static final String PARAM_BRIGHTNESS = "brightness";
    private static final String PARAM_BRIGHTNESS_MODE = "brightnessMode";
    private static final int PARAM_MSG = 1;
    private static final String PARAM_PACKAGE_NAME = "packageName";
    private static final String PARAM_TYPE = "paramType";
    private static final String TAG = "BackLightBrightnessMonitor";
    private static final String TYPE_BRIGHTNESS_MODE = "brightnessMode";
    private static final String TYPE_MANUAL_BRIGHTNESS = "manualBrightness";
    private static final String TYPE_TEMP_AUTO_BRIGHTNESS = "tempAutoBrightness";
    private static final String TYPE_TEMP_MANUAL_BRIGHTNESS = "tempManualBrightness";
    private static final String TYPE_WINDOW_MANAGER_BRIGHTNESS = "windowManagerBrightness";
    private HandlerThread mHandlerThread;
    private boolean mInManualBrightnessMode = true;
    private ParamLogPrinter mManualBrightnessPrinter;
    private DisplayEffectMonitor mMonitor;
    private ParamReceiveHandler mParamReceiveHandler;
    private String mTempAutoBrightnessPackageNameLast;
    private ParamLogPrinter mTempAutoBrightnessPrinter;
    private String mTempManualBrightnessPackageNameLast;
    private ParamLogPrinter mTempManualBrightnessPrinter;
    private String mWindowManagerBrightnessMonitorPackageName;
    private String mWindowManagerBrightnessPackageNameLast;
    private ParamLogPrinter mWindowManagerBrightnessPrinter;

    private class ParamReceiveHandler extends Handler {
        public ParamReceiveHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                BackLightBrightnessMonitor.this.processMonitorParam((ArrayMap) msg.obj);
            }
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z;
    }

    public BackLightBrightnessMonitor(DisplayEffectMonitor monitor) {
        if (monitor != null) {
            this.mMonitor = monitor;
            DisplayEffectMonitor displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mWindowManagerBrightnessPrinter = new ParamLogPrinter("WindowManagerBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mTempManualBrightnessPrinter = new ParamLogPrinter("TempManualBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mTempAutoBrightnessPrinter = new ParamLogPrinter("TempAutoBrightness", TAG);
            displayEffectMonitor = this.mMonitor;
            displayEffectMonitor.getClass();
            this.mManualBrightnessPrinter = new ParamLogPrinter("ManualBrightness", TAG);
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mParamReceiveHandler = new ParamReceiveHandler(this.mHandlerThread.getLooper());
            if (HWFLOW) {
                Slog.i(TAG, "new instance success");
            }
        }
    }

    public boolean isParamOwner(String paramType) {
        if (paramType == null) {
            return false;
        }
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS) || paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS) || paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS) || paramType.equals("brightnessMode") || paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            return true;
        }
        return false;
    }

    public void sendMonitorParam(ArrayMap<String, Object> params) {
        if (this.mParamReceiveHandler != null) {
            this.mParamReceiveHandler.sendMessage(this.mParamReceiveHandler.obtainMessage(1, params));
        }
    }

    private void processMonitorParam(ArrayMap<String, Object> params) {
        if (params == null || !(params.get(PARAM_TYPE) instanceof String)) {
            Slog.e(TAG, "processMonitorParam() input params format error!");
            return;
        }
        String paramType = (String) params.get(PARAM_TYPE);
        if (HWDEBUG) {
            Slog.d(TAG, "processMonitorParam() paramType: " + paramType);
        }
        if (paramType.equals(TYPE_WINDOW_MANAGER_BRIGHTNESS)) {
            recordWindowManagerBrightness(params);
        } else if (paramType.equals(TYPE_TEMP_MANUAL_BRIGHTNESS)) {
            recordTempManualBrightness(params);
        } else if (paramType.equals(TYPE_TEMP_AUTO_BRIGHTNESS)) {
            recordTempAutoBrightness(params);
        } else if (paramType.equals("brightnessMode")) {
            recordBrightnessMode(params);
        } else if (paramType.equals(TYPE_MANUAL_BRIGHTNESS)) {
            recordManualBrightness(params);
        } else {
            Slog.e(TAG, "processMonitorParam() undefine paramType: " + paramType);
        }
    }

    private void recordWindowManagerBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordWindowManagerBrightness() can't get param: packageName");
            } else if (params.get("packageName") instanceof String) {
                processWindowManagerBrightness((String) params.get("packageName"), ((Integer) params.get(PARAM_BRIGHTNESS)).intValue());
            } else {
                Slog.e(TAG, "recordWindowManagerBrightness() can't get param: packageName");
            }
        }
    }

    private void processWindowManagerBrightness(String packageName, int brightness) {
        if (this.mWindowManagerBrightnessPackageNameLast == null) {
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        }
        if (this.mWindowManagerBrightnessPackageNameLast.equals(packageName)) {
            if (HWDEBUG) {
                Slog.d(TAG, "recordWindowManagerBrightness() update brightness=" + brightness + ", packageName=" + packageName);
            }
            this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
        } else if (brightness == -255 && packageName.equals("android")) {
            if (HWDEBUG) {
                Slog.d(TAG, "recordWindowManagerBrightness() brightness reset to normal");
            }
            stopMonitorWindowManagerBrightness();
            this.mWindowManagerBrightnessPrinter.resetParam(brightness, packageName);
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        } else {
            if (HWDEBUG) {
                Slog.d(TAG, "recordWindowManagerBrightness() start brightness=" + brightness + ", packageName=" + packageName);
            }
            startMonitorWindowManagerBrightness(packageName);
            if (this.mWindowManagerBrightnessPackageNameLast.equals("android")) {
                this.mWindowManagerBrightnessPrinter.updateParam(brightness, packageName);
            } else {
                this.mWindowManagerBrightnessPrinter.changeName(brightness, packageName);
            }
            this.mWindowManagerBrightnessPackageNameLast = packageName;
        }
    }

    private void startMonitorWindowManagerBrightness(String packageName) {
        this.mWindowManagerBrightnessMonitorPackageName = packageName;
    }

    private void stopMonitorWindowManagerBrightness() {
        this.mWindowManagerBrightnessMonitorPackageName = null;
    }

    private void checkMonitorWindowManagerBrightness() {
        if (this.mWindowManagerBrightnessMonitorPackageName != null) {
            if (!this.mMonitor.isAppAlive(this.mWindowManagerBrightnessMonitorPackageName)) {
                Slog.e(TAG, "checkMonitorWindowManagerBrightness() error! " + this.mWindowManagerBrightnessMonitorPackageName + " is not in foreground");
                this.mWindowManagerBrightnessMonitorPackageName = null;
            } else if (HWDEBUG) {
                Slog.d(TAG, "checkMonitorWindowManagerBrightness() " + this.mWindowManagerBrightnessMonitorPackageName + " is still in foreground");
            }
        }
    }

    private void recordTempManualBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordTempManualBrightness() can't get param: brightness");
            } else if (params.get("packageName") instanceof String) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                String packageName = (String) params.get("packageName");
                if (HWDEBUG) {
                    Slog.d(TAG, "recordTempManualBrightness() brightness=" + brightness + ", packageName=" + packageName);
                }
                if (this.mTempManualBrightnessPackageNameLast == null) {
                    this.mTempManualBrightnessPackageNameLast = packageName;
                }
                if (this.mTempManualBrightnessPackageNameLast.equals(packageName)) {
                    this.mTempManualBrightnessPrinter.updateParam(brightness, packageName);
                } else {
                    this.mTempManualBrightnessPrinter.changeName(brightness, packageName);
                    this.mTempManualBrightnessPackageNameLast = packageName;
                }
                checkMonitorWindowManagerBrightness();
            } else {
                Slog.e(TAG, "recordTempManualBrightness() can't get param: packageName");
            }
        }
    }

    private void recordTempAutoBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (!(params.get(PARAM_BRIGHTNESS) instanceof Integer)) {
                Slog.e(TAG, "recordTempAutoBrightness() can't get param: brightness");
            } else if (params.get("packageName") instanceof String) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                String packageName = (String) params.get("packageName");
                if (HWDEBUG) {
                    Slog.d(TAG, "recordTempAutoBrightness() brightness=" + brightness + ", packageName=" + packageName);
                }
                if (this.mTempAutoBrightnessPackageNameLast == null) {
                    this.mTempAutoBrightnessPackageNameLast = packageName;
                }
                if (!this.mTempAutoBrightnessPackageNameLast.equals(packageName)) {
                    this.mTempAutoBrightnessPrinter.changeName(brightness, packageName);
                    this.mTempAutoBrightnessPackageNameLast = packageName;
                } else if (brightness == -1) {
                    this.mTempAutoBrightnessPrinter.resetParam(brightness, packageName);
                } else {
                    this.mTempAutoBrightnessPrinter.updateParam(brightness, packageName);
                }
                checkMonitorWindowManagerBrightness();
            } else {
                Slog.e(TAG, "recordTempAutoBrightness() can't get param: packageName");
            }
        }
    }

    private void recordBrightnessMode(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (params.get("brightnessMode") instanceof Boolean) {
                boolean manualMode = ((Boolean) params.get("brightnessMode")).booleanValue();
                if (this.mInManualBrightnessMode != manualMode) {
                    if (HWFLOW) {
                        Slog.i(TAG, "BrightnessMode " + (manualMode ? "MANUAL" : "AUTO"));
                    }
                    this.mInManualBrightnessMode = manualMode;
                }
                return;
            }
            Slog.e(TAG, "recordBrightnessMode() can't get param: brightnessMode");
        }
    }

    private void recordManualBrightness(ArrayMap<String, Object> params) {
        if (params != null && this.mMonitor != null) {
            if (params.get(PARAM_BRIGHTNESS) instanceof Integer) {
                int brightness = ((Integer) params.get(PARAM_BRIGHTNESS)).intValue();
                if (HWDEBUG) {
                    Slog.d(TAG, "recordManualBrightness() brightness=" + brightness);
                }
                this.mManualBrightnessPrinter.updateParam(brightness, null);
                return;
            }
            Slog.e(TAG, "recordManualBrightness() can't get param: brightness");
        }
    }
}
