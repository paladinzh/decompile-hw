package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlackListUtils {
    public static final AppFilter FILTER_FORBIDDEN = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            if ((entry.info.hwFlags & 268435456) == 0 || (entry.info.flags & 8388608) == 0) {
                return false;
            }
            return true;
        }
    };
    private static OnSharedPreferenceChangeListener sBlackListChangeListener;
    private static volatile AlertDialog sDialog;
    private static SharedPreferences sSharedPreference;
    private static volatile boolean sShowBlackListApp = false;

    static class BlackListSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {
        ManageApplications mApplications;
        FilterSpinnerAdapter mFilterAdapter;

        public BlackListSharedPreferenceChangeListener(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
            this.mApplications = applications;
            this.mFilterAdapter = filterAdapter;
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (sharedPreferences != null) {
                Map<String, ?> disableStatusMap = sharedPreferences.getAll();
                if (disableStatusMap != null) {
                    BlackListUtils.setHasForbidden(this.mApplications, this.mFilterAdapter, disableStatusMap.size() > 0);
                }
            }
        }
    }

    public static ArrayList<String> getBlackListApps() {
        List pkgInfoList = null;
        try {
            ParceledListSlice<PackageInfo> packages = AppGlobals.getPackageManager().getInstalledPackages(0, ActivityManager.getCurrentUser());
            if (packages != null) {
                pkgInfoList = packages.getList();
            }
            if (pkgInfoList == null) {
                return null;
            }
            ArrayList<String> pkgList = new ArrayList();
            for (int i = 0; i < pkgInfoList.size(); i++) {
                PackageInfo packageInfo = (PackageInfo) pkgInfoList.get(i);
                if ((packageInfo.applicationInfo.hwFlags & 268435456) != 0) {
                    pkgList.add(packageInfo.packageName);
                }
            }
            return pkgList;
        } catch (RemoteException e) {
            Log.e("BlackListUtils", "getBlackListApps failed due to RemoteException");
            return null;
        }
    }

    public static String getApplicationLabelName(String packageName, Context context) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            return context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(packageName, 0)).toString();
        } catch (NameNotFoundException e) {
            Log.e("BlackListUtils", "packagename not found");
            return null;
        }
    }

    public static boolean isBlackListApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, ActivityManager.getCurrentUser());
            if (appInfo == null || (appInfo.hwFlags & 268435456) == 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e("BlackListUtils", "check BlackListApp failed due to RemoteException");
            return false;
        }
    }

    public static boolean hasForBiddenApps(Context context) {
        if (context == null) {
            return false;
        }
        ArrayList<String> disabledAppList = BlackListPreferenceHelper.getDisabledAppList(context);
        if (disabledAppList == null || disabledAppList.size() <= 0) {
            return false;
        }
        return true;
    }

    public static ArrayList<AppEntry> filterBlackListApp(ArrayList<AppEntry> entries, int filterMode) {
        if (entries == null) {
            return null;
        }
        ArrayList<AppEntry> entriesBackUp = new ArrayList();
        if (filterMode == 101 || filterMode == 2) {
            return entries;
        }
        for (AppEntry entry : entries) {
            if (!isBlackListApp(entry.info.packageName)) {
                entriesBackUp.add(entry);
            }
        }
        return entriesBackUp;
    }

    private static boolean isAppRemovable(ApplicationInfo info) {
        boolean z = true;
        if (info == null) {
            return false;
        }
        if ((info.hwFlags & 33554432) == 0 && (info.hwFlags & 67108864) == 0 && (info.flags & 1) != 0) {
            z = false;
        }
        return z;
    }

    public static AlertDialog createWarningDialog(final Context context, ApplicationInfo info) {
        if (context == null || info == null) {
            return null;
        }
        Builder builder = new Builder(context);
        final String packageName = info.packageName;
        String packageLabel = context.getPackageManager().getApplicationLabel(info).toString();
        builder.setTitle(context.getString(2131628362));
        String message = "";
        String uninstall = context.getString(2131628365);
        String cancel = "";
        if (isAppRemovable(info)) {
            message = context.getString(2131628363);
            cancel = context.getString(2131628367);
            builder.setPositiveButton(uninstall, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BlackListUtils.unInstallPackage(context, packageName);
                }
            });
        } else {
            message = context.getString(2131628364);
            cancel = context.getString(2131628366);
        }
        builder.setMessage(String.format(message, new Object[]{packageLabel}));
        builder.setNegativeButton(cancel, null);
        return builder.create();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void unInstallPackage(Context context, String packageName) {
        if (!(context == null || TextUtils.isEmpty(packageName) || packageName == null)) {
            Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName));
            uninstallIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
            context.startActivity(uninstallIntent);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setHasForbidden(ManageApplications applications, FilterSpinnerAdapter filterAdapter, boolean enabled) {
        if (applications != null && filterAdapter != null && applications.mListType == 0) {
            filterAdapter.setFilterEnabled(101, enabled);
        }
    }

    public static void showWarningDialog(Context context, ApplicationInfo info) {
        sDialog = createWarningDialog(context, info);
        if (sDialog != null) {
            sDialog.show();
            Button removeButton = sDialog.getButton(-1);
            if (removeButton != null) {
                removeButton.setTextColor(-65536);
            }
        }
    }

    public static void dismissWarningDialog() {
        if (sDialog != null) {
            sDialog.dismiss();
            sDialog = null;
        }
    }

    public static boolean getShowingBlackListAppFlag() {
        return sShowBlackListApp;
    }

    public static void setShowingBlackListAppFlag(boolean flag) {
        sShowBlackListApp = flag;
    }

    public static void registerBlackListSharePreferenceListener(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        if (applications != null && filterAdapter != null) {
            sSharedPreference = applications.getActivity().getSharedPreferences("disabled_app", 0);
            if (sSharedPreference != null) {
                sBlackListChangeListener = new BlackListSharedPreferenceChangeListener(applications, filterAdapter);
                sSharedPreference.registerOnSharedPreferenceChangeListener(sBlackListChangeListener);
            }
        }
    }

    public static void unregisterBlackListSharePreferenceListener() {
        if (sSharedPreference != null) {
            sSharedPreference.unregisterOnSharedPreferenceChangeListener(sBlackListChangeListener);
        }
    }
}
