package com.android.systemui.recents;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.IRecentsSystemUserCallbacks.Stub;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.tv.RecentsTvImpl;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;

public class Recents extends SystemUI implements RecentsComponent {
    private static RecentsConfiguration sConfiguration;
    private static RecentsDebugFlags sDebugFlags;
    private static SystemServicesProxy sSystemServicesProxy;
    private static RecentsTaskLoader sTaskLoader;
    private int mDraggingInRecentsCurrentUser;
    private Handler mHandler;
    private RecentsImpl mImpl;
    private final ArrayList<Runnable> mOnConnectRunnables = new ArrayList();
    private String mOverrideRecentsPackageName;
    private RecentsSystemUser mSystemToUserCallbacks;
    private IRecentsSystemUserCallbacks mUserToSystemCallbacks;
    private final DeathRecipient mUserToSystemCallbacksDeathRcpt = new DeathRecipient() {
        public void binderDied() {
            Recents.this.mUserToSystemCallbacks = null;
            EventLog.writeEvent(36060, new Object[]{Integer.valueOf(3), Integer.valueOf(Recents.sSystemServicesProxy.getProcessUser())});
            Recents.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Recents.this.registerWithSystemUser();
                }
            }, 5000);
        }
    };
    private final ServiceConnection mUserToSystemServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                Recents.this.mUserToSystemCallbacks = Stub.asInterface(service);
                EventLog.writeEvent(36060, new Object[]{Integer.valueOf(2), Integer.valueOf(Recents.sSystemServicesProxy.getProcessUser())});
                try {
                    service.linkToDeath(Recents.this.mUserToSystemCallbacksDeathRcpt, 0);
                } catch (RemoteException e) {
                    Log.e("Recents", "Lost connection to (System) SystemUI", e);
                }
                Recents.this.runAndFlushOnConnectRunnables();
            }
            Recents.this.mContext.unbindService(this);
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public IBinder getSystemUserCallbacks() {
        return this.mSystemToUserCallbacks;
    }

    public static RecentsTaskLoader getTaskLoader() {
        return sTaskLoader;
    }

    public static SystemServicesProxy getSystemServices() {
        return sSystemServicesProxy;
    }

    public static RecentsConfiguration getConfiguration() {
        return sConfiguration;
    }

    public static RecentsDebugFlags getDebugFlags() {
        return sDebugFlags;
    }

    public void start() {
        sDebugFlags = new RecentsDebugFlags(this.mContext);
        sSystemServicesProxy = SystemServicesProxy.getInstance(this.mContext);
        sTaskLoader = new RecentsTaskLoader(this.mContext);
        sConfiguration = new RecentsConfiguration(this.mContext);
        this.mHandler = new Handler();
        if (((UiModeManager) this.mContext.getSystemService("uimode")).getCurrentModeType() == 4) {
            this.mImpl = new RecentsTvImpl(this.mContext);
        } else {
            this.mImpl = new RecentsImpl(this.mContext);
        }
        if ("userdebug".equals(Build.TYPE) || "eng".equals(Build.TYPE)) {
            String cnStr = SystemProperties.get("persist.recents_override_pkg");
            if (!cnStr.isEmpty()) {
                this.mOverrideRecentsPackageName = cnStr;
            }
        }
        EventBus.getDefault().register(this, 1);
        EventBus.getDefault().register(sSystemServicesProxy, 1);
        EventBus.getDefault().register(sTaskLoader, 1);
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            this.mSystemToUserCallbacks = new RecentsSystemUser(this.mContext, this.mImpl);
        } else {
            registerWithSystemUser();
        }
        putComponent(Recents.class, this);
    }

    public void onBootCompleted() {
        this.mImpl.onBootCompleted();
    }

    public void showRecents(boolean triggeredFromAltTab, boolean fromHome) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_SHOW")) {
            int recentsGrowTarget = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.showRecents(triggeredFromAltTab, false, true, false, fromHome, recentsGrowTarget);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.showRecents(triggeredFromAltTab, false, true, false, fromHome, recentsGrowTarget);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_HIDE")) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.hideRecents(triggeredFromAltTab, triggeredFromHomeKey);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.hideRecents(triggeredFromAltTab, triggeredFromHomeKey);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void toggleRecents(Display display) {
        if (isUserSetup() && !proxyToOverridePackage("com.android.systemui.recents.ACTION_TOGGLE")) {
            int growTarget = ((Divider) getComponent(Divider.class)).getView().growsRecents();
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.toggleRecents(growTarget);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.toggleRecents(growTarget);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void preloadRecents() {
        if (isUserSetup()) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.preloadRecents();
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.preloadRecents();
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    public void cancelPreloadingRecents() {
        if (isUserSetup()) {
            int currentUser = sSystemServicesProxy.getCurrentUser();
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.cancelPreloadingRecents();
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.cancelPreloadingRecents();
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
        }
    }

    private boolean isNotResizable(String pkgName) {
        if ("com.huawei.systemmanager".equals(pkgName)) {
            return true;
        }
        if (Constants.IS_TABLET && "com.huawei.camera".equals(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean dockTopTask(int dragMode, int stackCreateMode, Rect initialBounds, int metricsDockAction) {
        if (!isUserSetup()) {
            return false;
        }
        Point realSize = new Point();
        if (initialBounds == null) {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(0).getRealSize(realSize);
            Rect rect = new Rect(0, 0, realSize.x, realSize.y);
        }
        int currentUser = sSystemServicesProxy.getCurrentUser();
        SystemServicesProxy ssp = getSystemServices();
        RunningTaskInfo runningTask = ssp.getRunningTask();
        boolean screenPinningActive = ssp.isScreenPinningActive();
        boolean isHomeStack;
        if (runningTask != null) {
            isHomeStack = SystemServicesProxy.isHomeStack(runningTask.stackId);
        } else {
            isHomeStack = false;
        }
        if (runningTask == null || r4 || screenPinningActive) {
            SystemUiUtil.showToastForAllUser(this.mContext, R.string.split_app_long_press_overview_message);
            return false;
        }
        logDockAttempt(this.mContext, runningTask.topActivity, runningTask.resizeMode);
        if (!runningTask.isDockable) {
            SystemUiUtil.showToastForAllUser(this.mContext, R.string.recents_incompatible_app_message);
            BDReporter.e(this.mContext, 336, "status : false");
            return false;
        } else if (isNotResizable(runningTask.topActivity.getPackageName())) {
            SystemUiUtil.showToastForAllUser(this.mContext, R.string.recents_incompatible_app_message);
            return false;
        } else {
            if (metricsDockAction != -1) {
                MetricsLogger.action(this.mContext, metricsDockAction, runningTask.topActivity.flattenToShortString());
            }
            if (sSystemServicesProxy.isSystemUser(currentUser)) {
                this.mImpl.dockTopTask(runningTask.id, dragMode, stackCreateMode, initialBounds);
            } else if (this.mSystemToUserCallbacks != null) {
                IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
                if (callbacks != null) {
                    try {
                        callbacks.dockTopTask(runningTask.id, dragMode, stackCreateMode, initialBounds);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                } else {
                    Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
                }
            }
            this.mDraggingInRecentsCurrentUser = currentUser;
            BDReporter.e(this.mContext, 336, "status : true");
            return true;
        }
    }

    public static void logDockAttempt(Context ctx, ComponentName activity, int resizeMode) {
        if (resizeMode == 0) {
            MetricsLogger.action(ctx, 391, activity.flattenToShortString());
        }
        MetricsLogger.count(ctx, getMetricsCounterForResizeMode(resizeMode), 1);
    }

    private static String getMetricsCounterForResizeMode(int resizeMode) {
        switch (resizeMode) {
            case 2:
            case 3:
                return "window_enter_supported";
            case 4:
                return "window_enter_unsupported";
            default:
                return "window_enter_incompatible";
        }
    }

    public void onDraggingInRecents(float distanceFromTop) {
        if (sSystemServicesProxy.isSystemUser(this.mDraggingInRecentsCurrentUser)) {
            this.mImpl.onDraggingInRecents(distanceFromTop);
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(this.mDraggingInRecentsCurrentUser);
            if (callbacks != null) {
                try {
                    callbacks.onDraggingInRecents(distanceFromTop);
                    return;
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                    return;
                }
            }
            Log.e("Recents", "No SystemUI callbacks found for user: " + this.mDraggingInRecentsCurrentUser);
        }
    }

    public void onDraggingInRecentsEnded(float velocity) {
        if (sSystemServicesProxy.isSystemUser(this.mDraggingInRecentsCurrentUser)) {
            this.mImpl.onDraggingInRecentsEnded(velocity);
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(this.mDraggingInRecentsCurrentUser);
            if (callbacks != null) {
                try {
                    callbacks.onDraggingInRecentsEnded(velocity);
                    return;
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                    return;
                }
            }
            Log.e("Recents", "No SystemUI callbacks found for user: " + this.mDraggingInRecentsCurrentUser);
        }
    }

    public void showNextAffiliatedTask() {
        if (isUserSetup()) {
            this.mImpl.showNextAffiliatedTask();
        }
    }

    public void showPrevAffiliatedTask() {
        if (isUserSetup()) {
            this.mImpl.showPrevAffiliatedTask();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        int currentUser = sSystemServicesProxy.getCurrentUser();
        if (sSystemServicesProxy.isSystemUser(currentUser)) {
            this.mImpl.onConfigurationChanged();
        } else if (this.mSystemToUserCallbacks != null) {
            IRecentsNonSystemUserCallbacks callbacks = this.mSystemToUserCallbacks.getNonSystemUserRecentsForUser(currentUser);
            if (callbacks != null) {
                try {
                    callbacks.onConfigurationChanged();
                    return;
                } catch (RemoteException e) {
                    Log.e("Recents", "Callback failed", e);
                    return;
                }
            }
            Log.e("Recents", "No SystemUI callbacks found for user: " + currentUser);
        }
    }

    public final void onBusEvent(final RecentsVisibilityChangedEvent event) {
        SystemServicesProxy ssp = getSystemServices();
        if (ssp.isSystemUser(ssp.getProcessUser())) {
            this.mImpl.onVisibilityChanged(event.applicationContext, event.visible);
        } else {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        Recents.this.mUserToSystemCallbacks.updateRecentsVisibility(event.visible);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(final ScreenPinningRequestEvent event) {
        if (sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            this.mImpl.onStartScreenPinning(event.applicationContext, event.taskId);
        } else {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        Recents.this.mUserToSystemCallbacks.startScreenPinning(event.taskId);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(RecentsDrawnEvent event) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        Recents.this.mUserToSystemCallbacks.sendRecentsDrawnEvent();
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(final DockedTopTaskEvent event) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        Recents.this.mUserToSystemCallbacks.sendDockingTopTaskEvent(event.dragMode, event.initialRect);
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(RecentsActivityStartingEvent event) {
        if (!sSystemServicesProxy.isSystemUser(sSystemServicesProxy.getProcessUser())) {
            postToSystemUser(new Runnable() {
                public void run() {
                    try {
                        Recents.this.mUserToSystemCallbacks.sendLaunchRecentsEvent();
                    } catch (RemoteException e) {
                        Log.e("Recents", "Callback failed", e);
                    }
                }
            });
        }
    }

    public final void onBusEvent(ConfigurationChangedEvent event) {
        this.mImpl.onConfigurationChanged();
    }

    private void registerWithSystemUser() {
        final int processUser = sSystemServicesProxy.getProcessUser();
        postToSystemUser(new Runnable() {
            public void run() {
                try {
                    Recents.this.mUserToSystemCallbacks.registerNonSystemUserCallbacks(new RecentsImplProxy(Recents.this.mImpl), processUser);
                } catch (RemoteException e) {
                    Log.e("Recents", "Failed to register", e);
                }
            }
        });
    }

    private void postToSystemUser(Runnable onConnectRunnable) {
        this.mOnConnectRunnables.add(onConnectRunnable);
        if (this.mUserToSystemCallbacks == null) {
            Intent systemUserServiceIntent = new Intent();
            systemUserServiceIntent.setClass(this.mContext, RecentsSystemUserService.class);
            boolean bound = this.mContext.bindServiceAsUser(systemUserServiceIntent, this.mUserToSystemServiceConnection, 1, UserHandle.SYSTEM);
            EventLog.writeEvent(36060, new Object[]{Integer.valueOf(1), Integer.valueOf(sSystemServicesProxy.getProcessUser())});
            if (!bound) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        Recents.this.registerWithSystemUser();
                    }
                }, 5000);
                return;
            }
            return;
        }
        runAndFlushOnConnectRunnables();
    }

    private void runAndFlushOnConnectRunnables() {
        for (Runnable r : this.mOnConnectRunnables) {
            r.run();
        }
        this.mOnConnectRunnables.clear();
    }

    private boolean isUserSetup() {
        ContentResolver cr = this.mContext.getContentResolver();
        if (Global.getInt(cr, "device_provisioned", 0) == 0 || Secure.getInt(cr, "user_setup_complete", 0) == 0) {
            return false;
        }
        return true;
    }

    private boolean proxyToOverridePackage(String action) {
        if (this.mOverrideRecentsPackageName == null) {
            return false;
        }
        Intent intent = new Intent(action);
        intent.setPackage(this.mOverrideRecentsPackageName);
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent);
        return true;
    }
}
