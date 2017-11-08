package com.android.settings.applications;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.UserManager;
import com.android.settings.AppListPreference;
import java.util.ArrayList;
import java.util.List;

public class DefaultHomePreference extends AppListPreference {
    private final ArrayList<ComponentName> mAllHomeComponents;
    private final IntentFilter mHomeFilter;

    public void performClick() {
        refreshHomeOptions();
        super.performClick();
    }

    protected boolean persistString(String value) {
        if (value != null) {
            getContext().getPackageManager().replacePreferredActivity(this.mHomeFilter, 1048576, (ComponentName[]) this.mAllHomeComponents.toArray(new ComponentName[0]), ComponentName.unflattenFromString(value));
            setSummary(getEntry());
        }
        return super.persistString(value);
    }

    public void refreshHomeOptions() {
        String myPkg = getContext().getPackageName();
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        PackageManager pm = getContext().getPackageManager();
        ComponentName currentDefaultHome = pm.getHomeActivities(homeActivities);
        ArrayList<ComponentName> components = new ArrayList();
        this.mAllHomeComponents.clear();
        List<CharSequence> summaries = new ArrayList();
        boolean mustSupportManagedProfile = hasManagedProfile();
        for (int i = 0; i < homeActivities.size(); i++) {
            ResolveInfo candidate = (ResolveInfo) homeActivities.get(i);
            ActivityInfo info = candidate.activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            this.mAllHomeComponents.add(activityName);
            if (!info.packageName.equals(myPkg)) {
                components.add(activityName);
                if (!mustSupportManagedProfile || launcherHasManagedProfilesFeature(candidate, pm)) {
                    summaries.add(null);
                } else {
                    summaries.add(getContext().getString(2131625111));
                }
            }
        }
        setComponentNames((ComponentName[]) components.toArray(new ComponentName[0]), currentDefaultHome, (CharSequence[]) summaries.toArray(new CharSequence[0]));
    }

    private boolean launcherHasManagedProfilesFeature(ResolveInfo resolveInfo, PackageManager pm) {
        try {
            return versionNumberAtLeastL(pm.getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean versionNumberAtLeastL(int versionNumber) {
        return versionNumber >= 21;
    }

    private boolean hasManagedProfile() {
        for (UserInfo userInfo : ((UserManager) getContext().getSystemService(UserManager.class)).getProfiles(getContext().getUserId())) {
            if (userInfo.isManagedProfile()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasHomePreference(String pkg, Context context) {
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        context.getPackageManager().getHomeActivities(homeActivities);
        for (int i = 0; i < homeActivities.size(); i++) {
            if (((ResolveInfo) homeActivities.get(i)).activityInfo.packageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHomeDefault(String pkg, Context context) {
        ComponentName def = context.getPackageManager().getHomeActivities(new ArrayList());
        return def != null ? def.getPackageName().equals(pkg) : false;
    }
}
