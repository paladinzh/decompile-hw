package com.android.settingslib.deviceinfo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.UserInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.app.IMediaContainerService.Stub;
import com.android.internal.util.ArrayUtils;
import com.google.android.collect.Sets;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class StorageMeasurement {
    public static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName("com.android.defcontainer", "com.android.defcontainer.DefaultContainerService");
    static final boolean LOGV = false;
    private static final Set<String> sMeasureMediaTypes = Sets.newHashSet(new String[]{Environment.DIRECTORY_DCIM, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_PODCASTS, Environment.DIRECTORY_DOWNLOADS, "Android"});
    private final Context mContext;
    private final MainHandler mMainHandler = new MainHandler();
    private final MeasurementHandler mMeasurementHandler;
    private WeakReference<MeasurementReceiver> mReceiver;
    private final VolumeInfo mSharedVolume;
    private final VolumeInfo mVolume;

    public interface MeasurementReceiver {
        void onDetailsChanged(MeasurementDetails measurementDetails);
    }

    private class MainHandler extends Handler {
        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            MeasurementReceiver receiver = null;
            MeasurementDetails details = msg.obj;
            if (StorageMeasurement.this.mReceiver != null) {
                receiver = (MeasurementReceiver) StorageMeasurement.this.mReceiver.get();
            }
            if (receiver != null) {
                receiver.onDetailsChanged(details);
            }
        }
    }

    public static class MeasurementDetails {
        public SparseLongArray appsSize = new SparseLongArray();
        public long availSize;
        public long cacheSize;
        public SparseArray<HashMap<String, Long>> mediaSize = new SparseArray();
        public SparseLongArray miscSize = new SparseLongArray();
        public long totalSize;
        public SparseLongArray usersSize = new SparseLongArray();
    }

    private class MeasurementHandler extends Handler {
        private volatile boolean mBound = false;
        private MeasurementDetails mCached;
        private final ServiceConnection mDefContainerConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                IMediaContainerService imcs = Stub.asInterface(service);
                MeasurementHandler.this.mDefaultContainer = imcs;
                MeasurementHandler.this.mBound = true;
                MeasurementHandler.this.sendMessage(MeasurementHandler.this.obtainMessage(2, imcs));
            }

            public void onServiceDisconnected(ComponentName name) {
                MeasurementHandler.this.mBound = false;
                MeasurementHandler.this.removeMessages(2);
            }
        };
        private IMediaContainerService mDefaultContainer;
        private Object mLock = new Object();

        public MeasurementHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Object obj;
            switch (msg.what) {
                case 1:
                    if (this.mCached == null) {
                        obj = this.mLock;
                        synchronized (obj) {
                            if (!this.mBound) {
                                StorageMeasurement.this.mContext.bindServiceAsUser(new Intent().setComponent(StorageMeasurement.DEFAULT_CONTAINER_COMPONENT), this.mDefContainerConn, 1, UserHandle.SYSTEM);
                                break;
                            }
                            removeMessages(3);
                            sendMessage(obtainMessage(2, this.mDefaultContainer));
                            break;
                        }
                    }
                    StorageMeasurement.this.mMainHandler.obtainMessage(0, this.mCached).sendToTarget();
                    return;
                case 2:
                    StorageMeasurement.this.measureExactStorage(msg.obj);
                    return;
                case 3:
                    obj = this.mLock;
                    synchronized (obj) {
                        if (this.mBound) {
                            this.mBound = false;
                            StorageMeasurement.this.mContext.unbindService(this.mDefContainerConn);
                            break;
                        }
                    }
                    break;
                case 4:
                    this.mCached = (MeasurementDetails) msg.obj;
                    StorageMeasurement.this.mMainHandler.obtainMessage(0, this.mCached).sendToTarget();
                    return;
                case 5:
                    this.mCached = null;
                    return;
                default:
                    return;
            }
        }
    }

    private static class StatsObserver extends IPackageStatsObserver.Stub {
        private final int mCurrentUser;
        private final MeasurementDetails mDetails;
        private final Message mFinished;
        private final boolean mIsPrivate;
        private int mRemaining;

        public StatsObserver(boolean isPrivate, MeasurementDetails details, int currentUser, List<UserInfo> profiles, Message finished, int remaining) {
            this.mIsPrivate = isPrivate;
            this.mDetails = details;
            this.mCurrentUser = currentUser;
            if (isPrivate) {
                for (UserInfo userInfo : profiles) {
                    this.mDetails.appsSize.put(userInfo.id, 0);
                }
            }
            this.mFinished = finished;
            this.mRemaining = remaining;
        }

        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            synchronized (this.mDetails) {
                if (succeeded) {
                    addStatsLocked(stats);
                }
                int i = this.mRemaining - 1;
                this.mRemaining = i;
                if (i == 0) {
                    this.mFinished.sendToTarget();
                }
            }
        }

        private void addStatsLocked(PackageStats stats) {
            if (this.mIsPrivate) {
                long codeSize = stats.codeSize;
                long dataSize = stats.dataSize;
                long cacheSize = stats.cacheSize;
                if (Environment.isExternalStorageEmulated()) {
                    codeSize += stats.externalCodeSize + stats.externalObbSize;
                    dataSize += stats.externalDataSize + stats.externalMediaSize;
                    cacheSize += stats.externalCacheSize;
                }
                if (stats.userHandle == ActivityManager.getCurrentUser()) {
                    StorageMeasurement.addValueIfKeyExists(this.mDetails.appsSize, stats.userHandle, codeSize + dataSize);
                } else {
                    StorageMeasurement.addValueIfKeyExists(this.mDetails.appsSize, stats.userHandle, dataSize);
                }
                StorageMeasurement.addValue(this.mDetails.usersSize, stats.userHandle, dataSize);
                MeasurementDetails measurementDetails = this.mDetails;
                measurementDetails.cacheSize += cacheSize;
                return;
            }
            StorageMeasurement.addValue(this.mDetails.appsSize, this.mCurrentUser, ((stats.externalCodeSize + stats.externalDataSize) + stats.externalMediaSize) + stats.externalObbSize);
            measurementDetails = this.mDetails;
            measurementDetails.cacheSize += stats.externalCacheSize;
        }
    }

    public StorageMeasurement(Context context, VolumeInfo volume, VolumeInfo sharedVolume) {
        this.mContext = context.getApplicationContext();
        this.mVolume = volume;
        this.mSharedVolume = sharedVolume;
        HandlerThread handlerThread = new HandlerThread("MemoryMeasurement");
        handlerThread.start();
        this.mMeasurementHandler = new MeasurementHandler(handlerThread.getLooper());
    }

    public void setReceiver(MeasurementReceiver receiver) {
        if (this.mReceiver == null || this.mReceiver.get() == null) {
            this.mReceiver = new WeakReference(receiver);
        }
    }

    public void forceMeasure() {
        invalidate();
        measure();
    }

    public void measure() {
        if (!this.mMeasurementHandler.hasMessages(1)) {
            this.mMeasurementHandler.sendEmptyMessage(1);
        }
    }

    public void onDestroy() {
        this.mReceiver = null;
        this.mMeasurementHandler.removeMessages(1);
        this.mMeasurementHandler.sendEmptyMessage(3);
    }

    private void invalidate() {
        this.mMeasurementHandler.sendEmptyMessage(5);
    }

    private void measureExactStorage(IMediaContainerService imcs) {
        UserManager userManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        PackageManager packageManager = this.mContext.getPackageManager();
        List<UserInfo> users = userManager.getUsers();
        List<UserInfo> currentProfiles = userManager.getEnabledProfiles(ActivityManager.getCurrentUser());
        MeasurementDetails details = new MeasurementDetails();
        Message finished = this.mMeasurementHandler.obtainMessage(4, details);
        if (this.mVolume == null || !this.mVolume.isMountedReadable()) {
            finished.sendToTarget();
            return;
        }
        if (this.mSharedVolume != null && this.mSharedVolume.isMountedReadable()) {
            for (UserInfo currentUserInfo : currentProfiles) {
                int userId = currentUserInfo.id;
                File basePath = this.mSharedVolume.getPathForUser(userId);
                HashMap<String, Long> hashMap = new HashMap(sMeasureMediaTypes.size());
                details.mediaSize.put(userId, hashMap);
                for (String type : sMeasureMediaTypes) {
                    File file = new File(basePath, type);
                    hashMap.put(type, Long.valueOf(getDirectorySize(imcs, file)));
                }
                addValue(details.miscSize, userId, measureMisc(imcs, basePath));
            }
            if (this.mSharedVolume.getType() == 2) {
                for (UserInfo user : users) {
                    File userPath = this.mSharedVolume.getPathForUser(user.id);
                    if (userPath == null) {
                        Log.e("StorageMeasurement", "measureExactStorage()-->userPath is null !");
                    } else {
                        addValue(details.usersSize, user.id, getDirectorySize(imcs, userPath));
                    }
                }
            }
        }
        File file2 = this.mVolume.getPath();
        if (file2 != null) {
            details.totalSize = file2.getTotalSpace();
            details.availSize = file2.getFreeSpace();
        }
        if (this.mVolume.getType() == 1) {
            List<ApplicationInfo> apps = packageManager.getInstalledApplications(8704);
            List<ApplicationInfo> volumeApps = new ArrayList();
            for (ApplicationInfo app : apps) {
                if (Objects.equals(app.volumeUuid, this.mVolume.getFsUuid())) {
                    volumeApps.add(app);
                }
            }
            int count = users.size() * volumeApps.size();
            if (count == 0) {
                finished.sendToTarget();
                return;
            }
            StatsObserver observer = new StatsObserver(true, details, ActivityManager.getCurrentUser(), currentProfiles, finished, count);
            for (UserInfo user2 : users) {
                for (ApplicationInfo app2 : volumeApps) {
                    packageManager.getPackageSizeInfoAsUser(app2.packageName, user2.id, observer);
                }
            }
            return;
        }
        finished.sendToTarget();
    }

    private static long getDirectorySize(IMediaContainerService imcs, File path) {
        try {
            long size = imcs.calculateDirectorySize(path.toString());
            Log.d("StorageMeasurement", "getDirectorySize(" + path + ") returned " + size);
            return size;
        } catch (Exception e) {
            Log.w("StorageMeasurement", "Could not read memory from default container service for " + path, e);
            return 0;
        }
    }

    private long measureMisc(IMediaContainerService imcs, File dir) {
        if (dir == null) {
            return 0;
        }
        File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return 0;
        }
        long miscSize = 0;
        for (File file : files) {
            if (!sMeasureMediaTypes.contains(file.getName())) {
                if (file.isFile()) {
                    miscSize += file.length();
                } else if (file.isDirectory()) {
                    miscSize += getDirectorySize(imcs, file);
                }
            }
        }
        return miscSize;
    }

    private static void addValue(SparseLongArray array, int key, long value) {
        array.put(key, array.get(key) + value);
    }

    private static void addValueIfKeyExists(SparseLongArray array, int key, long value) {
        int index = array.indexOfKey(key);
        if (index >= 0) {
            array.put(key, array.valueAt(index) + value);
        }
    }
}
