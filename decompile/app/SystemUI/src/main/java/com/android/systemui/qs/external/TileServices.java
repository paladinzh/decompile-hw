package com.android.systemui.qs.external;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.quicksettings.IQSService.Stub;
import android.service.quicksettings.Tile;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TileServices extends Stub {
    private static final Comparator<TileServiceManager> SERVICE_SORT = new Comparator<TileServiceManager>() {
        public int compare(TileServiceManager left, TileServiceManager right) {
            return -Integer.compare(left.getBindPriority(), right.getBindPriority());
        }
    };
    private final Context mContext;
    private final Handler mHandler;
    private final QSTileHost mHost;
    private final Handler mMainHandler;
    private int mMaxBound = 10;
    private final BroadcastReceiver mRequestListeningReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.service.quicksettings.action.REQUEST_LISTENING".equals(intent.getAction())) {
                TileServices.this.requestListening((ComponentName) intent.getParcelableExtra("android.service.quicksettings.extra.COMPONENT"));
            }
        }
    };
    private final ArrayMap<CustomTile, TileServiceManager> mServices = new ArrayMap();
    private final ArrayMap<ComponentName, CustomTile> mTiles = new ArrayMap();

    public TileServices(QSTileHost host, Looper looper) {
        this.mHost = host;
        this.mContext = this.mHost.getContext();
        this.mContext.registerReceiver(this.mRequestListeningReceiver, new IntentFilter("android.service.quicksettings.action.REQUEST_LISTENING"));
        this.mHandler = new Handler(looper);
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    public Context getContext() {
        return this.mContext;
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public TileServiceManager getTileWrapper(CustomTile tile) {
        ComponentName component = tile.getComponent();
        TileServiceManager service = onCreateTileService(component, tile.getQsTile());
        synchronized (this.mServices) {
            this.mServices.put(tile, service);
            this.mTiles.put(component, tile);
        }
        return service;
    }

    protected TileServiceManager onCreateTileService(ComponentName component, Tile tile) {
        return new TileServiceManager(this, this.mHandler, component, tile);
    }

    public void freeService(CustomTile tile, TileServiceManager service) {
        synchronized (this.mServices) {
            service.setBindAllowed(false);
            service.handleDestroy();
            this.mServices.remove(tile);
            this.mTiles.remove(tile.getComponent());
            final String slot = tile.getComponent().getClassName();
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    TileServices.this.mHost.getIconController().removeIcon(slot);
                }
            });
        }
    }

    public void recalculateBindAllowance() {
        ArrayList<TileServiceManager> services;
        int i;
        synchronized (this.mServices) {
            services = new ArrayList(this.mServices.values());
        }
        int N = services.size();
        HwLog.i("TileServices", "recalculateBindAllowance: mMaxBound=" + this.mMaxBound + ", N=" + N);
        if (N > this.mMaxBound) {
            long currentTime = System.currentTimeMillis();
            for (i = 0; i < N; i++) {
                ((TileServiceManager) services.get(i)).calculateBindPriority(currentTime);
            }
            Collections.sort(services, SERVICE_SORT);
        }
        i = 0;
        while (i < this.mMaxBound && i < N) {
            ((TileServiceManager) services.get(i)).setBindAllowed(true);
            i++;
        }
        while (i < N) {
            ((TileServiceManager) services.get(i)).setBindAllowed(false);
            i++;
        }
    }

    private void verifyCaller(String packageName) {
        try {
            if (Binder.getCallingUid() != this.mContext.getPackageManager().getPackageUidAsUser(packageName, Binder.getCallingUserHandle().getIdentifier())) {
                throw new SecurityException("Component outside caller's uid");
            }
        } catch (NameNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    private void requestListening(ComponentName component) {
        synchronized (this.mServices) {
            CustomTile customTile = getTileForComponent(component);
            if (customTile == null) {
                Log.d("TileServices", "Couldn't find tile for " + component);
                return;
            }
            TileServiceManager service = (TileServiceManager) this.mServices.get(customTile);
            if (service.isActiveTile()) {
                service.setBindRequested(true);
                try {
                    service.getTileService().onStartListening();
                } catch (RemoteException e) {
                }
            } else {
                return;
            }
        }
    }

    public void updateQsTile(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "updateQsTile: " + componentName);
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            synchronized (this.mServices) {
                TileServiceManager tileServiceManager = (TileServiceManager) this.mServices.get(customTile);
                tileServiceManager.clearPendingBind();
                tileServiceManager.setLastUpdate(System.currentTimeMillis());
            }
            customTile.updateState(tile);
            customTile.refreshState();
        }
    }

    public void onStartSuccessful(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "onStartSuccessful: " + componentName);
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            synchronized (this.mServices) {
                ((TileServiceManager) this.mServices.get(customTile)).clearPendingBind();
            }
            customTile.refreshState();
        }
    }

    public void onShowDialog(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "onShowDialog: " + componentName);
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            customTile.onDialogShown();
            this.mHost.collapsePanels();
            ((TileServiceManager) this.mServices.get(customTile)).setShowingDialog(true);
        }
    }

    public void onDialogHidden(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "onDialogHidden: " + componentName);
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            ((TileServiceManager) this.mServices.get(customTile)).setShowingDialog(false);
            customTile.onDialogHidden();
        }
    }

    public void onStartActivity(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "onStartActivity: " + componentName);
        verifyCaller(componentName.getPackageName());
        if (getTileForComponent(componentName) != null) {
            this.mHost.collapsePanels();
        }
    }

    public void updateStatusIcon(Tile tile, Icon icon, String contentDescription) {
        final ComponentName componentName = tile.getComponentName();
        HwLog.i("TileServices", "updateStatusIcon: " + componentName);
        String packageName = componentName.getPackageName();
        verifyCaller(packageName);
        if (getTileForComponent(componentName) != null) {
            try {
                UserHandle userHandle = getCallingUserHandle();
                if (this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 0, userHandle.getIdentifier()).applicationInfo.isSystemApp()) {
                    StatusBarIcon statusBarIcon;
                    if (icon != null) {
                        statusBarIcon = new StatusBarIcon(userHandle, packageName, icon, 0, 0, contentDescription);
                    } else {
                        statusBarIcon = null;
                    }
                    this.mMainHandler.post(new Runnable() {
                        public void run() {
                            StatusBarIconController iconController = TileServices.this.mHost.getIconController();
                            iconController.setIcon(componentName.getClassName(), statusBarIcon);
                            iconController.setExternalIcon(componentName.getClassName());
                        }
                    });
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    public Tile getTile(ComponentName componentName) {
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            return customTile.getQsTile();
        }
        return null;
    }

    public void startUnlockAndRun(Tile tile) {
        ComponentName componentName = tile.getComponentName();
        verifyCaller(componentName.getPackageName());
        CustomTile customTile = getTileForComponent(componentName);
        if (customTile != null) {
            customTile.startUnlockAndRun();
        }
    }

    public boolean isLocked() {
        return this.mHost.getKeyguardMonitor().isShowing();
    }

    public boolean isSecure() {
        KeyguardMonitor keyguardMonitor = this.mHost.getKeyguardMonitor();
        return keyguardMonitor.isSecure() ? keyguardMonitor.isShowing() : false;
    }

    private CustomTile getTileForComponent(ComponentName component) {
        CustomTile customTile;
        synchronized (this.mServices) {
            customTile = (CustomTile) this.mTiles.get(component);
        }
        return customTile;
    }
}
