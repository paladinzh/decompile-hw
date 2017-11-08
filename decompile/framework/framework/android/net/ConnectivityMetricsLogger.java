package android.net;

import android.app.PendingIntent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class ConnectivityMetricsLogger {
    public static final int COMPONENT_TAG_BLUETOOTH = 1;
    public static final int COMPONENT_TAG_CONNECTIVITY = 0;
    public static final int COMPONENT_TAG_TELECOM = 3;
    public static final int COMPONENT_TAG_TELEPHONY = 4;
    public static final int COMPONENT_TAG_WIFI = 2;
    public static final String CONNECTIVITY_METRICS_LOGGER_SERVICE = "connectivity_metrics_logger";
    public static final String DATA_KEY_EVENTS_COUNT = "count";
    private static final boolean DBG = true;
    public static final int NUMBER_OF_COMPONENTS = 5;
    private static String TAG = "ConnectivityMetricsLogger";
    public static final int TAG_SKIPPED_EVENTS = -1;
    private int mNumSkippedEvents = 0;
    private IConnectivityMetricsLogger mService = Stub.asInterface(ServiceManager.getService(CONNECTIVITY_METRICS_LOGGER_SERVICE));
    private long mServiceUnblockedTimestampMillis = 0;

    public void logEvent(long timestamp, int componentTag, int eventTag, Parcelable data) {
        if (this.mService == null) {
            Log.d(TAG, "logEvent(" + componentTag + "," + eventTag + ") Service not ready");
        } else if (this.mServiceUnblockedTimestampMillis <= 0 || System.currentTimeMillis() >= this.mServiceUnblockedTimestampMillis) {
            long result;
            ConnectivityMetricsEvent connectivityMetricsEvent = null;
            if (this.mNumSkippedEvents > 0) {
                Bundle b = new Bundle();
                b.putInt(DATA_KEY_EVENTS_COUNT, this.mNumSkippedEvents);
                connectivityMetricsEvent = new ConnectivityMetricsEvent(this.mServiceUnblockedTimestampMillis, componentTag, -1, b);
                this.mServiceUnblockedTimestampMillis = 0;
            }
            ConnectivityMetricsEvent event = new ConnectivityMetricsEvent(timestamp, componentTag, eventTag, data);
            if (connectivityMetricsEvent == null) {
                try {
                    result = this.mService.logEvent(event);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error logging event " + e.getMessage());
                }
            } else {
                result = this.mService.logEvents(new ConnectivityMetricsEvent[]{connectivityMetricsEvent, event});
            }
            if (result == 0) {
                this.mNumSkippedEvents = 0;
            } else {
                this.mNumSkippedEvents++;
                if (result > 0) {
                    this.mServiceUnblockedTimestampMillis = result;
                }
            }
        } else {
            this.mNumSkippedEvents++;
        }
    }

    public ConnectivityMetricsEvent[] getEvents(Reference reference) {
        try {
            return this.mService.getEvents(reference);
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.getEvents: " + ex);
            return null;
        }
    }

    public boolean register(PendingIntent newEventsIntent) {
        try {
            return this.mService.register(newEventsIntent);
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.register: " + ex);
            return false;
        }
    }

    public boolean unregister(PendingIntent newEventsIntent) {
        try {
            this.mService.unregister(newEventsIntent);
            return true;
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.unregister: " + ex);
            return false;
        }
    }
}
