package com.android.systemui.qs.external;

import android.app.AppGlobals;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.IQSTileService.Stub;
import android.service.quicksettings.Tile;
import android.support.annotation.VisibleForTesting;
import android.util.ArraySet;
import android.util.Log;
import java.util.Set;
import libcore.util.Objects;

public class TileLifecycleManager extends BroadcastReceiver implements IQSTileService, ServiceConnection, DeathRecipient {
    private int mBindTryCount;
    private boolean mBound;
    public TileChangeListener mChangeListener;
    private IBinder mClickBinder;
    private final Context mContext;
    private final Handler mHandler;
    private final Intent mIntent;
    private boolean mIsBound;
    private boolean mListening;
    private PowerManager mPowerManager = null;
    private Set<Integer> mQueuedMessages = new ArraySet();
    @VisibleForTesting
    boolean mReceiverRegistered;
    private Runnable mRetry;
    private boolean mUnbindImmediate;
    private final UserHandle mUser;
    private QSTileServiceWrapper mWrapper;

    public interface TileChangeListener {
        void onBindFailed(ComponentName componentName);

        void onTileChanged(ComponentName componentName);
    }

    public TileLifecycleManager(Handler handler, Context context, IQSService service, Tile tile, Intent intent, UserHandle user) {
        this.mContext = context;
        this.mHandler = handler;
        this.mIntent = intent;
        this.mIntent.putExtra("service", service.asBinder());
        this.mIntent.putExtra("android.service.quicksettings.extra.COMPONENT", intent.getComponent());
        this.mUser = user;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        Log.d("TileLifecycleManager", "Creating " + this.mIntent + " " + this.mUser + " " + this);
    }

    public ComponentName getComponent() {
        return this.mIntent.getComponent();
    }

    public boolean hasPendingClick() {
        boolean contains;
        synchronized (this.mQueuedMessages) {
            contains = this.mQueuedMessages.contains(Integer.valueOf(2));
        }
        return contains;
    }

    public boolean isActiveTile() {
        boolean z = false;
        try {
            ServiceInfo info = this.mContext.getPackageManager().getServiceInfo(this.mIntent.getComponent(), 8320);
            if (info.metaData != null) {
                z = info.metaData.getBoolean("android.service.quicksettings.ACTIVE_TILE", false);
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public void flushMessagesAndUnbind() {
        this.mUnbindImmediate = true;
        setBindService(true);
    }

    public void setBindService(boolean bind) {
        Log.d("TileLifecycleManager", "setBindService: " + bind + ", " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        this.mBound = bind;
        if (!bind) {
            Log.d("TileLifecycleManager", "Unbinding service " + this.mIntent + " " + this.mUser + ", " + this + ", user=" + this.mUser);
            this.mBindTryCount = 0;
            this.mWrapper = null;
            try {
                if (this.mIsBound) {
                    this.mIsBound = false;
                    this.mContext.unbindService(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (checkComponentState()) {
            Log.d("TileLifecycleManager", "Binding service " + this.mIntent + " " + this.mUser + ", " + this + ", user=" + this.mUser + ", mBindTryCount=" + this.mBindTryCount);
            this.mBindTryCount++;
            try {
                this.mIsBound = this.mContext.bindServiceAsUser(this.mIntent, this, 33554433, this.mUser);
            } catch (SecurityException e2) {
                Log.e("TileLifecycleManager", "Failed to bind to service", e2);
                this.mIsBound = false;
            }
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("TileLifecycleManager", "onServiceConnected " + name + " " + this + ", user=" + this.mUser);
        this.mBindTryCount = 0;
        QSTileServiceWrapper wrapper = new QSTileServiceWrapper(Stub.asInterface(service));
        try {
            service.linkToDeath(this, 0);
        } catch (RemoteException e) {
        }
        this.mWrapper = wrapper;
        handlePendingMessages();
    }

    public void onServiceDisconnected(ComponentName name) {
        Log.d("TileLifecycleManager", "onServiceDisconnected " + name + ", " + this + ", user=" + this.mUser);
        handleDeath(false);
    }

    private void handlePendingMessages() {
        synchronized (this.mQueuedMessages) {
            ArraySet<Integer> queue = new ArraySet(this.mQueuedMessages);
            this.mQueuedMessages.clear();
        }
        if (queue.contains(Integer.valueOf(0))) {
            Log.d("TileLifecycleManager", "Handling pending onAdded");
            onTileAdded();
        }
        if (this.mListening) {
            Log.d("TileLifecycleManager", "Handling pending onStartListening");
            onStartListening();
        }
        if (queue.contains(Integer.valueOf(2))) {
            Log.d("TileLifecycleManager", "Handling pending onClick");
            if (this.mListening) {
                onClick(this.mClickBinder);
            } else {
                Log.w("TileLifecycleManager", "Managed to get click on non-listening state...");
            }
        }
        if (queue.contains(Integer.valueOf(3))) {
            Log.d("TileLifecycleManager", "Handling pending onUnlockComplete");
            if (this.mListening) {
                onUnlockComplete();
            } else {
                Log.w("TileLifecycleManager", "Managed to get unlock on non-listening state...");
            }
        }
        if (queue.contains(Integer.valueOf(1))) {
            Log.d("TileLifecycleManager", "Handling pending onRemoved");
            if (this.mListening) {
                Log.w("TileLifecycleManager", "Managed to get remove in listening state...");
                onStopListening();
            }
            onTileRemoved();
        }
        if (this.mUnbindImmediate) {
            this.mUnbindImmediate = false;
            setBindService(false);
        }
    }

    public void handleDestroy() {
        Log.d("TileLifecycleManager", "handleDestroy, " + getComponent().getPackageName() + " " + this + ", user=" + this.mUser);
        if (this.mReceiverRegistered) {
            stopPackageListening();
        }
    }

    private void handleDeath(boolean retry) {
        boolean isScreenOn = this.mPowerManager != null ? this.mPowerManager.isScreenOn() : true;
        Log.d("TileLifecycleManager", "handleDeath, isScreenOn=" + isScreenOn + ", retry=" + retry + ", mBound=" + this.mBound + ", mWrapper=" + this.mWrapper);
        if (this.mChangeListener != null) {
            this.mChangeListener.onBindFailed(this.mIntent.getComponent());
        }
        this.mWrapper = null;
        this.mHandler.removeCallbacks(this.mRetry);
        if (checkComponentState() && isScreenOn && retry && this.mBound) {
            this.mRetry = new Runnable() {
                public void run() {
                    if (TileLifecycleManager.this.mBound) {
                        TileLifecycleManager.this.setBindService(true);
                    }
                }
            };
            this.mHandler.postDelayed(this.mRetry, 1000);
        }
    }

    private boolean checkComponentState() {
        PackageManager pm = this.mContext.getPackageManager();
        if (isPackageAvailable(pm) && isComponentAvailable(pm)) {
            return true;
        }
        startPackageListening();
        return false;
    }

    private void startPackageListening() {
        Log.d("TileLifecycleManager", "startPackageListening");
        if (!this.mReceiverRegistered) {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this, this.mUser, filter, null, this.mHandler);
            this.mContext.registerReceiverAsUser(this, this.mUser, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, this.mHandler);
            this.mReceiverRegistered = true;
        }
    }

    private void stopPackageListening() {
        Log.d("TileLifecycleManager", "stopPackageListening");
        if (this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this);
            this.mReceiverRegistered = false;
        }
    }

    public void setTileChangeListener(TileChangeListener changeListener) {
        this.mChangeListener = changeListener;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("TileLifecycleManager", "onReceive: " + intent);
        if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) || Objects.equal(intent.getData().getEncodedSchemeSpecificPart(), this.mIntent.getComponent().getPackageName())) {
            if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction()) && this.mChangeListener != null) {
                this.mChangeListener.onTileChanged(this.mIntent.getComponent());
            }
            stopPackageListening();
            if (this.mBound) {
                Log.d("TileLifecycleManager", "Trying to rebind");
                setBindService(true);
            }
        }
    }

    private boolean isComponentAvailable(PackageManager pm) {
        boolean z = false;
        String packageName = this.mIntent.getComponent().getPackageName();
        try {
            ServiceInfo si = AppGlobals.getPackageManager().getServiceInfo(this.mIntent.getComponent(), 0, this.mUser.getIdentifier());
            if (si == null) {
                Log.d("TileLifecycleManager", "Can't find component " + this.mIntent.getComponent());
            }
            if (si != null) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean isPackageAvailable(PackageManager pm) {
        String packageName = this.mIntent.getComponent().getPackageName();
        try {
            pm.getPackageInfoAsUser(packageName, 0, this.mUser.getIdentifier());
            return true;
        } catch (NameNotFoundException e) {
            Log.d("TileLifecycleManager", "Package not available: " + packageName, e);
            return false;
        }
    }

    private void queueMessage(int message) {
        synchronized (this.mQueuedMessages) {
            this.mQueuedMessages.add(Integer.valueOf(message));
        }
    }

    public void onTileAdded() {
        Log.d("TileLifecycleManager", "onTileAdded, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        if (this.mWrapper == null || !this.mWrapper.onTileAdded()) {
            queueMessage(0);
            handleDeath(true);
        }
    }

    public void onTileRemoved() {
        Log.d("TileLifecycleManager", "onTileRemoved, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        if (this.mWrapper == null || !this.mWrapper.onTileRemoved()) {
            queueMessage(1);
            handleDeath(false);
        }
    }

    public void onStartListening() {
        Log.d("TileLifecycleManager", "onStartListening, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        this.mListening = true;
        if (this.mWrapper != null && !this.mWrapper.onStartListening()) {
            handleDeath(true);
        }
    }

    public void onStopListening() {
        Log.d("TileLifecycleManager", "onStopListening, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        this.mListening = false;
        if (this.mWrapper != null && !this.mWrapper.onStopListening()) {
            handleDeath(false);
        }
    }

    public void onClick(IBinder iBinder) {
        Log.d("TileLifecycleManager", "onClick " + iBinder + " " + this.mUser + ", " + getComponent().getPackageName() + ", mWrapper=" + this.mWrapper + ", " + this);
        if (this.mWrapper == null || !this.mWrapper.onClick(iBinder)) {
            this.mClickBinder = iBinder;
            queueMessage(2);
            handleDeath(true);
        }
    }

    public void onUnlockComplete() {
        Log.d("TileLifecycleManager", "onUnlockComplete, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        if (this.mWrapper == null || !this.mWrapper.onUnlockComplete()) {
            queueMessage(3);
            handleDeath(false);
        }
    }

    public IBinder asBinder() {
        return this.mWrapper != null ? this.mWrapper.asBinder() : null;
    }

    public void binderDied() {
        Log.d("TileLifecycleManager", "binderDeath, " + getComponent().getPackageName() + this + ", user=" + this.mUser);
        handleDeath(false);
    }
}
