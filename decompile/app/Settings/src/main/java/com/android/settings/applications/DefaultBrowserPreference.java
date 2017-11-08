package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.android.settings.AppListPreference;
import java.util.ArrayList;
import java.util.List;

public class DefaultBrowserPreference extends AppListPreference {
    private final Handler mHandler = new Handler();
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            DefaultBrowserPreference.this.mHandler.postDelayed(DefaultBrowserPreference.this.mUpdateRunnable, 500);
        }
    };
    private final PackageManager mPm;
    private final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            DefaultBrowserPreference.this.updateDefaultBrowserPreference();
        }
    };

    public DefaultBrowserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPm = context.getPackageManager();
        refreshBrowserApps();
    }

    public void onAttached() {
        super.onAttached();
        updateDefaultBrowserPreference();
        this.mPackageMonitor.register(getContext(), getContext().getMainLooper(), false);
    }

    public void onDetached() {
        this.mPackageMonitor.unregister();
        super.onDetached();
    }

    protected boolean persistString(String newValue) {
        boolean z = false;
        if (newValue == null) {
            return false;
        }
        String packageName = newValue;
        if (TextUtils.isEmpty(newValue)) {
            return false;
        }
        boolean result = this.mPm.setDefaultBrowserPackageNameAsUser(newValue.toString(), this.mUserId);
        if (result) {
            setSummary("%s");
        }
        if (result) {
            z = super.persistString(newValue);
        }
        return z;
    }

    public void refreshBrowserApps() {
        List<String> browsers = resolveBrowserApps();
        setPackageNames((CharSequence[]) browsers.toArray(new String[browsers.size()]), null);
    }

    private void updateDefaultBrowserPreference() {
        refreshBrowserApps();
        String packageName = getContext().getPackageManager().getDefaultBrowserPackageNameAsUser(this.mUserId);
        if (TextUtils.isEmpty(packageName)) {
            setSummary(2131626953);
            Log.d("DefaultBrowserPref", "Cannot set empty default Browser value!");
            return;
        }
        Intent intent = new Intent();
        intent.setPackage(packageName);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        if (this.mPm.resolveActivityAsUser(intent, 0, this.mUserId) != null) {
            setValue(packageName);
            setSummary("%s");
            return;
        }
        setSummary(2131626953);
    }

    private List<String> resolveBrowserApps() {
        List<String> result = new ArrayList();
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        List<ResolveInfo> list = this.mPm.queryIntentActivitiesAsUser(intent, 131072, this.mUserId);
        int count = list.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (!(info.activityInfo == null || result.contains(info.activityInfo.packageName) || !info.handleAllWebDataURI)) {
                result.add(info.activityInfo.packageName);
            }
        }
        return result;
    }

    public static boolean hasBrowserPreference(String pkg, Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setData(Uri.parse("http:"));
        intent.setPackage(pkg);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isBrowserDefault(String pkg, Context context) {
        String defaultPackage = context.getPackageManager().getDefaultBrowserPackageNameAsUser(UserHandle.myUserId());
        return defaultPackage != null ? defaultPackage.equals(pkg) : false;
    }
}
