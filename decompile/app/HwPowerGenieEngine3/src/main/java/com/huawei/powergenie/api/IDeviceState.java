package com.huawei.powergenie.api;

import java.util.Map;
import java.util.Set;

public interface IDeviceState {
    Map<String, String> getActiveSensorsByUid(int i);

    long getAudioStopDeltaTime(int i);

    int getBatteryLevel();

    long getBtDisConnectedTime();

    int getCrashCount();

    long getCurrentGpsTime(int i);

    long getGpsTime();

    long getGpsTime(int i);

    int getLCDCurNodeBrightness();

    int getLCDMaxNodeBrightness();

    int getLastReleaseAudioInUid();

    int getLastReleaseAudioMixUid();

    int getNetworkMode();

    long getScrOffActiveSensorTime(int i, long j);

    long getScrOffDuration();

    long getScrOffGpsTime(int i);

    long getScrOnTotalDuration();

    long getSensorStartTime(int i);

    int getTouchCount();

    long getWifiScanTime();

    long getWifiScanTime(int i);

    Set<Integer> getWkPidsByTag(String str);

    String getWkTagByUidPid(int i, int i2);

    long getWkTimeByUidPid(int i, int i2);

    Set<Integer> getWkUidsByTag(String str);

    boolean hasActiveGps();

    boolean hasActiveGps(int i);

    boolean hasActiveSensor(int i);

    boolean hasBluetoothConnected(String str, int i, int i2);

    boolean hasOperator();

    boolean hasVaildSensor(int i);

    boolean is2GNetworkClass();

    boolean isAudioIn(int i);

    boolean isAudioOut(int i);

    boolean isBluethoothConnected();

    boolean isBootCompleted();

    boolean isCalling();

    boolean isCharging();

    boolean isChinaOperator();

    boolean isCtsRunning();

    boolean isDeviceProvisioned();

    boolean isDisplayOn();

    boolean isDlUploading(int i);

    boolean isHeadsetOn();

    boolean isHoldWakeLockByUid(int i, int i2);

    boolean isKeyguardPresent();

    boolean isKeyguardSecure();

    boolean isListenerNetPackets(String str);

    boolean isMobileConnected();

    boolean isMonkeyRunning();

    boolean isNFCOn();

    boolean isNetworkConnected();

    boolean isPlayingSound();

    boolean isPlayingSound(int i);

    boolean isPlayingSoundByUid(int i);

    boolean isRestartAfterCrash();

    boolean isScreenOff();

    boolean isShutdown();

    boolean isTetheredMode();

    boolean isUserSetupComplete();

    boolean isWiFiConnected();

    boolean isWiFiOn();

    boolean setCinemaMode(boolean z);

    boolean setLCDBrightness(int i);

    void setMobileDataEnabled(boolean z);

    void setNetworkMode(int i);
}
