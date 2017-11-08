package com.huawei.powergenie.core.device;

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.ISdkService;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class SensorState {
    private final SparseArray<SensorRecord> mHistorySensorRecords = new SparseArray();
    private final ISdkService mISdkService;

    private class SensorRecord {
        private final HashMap<Integer, Integer> mHandles = new HashMap();
        private long mLastStopTime = 0;
        private long mStartTime;
        private int mUid;

        public SensorRecord(int uid, int handle) {
            this.mUid = uid;
            addSensor(handle);
        }

        public boolean hasSensor() {
            return this.mHandles.size() > 0;
        }

        public void addSensor(int handle) {
            if (!hasSensor()) {
                this.mStartTime = SystemClock.elapsedRealtime();
            }
            SensorState.this.updateAppSensorState(true, this.mUid, handle);
            Integer count = (Integer) this.mHandles.get(Integer.valueOf(handle));
            if (count == null) {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(1));
            } else {
                this.mHandles.put(Integer.valueOf(handle), Integer.valueOf(count.intValue() + 1));
            }
            Log.i("SensorState", "addSensor,mHandles:" + this.mHandles);
        }

        public void removeSensor(Integer handle) {
            Integer count = (Integer) this.mHandles.get(handle);
            if (count != null) {
                int value = count.intValue() - 1;
                if (value <= 0) {
                    this.mHandles.remove(handle);
                } else {
                    this.mHandles.put(handle, Integer.valueOf(value));
                }
            }
            Log.i("SensorState", "removeSensor,mHandles:" + this.mHandles);
            SensorState.this.updateAppSensorState(false, this.mUid, handle.intValue());
            if (!hasSensor()) {
                this.mLastStopTime = SystemClock.elapsedRealtime();
            }
        }

        public long getSensorTime(long statsStartTime) {
            if (!hasSensor()) {
                return 0;
            }
            if (this.mStartTime > statsStartTime) {
                return SystemClock.elapsedRealtime() - this.mStartTime;
            }
            return SystemClock.elapsedRealtime() - statsStartTime;
        }

        public long getStopTimeToNow() {
            return this.mLastStopTime == 0 ? -1 : SystemClock.elapsedRealtime() - this.mLastStopTime;
        }
    }

    protected SensorState(ICoreContext coreContext) {
        this.mISdkService = (ISdkService) coreContext.getService("sdk");
    }

    protected void handleSensorEvent(String strUid, String strSensor, boolean enable) {
        int uid = -1;
        int sensor = -1;
        if (strUid != null) {
            uid = Integer.parseInt(strUid);
        }
        if (strSensor != null) {
            sensor = Integer.parseInt(strSensor);
        }
        Log.i("SensorState", "sensor:" + sensor + " enable:" + enable + " uid:" + uid);
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        if (enable) {
            if (se == null) {
                this.mHistorySensorRecords.put(uid, new SensorRecord(uid, sensor));
                return;
            }
            se.addSensor(sensor);
        } else if (se != null && se.hasSensor()) {
            se.removeSensor(Integer.valueOf(sensor));
            if (!se.hasSensor()) {
            }
        } else if (se == null) {
            Log.i("SensorState", "not find sensor:" + sensor + " enable:" + enable + " uid:" + uid);
            updateAppSensorState(false, uid, sensor);
        }
    }

    protected boolean hasActiveSensor(int uid) {
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        return se != null ? se.hasSensor() : false;
    }

    protected boolean hasVaildSensor(int uid) {
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        if (se == null || se.getStopTimeToNow() >= 90000) {
            return false;
        }
        return true;
    }

    protected long getSensorStartTime(int uid) {
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        return se != null ? se.mStartTime : 0;
    }

    protected long getScrOffActiveSensorTime(int uid, long scrOffTime) {
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        return se != null ? se.getSensorTime(scrOffTime) : 0;
    }

    protected Map<String, String> getActiveSensorsByUid(int uid) {
        HashMap<Integer, Integer> sensorMap;
        SensorRecord se = (SensorRecord) this.mHistorySensorRecords.get(uid);
        if (se != null) {
            sensorMap = se.mHandles;
        } else {
            sensorMap = null;
        }
        if (sensorMap == null) {
            return null;
        }
        Map<String, String> states = new HashMap();
        for (Entry entry : sensorMap.entrySet()) {
            states.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return states;
    }

    private void updateAppSensorState(boolean sensorStart, int uid, int sensorHandle) {
        this.mISdkService.handleStateChanged(4, sensorStart ? 1 : 2, sensorHandle, null, uid);
    }
}
