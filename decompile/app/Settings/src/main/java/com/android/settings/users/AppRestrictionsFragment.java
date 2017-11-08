package com.android.settings.users;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionEntry;
import android.content.RestrictionsManager;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.users.AppRestrictionsHelper;
import com.android.settingslib.users.AppRestrictionsHelper.OnDisableUiForPackageListener;
import com.android.settingslib.users.AppRestrictionsHelper.SelectableAppInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class AppRestrictionsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnClickListener, OnPreferenceClickListener, OnDisableUiForPackageListener {
    private static final String TAG = AppRestrictionsFragment.class.getSimpleName();
    private PreferenceGroup mAppList;
    private boolean mAppListChanged;
    private AsyncTask mAppLoadingTask;
    private int mCustomRequestCode = 1000;
    private HashMap<Integer, AppRestrictionsPreference> mCustomRequestMap = new HashMap();
    private boolean mFirstTime = true;
    private AppRestrictionsHelper mHelper;
    protected IPackageManager mIPm;
    private boolean mNewUser;
    protected PackageManager mPackageManager;
    private BroadcastReceiver mPackageObserver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AppRestrictionsFragment.this.onPackageChanged(intent);
        }
    };
    protected boolean mRestrictedProfile;
    private PackageInfo mSysPackageInfo;
    protected UserHandle mUser;
    private BroadcastReceiver mUserBackgrounding = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (AppRestrictionsFragment.this.mAppListChanged) {
                AppRestrictionsFragment.this.mHelper.applyUserAppsStates(AppRestrictionsFragment.this);
            }
        }
    };
    protected UserManager mUserManager;

    private class AppLoadingTask extends AsyncTask<Void, Void, Void> {
        private AppLoadingTask() {
        }

        protected Void doInBackground(Void... params) {
            AppRestrictionsFragment.this.mHelper.fetchAndMergeApps();
            return null;
        }

        protected void onPostExecute(Void result) {
            AppRestrictionsFragment.this.populateApps();
        }
    }

    static class AppRestrictionsPreference extends SwitchPreference {
        private boolean hasSettings;
        private boolean immutable;
        private OnClickListener listener;
        private List<Preference> mChildren = new ArrayList();
        private boolean panelOpen;
        private ArrayList<RestrictionEntry> restrictions;

        AppRestrictionsPreference(Context context, OnClickListener listener) {
            super(context);
            setLayoutResource(2130968910);
            this.listener = listener;
        }

        private void setSettingsEnabled(boolean enable) {
            this.hasSettings = enable;
        }

        void setRestrictions(ArrayList<RestrictionEntry> restrictions) {
            this.restrictions = restrictions;
        }

        void setImmutable(boolean immutable) {
            this.immutable = immutable;
        }

        boolean isImmutable() {
            return this.immutable;
        }

        ArrayList<RestrictionEntry> getRestrictions() {
            return this.restrictions;
        }

        boolean isPanelOpen() {
            return this.panelOpen;
        }

        void setPanelOpen(boolean open) {
            this.panelOpen = open;
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            int i;
            boolean z;
            int i2 = 8;
            boolean z2 = false;
            super.onBindViewHolder(view);
            View appRestrictionsSettings = view.findViewById(2131886865);
            if (this.hasSettings) {
                i = 0;
            } else {
                i = 8;
            }
            appRestrictionsSettings.setVisibility(i);
            View findViewById = view.findViewById(2131886866);
            if (this.hasSettings) {
                i2 = 0;
            }
            findViewById.setVisibility(i2);
            appRestrictionsSettings.setOnClickListener(this.listener);
            appRestrictionsSettings.setTag(this);
            View appRestrictionsPref = view.findViewById(2131886864);
            appRestrictionsPref.setOnClickListener(this.listener);
            appRestrictionsPref.setTag(this);
            ViewGroup widget = (ViewGroup) view.findViewById(16908312);
            if (isImmutable()) {
                z = false;
            } else {
                z = true;
            }
            widget.setEnabled(z);
            if (widget.getChildCount() > 0) {
                final Switch toggle = (Switch) widget.getChildAt(0);
                if (!isImmutable()) {
                    z2 = true;
                }
                toggle.setEnabled(z2);
                toggle.setTag(this);
                toggle.setClickable(true);
                toggle.setFocusable(true);
                toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        AppRestrictionsPreference.this.listener.onClick(toggle);
                    }
                });
            }
        }
    }

    class RestrictionsResultReceiver extends BroadcastReceiver {
        boolean invokeIfCustom;
        String packageName;
        AppRestrictionsPreference preference;

        RestrictionsResultReceiver(String packageName, AppRestrictionsPreference preference, boolean invokeIfCustom) {
            this.packageName = packageName;
            this.preference = preference;
            this.invokeIfCustom = invokeIfCustom;
        }

        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);
            ArrayList<RestrictionEntry> restrictions = results.getParcelableArrayList("android.intent.extra.restrictions_list");
            Intent restrictionsIntent = (Intent) results.getParcelable("android.intent.extra.restrictions_intent");
            if (restrictions != null && restrictionsIntent == null) {
                AppRestrictionsFragment.this.onRestrictionsReceived(this.preference, restrictions);
                if (AppRestrictionsFragment.this.mRestrictedProfile) {
                    AppRestrictionsFragment.this.mUserManager.setApplicationRestrictions(this.packageName, RestrictionsManager.convertRestrictionsToBundle(restrictions), AppRestrictionsFragment.this.mUser);
                }
            } else if (restrictionsIntent != null) {
                this.preference.setRestrictions(restrictions);
                if (this.invokeIfCustom && AppRestrictionsFragment.this.isResumed()) {
                    assertSafeToStartCustomActivity(restrictionsIntent);
                    AppRestrictionsFragment.this.startActivityForResult(restrictionsIntent, AppRestrictionsFragment.this.generateCustomActivityRequestCode(this.preference));
                }
            }
        }

        private void assertSafeToStartCustomActivity(Intent intent) {
            if (intent.getPackage() == null || !intent.getPackage().equals(this.packageName)) {
                List<ResolveInfo> resolveInfos = AppRestrictionsFragment.this.mPackageManager.queryIntentActivities(intent, 0);
                if (resolveInfos.size() == 1) {
                    if (!this.packageName.equals(((ResolveInfo) resolveInfos.get(0)).activityInfo.packageName)) {
                        throw new SecurityException("Application " + this.packageName + " is not allowed to start activity " + intent);
                    }
                }
            }
        }
    }

    protected void init(Bundle icicle) {
        if (icicle != null) {
            this.mUser = new UserHandle(icicle.getInt("user_id"));
        } else {
            Bundle args = getArguments();
            if (args != null) {
                if (args.containsKey("user_id")) {
                    this.mUser = new UserHandle(args.getInt("user_id"));
                }
                this.mNewUser = args.getBoolean("new_user", false);
            }
        }
        if (this.mUser == null) {
            this.mUser = Process.myUserHandle();
        }
        this.mHelper = new AppRestrictionsHelper(getContext(), this.mUser);
        this.mPackageManager = getActivity().getPackageManager();
        this.mIPm = Stub.asInterface(ServiceManager.getService("package"));
        this.mUserManager = (UserManager) getActivity().getSystemService("user");
        this.mRestrictedProfile = this.mUserManager.getUserInfo(this.mUser.getIdentifier()).isRestricted();
        try {
            this.mSysPackageInfo = this.mPackageManager.getPackageInfo("android", 64);
        } catch (NameNotFoundException e) {
        }
        addPreferencesFromResource(2131230740);
        this.mAppList = getAppPreferenceGroup();
        this.mAppList.setOrderingAsAdded(false);
    }

    protected int getMetricsCategory() {
        return 97;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("user_id", this.mUser.getIdentifier());
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mUserBackgrounding, new IntentFilter("android.intent.action.USER_BACKGROUND"));
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        getActivity().registerReceiver(this.mPackageObserver, packageFilter);
        this.mAppListChanged = false;
        if (this.mAppLoadingTask == null || this.mAppLoadingTask.getStatus() == Status.FINISHED) {
            this.mAppLoadingTask = new AppLoadingTask().execute(new Void[0]);
        }
    }

    public void onPause() {
        super.onPause();
        this.mNewUser = false;
        getActivity().unregisterReceiver(this.mUserBackgrounding);
        getActivity().unregisterReceiver(this.mPackageObserver);
        if (this.mAppListChanged) {
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    AppRestrictionsFragment.this.mHelper.applyUserAppsStates(AppRestrictionsFragment.this);
                    return null;
                }
            }.execute(new Void[0]);
        }
    }

    private void onPackageChanged(Intent intent) {
        String action = intent.getAction();
        AppRestrictionsPreference pref = (AppRestrictionsPreference) findPreference(getKeyForPackage(intent.getData() == null ? "" : intent.getData().getSchemeSpecificPart()));
        if (pref != null) {
            if (("android.intent.action.PACKAGE_ADDED".equals(action) && pref.isChecked()) || ("android.intent.action.PACKAGE_REMOVED".equals(action) && !pref.isChecked())) {
                pref.setEnabled(true);
            }
        }
    }

    protected PreferenceGroup getAppPreferenceGroup() {
        return getPreferenceScreen();
    }

    public void onDisableUiForPackage(String packageName) {
        AppRestrictionsPreference pref = (AppRestrictionsPreference) findPreference(getKeyForPackage(packageName));
        if (pref != null) {
            pref.setEnabled(false);
        }
    }

    private boolean isPlatformSigned(PackageInfo pi) {
        if (pi == null || pi.signatures == null) {
            return false;
        }
        return this.mSysPackageInfo.signatures[0].equals(pi.signatures[0]);
    }

    private boolean isAppEnabledForUser(PackageInfo pi) {
        boolean z = false;
        if (pi == null) {
            return false;
        }
        int flags = pi.applicationInfo.flags;
        int privateFlags = pi.applicationInfo.privateFlags;
        if ((8388608 & flags) != 0 && (privateFlags & 1) == 0) {
            z = true;
        }
        return z;
    }

    private void populateApps() {
        Context context = getActivity();
        if (context != null) {
            PackageManager pm = this.mPackageManager;
            IPackageManager ipm = this.mIPm;
            int userId = this.mUser.getIdentifier();
            if (Utils.getExistingUser(this.mUserManager, this.mUser) != null) {
                this.mAppList.removeAll();
                List<ResolveInfo> receivers = pm.queryBroadcastReceivers(new Intent("android.intent.action.GET_RESTRICTION_ENTRIES"), 0);
                for (SelectableAppInfo app : this.mHelper.getVisibleApps()) {
                    String packageName = app.packageName;
                    if (packageName != null) {
                        boolean isSettingsApp = packageName.equals(context.getPackageName());
                        AppRestrictionsPreference p = new AppRestrictionsPreference(getPrefContext(), this);
                        boolean hasSettings = resolveInfoListHasPackage(receivers, packageName);
                        if (isSettingsApp) {
                            addLocationAppRestrictionsPreference(app, p);
                            this.mHelper.setPackageSelected(packageName, true);
                        } else {
                            PackageInfo pi = null;
                            try {
                                pi = ipm.getPackageInfo(packageName, 8256, userId);
                            } catch (RemoteException e) {
                            }
                            if (!(pi == null || (this.mRestrictedProfile && isAppUnsupportedInRestrictedProfile(pi)))) {
                                p.setIcon(app.icon != null ? app.icon.mutate() : null);
                                p.setChecked(false);
                                p.setTitle(app.activityName);
                                p.setKey(getKeyForPackage(packageName));
                                boolean z = hasSettings && app.masterEntry == null;
                                p.setSettingsEnabled(z);
                                p.setPersistent(false);
                                p.setOnPreferenceChangeListener(this);
                                p.setOnPreferenceClickListener(this);
                                p.setSummary(getPackageSummary(pi, app));
                                if (pi.requiredForAllUsers || isPlatformSigned(pi)) {
                                    p.setChecked(true);
                                    p.setImmutable(true);
                                    if (hasSettings) {
                                        if (app.masterEntry == null) {
                                            requestRestrictionsForApp(packageName, p, false);
                                        }
                                    }
                                } else if (!this.mNewUser && isAppEnabledForUser(pi)) {
                                    p.setChecked(true);
                                }
                                if (app.masterEntry != null) {
                                    p.setImmutable(true);
                                    p.setChecked(this.mHelper.isPackageSelected(packageName));
                                }
                                p.setOrder((this.mAppList.getPreferenceCount() + 2) * 100);
                                this.mHelper.setPackageSelected(packageName, p.isChecked());
                                this.mAppList.addPreference(p);
                            }
                        }
                    }
                }
                this.mAppListChanged = true;
                if (this.mNewUser && this.mFirstTime) {
                    this.mFirstTime = false;
                    this.mHelper.applyUserAppsStates(this);
                }
            }
        }
    }

    private String getPackageSummary(PackageInfo pi, SelectableAppInfo app) {
        if (app.masterEntry != null) {
            if (!this.mRestrictedProfile || pi.restrictedAccountType == null) {
                return getString(2131626579, new Object[]{app.masterEntry.activityName});
            }
            return getString(2131626581, new Object[]{app.masterEntry.activityName});
        } else if (pi.restrictedAccountType != null) {
            return getString(2131626580);
        } else {
            return null;
        }
    }

    private static boolean isAppUnsupportedInRestrictedProfile(PackageInfo pi) {
        return pi.requiredAccountType != null && pi.restrictedAccountType == null;
    }

    private void addLocationAppRestrictionsPreference(SelectableAppInfo app, AppRestrictionsPreference p) {
        String packageName = app.packageName;
        p.setIcon(2130838380);
        p.setKey(getKeyForPackage(packageName));
        ArrayList<RestrictionEntry> restrictions = RestrictionUtils.getRestrictions(getActivity(), this.mUser);
        RestrictionEntry locationRestriction = (RestrictionEntry) restrictions.get(0);
        p.setTitle(locationRestriction.getTitle());
        p.setRestrictions(restrictions);
        p.setSummary(locationRestriction.getDescription());
        p.setChecked(locationRestriction.getSelectedState());
        p.setPersistent(false);
        p.setOnPreferenceClickListener(this);
        p.setOrder(100);
        this.mAppList.addPreference(p);
    }

    private String getKeyForPackage(String packageName) {
        return "pkg_" + packageName;
    }

    private boolean resolveInfoListHasPackage(List<ResolveInfo> receivers, String packageName) {
        for (ResolveInfo info : receivers) {
            if (info.activityInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void updateAllEntries(String prefKey, boolean checked) {
        for (int i = 0; i < this.mAppList.getPreferenceCount(); i++) {
            Preference pref = this.mAppList.getPreference(i);
            if ((pref instanceof AppRestrictionsPreference) && prefKey.equals(pref.getKey())) {
                ((AppRestrictionsPreference) pref).setChecked(checked);
            }
        }
    }

    public void onClick(View v) {
        if (v.getTag() instanceof AppRestrictionsPreference) {
            AppRestrictionsPreference pref = (AppRestrictionsPreference) v.getTag();
            if (v.getId() == 2131886865) {
                onAppSettingsIconClicked(pref);
            } else if (!pref.isImmutable()) {
                boolean z;
                if (pref.isChecked()) {
                    z = false;
                } else {
                    z = true;
                }
                pref.setChecked(z);
                String packageName = pref.getKey().substring("pkg_".length());
                if (packageName.equals(getActivity().getPackageName())) {
                    ((RestrictionEntry) pref.restrictions.get(0)).setSelectedState(pref.isChecked());
                    RestrictionUtils.setRestrictions(getActivity(), pref.restrictions, this.mUser);
                    return;
                }
                this.mHelper.setPackageSelected(packageName, pref.isChecked());
                if (pref.isChecked() && pref.hasSettings && pref.restrictions == null) {
                    requestRestrictionsForApp(packageName, pref, false);
                }
                this.mAppListChanged = true;
                if (!this.mRestrictedProfile) {
                    this.mHelper.applyUserAppState(packageName, pref.isChecked(), this);
                }
                updateAllEntries(pref.getKey(), pref.isChecked());
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key != null && key.contains(";")) {
            StringTokenizer st = new StringTokenizer(key, ";");
            String packageName = st.nextToken();
            String restrictionKey = st.nextToken();
            ArrayList<RestrictionEntry> restrictions = ((AppRestrictionsPreference) this.mAppList.findPreference("pkg_" + packageName)).getRestrictions();
            if (restrictions != null) {
                for (RestrictionEntry entry : restrictions) {
                    if (entry.getKey().equals(restrictionKey)) {
                        switch (entry.getType()) {
                            case 1:
                                entry.setSelectedState(((Boolean) newValue).booleanValue());
                                break;
                            case 2:
                            case 3:
                                ListPreference listPref = (ListPreference) preference;
                                entry.setSelectedString((String) newValue);
                                listPref.setSummary(findInArray(entry.getChoiceEntries(), entry.getChoiceValues(), (String) newValue));
                                break;
                            case 4:
                                Set<String> set = (Set) newValue;
                                String[] selectedValues = new String[set.size()];
                                set.toArray(selectedValues);
                                entry.setAllSelectedStrings(selectedValues);
                                break;
                            default:
                                continue;
                        }
                        this.mUserManager.setApplicationRestrictions(packageName, RestrictionsManager.convertRestrictionsToBundle(restrictions), this.mUser);
                    }
                }
            }
        }
        return true;
    }

    private void removeRestrictionsForApp(AppRestrictionsPreference preference) {
        for (Preference p : preference.mChildren) {
            this.mAppList.removePreference(p);
        }
        preference.mChildren.clear();
    }

    private void onAppSettingsIconClicked(AppRestrictionsPreference preference) {
        boolean z = true;
        if (preference.getKey().startsWith("pkg_")) {
            if (preference.isPanelOpen()) {
                removeRestrictionsForApp(preference);
            } else {
                requestRestrictionsForApp(preference.getKey().substring("pkg_".length()), preference, true);
            }
            if (preference.isPanelOpen()) {
                z = false;
            }
            preference.setPanelOpen(z);
        }
    }

    private void requestRestrictionsForApp(String packageName, AppRestrictionsPreference preference, boolean invokeIfCustom) {
        Bundle oldEntries = this.mUserManager.getApplicationRestrictions(packageName, this.mUser);
        Intent intent = new Intent("android.intent.action.GET_RESTRICTION_ENTRIES");
        intent.setPackage(packageName);
        intent.putExtra("android.intent.extra.restrictions_bundle", oldEntries);
        intent.addFlags(32);
        getActivity().sendOrderedBroadcast(intent, null, new RestrictionsResultReceiver(packageName, preference, invokeIfCustom), null, -1, null, null);
    }

    private void onRestrictionsReceived(AppRestrictionsPreference preference, ArrayList<RestrictionEntry> restrictions) {
        removeRestrictionsForApp(preference);
        int count = 1;
        for (RestrictionEntry entry : restrictions) {
            Preference p = null;
            switch (entry.getType()) {
                case 1:
                    p = new SwitchPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    p.setSummary(entry.getDescription());
                    ((SwitchPreference) p).setChecked(entry.getSelectedState());
                    break;
                case 2:
                case 3:
                    p = new ListPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    String value = entry.getSelectedString();
                    if (value == null) {
                        value = entry.getDescription();
                    }
                    p.setSummary(findInArray(entry.getChoiceEntries(), entry.getChoiceValues(), value));
                    ((ListPreference) p).setEntryValues(entry.getChoiceValues());
                    ((ListPreference) p).setEntries(entry.getChoiceEntries());
                    ((ListPreference) p).setValue(value);
                    ((ListPreference) p).setDialogTitle(entry.getTitle());
                    break;
                case 4:
                    p = new MultiSelectListPreference(getPrefContext());
                    p.setTitle(entry.getTitle());
                    ((MultiSelectListPreference) p).setEntryValues(entry.getChoiceValues());
                    ((MultiSelectListPreference) p).setEntries(entry.getChoiceEntries());
                    HashSet<String> set = new HashSet();
                    Collections.addAll(set, entry.getAllSelectedStrings());
                    ((MultiSelectListPreference) p).setValues(set);
                    ((MultiSelectListPreference) p).setDialogTitle(entry.getTitle());
                    break;
            }
            if (p != null) {
                p.setPersistent(false);
                p.setOrder(preference.getOrder() + count);
                p.setKey(preference.getKey().substring("pkg_".length()) + ";" + entry.getKey());
                this.mAppList.addPreference(p);
                p.setOnPreferenceChangeListener(this);
                p.setIcon(2130837691);
                preference.mChildren.add(p);
                count++;
            }
        }
        preference.setRestrictions(restrictions);
        if (count == 1 && preference.isImmutable() && preference.isChecked()) {
            this.mAppList.removePreference(preference);
        }
    }

    private int generateCustomActivityRequestCode(AppRestrictionsPreference preference) {
        this.mCustomRequestCode++;
        this.mCustomRequestMap.put(Integer.valueOf(this.mCustomRequestCode), preference);
        return this.mCustomRequestCode;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppRestrictionsPreference pref = (AppRestrictionsPreference) this.mCustomRequestMap.get(Integer.valueOf(requestCode));
        if (pref == null) {
            Log.w(TAG, "Unknown requestCode " + requestCode);
            return;
        }
        if (resultCode == -1) {
            String packageName = pref.getKey().substring("pkg_".length());
            ArrayList<RestrictionEntry> list = data.getParcelableArrayListExtra("android.intent.extra.restrictions_list");
            Bundle bundle = data.getBundleExtra("android.intent.extra.restrictions_bundle");
            if (list != null) {
                pref.setRestrictions(list);
                this.mUserManager.setApplicationRestrictions(packageName, RestrictionsManager.convertRestrictionsToBundle(list), this.mUser);
            } else if (bundle != null) {
                this.mUserManager.setApplicationRestrictions(packageName, bundle, this.mUser);
            }
        }
        this.mCustomRequestMap.remove(Integer.valueOf(requestCode));
    }

    private String findInArray(String[] choiceEntries, String[] choiceValues, String selectedString) {
        for (int i = 0; i < choiceValues.length; i++) {
            if (choiceValues[i].equals(selectedString)) {
                return choiceEntries[i];
            }
        }
        return selectedString;
    }

    public boolean onPreferenceClick(Preference preference) {
        boolean newEnabledState = false;
        if (!preference.getKey().startsWith("pkg_")) {
            return false;
        }
        AppRestrictionsPreference arp = (AppRestrictionsPreference) preference;
        if (!arp.isImmutable()) {
            String packageName = arp.getKey().substring("pkg_".length());
            if (!arp.isChecked()) {
                newEnabledState = true;
            }
            arp.setChecked(newEnabledState);
            this.mHelper.setPackageSelected(packageName, newEnabledState);
            updateAllEntries(arp.getKey(), newEnabledState);
            this.mAppListChanged = true;
            this.mHelper.applyUserAppState(packageName, newEnabledState, this);
        }
        return true;
    }
}
