package com.android.settingslib.location;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManagerEx;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class RecentLocationApps {
    private static final int[] LOCATION_OPS = new int[]{41, 42};
    private static final String TAG = RecentLocationApps.class.getSimpleName();
    private final Context mContext;
    private final PackageManager mPackageManager;

    public static class Request {
        public final CharSequence contentDescription;
        public final Drawable icon;
        public final boolean isHighBattery;
        private boolean isLocating;
        public final CharSequence label;
        public final String packageName;
        public final UserHandle userHandle;

        private Request(String packageName, UserHandle userHandle, Drawable icon, CharSequence label, boolean isHighBattery, CharSequence contentDescription) {
            this.packageName = packageName;
            this.userHandle = userHandle;
            this.icon = icon;
            this.label = label;
            this.isHighBattery = isHighBattery;
            this.contentDescription = contentDescription;
        }

        public void setLocating(boolean locating) {
            this.isLocating = locating;
        }

        public boolean isLocating() {
            return this.isLocating;
        }
    }

    public RecentLocationApps(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
    }

    public List<Request> getAppList() {
        List<PackageOps> appOps = ((AppOpsManager) this.mContext.getSystemService("appops")).getPackagesForOps(LOCATION_OPS);
        int appOpsCount = appOps != null ? appOps.size() : 0;
        ArrayList<Request> requests = new ArrayList(appOpsCount);
        long now = System.currentTimeMillis();
        List<UserHandle> profiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        for (int i = 0; i < appOpsCount; i++) {
            PackageOps ops = (PackageOps) appOps.get(i);
            String packageName = ops.getPackageName();
            int uid = ops.getUid();
            int userId = UserHandle.getUserId(uid);
            if (!(uid == 1000 ? "android".equals(packageName) : false) && profiles.contains(new UserHandle(userId))) {
                Request request = getRequestFromOps(now, ops);
                if (request != null) {
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    private Request getRequestFromOps(long now, PackageOps ops) {
        RemoteException e;
        String packageName = ops.getPackageName();
        List<OpEntry> entries = ops.getOps();
        boolean highBattery = false;
        boolean normalBattery = false;
        boolean isRunning = false;
        boolean isLocationModeOn = Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0;
        long recentLocationCutoffTime = now - 900000;
        for (OpEntry entry : entries) {
            if (isLocationModeOn && entry.isRunning() && LocationManagerEx.getPowerTypeByPackageName(packageName) > 0) {
                isRunning = true;
            }
            if (entry.isRunning() || entry.getTime() >= recentLocationCutoffTime) {
                switch (entry.getOp()) {
                    case 41:
                        normalBattery = true;
                        break;
                    case 42:
                        highBattery = true;
                        break;
                    default:
                        break;
                }
            }
        }
        if (highBattery || normalBattery) {
            int userId = UserHandle.getUserId(ops.getUid());
            Request request;
            try {
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 128, userId);
                if (appInfo == null) {
                    Log.w(TAG, "Null application info retrieved for package " + packageName + ", userId " + userId);
                    return null;
                }
                UserHandle userHandle = new UserHandle(userId);
                Drawable icon = this.mPackageManager.getUserBadgedIcon(this.mPackageManager.getApplicationIcon(appInfo), userHandle);
                CharSequence appLabel = this.mPackageManager.getApplicationLabel(appInfo);
                CharSequence badgedAppLabel = this.mPackageManager.getUserBadgedLabel(appLabel, userHandle);
                if (appLabel.toString().contentEquals(badgedAppLabel)) {
                    badgedAppLabel = null;
                }
                request = new Request(packageName, userHandle, icon, appLabel, highBattery, badgedAppLabel);
                try {
                    request.setLocating(isRunning);
                } catch (RemoteException e2) {
                    e = e2;
                    Log.w(TAG, "Error while retrieving application info for package " + packageName + ", userId " + userId, e);
                    return request;
                }
                return request;
            } catch (RemoteException e3) {
                e = e3;
                request = null;
                Log.w(TAG, "Error while retrieving application info for package " + packageName + ", userId " + userId, e);
                return request;
            }
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, packageName + " hadn't used location within the time interval.");
        }
        return null;
    }
}
