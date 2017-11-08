package com.android.settings.datausage;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.format.Formatter;
import android.util.ArraySet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.android.settings.AppHeader;
import com.android.settings.datausage.CycleAdapter.CycleItem;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settingslib.AppItem;
import com.android.settingslib.R$string;
import com.android.settingslib.Utils;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;
import com.android.settingslib.net.UidDetailProvider;

public class AppDataUsage extends DataUsageBase implements OnPreferenceChangeListener, Listener {
    private AppItem mAppItem;
    private PreferenceCategory mAppList;
    private Preference mAppSettings;
    private Intent mAppSettingsIntent;
    private Preference mBackgroundUsage;
    private ChartData mChartData;
    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(AppDataUsage.this.getActivity(), AppDataUsage.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            AppDataUsage.this.mChartData = data;
            AppDataUsage.this.mCycleAdapter.updateCycleList(AppDataUsage.this.mPolicy, AppDataUsage.this.mChartData);
            AppDataUsage.this.bindData();
        }

        public void onLoaderReset(Loader<ChartData> loader) {
        }
    };
    private SpinnerPreference mCycle;
    private CycleAdapter mCycleAdapter;
    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            CycleItem cycle = (CycleItem) AppDataUsage.this.mCycle.getSelectedItem();
            AppDataUsage.this.mStart = cycle.start;
            AppDataUsage.this.mEnd = cycle.end;
            AppDataUsage.this.bindData();
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private DataSaverBackend mDataSaverBackend;
    private long mEnd;
    private Preference mForegroundUsage;
    private Drawable mIcon;
    private CharSequence mLabel;
    private String mPackageName;
    private final ArraySet<String> mPackages = new ArraySet();
    private NetworkPolicy mPolicy;
    private SwitchPreference mRestrictBackground;
    private long mStart;
    private INetworkStatsSession mStatsSession;
    private NetworkTemplate mTemplate;
    private Preference mTotalUsage;
    private SwitchPreference mUnrestrictedData;

    private class AppPrefLoader extends AsyncTask<String, Void, Preference> {
        private AppPrefLoader() {
        }

        protected Preference doInBackground(String... params) {
            PackageManager pm = AppDataUsage.this.getPackageManager();
            try {
                ApplicationInfo info = pm.getApplicationInfo(params[0], 0);
                Preference preference = new Preference(AppDataUsage.this.getPrefContext());
                preference.setIcon(info.loadIcon(pm));
                preference.setTitle(info.loadLabel(pm));
                preference.setSelectable(false);
                return preference;
            } catch (NameNotFoundException e) {
                return null;
            }
        }

        protected void onPostExecute(Preference pref) {
            if (pref != null && AppDataUsage.this.mAppList != null) {
                AppDataUsage.this.mAppList.addPreference(pref);
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        try {
            NetworkTemplate networkTemplate;
            int i;
            this.mStatsSession = this.services.mStatsService.openSession();
            this.mAppItem = args != null ? (AppItem) args.getParcelable("app_item") : null;
            if (args != null) {
                networkTemplate = (NetworkTemplate) args.getParcelable("network_template");
            } else {
                networkTemplate = null;
            }
            this.mTemplate = networkTemplate;
            if (this.mTemplate == null) {
                Context context = getContext();
                this.mTemplate = DataUsageSummary.getDefaultTemplate(context, DataUsageSummary.getDefaultSubscriptionId(context));
            }
            if (this.mAppItem == null) {
                int uid;
                if (args != null) {
                    uid = args.getInt("uid", -1);
                } else {
                    uid = getActivity().getIntent().getIntExtra("uid", -1);
                }
                if (uid == -1) {
                    getActivity().finish();
                } else {
                    addUid(uid);
                    this.mAppItem = new AppItem(uid);
                    this.mAppItem.addUid(uid);
                }
            } else {
                for (i = 0; i < this.mAppItem.uids.size(); i++) {
                    addUid(this.mAppItem.uids.keyAt(i));
                }
            }
            addPreferencesFromResource(2131230736);
            this.mTotalUsage = findPreference("total_usage");
            this.mForegroundUsage = findPreference("foreground_usage");
            this.mBackgroundUsage = findPreference("background_usage");
            this.mCycle = (SpinnerPreference) findPreference("cycle");
            this.mCycleAdapter = new CycleAdapter(getContext(), this.mCycle, this.mCycleListener, false);
            PackageManager pm;
            if (this.mAppItem.key > 0) {
                if (this.mPackages.size() != 0) {
                    pm = getPackageManager();
                    try {
                        ApplicationInfo info = pm.getApplicationInfo((String) this.mPackages.valueAt(0), 0);
                        this.mIcon = info.loadIcon(pm);
                        this.mLabel = info.loadLabel(pm);
                        this.mPackageName = info.packageName;
                    } catch (NameNotFoundException e) {
                    }
                }
                if (this.mAppItem.key == 1000) {
                    removePreference("unrestricted_data_saver");
                    removePreference("restrict_background");
                } else {
                    this.mRestrictBackground = (SwitchPreference) findPreference("restrict_background");
                    this.mRestrictBackground.setOnPreferenceChangeListener(this);
                    this.mUnrestrictedData = (SwitchPreference) findPreference("unrestricted_data_saver");
                    this.mUnrestrictedData.setOnPreferenceChangeListener(this);
                }
                this.mDataSaverBackend = new DataSaverBackend(getContext());
                this.mAppSettings = findPreference("app_settings");
                this.mAppSettingsIntent = new Intent("android.intent.action.MANAGE_NETWORK_USAGE");
                this.mAppSettingsIntent.addCategory("android.intent.category.DEFAULT");
                pm = getPackageManager();
                boolean matchFound = false;
                for (String packageName : this.mPackages) {
                    this.mAppSettingsIntent.setPackage(packageName);
                    if (pm.resolveActivity(this.mAppSettingsIntent, 0) != null) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    removePreference("app_settings");
                    this.mAppSettings = null;
                }
                if (this.mPackages.size() > 1) {
                    this.mAppList = (PreferenceCategory) findPreference("app_list");
                    for (i = 1; i < this.mPackages.size(); i++) {
                        new AppPrefLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{(String) this.mPackages.valueAt(i)});
                    }
                    return;
                }
                removePreference("app_list");
                return;
            }
            if (this.mAppItem.key == -4) {
                this.mLabel = getContext().getString(R$string.data_usage_uninstalled_apps_users);
            } else if (this.mAppItem.key == -5) {
                this.mLabel = getContext().getString(R$string.tether_settings_title_all);
            } else {
                int userId = UidDetailProvider.getUserIdForKey(this.mAppItem.key);
                UserManager um = UserManager.get(getActivity());
                UserInfo info2 = um.getUserInfo(userId);
                pm = getPackageManager();
                this.mIcon = Utils.getUserIcon(getActivity(), um, info2);
                this.mLabel = Utils.getUserLabel(getActivity(), info2);
                this.mPackageName = getActivity().getPackageName();
            }
            removePreference("unrestricted_data_saver");
            removePreference("app_settings");
            removePreference("restrict_background");
            removePreference("app_list");
        } catch (RemoteException e2) {
            throw new RuntimeException(e2);
        }
    }

    public void onDestroy() {
        TrafficStats.closeQuietly(this.mStatsSession);
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        if (this.mDataSaverBackend != null) {
            this.mDataSaverBackend.addListener(this);
        }
        this.mPolicy = this.services.mPolicyEditor.getPolicy(this.mTemplate);
        getLoaderManager().restartLoader(2, ChartDataLoader.buildArgs(this.mTemplate, this.mAppItem), this.mChartDataCallbacks);
        updatePrefs();
    }

    public void onPause() {
        super.onPause();
        if (this.mDataSaverBackend != null) {
            this.mDataSaverBackend.remListener(this);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean z = false;
        if (preference == this.mRestrictBackground) {
            DataSaverBackend dataSaverBackend = this.mDataSaverBackend;
            int i = this.mAppItem.key;
            String str = this.mPackageName;
            if (!((Boolean) newValue).booleanValue()) {
                z = true;
            }
            dataSaverBackend.setIsBlacklisted(i, str, z);
            return true;
        } else if (preference != this.mUnrestrictedData) {
            return false;
        } else {
            this.mDataSaverBackend.setIsWhitelisted(this.mAppItem.key, this.mPackageName, ((Boolean) newValue).booleanValue());
            return true;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference != this.mAppSettings) {
            return super.onPreferenceTreeClick(preference);
        }
        getActivity().startActivityAsUser(this.mAppSettingsIntent, new UserHandle(UserHandle.getUserId(this.mAppItem.key)));
        return true;
    }

    private void updatePrefs() {
        updatePrefs(getAppRestrictBackground(), getUnrestrictData());
    }

    private void updatePrefs(boolean restrictBackground, boolean unrestrictData) {
        if (this.mRestrictBackground != null) {
            boolean z;
            SwitchPreference switchPreference = this.mRestrictBackground;
            if (restrictBackground) {
                z = false;
            } else {
                z = true;
            }
            switchPreference.setChecked(z);
        }
        if (this.mUnrestrictedData == null) {
            return;
        }
        if (restrictBackground) {
            this.mUnrestrictedData.setVisible(false);
            return;
        }
        this.mUnrestrictedData.setVisible(true);
        this.mUnrestrictedData.setChecked(unrestrictData);
    }

    private void addUid(int uid) {
        String[] packages = getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            for (Object add : packages) {
                this.mPackages.add(add);
            }
        }
    }

    private void bindData() {
        long foregroundBytes;
        long backgroundBytes;
        if (this.mChartData == null || this.mStart == 0) {
            foregroundBytes = 0;
            backgroundBytes = 0;
            this.mCycle.setVisible(false);
        } else {
            this.mCycle.setVisible(true);
            long now = System.currentTimeMillis();
            Entry entry = this.mChartData.detailDefault.getValues(this.mStart, this.mEnd, now, null);
            backgroundBytes = entry.rxBytes + entry.txBytes;
            entry = this.mChartData.detailForeground.getValues(this.mStart, this.mEnd, now, entry);
            foregroundBytes = entry.rxBytes + entry.txBytes;
        }
        long totalBytes = backgroundBytes + foregroundBytes;
        Context context = getContext();
        this.mTotalUsage.setSummary(Formatter.formatFileSize(context, totalBytes));
        this.mForegroundUsage.setSummary(Formatter.formatFileSize(context, foregroundBytes));
        this.mBackgroundUsage.setSummary(Formatter.formatFileSize(context, backgroundBytes));
    }

    private boolean getAppRestrictBackground() {
        if ((this.services.mPolicyManager.getUidPolicy(this.mAppItem.key) & 1) != 0) {
            return true;
        }
        return false;
    }

    private boolean getUnrestrictData() {
        if (this.mDataSaverBackend != null) {
            return this.mDataSaverBackend.isWhitelisted(this.mAppItem.key);
        }
        return false;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = setPinnedHeaderView(2130968627);
        String str = this.mPackages.size() != 0 ? (String) this.mPackages.valueAt(0) : null;
        int uid = 0;
        if (str != null) {
            try {
                uid = getPackageManager().getPackageUid(str, 0);
            } catch (NameNotFoundException e) {
            }
        } else {
            uid = 0;
        }
        AppHeader.setupHeaderView(getActivity(), this.mIcon, this.mLabel, str, uid, AppHeader.includeAppInfo(this), 0, header, null);
    }

    protected int getMetricsCategory() {
        return 343;
    }

    public void onDataSaverChanged(boolean isDataSaving) {
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
        if (this.mAppItem.uids.get(uid, false)) {
            updatePrefs(getAppRestrictBackground(), isWhitelisted);
        }
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
        if (this.mAppItem.uids.get(uid, false)) {
            updatePrefs(isBlacklisted, getUnrestrictData());
        }
    }
}
