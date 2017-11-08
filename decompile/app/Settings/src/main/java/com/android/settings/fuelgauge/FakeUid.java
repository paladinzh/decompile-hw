package com.android.settings.fuelgauge;

import android.os.BatteryStats.ControllerActivityCounter;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Pid;
import android.os.BatteryStats.Uid.Pkg;
import android.os.BatteryStats.Uid.Proc;
import android.os.BatteryStats.Uid.Sensor;
import android.os.BatteryStats.Uid.Wakelock;
import android.util.ArrayMap;
import android.util.SparseArray;

public class FakeUid extends Uid {
    private final int mUid;

    public FakeUid(int uid) {
        this.mUid = uid;
    }

    public int getUid() {
        return this.mUid;
    }

    public ArrayMap<String, ? extends Wakelock> getWakelockStats() {
        return null;
    }

    public ArrayMap<String, ? extends Timer> getSyncStats() {
        return null;
    }

    public ArrayMap<String, ? extends Timer> getJobStats() {
        return null;
    }

    public SparseArray<? extends Sensor> getSensorStats() {
        return null;
    }

    public SparseArray<? extends Pid> getPidStats() {
        return null;
    }

    public ArrayMap<String, ? extends Proc> getProcessStats() {
        return null;
    }

    public ArrayMap<String, ? extends Pkg> getPackageStats() {
        return null;
    }

    public void noteWifiRunningLocked(long elapsedRealtime) {
    }

    public void noteWifiStoppedLocked(long elapsedRealtime) {
    }

    public void noteFullWifiLockAcquiredLocked(long elapsedRealtime) {
    }

    public void noteFullWifiLockReleasedLocked(long elapsedRealtime) {
    }

    public void noteWifiScanStartedLocked(long elapsedRealtime) {
    }

    public void noteWifiScanStoppedLocked(long elapsedRealtime) {
    }

    public void noteWifiBatchedScanStartedLocked(int csph, long elapsedRealtime) {
    }

    public void noteWifiBatchedScanStoppedLocked(long elapsedRealtime) {
    }

    public void noteWifiMulticastEnabledLocked(long elapsedRealtime) {
    }

    public void noteWifiMulticastDisabledLocked(long elapsedRealtime) {
    }

    public void noteActivityResumedLocked(long elapsedRealtime) {
    }

    public void noteActivityPausedLocked(long elapsedRealtime) {
    }

    public long getWifiRunningTime(long elapsedRealtimeUs, int which) {
        return 0;
    }

    public long getFullWifiLockTime(long elapsedRealtimeUs, int which) {
        return 0;
    }

    public long getWifiScanTime(long elapsedRealtimeUs, int which) {
        return 0;
    }

    public int getWifiScanCount(int which) {
        return 0;
    }

    public long getWifiBatchedScanTime(int csphBin, long elapsedRealtimeUs, int which) {
        return 0;
    }

    public int getWifiBatchedScanCount(int csphBin, int which) {
        return 0;
    }

    public long getWifiMulticastTime(long elapsedRealtimeUs, int which) {
        return 0;
    }

    public Timer getAudioTurnedOnTimer() {
        return null;
    }

    public Timer getVideoTurnedOnTimer() {
        return null;
    }

    public Timer getFlashlightTurnedOnTimer() {
        return null;
    }

    public Timer getCameraTurnedOnTimer() {
        return null;
    }

    public Timer getForegroundActivityTimer() {
        return null;
    }

    public long getProcessStateTime(int state, long elapsedRealtimeUs, int which) {
        return 0;
    }

    public Timer getProcessStateTimer(int state) {
        return null;
    }

    public Timer getVibratorOnTimer() {
        return null;
    }

    public void noteUserActivityLocked(int type) {
    }

    public boolean hasUserActivity() {
        return false;
    }

    public int getUserActivityCount(int type, int which) {
        return 0;
    }

    public boolean hasNetworkActivity() {
        return false;
    }

    public long getNetworkActivityBytes(int type, int which) {
        return 0;
    }

    public long getNetworkActivityPackets(int type, int which) {
        return 0;
    }

    public long getMobileRadioActiveTime(int which) {
        return 0;
    }

    public int getMobileRadioActiveCount(int which) {
        return 0;
    }

    public long getUserCpuTimeUs(int which) {
        return 0;
    }

    public long getSystemCpuTimeUs(int which) {
        return 0;
    }

    public long getTimeAtCpuSpeed(int cluster, int step, int which) {
        return 0;
    }

    public long getCpuPowerMaUs(int which) {
        return 0;
    }

    public ControllerActivityCounter getWifiControllerActivity() {
        return null;
    }

    public ControllerActivityCounter getBluetoothControllerActivity() {
        return null;
    }

    public ControllerActivityCounter getModemControllerActivity() {
        return null;
    }

    public Timer getBluetoothScanTimer() {
        return null;
    }
}
