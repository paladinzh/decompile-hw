package com.android.settings.applications;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import com.android.settings.DividerPreference;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.AppStateSmsPremBridge.SmsState;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public class PremiumSmsAccess extends EmptyTextSettings implements Callback, Callbacks, OnPreferenceChangeListener {
    private ApplicationsState mApplicationsState;
    private Session mSession;
    private AppStateSmsPremBridge mSmsBackend;

    private class PremiumSmsPreference extends DropDownPreference {
        private final AppEntry mAppEntry;

        public PremiumSmsPreference(AppEntry appEntry, Context context) {
            super(context);
            this.mAppEntry = appEntry;
            this.mAppEntry.ensureLabel(context);
            setTitle(this.mAppEntry.label);
            if (this.mAppEntry.icon != null) {
                setIcon(this.mAppEntry.icon);
            }
            setEntries(2131361925);
            setEntryValues(new CharSequence[]{String.valueOf(1), String.valueOf(2), String.valueOf(3)});
            setValue(String.valueOf(getCurrentValue()));
            setSummary("%s");
        }

        private int getCurrentValue() {
            if (this.mAppEntry.extraInfo instanceof SmsState) {
                return ((SmsState) this.mAppEntry.extraInfo).smsState;
            }
            return 0;
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (getIcon() == null) {
                holder.itemView.post(new Runnable() {
                    public void run() {
                        PremiumSmsAccess.this.mApplicationsState.ensureIcon(PremiumSmsPreference.this.mAppEntry);
                        PremiumSmsPreference.this.setIcon(PremiumSmsPreference.this.mAppEntry.icon);
                    }
                });
            }
            super.onBindViewHolder(holder);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mSession = this.mApplicationsState.newSession(this);
        this.mSmsBackend = new AppStateSmsPremBridge(getContext(), this.mApplicationsState, this);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLoading(true, false);
    }

    public void onResume() {
        super.onResume();
        this.mSession.resume();
        this.mSmsBackend.resume();
    }

    public void onPause() {
        this.mSmsBackend.pause();
        this.mSession.pause();
        super.onPause();
    }

    protected int getMetricsCategory() {
        return 388;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mSmsBackend.setSmsState(((PremiumSmsPreference) preference).mAppEntry.info.packageName, Integer.parseInt((String) newValue));
        return true;
    }

    private void updatePrefs(ArrayList<AppEntry> apps) {
        if (apps != null) {
            setEmptyText(2131627220);
            setLoading(false, true);
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getPrefContext());
            screen.setOrderingAsAdded(true);
            for (int i = 0; i < apps.size(); i++) {
                PremiumSmsPreference smsPreference = new PremiumSmsPreference((AppEntry) apps.get(i), getPrefContext());
                smsPreference.setOnPreferenceChangeListener(this);
                screen.addPreference(smsPreference);
            }
            if (apps.size() != 0) {
                DividerPreference summary = new DividerPreference(getPrefContext());
                summary.setSelectable(false);
                summary.setSummary(2131627221);
                summary.setDividerAllowedAbove(true);
                screen.addPreference(summary);
            }
            setPreferenceScreen(screen);
        }
    }

    private void update() {
        updatePrefs(this.mSession.rebuild(AppStateSmsPremBridge.FILTER_APP_PREMIUM_SMS, ApplicationsState.ALPHA_COMPARATOR));
    }

    public void onExtraInfoUpdated() {
        update();
    }

    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        updatePrefs(apps);
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
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
}
