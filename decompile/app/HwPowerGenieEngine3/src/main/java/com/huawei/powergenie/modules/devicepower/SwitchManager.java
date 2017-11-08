package com.huawei.powergenie.modules.devicepower;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.policy.PolicyProvider;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.HashMap;
import java.util.Map.Entry;

public final class SwitchManager extends BaseModule {
    private static final String EXTRE_MODE_SWITCH_CONTROL = SystemProperties.get("ro.config.hw_extr_close_switch", "");
    private static int mCurNodeBrightness = -1;
    private static boolean mIsCinemaMode = false;
    private static int mMaxLCDNodeVal = -1;
    private static int mSettingLcdBrightness = -1;
    private final ContentObserver mBluetoothObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(101, 601);
        }
    };
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurChargingLevel = 0;
    private int mCurPolicy = 699;
    private int mCurWlanLevel = 0;
    private final ContentObserver mDialingSoundObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(114, 602);
        }
    };
    private long mDormancyTime = 0;
    private final ContentObserver mGpsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(103, 601);
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    Log.w("SwitchManager", "thermal: shutdown");
                    Intent shutdown = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                    shutdown.putExtra("android.intent.extra.KEY_CONFIRM", false);
                    shutdown.setFlags(268435456);
                    SwitchManager.this.mContext.startActivity(shutdown);
                    return;
                case 101:
                    SwitchManager.this.mIDeviceState.setCinemaMode(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    };
    private final ContentObserver mHapticFeedbackObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(105, 602);
        }
    };
    private IDeviceState mIDeviceState;
    private IThermal mIThermal;
    private boolean mIsCameraFront = false;
    private boolean mIsExtremeClose4G = false;
    private boolean mIsExtremeMode = false;
    private boolean mLimitFlashLowBattery = false;
    private boolean mLimitFlashThermal = false;
    private IPolicy mPolicy;
    private HashMap<Integer, Integer> mRestoreSwitchMap = new HashMap();
    private final ContentObserver mScrLockSoundObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(113, 602);
        }
    };
    private final ContentObserver mSoundEffectObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            SwitchManager.this.removeStoreSwitchState(110, 602);
        }
    };
    private SwitcherControl mSwitcherControl;
    private WifiManager mWifiManager = null;

    public void onCreate() {
        super.onCreate();
        this.mContext = getCoreContext().getContext();
        this.mPolicy = (IPolicy) getCoreContext().getService("policy");
        this.mIDeviceState = (IDeviceState) getCoreContext().getService("device");
        this.mIThermal = (IThermal) getCoreContext().getService("thermal");
        this.mContentResolver = this.mContext.getContentResolver();
        this.mSwitcherControl = new SwitcherControl(this.mContext, getCoreContext());
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        addAction(350);
        if (this.mPolicy.supportCinemaMode()) {
            addAction(246);
            addAction(247);
        }
        addAction(303);
        addAction(253);
        addAction(221);
        addAction(244);
        if (SystemProperties.getBoolean("ro.config.limit_lcd_manual", false)) {
            addAction(254);
        }
        addAction(301);
        addAction(324);
        addAction(323);
        if (!SystemProperties.getBoolean("sys.huawei.thermal.enable", false) && getCoreContext().isHisiPlatform()) {
            addAction(333);
            addAction(334);
        }
    }

    public void onStart() {
        super.onStart();
        this.mCurPolicy = powerModeToPolicy(this.mPolicy.getPowerMode());
        if (699 == this.mCurPolicy) {
            return;
        }
        if (this.mCurPolicy == 601 || this.mCurPolicy == 602) {
            this.mRestoreSwitchMap = readRestorePolicy(this.mCurPolicy);
        }
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        int settingLcd;
        switch (action.getActionId()) {
            case 221:
                this.mIsCameraFront = true;
                break;
            case 244:
                this.mIsCameraFront = false;
                break;
            case 246:
                if (isNeedEnterCinemaMode()) {
                    mIsCinemaMode = true;
                    this.mHandler.removeMessages(101);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, Boolean.valueOf(true)), 40);
                    break;
                }
                break;
            case 247:
                if (mIsCinemaMode) {
                    mIsCinemaMode = false;
                    this.mHandler.removeMessages(101);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, Boolean.valueOf(false)), 40);
                    break;
                }
                break;
            case 253:
                if (this.mIThermal != null) {
                    handleThermalEvent(action.getPkgName(), (int) action.getExtraLong());
                    break;
                }
                break;
            case 254:
                int level = (int) action.getExtraLong();
                settingLcd = this.mSwitcherControl.getLCDBrightness();
                if (level <= 0) {
                    if (mSettingLcdBrightness > 0 && mSettingLcdBrightness <= 255) {
                        if (settingLcd != 184) {
                            Log.i("SwitchManager", "user has changed the brightness:" + settingLcd);
                        } else {
                            this.mIDeviceState.setLCDBrightness(mSettingLcdBrightness);
                            Log.i("SwitchManager", "thermal:restore brightness " + mSettingLcdBrightness);
                        }
                        mSettingLcdBrightness = -1;
                        break;
                    }
                } else if (mSettingLcdBrightness == -1 && settingLcd >= 185 && getSwitcherState(108) == 0) {
                    mSettingLcdBrightness = settingLcd;
                    this.mIDeviceState.setLCDBrightness(184);
                    Log.i("SwitchManager", "thermal:set brightness 184");
                    break;
                }
            case 301:
                this.mDormancyTime = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis();
                if (this.mPolicy.getPowerMode() == 4 && this.mIDeviceState.isBootCompleted() && !this.mPolicy.isExtremeModeV2()) {
                    closeExhaustSwitch();
                    break;
                }
            case 303:
                this.mIsExtremeMode = false;
                if (mSettingLcdBrightness > 0 && mSettingLcdBrightness <= 255) {
                    settingLcd = this.mSwitcherControl.getLCDBrightness();
                    if (settingLcd != 184) {
                        Log.i("SwitchManager", "shutdown, user has changed the brightness:" + settingLcd);
                    } else {
                        this.mIDeviceState.setLCDBrightness(mSettingLcdBrightness);
                        Log.i("SwitchManager", "shutdown, set brightness " + mSettingLcdBrightness);
                    }
                    mSettingLcdBrightness = -1;
                }
                if (this.mRestoreSwitchMap.size() != 0) {
                    if (this.mPolicy.getPowerMode() == 4) {
                        controlSwitches(this.mRestoreSwitchMap);
                        this.mRestoreSwitchMap.clear();
                        break;
                    }
                    saveRestorePolicy(this.mCurPolicy, this.mRestoreSwitchMap);
                    break;
                }
                break;
            case 323:
                if (this.mPolicy.getPowerMode() != 4 && this.mIsExtremeClose4G) {
                    Log.i("SwitchManager", "Call idle restore net mode!");
                    this.mIsExtremeClose4G = this.mSwitcherControl.setNetworkMode(1);
                    break;
                }
            case 324:
                if (this.mIThermal != null) {
                    thermalCrashInitialize();
                    break;
                }
                break;
            case 333:
                setFlashSwitcher(false);
                this.mLimitFlashLowBattery = true;
                break;
            case 334:
                setFlashSwitcher(true);
                this.mLimitFlashLowBattery = false;
                break;
            case 350:
                int powerMode = action.getExtraInt();
                if (this.mPolicy.getOldPowerMode() == 1) {
                    unRegisterSuperModeObserver();
                }
                if (powerMode != 4) {
                    this.mIsExtremeMode = false;
                    processPolicyChanged(powerModeToPolicy(powerMode), this.mCurPolicy);
                    saveRestorePolicy(powerModeToPolicy(powerMode), this.mRestoreSwitchMap);
                    if (this.mPolicy.getOldPowerMode() == 4) {
                        this.mPolicy.sendExtremeMode(false);
                    }
                    if (this.mPolicy.getPowerMode() == 1) {
                        registerSuperModeObserver();
                    }
                    if (this.mPolicy.getOldPowerMode() == 4 && this.mPolicy.isSupportExtrModeV2()) {
                        unRegisterExtreModeObserver();
                        break;
                    }
                }
                this.mIsExtremeMode = true;
                processPolicyChanged(powerModeToPolicy(powerMode), this.mCurPolicy);
                saveRestorePolicy(powerModeToPolicy(powerMode), this.mRestoreSwitchMap);
                this.mPolicy.sendExtremeMode(true);
                if (this.mPolicy.isExtremeModeV2()) {
                    registerExtreModeObserver();
                    break;
                }
                break;
        }
        return true;
    }

    private void closeExhaustSwitch() {
        boolean isNeedRestoreSwitcher = false;
        if (this.mSwitcherControl.getDataServiceSwither()) {
            Log.i("SwitchManager", "extreme mode close data service at screen off");
            isNeedRestoreSwitcher = true;
            this.mRestoreSwitchMap.put(Integer.valueOf(104), Integer.valueOf(1));
            setSwitchState(104, 0);
        }
        if (this.mIDeviceState.isWiFiOn()) {
            Log.i("SwitchManager", "extreme mode close wifi at screen off");
            isNeedRestoreSwitcher = true;
            this.mRestoreSwitchMap.put(Integer.valueOf(102), Integer.valueOf(1));
            setSwitchState(102, 0);
        }
        if (this.mSwitcherControl.getBluetoothSwitcher()) {
            Log.i("SwitchManager", "extreme mode close bluetooth at screen off");
            isNeedRestoreSwitcher = true;
            this.mRestoreSwitchMap.put(Integer.valueOf(101), Integer.valueOf(1));
            setSwitchState(101, 0);
        }
        if (this.mSwitcherControl.getNetworkMode() == 1) {
            Log.i("SwitchManager", "extreme mode close 4G at screen off");
            isNeedRestoreSwitcher = true;
            this.mRestoreSwitchMap.put(Integer.valueOf(116), Integer.valueOf(1));
            setSwitchState(116, 0);
        }
        if (isNeedRestoreSwitcher) {
            saveRestorePolicy(powerModeToPolicy(4), this.mRestoreSwitchMap);
        }
    }

    private void registerSuperModeObserver() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(System.getUriFor("sound_effects_enabled"), true, this.mSoundEffectObserver, -1);
        contentResolver.registerContentObserver(System.getUriFor("lockscreen_sounds_enabled"), true, this.mScrLockSoundObserver, -1);
        contentResolver.registerContentObserver(System.getUriFor("dtmf_tone"), true, this.mDialingSoundObserver, -1);
        contentResolver.registerContentObserver(System.getUriFor("haptic_feedback_enabled"), true, this.mHapticFeedbackObserver, -1);
    }

    private void registerExtreModeObserver() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(System.getUriFor("bluetooth_on"), true, this.mBluetoothObserver, -1);
        contentResolver.registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, this.mGpsObserver, -1);
    }

    private void unRegisterSuperModeObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSoundEffectObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mScrLockSoundObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mDialingSoundObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mHapticFeedbackObserver);
    }

    private void unRegisterExtreModeObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mBluetoothObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mGpsObserver);
    }

    private void removeStoreSwitchState(int switchId, int policy) {
        if (this.mPolicy.getPowerMode() == 1 || this.mPolicy.getPowerMode() == 4) {
            Log.i("SwitchManager", "remove switch state :" + switchId);
            this.mRestoreSwitchMap.remove(Integer.valueOf(switchId));
            SharedPreferences pref = this.mContext.getSharedPreferences("switch_backup", 0);
            String[] switchpolicy = pref.getString("" + policy, "").split(";");
            StringBuilder sb = new StringBuilder();
            for (String switchState : switchpolicy) {
                String[] switchItem = switchState.split("-");
                if (switchItem.length >= 2 && !switchItem[0].equals("" + switchId)) {
                    sb.append(switchItem[0]);
                    sb.append("-");
                    sb.append(switchItem[1]);
                    sb.append(";");
                }
            }
            Editor editor = pref.edit();
            editor.putString("" + policy, sb.toString());
            editor.commit();
            return;
        }
        Log.d("SwitchManager", "Not in super or extreme mode, don't care switch id :" + switchId);
    }

    public void processPolicyChanged(int newPolicy, int oldPolicy) {
        Log.d("SwitchManager", "newPolicy:" + newPolicy + ", oldPolicy:" + oldPolicy);
        if (newPolicy == oldPolicy) {
            Log.d("SwitchManager", "policy not changes,do nothing!");
            return;
        }
        this.mCurPolicy = newPolicy;
        HashMap<Integer, Integer> backupPolicy = new HashMap();
        HashMap<Integer, Integer> switchPolicy = getPolicyFrmDB(this.mCurPolicy, backupPolicy);
        if (switchPolicy == null) {
            Log.w("SwitchManager", "getPolicyFrmDB failed!");
            return;
        }
        if (this.mRestoreSwitchMap.size() > 0) {
            for (Integer intValue : this.mRestoreSwitchMap.keySet()) {
                int restoreSwitchId = intValue.intValue();
                int restoreValue = ((Integer) this.mRestoreSwitchMap.get(Integer.valueOf(restoreSwitchId))).intValue();
                if (switchPolicy.containsKey(Integer.valueOf(restoreSwitchId))) {
                    int switchValue = ((Integer) switchPolicy.get(Integer.valueOf(restoreSwitchId))).intValue();
                    if (backupPolicy.containsKey(Integer.valueOf(restoreSwitchId))) {
                        backupPolicy.put(Integer.valueOf(restoreSwitchId), Integer.valueOf(restoreValue));
                    } else if (switchValue != restoreValue) {
                        backupPolicy.put(Integer.valueOf(restoreSwitchId), Integer.valueOf(restoreValue));
                    } else {
                        Log.i("SwitchManager", "switch value the same as restore.");
                    }
                } else {
                    switchPolicy.put(Integer.valueOf(restoreSwitchId), Integer.valueOf(restoreValue));
                }
            }
            this.mRestoreSwitchMap.clear();
        }
        controlSwitches(switchPolicy);
        if (backupPolicy.size() != 0) {
            this.mRestoreSwitchMap.putAll(backupPolicy);
        }
    }

    private void saveRestorePolicy(int policyId, HashMap<Integer, Integer> restoreMap) {
        SharedPreferences pref = this.mContext.getSharedPreferences("switch_backup", 0);
        Editor editor = pref.edit();
        if (restoreMap.size() == 0) {
            if (pref.contains("" + policyId)) {
                editor.remove("" + policyId);
                editor.commit();
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Integer intValue : restoreMap.keySet()) {
            int switchId = intValue.intValue();
            int switchValue = ((Integer) restoreMap.get(Integer.valueOf(switchId))).intValue();
            if (switchValue < 0) {
                Log.e("SwitchManager", "invalid switch value and not store switchId:" + switchId);
            } else {
                sb.append(switchId);
                sb.append("-");
                sb.append(switchValue);
                sb.append(";");
            }
        }
        editor.putString("" + policyId, sb.toString());
        editor.commit();
        Log.i("SwitchManager", "store policy id:" + policyId + " values:" + sb.toString());
    }

    private HashMap<Integer, Integer> readRestorePolicy(int policyId) {
        HashMap<Integer, Integer> powerModeRestoreMap = new HashMap();
        String tmpSwitchString = this.mContext.getSharedPreferences("switch_backup", 0).getString("" + policyId, "");
        Log.i("SwitchManager", "read policy id:" + policyId + " values:" + tmpSwitchString);
        if ("".equals(tmpSwitchString)) {
            return powerModeRestoreMap;
        }
        for (String switchState : tmpSwitchString.split(";")) {
            String[] switchItem = switchState.split("-");
            if (switchItem.length >= 2) {
                powerModeRestoreMap.put(Integer.valueOf(switchItem[0]), Integer.valueOf(switchItem[1]));
            }
        }
        return powerModeRestoreMap;
    }

    private void setSwitchState(int switchId, int switchValue) {
        if (switchId < 0) {
            Log.w("SwitchManager", "invalid switch Name in setSwitchState");
            return;
        }
        Log.d("SwitchManager", "Switch ID:" + switchId + " Switch Value:" + switchValue);
        switch (switchId) {
            case 101:
                if (!this.mIDeviceState.isBluethoothConnected() || this.mIsExtremeMode) {
                    this.mSwitcherControl.setBluetoothSwitcher(intToBoolean(switchValue));
                    break;
                }
            case 102:
                if (!this.mIDeviceState.isWiFiConnected() || this.mIsExtremeMode) {
                    this.mSwitcherControl.setWiFiSwitcher(intToBoolean(switchValue));
                    break;
                }
            case 103:
                this.mSwitcherControl.setGPSSwitcher(intToBoolean(switchValue));
                break;
            case 104:
                this.mSwitcherControl.setDataServiceEnabled(intToBoolean(switchValue));
                break;
            case 105:
                this.mSwitcherControl.setHapticFeedback(switchValue);
                break;
            case 106:
                this.mSwitcherControl.setAutoSyncSwitcher(intToBoolean(switchValue));
                break;
            case 107:
                this.mSwitcherControl.setWindowAutoRotation(switchValue);
                break;
            case 108:
                mCurNodeBrightness = this.mIDeviceState.getLCDCurNodeBrightness();
                if (1 != switchValue) {
                    this.mSwitcherControl.setBrightnessMode(0);
                    break;
                } else {
                    this.mSwitcherControl.setBrightnessMode(1);
                    break;
                }
            case 110:
                this.mSwitcherControl.setTouchSounds(switchValue);
                break;
            case 111:
                if (this.mIsExtremeMode) {
                    if (mCurNodeBrightness == -1) {
                        mCurNodeBrightness = this.mIDeviceState.getLCDCurNodeBrightness();
                        Log.i("SwitchManager", "Current brightness = " + mCurNodeBrightness);
                    }
                    if (mMaxLCDNodeVal == -1) {
                        mMaxLCDNodeVal = this.mIDeviceState.getLCDMaxNodeBrightness();
                        Log.i("SwitchManager", "Max brightness = " + mMaxLCDNodeVal);
                    }
                    if (mMaxLCDNodeVal == -1 || mCurNodeBrightness == -1) {
                        if (this.mSwitcherControl.getLCDBrightness() > switchValue) {
                            this.mIDeviceState.setLCDBrightness(switchValue);
                        }
                    } else if (mCurNodeBrightness * 255 > mMaxLCDNodeVal * switchValue) {
                        this.mIDeviceState.setLCDBrightness(switchValue);
                    } else if (mCurNodeBrightness * 255 < mMaxLCDNodeVal * 20) {
                        this.mIDeviceState.setLCDBrightness(20);
                    } else {
                        int val = (mCurNodeBrightness * 255) / mMaxLCDNodeVal;
                        if (val >= 20 && val <= 35) {
                            this.mIDeviceState.setLCDBrightness(val);
                        }
                    }
                } else {
                    this.mIDeviceState.setLCDBrightness(switchValue);
                }
                mCurNodeBrightness = -1;
                break;
            case 112:
                if (this.mIsExtremeMode) {
                    this.mSwitcherControl.setWifiApSwitcher(intToBoolean(switchValue));
                    break;
                }
                break;
            case 113:
                this.mSwitcherControl.setLockScreenSounds(switchValue);
                break;
            case 114:
                this.mSwitcherControl.setDialingSounds(switchValue);
                break;
            case 115:
                this.mSwitcherControl.setSmartBacklight(switchValue);
                break;
            case 116:
                this.mIsExtremeClose4G = this.mSwitcherControl.setNetworkMode(switchValue);
                break;
            case 117:
                this.mSwitcherControl.setMFlipSilent(intToBoolean(switchValue));
                break;
            case 118:
                this.mSwitcherControl.setNfc(intToBoolean(switchValue));
                break;
            case 119:
                this.mSwitcherControl.setSIM1RingVibrate(switchValue);
                break;
            case 120:
                this.mSwitcherControl.setSIM2RingVibrate(switchValue);
                break;
            case 201:
                if (this.mPolicy.getPowerMode() == 1 && this.mPolicy.getOldPowerMode() != 4) {
                    int scrTimeoutTime = this.mSwitcherControl.getScreenTimeout();
                    if (switchValue > scrTimeoutTime) {
                        Log.i("SwitchManager", "Current screent timeout = " + scrTimeoutTime + " value low than set value = " + switchValue);
                        break;
                    }
                }
                this.mSwitcherControl.setScreenTimeout(switchValue);
                break;
            case 202:
                float fSwitchValue = ((float) switchValue) / 1000.0f;
                this.mSwitcherControl.setWindowAnimation(0, fSwitchValue);
                this.mSwitcherControl.setWindowAnimation(1, fSwitchValue);
                break;
            case 203:
                this.mSwitcherControl.setFloatWindow(intToBoolean(switchValue));
                break;
        }
    }

    private int getSwitcherState(int switchId) {
        if (switchId < 0) {
            Log.w("SwitchManager", "invalid switch Name in setSwitchState");
            return -1;
        }
        int nResult = -1;
        switch (switchId) {
            case 101:
                nResult = booleanToint(this.mSwitcherControl.getBluetoothSwitcher());
                break;
            case 102:
                nResult = booleanToint(this.mSwitcherControl.getWiFiSwitcher());
                break;
            case 103:
                nResult = booleanToint(this.mSwitcherControl.getGPSSwitcher());
                break;
            case 104:
                nResult = booleanToint(this.mSwitcherControl.getDataServiceSwither());
                break;
            case 105:
                nResult = this.mSwitcherControl.getHapticFeedback();
                break;
            case 106:
                nResult = booleanToint(this.mSwitcherControl.getAutoSyncSwitcher());
                break;
            case 107:
                nResult = this.mSwitcherControl.getWindowAutoRotation();
                break;
            case 108:
                if (1 != this.mSwitcherControl.getBrightnessMode()) {
                    nResult = 0;
                    break;
                }
                nResult = 1;
                break;
            case 110:
                nResult = this.mSwitcherControl.getTouchSounds();
                break;
            case 111:
                nResult = this.mSwitcherControl.getLCDBrightness();
                break;
            case 112:
                nResult = booleanToint(this.mSwitcherControl.getWifiApSwitcher());
                break;
            case 113:
                nResult = this.mSwitcherControl.getLockScreenSounds();
                break;
            case 114:
                nResult = this.mSwitcherControl.getDialingSounds();
                break;
            case 115:
                nResult = this.mSwitcherControl.getSmartBacklight();
                break;
            case 116:
                nResult = this.mSwitcherControl.getNetworkMode();
                break;
            case 117:
                nResult = this.mSwitcherControl.getMFlipSilent();
                break;
            case 118:
                nResult = this.mSwitcherControl.getNfcState();
                break;
            case 119:
                nResult = this.mSwitcherControl.getSIM1RingVibrateState();
                break;
            case 120:
                nResult = this.mSwitcherControl.getSIM2RingVibrateState();
                break;
            case 201:
                nResult = this.mSwitcherControl.getScreenTimeout();
                break;
            case 202:
                nResult = (int) (this.mSwitcherControl.getWindowAnimation(0) * 1000.0f);
                break;
            case 203:
                nResult = this.mSwitcherControl.getFloatWindow();
                break;
        }
        return nResult;
    }

    private boolean intToBoolean(int value) {
        return value == 1;
    }

    private int booleanToint(boolean value) {
        return value ? 1 : 0;
    }

    private HashMap<Integer, Integer> getPolicyFrmDB(int policyId, HashMap<Integer, Integer> restorePolicy) {
        Cursor cursor = this.mContext.getContentResolver().query(PolicyProvider.SWITCHER_URI, null, "power_level = " + policyId, null, null);
        if (cursor == null) {
            Log.w("SwitchManager", "switch table is not exist.");
            return null;
        }
        HashMap<Integer, Integer> switchPolicy = new HashMap();
        while (cursor.moveToNext()) {
            try {
                int switchId = cursor.getInt(cursor.getColumnIndex("switcher_id"));
                int switchValue = cursor.getInt(cursor.getColumnIndex("switcher_value"));
                if (1 == cursor.getInt(cursor.getColumnIndex("switcher_flag"))) {
                    restorePolicy.put(Integer.valueOf(switchId), Integer.valueOf(getSwitcherState(switchId)));
                }
                switchPolicy.put(Integer.valueOf(switchId), Integer.valueOf(switchValue));
            } finally {
                cursor.close();
            }
        }
        if (this.mPolicy.isSupportExtrModeV2() && policyId == 601) {
            if (!EXTRE_MODE_SWITCH_CONTROL.contains("data")) {
                restorePolicy.remove(Integer.valueOf(104));
                switchPolicy.remove(Integer.valueOf(104));
                Log.i("SwitchManager", "Cannot control Data Service in extreme mode v2");
            }
            if (!EXTRE_MODE_SWITCH_CONTROL.contains("wifi")) {
                restorePolicy.remove(Integer.valueOf(102));
                switchPolicy.remove(Integer.valueOf(102));
                Log.i("SwitchManager", "Cannot control Wifi in extreme mode v2");
            }
            if (!EXTRE_MODE_SWITCH_CONTROL.contains("4g")) {
                restorePolicy.remove(Integer.valueOf(116));
                switchPolicy.remove(Integer.valueOf(116));
                Log.i("SwitchManager", "Cannot control 4G in extreme mode v2");
            }
        }
        return switchPolicy;
    }

    private void controlSwitches(HashMap<Integer, Integer> swichPolicy) {
        if (swichPolicy != null) {
            boolean wifiSwitchClose = false;
            for (Entry entry : ((HashMap) swichPolicy.clone()).entrySet()) {
                Integer switchId = (Integer) entry.getKey();
                Integer switchValue = (Integer) entry.getValue();
                if (switchId.intValue() == 102 && switchValue.intValue() == 0) {
                    wifiSwitchClose = true;
                } else {
                    int curSwitchValue = getSwitcherState(switchId.intValue());
                    if (curSwitchValue < 0) {
                        Log.w("SwitchManager", "invalid restore switchId:" + switchId + "  switchValue:" + curSwitchValue);
                    } else if (curSwitchValue != switchValue.intValue()) {
                        setSwitchState(switchId.intValue(), switchValue.intValue());
                    }
                }
            }
            if (wifiSwitchClose) {
                Log.i("SwitchManager", "control WIFI OFF");
                setSwitchState(102, 0);
            }
        }
    }

    private int powerModeToPolicy(int mode) {
        int switchPolicy = this.mCurPolicy;
        switch (mode) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return 602;
            case NativeAdapter.PLATFORM_HI /*2*/:
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return 699;
            case 4:
                return 601;
            default:
                return switchPolicy;
        }
    }

    private void thermalCrashInitialize() {
        Log.i("SwitchManager", "crash, thermal action initialize");
        String path = this.mIThermal.getThermalInterface("battery");
        if (path != null) {
            this.mIThermal.setChargingLimit(0, path);
            this.mCurChargingLevel = 0;
        } else {
            Log.w("SwitchManager", "not config battery path");
        }
        path = this.mIThermal.getThermalInterface("wlan");
        if (path != null) {
            this.mIThermal.setWlanLimit(0, path);
            this.mCurWlanLevel = 0;
        } else {
            Log.w("SwitchManager", "not config wlan path");
        }
        this.mLimitFlashThermal = false;
        this.mLimitFlashLowBattery = false;
        setFlashSwitcher(true);
        setFlashSwitcher(true, true);
        this.mIThermal.setCameraFps(0);
        this.mIThermal.setPAFallback(false);
    }

    private void handleThermalEvent(String event, int value) {
        boolean z = false;
        boolean enable = value != 0;
        if (event != null) {
            if ("ucurrent".equals(event)) {
                setChargeHotLimit(1, value);
            } else if ("bcurrent".equals(event)) {
                setChargeHotLimit(2, value);
            } else if ("ucurrent_aux".equals(event)) {
                setChargeHotLimit(3, value);
            } else if ("bcurrent_aux".equals(event)) {
                setChargeHotLimit(4, value);
            } else if ("direct_charger".equals(event)) {
                setChargeHotLimit(5, value);
            } else if ("battery".equals(event) || "call_battery".equals(event)) {
                path = this.mIThermal.getThermalInterface("battery");
                if (path == null || value == this.mCurChargingLevel) {
                    Log.w("SwitchManager", "not config " + event + " path");
                } else {
                    this.mIThermal.setChargingLimit(value, path);
                    this.mCurChargingLevel = value;
                }
            } else if ("wlan".equals(event)) {
                path = this.mIThermal.getThermalInterface(event);
                if (path == null || value == this.mCurWlanLevel) {
                    Log.w("SwitchManager", "not config " + event + " path");
                } else {
                    this.mIThermal.setWlanLimit(value, path);
                    this.mCurWlanLevel = value;
                }
            } else if ("camera_fps".equals(event)) {
                this.mIThermal.setCameraFps(value);
            } else if ("vr_warning_level".equals(event)) {
                this.mIThermal.sendVRWarningLevel(event, value);
            } else if ("flash".equals(event)) {
                if (!enable) {
                    z = true;
                }
                setFlashSwitcher(z);
                this.mLimitFlashThermal = enable;
            } else if ("flash_front".equals(event)) {
                if (!enable) {
                    z = true;
                }
                setFlashSwitcher(true, z);
            } else if ("paback".equals(event)) {
                this.mIThermal.setPAFallback(enable);
            } else if ("isp".equals(event)) {
                this.mIThermal.setIspLimit(value);
            } else if (enable && "camera_warning".equals(event)) {
                sendNotifyToCamera("camera_ui", "warning");
            } else if (enable && "camera_stop".equals(event)) {
                sendNotifyToCamera("camera_ui", "stop");
            } else if ("camera".equals(event) && enable && this.mIsCameraFront) {
                sendThermalBroadCast(event);
                closeCameraSwitcher();
            } else if ("wifiap".equals(event) && this.mSwitcherControl.getWifiApSwitcher()) {
                sendThermalBroadCast(event);
                r4 = this.mSwitcherControl;
                if (!enable) {
                    z = true;
                }
                r4.setWifiApSwitcher(z);
            } else if ("modem".equals(event) && this.mSwitcherControl.getDataServiceSwither()) {
                sendThermalBroadCast(event);
                r4 = this.mSwitcherControl;
                if (!enable) {
                    z = true;
                }
                r4.setDataServiceEnabled(z);
            } else if ("gps".equals(event) && this.mSwitcherControl.getGPSSwitcher()) {
                sendThermalBroadCast(event);
                r4 = this.mSwitcherControl;
                if (!enable) {
                    z = true;
                }
                r4.setGPSSwitcher(z);
            } else if ("shutdown".equals(event)) {
                sendThermalBroadCast(event);
                shutdownPhone(value);
            } else if ("wifioff".equals(event) && this.mSwitcherControl.getWiFiSwitcher()) {
                sendThermalBroadCast(event);
                r4 = this.mSwitcherControl;
                if (!enable) {
                    z = true;
                }
                r4.setWiFiSwitcher(z);
            }
        }
    }

    private void sendNotifyToCamera(String event, String flag) {
        if (this.mIThermal != null) {
            this.mIThermal.sendThermalComUIEvent(this.mContext, event, flag);
        }
    }

    private void sendThermalBroadCast(String event) {
        if (this.mIThermal != null && getCoreContext().isHisiPlatform()) {
            this.mIThermal.sendThermalUIEvent(this.mContext, event);
        }
    }

    private boolean isNeedEnterCinemaMode() {
        String frontPkg = ((IScenario) getCoreContext().getService("scenario")).getFrontPkg();
        if (frontPkg == null || frontPkg.contains("hwvplayer") || mIsCinemaMode) {
            return false;
        }
        return true;
    }

    private void setFlashSwitcher(boolean enable) {
        setFlashSwitcher(false, enable);
    }

    private void setFlashSwitcher(boolean isFront, boolean enable) {
        boolean z = true;
        boolean z2 = false;
        if (this.mIThermal != null) {
            IThermal iThermal;
            if (isFront) {
                iThermal = this.mIThermal;
                if (!enable) {
                    z2 = true;
                }
                iThermal.setFlashLimit(true, z2);
            } else {
                if (enable) {
                    if (this.mLimitFlashLowBattery && this.mLimitFlashThermal) {
                        Log.i("SwitchManager", "not limit flash, do nothing, flash limit by thermal and low battery");
                        return;
                    }
                } else if (this.mLimitFlashLowBattery || this.mLimitFlashThermal) {
                    Log.i("SwitchManager", "limit flash, do nothing, flash limit by thermal or low battery");
                    return;
                }
                if (getCoreContext().isHisiPlatform() || getCoreContext().isQcommPlatform()) {
                    iThermal = this.mIThermal;
                    if (enable) {
                        z = false;
                    }
                    iThermal.setFlashLimit(false, z);
                } else {
                    Log.e("SwitchManager", "IHardwareService->setFlashlightEnabled not implements for platform");
                }
            }
        }
    }

    private void closeCameraSwitcher() {
        if (this.mIsCameraFront) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(268435456);
            intent.addCategory("android.intent.category.HOME");
            this.mContext.startActivity(intent);
        }
    }

    private void shutdownPhone(int delay) {
        if (delay <= 100) {
            delay = 0;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), (long) delay);
    }

    private void setChargeHotLimit(int mode, int value) {
        if (this.mIThermal != null) {
            Log.i("SwitchManager", "setChargeHotLimit : mode = " + mode + " vaule = " + value);
            this.mIThermal.setChargeHotLimit(mode, value);
        }
    }
}
