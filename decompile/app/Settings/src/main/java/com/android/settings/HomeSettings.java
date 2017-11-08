package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class HomeSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            PackageManager pm = context.getPackageManager();
            ArrayList<ResolveInfo> homeActivities = new ArrayList();
            pm.getHomeActivities(homeActivities);
            boolean doShowHome = context.getSharedPreferences("home_prefs", 0).getBoolean("do_show", false);
            if (homeActivities.size() > 1 || doShowHome) {
                Resources res = context.getResources();
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(2131625099);
                data.screenTitle = res.getString(2131625099);
                data.keywords = res.getString(2131626646);
                result.add(data);
                for (int i = 0; i < homeActivities.size(); i++) {
                    ActivityInfo activityInfo = ((ResolveInfo) homeActivities.get(i)).activityInfo;
                    try {
                        CharSequence name = activityInfo.loadLabel(pm);
                        if (!TextUtils.isEmpty(name)) {
                            data = new SearchIndexableRaw(context);
                            data.title = name.toString();
                            data.screenTitle = res.getString(2131625099);
                            result.add(data);
                        }
                    } catch (Exception e) {
                        Log.v("HomeSettings", "Problem dealing with Home " + activityInfo.name, e);
                    }
                }
            }
            return result;
        }
    };
    private HomeAppPreference mCurrentHome = null;
    OnClickListener mDeleteClickListener = new OnClickListener() {
        public void onClick(View v) {
            HomeSettings.this.uninstallApp((HomeAppPreference) HomeSettings.this.mPrefs.get(((Integer) v.getTag()).intValue()));
        }
    };
    OnClickListener mHomeClickListener = new OnClickListener() {
        public void onClick(View v) {
            HomeAppPreference pref = (HomeAppPreference) HomeSettings.this.mPrefs.get(((Integer) v.getTag()).intValue());
            if (!pref.isChecked) {
                HomeSettings.this.makeCurrentHome(pref);
            }
        }
    };
    private ComponentName[] mHomeComponentSet;
    private final IntentFilter mHomeFilter = new IntentFilter("android.intent.action.MAIN");
    private HomePackageReceiver mHomePackageReceiver = new HomePackageReceiver();
    private PackageManager mPm;
    private PreferenceGroup mPrefGroup;
    private ArrayList<HomeAppPreference> mPrefs;
    private boolean mShowNotice;

    private class HomeAppPreference extends Preference {
        ComponentName activityName;
        HomeSettings fragment;
        final ColorFilter grayscaleFilter;
        int index;
        boolean isChecked;
        boolean isSystem;
        String uninstallTarget;

        public HomeAppPreference(Context context, ComponentName activity, int i, Drawable icon, CharSequence title, HomeSettings parent, ActivityInfo info, boolean enabled, CharSequence summary) {
            super(context);
            setLayoutResource(2130968942);
            setIcon(icon);
            setTitle(title);
            setEnabled(enabled);
            setSummary(summary);
            this.activityName = activity;
            this.fragment = parent;
            this.index = i;
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            colorMatrix.getArray()[18] = 0.5f;
            this.grayscaleFilter = new ColorMatrixColorFilter(colorMatrix);
            determineTargets(info);
        }

        private void determineTargets(ActivityInfo info) {
            boolean z = true;
            Bundle meta = info.metaData;
            if (meta != null) {
                String altHomePackage = meta.getString("android.app.home.alternate");
                if (altHomePackage != null) {
                    try {
                        if (HomeSettings.this.mPm.checkSignatures(info.packageName, altHomePackage) >= 0) {
                            boolean z2;
                            PackageInfo altInfo = HomeSettings.this.mPm.getPackageInfo(altHomePackage, 0);
                            if ((altInfo.applicationInfo.flags & 1) != 0) {
                                z2 = true;
                            } else {
                                z2 = false;
                            }
                            this.isSystem = z2;
                            this.uninstallTarget = altInfo.packageName;
                            return;
                        }
                    } catch (Exception e) {
                        Log.w("HomeSettings", "Unable to compare/resolve alternate", e);
                    }
                }
            }
            if ((info.applicationInfo.flags & 1) == 0) {
                z = false;
            }
            this.isSystem = z;
            this.uninstallTarget = info.packageName;
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            ((RadioButton) view.findViewById(2131886910)).setChecked(this.isChecked);
            Integer indexObj = new Integer(this.index);
            ImageView icon = (ImageView) view.findViewById(2131886912);
            if (this.isSystem) {
                icon.setEnabled(false);
                icon.setColorFilter(this.grayscaleFilter);
            } else {
                icon.setEnabled(true);
                icon.setOnClickListener(HomeSettings.this.mDeleteClickListener);
                icon.setTag(indexObj);
            }
            View v = view.findViewById(2131886909);
            v.setTag(indexObj);
            v.setOnClickListener(HomeSettings.this.mHomeClickListener);
        }

        void setChecked(boolean state) {
            if (state != this.isChecked) {
                this.isChecked = state;
                notifyChanged();
            }
        }
    }

    private class HomePackageReceiver extends BroadcastReceiver {
        private HomePackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            HomeSettings.this.buildHomeActivitiesList();
            Index.getInstance(context).updateFromClassNameResource(HomeSettings.class.getName(), true, true);
        }
    }

    public HomeSettings() {
        this.mHomeFilter.addCategory("android.intent.category.HOME");
        this.mHomeFilter.addCategory("android.intent.category.DEFAULT");
    }

    void makeCurrentHome(HomeAppPreference newHome) {
        if (this.mCurrentHome != null) {
            this.mCurrentHome.setChecked(false);
        }
        newHome.setChecked(true);
        this.mCurrentHome = newHome;
        this.mPm.replacePreferredActivity(this.mHomeFilter, 1048576, this.mHomeComponentSet, newHome.activityName);
        getActivity().setResult(-1);
    }

    void uninstallApp(HomeAppPreference pref) {
        int i = 0;
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + pref.uninstallTarget));
        uninstallIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
        if (pref.isChecked) {
            i = 1;
        }
        startActivityForResult(uninstallIntent, i + 10);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        buildHomeActivitiesList();
        if (requestCode > 10 && this.mCurrentHome == null) {
            for (int i = 0; i < this.mPrefs.size(); i++) {
                HomeAppPreference pref = (HomeAppPreference) this.mPrefs.get(i);
                if (pref.isSystem) {
                    makeCurrentHome(pref);
                    return;
                }
            }
        }
    }

    private void buildHomeActivitiesList() {
        boolean mustSupportManagedProfile;
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        ComponentName currentDefaultHome = this.mPm.getHomeActivities(homeActivities);
        Context context = getPrefContext();
        this.mCurrentHome = null;
        this.mPrefGroup.removeAll();
        this.mPrefs = new ArrayList();
        this.mHomeComponentSet = new ComponentName[homeActivities.size()];
        int prefIndex = 0;
        boolean supportManagedProfilesExtra = getActivity().getIntent().getBooleanExtra("support_managed_profiles", false);
        if (hasManagedProfile()) {
            mustSupportManagedProfile = true;
        } else {
            mustSupportManagedProfile = supportManagedProfilesExtra;
        }
        for (int i = 0; i < homeActivities.size(); i++) {
            ResolveInfo candidate = (ResolveInfo) homeActivities.get(i);
            ActivityInfo info = candidate.activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            this.mHomeComponentSet[i] = activityName;
            try {
                HomeAppPreference pref;
                Drawable icon = info.loadIcon(this.mPm);
                CharSequence name = info.loadLabel(this.mPm);
                if (!mustSupportManagedProfile || launcherHasManagedProfilesFeature(candidate)) {
                    pref = new HomeAppPreference(context, activityName, prefIndex, icon, name, this, info, true, null);
                } else {
                    pref = new HomeAppPreference(context, activityName, prefIndex, icon, name, this, info, false, getResources().getString(2131625111));
                }
                this.mPrefs.add(pref);
                this.mPrefGroup.addPreference(pref);
                if (activityName.equals(currentDefaultHome)) {
                    this.mCurrentHome = pref;
                }
                prefIndex++;
            } catch (Exception e) {
                Log.v("HomeSettings", "Problem dealing with activity " + activityName, e);
            }
        }
        if (this.mCurrentHome != null) {
            if (this.mCurrentHome.isEnabled()) {
                getActivity().setResult(-1);
            }
            new Handler().post(new Runnable() {
                public void run() {
                    HomeSettings.this.mCurrentHome.setChecked(true);
                }
            });
        }
    }

    private boolean hasManagedProfile() {
        for (UserInfo userInfo : ((UserManager) getSystemService("user")).getProfiles(getActivity().getUserId())) {
            if (userInfo.isManagedProfile()) {
                return true;
            }
        }
        return false;
    }

    private boolean launcherHasManagedProfilesFeature(ResolveInfo resolveInfo) {
        try {
            return versionNumberAtLeastL(getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion);
        } catch (NameNotFoundException e) {
            Log.w("HomeSettings", "Unable to find/resolve packageName", e);
            return false;
        }
    }

    private boolean versionNumberAtLeastL(int versionNumber) {
        return versionNumber >= 21;
    }

    protected int getMetricsCategory() {
        return 55;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230799);
        this.mPm = getPackageManager();
        this.mPrefGroup = (PreferenceGroup) findPreference("home");
        Bundle args = getArguments();
        if (args != null) {
            z = args.getBoolean("show", false);
        } else {
            z = false;
        }
        this.mShowNotice = z;
        getActivity().finish();
    }

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        getActivity().registerReceiver(this.mHomePackageReceiver, filter);
        buildHomeActivitiesList();
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mHomePackageReceiver);
    }
}
