package com.android.server;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.hsm.HwSystemManager;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.IFusedGeofenceHardware;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationListener;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.util.ArrayUtils;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.FusedProxy;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssLocationProvider.GnssSystemInfoProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.IHwGpsActionReporter;
import com.android.server.location.IHwGpsLogServices;
import com.android.server.location.IHwLocalLocationProvider;
import com.android.server.location.IHwLocationProviderInterface;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.LocationRequestStatistics.PackageProviderKey;
import com.android.server.location.LocationRequestStatistics.PackageStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.PassiveProvider;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class LocationManagerService extends AbsLocationManagerService {
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS = "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS";
    private static final String ACCESS_MOCK_LOCATION = "android.permission.ACCESS_MOCK_LOCATION";
    public static final boolean D = false;
    private static final LocationRequest DEFAULT_LOCATION_REQUEST = new LocationRequest();
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    private static final long HIGH_POWER_INTERVAL_MS = 300000;
    private static final String INSTALL_LOCATION_PROVIDER = "android.permission.INSTALL_LOCATION_PROVIDER";
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;
    private static final int MSG_LOCATION_CHANGED = 1;
    private static final int MSG_LOCATION_REMOVE = 3;
    private static final int MSG_LOCATION_REQUEST = 2;
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    private static final int RESOLUTION_LEVEL_FINE = 2;
    private static final int RESOLUTION_LEVEL_NONE = 0;
    private static final String TAG = "LocationManagerService";
    private static final String WAKELOCK_KEY = "LocationManagerService";
    private final AppOpsManager mAppOps;
    private LocationBlacklist mBlacklist;
    protected final Context mContext;
    private int mCurrentUserId = 0;
    private int[] mCurrentUserProfiles = new int[]{0};
    private final Set<String> mDisabledProviders = new HashSet();
    private final Set<String> mEnabledProviders = new HashSet();
    private GeocoderProxy mGeocodeProvider;
    private GeofenceManager mGeofenceManager;
    private GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private IGnssStatusProvider mGnssStatusProvider;
    private GnssSystemInfoProvider mGnssSystemInfoProvider;
    private IGpsGeofenceHardware mGpsGeofenceProxy;
    private IHwGpsActionReporter mHwGpsActionReporter;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    private final HashMap<String, Location> mLastLocation = new HashMap();
    private final HashMap<String, Location> mLastLocationCoarseInterval = new HashMap();
    protected IHwLocalLocationProvider mLocalLocationProvider;
    private LocationFudger mLocationFudger;
    private LocationWorkerHandler mLocationHandler;
    HandlerThread mLocationThread;
    private final Object mLock = new Object();
    private final HashMap<String, MockProvider> mMockProviders = new HashMap();
    private INetInitiatedListener mNetInitiatedListener;
    private PackageManager mPackageManager;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onPackageDisappeared(String packageName, int reason) {
            Throwable th;
            synchronized (LocationManagerService.this.mLock) {
                try {
                    ArrayList<Receiver> deadReceivers = null;
                    for (Receiver receiver : LocationManagerService.this.mReceivers.values()) {
                        ArrayList<Receiver> deadReceivers2;
                        try {
                            if (receiver.mPackageName.equals(packageName)) {
                                if (deadReceivers == null) {
                                    deadReceivers2 = new ArrayList();
                                } else {
                                    deadReceivers2 = deadReceivers;
                                }
                                deadReceivers2.add(receiver);
                            } else {
                                deadReceivers2 = deadReceivers;
                            }
                            deadReceivers = deadReceivers2;
                        } catch (Throwable th2) {
                            th = th2;
                            deadReceivers2 = deadReceivers;
                        }
                    }
                    if (deadReceivers != null) {
                        for (Receiver receiver2 : deadReceivers) {
                            LocationManagerService.this.removeUpdatesLocked(receiver2);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            throw th;
        }
    };
    private PassiveProvider mPassiveProvider;
    private PowerManager mPowerManager;
    private final ArrayList<LocationProviderInterface> mProviders = new ArrayList();
    private final HashMap<String, LocationProviderInterface> mProvidersByName = new HashMap();
    private final ArrayList<LocationProviderProxy> mProxyProviders = new ArrayList();
    private final HashMap<String, LocationProviderInterface> mRealProviders = new HashMap();
    private final HashMap<Object, Receiver> mReceivers = new HashMap();
    private final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider = new HashMap();
    private final LocationRequestStatistics mRequestStatistics = new LocationRequestStatistics();
    private UserManager mUserManager;

    private class LocationWorkerHandler extends Handler {
        public LocationWorkerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    LocationManagerService locationManagerService = LocationManagerService.this;
                    Location location = (Location) msg.obj;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    locationManagerService.handleLocationChanged(location, z);
                    return;
                case 2:
                    LocationManagerService.this.mHwGpsActionReporter.uploadLocationAction(1, (String) msg.obj);
                    return;
                case 3:
                    LocationManagerService.this.mHwGpsActionReporter.uploadLocationAction(0, (String) msg.obj);
                    return;
                default:
                    Log.e("LocationManagerService", "receive unexpected message");
                    return;
            }
        }
    }

    public class Receiver implements DeathRecipient, OnFinished {
        final int mAllowedResolutionLevel;
        final boolean mHideFromAppOps;
        final Object mKey;
        final ILocationListener mListener;
        boolean mOpHighPowerMonitoring;
        boolean mOpMonitoring;
        final String mPackageName;
        int mPendingBroadcasts;
        final PendingIntent mPendingIntent;
        final int mPid;
        final int mUid;
        final HashMap<String, UpdateRecord> mUpdateRecords = new HashMap();
        WakeLock mWakeLock;
        final WorkSource mWorkSource;

        Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
            this.mListener = listener;
            this.mPendingIntent = intent;
            if (listener != null) {
                this.mKey = listener.asBinder();
            } else {
                this.mKey = intent;
            }
            this.mAllowedResolutionLevel = LocationManagerService.this.getAllowedResolutionLevel(pid, uid);
            this.mUid = uid;
            this.mPid = pid;
            this.mPackageName = packageName;
            if (workSource != null && workSource.size() <= 0) {
                workSource = null;
            }
            this.mWorkSource = workSource;
            this.mHideFromAppOps = hideFromAppOps;
            updateMonitoring(true);
            this.mWakeLock = LocationManagerService.this.mPowerManager.newWakeLock(1, "LocationManagerService");
            if (workSource == null) {
                workSource = new WorkSource(this.mUid, this.mPackageName);
            }
            this.mWakeLock.setWorkSource(workSource);
        }

        public boolean equals(Object otherObj) {
            if (otherObj instanceof Receiver) {
                return this.mKey.equals(((Receiver) otherObj).mKey);
            }
            return false;
        }

        public int hashCode() {
            return this.mKey.hashCode();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reciever[");
            s.append(Integer.toHexString(System.identityHashCode(this)));
            if (this.mListener != null) {
                s.append(" listener");
            } else {
                s.append(" intent");
            }
            for (String p : this.mUpdateRecords.keySet()) {
                s.append(" ").append(((UpdateRecord) this.mUpdateRecords.get(p)).toString());
            }
            s.append("]");
            return s.toString();
        }

        public void updateMonitoring(boolean allow) {
            if (!this.mHideFromAppOps) {
                boolean requestingLocation = false;
                boolean requestingHighPowerLocation = false;
                if (allow) {
                    for (UpdateRecord updateRecord : this.mUpdateRecords.values()) {
                        if (LocationManagerService.this.isAllowedByCurrentUserSettingsLocked(updateRecord.mProvider)) {
                            ProviderProperties properties;
                            requestingLocation = true;
                            LocationProviderInterface locationProvider = (LocationProviderInterface) LocationManagerService.this.mProvidersByName.get(updateRecord.mProvider);
                            if (locationProvider != null) {
                                properties = locationProvider.getProperties();
                            } else {
                                properties = null;
                            }
                            if (properties != null && properties.mPowerRequirement == 3 && updateRecord.mRequest.getInterval() < LocationManagerService.HIGH_POWER_INTERVAL_MS) {
                                requestingHighPowerLocation = true;
                                break;
                            }
                        }
                    }
                }
                boolean wasOpMonitoring = this.mOpMonitoring;
                this.mOpMonitoring = updateMonitoring(requestingLocation, this.mOpMonitoring, 41);
                if (this.mOpMonitoring != wasOpMonitoring) {
                    LocationManagerService.this.hwSendLocationChangedAction(LocationManagerService.this.mContext, this.mPackageName);
                }
                boolean wasHighPowerMonitoring = this.mOpHighPowerMonitoring;
                this.mOpHighPowerMonitoring = updateMonitoring(requestingHighPowerLocation, this.mOpHighPowerMonitoring, 42);
                if (this.mOpHighPowerMonitoring != wasHighPowerMonitoring) {
                    Intent intent = new Intent("android.location.HIGH_POWER_REQUEST_CHANGE");
                    intent.putExtra("isFrameworkBroadcast", "true");
                    LocationManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
            }
        }

        private boolean updateMonitoring(boolean allowMonitoring, boolean currentlyMonitoring, int op) {
            boolean z = false;
            if (currentlyMonitoring) {
                if (!(allowMonitoring && LocationManagerService.this.mAppOps.checkOpNoThrow(op, this.mUid, this.mPackageName) == 0)) {
                    LocationManagerService.this.mAppOps.finishOp(op, this.mUid, this.mPackageName);
                    return false;
                }
            } else if (allowMonitoring) {
                if (LocationManagerService.this.mAppOps.startOpNoThrow(op, this.mUid, this.mPackageName) == 0) {
                    z = true;
                }
                return z;
            }
            return currentlyMonitoring;
        }

        public boolean isListener() {
            return this.mListener != null;
        }

        public boolean isPendingIntent() {
            return this.mPendingIntent != null;
        }

        public ILocationListener getListener() {
            if (this.mListener != null) {
                return this.mListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                            return true;
                        }
                        this.mListener.onStatusChanged(provider, status, extras);
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent statusChanged = new Intent();
            statusChanged.putExtras(new Bundle(extras));
            statusChanged.putExtra("status", status);
            try {
                synchronized (this) {
                    if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                        return true;
                    }
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, statusChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public boolean callLocationChangedLocked(Location location) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                            return true;
                        }
                        this.mListener.onLocationChanged(new Location(location));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent locationChanged = new Intent();
            locationChanged.putExtra("location", new Location(location));
            try {
                synchronized (this) {
                    if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                        return true;
                    }
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, locationChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public boolean callProviderEnabledLocked(String provider, boolean enabled) {
            updateMonitoring(true);
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                            return true;
                        }
                        if (enabled) {
                            this.mListener.onProviderEnabled(provider);
                        } else {
                            this.mListener.onProviderDisabled(provider);
                        }
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent providerIntent = new Intent();
            providerIntent.putExtra("providerEnabled", enabled);
            try {
                synchronized (this) {
                    if (LocationManagerService.this.isFreeze(this.mPackageName)) {
                        return true;
                    }
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, providerIntent, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public void binderDied() {
            Log.i("LocationManagerService", "Location listener died");
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.removeUpdatesLocked(this);
            }
            synchronized (this) {
                clearPendingBroadcastsLocked();
            }
        }

        public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (this) {
                decrementPendingBroadcastsLocked();
            }
        }

        private void incrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts;
            this.mPendingBroadcasts = i + 1;
            if (i == 0) {
                this.mWakeLock.acquire();
            }
        }

        private void decrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts - 1;
            this.mPendingBroadcasts = i;
            if (i == 0 && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (this.mPendingBroadcasts > 0) {
                this.mPendingBroadcasts = 0;
                if (this.mWakeLock.isHeld()) {
                    this.mWakeLock.release();
                }
            }
        }
    }

    public class UpdateRecord {
        Location mLastFixBroadcast;
        long mLastStatusBroadcast;
        final String mProvider;
        final Receiver mReceiver;
        final LocationRequest mRequest;

        UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            this.mProvider = provider;
            this.mRequest = request;
            this.mReceiver = receiver;
            ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(provider);
            if (records == null) {
                records = new ArrayList();
                LocationManagerService.this.mRecordsByProvider.put(provider, records);
            }
            if (!records.contains(this)) {
                records.add(this);
            }
            LocationManagerService.this.mRequestStatistics.startRequesting(this.mReceiver.mPackageName, provider, request.getInterval());
        }

        void disposeLocked(boolean removeReceiver) {
            LocationManagerService.this.mRequestStatistics.stopRequesting(this.mReceiver.mPackageName, this.mProvider);
            ArrayList<UpdateRecord> globalRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
            }
            if (removeReceiver) {
                HashMap<String, UpdateRecord> receiverRecords = this.mReceiver.mUpdateRecords;
                if (receiverRecords != null) {
                    receiverRecords.remove(this.mProvider);
                    if (removeReceiver && receiverRecords.size() == 0) {
                        LocationManagerService.this.removeUpdatesLocked(this.mReceiver);
                    }
                }
            }
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("UpdateRecord[");
            s.append(this.mProvider);
            s.append(' ').append(this.mReceiver.mPackageName).append('(');
            s.append(this.mReceiver.mUid).append(')');
            s.append(' ').append(this.mRequest);
            s.append(']');
            return s.toString();
        }
    }

    public LocationManagerService(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setLocationPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                return LocationManagerService.this.mContext.getResources().getStringArray(17236013);
            }
        });
        Log.i("LocationManagerService", "Constructed");
    }

    public void systemRunning() {
        synchronized (this.mLock) {
            Log.i("LocationManagerService", "systemReady()");
            this.mPackageManager = this.mContext.getPackageManager();
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mLocationThread = new HandlerThread("LocationThread");
            this.mLocationThread.start();
            this.mLocationHandler = new LocationWorkerHandler(this.mLocationThread.getLooper());
            this.mLocationFudger = new LocationFudger(this.mContext, this.mLocationHandler);
            this.mBlacklist = new LocationBlacklist(this.mContext, this.mLocationHandler);
            this.mBlacklist.init();
            this.mGeofenceManager = new GeofenceManager(this.mContext, this.mBlacklist);
            this.mAppOps.startWatchingMode(0, null, new OnOpChangedInternalListener() {
                public void onOpChanged(int op, String packageName) {
                    synchronized (LocationManagerService.this.mLock) {
                        for (Receiver receiver : LocationManagerService.this.mReceivers.values()) {
                            receiver.updateMonitoring(true);
                        }
                        LocationManagerService.this.applyAllProviderRequirementsLocked();
                    }
                }
            });
            this.mPackageManager.addOnPermissionsChangeListener(new OnPermissionsChangedListener() {
                public void onPermissionsChanged(int uid) {
                    synchronized (LocationManagerService.this.mLock) {
                        LocationManagerService.this.applyAllProviderRequirementsLocked();
                    }
                }
            });
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            updateUserProfiles(this.mCurrentUserId);
            HwServiceFactory.getHwNLPManager().setLocationManagerService(this, this.mContext);
            HwServiceFactory.getHwNLPManager().setHwMultiNlpPolicy(this.mContext);
            initHwLocationPowerTracker(this.mContext);
            this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
            this.mLocationHandler.post(new Runnable() {
                public void run() {
                    synchronized (LocationManagerService.this.mLock) {
                        LocationManagerService.this.loadProvidersLocked();
                        LocationManagerService.this.updateProvidersLocked();
                    }
                }
            });
        }
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mLocationHandler) {
            public void onChange(boolean selfChange) {
                if (LocationManagerService.this.isGPSDisabled()) {
                    Log.d("LocationManagerService", "gps is disabled by dpm .");
                }
                synchronized (LocationManagerService.this.mLock) {
                    Log.d("LocationManagerService", "LOCATION_PROVIDERS_ALLOWED onchange");
                    LocationManagerService.this.updateProvidersLocked();
                }
            }
        }, -1);
        hwQuickGpsSwitch();
        this.mPackageMonitor.register(this.mContext, this.mLocationHandler.getLooper(), true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    LocationManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action) || "android.intent.action.MANAGED_PROFILE_REMOVED".equals(action)) {
                    LocationManagerService.this.updateUserProfiles(LocationManagerService.this.mCurrentUserId);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    Boolean userSpaceOnly = Boolean.valueOf(intent.getBooleanExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", false));
                    Log.i("LocationManagerService", "userSpaceOnly " + userSpaceOnly);
                    if (!userSpaceOnly.booleanValue()) {
                        LocationManagerService.this.shutdownComponents();
                    }
                } else if ("android.intent.action.USER_ADDED".equals(action) || "android.intent.action.USER_REMOVED".equals(action)) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userId != LocationManagerService.this.mCurrentUserId) {
                        UserInfo ui = LocationManagerService.this.mUserManager.getUserInfo(userId);
                        if (ui != null && ui.profileGroupId == LocationManagerService.this.mCurrentUserId) {
                            LocationManagerService.this.updateUserProfiles(LocationManagerService.this.mCurrentUserId);
                            Log.i("LocationManagerService", "onReceive action:" + action + ", userId:" + userId + ", updateUserProfiles for currentUserId:" + LocationManagerService.this.mCurrentUserId);
                        }
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mLocationHandler);
    }

    private void shutdownComponents() {
        LocationProviderInterface gpsProvider = (LocationProviderInterface) this.mProvidersByName.get("gps");
        if (gpsProvider != null && gpsProvider.isEnabled()) {
            gpsProvider.disable();
        }
        if (FlpHardwareProvider.isSupported()) {
            FlpHardwareProvider.getInstance(this.mContext).cleanup();
        }
    }

    void updateUserProfiles(int currentUserId) {
        int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(currentUserId);
        synchronized (this.mLock) {
            this.mCurrentUserProfiles = profileIds;
        }
    }

    private boolean isCurrentProfile(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mCurrentUserProfiles, userId);
        }
        return contains;
    }

    private void ensureFallbackFusedProviderPresentLocked(ArrayList<String> pkgs) {
        PackageManager pm = this.mContext.getPackageManager();
        String systemPackageName = this.mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(this.mContext, pkgs);
        for (ResolveInfo rInfo : pm.queryIntentServicesAsUser(new Intent(FUSED_LOCATION_SERVICE_ACTION), 128, this.mCurrentUserId)) {
            String packageName = rInfo.serviceInfo.packageName;
            try {
                if (!ServiceWatcher.isSignatureMatch(pm.getPackageInfo(packageName, 64).signatures, sigSets)) {
                    Log.w("LocationManagerService", packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION + ", but has wrong signature, ignoring");
                } else if (rInfo.serviceInfo.metaData == null) {
                    Log.w("LocationManagerService", "Found fused provider without metadata: " + packageName);
                } else if (rInfo.serviceInfo.metaData.getInt(ServiceWatcher.EXTRA_SERVICE_VERSION, -1) != 0) {
                    Log.i("LocationManagerService", "Fallback candidate not version 0: " + packageName);
                } else if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                    Log.i("LocationManagerService", "Fallback candidate not in /system: " + packageName);
                } else if (pm.checkSignatures(systemPackageName, packageName) != 0) {
                    Log.i("LocationManagerService", "Fallback candidate not signed the same as system: " + packageName);
                } else {
                    Log.i("LocationManagerService", "Found fallback provider: " + packageName);
                    return;
                }
            } catch (NameNotFoundException e) {
                Log.e("LocationManagerService", "missing package: " + packageName);
            }
        }
        throw new IllegalStateException("Unable to find a fused location provider that is in the system partition with version 0 and signed with the platform certificate. Such a package is needed to provide a default fused location provider in the event that no other fused location provider has been installed or is currently available. For example, coreOnly boot mode when decrypting the data partition. The fallback must also be marked coreApp=\"true\" in the manifest");
    }

    private void loadProvidersLocked() {
        FlpHardwareProvider flpHardwareProvider;
        IFusedGeofenceHardware geofenceHardware;
        PassiveProvider passiveProvider = new PassiveProvider(this);
        addProviderLocked(passiveProvider);
        this.mEnabledProviders.add(passiveProvider.getName());
        this.mPassiveProvider = passiveProvider;
        GnssLocationProvider gnssProvider = HwServiceFactory.createHwGnssLocationProvider(this.mContext, this, this.mLocationHandler.getLooper());
        if (GnssLocationProvider.isSupported()) {
            this.mGnssSystemInfoProvider = gnssProvider.getGnssSystemInfoProvider();
            this.mGnssStatusProvider = gnssProvider.getGnssStatusProvider();
            this.mNetInitiatedListener = gnssProvider.getNetInitiatedListener();
            addProviderLocked(gnssProvider);
            this.mRealProviders.put("gps", gnssProvider);
            this.mGnssMeasurementsProvider = gnssProvider.getGnssMeasurementsProvider();
            this.mGnssNavigationMessageProvider = gnssProvider.getGnssNavigationMessageProvider();
            this.mGpsGeofenceProxy = gnssProvider.getGpsGeofenceProxy();
        }
        Resources resources = this.mContext.getResources();
        ArrayList<String> providerPackageNames = new ArrayList();
        String[] pkgs = resources.getStringArray(17236013);
        Log.i("LocationManagerService", "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        if (pkgs != null) {
            providerPackageNames.addAll(Arrays.asList(pkgs));
        }
        ensureFallbackFusedProviderPresentLocked(providerPackageNames);
        LocationProviderProxy networkProvider = HwServiceFactory.locationProviderProxyCreateAndBind(this.mContext, "network", NETWORK_LOCATION_SERVICE_ACTION, 17956945, 17039423, 17236013, this.mLocationHandler);
        if (networkProvider != null) {
            this.mRealProviders.put("network", networkProvider);
            this.mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w("LocationManagerService", "no network location provider found");
        }
        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(this.mContext, "fused", FUSED_LOCATION_SERVICE_ACTION, 17956946, 17039424, 17236013, this.mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            this.mProxyProviders.add(fusedLocationProvider);
            this.mEnabledProviders.add(fusedLocationProvider.getName());
            this.mRealProviders.put("fused", fusedLocationProvider);
        } else {
            Slog.e("LocationManagerService", "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        this.mGeocodeProvider = HwServiceFactory.geocoderProxyCreateAndBind(this.mContext, 17956948, 17039426, 17236013, this.mLocationHandler);
        if (this.mGeocodeProvider == null) {
            Slog.e("LocationManagerService", "no geocoder provider found");
        }
        checkGeoFencerEnabled(this.mPackageManager);
        if (FlpHardwareProvider.isSupported()) {
            flpHardwareProvider = FlpHardwareProvider.getInstance(this.mContext);
            if (FusedProxy.createAndBind(this.mContext, this.mLocationHandler, flpHardwareProvider.getLocationHardware(), 17956947, 17039425, 17236013) == null) {
                Slog.d("LocationManagerService", "Unable to bind FusedProxy.");
            }
        } else {
            flpHardwareProvider = null;
            Slog.d("LocationManagerService", "FLP HAL not supported");
        }
        Context context = this.mContext;
        Handler handler = this.mLocationHandler;
        IGpsGeofenceHardware iGpsGeofenceHardware = this.mGpsGeofenceProxy;
        if (flpHardwareProvider != null) {
            geofenceHardware = flpHardwareProvider.getGeofenceHardware();
        } else {
            geofenceHardware = null;
        }
        if (GeofenceProxy.createAndBind(context, 17956949, 17039427, 17236013, handler, iGpsGeofenceHardware, geofenceHardware) == null) {
            Slog.d("LocationManagerService", "Unable to bind FLP Geofence proxy.");
        }
        this.mLocalLocationProvider = HwServiceFactory.getHwLocalLocationProvider(this.mContext, this);
        enableLocalLocationProviders(gnssProvider);
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        ActivityRecognitionHardware activityRecognitionHardware = null;
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(this.mContext);
        } else {
            Slog.d("LocationManagerService", "Hardware Activity-Recognition not supported.");
        }
        if (ActivityRecognitionProxy.createAndBind(this.mContext, this.mLocationHandler, activityRecognitionHardwareIsSupported, activityRecognitionHardware, 17956950, 17039428, 17236013) == null) {
            Slog.d("LocationManagerService", "Unable to bind ActivityRecognitionProxy.");
        }
        for (String split : resources.getStringArray(17236014)) {
            String[] fragments = split.split(",");
            String name = fragments[0].trim();
            if (this.mProvidersByName.get(name) != null) {
                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
            }
            addTestProviderLocked(name, new ProviderProperties(Boolean.parseBoolean(fragments[1]), Boolean.parseBoolean(fragments[2]), Boolean.parseBoolean(fragments[3]), Boolean.parseBoolean(fragments[4]), Boolean.parseBoolean(fragments[5]), Boolean.parseBoolean(fragments[6]), Boolean.parseBoolean(fragments[7]), Integer.parseInt(fragments[8]), Integer.parseInt(fragments[9])));
        }
        this.mHwGpsActionReporter = HwServiceFactory.getHwGpsActionReporter(this.mContext, this);
    }

    private void switchUser(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mBlacklist.switchUser(userId);
            this.mLocationHandler.removeMessages(1);
            this.mLocationHandler.removeMessages(2);
            this.mLocationHandler.removeMessages(3);
            synchronized (this.mLock) {
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                for (LocationProviderInterface p : this.mProviders) {
                    updateProviderListenersLocked(p.getName(), false);
                }
                this.mCurrentUserId = userId;
                updateUserProfiles(userId);
                updateProvidersLocked();
            }
        }
    }

    public void locationCallbackFinished(ILocationListener listener) {
        synchronized (this.mLock) {
            Receiver receiver = (Receiver) this.mReceivers.get(listener.asBinder());
            if (receiver != null) {
                synchronized (receiver) {
                    long identity = Binder.clearCallingIdentity();
                    receiver.decrementPendingBroadcastsLocked();
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public int getGnssYearOfHardware() {
        if (this.mGnssNavigationMessageProvider != null) {
            return this.mGnssSystemInfoProvider.getGnssYearOfHardware();
        }
        return 0;
    }

    private void addProviderLocked(LocationProviderInterface provider) {
        this.mProviders.add(provider);
        this.mProvidersByName.put(provider.getName(), provider);
    }

    private void removeProviderLocked(LocationProviderInterface provider) {
        provider.disable();
        this.mProviders.remove(provider);
        this.mProvidersByName.remove(provider.getName());
    }

    private boolean isAllowedByCurrentUserSettingsLocked(String provider) {
        if (this.mEnabledProviders.contains(provider)) {
            return true;
        }
        if (this.mDisabledProviders.contains(provider)) {
            return false;
        }
        return Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), provider, this.mCurrentUserId);
    }

    private boolean isAllowedByUserSettingsLocked(String provider, int uid) {
        if (isCurrentProfile(UserHandle.getUserId(uid)) || isUidALocationProvider(uid)) {
            return isAllowedByCurrentUserSettingsLocked(provider);
        }
        return false;
    }

    private String getResolutionPermission(int resolutionLevel) {
        switch (resolutionLevel) {
            case 1:
                return "android.permission.ACCESS_COARSE_LOCATION";
            case 2:
                return "android.permission.ACCESS_FINE_LOCATION";
            default:
                return null;
        }
    }

    private int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0) {
            return 2;
        }
        if (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0) {
            return 1;
        }
        return 0;
    }

    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < 2) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    private int getMinimumResolutionLevelForProviderUse(String provider) {
        if ("gps".equals(provider) || "passive".equals(provider)) {
            return 2;
        }
        if ("network".equals(provider) || "fused".equals(provider)) {
            return 1;
        }
        LocationProviderInterface lp = (LocationProviderInterface) this.mMockProviders.get(provider);
        if (lp != null) {
            ProviderProperties properties = lp.getProperties();
            if (properties == null || properties.mRequiresSatellite) {
                return 2;
            }
            if (properties.mRequiresNetwork || properties.mRequiresCell) {
                return 1;
            }
        }
        return 2;
    }

    private void checkResolutionLevelIsSufficientForProviderUse(int allowedResolutionLevel, String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUse(providerName);
        if (allowedResolutionLevel < requiredResolutionLevel) {
            switch (requiredResolutionLevel) {
                case 1:
                    throw new SecurityException("\"" + providerName + "\" location provider " + "requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
                case 2:
                    throw new SecurityException("\"" + providerName + "\" location provider " + "requires ACCESS_FINE_LOCATION permission.");
                default:
                    throw new SecurityException("Insufficient permission for \"" + providerName + "\" location provider.");
            }
        }
    }

    private void checkDeviceStatsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
    }

    private void checkUpdateAppOpsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_APP_OPS_STATS", null);
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel != 0) {
            return allowedResolutionLevel == 1 ? 0 : 1;
        } else {
            return -1;
        }
    }

    boolean reportLocationAccessNoThrow(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if ((op < 0 || this.mAppOps.noteOpNoThrow(op, uid, packageName) == 0) && getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        return false;
    }

    boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if ((op < 0 || this.mAppOps.checkOp(op, uid, packageName) == 0) && getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        return false;
    }

    public List<String> getAllProviders() {
        ArrayList<String> out;
        synchronized (this.mLock) {
            out = new ArrayList(this.mProviders.size());
            for (LocationProviderInterface provider : this.mProviders) {
                String name = provider.getName();
                if (!"fused".equals(name)) {
                    out.add(name);
                }
            }
        }
        Log.i("LocationManagerService", "getAllProviders()=" + out);
        return out;
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            ArrayList<String> out;
            synchronized (this.mLock) {
                out = new ArrayList(this.mProviders.size());
                for (LocationProviderInterface provider : this.mProviders) {
                    String name = provider.getName();
                    if (!"fused".equals(name) && allowedResolutionLevel >= getMinimumResolutionLevelForProviderUse(name)) {
                        if ((!enabledOnly || isAllowedByUserSettingsLocked(name, uid)) && (criteria == null || LocationProvider.propertiesMeetCriteria(name, provider.getProperties(), criteria))) {
                            out.add(name);
                        }
                    }
                }
            }
            Binder.restoreCallingIdentity(identity);
            Log.i("LocationManagerService", "getProviders()=" + out);
            return out;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        List<String> providers = getProviders(criteria, enabledOnly);
        if (providers.isEmpty()) {
            providers = getProviders(null, enabledOnly);
            if (providers.isEmpty()) {
                Log.i("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + null);
                return null;
            }
            String result = pickBest(providers);
            Log.i("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
            return result;
        }
        result = pickBest(providers);
        Log.i("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
        return result;
    }

    private String pickBest(List<String> providers) {
        if (providers.contains("gps")) {
            return "gps";
        }
        if (providers.contains("network")) {
            return "network";
        }
        return (String) providers.get(0);
    }

    public boolean providerMeetsCriteria(String provider, Criteria criteria) {
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p == null) {
            throw new IllegalArgumentException("provider=" + provider);
        }
        boolean result = LocationProvider.propertiesMeetCriteria(p.getName(), p.getProperties(), criteria);
        Log.i("LocationManagerService", "providerMeetsCriteria(" + provider + ", " + criteria + ")=" + result);
        return result;
    }

    private void updateProvidersLocked() {
        boolean changesMade = false;
        for (int i = this.mProviders.size() - 1; i >= 0; i--) {
            LocationProviderInterface p = (LocationProviderInterface) this.mProviders.get(i);
            boolean isEnabled = p.isEnabled();
            String name = p.getName();
            boolean shouldBeEnabled = isAllowedByCurrentUserSettingsLocked(name);
            Log.d("LocationManagerService", "Provider name = " + name + " shouldbeEnabled = " + shouldBeEnabled);
            if (isEnabled && !shouldBeEnabled) {
                updateProviderListenersLocked(name, false);
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                changesMade = true;
            } else if (!isEnabled && shouldBeEnabled) {
                updateProviderListenersLocked(name, true);
                changesMade = true;
            }
        }
        if (changesMade) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
            this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        }
    }

    private void updateProviderListenersLocked(String provider, boolean enabled) {
        int listeners = 0;
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p != null) {
            int i;
            ArrayList arrayList = null;
            ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
            if (records != null) {
                int N = records.size();
                for (i = 0; i < N; i++) {
                    UpdateRecord record = (UpdateRecord) records.get(i);
                    if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid))) {
                        if (!record.mReceiver.callProviderEnabledLocked(provider, enabled)) {
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                            }
                            arrayList.add(record.mReceiver);
                        }
                        listeners++;
                    }
                }
            }
            if (arrayList != null) {
                for (i = arrayList.size() - 1; i >= 0; i--) {
                    removeUpdatesLocked((Receiver) arrayList.get(i));
                }
            }
            if (enabled) {
                p.enable();
                if (listeners > 0) {
                    applyRequirementsLocked(provider);
                }
            } else {
                p.disable();
            }
        }
    }

    private void applyRequirementsLocked(String provider) {
        Log.i("LocationManagerService", "applyRequirementsLocked to " + provider);
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p == null) {
            Log.i("LocationManagerService", "LocationProviderInterface is null.");
            return;
        }
        ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
        WorkSource worksource = new WorkSource();
        ProviderRequest providerRequest = new ProviderRequest();
        if (records != null) {
            LocationRequest locationRequest;
            for (UpdateRecord record : records) {
                if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid)) && checkLocationAccess(record.mReceiver.mPid, record.mReceiver.mUid, record.mReceiver.mPackageName, record.mReceiver.mAllowedResolutionLevel)) {
                    locationRequest = record.mRequest;
                    providerRequest.locationRequests.add(locationRequest);
                    if (locationRequest.getInterval() < providerRequest.interval) {
                        providerRequest.reportLocation = true;
                        providerRequest.interval = locationRequest.getInterval();
                    }
                }
            }
            if (providerRequest.reportLocation) {
                long thresholdInterval = ((providerRequest.interval + 1000) * 3) / 2;
                for (UpdateRecord record2 : records) {
                    if (isCurrentProfile(UserHandle.getUserId(record2.mReceiver.mUid))) {
                        locationRequest = record2.mRequest;
                        if (providerRequest.locationRequests.contains(locationRequest) && locationRequest.getInterval() <= thresholdInterval) {
                            if (record2.mReceiver.mWorkSource == null || record2.mReceiver.mWorkSource.size() <= 0 || record2.mReceiver.mWorkSource.getName(0) == null) {
                                worksource.add(record2.mReceiver.mUid, record2.mReceiver.mPackageName);
                            } else {
                                worksource.add(record2.mReceiver.mWorkSource);
                            }
                        }
                    }
                }
            }
        } else {
            Log.i("LocationManagerService", "UpdateRecords is null.");
        }
        Log.i("LocationManagerService", "provider request: " + provider + " " + providerRequest);
        p.setRequest(providerRequest, worksource);
        this.mHwLocationGpsLogServices.netWorkLocation(provider, providerRequest);
    }

    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = (Receiver) this.mReceivers.get(binder);
        if (receiver == null) {
            receiver = new Receiver(listener, null, pid, uid, packageName, workSource, hideFromAppOps);
            try {
                receiver.getListener().asBinder().linkToDeath(receiver, 0);
                this.mReceivers.put(binder, receiver);
            } catch (RemoteException e) {
                Slog.e("LocationManagerService", "linkToDeath failed:", e);
                return null;
            }
        }
        return receiver;
    }

    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        Receiver receiver = (Receiver) this.mReceivers.get(intent);
        if (receiver != null) {
            return receiver;
        }
        receiver = new Receiver(null, intent, pid, uid, packageName, workSource, hideFromAppOps);
        this.mReceivers.put(intent, receiver);
        return receiver;
    }

    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (resolutionLevel < 2) {
            switch (sanitizedRequest.getQuality()) {
                case 100:
                    sanitizedRequest.setQuality(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION);
                    break;
                case 203:
                    sanitizedRequest.setQuality(201);
                    break;
            }
            if (sanitizedRequest.getInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
            if (sanitizedRequest.getFastestInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setFastestInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
        }
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            request.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName == null) {
            throw new SecurityException("invalid package name: " + packageName);
        }
        int uid = Binder.getCallingUid();
        String[] packages = this.mPackageManager.getPackagesForUid(uid);
        if (packages == null) {
            throw new SecurityException("invalid UID " + uid);
        }
        int i = 0;
        int length = packages.length;
        while (i < length) {
            if (!packageName.equals(packages[i])) {
                i++;
            } else {
                return;
            }
        }
        throw new SecurityException("invalid package name: " + packageName);
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("invalid pending intent: " + intent);
        }
    }

    private Receiver checkListenerOrIntentLocked(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        if (intent == null && listener == null) {
            throw new IllegalArgumentException("need either listener or intent");
        } else if (intent != null && listener != null) {
            throw new IllegalArgumentException("cannot register both listener and intent");
        } else if (intent == null) {
            return getReceiverLocked(listener, pid, uid, packageName, workSource, hideFromAppOps);
        } else {
            checkPendingIntent(intent);
            return getReceiverLocked(intent, pid, uid, packageName, workSource, hideFromAppOps);
        }
    }

    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        checkPackageName(packageName);
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        WorkSource workSource = request.getWorkSource();
        if (workSource != null && workSource.size() > 0) {
            checkDeviceStatsAllowed();
        }
        boolean hideFromAppOps = request.getHideFromAppOps();
        if (hideFromAppOps) {
            checkUpdateAppOpsAllowed();
        }
        LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (!HwSystemManager.allowOp(this.mContext, 8)) {
            this.mHwLocationGpsLogServices.permissionErr();
        }
        long identity = Binder.clearCallingIdentity();
        try {
            checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            synchronized (this.mLock) {
                Receiver recevier = checkListenerOrIntentLocked(listener, intent, pid, uid, packageName, workSource, hideFromAppOps);
                if (recevier == null) {
                    Log.e("LocationManagerService", "recevier creating failed, value is null");
                    Binder.restoreCallingIdentity(identity);
                    return;
                }
                requestLocationUpdatesLocked(sanitizedRequest, recevier, pid, uid, packageName);
                Binder.restoreCallingIdentity(identity);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver, int pid, int uid, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        String name = request.getProvider();
        if (name == null) {
            throw new IllegalArgumentException("provider name must not be null");
        }
        if (this.mHwGpsActionReporter != null && ("gps".equals(name) || "network".equals(name) || "fused".equals(name))) {
            String reportString = "" + "PROVIDER:" + name + ",PN:" + packageName + ",HC:" + Integer.toHexString(System.identityHashCode(receiver));
            this.mLocationHandler.removeMessages(2, reportString);
            this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 2, reportString));
        }
        Log.i("LocationManagerService", "request " + Integer.toHexString(System.identityHashCode(receiver)) + " " + name + " " + request + " from " + packageName + "(" + uid + ")");
        this.mHwLocationGpsLogServices.updateApkName(request, packageName);
        LocationProviderInterface provider = (LocationProviderInterface) this.mProvidersByName.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("provider doesn't exist: " + name);
        }
        UpdateRecord oldRecord = (UpdateRecord) receiver.mUpdateRecords.put(name, new UpdateRecord(name, request, receiver));
        if (oldRecord != null) {
            oldRecord.disposeLocked(false);
        }
        if (isAllowedByUserSettingsLocked(name, uid)) {
            if (name.equals("network")) {
                if (provider instanceof IHwLocationProviderInterface) {
                    ((IHwLocationProviderInterface) provider).resetNLPFlag();
                } else {
                    Log.d("LocationManagerService", "instanceof fail");
                }
            }
            applyRequirementsLocked(name);
            printFormatLog(packageName, uid, "requestLocationUpdatesLocked", name);
        } else {
            receiver.callProviderEnabledLocked(name, false);
        }
        if (this.mLocalLocationProvider != null && this.mLocalLocationProvider.isEnabled()) {
            UpdateRecord oldLocalProviderRrecord = (UpdateRecord) receiver.mUpdateRecords.put(IHwLocalLocationProvider.LOCAL_PROVIDER, new UpdateRecord(IHwLocalLocationProvider.LOCAL_PROVIDER, request, receiver));
            if (oldLocalProviderRrecord != null) {
                oldLocalProviderRrecord.disposeLocked(false);
            }
            this.mLocalLocationProvider.requestLocation();
            printFormatLog(packageName, uid, "requestLocationUpdatesLocked", "network");
        }
        receiver.updateMonitoring(true);
        hwLocationPowerTrackerRecordRequest(packageName, request.getQuality(), receiver.mListener == null);
    }

    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) {
        checkPackageName(packageName);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        synchronized (this.mLock) {
            Receiver receiver = checkListenerOrIntentLocked(listener, intent, pid, uid, packageName, null, false);
            long identity = Binder.clearCallingIdentity();
            if (receiver != null) {
                try {
                    removeUpdatesLocked(receiver);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeUpdatesLocked(Receiver receiver) {
        Log.i("LocationManagerService", "remove " + Integer.toHexString(System.identityHashCode(receiver)));
        if (this.mHwGpsActionReporter != null) {
            String reportString = "" + "PROVIDER:all,PN:" + receiver.mPackageName + ",HC:" + Integer.toHexString(System.identityHashCode(receiver));
            this.mLocationHandler.removeMessages(3, reportString);
            this.mLocationHandler.sendMessage(Message.obtain(this.mLocationHandler, 3, reportString));
        }
        if (this.mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            receiver.getListener().asBinder().unlinkToDeath(receiver, 0);
            synchronized (receiver) {
                receiver.clearPendingBroadcastsLocked();
            }
        }
        receiver.updateMonitoring(false);
        hwLocationPowerTrackerRemoveRequest(receiver.mPackageName);
        HashSet<String> providers = new HashSet();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            for (UpdateRecord record : oldRecords.values()) {
                record.disposeLocked(false);
            }
            providers.addAll(oldRecords.keySet());
        }
        for (String provider : providers) {
            Log.i("LocationManagerService", "isAllowedByCurrentUserSettingsLocked started: " + provider);
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                applyRequirementsLocked(provider);
            }
        }
    }

    private void applyAllProviderRequirementsLocked() {
        for (LocationProviderInterface p : this.mProviders) {
            if (isAllowedByCurrentUserSettingsLocked(p.getName())) {
                applyRequirementsLocked(p.getName());
            }
        }
    }

    public Location getLastLocation(LocationRequest request, String packageName) {
        Log.i("LocationManagerService", "getLastLocation: " + request);
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        if (!HwSystemManager.allowOp(this.mContext, 8)) {
            return HwSystemManager.getFakeLocation(request.getProvider());
        }
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mBlacklist.isBlacklisted(packageName)) {
                Log.i("LocationManagerService", "not returning last loc for blacklisted app: " + packageName);
                Binder.restoreCallingIdentity(identity);
                return null;
            } else if (reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                synchronized (this.mLock) {
                    String name = request.getProvider();
                    if (name == null) {
                        name = "fused";
                    }
                    if (((LocationProviderInterface) this.mProvidersByName.get(name)) == null) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (isAllowedByUserSettingsLocked(name, uid)) {
                        Location location;
                        if (allowedResolutionLevel < 2) {
                            location = (Location) this.mLastLocationCoarseInterval.get(name);
                        } else {
                            location = (Location) this.mLastLocation.get(name);
                        }
                        if (location == null) {
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else if (allowedResolutionLevel < 2) {
                            Location noGPSLocation = location.getExtraLocation("noGPSLocation");
                            if (noGPSLocation != null) {
                                r9 = new Location(this.mLocationFudger.getOrCreate(noGPSLocation));
                                Binder.restoreCallingIdentity(identity);
                                return r9;
                            }
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else {
                            r9 = new Location(location);
                            Binder.restoreCallingIdentity(identity);
                            return r9;
                        }
                    } else {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    }
                }
            } else {
                Log.i("LocationManagerService", "not returning last loc for no op app: " + packageName);
                Binder.restoreCallingIdentity(identity);
                return null;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        checkPendingIntent(intent);
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);
        Log.i("LocationManagerService", "requestGeofence: " + sanitizedRequest + " " + geofence + " " + intent);
        int uid = Binder.getCallingUid();
        if (UserHandle.getUserId(uid) != 0) {
            Log.w("LocationManagerService", "proximity alerts are currently available only to the primary user");
            return;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (!addQcmGeoFencer(geofence, sanitizedRequest, uid, intent, packageName)) {
                this.mGeofenceManager.addFence(sanitizedRequest, geofence, intent, allowedResolutionLevel, uid, packageName);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        checkPendingIntent(intent);
        checkPackageName(packageName);
        Log.i("LocationManagerService", "removeGeofence: " + geofence + " " + intent);
        long identity = Binder.clearCallingIdentity();
        try {
            if (!removeQcmGeoFencer(intent)) {
                this.mGeofenceManager.removeFence(geofence, intent);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (!checkLocationAccess(pid, uid, packageName, allowedResolutionLevel)) {
                return false;
            }
            Binder.restoreCallingIdentity(ident);
            HwSystemManager.allowOp(this.mContext, 8);
            if (this.mGnssStatusProvider == null) {
                return false;
            }
            try {
                this.mGnssStatusProvider.registerGnssStatusCallback(callback);
                return true;
            } catch (RemoteException e) {
                Slog.e("LocationManagerService", "mGpsStatusProvider.registerGnssStatusCallback failed", e);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
        synchronized (this.mLock) {
            try {
                this.mGnssStatusProvider.unregisterGnssStatusCallback(callback);
            } catch (Exception e) {
                Slog.e("LocationManagerService", "mGpsStatusProvider.unregisterGnssStatusCallback failed", e);
            }
        }
    }

    public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            boolean hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            if (!hasLocationAccess || this.mGnssMeasurementsProvider == null) {
                return false;
            }
            return this.mGnssMeasurementsProvider.addListener(listener, packageName);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        if (this.mGnssMeasurementsProvider != null) {
            this.mGnssMeasurementsProvider.removeListener(listener);
        }
    }

    public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            boolean hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            if (!hasLocationAccess || this.mGnssNavigationMessageProvider == null) {
                return false;
            }
            return this.mGnssNavigationMessageProvider.addListener(listener, packageName);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        if (this.mGnssNavigationMessageProvider != null) {
            this.mGnssNavigationMessageProvider.removeListener(listener);
        }
    }

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        if (provider == null) {
            throw new NullPointerException();
        }
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
        if (this.mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS) != 0) {
            throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
        }
        HwSystemManager.allowOp(this.mContext, 8);
        synchronized (this.mLock) {
            LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
            if (p == null) {
                return false;
            }
            boolean sendExtraCommand = p.sendExtraCommand(command, extras);
            return sendExtraCommand;
        }
    }

    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() != Process.myUid()) {
            throw new SecurityException("calling sendNiResponse from outside of the system is not allowed");
        }
        try {
            return this.mNetInitiatedListener.sendNiResponse(notifId, userResponse);
        } catch (RemoteException e) {
            Slog.e("LocationManagerService", "RemoteException in LocationManagerService.sendNiResponse");
            return false;
        }
    }

    public ProviderProperties getProviderProperties(String provider) {
        if (this.mProvidersByName.get(provider) == null) {
            return null;
        }
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
        synchronized (this.mLock) {
            LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        }
        if (p == null) {
            return null;
        }
        return p.getProperties();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getNetworkProviderPackage() {
        synchronized (this.mLock) {
            if (this.mProvidersByName.get("network") == null) {
                return null;
            }
            LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get("network");
        }
    }

    public boolean isProviderEnabled(String provider) {
        if ("fused".equals(provider)) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (((LocationProviderInterface) this.mProvidersByName.get(provider)) == null) {
                    Binder.restoreCallingIdentity(identity);
                    return false;
                }
                boolean isAllowedByUserSettingsLocked = isAllowedByUserSettingsLocked(provider, uid);
                Binder.restoreCallingIdentity(identity);
                return isAllowedByUserSettingsLocked;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean isUidALocationProvider(int uid) {
        if (uid == 1000) {
            return true;
        }
        if (this.mGeocodeProvider != null && doesUidHavePackage(uid, this.mGeocodeProvider.getConnectedPackageName())) {
            return true;
        }
        for (LocationProviderProxy proxy : this.mProxyProviders) {
            if (doesUidHavePackage(uid, proxy.getConnectedPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void checkCallerIsProvider() {
        if (this.mContext.checkCallingOrSelfPermission(INSTALL_LOCATION_PROVIDER) != 0 && !isUidALocationProvider(Binder.getCallingUid())) {
            throw new SecurityException("need INSTALL_LOCATION_PROVIDER permission, or UID of a currently bound location provider");
        }
    }

    private boolean doesUidHavePackage(int uid, String packageName) {
        if (packageName == null) {
            return false;
        }
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames == null) {
            return false;
        }
        String[] pNames = packageName.split(";");
        if (pNames.length >= 2) {
            for (String pName : pNames) {
                for (String name : packageNames) {
                    if (pName.equals(name)) {
                        return true;
                    }
                }
            }
        } else {
            for (String name2 : packageNames) {
                if (packageName.equals(name2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reportLocation(Location location, boolean passive) {
        int i = 1;
        checkCallerIsProvider();
        if (location.isComplete()) {
            HwSystemManager.allowOp(this.mContext, 8);
            String provider = passive ? "passive" : location.getProvider();
            if (provider.equals("network")) {
                LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
                if (p == null || !(p instanceof IHwLocationProviderInterface)) {
                    Log.d("LocationManagerService", "instanceof fail");
                } else if (!((IHwLocationProviderInterface) p).reportNLPLocation(Binder.getCallingPid())) {
                    return;
                }
            }
            this.mLocationHandler.removeMessages(1, location);
            Message m = Message.obtain(this.mLocationHandler, 1, location);
            if (!passive) {
                i = 0;
            }
            m.arg1 = i;
            this.mLocationHandler.sendMessageAtFrontOfQueue(m);
            return;
        }
        Log.w("LocationManagerService", "Dropping incomplete location: " + location);
    }

    private static boolean shouldBroadcastSafe(Location loc, Location lastLoc, UpdateRecord record, long now) {
        if (lastLoc == null) {
            return true;
        }
        if ((loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos()) / NANOS_PER_MILLI < record.mRequest.getFastestInterval() - 100) {
            return false;
        }
        double minDistance = (double) record.mRequest.getSmallestDisplacement();
        if (minDistance > 0.0d && ((double) loc.distanceTo(lastLoc)) <= minDistance) {
            return false;
        }
        if (record.mRequest.getNumUpdates() <= 0) {
            return false;
        }
        if (record.mRequest.getExpireAt() < now) {
            return false;
        }
        return true;
    }

    private void handleLocationChangedLocked(Location location, boolean passive) {
        Log.i("LocationManagerService", "incoming location: " + location);
        long now = SystemClock.elapsedRealtime();
        String provider = passive ? "passive" : location.getProvider();
        this.mHwLocationGpsLogServices.updateLocation(location, SystemClock.elapsedRealtimeNanos(), provider);
        updateLocalLocationDB(location, provider);
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p != null) {
            Location noGPSLocation = location.getExtraLocation("noGPSLocation");
            Location lastLocation = (Location) this.mLastLocation.get(provider);
            if (lastLocation == null) {
                lastLocation = new Location(provider);
                this.mLastLocation.put(provider, lastLocation);
            } else {
                Location lastNoGPSLocation = lastLocation.getExtraLocation("noGPSLocation");
                if (noGPSLocation == null && lastNoGPSLocation != null) {
                    location.setExtraLocation("noGPSLocation", lastNoGPSLocation);
                }
            }
            lastLocation.set(location);
            Location lastLocationCoarseInterval = (Location) this.mLastLocationCoarseInterval.get(provider);
            if (lastLocationCoarseInterval == null) {
                lastLocationCoarseInterval = new Location(location);
                this.mLastLocationCoarseInterval.put(provider, lastLocationCoarseInterval);
            }
            if (location.getElapsedRealtimeNanos() - lastLocationCoarseInterval.getElapsedRealtimeNanos() > 600000000000L) {
                lastLocationCoarseInterval.set(location);
            }
            noGPSLocation = lastLocationCoarseInterval.getExtraLocation("noGPSLocation");
            ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
            if (records != null && records.size() != 0) {
                Receiver receiver;
                Location coarseLocation = null;
                if (noGPSLocation != null) {
                    coarseLocation = this.mLocationFudger.getOrCreate(noGPSLocation);
                }
                long newStatusUpdateTime = p.getStatusUpdateTime();
                Bundle extras = new Bundle();
                int status = p.getStatus(extras);
                Iterable deadReceivers = null;
                Iterable deadUpdateRecords = null;
                for (UpdateRecord r : records) {
                    receiver = r.mReceiver;
                    boolean receiverDead = false;
                    int receiverUserId = UserHandle.getUserId(receiver.mUid);
                    if (!isCurrentProfile(receiverUserId)) {
                        if (!isUidALocationProvider(receiver.mUid)) {
                            Log.i("LocationManagerService", "skipping loc update for background user " + receiverUserId + " (current user: " + this.mCurrentUserId + ", app: " + receiver.mPackageName + ")");
                        }
                    }
                    if (this.mBlacklist.isBlacklisted(receiver.mPackageName)) {
                        Log.i("LocationManagerService", "skipping loc update for blacklisted app: " + receiver.mPackageName);
                    } else {
                        if (reportLocationAccessNoThrow(receiver.mPid, receiver.mUid, receiver.mPackageName, receiver.mAllowedResolutionLevel)) {
                            Location notifyLocation;
                            if (receiver.mAllowedResolutionLevel < 2) {
                                notifyLocation = coarseLocation;
                            } else {
                                notifyLocation = lastLocation;
                            }
                            if (notifyLocation != null) {
                                Location lastLoc = r.mLastFixBroadcast;
                                if (lastLoc == null || shouldBroadcastSafe(notifyLocation, lastLoc, r, now)) {
                                    if (lastLoc == null) {
                                        r.mLastFixBroadcast = new Location(notifyLocation);
                                    } else {
                                        lastLoc.set(notifyLocation);
                                    }
                                    if (!receiver.callLocationChangedLocked(notifyLocation)) {
                                        Slog.w("LocationManagerService", "RemoteException calling onLocationChanged on " + receiver);
                                        receiverDead = true;
                                    }
                                    r.mRequest.decrementNumUpdates();
                                    printFormatLog(receiver.mPackageName, receiver.mUid, "handleLocationChangedLocked", "report_location");
                                }
                            }
                            long prevStatusUpdateTime = r.mLastStatusBroadcast;
                            if (newStatusUpdateTime > prevStatusUpdateTime && !(prevStatusUpdateTime == 0 && status == 2)) {
                                r.mLastStatusBroadcast = newStatusUpdateTime;
                                if (!receiver.callStatusChangedLocked(provider, status, extras)) {
                                    receiverDead = true;
                                    Slog.w("LocationManagerService", "RemoteException calling onStatusChanged on " + receiver);
                                }
                            }
                            if (r.mRequest.getNumUpdates() <= 0 || r.mRequest.getExpireAt() < now) {
                                if (r8 == null) {
                                    deadUpdateRecords = new ArrayList();
                                }
                                deadUpdateRecords.add(r);
                            }
                            if (receiverDead) {
                                if (r7 == null) {
                                    deadReceivers = new ArrayList();
                                }
                                if (!deadReceivers.contains(receiver)) {
                                    deadReceivers.add(receiver);
                                }
                            }
                        } else {
                            Log.i("LocationManagerService", "skipping loc update for no op app: " + receiver.mPackageName);
                        }
                    }
                }
                if (r7 != null) {
                    for (Receiver receiver2 : r7) {
                        removeUpdatesLocked(receiver2);
                    }
                }
                if (r8 != null) {
                    for (UpdateRecord r2 : r8) {
                        r2.disposeLocked(true);
                    }
                    applyRequirementsLocked(provider);
                }
            }
        }
    }

    private boolean isMockProvider(String provider) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mMockProviders.containsKey(provider);
        }
        return containsKey;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleLocationChanged(Location location, boolean passive) {
        Location myLocation = new Location(location);
        String provider = myLocation.getProvider();
        if (!myLocation.isFromMockProvider() && isMockProvider(provider)) {
            myLocation.setIsFromMockProvider(true);
        }
        synchronized (this.mLock) {
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                if (!passive) {
                    if (screenLocationLocked(location, provider) == null) {
                        return;
                    }
                    this.mPassiveProvider.updateLocation(myLocation);
                }
                handleLocationChangedLocked(myLocation, passive);
            }
        }
    }

    public boolean geocoderIsPresent() {
        return this.mGeocodeProvider != null;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mGeocodeProvider.getFromLocation(latitude, longitude, maxResults, params, addrs);
        }
        return null;
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mGeocodeProvider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        return null;
    }

    private boolean canCallerAccessMockLocation(String opPackageName) {
        return this.mAppOps.noteOp(58, Binder.getCallingUid(), opPackageName) == 0;
    }

    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }
        if ("passive".equals(name)) {
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }
        long identity = Binder.clearCallingIdentity();
        synchronized (this.mLock) {
            if ("gps".equals(name) || "network".equals(name) || IHwLocalLocationProvider.LOCAL_PROVIDER.equals(name) || "fused".equals(name)) {
                LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(name);
                if (p != null) {
                    removeProviderLocked(p);
                }
            }
            setGeoFencerEnabled(false);
            addTestProviderLocked(name, properties);
            updateProvidersLocked();
        }
        Binder.restoreCallingIdentity(identity);
    }

    private void addTestProviderLocked(String name, ProviderProperties properties) {
        if (this.mProvidersByName.get(name) != null) {
            throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
        }
        MockProvider provider = new MockProvider(name, this, properties);
        addProviderLocked(provider);
        this.mMockProviders.put(name, provider);
        this.mLastLocation.put(name, null);
        this.mLastLocationCoarseInterval.put(name, null);
    }

    public void removeTestProvider(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                clearTestProviderEnabled(provider, opPackageName);
                clearTestProviderLocation(provider, opPackageName);
                clearTestProviderStatus(provider, opPackageName);
                if (((MockProvider) this.mMockProviders.remove(provider)) == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                removeProviderLocked((LocationProviderInterface) this.mProvidersByName.get(provider));
                setGeoFencerEnabled(true);
                LocationProviderInterface realProvider = (LocationProviderInterface) this.mRealProviders.get(provider);
                if (realProvider != null) {
                    addProviderLocked(realProvider);
                }
                this.mLastLocation.put(provider, null);
                this.mLastLocationCoarseInterval.put(provider, null);
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setTestProviderLocation(String provider, Location loc, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                Location mock = new Location(loc);
                mock.setIsFromMockProvider(true);
                if (!(TextUtils.isEmpty(loc.getProvider()) || provider.equals(loc.getProvider()))) {
                    EventLog.writeEvent(1397638484, new Object[]{"33091107", Integer.valueOf(Binder.getCallingUid()), provider + "!=" + loc.getProvider()});
                }
                long identity = Binder.clearCallingIdentity();
                mockProvider.setLocation(mock);
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void clearTestProviderLocation(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.clearLocation();
            }
        }
    }

    public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                if (enabled) {
                    mockProvider.enable();
                    this.mEnabledProviders.add(provider);
                    this.mDisabledProviders.remove(provider);
                } else {
                    mockProvider.disable();
                    this.mEnabledProviders.remove(provider);
                    this.mDisabledProviders.add(provider);
                }
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void clearTestProviderEnabled(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                if (((MockProvider) this.mMockProviders.get(provider)) == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                this.mEnabledProviders.remove(provider);
                this.mDisabledProviders.remove(provider);
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.setStatus(status, extras, updateTime);
            }
        }
    }

    public void clearTestProviderStatus(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.clearStatus();
            }
        }
    }

    private void log(String log) {
        if (Log.isLoggable("LocationManagerService", 2)) {
            Slog.d("LocationManagerService", log);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump LocationManagerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mLock) {
            pw.println("Current Location Manager state:");
            pw.println("  Location Listeners:");
            for (Receiver receiver : this.mReceivers.values()) {
                pw.println("    " + receiver);
            }
            pw.println("  Active Records by Provider:");
            for (Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                pw.println("    " + ((String) entry.getKey()) + ":");
                for (UpdateRecord record : (ArrayList) entry.getValue()) {
                    pw.println("      " + record);
                }
            }
            pw.println("  Historical Records by Provider:");
            for (Entry<PackageProviderKey, PackageStatistics> entry2 : this.mRequestStatistics.statistics.entrySet()) {
                PackageProviderKey key = (PackageProviderKey) entry2.getKey();
                pw.println("    " + key.packageName + ": " + key.providerName + ": " + ((PackageStatistics) entry2.getValue()));
            }
            pw.println("  Last Known Locations:");
            for (Entry<String, Location> entry3 : this.mLastLocation.entrySet()) {
                Location location = (Location) entry3.getValue();
                pw.println("    " + ((String) entry3.getKey()) + ": " + location);
            }
            pw.println("  Last Known Locations Coarse Intervals:");
            for (Entry<String, Location> entry32 : this.mLastLocationCoarseInterval.entrySet()) {
                location = (Location) entry32.getValue();
                pw.println("    " + ((String) entry32.getKey()) + ": " + location);
            }
            this.mGeofenceManager.dump(pw);
            if (this.mEnabledProviders.size() > 0) {
                pw.println("  Enabled Providers:");
                for (String i : this.mEnabledProviders) {
                    pw.println("    " + i);
                }
            }
            if (this.mDisabledProviders.size() > 0) {
                pw.println("  Disabled Providers:");
                for (String i2 : this.mDisabledProviders) {
                    pw.println("    " + i2);
                }
            }
            pw.append("  ");
            this.mBlacklist.dump(pw);
            if (this.mMockProviders.size() > 0) {
                pw.println("  Mock Providers:");
                for (Entry<String, MockProvider> i3 : this.mMockProviders.entrySet()) {
                    ((MockProvider) i3.getValue()).dump(pw, "      ");
                }
            }
            pw.append("  fudger: ");
            this.mLocationFudger.dump(fd, pw, args);
            if (args.length <= 0 || !"short".equals(args[0])) {
                for (LocationProviderInterface provider : this.mProviders) {
                    pw.print(provider.getName() + " Internal State");
                    if (provider instanceof LocationProviderProxy) {
                        LocationProviderProxy proxy = (LocationProviderProxy) provider;
                        pw.print(" (" + proxy.getConnectedPackageName() + ")");
                    }
                    pw.println(":");
                    provider.dump(fd, pw, args);
                }
                hwLocationPowerTrackerDump(pw);
                dumpGpsFreezeProxy(pw);
                return;
            }
        }
    }

    private String getCallingAppName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo == null) {
            return packageName;
        }
        return (String) this.mPackageManager.getApplicationLabel(appInfo);
    }

    private void printFormatLog(String packageName, int uid, String callingMethodName, String tag) {
        if (!TextUtils.isEmpty(packageName)) {
            ArrayList<String> applicationInfo = new ArrayList();
            applicationInfo.add(getCallingAppName(packageName));
            applicationInfo.add(packageName);
            applicationInfo.add(this.mPackageManager.getNameForUid(uid));
            if (tag.equals("gps")) {
                Log.i(applicationInfo, callingMethodName, "定位..GPS定位");
            }
            if (tag.equals("network")) {
                Log.i(applicationInfo, callingMethodName, "定位..Wifi/基站定位");
            }
            if (tag.equals("report_location")) {
                Log.i(applicationInfo, callingMethodName, "读取定位信息");
            }
        }
    }
}
