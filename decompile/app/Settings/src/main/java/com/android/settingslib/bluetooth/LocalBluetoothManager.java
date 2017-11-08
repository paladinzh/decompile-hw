package com.android.settingslib.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import java.util.List;

public final class LocalBluetoothManager {
    private static LocalBluetoothManager sInstance;
    private ActivityManager mActivityManager = null;
    private BtDialogObserver mBtDialogObserver;
    private final CachedBluetoothDeviceManager mCachedDeviceManager;
    private final Context mContext;
    private Object mDiscoverableEnabler;
    private final BluetoothEventManager mEventManager;
    private Context mForegroundActivity;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;

    public interface BtDialogObserver {
        void onDialogShow(Dialog dialog);
    }

    public interface BluetoothManagerCallback {
        void onBluetoothManagerInitialized(Context context, LocalBluetoothManager localBluetoothManager);
    }

    private LocalBluetoothManager(LocalBluetoothAdapter adapter, Context context) {
        this.mContext = context;
        this.mLocalAdapter = adapter;
        this.mCachedDeviceManager = new CachedBluetoothDeviceManager(context, this);
        this.mEventManager = new BluetoothEventManager(this.mLocalAdapter, this.mCachedDeviceManager, context);
        this.mProfileManager = new LocalBluetoothProfileManager(context, this.mLocalAdapter, this.mCachedDeviceManager, this.mEventManager);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized LocalBluetoothManager getInstance(Context context, BluetoothManagerCallback onInitCallback) {
        synchronized (LocalBluetoothManager.class) {
            if (sInstance == null) {
                LocalBluetoothAdapter adapter = LocalBluetoothAdapter.getInstance();
                if (adapter == null || context == null) {
                } else {
                    Context appContext = context.getApplicationContext();
                    sInstance = new LocalBluetoothManager(adapter, appContext);
                    if (onInitCallback != null) {
                        onInitCallback.onBluetoothManagerInitialized(appContext, sInstance);
                    }
                }
            }
            LocalBluetoothManager localBluetoothManager = sInstance;
            return localBluetoothManager;
        }
    }

    public void setDiscoverableEnabler(Object discoverableEnabler) {
        this.mDiscoverableEnabler = discoverableEnabler;
    }

    public Object getDiscoverableEnabler() {
        return this.mDiscoverableEnabler;
    }

    public LocalBluetoothAdapter getBluetoothAdapter() {
        return this.mLocalAdapter;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Context getForegroundActivity() {
        return this.mForegroundActivity;
    }

    public boolean isForegroundActivity() {
        return this.mForegroundActivity != null;
    }

    public synchronized void setForegroundActivity(Context context) {
        int orientation = SystemProperties.getInt("ro.panel.hw_orientation", 0);
        if (context != null) {
            Log.d("LocalBluetoothManager", "setting foreground activity to non-null context");
            if (orientation != 90) {
                this.mForegroundActivity = context;
            } else if ((context instanceof Activity) && isForgroundActivity((Activity) context)) {
                this.mForegroundActivity = context;
            }
        } else if (this.mForegroundActivity != null) {
            Log.d("LocalBluetoothManager", "setting foreground activity to null");
            if (orientation != 90) {
                this.mForegroundActivity = null;
            } else if (!isForgroundActivity((Activity) this.mForegroundActivity)) {
                this.mForegroundActivity = null;
            }
        }
    }

    private boolean isForgroundActivity(Activity activity) {
        ComponentName curForgroundActivity = getCurForegroundActivity(activity);
        if (curForgroundActivity == null || activity == null) {
            Log.d("LocalBluetoothManager", "curForgroundActivity or activity == null,return false");
            return false;
        } else if (curForgroundActivity.equals(activity.getComponentName())) {
            return true;
        } else {
            return false;
        }
    }

    private ComponentName getCurForegroundActivity(Context context) {
        if (this.mActivityManager == null) {
            if (context != null) {
                this.mActivityManager = (ActivityManager) context.getSystemService("activity");
            }
            if (this.mActivityManager == null) {
                return null;
            }
        }
        List<RunningTaskInfo> tasksInfo = this.mActivityManager.getRunningTasks(1);
        if (tasksInfo == null || tasksInfo.size() <= 0) {
            return null;
        }
        return ((RunningTaskInfo) tasksInfo.get(0)).topActivity;
    }

    public CachedBluetoothDeviceManager getCachedDeviceManager() {
        return this.mCachedDeviceManager;
    }

    public BluetoothEventManager getEventManager() {
        return this.mEventManager;
    }

    public LocalBluetoothProfileManager getProfileManager() {
        return this.mProfileManager;
    }

    public synchronized void setBtDialogObserver(BtDialogObserver observer) {
        this.mBtDialogObserver = observer;
    }

    public synchronized BtDialogObserver getBtDialogObserver() {
        return this.mBtDialogObserver;
    }
}
