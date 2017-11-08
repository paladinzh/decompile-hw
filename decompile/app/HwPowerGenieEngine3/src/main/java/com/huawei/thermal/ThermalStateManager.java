package com.huawei.thermal;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.Log;
import com.huawei.thermal.adapter.BroadcastAdapter;
import com.huawei.thermal.adapter.DeviceAdapter;
import com.huawei.thermal.config.SensorTempAction;
import com.huawei.thermal.config.ThermalConfig;
import com.huawei.thermal.config.TriggerAction;
import com.huawei.thermal.event.Event;
import com.huawei.thermal.event.MsgEvent;
import com.huawei.thermal.event.SceneEvent;
import com.huawei.thermal.event.ThermalEvent;
import com.huawei.thermal.policy.PolicyDispatcher;
import com.huawei.thermal.policy.ThermalAction;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThermalStateManager implements InputListener {
    private static final boolean DEBUG;
    private static int mBatteryLevel = 0;
    private final HashMap<Integer, Integer> mA15CpuMaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mAcChargeAuxLimit = new HashMap();
    private final HashMap<Integer, Integer> mAcChargeLimit = new HashMap();
    private boolean mAlarmCleared = false;
    private boolean mAlarmRaised = false;
    private final HashMap<Integer, Integer> mAppAction = new HashMap();
    private final HashMap<Integer, Integer> mAppCtrl = new HashMap();
    private boolean mBatteryCleared = false;
    private final HashMap<Integer, Integer> mBatteryLimit = new HashMap();
    private boolean mBatteryTrigged = false;
    private final HashMap<Integer, Integer> mBoost = new HashMap();
    private final HashMap<Integer, Integer> mCallBatteryLimit = new HashMap();
    private boolean mCameraClear = false;
    private final HashMap<Integer, Integer> mCameraFps = new HashMap();
    private int mCatchActionlvl = -1;
    private int mChargingPlugType = 0;
    private final ThermalConfig mConfig;
    private final Context mContext;
    private final HashMap<Integer, Integer> mCpu1MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpu2MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpu3MaxFreq = new HashMap();
    private final HashMap<Integer, Integer> mCpuMaxFreq = new HashMap();
    private int mCurA15Cpufreq = 0;
    private int mCurAcChargeAuxLimt = 0;
    private int mCurAcChargeLimt = 0;
    private int mCurAppAction = 0;
    private int mCurAppCtrl = 0;
    private int mCurBoost = -1;
    private String mCurCallBatteryPolicy = "0";
    private int mCurCallBatteryTemp = -100000;
    private int mCurCallBatteryType = -1;
    private int mCurCameraFps = 0;
    private int mCurChargingLvl = 0;
    private int mCurCpu1freq = 0;
    private int mCurCpu2freq = 0;
    private int mCurCpu3freq = 0;
    private int mCurCpufreq = 0;
    private int mCurDirectChargeLimt = 0;
    private int mCurEventID = 10000;
    private int mCurFlashLimt = 0;
    private int mCurForkOnBig = -1;
    private int mCurFrontFlashLimt = 0;
    private int mCurGpufreq = 0;
    private int mCurIpaPower = 0;
    private int mCurIpaSwitch = 0;
    private int mCurIpaTemp = 0;
    private int mCurKeyThreadSched = 0;
    private int mCurLcdLimt = 0;
    private int mCurPAFallbackLimt = 0;
    private final HashMap<Integer, Integer> mCurTemp = new HashMap();
    private int mCurThresholdDownPolicy = 0;
    private int mCurThresholdUpPolicy = 0;
    private int mCurUsbChargeAuxLimt = 0;
    private int mCurUsbChargeLimt = 0;
    private int mCurUsbVoltage = 0;
    private int mCurVRWarningLevel = 0;
    private int mCurWlanLvl = 0;
    private int mCurpopState = 0;
    private String mCurrentScreenCtrlBcurrentVal = null;
    private final DeviceAdapter mDeviceAdapter;
    private final HashMap<Integer, Integer> mDirectChargeLimit = new HashMap();
    private Multimap<Integer, String> mExecAction = new Multimap();
    private final HashMap<Integer, Integer> mFlashLimit = new HashMap();
    private final HashMap<Integer, Integer> mForkOnBig = new HashMap();
    private final HashMap<Integer, Integer> mFrontFlashLimit = new HashMap();
    private final HashMap<Integer, Integer> mGpuMaxFreq = new HashMap();
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 100:
                    if (!ThermalStateManager.this.mIsWarningBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "warning_temperature", "hot");
                        ThermalStateManager.this.mIsWarningBroadcast = true;
                        return;
                    }
                    return;
                case 101:
                    if (ThermalStateManager.this.mIsWarningBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "warning_temperature", "normal");
                        ThermalStateManager.this.mIsWarningBroadcast = false;
                        return;
                    }
                    return;
                case 102:
                    if (!ThermalStateManager.this.mIsColdChargingBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "charging_lowtemperature", "cold");
                        ThermalStateManager.this.mIsColdChargingBroadcast = true;
                        return;
                    }
                    return;
                case 103:
                    if (ThermalStateManager.this.mIsColdChargingBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(ThermalStateManager.this.mContext, "charging_lowtemperature", "normal");
                        ThermalStateManager.this.mIsColdChargingBroadcast = false;
                        return;
                    }
                    return;
                case 200:
                    ThermalStateManager thermalStateManager = ThermalStateManager.this;
                    if (msg.arg1 == 1) {
                        z = true;
                    }
                    thermalStateManager.handleThermalPolicyChange(z);
                    return;
                case 202:
                    ThermalStateManager.this.handleVRModeChange(((Boolean) msg.obj).booleanValue());
                    return;
                case 300:
                    synchronized (ThermalStateManager.this.mLock) {
                        ThermalStateManager.this.mScrnCtrlChargeCounter.incrementAndGet();
                    }
                    if (ThermalStateManager.this.mSimDevt != null) {
                        ThermalStateManager.this.handleThermalEvent(ThermalStateManager.this.mSimDevt, false);
                        return;
                    }
                    return;
                case 301:
                    ThermalStateManager.this.checkQuickCharger();
                    return;
                default:
                    return;
            }
        }
    };
    private final HashMap<Integer, Integer> mHmpThresholdDown = new HashMap();
    private final HashMap<Integer, Integer> mHmpThresholdUp = new HashMap();
    private final HashMap<Integer, Integer> mIpaPower = new HashMap();
    private final HashMap<Integer, Integer> mIpaSwitch = new HashMap();
    private final HashMap<Integer, Integer> mIpaTemp = new HashMap();
    private boolean mIsCalling = false;
    private boolean mIsCharging = false;
    private boolean mIsColdChargingBroadcast = false;
    private boolean mIsCrashReboot = false;
    private boolean mIsLowTempWaringBroadcast = false;
    private boolean mIsQuickChargeOff = false;
    private boolean mIsSceneChange = false;
    private boolean mIsScreenOff = false;
    private boolean mIsSupportQCOff = false;
    private boolean mIsSwitchBroadcast = false;
    private boolean mIsVRMode = false;
    private boolean mIsWarningBroadcast = false;
    private final HashMap<Integer, Integer> mLcdLimit = new HashMap();
    private Object mLock = new Object();
    private final HashMap<Integer, Boolean[]> mLvlAlarm = new HashMap();
    private final HashMap<Integer, Integer> mPAFallbackLimit = new HashMap();
    private int mParentEvtId = 10000;
    private String mParentPowerPkgName = null;
    private final PolicyDispatcher mPolicy;
    private final HashMap<Integer, Integer> mPopUpDialogState = new HashMap();
    private int mPowerMode = 2;
    private volatile AtomicInteger mScrnCtrlChargeCounter = new AtomicInteger(0);
    private boolean mScrnStateCtrlChargeActive = false;
    private final HashMap<Integer, SensorTempAction> mSensorActions = new HashMap();
    private ThermalEvent mSimDevt = null;
    private boolean mSupportPowerSaveThermalPolicy = false;
    private boolean mSupportVRMode = false;
    private final TContext mTContext;
    private final HashMap<Integer, Integer> mUsbChargeAuxLimit = new HashMap();
    private final HashMap<Integer, Integer> mUsbChargeLimit = new HashMap();
    private boolean mUsbConnected = false;
    private final HashMap<Integer, Integer> mUsbVoltage = new HashMap();
    private final HashMap<Integer, Integer> mVRWarningLevel = new HashMap();
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(boolean enabled) throws RemoteException {
            Log.i("HwThermalStateManager", "vr state change, enabled: " + enabled);
            ThermalStateManager.this.notifyVRMode(enabled);
        }
    };
    private final HashMap<Integer, Integer> mWlanLimit = new HashMap();
    private final HashMap<Integer, Integer> mkeyThreadSched = new HashMap();

    static {
        boolean z;
        if (Log.isLoggable("HwThermalStateManager", 2)) {
            z = true;
        } else {
            z = false;
        }
        DEBUG = z;
    }

    public ThermalStateManager(TContext tcontext, boolean crashReboot) {
        this.mContext = tcontext.getContext();
        this.mTContext = tcontext;
        this.mConfig = new ThermalConfig(tcontext);
        this.mPolicy = PolicyDispatcher.getInstance(tcontext);
        this.mDeviceAdapter = new DeviceAdapter(tcontext);
    }

    public void start() {
        initPowerMode();
        initialize();
        registerVR();
        if (this.mIsCrashReboot) {
            this.mPolicy.thermalCrashInitialize();
        }
        this.mHandler.removeMessages(301);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 10000);
    }

    private void initialize() {
        boolean z;
        boolean loadResult;
        this.mSupportPowerSaveThermalPolicy = this.mConfig.checkPowerSaveThermalConfigFileExist(this.mIsQuickChargeOff);
        this.mIsSupportQCOff = this.mConfig.checkQCThermalConfigFileExist();
        this.mSupportVRMode = this.mConfig.checkVRThermalConfigFileExist();
        if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
            z = false;
        } else {
            z = true;
        }
        this.mIsScreenOff = z;
        DeviceAdapter deviceAdapter = this.mDeviceAdapter;
        this.mIsCalling = DeviceAdapter.isPhoneInCall();
        Log.i("HwThermalStateManager", "in initialize, mSupportPowerSaveThermalPolicy = " + this.mSupportPowerSaveThermalPolicy + " ,mIsSupportQCOff = " + this.mIsSupportQCOff + " ,mSupportVRMode = " + this.mSupportVRMode);
        if (this.mIsSupportQCOff) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 10000);
        }
        if (this.mSupportPowerSaveThermalPolicy) {
            boolean initPowerSaveThermalConfig;
            if (this.mPowerMode != 1) {
                initPowerSaveThermalConfig = false;
            } else {
                initPowerSaveThermalConfig = true;
            }
            loadResult = loadThermalConf(initPowerSaveThermalConfig);
        } else {
            loadResult = loadThermalConf(false);
        }
        if (loadResult) {
            if (DEBUG) {
                for (Entry entry : this.mSensorActions.entrySet()) {
                    Log.d("HwThermalStateManager", "Thermald config:" + ((SensorTempAction) entry.getValue()).toString());
                }
            }
            Log.i("HwThermalStateManager", "mutil battery clr feature:" + this.mConfig.mMutilSensorBatteryClr);
        } else {
            Log.e("HwThermalStateManager", "error: because of thermald config");
        }
        if (this.mConfig.mScreenOnMaxBcurrentVal != null) {
            this.mScrnCtrlChargeCounter.incrementAndGet();
        }
    }

    public void onInputEvent(Event evt) {
        if (evt instanceof ThermalEvent) {
            onInputThermalEvent((ThermalEvent) evt);
        } else if (evt instanceof MsgEvent) {
            onInputMsgEvent((MsgEvent) evt);
        } else if (evt instanceof SceneEvent) {
            onInputSceneEvent((SceneEvent) evt);
        }
    }

    private boolean onInputSceneEvent(SceneEvent evt) {
        if (DEBUG) {
            Log.d("HwThermalStateManager", "onInputSceneEvent : " + evt);
        }
        int eventId = evt.getEventId();
        String pkg = evt.getPkg();
        int vaildEventID = eventId;
        int subFlag = evt.getSubFlag(eventId);
        if (subFlag == 2) {
            vaildEventID = eventId != 10021 ? this.mParentEvtId : 10007;
        } else if (subFlag != 1) {
            this.mParentEvtId = eventId;
            this.mParentPowerPkgName = evt.getPkg();
            vaildEventID = eventId;
            if (requireMatchCust(eventId, subFlag)) {
                int matchCustId = this.mConfig.matchCustPkgName(pkg);
                if (matchCustId != 0) {
                    vaildEventID = matchCustId;
                    this.mParentEvtId = matchCustId;
                }
            }
        } else {
            vaildEventID = this.mConfig.matchCustComb(eventId, this.mParentEvtId, this.mParentPowerPkgName);
        }
        if (this.mConfig.mSceneSensorActions.containsKey(Integer.valueOf(vaildEventID))) {
            if (DEBUG) {
                Log.d("HwThermalStateManager", "vaildEventID: " + vaildEventID + ", mCurEventID: " + this.mCurEventID);
            }
            if (vaildEventID == this.mCurEventID) {
                this.mIsSceneChange = false;
            } else {
                Log.d("HwThermalStateManager", "handle thermal policy:" + vaildEventID);
                if (this.mCurEventID == 10000 && vaildEventID == 10007) {
                    this.mCameraClear = true;
                }
                this.mIsSceneChange = true;
                handleThermalPolicyChange(vaildEventID);
                this.mCurEventID = vaildEventID;
            }
        }
        return true;
    }

    private void onInputThermalEvent(ThermalEvent evt) {
        int evtId = evt.getEventId();
        if (DEBUG) {
        }
        if (evtId == 100) {
            if (this.mConfig.mScreenOnMaxBcurrentVal != null) {
                ThermalEvent tempDefinedEvent = evt;
                this.mSimDevt = new ThermalEvent(evt.getEventId(), evt.getSensorType(), evt.getSensorTemp());
            }
            handleThermalEvent(evt, true);
        }
    }

    private void onInputMsgEvent(MsgEvent evt) {
        int evtId = evt.getEventId();
        if (DEBUG) {
            Log.d("HwThermalStateManager", "onInputMsgEvent : " + evt);
        }
        if (evtId == 206) {
            this.mIsCalling = true;
            handleCallEvent(evt);
        } else if (evtId == 207) {
            this.mIsCalling = false;
            handleCallEvent(evt);
        } else if (evtId == 203) {
            this.mChargingPlugType = evt.getIntent().getIntExtra("plugged", 0);
            if (!(this.mIsCharging || this.mChargingPlugType == 0)) {
                this.mIsCharging = true;
            }
            handleBatteryEvent(evt);
        } else if (evtId == 200) {
            this.mIsScreenOff = false;
            if (this.mConfig.mScreenOnMaxBcurrentVal != null) {
                if (this.mHandler.hasMessages(300)) {
                    this.mHandler.removeMessages(300);
                }
                synchronized (this.mLock) {
                    this.mScrnCtrlChargeCounter.incrementAndGet();
                }
                if (this.mSimDevt != null) {
                    Log.i("HwThermalStateManager", "screen on, handle screen state ctrl charge immediately");
                    handleThermalEvent(this.mSimDevt, false);
                }
            }
        } else if (evtId == 20004) {
            this.mIsScreenOff = true;
            if (this.mConfig.mScreenOnMaxBcurrentVal != null && this.mConfig.mPostPoneTimeInMSec >= 0) {
                Log.i("HwThermalStateManager", "will handle screen state ctrl charge after " + this.mConfig.mPostPoneTimeInMSec + " ms");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(300), (long) this.mConfig.mPostPoneTimeInMSec);
            }
        } else if (evtId == 215) {
            Bundle bundle = evt.getIntent().getExtras();
            if (bundle != null) {
                boolean isConnected = bundle.getBoolean("connected");
                if (isConnected != this.mUsbConnected) {
                    this.mUsbConnected = isConnected;
                }
                if (this.mIsCharging && !this.mUsbConnected) {
                    this.mIsCharging = false;
                    Log.d("HwThermalStateManager", "power disconnected after usb disconnect...");
                }
            }
        } else if (evtId == 204) {
            this.mIsCharging = true;
            sendChargeType(evt);
        } else if (evtId == 202) {
            sendChargeType(evt);
        } else if (evtId == 210) {
            sendChargeType(evt);
        } else if (evtId == 205) {
            if (!this.mUsbConnected) {
                this.mIsCharging = false;
                Log.i("HwThermalStateManager", "power disconnected.");
            }
            if (this.mIsSupportQCOff) {
                this.mHandler.removeMessages(301);
            }
        } else if (evtId == 208) {
            powerModeChanged(evt.getIntent().getIntExtra("state", 0));
        } else if (evtId == 209) {
            notifyVRMode(evt.getIntent().getBooleanExtra("connected", false));
        }
    }

    private void registerVR() {
        String str;
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        String str2 = "HwThermalStateManager";
        if (vrManager != null) {
            str = "I got it! VrManagerService! ";
        } else {
            str = "service null";
        }
        Log.d(str2, str);
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Log.i("HwThermalStateManager", "Failed to register VR mode state listener: " + e);
            }
        }
    }

    private void sendChargeType(MsgEvent evt) {
        if (!this.mIsSupportQCOff) {
            return;
        }
        if (evt.getEventId() != 210) {
            this.mHandler.removeMessages(301);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 10000);
            return;
        }
        if ("1".equals(evt.getIntent().getStringExtra("quick_charge_status"))) {
            this.mHandler.removeMessages(301);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(301), 3000);
        }
    }

    private boolean loadThermalConf(boolean usePowerSaveThermalConf) {
        boolean ret = this.mConfig.loadThermalConf(usePowerSaveThermalConf, this.mIsVRMode, this.mIsQuickChargeOff);
        if (!ret) {
            Log.w("HwThermalStateManager", "load thermald config error, check again,usePowerSaveThermalConf:" + usePowerSaveThermalConf + " qcoff:" + this.mIsQuickChargeOff + " vr:" + this.mIsVRMode);
            if (usePowerSaveThermalConf) {
                ret = this.mConfig.loadThermalConf(false, this.mIsVRMode, this.mIsQuickChargeOff);
                Log.w("HwThermalStateManager", "load normal thermald config:" + ret);
            } else if (this.mIsQuickChargeOff) {
                ret = this.mConfig.loadThermalConf(usePowerSaveThermalConf, this.mIsVRMode, false);
                Log.w("HwThermalStateManager", "load qc thermald config:" + ret);
            } else if (this.mIsVRMode) {
                ret = this.mConfig.loadThermalConf(usePowerSaveThermalConf, false, this.mIsQuickChargeOff);
                Log.w("HwThermalStateManager", "load un_vr thermald config:" + ret);
            }
            if (!ret) {
                ret = this.mConfig.loadThermalConf(false, false, false);
                Log.w("HwThermalStateManager", "load default thermald config:" + ret);
            }
        }
        if (ret) {
            this.mSensorActions.clear();
            HashMap<Integer, SensorTempAction> defaultSensorActions = (HashMap) this.mConfig.mSceneSensorActions.get(Integer.valueOf(this.mCurEventID));
            if (defaultSensorActions == null) {
                defaultSensorActions = (HashMap) this.mConfig.mSceneSensorActions.get(Integer.valueOf(10000));
            }
            this.mSensorActions.putAll(defaultSensorActions);
            for (Entry entry : this.mSensorActions.entrySet()) {
                Integer sensorType = (Integer) entry.getKey();
                Boolean[] lvlalarm = new Boolean[8];
                for (int i = 0; i < 8; i++) {
                    lvlalarm[i] = Boolean.valueOf(false);
                }
                this.mLvlAlarm.put(sensorType, lvlalarm);
            }
            refreshThermalActions();
        } else {
            Log.e("HwThermalStateManager", "thermald config error,usePowerSaveThermalConf:" + usePowerSaveThermalConf + " qcoff:" + this.mIsQuickChargeOff + " vr:" + this.mIsVRMode);
        }
        return ret;
    }

    private void refreshThermalActions() {
        ArrayList<Integer> configScene = this.mConfig.getConfigThermalScene();
        if (configScene != null && configScene.size() != 1) {
            this.mTContext.registerPGActions(configScene);
        }
    }

    private boolean requireMatchCust(int actionId, int subActionFlag) {
        if (subActionFlag == 2 || subActionFlag == 1) {
            return false;
        }
        return true;
    }

    private void handleThermalPolicyChange(int sceneID) {
        synchronized (this.mLock) {
            clearCurrentPolicy();
            clearCachedConf();
            this.mCameraClear = false;
            this.mSensorActions.clear();
            this.mSensorActions.putAll((HashMap) this.mConfig.mSceneSensorActions.get(Integer.valueOf(sceneID)));
            for (Entry entry : this.mSensorActions.entrySet()) {
                Integer sensorType = (Integer) entry.getKey();
                Boolean[] lvlalarm = new Boolean[8];
                for (int i = 0; i < 8; i++) {
                    lvlalarm[i] = Boolean.valueOf(false);
                }
                this.mLvlAlarm.put(sensorType, lvlalarm);
            }
            trigerThermal();
        }
    }

    private void notifyVRMode(boolean connect) {
        if (this.mSupportVRMode) {
            this.mHandler.removeMessages(202);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(202, Boolean.valueOf(connect)), 3000);
        }
    }

    private void notifyUsePowerSaveThermalPolicy(boolean usePowerSaveThermalConf) {
        if (this.mSupportPowerSaveThermalPolicy) {
            int i;
            if (this.mHandler.hasMessages(200)) {
                this.mHandler.removeMessages(200);
            }
            Handler handler = this.mHandler;
            if (usePowerSaveThermalConf) {
                i = 1;
            } else {
                i = 0;
            }
            this.mHandler.sendMessageDelayed(handler.obtainMessage(200, i, 0), 5000);
        }
    }

    private void handleThermalPolicyChange(boolean usePowerSaveThermalConf) {
        synchronized (this.mLock) {
            String str;
            String str2 = "HwThermalStateManager";
            if (usePowerSaveThermalConf) {
                str = "Switch to power save thermal configure";
            } else {
                str = "Switch to default thermal configure";
            }
            Log.w(str2, str);
            clearCurrentPolicy();
            clearCachedConf();
            this.mCameraClear = false;
            loadThermalConf(usePowerSaveThermalConf);
        }
    }

    private void handleVRModeChange(boolean connect) {
        synchronized (this.mLock) {
            if (connect != this.mIsVRMode) {
                String str;
                this.mIsVRMode = connect;
                String str2 = "HwThermalStateManager";
                if (connect) {
                    str = "Switch to VR thermal configure";
                } else {
                    str = "Switch to close VR thermal configure";
                }
                Log.i(str2, str);
                clearCurrentPolicy();
                clearCachedConf();
                loadThermalConf(isOffPowerMode());
                return;
            }
            Log.w("HwThermalStateManager", "warning: same VR mode");
        }
    }

    private void handleQuickChargeStateChange(boolean quickChargeOff) {
        synchronized (this.mLock) {
            if (quickChargeOff != this.mIsQuickChargeOff) {
                String str;
                this.mIsQuickChargeOff = quickChargeOff;
                String str2 = "HwThermalStateManager";
                if (quickChargeOff) {
                    str = "Switch to off quick charge state thermal configure";
                } else {
                    str = "Switch to quick charge state thermal configure";
                }
                Log.i(str2, str);
                clearCurrentPolicy();
                clearCachedConf();
                loadThermalConf(isOffPowerMode());
                return;
            }
            Log.w("HwThermalStateManager", "warning: Quick charge state is no change, just return !");
        }
    }

    private void clearCachedConf() {
        HashMap<Integer, Boolean[]> lvlAlarm = new HashMap();
        for (Entry entry : this.mLvlAlarm.entrySet()) {
            Integer sensorType = (Integer) entry.getKey();
            Boolean[] alarm = new Boolean[8];
            for (int i = 0; i < 8; i++) {
                alarm[i] = Boolean.valueOf(false);
            }
            lvlAlarm.put(sensorType, alarm);
        }
        this.mLvlAlarm.clear();
        this.mLvlAlarm.putAll(lvlAlarm);
        this.mCpuMaxFreq.clear();
        this.mCpu1MaxFreq.clear();
        this.mCpu2MaxFreq.clear();
        this.mCpu3MaxFreq.clear();
        this.mA15CpuMaxFreq.clear();
        this.mGpuMaxFreq.clear();
        this.mWlanLimit.clear();
        this.mLcdLimit.clear();
        this.mFlashLimit.clear();
        this.mFrontFlashLimit.clear();
        this.mBatteryLimit.clear();
        this.mCallBatteryLimit.clear();
        this.mPAFallbackLimit.clear();
        this.mUsbChargeLimit.clear();
        this.mAcChargeLimit.clear();
        this.mUsbChargeAuxLimit.clear();
        this.mAcChargeAuxLimit.clear();
        this.mUsbVoltage.clear();
        this.mHmpThresholdUp.clear();
        this.mHmpThresholdDown.clear();
        this.mPopUpDialogState.clear();
        this.mIpaTemp.clear();
        this.mIpaPower.clear();
        this.mIpaSwitch.clear();
        if (!this.mCameraClear) {
            this.mCameraFps.clear();
        }
        this.mAppAction.clear();
        this.mAppCtrl.clear();
        this.mDirectChargeLimit.clear();
        this.mSensorActions.clear();
        this.mConfig.resetScreenStateCharge();
        this.mkeyThreadSched.clear();
    }

    private void trigerThermal() {
        ThermalEvent devt = new ThermalEvent(100);
        for (Entry entry : this.mSensorActions.entrySet()) {
            int sensorType = ((Integer) entry.getKey()).intValue();
            if (this.mCurTemp.containsKey(Integer.valueOf(sensorType))) {
                int curTmp = ((Integer) this.mCurTemp.get(Integer.valueOf(sensorType))).intValue();
                devt.setSensorType(sensorType);
                devt.setSensorTemp(curTmp);
                handleThermalEvent(devt, false);
            }
        }
    }

    private boolean isOffPowerMode() {
        return this.mPowerMode == 3;
    }

    private boolean isCharging() {
        return this.mIsCharging;
    }

    private boolean isCalling() {
        return this.mIsCalling;
    }

    private int getBatteryLevel() {
        if (this.mIsScreenOff) {
            DeviceAdapter deviceAdapter = this.mDeviceAdapter;
            int level = DeviceAdapter.getBatteryLevelFromNode();
            if (level > 0) {
                mBatteryLevel = level;
            }
        }
        return mBatteryLevel;
    }

    private boolean isScreenOff() {
        return this.mIsScreenOff;
    }

    private void notifyThermalActionChanged(ThermalAction thermalAction) {
        this.mPolicy.dispatchPolicy(thermalAction);
    }

    private void initPowerMode() {
        int mode = SystemProperties.getInt("persist.sys.smart_power", 0);
        if (mode == 0) {
            Log.w("HwThermalStateManager", "unknown power mode.");
            mode = 2;
        }
        this.mPowerMode = mode;
        Log.i("HwThermalStateManager", "init power mode:" + mode);
    }

    private void powerModeChanged(int newMode) {
        if (newMode < 1 || newMode > 4) {
            Log.e("HwThermalStateManager", "invalid new mode:" + newMode);
        } else if (this.mPowerMode != newMode) {
            Log.i("HwThermalStateManager", "power mode:" + this.mPowerMode + " -> " + newMode);
            if (newMode != 1) {
                notifyUsePowerSaveThermalPolicy(false);
            } else {
                notifyUsePowerSaveThermalPolicy(true);
            }
            this.mPowerMode = newMode;
        } else {
            Log.i("HwThermalStateManager", "power mode:" + this.mPowerMode + " same, do nothing ");
        }
    }

    private void clearCurrentPolicy() {
        ThermalEvent devt = new ThermalEvent(100);
        for (Entry entry : this.mSensorActions.entrySet()) {
            int sensorType = ((Integer) entry.getKey()).intValue();
            int lowestTmp = ((SensorTempAction) entry.getValue()).mPostiveMiniClrTemperature;
            devt.setSensorType(sensorType);
            devt.setSensorTemp(lowestTmp);
            handleThermalEvent(devt, false);
        }
    }

    private void handleBatteryEvent(Event evt) {
        synchronized (this.mLock) {
            int level = ((MsgEvent) evt).getIntent().getIntExtra("level", 0);
            if (mBatteryLevel != level) {
                mBatteryLevel = level;
                if (this.mConfig.mIsConfigBatteryLevel) {
                    this.mBatteryTrigged = false;
                    this.mBatteryCleared = false;
                    for (Entry entry : this.mSensorActions.entrySet()) {
                        int sensorType = ((Integer) entry.getKey()).intValue();
                        int sensorTmp = getThermalTemp(sensorType);
                        if (sensorTmp != -100000) {
                            multipleToTrigger(sensorType, sensorTmp, level, true);
                        }
                    }
                    return;
                }
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCallEvent(Event evt) {
        synchronized (this.mLock) {
            if (this.mCurCallBatteryType != -1) {
                if (this.mSensorActions.get(Integer.valueOf(this.mCurCallBatteryType)) != null) {
                    handleThermalPolicyChange(this.mCurEventID);
                } else if (DEBUG) {
                    Log.d("HwThermalStateManager", "current scene don't contain call_battery sensor type, do nothing");
                }
            } else if (DEBUG) {
                Log.d("HwThermalStateManager", "not ready for thermal, do nothing");
            }
        }
    }

    private void checkQuickCharger() {
        if (hasChargerType()) {
            synchronized (this.mLock) {
                if (isCharging()) {
                    if (isQuickCharger()) {
                        handleQuickChargeStateChange(false);
                    } else {
                        handleQuickChargeStateChange(true);
                    }
                }
            }
        }
    }

    private boolean isQuickCharger() {
        return getChargerType() == 4;
    }

    private boolean hasChargerType() {
        return getChargerType() != -1;
    }

    private int getChargerType() {
        Throwable th;
        FileInputStream fileInputStream = null;
        byte[] bytes = new byte[10];
        int type = -1;
        String chargerTypePath = "/sys/class/hw_power/charger/charge_data/chargerType";
        try {
            Arrays.fill(bytes, (byte) 0);
            FileInputStream fis = new FileInputStream(chargerTypePath);
            try {
                int len = fis.read(bytes);
                if (len > 0) {
                    String strType = new String(bytes, 0, len, "UTF-8");
                    try {
                        type = Integer.parseInt(strType.trim());
                    } catch (Exception e) {
                        fileInputStream = fis;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e2) {
                                Log.w("HwThermalStateManager", "close failed: " + chargerTypePath);
                            }
                        }
                        Log.i("HwThermalStateManager", "charger type: " + type);
                        return type;
                    } catch (Throwable th2) {
                        th = th2;
                        fileInputStream = fis;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Exception e3) {
                                Log.w("HwThermalStateManager", "close failed: " + chargerTypePath);
                            }
                        }
                        throw th;
                    }
                }
                if (fis == null) {
                    fileInputStream = fis;
                } else {
                    try {
                        fis.close();
                    } catch (Exception e4) {
                        Log.w("HwThermalStateManager", "close failed: " + chargerTypePath);
                    }
                }
            } catch (Exception e5) {
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.i("HwThermalStateManager", "charger type: " + type);
                return type;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (Exception e6) {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Log.i("HwThermalStateManager", "charger type: " + type);
            return type;
        } catch (Throwable th4) {
            th = th4;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        Log.i("HwThermalStateManager", "charger type: " + type);
        return type;
    }

    private void handleThermalEvent(Event evt, boolean fromEvent) {
        synchronized (this.mLock) {
            ThermalEvent devt = (ThermalEvent) evt;
            int sensorType = -1;
            int temperature = -100000;
            this.mAlarmRaised = false;
            this.mAlarmCleared = false;
            try {
                if (devt.getSensorType() > -1) {
                    sensorType = devt.getSensorType();
                }
                if (devt.getSensorTemp() != -100000) {
                    temperature = devt.getSensorTemp();
                }
            } catch (NumberFormatException e) {
                Log.e("HwThermalStateManager", "thermal event : " + devt + ", exception:" + e);
            }
            if (sensorType == -1 || temperature == -100000) {
                Log.w("HwThermalStateManager", "warning: invalid value");
                return;
            }
            if (sensorType == 9) {
                if (temperature <= -1000 || temperature >= 1000) {
                    temperature /= 1000;
                }
            }
            if (fromEvent) {
                if (DEBUG || temperature > 40) {
                    Log.d("HwThermalStateManager", "Thermal type: " + sensorType + " cur_temperature:" + temperature);
                }
            } else if (DEBUG) {
                Log.d("HwThermalStateManager", "Thermal type: " + sensorType + " cur_temperature:" + temperature + " trigger by self");
            }
            if (fromEvent) {
                this.mCurTemp.put(Integer.valueOf(sensorType), Integer.valueOf(temperature));
            }
            if (sensorType != this.mConfig.mWarningType) {
                if (sensorType != 2) {
                    if (this.mConfig.mDisabledLowSensorType >= 0 && sensorType == this.mConfig.mDisabledLowSensorType) {
                        ThermalConfig thermalConfig;
                        StringBuilder append;
                        ThermalConfig thermalConfig2;
                        if (temperature > this.mConfig.mDisabledLowTemp) {
                            thermalConfig = this.mConfig;
                            if (SystemProperties.getBoolean("hw.flash.disabled.by.low_temp", false)) {
                                append = new StringBuilder().append("set system prop ");
                                thermalConfig2 = this.mConfig;
                                Log.i("HwThermalStateManager", append.append("hw.flash.disabled.by.low_temp").append(" to false to notify camera enable the flash func").toString());
                                thermalConfig = this.mConfig;
                                SystemProperties.set("hw.flash.disabled.by.low_temp", "false");
                            }
                        } else {
                            thermalConfig = this.mConfig;
                            if (!SystemProperties.getBoolean("hw.flash.disabled.by.low_temp", false)) {
                                append = new StringBuilder().append("set system prop ");
                                thermalConfig2 = this.mConfig;
                                Log.i("HwThermalStateManager", append.append("hw.flash.disabled.by.low_temp").append(" to true to notify camera disable the flash func").toString());
                                thermalConfig = this.mConfig;
                                SystemProperties.set("hw.flash.disabled.by.low_temp", "true");
                            }
                        }
                    }
                } else if (temperature > 0) {
                    if (temperature >= 2) {
                        this.mHandler.removeMessages(102);
                        if (this.mIsColdChargingBroadcast) {
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(103));
                        }
                    }
                } else if (!(this.mHandler.hasMessages(102) || this.mIsColdChargingBroadcast)) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(102));
                }
            } else if (temperature < this.mConfig.mWarningTemp) {
                this.mHandler.removeMessages(100);
                if (this.mIsWarningBroadcast) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(101));
                }
            } else if (!(this.mHandler.hasMessages(100) || this.mIsWarningBroadcast)) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 120000);
            }
            multipleToTrigger(sensorType, temperature, getBatteryLevel(), false);
        }
    }

    private void handleScrnCtrlCharge(int sensorType, int temperature, Boolean[] lvl_alarm, int lvl_min) {
        String tempPolicy = this.mConfig.mNotLimitChargeVal;
        int trigLevel = -1;
        int mSimTrigLevel = -1;
        SensorTempAction sensorTemp = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        for (int i = sensorTemp.mNumThresholds - 1; i >= 0; i--) {
            if (temperature <= 0) {
                if (lvl_alarm[i].booleanValue()) {
                    trigLevel = i;
                }
            } else if (lvl_alarm[i].booleanValue()) {
                trigLevel = i;
                break;
            }
        }
        if (trigLevel >= 0 && sensorTemp.mTriggerAction[trigLevel].mActions != null) {
            mSimTrigLevel = trigLevel;
        } else if (lvl_min >= 0 && lvl_min <= sensorTemp.mNumThresholds && sensorTemp.mTriggerAction[lvl_min].mActions != null) {
            mSimTrigLevel = lvl_min;
        }
        if (mSimTrigLevel != -1) {
            String bcurrent = (String) sensorTemp.mTriggerAction[trigLevel].mActions.get("bcurrent");
            if (bcurrent != null) {
                tempPolicy = bcurrent;
            }
        }
        Log.w("HwThermalStateManager", "trigLevel = " + trigLevel + ", mSimTrigLevel = " + mSimTrigLevel + ", tempPolicy = " + tempPolicy);
        ThermalAction simulateThermalAction = generateThermalAction(System.currentTimeMillis(), sensorType, sensorTemp.mSensorName, temperature, "bcurrent", tempPolicy);
        if (simulateThermalAction != null) {
            notifyThermalActionChanged(simulateThermalAction);
        }
    }

    private void multipleToTrigger(int sensorType, int temperature, int batteryLevel, boolean isBatteryTrigger) {
        if (this.mSensorActions.containsKey(Integer.valueOf(sensorType))) {
            SensorTempAction sensorTemp = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
            int max_thr = sensorTemp.mNumThresholds;
            int lvl_max = -1;
            int lvl_min = 9;
            Object lvl_alarm = (Boolean[]) this.mLvlAlarm.get(Integer.valueOf(sensorType));
            int i = max_thr - 1;
            while (i >= 0) {
                if (thresholdTrigger(temperature, sensorType, i)) {
                    if (!lvl_alarm[i].booleanValue()) {
                        lvl_alarm[i] = Boolean.valueOf(true);
                        this.mAlarmRaised = true;
                    }
                    if (i > lvl_max) {
                        lvl_max = i;
                    }
                }
                if (thresholdClear(temperature, sensorType, i) && lvl_alarm[i].booleanValue()) {
                    lvl_alarm[i] = Boolean.valueOf(false);
                    this.mAlarmCleared = true;
                    if (i < lvl_min) {
                        lvl_min = i;
                    }
                }
                i--;
            }
            if (this.mConfig.mNotLimitChargeVal != null) {
                synchronized (this.mLock) {
                    if (this.mScrnCtrlChargeCounter.get() <= 0) {
                        this.mScrnCtrlChargeCounter.set(0);
                        this.mScrnStateCtrlChargeActive = false;
                        this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mNotLimitChargeVal;
                    } else {
                        this.mScrnStateCtrlChargeActive = true;
                        this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mScreenOnMaxBcurrentVal;
                    }
                }
                if (!(isBatteryTrigger || this.mAlarmRaised || this.mAlarmCleared)) {
                    handleScrnCtrlCharge(sensorType, temperature, lvl_alarm, lvl_min);
                }
            }
            if (isBatteryTrigger || this.mAlarmRaised || this.mAlarmCleared) {
                this.mIsSwitchBroadcast = false;
                this.mIsLowTempWaringBroadcast = false;
                long time = System.currentTimeMillis();
                int triggerLevel = -1;
                for (i = max_thr - 1; i >= 0; i--) {
                    if (temperature <= 0) {
                        if (lvl_alarm[i].booleanValue()) {
                            triggerLevel = i;
                        }
                    } else if (lvl_alarm[i].booleanValue()) {
                        this.mCatchActionlvl = i;
                        triggerLevel = i;
                        break;
                    }
                }
                if (this.mConfig.mIsConfigBatteryLevel && !isBatteryTrigger && this.mAlarmCleared && lvl_min < sensorTemp.mNumThresholds) {
                    TriggerAction clrAction = sensorTemp.mTriggerAction[lvl_min];
                    for (i = 0; i < clrAction.getActionItemsNum(); i++) {
                        if (clrAction.mActionState[i]) {
                            clrAction.mActionState[i] = false;
                        }
                    }
                }
                HashMap actions = null;
                if (triggerLevel >= 0) {
                    TriggerAction triggerAction = sensorTemp.mTriggerAction[triggerLevel];
                    if (triggerAction.mActions != null) {
                        actions = (HashMap) triggerAction.mActions.clone();
                    } else if (this.mConfig.mIsConfigBatteryLevel) {
                        actions = getActionsByBattery(triggerAction, batteryLevel);
                    }
                }
                if (actions != null) {
                    Log.i("HwThermalStateManager", "triggerLevel = " + triggerLevel + ", triggerAction :" + actions.toString());
                    for (Entry entry : actions.entrySet()) {
                        String action = (String) entry.getKey();
                        String policy = (String) entry.getValue();
                        if (this.mConfig.mScreenOnMaxBcurrentVal != null) {
                            if (!isScreenOff() && "bcurrent".equals(action)) {
                                if (!(this.mConfig.mNotLimitChargeVal == null || this.mConfig.mScreenOnMaxBcurrentVal == null)) {
                                    int policyVal = Integer.parseInt(policy);
                                    int notLimitChargeVal = Integer.parseInt(this.mConfig.mNotLimitChargeVal);
                                    int screenOnMaxBcurrentVal = Integer.parseInt(this.mConfig.mScreenOnMaxBcurrentVal);
                                    Log.i("HwThermalStateManager", "check policyVal = " + policyVal + ", notLimitChargeVal = " + notLimitChargeVal + ", screenOnMaxBcurrentVal = " + screenOnMaxBcurrentVal);
                                    if (policyVal == notLimitChargeVal || policyVal > screenOnMaxBcurrentVal) {
                                        this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mScreenOnMaxBcurrentVal;
                                        this.mScrnStateCtrlChargeActive = true;
                                    }
                                }
                            } else if (isScreenOff() && "bcurrent".equals(action)) {
                                synchronized (this.mLock) {
                                    this.mScrnCtrlChargeCounter.set(0);
                                }
                                this.mScrnStateCtrlChargeActive = false;
                                Log.w("HwThermalStateManager", "set mScrnCtrlChargeCounter to 0 for handle screen off event");
                            }
                        }
                        if (!this.mExecAction.containsEntry(Integer.valueOf(sensorType), action)) {
                            this.mExecAction.put(Integer.valueOf(sensorType), action);
                        }
                        if ("call_battery".equals(action)) {
                            this.mCurCallBatteryType = sensorType;
                            this.mCurCallBatteryTemp = temperature;
                            this.mCurCallBatteryPolicy = policy;
                        }
                        ThermalAction thermalAction = generateThermalAction(time, sensorType, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, action, policy);
                        if (thermalAction != null) {
                            notifyThermalActionChanged(thermalAction);
                        }
                    }
                }
                if (this.mAlarmCleared && temperature >= 0) {
                    if (!isMatchClearAll(lvl_min, sensorTemp)) {
                        if (!isSameState(lvl_alarm, lvl_alarm.length, false)) {
                        }
                    }
                    if (DEBUG) {
                        Log.d("HwThermalStateManager", "Clearing all alarms on sensor:" + sensorType);
                    }
                    clearAllAction(sensorType, temperature, time);
                }
                if (!isBatteryTrigger) {
                    this.mLvlAlarm.put(Integer.valueOf(sensorType), lvl_alarm);
                }
                return;
            }
            if (DEBUG) {
                Log.d("HwThermalStateManager", "do nothing");
            }
            return;
        }
        Log.w("HwThermalStateManager", "warning: no config thermal type:" + sensorType + " temperature:" + temperature);
    }

    private boolean isSameState(Boolean[] array, int len, boolean state) {
        if (this.mConfig.mIsConfigBatteryLevel) {
            for (int index = 0; index < len; index++) {
                if (array[index].booleanValue() != state) {
                    return false;
                }
            }
            Log.i("HwThermalStateManager", "is same state, will to clr all.");
            return true;
        }
        Log.i("HwThermalStateManager", "not config low temperature policy, not to handle.");
        return false;
    }

    private int getMinValue(HashMap<Integer, Integer> checkHashMap, int exceptionValue) {
        int retValue = Integer.MAX_VALUE;
        for (Entry entry : checkHashMap.entrySet()) {
            int val = ((Integer) entry.getValue()).intValue();
            if (val != exceptionValue && val < retValue) {
                retValue = val;
            }
        }
        if (retValue != Integer.MAX_VALUE) {
            return retValue;
        }
        return 0;
    }

    private HashMap<String, String> getActionsByBattery(TriggerAction triggerAction, int batteryLevel) {
        if (triggerAction == null) {
            return null;
        }
        int triggerLevel = -1;
        int actionsNum = triggerAction.getActionItemsNum();
        boolean[] lvl_alarm = triggerAction.mActionState;
        int i = 0;
        while (i < actionsNum) {
            if (batteryLevelTrigger(batteryLevel, triggerAction, i) && !lvl_alarm[i]) {
                lvl_alarm[i] = true;
                this.mBatteryTrigged = true;
            }
            if (batteryLevelClear(batteryLevel, triggerAction, i) && lvl_alarm[i]) {
                lvl_alarm[i] = false;
                this.mBatteryCleared = true;
            }
            i++;
        }
        if (this.mBatteryTrigged || this.mBatteryCleared || this.mIsSceneChange) {
            triggerAction.mActionState = lvl_alarm;
            for (i = 0; i < actionsNum; i++) {
                if (lvl_alarm[i]) {
                    triggerLevel = i;
                    break;
                }
            }
            if (triggerLevel == -1) {
                return null;
            }
            Log.i("HwThermalStateManager", "battery trigger: batteryLevel = " + batteryLevel + ", triggerLevel = " + triggerLevel + ", actions = " + triggerAction.mActionItems[triggerLevel].mActions.toString());
            return triggerAction.mActionItems[triggerLevel].mActions;
        }
        if (DEBUG) {
            Log.d("HwThermalStateManager", "do nothing, not trigger battery policy.");
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isMatchClearAll(int minLevel, SensorTempAction sensorTemp) {
        boolean ret = false;
        if (minLevel == 0) {
            ret = sensorTemp.mTriggerAction[minLevel].mLevelClear > 0;
        } else if (minLevel > 0) {
            if (minLevel > sensorTemp.mNumThresholds) {
                return false;
            }
            if (sensorTemp.mTriggerAction[minLevel].mLevelClear <= 0 || sensorTemp.mTriggerAction[minLevel - 1].mLevelClear > 0) {
                if (sensorTemp.mTriggerAction[minLevel].mLevelClear <= 0) {
                }
            }
            ret = true;
        }
        return ret;
    }

    private void clearAllAction(int sensorType, int temperature, long time) {
        for (String action : this.mExecAction.getAll(Integer.valueOf(sensorType))) {
            boolean isOkay = true;
            String policy = null;
            if ("cpu".equals(action)) {
                policy = "0";
            } else if ("cpu1".equals(action)) {
                policy = "0";
            } else if ("cpu2".equals(action)) {
                policy = "0";
            } else if ("cpu3".equals(action)) {
                policy = "0";
            } else if ("cpu_a15".equals(action)) {
                policy = "0";
            } else if ("gpu".equals(action)) {
                policy = "0";
            } else if ("lcd".equals(action)) {
                policy = "0";
            } else if ("battery".equals(action)) {
                policy = "0";
            } else if ("call_battery".equals(action)) {
                policy = "0";
            } else if ("paback".equals(action)) {
                policy = "0";
            } else if ("flash".equals(action)) {
                policy = "0";
            } else if ("flash_front".equals(action)) {
                policy = "0";
            } else if ("wlan".equals(action)) {
                policy = "0";
            } else if ("ucurrent".equals(action)) {
                policy = "0";
            } else if ("ucurrent_aux".equals(action)) {
                policy = "0";
            } else if ("ipa_power".equals(action)) {
                policy = "0";
            } else if ("ipa_temp".equals(action)) {
                policy = "0";
            } else if ("ipa_switch".equals(action)) {
                policy = "0";
            } else if ("fork_on_big".equals(action)) {
                policy = "1";
            } else if ("boost".equals(action)) {
                policy = "0";
            } else if ("camera_fps".equals(action)) {
                if (!this.mCameraClear) {
                    policy = "0";
                }
            } else if ("uvoltage".equals(action)) {
                policy = "0";
            } else if ("app_action".equals(action)) {
                policy = "0";
            } else if ("app_ctrl".equals(action)) {
                policy = "0";
            } else if ("vr_warning_level".equals(action)) {
                policy = "0";
            } else if ("bcurrent".equals(action)) {
                if (this.mConfig.mScreenOnMaxBcurrentVal == null || !this.mScrnStateCtrlChargeActive || isScreenOff()) {
                    this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mNotLimitChargeVal;
                } else {
                    Log.i("HwThermalStateManager", "assign exec policy from: " + null + " to: " + this.mConfig.mScreenOnMaxBcurrentVal);
                    this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mScreenOnMaxBcurrentVal;
                }
                policy = "0";
            } else if ("bcurrent_aux".equals(action)) {
                policy = "0";
            } else if ("threshold_up".equals(action)) {
                policy = "0";
            } else if ("threshold_down".equals(action)) {
                policy = "0";
            } else if ("direct_charger".equals(action)) {
                policy = "0";
            } else if ("key_thread_sched".equals(action)) {
                policy = "0";
            } else {
                if (DEBUG) {
                    Log.i("HwThermalStateManager", "not need reset action: " + action);
                }
                isOkay = false;
            }
            if (isOkay) {
                ThermalAction thermalAction = generateThermalAction(time, sensorType, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, action, policy);
                if (thermalAction != null) {
                    notifyThermalActionChanged(thermalAction);
                }
            }
        }
        this.mExecAction.removeAll(Integer.valueOf(sensorType));
    }

    private ThermalAction generateThermalAction(long time, int sensorType, String sensorName, int temperature, String action, String policy) {
        ThermalAction thermalAction = null;
        try {
            int reqFrequency;
            if ("cpu".equals(action)) {
                this.mCpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpufreq, this.mCpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpufreq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu1".equals(action)) {
                this.mCpu1MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu1freq, this.mCpu1MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu1freq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu2".equals(action)) {
                this.mCpu2MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu2freq, this.mCpu2MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu2freq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu3".equals(action)) {
                this.mCpu3MaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurCpu3freq, this.mCpu3MaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurCpu3freq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("cpu_a15".equals(action)) {
                this.mA15CpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurA15Cpufreq, this.mA15CpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurA15Cpufreq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("gpu".equals(action)) {
                this.mGpuMaxFreq.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                reqFrequency = getThermalActionPolicy(this.mCurGpufreq, this.mGpuMaxFreq, false);
                if (reqFrequency != -1) {
                    this.mCurGpufreq = reqFrequency;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqFrequency));
                }
            } else if ("ipa_power".equals(action)) {
                this.mIpaPower.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaPower = getThermalActionPolicy(this.mCurIpaPower, this.mIpaPower, false);
                if (reqIpaPower != -1) {
                    this.mCurIpaPower = reqIpaPower;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaPower));
                }
            } else if ("ipa_temp".equals(action)) {
                this.mIpaTemp.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaTemp = getThermalActionPolicy(this.mCurIpaTemp, this.mIpaTemp, false);
                if (reqIpaTemp != -1) {
                    this.mCurIpaTemp = reqIpaTemp;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaTemp));
                }
            } else if ("ipa_switch".equals(action)) {
                this.mIpaSwitch.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqIpaSwitch = getThermalActionPolicy(this.mCurIpaSwitch, this.mIpaSwitch, false);
                if (reqIpaSwitch != -1) {
                    this.mCurIpaSwitch = reqIpaSwitch;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqIpaSwitch));
                }
            } else if ("boost".equals(action)) {
                this.mBoost.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqBoost = 0;
                for (Entry entry : this.mBoost.entrySet()) {
                    lvl = ((Integer) entry.getValue()).intValue();
                    if (lvl != 0) {
                        reqBoost = lvl;
                        break;
                    }
                }
                if (reqBoost != this.mCurBoost) {
                    this.mCurBoost = reqBoost;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqBoost));
                }
            } else if ("app_ctrl".equals(action)) {
                this.mAppCtrl.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqAppCtrl = 0;
                for (Entry entry2 : this.mAppCtrl.entrySet()) {
                    lvl = ((Integer) entry2.getValue()).intValue();
                    if (lvl != -1) {
                        reqAppCtrl = lvl;
                        break;
                    }
                }
                this.mCurAppCtrl = reqAppCtrl;
                thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqAppCtrl));
            } else if ("fork_on_big".equals(action)) {
                this.mForkOnBig.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int reqForkOnBig = 1;
                for (Entry entry22 : this.mForkOnBig.entrySet()) {
                    lvl = ((Integer) entry22.getValue()).intValue();
                    if (lvl == 0) {
                        reqForkOnBig = lvl;
                        break;
                    }
                }
                if (reqForkOnBig != this.mCurForkOnBig) {
                    this.mCurForkOnBig = reqForkOnBig;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(reqForkOnBig));
                }
            } else if ("app".equals(action)) {
                if (!this.mIsSwitchBroadcast) {
                    BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                    this.mIsSwitchBroadcast = true;
                }
                thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
            } else if ("lcd".equals(action)) {
                this.mLcdLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                int lcdLimit = getThermalActionPolicy(this.mCurLcdLimt, this.mLcdLimit, true);
                if (lcdLimit != -1) {
                    this.mCurLcdLimt = lcdLimit;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lcdLimit));
                }
            } else if ("wlan".equals(action)) {
                int wlanLvl = Integer.parseInt(policy);
                if (isVaildWlanLimitLvl(wlanLvl)) {
                    this.mWlanLimit.put(Integer.valueOf(sensorType), Integer.valueOf(wlanLvl));
                    lvl = getThermalActionPolicy(this.mCurWlanLvl, this.mWlanLimit, true);
                    if (lvl != -1) {
                        this.mCurWlanLvl = lvl;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lvl));
                    }
                }
            } else if ("flash".equals(action)) {
                this.mFlashLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                flashLimit = getThermalActionPolicy(this.mCurFlashLimt, this.mFlashLimit, true);
                if (flashLimit != -1) {
                    if (!(this.mIsSwitchBroadcast || flashLimit == 0)) {
                        BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                        this.mIsSwitchBroadcast = true;
                    }
                    this.mCurFlashLimt = flashLimit;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(flashLimit));
                }
            } else if ("flash_front".equals(action)) {
                this.mFrontFlashLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                flashLimit = getThermalActionPolicy(this.mCurFrontFlashLimt, this.mFrontFlashLimit, true);
                if (flashLimit != -1) {
                    if (!(this.mIsSwitchBroadcast || flashLimit == 0)) {
                        BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                        this.mIsSwitchBroadcast = true;
                    }
                    this.mCurFrontFlashLimt = flashLimit;
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(flashLimit));
                }
            } else {
                if ("camera_fps".equals(action)) {
                    if (!this.mCameraClear) {
                        this.mCameraFps.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                        int cameraFpsLimit = getThermalActionPolicy(this.mCurCameraFps, this.mCameraFps, true);
                        if (cameraFpsLimit != -1) {
                            this.mCurCameraFps = cameraFpsLimit;
                            thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(cameraFpsLimit));
                        }
                    }
                }
                if ("uvoltage".equals(action)) {
                    this.mUsbVoltage.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int usbVoltageLimit = getThermalActionPolicy(this.mCurUsbVoltage, this.mUsbVoltage, true);
                    if (usbVoltageLimit != -1) {
                        this.mCurUsbVoltage = usbVoltageLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(usbVoltageLimit));
                    }
                } else if ("app_action".equals(action)) {
                    this.mAppAction.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int appActionLimit = getThermalActionPolicy(this.mCurAppAction, this.mAppAction, true);
                    if (appActionLimit != -1) {
                        this.mCurAppAction = appActionLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(appActionLimit));
                    }
                } else if ("key_thread_sched".equals(action)) {
                    this.mkeyThreadSched.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int keyThreadSched = 0;
                    for (Entry entry222 : this.mkeyThreadSched.entrySet()) {
                        lvl = ((Integer) entry222.getValue()).intValue();
                        if (lvl != 0) {
                            keyThreadSched = lvl;
                            break;
                        }
                    }
                    if (keyThreadSched != this.mCurKeyThreadSched) {
                        this.mCurKeyThreadSched = keyThreadSched;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(keyThreadSched));
                    }
                } else if ("vr_warning_level".equals(action)) {
                    this.mVRWarningLevel.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int warningLevel = getThermalActionPolicy(this.mCurVRWarningLevel, this.mVRWarningLevel, true);
                    if (warningLevel != -1) {
                        this.mCurVRWarningLevel = warningLevel;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(warningLevel));
                    }
                } else if ("shutdown".equals(action)) {
                    BroadcastAdapter.sendThermalComUIEvent(this.mContext, "shutdown", null);
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
                } else if ("paback".equals(action)) {
                    this.mPAFallbackLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int fallback = getThermalActionPolicy(this.mCurPAFallbackLimt, this.mPAFallbackLimit, true);
                    if (fallback != -1) {
                        this.mCurPAFallbackLimt = fallback;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(fallback));
                    }
                } else if ("camera_warning".equals(action) || "camera_stop".equals(action)) {
                    if (!this.mIsSwitchBroadcast) {
                        BroadcastAdapter.sendThermalComUIEvent(this.mContext, "close_powerswitch_app", null);
                        this.mIsSwitchBroadcast = true;
                    }
                    thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(policy));
                } else if ("call_battery".equals(action) || "battery".equals(action)) {
                    if (DEBUG) {
                        Log.d("HwThermalStateManager", "isCharging:" + isCharging() + " isCalling:" + isCalling());
                    }
                    if (!isCalling() && this.mCallBatteryLimit.size() > 0) {
                        if (DEBUG) {
                            Log.d("HwThermalStateManager", "not calling, clear limit");
                        }
                        this.mCallBatteryLimit.clear();
                    }
                    int reqLimit = Integer.parseInt(policy);
                    if ("call_battery".equals(action) && isCalling()) {
                        this.mCallBatteryLimit.put(Integer.valueOf(sensorType), Integer.valueOf(reqLimit));
                    } else if ("battery".equals(action)) {
                        this.mBatteryLimit.put(Integer.valueOf(sensorType), Integer.valueOf(reqLimit));
                    }
                    if (this.mConfig.mMutilSensorBatteryClr && "battery".equals(action) && this.mAlarmCleared) {
                        HashMap<Integer, Integer> clearBattery = ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mTriggerAction[this.mCatchActionlvl + 1].mLevelClearBattery;
                        if (clearBattery.size() > 0 && !multiThresholdClear(clearBattery)) {
                            Log.i("HwThermalStateManager", "clear battery, but other sensor can't satisfy the conditions at the same time, do nothing");
                            return null;
                        }
                    }
                    lvl = getBatteryPolicy(this.mBatteryLimit, this.mCallBatteryLimit);
                    if (this.mCurChargingLvl != lvl) {
                        this.mCurChargingLvl = lvl;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(lvl));
                    }
                } else if ("direct_charger".equals(action)) {
                    this.mDirectChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int directChargeLimit = getThermalActionPolicy(this.mCurDirectChargeLimt, this.mDirectChargeLimit, false);
                    if (directChargeLimit != -1) {
                        this.mCurDirectChargeLimt = directChargeLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(directChargeLimit));
                    }
                } else if ("ucurrent".equals(action)) {
                    this.mUsbChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int usbChargeLimit = getThermalActionPolicy(this.mCurUsbChargeLimt, this.mUsbChargeLimit, false);
                    if (usbChargeLimit != -1) {
                        this.mCurUsbChargeLimt = usbChargeLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(usbChargeLimit));
                    }
                } else if ("ucurrent_aux".equals(action)) {
                    this.mUsbChargeAuxLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int usbChargeAuxLimit = getThermalActionPolicy(this.mCurUsbChargeAuxLimt, this.mUsbChargeAuxLimit, false);
                    if (usbChargeAuxLimit != -1) {
                        this.mCurUsbChargeAuxLimt = usbChargeAuxLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(usbChargeAuxLimit));
                    }
                } else if ("bcurrent".equals(action)) {
                    this.mAcChargeLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    if (DEBUG) {
                        Log.i("HwThermalStateManager", "after put sensorType = " + sensorType + ", policy = " + policy + ", mAcChargeLimit = " + this.mAcChargeLimit);
                    }
                    int acChargeLimit = getThermalActionPolicy(this.mCurAcChargeLimt, this.mAcChargeLimit, false);
                    if (DEBUG) {
                        Log.i("HwThermalStateManager", "after invoke getThermalActionPolicy, action = " + action + ", policy = " + policy + ", acChargeLimit = " + acChargeLimit + ", mCurrentScreenCtrlBcurrentVal = " + this.mCurrentScreenCtrlBcurrentVal);
                    }
                    if (!(this.mConfig.mNotLimitChargeVal == null || isScreenOff())) {
                        if (this.mCurrentScreenCtrlBcurrentVal == null) {
                            this.mCurrentScreenCtrlBcurrentVal = this.mConfig.mScreenOnMaxBcurrentVal;
                        }
                        int mCrntScrnCtrlBCurntVal = Integer.parseInt(this.mCurrentScreenCtrlBcurrentVal);
                        acChargeLimit = getMinValue(this.mAcChargeLimit, Integer.parseInt(this.mConfig.mNotLimitChargeVal));
                        if (acChargeLimit == -1 || acChargeLimit == Integer.parseInt(this.mConfig.mNotLimitChargeVal) || acChargeLimit > mCrntScrnCtrlBCurntVal) {
                            acChargeLimit = mCrntScrnCtrlBCurntVal;
                        }
                        if (DEBUG) {
                            Log.i("HwThermalStateManager", "at last, acChargeLimit = " + acChargeLimit + ", mCurAcChargeLimt = " + this.mCurAcChargeLimt + ", mNotLimitChargeVal = " + this.mConfig.mNotLimitChargeVal);
                        }
                    }
                    if (acChargeLimit != -1) {
                        this.mCurAcChargeLimt = acChargeLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(acChargeLimit));
                    }
                } else if ("bcurrent_aux".equals(action)) {
                    this.mAcChargeAuxLimit.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int acChargeAuxLimit = getThermalActionPolicy(this.mCurAcChargeAuxLimt, this.mAcChargeAuxLimit, false);
                    if (acChargeAuxLimit != -1) {
                        this.mCurAcChargeAuxLimt = acChargeAuxLimit;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(acChargeAuxLimit));
                    }
                } else if ("threshold_up".equals(action)) {
                    this.mHmpThresholdUp.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int upPolicyValue = getThermalActionPolicy(this.mCurThresholdUpPolicy, this.mHmpThresholdUp, false);
                    if (upPolicyValue != -1) {
                        this.mCurThresholdUpPolicy = upPolicyValue;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(upPolicyValue));
                    }
                } else if ("threshold_down".equals(action)) {
                    this.mHmpThresholdDown.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int downPolicyValue = getThermalActionPolicy(this.mCurThresholdDownPolicy, this.mHmpThresholdDown, false);
                    if (downPolicyValue != -1) {
                        this.mCurThresholdDownPolicy = downPolicyValue;
                        thermalAction = newThermalAction(time, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, sensorType, temperature, action, String.valueOf(downPolicyValue));
                    }
                } else if ("pop_up_dialog".equals(action)) {
                    this.mPopUpDialogState.put(Integer.valueOf(sensorType), Integer.valueOf(Integer.parseInt(policy)));
                    int popState = getThermalActionPolicy(this.mCurpopState, this.mPopUpDialogState, true);
                    if (popState != -1) {
                        if (!(this.mIsLowTempWaringBroadcast || popState == 0)) {
                            BroadcastAdapter.sendLowTempWarningEvent(this.mContext, ((SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType))).mSensorName, temperature, getBatteryLevel());
                            Log.i("HwThermalStateManager", "pop up dialog!");
                            this.mIsLowTempWaringBroadcast = true;
                        }
                        this.mCurpopState = popState;
                    }
                }
            }
        } catch (Throwable e) {
            Log.e("HwThermalStateManager", "generate thermal action exception", e);
        } catch (Throwable e2) {
            Log.e("HwThermalStateManager", "generate thermal action index out of bounds exception", e2);
        } catch (Throwable e3) {
            Log.e("HwThermalStateManager", "generate thermal action null pointer exception", e3);
        }
        if (thermalAction != null) {
            Log.i("HwThermalStateManager", "sensor name: " + thermalAction.getSensorName() + " temperature: " + thermalAction.getTemperature() + " , thermal action: " + thermalAction.getAction() + " policy:" + thermalAction.getValue());
        }
        return thermalAction;
    }

    private ThermalAction newThermalAction(long timestamp, String name, int type, int temperature, String action, String value) {
        ThermalAction thermalAction = ThermalAction.obtain();
        thermalAction.resetAs(timestamp, name, type, temperature, action, value);
        return thermalAction;
    }

    private int getThermalActionPolicy(int curLvl, HashMap<Integer, Integer> map, boolean isMax) {
        int limit_lvl;
        boolean clearAll = true;
        if (isMax) {
            limit_lvl = -1;
        } else {
            limit_lvl = Integer.MAX_VALUE;
        }
        for (Entry entry : map.entrySet()) {
            int lvl = ((Integer) entry.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (isMax) {
                    if (lvl > limit_lvl) {
                        limit_lvl = lvl;
                    }
                } else if (lvl < limit_lvl) {
                    limit_lvl = lvl;
                }
            }
        }
        if (DEBUG) {
            Log.d("HwThermalStateManager", "clearAll:" + clearAll + " limit_lvl:" + limit_lvl + " mCurLvl:" + curLvl);
        }
        if (clearAll) {
            limit_lvl = 0;
        }
        if (limit_lvl == curLvl) {
            return -1;
        }
        return limit_lvl;
    }

    private int getBatteryPolicy(HashMap<Integer, Integer> batteryMap, HashMap<Integer, Integer> callBatteryMap) {
        boolean clearAll = true;
        int battery_lvl = Integer.MAX_VALUE;
        int call_battery_lvl = Integer.MAX_VALUE;
        for (Entry entry : batteryMap.entrySet()) {
            int lvl = ((Integer) entry.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (lvl < battery_lvl) {
                    battery_lvl = lvl;
                }
            }
        }
        if (DEBUG) {
            Log.d("HwThermalStateManager", "battery_lvl:" + battery_lvl);
        }
        for (Entry entry2 : callBatteryMap.entrySet()) {
            lvl = ((Integer) entry2.getValue()).intValue();
            if (lvl > 0) {
                clearAll = false;
                if (lvl < call_battery_lvl) {
                    call_battery_lvl = lvl;
                }
            }
        }
        if (DEBUG) {
            Log.d("HwThermalStateManager", "call_battery_lvl:" + call_battery_lvl);
        }
        if (clearAll) {
            return 0;
        }
        return Math.min(battery_lvl, call_battery_lvl);
    }

    private boolean thresholdTrigger(int temp, int sensorType, int level) {
        SensorTempAction mSensorTempAction = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        boolean ret = false;
        if (temp > 0 && mSensorTempAction.mTriggerAction[level].mLevelTrigger > 0) {
            ret = temp >= mSensorTempAction.mTriggerAction[level].mLevelTrigger;
        } else if (temp < 0 && mSensorTempAction.mTriggerAction[level].mLevelTrigger < 0) {
            ret = temp <= mSensorTempAction.mTriggerAction[level].mLevelTrigger;
        }
        return ret;
    }

    private boolean thresholdClear(int temp, int sensorType, int level) {
        SensorTempAction mSensorTempAction = (SensorTempAction) this.mSensorActions.get(Integer.valueOf(sensorType));
        if (temp <= 0) {
            if (mSensorTempAction.mTriggerAction[level].mLevelClear > 0) {
                return true;
            }
            return temp >= mSensorTempAction.mTriggerAction[level].mLevelClear;
        } else if (mSensorTempAction.mTriggerAction[level].mLevelClear <= 0) {
            return true;
        } else {
            return temp <= mSensorTempAction.mTriggerAction[level].mLevelClear;
        }
    }

    private boolean batteryLevelTrigger(int batteryLevel, TriggerAction triggerAction, int level) {
        if (batteryLevel > triggerAction.mActionItems[level].mBatLevelTrigger) {
            return false;
        }
        return true;
    }

    private boolean batteryLevelClear(int batteryLevel, TriggerAction triggerAction, int level) {
        if (batteryLevel < triggerAction.mActionItems[level].mBatLevelClear) {
            return false;
        }
        return true;
    }

    private boolean multiThresholdClear(HashMap<Integer, Integer> clear) {
        for (Entry entry : clear.entrySet()) {
            int cur_temp;
            int trigger_sensor = ((Integer) entry.getKey()).intValue();
            int trigger_temp = ((Integer) entry.getValue()).intValue();
            if (this.mCurTemp.get(Integer.valueOf(trigger_sensor)) == null) {
                cur_temp = -1;
                continue;
            } else {
                cur_temp = ((Integer) this.mCurTemp.get(Integer.valueOf(trigger_sensor))).intValue();
                continue;
            }
            if (cur_temp > trigger_temp) {
                return false;
            }
        }
        return true;
    }

    public String getThermalInterface(String action) {
        return this.mConfig.getThermalInterface(action);
    }

    private int getThermalTemp(int sensorType) {
        if (this.mCurTemp.containsKey(Integer.valueOf(sensorType))) {
            return ((Integer) this.mCurTemp.get(Integer.valueOf(sensorType))).intValue();
        }
        return -100000;
    }

    private boolean isVaildWlanLimitLvl(int lvl) {
        if (lvl >= 0 && lvl <= 4) {
            return true;
        }
        return false;
    }

    public void dump(PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
            pw.println("\nTHERMAL STATE MANAGER ");
            pw.println("  Config: ");
            pw.println("        mCurrentScreenCtrlBcurrentVal: " + this.mCurrentScreenCtrlBcurrentVal);
            pw.println("        mSupportPowerSaveThermalPolicy: " + this.mSupportPowerSaveThermalPolicy);
            pw.println("        mIsSupportQCOff: " + this.mIsSupportQCOff);
            pw.println("        mIsQuickChargeOff: " + this.mIsQuickChargeOff);
            pw.println("        mSupportVRMode: " + this.mSupportVRMode);
            pw.println("        mIsVRMode: " + this.mIsVRMode);
            this.mConfig.dump(pw, args);
            pw.println("  Sensor Temperature: ");
            for (Entry entry : this.mCurTemp.entrySet()) {
                int type = ((Integer) entry.getKey()).intValue();
                pw.println("    Sensor:" + type + " Temperature:" + ((Integer) entry.getValue()).intValue());
            }
            pw.println("");
            pw.println("  Current Policy: ");
            pw.println("    Current Action ID:" + this.mCurEventID);
            pw.println("    CPU  :" + this.mCurCpufreq + "  CPU1 :" + this.mCurCpu1freq + "  CPU2 :" + this.mCurCpu2freq + "  CPU3 :" + this.mCurCpu3freq);
            pw.println("    A15  :" + this.mCurA15Cpufreq);
            pw.println("    GPU  : " + this.mCurGpufreq);
            pw.println("    IPA Power : " + this.mCurIpaPower + " Control Temperature : " + this.mCurIpaTemp + " Switch Temperature : " + this.mCurIpaSwitch);
            pw.println("    Fork On Big Cluster : " + this.mCurForkOnBig);
            pw.println("    BOOST : " + this.mCurBoost);
            pw.println("    Battery Charging Level : " + this.mCurChargingLvl);
            pw.println("    Lcd Limit Level: " + this.mCurLcdLimt);
            pw.println("    Flash Limit, Backgroud : " + this.mCurFlashLimt + " Foregroud : " + this.mCurFrontFlashLimt);
            pw.println("    Charge current, USB : " + this.mCurUsbChargeLimt + "  AC : " + this.mCurAcChargeLimt);
            pw.println("    Charge current, USB AUX: " + this.mCurUsbChargeAuxLimt + "  AC AUX: " + this.mCurAcChargeAuxLimt);
            pw.println("    Charge Direct Charge limit : " + this.mCurDirectChargeLimt);
            pw.println("    Usb Charge Voltage : " + this.mCurUsbVoltage);
            pw.println("    HMP ThresholdDown : " + this.mCurThresholdDownPolicy + " ThresholdUp : " + this.mCurThresholdUpPolicy);
            pw.println("    Camera Fps : " + this.mCurCameraFps);
            pw.println("    App_Action : " + this.mCurAppAction);
            pw.println("    App_CTRL : " + this.mCurAppCtrl);
            pw.println("    PA Fallback Limit : " + this.mCurPAFallbackLimt);
            pw.println("    Wlan Limit level : " + this.mCurWlanLvl);
            pw.println("    VR Warning Level : " + this.mCurVRWarningLevel);
            pw.println("    Key Thread Sched : " + this.mCurKeyThreadSched);
            return;
        }
        pw.println("Permission Denial: can't dump hwthermal service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
    }
}
