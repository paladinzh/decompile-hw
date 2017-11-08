package com.huawei.powergenie.core.device;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.StateAction;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.BroadcastAdapter;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import com.huawei.powergenie.integration.adapter.HardwareAdapter;
import com.huawei.powergenie.integration.adapter.PGManagerAdapter;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DeviceStateService extends BaseService implements IDeviceState {
    private static final ComponentName mSelfProtectComponent = new ComponentName("com.huawei.powergenie", "com.huawei.powergenie.integration.eventhub.SysBroadcastReceiver");
    private final AudioManager mAudioManager;
    private AudioState mAudioState;
    private int mBatteryLevel = 0;
    private BluetoothState mBluetoothState;
    private int mChargingPlugType = 0;
    private final Context mContext;
    private final int mCrashCount;
    private final ArrayList<String> mCtrlSocketApps = new ArrayList();
    private boolean mCtsTesting = false;
    private Display mCurDisplay;
    private DeviceMonitor mDeviceMonitor;
    private DownloadState mDownloadState;
    private boolean mHasOperator = false;
    private final ICoreContext mICoreContext;
    private boolean mIsAirPlaneMode = false;
    private boolean mIsBatteryLow = false;
    private boolean mIsBluetoothShareNetwork = false;
    private boolean mIsBootCompleted = false;
    private boolean mIsCalling = false;
    private boolean mIsCharging = false;
    private boolean mIsCtrlSocket = false;
    private boolean mIsHeadsetPlug = false;
    private boolean mIsInChinaOperator = true;
    private boolean mIsMobileConnected = false;
    private boolean mIsNFCOn = false;
    private boolean mIsScreenOff = false;
    private boolean mIsShutdown = false;
    private boolean mIsTetheredMode = false;
    private boolean mIsWifiAPOn = false;
    private boolean mIsWifiConnected = false;
    private boolean mIsWifiOn = false;
    private final KeyguardManager mKeyguardManager;
    private long mScreenOffTime;
    private long mScreenOnTime;
    private long mScreenOnTotalTime;
    private SensorState mSensorState;
    private StateHandler mStateHandler;
    private final TelephonyManager mTelephonymanager;
    private int mTotalTouchCount = 0;
    private boolean mUsbConnected = false;
    private long mUserActivityTime = 0;

    private class StateHandler extends Handler {
        private StateHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 102:
                    long deltaT = System.currentTimeMillis() - DeviceStateService.this.mUserActivityTime;
                    Message msg1;
                    if (deltaT < 180000) {
                        msg1 = Message.obtain();
                        msg1.what = 102;
                        DeviceStateService.this.mStateHandler.sendMessageDelayed(msg1, 180000 - deltaT);
                        return;
                    }
                    long interval;
                    int offTimeOut = DeviceStateService.this.getScnOffTimeOut();
                    if (offTimeOut <= 180000) {
                        interval = deltaT - 180000;
                    } else {
                        interval = deltaT - ((long) offTimeOut);
                    }
                    if (interval >= 0) {
                        ((IAppManager) DeviceStateService.this.mICoreContext.getService("appmamager")).handleNonActiveTimeout();
                        return;
                    }
                    msg1 = Message.obtain();
                    msg1.what = 102;
                    DeviceStateService.this.mStateHandler.sendMessageDelayed(msg1, Math.abs(interval));
                    return;
                default:
                    return;
            }
        }
    }

    public DeviceStateService(ICoreContext context, int crashCnt) {
        this.mICoreContext = context;
        this.mContext = context.getContext();
        this.mCrashCount = crashCnt;
        this.mStateHandler = new StateHandler();
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mTelephonymanager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
    }

    public void start() {
        this.mDeviceMonitor = new DeviceMonitor(this.mContext, this.mICoreContext);
        this.mBluetoothState = new BluetoothState(this.mICoreContext);
        this.mSensorState = new SensorState(this.mICoreContext);
        this.mAudioState = new AudioState(this.mICoreContext);
        this.mDownloadState = new DownloadState(this.mICoreContext, this);
        this.mIsScreenOff = !((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
        if (this.mIsScreenOff) {
            this.mScreenOffTime = SystemClock.elapsedRealtime();
            this.mScreenOnTime = 0;
        } else {
            this.mScreenOffTime = 0;
            this.mScreenOnTime = SystemClock.elapsedRealtime();
        }
        if (this.mAudioManager != null) {
            this.mIsHeadsetPlug = this.mAudioManager.isWiredHeadsetOn();
        }
        printStates();
    }

    private void initAfterBootCompleted() {
        boolean z = true;
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connMgr != null) {
            boolean z2;
            NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
            if (activeInfo == null || !activeInfo.isConnected()) {
                this.mIsWifiConnected = false;
                this.mIsMobileConnected = false;
            } else {
                if (activeInfo.getType() == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mIsWifiConnected = z2;
                if (activeInfo.getType() == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mIsMobileConnected = z2;
            }
            if (connMgr.getTetheredIfaces() != null) {
                if (connMgr.getTetheredIfaces().length > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                this.mIsTetheredMode = z2;
            } else {
                this.mIsTetheredMode = false;
            }
        }
        WifiManager wf = (WifiManager) this.mContext.getSystemService("wifi");
        if (wf != null) {
            this.mIsWifiOn = wf.isWifiEnabled();
        }
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfc != null) {
            this.mIsNFCOn = nfc.isEnabled();
            Log.i("DeviceStateService", "NFC enable:" + this.mIsNFCOn);
        }
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
            z = false;
        }
        this.mIsAirPlaneMode = z;
        this.mIsCalling = isPhoneInCall();
        updateNetworkOperator();
    }

    public void allReady() {
        if (this.mCrashCount > 0) {
            onInputMsgEvent(new MsgEvent(324, null));
            BroadcastAdapter.queryBtActiveApps(this.mContext);
            DbgUtils.sendNotification("pg crash count: " + this.mCrashCount, "");
        }
    }

    public void onInputMsgEvent(MsgEvent evt) {
        if (updateStates(evt)) {
            trigerStateAction(evt);
        }
    }

    public void onInputHookEvent(HookEvent event) {
        if (!isSkipEvent(event.getEventId()) && !this.mDeviceMonitor.processStatsEvent(event)) {
            switch (event.getEventId()) {
                case 113:
                    trigerUserActivity(event.getTimeStamp());
                    String frontPkg = event.getPkgName();
                    if (!(this.mCtsTesting || frontPkg == null || (!frontPkg.startsWith("com.android.cts.") && !frontPkg.equals("android.tests.devicesetup") && !frontPkg.equals("com.android.compatibility.common.deviceinfo") && (!frontPkg.startsWith("com.google.android") || !frontPkg.contains(".gts"))))) {
                        this.mCtsTesting = true;
                        Log.i("DeviceStateService", "stc running.");
                        dispatchCtsStateAction(true);
                        break;
                    }
                case 126:
                    trigerUserActivity(event.getTimeStamp());
                    this.mTotalTouchCount++;
                    break;
                case 143:
                    this.mSensorState.handleSensorEvent(event.getPkgName(), event.getValue1(), true);
                    break;
                case 144:
                    this.mSensorState.handleSensorEvent(event.getPkgName(), event.getValue1(), false);
                    break;
                case 147:
                    this.mAudioState.hasAudioFocus(event);
                    break;
                case 162:
                    this.mAudioState.processAudioSessionNew(event);
                    break;
                case 163:
                    this.mAudioState.processAudioSessionRelease(event);
                    break;
                case 164:
                    this.mAudioState.processAudioSessionStart(event);
                    break;
                case 165:
                    this.mAudioState.processAudioSessionStop(event);
                    break;
                case 169:
                case 170:
                case 171:
                    this.mAudioState.updateLPAState(event);
                    break;
                case 172:
                case 173:
                    this.mBluetoothState.processBtSockect(event);
                    break;
                case 181:
                case 182:
                    this.mBluetoothState.processActiveBtApp(event);
                    break;
            }
        }
    }

    private void enableDLState() {
        this.mDownloadState.enable(!isNetworkConnected() ? this.mIsWifiAPOn : true);
    }

    public boolean isScreenOff() {
        return this.mIsScreenOff;
    }

    public boolean isDisplayOn() {
        boolean screenOn = false;
        if (this.mCurDisplay == null) {
            DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService("display");
            if (displayManager != null) {
                this.mCurDisplay = displayManager.getDisplay(0);
            }
        }
        if (this.mCurDisplay != null) {
            if (this.mCurDisplay.getState() == 2) {
                screenOn = true;
            }
            return screenOn;
        }
        Log.e("DeviceStateService", "mCurDisplay is null and using PowerManager->isScreenOn");
        return ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
    }

    public long getScrOffDuration() {
        if (this.mIsScreenOff) {
            return SystemClock.elapsedRealtime() - this.mScreenOffTime;
        }
        return 0;
    }

    public long getScrOnTotalDuration() {
        if (this.mIsScreenOff || this.mScreenOnTime <= 0) {
            return this.mScreenOnTotalTime;
        }
        return (SystemClock.elapsedRealtime() - this.mScreenOnTime) + this.mScreenOnTotalTime;
    }

    public boolean isCharging() {
        return this.mIsCharging;
    }

    public boolean isCalling() {
        return this.mIsCalling;
    }

    public boolean isBluethoothOn() {
        return this.mBluetoothState.isBluethoothOn();
    }

    public boolean isGpsOn() {
        return Secure.isLocationProviderEnabled(this.mContext.getContentResolver(), "gps");
    }

    public boolean isWiFiOn() {
        return this.mIsWifiOn;
    }

    public boolean isNFCOn() {
        return this.mIsNFCOn;
    }

    public boolean isWiFiConnected() {
        return this.mIsWifiConnected;
    }

    public void setMobileDataEnabled(boolean enable) {
        CommonAdapter.setMobileDataEnabled(this.mContext, enable);
    }

    public int getNetworkMode() {
        return CommonAdapter.getNetworkMode(this.mContext);
    }

    public void setNetworkMode(int lteMode) {
        CommonAdapter.setNetworkMode(lteMode);
    }

    public boolean isMobileConnected() {
        return this.mIsMobileConnected;
    }

    public boolean isBTShareConnected() {
        return this.mIsBluetoothShareNetwork;
    }

    public boolean isNetworkConnected() {
        return (isWiFiConnected() || isMobileConnected()) ? true : isBTShareConnected();
    }

    public boolean isTetheredMode() {
        return this.mIsTetheredMode;
    }

    public boolean is2GNetworkClass() {
        TelephonyManager telephonyManager = this.mTelephonymanager;
        if (TelephonyManager.getNetworkClass(this.mTelephonymanager.getNetworkType()) == 1) {
            return true;
        }
        return false;
    }

    public boolean isDlUploading(int uid) {
        return this.mDownloadState.isDlUploading(uid);
    }

    public boolean isBootCompleted() {
        return this.mIsBootCompleted;
    }

    public boolean isRestartAfterCrash() {
        return this.mCrashCount > 0;
    }

    public int getCrashCount() {
        return this.mCrashCount;
    }

    public boolean isBluethoothConnected() {
        return this.mBluetoothState.isBluethoothConnected();
    }

    public long getBtDisConnectedTime() {
        return this.mBluetoothState.getBtDisConnectedTime();
    }

    public boolean hasOperator() {
        return this.mHasOperator;
    }

    public boolean isChinaOperator() {
        return this.mIsInChinaOperator;
    }

    public boolean isShutdown() {
        return this.mIsShutdown;
    }

    public boolean isCtsRunning() {
        return this.mCtsTesting;
    }

    public boolean isMonkeyRunning() {
        if (ActivityManager.isUserAMonkey()) {
            return true;
        }
        return false;
    }

    public boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    public boolean isUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
    }

    public boolean isListenerNetPackets(String pkg) {
        return (this.mIsInChinaOperator && this.mIsCtrlSocket && this.mCtrlSocketApps.size() > 0 && !this.mCtrlSocketApps.contains(pkg) && isScreenOff()) ? false : true;
    }

    public int getBatteryLevel() {
        if (this.mIsScreenOff) {
            int level = CommonAdapter.getBatteryLevelFromNode();
            if (level > 0) {
                this.mBatteryLevel = level;
            }
        }
        return this.mBatteryLevel;
    }

    public int getTouchCount() {
        return this.mTotalTouchCount;
    }

    public boolean hasBluetoothConnected(String pkg, int uid, int pid) {
        return this.mBluetoothState.isActiveBtApp(pkg, uid, pid);
    }

    public boolean hasActiveGps(int uid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.hasActiveGps(uid);
        }
        return false;
    }

    public boolean hasActiveGps() {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.hasActiveGps();
        }
        return false;
    }

    public long getGpsTime() {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getGpsTime(0);
        }
        return 0;
    }

    public long getGpsTime(int uid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getGpsTime(uid);
        }
        return 0;
    }

    public long getCurrentGpsTime(int uid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getCurrentGpsTime(uid);
        }
        return 0;
    }

    public long getScrOffGpsTime(int uid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getScrOffGpsTime(uid);
        }
        return 0;
    }

    public boolean hasActiveSensor(int uid) {
        return this.mSensorState.hasActiveSensor(uid);
    }

    public boolean hasVaildSensor(int uid) {
        return this.mSensorState.hasVaildSensor(uid);
    }

    public long getScrOffActiveSensorTime(int uid, long scrOffTime) {
        return this.mSensorState.getScrOffActiveSensorTime(uid, scrOffTime);
    }

    public long getSensorStartTime(int uid) {
        return this.mSensorState.getSensorStartTime(uid);
    }

    public Map<String, String> getActiveSensorsByUid(int uid) {
        return this.mSensorState.getActiveSensorsByUid(uid);
    }

    public long getWifiScanTime() {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWifiScanTime(0);
        }
        return 0;
    }

    public long getWifiScanTime(int uid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWifiScanTime(uid);
        }
        return 0;
    }

    public long getWkTimeByUidPid(int uid, int pid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWkTimeByUidPid(uid, pid);
        }
        return 0;
    }

    public String getWkTagByUidPid(int uid, int pid) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWkTagByUidPid(uid, pid);
        }
        return "";
    }

    public Set<Integer> getWkUidsByTag(String tag) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWkUidsByTag(tag);
        }
        return null;
    }

    public Set<Integer> getWkPidsByTag(String tag) {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getWkPidsByTag(tag);
        }
        return null;
    }

    public int getLastReleaseAudioMixUid() {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getLastReleaseAudioMixUid();
        }
        return -1;
    }

    public int getLastReleaseAudioInUid() {
        if (this.mDeviceMonitor != null) {
            return this.mDeviceMonitor.getLastReleaseAudioInUid();
        }
        return -1;
    }

    public boolean isHoldWakeLockByUid(int uid, int wakeflag) {
        if (PGManagerAdapter.isHoldWakeLock(uid, wakeflag) == 1) {
            return true;
        }
        return false;
    }

    public boolean isHeadsetOn() {
        if (!this.mAudioManager.isWiredHeadsetOn() && !this.mAudioManager.isBluetoothScoOn() && !this.mAudioManager.isBluetoothA2dpOn()) {
            return false;
        }
        Log.d("DeviceStateService", "Headset is on !");
        return true;
    }

    public boolean isPlayingSound(int pid) {
        return this.mAudioState.isPlayingSound(pid);
    }

    public boolean isPlayingSoundByUid(int uid) {
        return this.mAudioState.isPlayingSoundByuid(uid);
    }

    public boolean isPlayingSound() {
        return this.mAudioState.isPlayingSound();
    }

    public long getAudioStopDeltaTime(int uid) {
        return this.mAudioState.getAudioStopDeltaTime(uid);
    }

    public boolean isAudioOut(int pid) {
        return this.mAudioState.isAudioOut(pid);
    }

    public boolean isAudioIn(int pid) {
        return this.mAudioState.isAudioIn(pid);
    }

    public boolean isKeyguardPresent() {
        return this.mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    public boolean isKeyguardSecure() {
        return this.mKeyguardManager.isKeyguardSecure();
    }

    private boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            Log.w("DeviceStateService", "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
            return false;
        }
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void updateNetworkOperator() {
        String networkOperator = TelephonyManager.getDefault().getNetworkOperator();
        Log.i("DeviceStateService", "networkOperator: " + networkOperator);
        if (networkOperator != null && networkOperator.startsWith("460")) {
            this.mIsInChinaOperator = true;
            this.mHasOperator = true;
        } else if (TextUtils.isEmpty(networkOperator) && CommonAdapter.isChinaRegion()) {
            this.mIsInChinaOperator = true;
        } else {
            this.mIsInChinaOperator = false;
        }
    }

    private boolean isSkipEvent(int eventID) {
        switch (eventID) {
            case 146:
                return true;
            case 302:
                return false;
            default:
                return this.mIsShutdown;
        }
    }

    private boolean updateStates(MsgEvent event) {
        boolean trigerAction = true;
        StateAction stAction;
        switch (event.getEventId()) {
            case 300:
                this.mIsScreenOff = false;
                this.mScreenOnTime = SystemClock.elapsedRealtime();
                if (this.mDeviceMonitor != null) {
                    this.mDeviceMonitor.handleScrState(true);
                }
                trigerUserActivity(event.getTimeStamp());
                return true;
            case 301:
                this.mIsScreenOff = true;
                this.mScreenOffTime = SystemClock.elapsedRealtime();
                if (this.mScreenOnTime > 0) {
                    this.mScreenOnTotalTime += this.mScreenOffTime - this.mScreenOnTime;
                    this.mScreenOnTime = 0;
                }
                if (this.mDeviceMonitor != null) {
                    this.mDeviceMonitor.handleScrState(false);
                }
                removeUserActivityCheck();
                return true;
            case 302:
            case 324:
                this.mIsShutdown = false;
                this.mIsBootCompleted = true;
                initAfterBootCompleted();
                enableDLState();
                return true;
            case 303:
                this.mIsShutdown = true;
                protectMyself();
                return true;
            case 304:
                this.mIsCtrlSocket = false;
                this.mCtrlSocketApps.clear();
                return true;
            case 308:
                int level = event.getIntent().getIntExtra("level", 0);
                if (this.mBatteryLevel != level) {
                    this.mBatteryLevel = level;
                } else {
                    trigerAction = false;
                }
                this.mChargingPlugType = event.getIntent().getIntExtra("plugged", 0);
                if (this.mIsCharging || this.mChargingPlugType == 0) {
                    return trigerAction;
                }
                this.mIsCharging = true;
                Log.d("DeviceStateService", "power connected battery charging plugged type: " + this.mChargingPlugType);
                stAction = StateAction.obtain();
                stAction.resetAs(310, "message", 1, event.getTimeStamp(), event.getIntent());
                notifyPowerActionChanged(this.mICoreContext, stAction);
                return trigerAction;
            case 309:
                if (event.getIntent().getExtras() != null) {
                    boolean isConnected = event.getIntent().getExtras().getBoolean("connected");
                    if (isConnected != this.mUsbConnected) {
                        this.mUsbConnected = isConnected;
                    } else {
                        trigerAction = false;
                    }
                    if (!this.mIsCharging || this.mUsbConnected) {
                        return trigerAction;
                    }
                    this.mIsCharging = false;
                    Log.d("DeviceStateService", "power disconnected after usb disconnect...");
                    stAction = StateAction.obtain();
                    stAction.resetAs(311, "message", 1, event.getTimeStamp(), event.getIntent());
                    notifyPowerActionChanged(this.mICoreContext, stAction);
                    return trigerAction;
                }
                Log.w("DeviceStateService", "USB state exception !");
                return false;
            case 310:
                this.mIsCharging = true;
                Log.i("DeviceStateService", "power connected.");
                return true;
            case 311:
                if (this.mUsbConnected) {
                    Log.i("DeviceStateService", "power disconnected not triger action.");
                    return false;
                }
                this.mIsCharging = false;
                Log.i("DeviceStateService", "power disconnected.");
                return true;
            case 312:
                NetworkInfo networkInfo = (NetworkInfo) event.getIntent().getParcelableExtra("networkInfo");
                if (networkInfo == null) {
                    return false;
                }
                boolean isNowConnected = networkInfo.isConnected();
                if (networkInfo.getType() == 0) {
                    if (isNowConnected == this.mIsMobileConnected) {
                        return false;
                    }
                    this.mIsMobileConnected = isNowConnected;
                    Log.i("DeviceStateService", "changed mobile connected : " + isNowConnected);
                    enableDLState();
                    if (!DbgUtils.DBG_TIPS || !isScreenOff()) {
                        return true;
                    }
                    DbgUtils.sendNotification("data service", this.mIsMobileConnected ? "connected" : "disconnected");
                    return true;
                } else if (networkInfo.getType() != 7 || isNowConnected == this.mIsBluetoothShareNetwork) {
                    return false;
                } else {
                    this.mIsBluetoothShareNetwork = isNowConnected;
                    Log.i("DeviceStateService", "changed bluetooth share network connected : " + isNowConnected);
                    enableDLState();
                    return true;
                }
            case 313:
                ArrayList<String> active = event.getIntent().getStringArrayListExtra("activeArray");
                boolean z = active != null && active.size() > 0;
                this.mIsTetheredMode = z;
                Log.d("DeviceStateService", "tether state changed : " + this.mIsTetheredMode);
                return true;
            case 314:
                NetworkInfo wifiInfo = (NetworkInfo) event.getIntent().getParcelableExtra("networkInfo");
                if (wifiInfo == null) {
                    return false;
                }
                boolean isWifiConnected = wifiInfo.isConnected();
                if (isWifiConnected == this.mIsWifiConnected) {
                    return false;
                }
                this.mIsWifiConnected = isWifiConnected;
                Log.i("DeviceStateService", "changed wifi connected : " + isWifiConnected);
                enableDLState();
                if (!DbgUtils.DBG_TIPS || !isScreenOff()) {
                    return true;
                }
                DbgUtils.sendNotification("wifi state", this.mIsWifiConnected ? "connected" : "disconnected");
                return true;
            case 315:
                boolean isOn = event.getIntent().getIntExtra("wifi_state", 4) == 3;
                if (isOn == this.mIsWifiOn) {
                    return false;
                }
                this.mIsWifiOn = isOn;
                return true;
            case 317:
                this.mIsAirPlaneMode = event.getIntent().getBooleanExtra("state", false);
                return true;
            case 319:
                this.mIsBatteryLow = true;
                return true;
            case 322:
                this.mIsCalling = true;
                return true;
            case 323:
                this.mIsCalling = false;
                return true;
            case 325:
                this.mBluetoothState.updateBtConnected(true);
                return true;
            case 326:
                this.mBluetoothState.updateBtConnected(false);
                return true;
            case 327:
                this.mIsBatteryLow = false;
                return true;
            case 328:
                this.mIsInChinaOperator = true;
                this.mHasOperator = true;
                return true;
            case 329:
                this.mIsInChinaOperator = false;
                this.mHasOperator = true;
                return true;
            case 335:
                this.mIsHeadsetPlug = event.getIntent().getIntExtra("state", 0) == 1;
                return true;
            case 336:
                this.mBluetoothState.updateBtState(event.getIntent().getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE));
                return true;
            case 337:
                int nfcState = event.getIntent().getIntExtra("android.nfc.extra.ADAPTER_STATE", 1);
                Log.i("DeviceStateService", "NFC state changed : " + nfcState);
                if (nfcState == 3) {
                    this.mIsNFCOn = true;
                    return true;
                } else if (nfcState != 1) {
                    return false;
                } else {
                    this.mIsNFCOn = false;
                    return true;
                }
            case 338:
                int wifiApState = event.getIntent().getIntExtra("wifi_state", 14);
                Log.i("DeviceStateService", "wifiApState=" + wifiApState);
                if (wifiApState == 13) {
                    this.mIsWifiAPOn = true;
                    enableDLState();
                    return false;
                } else if (wifiApState != 11) {
                    return false;
                } else {
                    this.mIsWifiAPOn = false;
                    enableDLState();
                    return false;
                }
            case 357:
                this.mIsCtrlSocket = true;
                if (event.getIntent().getBooleanExtra("ctrl_socket_status", true)) {
                    String applistStr = event.getIntent().getStringExtra("ctrl_socket_list");
                    this.mCtrlSocketApps.add("com.tencent.mm");
                    this.mCtrlSocketApps.add("com.tencent.mobileqq");
                    if (applistStr == null) {
                        Log.i("DeviceStateService", "no apps register socket. applistStr is null!");
                        return true;
                    }
                    String[] appsArray = applistStr.split("\t");
                    int len = appsArray.length;
                    for (int i = 0; i < len; i++) {
                        if (!this.mCtrlSocketApps.contains(appsArray[i])) {
                            this.mCtrlSocketApps.add(appsArray[i]);
                        }
                    }
                }
                Log.i("DeviceStateService", "now Ctrl Socket, white list:" + this.mCtrlSocketApps);
                return true;
            case 360:
                this.mBluetoothState.initBtActiveApps(event.getIntent());
                return false;
            case 365:
                this.mIsCtrlSocket = false;
                this.mCtrlSocketApps.clear();
                Log.i("DeviceStateService", "exit Ctrl Socket");
                return false;
            default:
                return true;
        }
    }

    private void trigerStateAction(MsgEvent evt) {
        StateAction stAction = StateActionFactory.makeAction(evt);
        if (stAction != null) {
            notifyPowerActionChanged(this.mICoreContext, stAction);
        }
    }

    private void trigerUserActivity(long time) {
        this.mUserActivityTime = time;
        if (!this.mStateHandler.hasMessages(102)) {
            Message msg = Message.obtain();
            msg.what = 102;
            this.mStateHandler.sendMessageDelayed(msg, 180000);
        }
    }

    private void removeUserActivityCheck() {
        this.mStateHandler.removeMessages(102);
    }

    private int getScnOffTimeOut() {
        ContentResolver resolver = this.mContext.getContentResolver();
        if ("hi6620sft".equals(SystemProperties.get("ro.board.platform"))) {
            return System.getIntForUser(resolver, "screen_off_timeout", 300000, -2);
        }
        return System.getIntForUser(resolver, "screen_off_timeout", 15000, -2);
    }

    private void dispatchCtsStateAction(boolean ctsStart) {
        StateAction stAction = StateAction.obtain();
        stAction.resetAs(275, 0, "stc");
        stAction.putExtra(ctsStart);
        notifyPowerActionChanged(this.mICoreContext, stAction);
    }

    private String getStates() {
        StringBuilder builder = new StringBuilder();
        builder.append("GpsOn=").append(isGpsOn());
        builder.append("  WifiOn=").append(this.mIsWifiOn);
        builder.append("  NFCOn=").append(this.mIsNFCOn);
        builder.append("  BtOn=").append(isBluethoothOn()).append("\n");
        builder.append("WifiConnected=").append(this.mIsWifiConnected);
        builder.append("  MobileConnected=").append(this.mIsMobileConnected);
        builder.append("  TetheredMode=").append(this.mIsTetheredMode).append("\n");
        builder.append("ScreenOff=").append(this.mIsScreenOff);
        builder.append("  Shutdown=").append(this.mIsShutdown);
        builder.append("  UsbConnected=").append(this.mUsbConnected).append("\n");
        builder.append("Charging=").append(this.mIsCharging);
        builder.append("  PlugType=").append(this.mChargingPlugType);
        builder.append("  BatteryLevel=").append(this.mBatteryLevel).append("\n");
        builder.append("BatteryLow=").append(this.mIsBatteryLow);
        builder.append("  BootCompleted=").append(this.mIsBootCompleted);
        builder.append("  AirPlane=").append(this.mIsAirPlaneMode).append("\n");
        builder.append("Calling=").append(this.mIsCalling);
        builder.append("  stc=").append(this.mCtsTesting);
        builder.append("  CrashCount=").append(this.mCrashCount).append("\n");
        builder.append("BluethoothConnected=").append(this.mBluetoothState.isBluethoothConnected());
        builder.append("  ChinaOperator=").append(this.mIsInChinaOperator);
        builder.append("  ScreenOffTime=").append(SystemClock.elapsedRealtime() - this.mScreenOffTime).append("\n");
        builder.append("CtrlSocket=").append(this.mIsCtrlSocket);
        builder.append("  White list=").append(this.mCtrlSocketApps);
        return builder.toString();
    }

    private void printStates() {
        Log.d("DeviceStateService", "device states:" + getStates());
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("DEVICE STATE");
        pw.println(getStates());
        this.mDownloadState.dump(pw, args);
    }

    public boolean setLCDBrightness(int lcd_value) {
        return CommonAdapter.setLCDBrightness(this.mContext, lcd_value);
    }

    public boolean setCinemaMode(boolean enable) {
        return HardwareAdapter.setCinemaMode(enable);
    }

    public int getLCDCurNodeBrightness() {
        return HardwareAdapter.getLCDCurNodeBrightness();
    }

    public int getLCDMaxNodeBrightness() {
        return HardwareAdapter.getLCDMaxNodeBrightness();
    }

    private void protectMyself() {
        PackageManager pm = this.mContext.getPackageManager();
        if (2 == pm.getComponentEnabledSetting(mSelfProtectComponent)) {
            pm.setComponentEnabledSetting(mSelfProtectComponent, 1, 1);
            Log.w("DeviceStateService", "enable myself by component!");
        }
        if (pm.getApplicationEnabledSetting("com.huawei.powergenie") == 2) {
            pm.setApplicationEnabledSetting("com.huawei.powergenie", 1, 1);
            Log.w("DeviceStateService", "enable myself by application!");
        }
    }
}
