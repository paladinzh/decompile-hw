package com.android.util;

import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;

public class IMonitorWrapper {

    public static class EventStreamWrapper {
        private EventStream mEventStream;

        public EventStreamWrapper(EventStream eStream) {
            this.mEventStream = eStream;
        }

        private EventStream getEventStream() {
            return this.mEventStream;
        }

        public EventStreamWrapper setParam(short paramID, String value) {
            this.mEventStream.setParam(paramID, value);
            return this;
        }
    }

    private static EventStreamWrapper openEventStream(int eventID) {
        return new EventStreamWrapper(IMonitor.openEventStream(eventID));
    }

    private static void closeEventStream(EventStreamWrapper eStream) {
        IMonitor.closeEventStream(eStream.getEventStream());
    }

    private static boolean sendEvent(EventStreamWrapper eStream) {
        return IMonitor.sendEvent(eStream.getEventStream());
    }

    public static void reportNormalAlarmAlertEventFailed(String apkVer, int bugType, boolean isAlertInSilent, long time) {
        EventStreamWrapper streamWrapper = openEventStream(907061001);
        if (streamWrapper.getEventStream() != null) {
            if (!TextUtils.isEmpty(apkVer)) {
                streamWrapper.setParam((short) 0, " apkVer = " + apkVer + " ");
            }
            streamWrapper.setParam((short) 1, " bugType = " + bugType + " ");
            streamWrapper.setParam((short) 2, " isAlertInSilent = " + isAlertInSilent + " ");
            streamWrapper.setParam((short) 3, " time = " + time + " ");
            sendEvent(streamWrapper);
            closeEventStream(streamWrapper);
        }
    }

    public static void reportPowerOffAlarmAlertEventFailed(String apkVer, int bugType, boolean isAlertInSilent, long time, boolean powerOnReasonForAlarm) {
        EventStreamWrapper streamWrapper = openEventStream(907061002);
        if (streamWrapper.getEventStream() != null) {
            if (!TextUtils.isEmpty(apkVer)) {
                streamWrapper.setParam((short) 0, " apkVer = " + apkVer + " ");
            }
            streamWrapper.setParam((short) 1, " bugType = " + bugType + " ");
            streamWrapper.setParam((short) 2, " isAlertInSilent = " + isAlertInSilent + " ");
            streamWrapper.setParam((short) 3, " time = " + time + " ");
            streamWrapper.setParam((short) 4, " powerOnReason = " + powerOnReasonForAlarm + " ");
            sendEvent(streamWrapper);
            closeEventStream(streamWrapper);
        }
    }
}
