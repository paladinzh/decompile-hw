package com.android.systemui.qs.external;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import libcore.util.Objects;

public class TileServiceManager {
    @VisibleForTesting
    static final String PREFS_FILE = "CustomTileModes";
    private boolean mBindAllowed;
    private boolean mBindRequested;
    private boolean mBound;
    private final Handler mHandler;
    private boolean mJustBound;
    @VisibleForTesting
    final Runnable mJustBoundOver;
    private long mLastUpdate;
    private boolean mPendingBind;
    private PowerManager mPowerManager;
    private int mPriority;
    private final TileServices mServices;
    private boolean mShowingDialog;
    private final TileLifecycleManager mStateManager;
    private final Runnable mUnbind;
    private final BroadcastReceiver mUninstallReceiver;

    TileServiceManager(TileServices tileServices, Handler handler, ComponentName component, Tile tile) {
        this(tileServices, handler, new TileLifecycleManager(handler, tileServices.getContext(), tileServices, tile, new Intent().setComponent(component), new UserHandle(ActivityManager.getCurrentUser())));
    }

    @VisibleForTesting
    TileServiceManager(TileServices tileServices, Handler handler, TileLifecycleManager tileLifecycleManager) {
        this.mPendingBind = true;
        this.mPowerManager = null;
        this.mUnbind = new Runnable() {
            public void run() {
                if (TileServiceManager.this.mBound && !TileServiceManager.this.mBindRequested) {
                    TileServiceManager.this.unbindService();
                }
            }
        };
        this.mJustBoundOver = new Runnable() {
            public void run() {
                TileServiceManager.this.mJustBound = false;
                TileServiceManager.this.mServices.recalculateBindAllowance();
            }
        };
        this.mUninstallReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                    String pkgName = intent.getData().getEncodedSchemeSpecificPart();
                    ComponentName component = TileServiceManager.this.mStateManager.getComponent();
                    if (Objects.equal(pkgName, component.getPackageName())) {
                        if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            Intent queryIntent = new Intent("android.service.quicksettings.action.QS_TILE");
                            queryIntent.setPackage(pkgName);
                            for (ResolveInfo info : context.getPackageManager().queryIntentServicesAsUser(queryIntent, 0, ActivityManager.getCurrentUser())) {
                                if (Objects.equal(info.serviceInfo.packageName, component.getPackageName()) && Objects.equal(info.serviceInfo.name, component.getClassName())) {
                                    return;
                                }
                            }
                        }
                        TileServiceManager.this.mServices.getHost().removeTile(component);
                    }
                }
            }
        };
        this.mServices = tileServices;
        this.mHandler = handler;
        this.mStateManager = tileLifecycleManager;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.mServices.getContext().registerReceiverAsUser(this.mUninstallReceiver, new UserHandle(UserSwitchUtils.getCurrentUser()), filter, null, this.mHandler);
        this.mPowerManager = (PowerManager) this.mServices.getContext().getSystemService("power");
    }

    public void setTileChangeListener(TileChangeListener changeListener) {
        this.mStateManager.setTileChangeListener(changeListener);
    }

    public boolean isActiveTile() {
        return this.mStateManager.isActiveTile();
    }

    public void setShowingDialog(boolean dialog) {
        this.mShowingDialog = dialog;
    }

    public IQSTileService getTileService() {
        return this.mStateManager;
    }

    public void setBindRequested(boolean bindRequested) {
        HwLog.i("TileServiceManager", "setBindRequested: bindRequested=" + bindRequested + ", mBindRequested=" + this.mBindRequested + ", mBindAllowed=" + this.mBindAllowed + ", mBound=" + this.mBound);
        if (this.mBindRequested != bindRequested) {
            this.mBindRequested = bindRequested;
            if (this.mBindAllowed && this.mBindRequested && !this.mBound) {
                this.mHandler.removeCallbacks(this.mUnbind);
                bindService();
            } else {
                this.mServices.recalculateBindAllowance();
            }
            if (this.mBound && !this.mBindRequested) {
                this.mHandler.postDelayed(this.mUnbind, this.mPowerManager != null ? this.mPowerManager.isScreenOn() : true ? 30000 : 3000);
            }
        }
    }

    public void setLastUpdate(long lastUpdate) {
        this.mLastUpdate = lastUpdate;
        if (this.mBound && isActiveTile()) {
            this.mStateManager.onStopListening();
            setBindRequested(false);
        }
        this.mServices.recalculateBindAllowance();
    }

    public void handleDestroy() {
        this.mServices.getContext().unregisterReceiver(this.mUninstallReceiver);
        this.mStateManager.handleDestroy();
    }

    public void setBindAllowed(boolean allowed) {
        HwLog.i("TileServiceManager", "setBindAllowed: mBindAllowed=" + this.mBindAllowed + ", allowed=" + allowed + ", mBindRequested=" + this.mBindRequested + ", mBound=" + this.mBound);
        if (this.mBindAllowed != allowed) {
            this.mBindAllowed = allowed;
            if (!this.mBindAllowed && this.mBound) {
                unbindService();
            } else if (this.mBindAllowed && this.mBindRequested && !this.mBound) {
                bindService();
            }
        }
    }

    public boolean hasPendingBind() {
        return this.mPendingBind;
    }

    public void clearPendingBind() {
        this.mPendingBind = false;
    }

    private void bindService() {
        if (this.mBound) {
            Log.e("TileServiceManager", "Service already bound");
            return;
        }
        this.mPendingBind = true;
        this.mBound = true;
        this.mJustBound = true;
        this.mHandler.postDelayed(this.mJustBoundOver, 5000);
        this.mStateManager.setBindService(true);
    }

    private void unbindService() {
        if (this.mBound) {
            this.mBound = false;
            this.mJustBound = false;
            this.mStateManager.setBindService(false);
            return;
        }
        Log.e("TileServiceManager", "Service not bound");
    }

    public void calculateBindPriority(long currentTime) {
        if (this.mStateManager.hasPendingClick()) {
            this.mPriority = Integer.MAX_VALUE;
        } else if (this.mShowingDialog) {
            this.mPriority = 2147483646;
        } else if (this.mJustBound) {
            this.mPriority = 2147483645;
        } else if (this.mBindRequested) {
            long timeSinceUpdate = currentTime - this.mLastUpdate;
            if (timeSinceUpdate > 2147483644) {
                this.mPriority = 2147483644;
            } else {
                this.mPriority = (int) timeSinceUpdate;
            }
        } else {
            this.mPriority = Integer.MIN_VALUE;
        }
    }

    public int getBindPriority() {
        return this.mPriority;
    }

    public void onBindFailed() {
        HwLog.i("TileServiceManager", "onBindFailed");
        this.mBound = false;
    }
}
