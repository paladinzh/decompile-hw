package com.android.server.location;

import android.content.Context;
import android.hardware.location.GeofenceHardwareImpl;
import android.hardware.location.GeofenceHardwareRequestParcelable;
import android.hardware.location.IFusedLocationHardware;
import android.hardware.location.IFusedLocationHardwareSink;
import android.location.FusedBatchOptions;
import android.location.IFusedGeofenceHardware;
import android.location.IFusedGeofenceHardware.Stub;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class FlpHardwareProvider {
    private static final boolean DEBUG = true;
    private static final int FIRST_VERSION_WITH_FLUSH_LOCATIONS = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE = 1;
    private static final int FLP_RESULT_ERROR = -1;
    private static final int FLP_RESULT_ID_EXISTS = -4;
    private static final int FLP_RESULT_ID_UNKNOWN = -5;
    private static final int FLP_RESULT_INSUFFICIENT_MEMORY = -2;
    private static final int FLP_RESULT_INVALID_GEOFENCE_TRANSITION = -6;
    private static final int FLP_RESULT_SUCCESS = 0;
    private static final int FLP_RESULT_TOO_MANY_GEOFENCES = -3;
    public static final String GEOFENCING = "Geofencing";
    public static final String LOCATION = "Location";
    private static final String TAG = "FlpHardwareProvider";
    private static FlpHardwareProvider sSingletonInstance = null;
    private int mBatchingCapabilities;
    private final Context mContext;
    private final IFusedGeofenceHardware mGeofenceHardwareService = new Stub() {
        public boolean isSupported() {
            return FlpHardwareProvider.this.nativeIsGeofencingSupported();
        }

        public void addGeofences(GeofenceHardwareRequestParcelable[] geofenceRequestsArray) {
            FlpHardwareProvider.this.nativeAddGeofences(geofenceRequestsArray);
        }

        public void removeGeofences(int[] geofenceIds) {
            FlpHardwareProvider.this.nativeRemoveGeofences(geofenceIds);
        }

        public void pauseMonitoringGeofence(int geofenceId) {
            FlpHardwareProvider.this.nativePauseGeofence(geofenceId);
        }

        public void resumeMonitoringGeofence(int geofenceId, int monitorTransitions) {
            FlpHardwareProvider.this.nativeResumeGeofence(geofenceId, monitorTransitions);
        }

        public void modifyGeofenceOptions(int geofenceId, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer, int sourcesToUse) {
            FlpHardwareProvider.this.nativeModifyGeofenceOption(geofenceId, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer, sourcesToUse);
        }
    };
    private GeofenceHardwareImpl mGeofenceHardwareSink = null;
    private boolean mHaveBatchingCapabilities;
    private final IFusedLocationHardware mLocationHardware = new IFusedLocationHardware.Stub() {
        public void registerSink(IFusedLocationHardwareSink eventSink) {
            synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                Log.d(FlpHardwareProvider.TAG, "registerSink from pid:" + Binder.getCallingPid());
                if (FlpHardwareProvider.this.mLocationSink != null) {
                    Log.e(FlpHardwareProvider.TAG, "Replacing an existing IFusedLocationHardware sink");
                }
                FlpHardwareProvider.this.mLocationSink = eventSink;
            }
            FlpHardwareProvider.this.maybeSendCapabilities();
        }

        public void unregisterSink(IFusedLocationHardwareSink eventSink) {
            synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                Log.d(FlpHardwareProvider.TAG, "unregisterSink from pid:" + Binder.getCallingPid());
                if (FlpHardwareProvider.this.mLocationSink == eventSink) {
                    FlpHardwareProvider.this.mLocationSink = null;
                }
            }
        }

        public int getSupportedBatchSize() {
            Log.d(FlpHardwareProvider.TAG, "getSupportedBatchSize");
            return FlpHardwareProvider.this.nativeGetBatchSize();
        }

        public void startBatching(int requestId, FusedBatchOptions options) {
            if (options == null) {
                Log.e(FlpHardwareProvider.TAG, "startBatching error, options is null!");
                return;
            }
            Log.d(FlpHardwareProvider.TAG, "startBatching from pid:" + Binder.getCallingPid() + " requestId:" + requestId + " getPeriodInNS:" + options.getPeriodInNS() + " getSourcesToUse:" + options.getSourcesToUse() + " getFlags:" + options.getFlags());
            FlpHardwareProvider.this.nativeStartBatching(requestId, options);
        }

        public void stopBatching(int requestId) {
            Log.d(FlpHardwareProvider.TAG, " stopBatching from pid:" + Binder.getCallingPid() + " requestId:" + requestId);
            FlpHardwareProvider.this.nativeStopBatching(requestId);
        }

        public void updateBatchingOptions(int requestId, FusedBatchOptions options) {
            if (options == null) {
                Log.e(FlpHardwareProvider.TAG, "updateBatchingOptions error, options is null!");
                return;
            }
            Log.d(FlpHardwareProvider.TAG, "updateBatchingOptions from pid:" + Binder.getCallingPid() + " requestId:" + requestId + " getPeriodInNS:" + options.getPeriodInNS() + " getSourcesToUse:" + options.getSourcesToUse() + " getFlags:" + options.getFlags());
            FlpHardwareProvider.this.nativeUpdateBatchingOptions(requestId, options);
        }

        public void requestBatchOfLocations(int batchSizeRequested) {
            Log.d(FlpHardwareProvider.TAG, "requestBatchOfLocations from pid:" + Binder.getCallingPid() + " batchSizeRequested:" + batchSizeRequested);
            FlpHardwareProvider.this.nativeRequestBatchedLocation(batchSizeRequested);
        }

        public void flushBatchedLocations() {
            Log.d(FlpHardwareProvider.TAG, "flushBatchedLocations from pid:" + Binder.getCallingPid());
            if (getVersion() >= 2) {
                FlpHardwareProvider.this.nativeFlushBatchedLocations();
            } else {
                Log.wtf(FlpHardwareProvider.TAG, "Tried to call flushBatchedLocations on an unsupported implementation");
            }
        }

        public boolean supportsDiagnosticDataInjection() {
            return FlpHardwareProvider.this.nativeIsDiagnosticSupported();
        }

        public void injectDiagnosticData(String data) {
            FlpHardwareProvider.this.nativeInjectDiagnosticData(data);
        }

        public boolean supportsDeviceContextInjection() {
            return FlpHardwareProvider.this.nativeIsDeviceContextSupported();
        }

        public void injectDeviceContext(int deviceEnabledContext) {
            FlpHardwareProvider.this.nativeInjectDeviceContext(deviceEnabledContext);
        }

        public int getVersion() {
            return FlpHardwareProvider.this.getVersion();
        }
    };
    private IFusedLocationHardwareSink mLocationSink = null;
    private final Object mLocationSinkLock = new Object();
    private int mVersion = 1;

    private final class NetworkLocationListener implements LocationListener {
        private NetworkLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider()) && location.hasAccuracy()) {
                FlpHardwareProvider.this.nativeInjectLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private native void nativeAddGeofences(GeofenceHardwareRequestParcelable[] geofenceHardwareRequestParcelableArr);

    private static native void nativeClassInit();

    private native void nativeCleanup();

    private native void nativeFlushBatchedLocations();

    private native int nativeGetBatchSize();

    private native void nativeInit();

    private native void nativeInjectDeviceContext(int i);

    private native void nativeInjectDiagnosticData(String str);

    private native void nativeInjectLocation(Location location);

    private native boolean nativeIsDeviceContextSupported();

    private native boolean nativeIsDiagnosticSupported();

    private native boolean nativeIsGeofencingSupported();

    private static native boolean nativeIsSupported();

    private native void nativeModifyGeofenceOption(int i, int i2, int i3, int i4, int i5, int i6);

    private native void nativePauseGeofence(int i);

    private native void nativeRemoveGeofences(int[] iArr);

    private native void nativeRequestBatchedLocation(int i);

    private native void nativeResumeGeofence(int i, int i2);

    private native void nativeStartBatching(int i, FusedBatchOptions fusedBatchOptions);

    private native void nativeStopBatching(int i);

    private native void nativeUpdateBatchingOptions(int i, FusedBatchOptions fusedBatchOptions);

    static {
        nativeClassInit();
    }

    public static FlpHardwareProvider getInstance(Context context) {
        if (sSingletonInstance == null) {
            sSingletonInstance = new FlpHardwareProvider(context);
            sSingletonInstance.nativeInit();
        }
        return sSingletonInstance;
    }

    private FlpHardwareProvider(Context context) {
        this.mContext = context;
        LocationManager manager = (LocationManager) this.mContext.getSystemService("location");
        LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, 0.0f, false);
        request.setHideFromAppOps(DEBUG);
        manager.requestLocationUpdates(request, new NetworkLocationListener(), Looper.myLooper());
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    private void onLocationReport(Location[] locations) {
        Log.d(TAG, "onLocationReport locations[] size:" + locations.length);
        for (Location location : locations) {
            location.setProvider("fused");
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onLocationAvailable(locations);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onBatchingCapabilities(int capabilities) {
        synchronized (this.mLocationSinkLock) {
            this.mHaveBatchingCapabilities = DEBUG;
            this.mBatchingCapabilities = capabilities;
        }
        maybeSendCapabilities();
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void onBatchingStatus(int status) {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onStatusChanged(status);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onBatchingStatus");
            }
        }
    }

    private int getVersion() {
        synchronized (this.mLocationSinkLock) {
            if (this.mHaveBatchingCapabilities) {
                int i = this.mVersion;
                return i;
            }
            return 1;
        }
    }

    private void setVersion(int version) {
        this.mVersion = version;
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void maybeSendCapabilities() {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
            boolean haveBatchingCapabilities = this.mHaveBatchingCapabilities;
            int batchingCapabilities = this.mBatchingCapabilities;
        }
        if (sink != null && haveBatchingCapabilities) {
            try {
                sink.onCapabilities(batchingCapabilities);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onDataReport(String data) {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        try {
            if (this.mLocationSink != null) {
                sink.onDiagnosticDataAvailable(data);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling onDiagnosticDataAvailable");
        }
    }

    private void onGeofenceTransition(int geofenceId, Location location, int transition, long timestamp, int sourcesUsed) {
        getGeofenceHardwareSink().reportGeofenceTransition(geofenceId, updateLocationInformation(location), transition, timestamp, 1, sourcesUsed);
    }

    private void onGeofenceMonitorStatus(int status, int source, Location location) {
        int monitorStatus;
        Location updatedLocation = null;
        if (location != null) {
            updatedLocation = updateLocationInformation(location);
        }
        switch (status) {
            case 1:
                monitorStatus = 1;
                break;
            case 2:
                monitorStatus = 0;
                break;
            default:
                Log.e(TAG, "Invalid FlpHal Geofence monitor status: " + status);
                monitorStatus = 1;
                break;
        }
        getGeofenceHardwareSink().reportGeofenceMonitorStatus(1, monitorStatus, updatedLocation, source);
    }

    private void onGeofenceAdd(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceAddStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceRemove(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceRemoveStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencePause(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofencePauseStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceResume(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceResumeStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencingCapabilities(int capabilities) {
        getGeofenceHardwareSink().onCapabilities(capabilities);
    }

    public IFusedLocationHardware getLocationHardware() {
        return this.mLocationHardware;
    }

    public IFusedGeofenceHardware getGeofenceHardware() {
        return this.mGeofenceHardwareService;
    }

    public void cleanup() {
        Log.i(TAG, "Calling nativeCleanup()");
        nativeCleanup();
    }

    private GeofenceHardwareImpl getGeofenceHardwareSink() {
        if (this.mGeofenceHardwareSink == null) {
            this.mGeofenceHardwareSink = GeofenceHardwareImpl.getInstance(this.mContext);
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
        return this.mGeofenceHardwareSink;
    }

    private static int translateToGeofenceHardwareStatus(int flpHalResult) {
        switch (flpHalResult) {
            case -6:
                return 4;
            case -5:
                return 3;
            case -4:
                return 2;
            case -3:
                return 1;
            case -2:
                return 6;
            case -1:
                return 5;
            case 0:
                return 0;
            default:
                Log.e(TAG, String.format("Invalid FlpHal result code: %d", new Object[]{Integer.valueOf(flpHalResult)}));
                return 5;
        }
    }

    private Location updateLocationInformation(Location location) {
        location.setProvider("fused");
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }
}
