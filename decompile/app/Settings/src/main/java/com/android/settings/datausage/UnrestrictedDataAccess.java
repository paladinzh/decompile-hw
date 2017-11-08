package com.android.settings.datausage;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.InstalledAppDetails;
import com.android.settings.datausage.AppStateDataUsageBridge.DataUsageState;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public class UnrestrictedDataAccess extends SettingsPreferenceFragment implements Callbacks, Callback, OnPreferenceChangeListener {
    private ApplicationsState mApplicationsState;
    private DataSaverBackend mDataSaverBackend;
    private AppStateDataUsageBridge mDataUsageBridge;
    private boolean mExtraLoaded;
    private AppFilter mFilter;
    private Session mSession;
    private boolean mShowSystem;

    private class AccessPreference extends SwitchPreference implements Listener {
        private final AppEntry mEntry;
        private final DataUsageState mState = ((DataUsageState) this.mEntry.extraInfo);

        public AccessPreference(Context context, AppEntry entry) {
            super(context);
            this.mEntry = entry;
            this.mEntry.ensureLabel(getContext());
            setState();
            if (this.mEntry.icon != null) {
                setIcon(this.mEntry.icon);
            }
        }

        public void onAttached() {
            super.onAttached();
            UnrestrictedDataAccess.this.mDataSaverBackend.addListener(this);
        }

        public void onDetached() {
            UnrestrictedDataAccess.this.mDataSaverBackend.remListener(this);
            super.onDetached();
        }

        protected void onClick() {
            if (this.mState == null || !this.mState.isDataSaverBlacklisted) {
                super.onClick();
            } else {
                InstalledAppDetails.startAppInfoFragment(AppDataUsage.class, getContext().getString(2131626899), UnrestrictedDataAccess.this, this.mEntry);
            }
        }

        private void setState() {
            setTitle(this.mEntry.label);
            if (this.mState != null) {
                setChecked(this.mState.isDataSaverWhitelisted);
                if (this.mState.isDataSaverBlacklisted) {
                    setSummary(2131627160);
                } else {
                    setSummary((CharSequence) "");
                }
            }
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (this.mEntry.icon == null) {
                holder.itemView.post(new Runnable() {
                    public void run() {
                        UnrestrictedDataAccess.this.mApplicationsState.ensureIcon(AccessPreference.this.mEntry);
                        AccessPreference.this.setIcon(AccessPreference.this.mEntry.icon);
                    }
                });
            }
            View findViewById = holder.findViewById(16908312);
            int i = (this.mState == null || !this.mState.isDataSaverBlacklisted) ? 0 : 4;
            findViewById.setVisibility(i);
            super.onBindViewHolder(holder);
        }

        public void onDataSaverChanged(boolean isDataSaving) {
        }

        public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
            if (this.mState != null && this.mEntry.info.uid == uid) {
                this.mState.isDataSaverWhitelisted = isWhitelisted;
                reuse();
            }
        }

        public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
            if (this.mState != null && this.mEntry.info.uid == uid) {
                this.mState.isDataSaverBlacklisted = isBlacklisted;
                reuse();
            }
        }
    }

    public void onCreate(Bundle icicle) {
        AppFilter appFilter;
        super.onCreate(icicle);
        setAnimationAllowed(true);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getContext()));
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this);
        this.mShowSystem = icicle != null ? icicle.getBoolean("show_system") : false;
        if (this.mShowSystem) {
            appFilter = ApplicationsState.FILTER_ALL_ENABLED;
        } else {
            appFilter = ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
        }
        this.mFilter = appFilter;
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 43, 0, this.mShowSystem ? 2131626078 : 2131626077).setShowAsAction(0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 43:
                AppFilter appFilter;
                this.mShowSystem = !this.mShowSystem;
                item.setTitle(this.mShowSystem ? 2131626078 : 2131626077);
                if (this.mShowSystem) {
                    appFilter = ApplicationsState.FILTER_ALL_ENABLED;
                } else {
                    appFilter = ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
                }
                this.mFilter = appFilter;
                if (this.mExtraLoaded) {
                    rebuild();
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("show_system", this.mShowSystem);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLoading(true, false);
    }

    public void onResume() {
        super.onResume();
        this.mSession.resume();
        this.mDataUsageBridge.resume();
    }

    public void onPause() {
        super.onPause();
        this.mDataUsageBridge.pause();
        this.mSession.pause();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSession.release();
        this.mDataUsageBridge.release();
    }

    public void onExtraInfoUpdated() {
        this.mExtraLoaded = true;
        rebuild();
    }

    protected int getHelpResource() {
        return 2131626539;
    }

    private void rebuild() {
        ArrayList<AppEntry> apps = this.mSession.rebuild(this.mFilter, ApplicationsState.ALPHA_COMPARATOR);
        if (apps != null) {
            onRebuildComplete(apps);
        }
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
    }

    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        if (getContext() != null) {
            cacheRemoveAllPrefs(getPreferenceScreen());
            int N = apps.size();
            for (int i = 0; i < N; i++) {
                AppEntry entry = (AppEntry) apps.get(i);
                String key = entry.info.packageName + "|" + entry.info.uid;
                AccessPreference preference = (AccessPreference) getCachedPreference(key);
                if (preference == null) {
                    preference = new AccessPreference(getPrefContext(), entry);
                    preference.setKey(key);
                    preference.setOnPreferenceChangeListener(this);
                    getPreferenceScreen().addPreference(preference);
                } else {
                    preference.reuse();
                }
                preference.setOrder(i);
            }
            setLoading(false, true);
            removeCachedPrefs(getPreferenceScreen());
        }
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
    }

    protected int getMetricsCategory() {
        return 349;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!(preference instanceof AccessPreference)) {
            return false;
        }
        AccessPreference accessPreference = (AccessPreference) preference;
        boolean whitelisted = newValue == Boolean.TRUE;
        this.mDataSaverBackend.setIsWhitelisted(accessPreference.mEntry.info.uid, accessPreference.mEntry.info.packageName, whitelisted);
        if (accessPreference.mState != null) {
            accessPreference.mState.isDataSaverWhitelisted = whitelisted;
        }
        return true;
    }
}
