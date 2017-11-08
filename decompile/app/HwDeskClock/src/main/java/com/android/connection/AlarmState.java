package com.android.connection;

import android.content.SharedPreferences.Editor;
import com.android.deskclock.DeskClockApplication;
import com.android.util.HwLog;
import com.android.util.Utils;

public class AlarmState {
    private static final AlarmState sInstance = new AlarmState();
    private int alarmID = -1;
    private boolean mFireType = false;
    private int state = -1;

    private AlarmState() {
    }

    public static AlarmState getInstance() {
        return sInstance;
    }

    public int getState() {
        if (this.state == -1) {
            this.state = Utils.getSharedPreferences(DeskClockApplication.getDeskClockApplication(), "alarm_state", 0).getInt("alarm_state", 0);
        }
        return this.state;
    }

    public void setState(int state) {
        if (state < 0 || state > 3) {
            HwLog.d("connection", "set a error state");
            return;
        }
        this.state = state;
        Editor editor = Utils.getSharedPreferences(DeskClockApplication.getDeskClockApplication(), "alarm_state", 0).edit();
        editor.putInt("alarm_state", state);
        editor.commit();
    }

    public int getAlramID() {
        if (this.alarmID == -1) {
            this.alarmID = Utils.getSharedPreferences(DeskClockApplication.getDeskClockApplication(), "alarm_state", 0).getInt("alarm_id", -1);
        }
        return this.alarmID;
    }

    public void setAlramID(int alramID) {
        if (alramID >= 0) {
            this.alarmID = alramID;
            Editor editor = Utils.getSharedPreferences(DeskClockApplication.getDeskClockApplication(), "alarm_state", 0).edit();
            editor.putInt("alarm_id", alramID);
            editor.commit();
        }
    }

    public boolean getFireType() {
        return this.mFireType;
    }

    public void setFireType(boolean fireType) {
        this.mFireType = fireType;
    }

    public String toString() {
        return "AlarmState [state=" + this.state + ", alarmID=" + this.alarmID + "]";
    }
}
