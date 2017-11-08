package com.android.systemui.statusbar.policy;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.ArrayList;
import java.util.List;

public class LocationControllerImpl extends BroadcastReceiver implements LocationController {
    private static final int[] mHighPowerRequestAppOpArray = new int[]{42};
    private AppOpsManager mAppOpsManager;
    private boolean mAreActiveLocationRequests;
    private Context mContext;
    private final H mHandler = new H();
    private ArrayList<LocationSettingsChangeCallback> mSettingsChangeCallbacks = new ArrayList();
    public final String mSlotLocation;
    private StatusBarManager mStatusBarManager;
    private Object mSyncObj = new Object();

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    locationSettingsChanged();
                    return;
                default:
                    return;
            }
        }

        private void locationSettingsChanged() {
            boolean isEnabled = LocationControllerImpl.this.isLocationEnabled();
            synchronized (LocationControllerImpl.this.mSyncObj) {
                ArrayList<LocationSettingsChangeCallback> mTempList = (ArrayList) LocationControllerImpl.this.mSettingsChangeCallbacks.clone();
            }
            for (LocationSettingsChangeCallback cb : mTempList) {
                cb.onLocationSettingsChanged(isEnabled);
            }
        }
    }

    public LocationControllerImpl(Context context, Looper bgLooper) {
        this.mContext = context;
        this.mSlotLocation = this.mContext.getString(17039394);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.HIGH_POWER_REQUEST_CHANGE");
        filter.addAction("android.location.MODE_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, new Handler(bgLooper));
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        updateActiveLocationRequests();
        refreshViews();
    }

    public void addSettingsChangedCallback(LocationSettingsChangeCallback cb) {
        synchronized (this.mSyncObj) {
            this.mSettingsChangeCallbacks.add(cb);
        }
        this.mHandler.sendEmptyMessage(1);
    }

    public void removeSettingsChangedCallback(LocationSettingsChangeCallback cb) {
        synchronized (this.mSyncObj) {
            this.mSettingsChangeCallbacks.remove(cb);
        }
    }

    public boolean setLocationEnabled(boolean enabled) {
        int currentUserId = UserSwitchUtils.getCurrentUser();
        if (isUserLocationRestricted(currentUserId)) {
            return false;
        }
        return Secure.putIntForUser(this.mContext.getContentResolver(), "location_mode", enabled ? -1 : 0, currentUserId);
    }

    public boolean isLocationEnabled() {
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, UserSwitchUtils.getCurrentUser()) != 0) {
            return true;
        }
        return false;
    }

    private boolean isUserLocationRestricted(int userId) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_share_location", UserHandle.of(userId));
    }

    private boolean areActiveHighPowerLocationRequests() {
        List<PackageOps> packages = this.mAppOpsManager.getPackagesForOps(mHighPowerRequestAppOpArray);
        if (packages != null) {
            int numPackages = packages.size();
            for (int packageInd = 0; packageInd < numPackages; packageInd++) {
                List<OpEntry> opEntries = ((PackageOps) packages.get(packageInd)).getOps();
                if (opEntries != null) {
                    int numOps = opEntries.size();
                    for (int opInd = 0; opInd < numOps; opInd++) {
                        OpEntry opEntry = (OpEntry) opEntries.get(opInd);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }

    private void refreshViews() {
        if (this.mAreActiveLocationRequests) {
            this.mStatusBarManager.setIcon(this.mSlotLocation, R.drawable.stat_sys_location, 0, this.mContext.getString(R.string.accessibility_location_active));
        } else {
            this.mStatusBarManager.removeIcon(this.mSlotLocation);
        }
    }

    private void updateActiveLocationRequests() {
        boolean hadActiveLocationRequests = this.mAreActiveLocationRequests;
        this.mAreActiveLocationRequests = areActiveHighPowerLocationRequests();
        if (this.mAreActiveLocationRequests != hadActiveLocationRequests) {
            refreshViews();
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
            updateActiveLocationRequests();
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            this.mHandler.sendEmptyMessage(1);
        }
    }
}
