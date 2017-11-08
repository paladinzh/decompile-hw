package com.android.settings.applications;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.settings.AppListPreference;
import com.android.settings.SelfAvailablePreference;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefaultEmergencyPreference extends AppListPreference implements SelfAvailablePreference {
    public static final Intent QUERY_INTENT = new Intent("android.telephony.action.EMERGENCY_ASSISTANCE");
    private final ContentResolver mContentResolver;

    public DefaultEmergencyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContentResolver = context.getContentResolver();
        load();
    }

    protected boolean persistString(String value) {
        String previousValue = Secure.getString(this.mContentResolver, "emergency_assistance_application");
        if (!(TextUtils.isEmpty(value) || Objects.equals(value, previousValue))) {
            Secure.putString(this.mContentResolver, "emergency_assistance_application", value);
        }
        setSummary(getEntry());
        return true;
    }

    private void load() {
        new AsyncTask<Void, Void, Set<String>>() {
            protected Set<String> doInBackground(Void[] params) {
                return DefaultEmergencyPreference.this.resolveAssistPackageAndQueryApps();
            }

            protected void onPostExecute(Set<String> entries) {
                DefaultEmergencyPreference.this.setPackageNames((CharSequence[]) entries.toArray(new String[entries.size()]), Secure.getString(DefaultEmergencyPreference.this.mContentResolver, "emergency_assistance_application"));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private Set<String> resolveAssistPackageAndQueryApps() {
        Set<String> packages = new ArraySet();
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> infos = packageManager.queryIntentActivities(QUERY_INTENT, 0);
        PackageInfo bestMatch = null;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo info = (ResolveInfo) infos.get(i);
            if (!(info == null || info.activityInfo == null || packages.contains(info.activityInfo.packageName))) {
                String packageName = info.activityInfo.packageName;
                packages.add(packageName);
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                    if (isSystemApp(packageInfo) && (bestMatch == null || bestMatch.firstInstallTime > packageInfo.firstInstallTime)) {
                        bestMatch = packageInfo;
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        String defaultPackage = Secure.getString(this.mContentResolver, "emergency_assistance_application");
        boolean defaultMissing = !TextUtils.isEmpty(defaultPackage) ? !packages.contains(defaultPackage) : true;
        if (bestMatch != null && defaultMissing) {
            Secure.putString(this.mContentResolver, "emergency_assistance_application", bestMatch.packageName);
        }
        return packages;
    }

    private static boolean isCapable(Context context) {
        return context.getResources().getBoolean(17956956);
    }

    private static boolean isSystemApp(PackageInfo info) {
        if (info.applicationInfo == null || (info.applicationInfo.flags & 1) == 0) {
            return false;
        }
        return true;
    }

    public boolean isAvailable(Context context) {
        return false;
    }

    public static boolean hasEmergencyPreference(String pkg, Context context) {
        Intent i = new Intent(QUERY_INTENT);
        i.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(i, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isEmergencyDefault(String pkg, Context context) {
        String defaultPackage = Secure.getString(context.getContentResolver(), "emergency_assistance_application");
        return defaultPackage != null ? defaultPackage.equals(pkg) : false;
    }
}
