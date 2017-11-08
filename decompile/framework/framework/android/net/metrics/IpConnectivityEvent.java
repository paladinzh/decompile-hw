package android.net.metrics;

import android.net.ConnectivityMetricsLogger;
import android.os.Parcelable;

public abstract class IpConnectivityEvent {
    private static final int COMPONENT_TAG = 0;
    private static final ConnectivityMetricsLogger sMetricsLogger = new ConnectivityMetricsLogger();

    public static <T extends IpConnectivityEvent & Parcelable> void logEvent(T event) {
        sMetricsLogger.logEvent(System.currentTimeMillis(), 0, 0, (Parcelable) event);
    }
}
