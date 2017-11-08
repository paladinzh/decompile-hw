package com.android.systemui.qs.external;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.quicksettings.IQSTileService;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.external.TileLifecycleManager.TileChangeListener;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import libcore.util.Objects;

public class CustomTile extends QSTile<State> implements TileChangeListener {
    private final ComponentName mComponent;
    private Icon mDefaultIcon;
    private boolean mIsShowingDialog;
    private boolean mIsTokenGranted;
    private boolean mListening;
    private final IQSTileService mService;
    private final TileServiceManager mServiceManager;
    private final Tile mTile;
    private final IBinder mToken = new Binder();
    private final int mUser;
    private final IWindowManager mWindowManager = WindowManagerGlobal.getWindowManagerService();

    private CustomTile(QSTileHost host, String action) {
        super(host);
        this.mComponent = ComponentName.unflattenFromString(action);
        this.mTile = new Tile(this.mComponent);
        setTileIcon();
        this.mServiceManager = host.getTileServices().getTileWrapper(this);
        this.mService = this.mServiceManager.getTileService();
        this.mServiceManager.setTileChangeListener(this);
        this.mUser = UserSwitchUtils.getCurrentUser();
    }

    private void setTileIcon() {
        try {
            int icon;
            boolean iconEquals;
            Icon createWithResource;
            PackageManager pm = this.mContext.getPackageManager();
            ServiceInfo info = pm.getServiceInfo(this.mComponent, 786432);
            if (info.icon != 0) {
                icon = info.icon;
            } else {
                icon = info.applicationInfo.icon;
            }
            if (this.mTile.getIcon() != null) {
                iconEquals = iconEquals(this.mTile.getIcon(), this.mDefaultIcon);
            } else {
                iconEquals = true;
            }
            if (icon != 0) {
                createWithResource = Icon.createWithResource(this.mComponent.getPackageName(), icon);
            } else {
                createWithResource = null;
            }
            this.mDefaultIcon = createWithResource;
            if (iconEquals) {
                this.mTile.setIcon(this.mDefaultIcon);
            }
            if (this.mTile.getLabel() == null) {
                this.mTile.setLabel(info.loadLabel(pm));
            }
        } catch (Exception e) {
            HwLog.w(this.TAG, "setTileIcon: " + e);
            this.mDefaultIcon = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean iconEquals(Icon icon1, Icon icon2) {
        if (icon1 == icon2) {
            return true;
        }
        return icon1 != null && icon2 != null && icon1.getType() == 2 && icon2.getType() == 2 && icon1.getResId() == icon2.getResId() && Objects.equal(icon1.getResPackage(), icon2.getResPackage());
    }

    public void onTileChanged(ComponentName tile) {
        setTileIcon();
    }

    public void onBindFailed(ComponentName tile) {
        this.mServiceManager.onBindFailed();
    }

    public boolean isAvailable() {
        return this.mDefaultIcon != null;
    }

    public int getUser() {
        return this.mUser;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public Tile getQsTile() {
        return this.mTile;
    }

    public void updateState(Tile tile) {
        this.mTile.setIcon(tile.getIcon());
        this.mTile.setLabel(tile.getLabel());
        this.mTile.setContentDescription(tile.getContentDescription());
        this.mTile.setState(tile.getState());
    }

    public void onDialogShown() {
        this.mIsShowingDialog = true;
    }

    public void onDialogHidden() {
        this.mIsShowingDialog = false;
        try {
            this.mWindowManager.removeWindowToken(this.mToken);
        } catch (RemoteException e) {
        }
    }

    protected void setListening(boolean listening) {
        HwLog.i(this.TAG, "setListening: " + listening + ", mListening=" + this.mListening + ", mComponent=" + this.mComponent.getPackageName() + ", user=" + this.mUser);
        if (this.mListening != listening) {
            this.mListening = listening;
            if (listening) {
                try {
                    setTileIcon();
                    refreshState();
                    if (!this.mServiceManager.isActiveTile()) {
                        this.mServiceManager.setBindRequested(true);
                        this.mService.onStartListening();
                    }
                } catch (RemoteException e) {
                }
            } else {
                this.mService.onStopListening();
                if (this.mIsTokenGranted && !this.mIsShowingDialog) {
                    try {
                        this.mWindowManager.removeWindowToken(this.mToken);
                    } catch (RemoteException e2) {
                    }
                    this.mIsTokenGranted = false;
                }
                this.mIsShowingDialog = false;
                this.mServiceManager.setBindRequested(false);
            }
        }
    }

    public void handleDestroy() {
        super.handleDestroy();
        if (this.mIsTokenGranted) {
            try {
                this.mWindowManager.removeWindowToken(this.mToken);
            } catch (RemoteException e) {
            }
        }
        this.mHost.getTileServices().freeService(this, this.mServiceManager);
    }

    public State newTileState() {
        return new State();
    }

    public Intent getLongClickIntent() {
        Intent i = new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES");
        i.setPackage(this.mComponent.getPackageName());
        i = resolveIntent(i);
        if (i != null) {
            return i;
        }
        return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", this.mComponent.getPackageName(), null));
    }

    private Intent resolveIntent(Intent i) {
        ResolveInfo result = this.mContext.getPackageManager().resolveActivityAsUser(i, 0, UserSwitchUtils.getCurrentUser());
        if (result != null) {
            return new Intent("android.service.quicksettings.action.QS_TILE_PREFERENCES").setClassName(result.activityInfo.packageName, result.activityInfo.name);
        }
        return null;
    }

    protected void handleClick() {
        if (this.mTile.getState() != 0) {
            try {
                this.mWindowManager.addWindowToken(this.mToken, 2035);
                this.mIsTokenGranted = true;
            } catch (RemoteException e) {
            }
            try {
                if (this.mServiceManager.isActiveTile()) {
                    this.mServiceManager.setBindRequested(true);
                    this.mService.onStartListening();
                }
                this.mService.onClick(this.mToken);
            } catch (RemoteException e2) {
            }
            MetricsLogger.action(this.mContext, getMetricsCategory(), this.mComponent.getPackageName());
        }
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    protected void handleUpdateState(State state, Object arg) {
        Drawable drawable;
        int i = 0;
        int tileState = this.mTile.getState();
        if (this.mServiceManager.hasPendingBind()) {
            tileState = 0;
        }
        try {
            drawable = this.mTile.getIcon().loadDrawable(this.mContext);
        } catch (Exception e) {
            Log.w(this.TAG, "Invalid icon, forcing into unavailable state");
            tileState = 0;
            drawable = this.mDefaultIcon.loadDrawable(this.mContext);
        }
        drawable.setTint(this.mContext.getColor(getColor(tileState)));
        state.icon = new DrawableIcon(drawable);
        state.label = this.mTile.getLabel();
        if (tileState == 0) {
            state.labelTint = 2;
        } else {
            if (2 == tileState) {
                i = 1;
            }
            state.labelTint = i;
        }
        if (this.mTile.getContentDescription() != null) {
            state.contentDescription = this.mTile.getContentDescription();
        } else {
            state.contentDescription = state.label;
        }
    }

    public int getMetricsCategory() {
        return 268;
    }

    public void startUnlockAndRun() {
        this.mHost.startRunnableDismissingKeyguard(new Runnable() {
            public void run() {
                try {
                    CustomTile.this.mService.onUnlockComplete();
                } catch (RemoteException e) {
                }
            }
        });
    }

    private static int getColor(int state) {
        switch (state) {
            case 0:
                return R.color.qs_tile_tint_disable;
            case 1:
                return R.color.qs_tile_tint_off;
            case 2:
                return R.color.qs_tile_tint_on;
            default:
                return 0;
        }
    }

    public static String toSpec(ComponentName name) {
        return "custom(" + name.flattenToShortString() + ")";
    }

    public static ComponentName getComponentFromSpec(String spec) {
        String action = spec.substring("custom(".length(), spec.length() - 1);
        if (!action.isEmpty()) {
            return ComponentName.unflattenFromString(action);
        }
        throw new IllegalArgumentException("Empty custom tile spec action");
    }

    public static QSTile<?> create(QSTileHost host, String spec) {
        if (spec != null && spec.startsWith("custom(") && spec.endsWith(")")) {
            String action = spec.substring("custom(".length(), spec.length() - 1);
            if (!action.isEmpty()) {
                return new CustomTile(host, action);
            }
            throw new IllegalArgumentException("Empty custom tile spec action");
        }
        throw new IllegalArgumentException("Bad custom tile spec: " + spec);
    }
}
