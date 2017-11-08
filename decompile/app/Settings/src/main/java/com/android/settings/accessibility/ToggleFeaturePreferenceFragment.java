package com.android.settings.accessibility;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.TwoStatePreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;

public abstract class ToggleFeaturePreferenceFragment extends SettingsPreferenceFragment {
    protected String mPreferenceKey;
    protected Intent mSettingsIntent;
    protected int mSettingsMenuIcon = -1;
    protected CharSequence mSettingsTitle;
    protected Preference mSummaryPreference;
    protected TwoStatePreference mToggleSwitch;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
        setPreferenceScreen(preferenceScreen);
        initSwitchPreferenceAndHelpCategory(preferenceScreen);
        this.mSummaryPreference = new Preference(getPrefContext()) {
            public void onBindViewHolder(PreferenceViewHolder view) {
                super.onBindViewHolder(view);
                view.setDividerAllowedAbove(false);
                view.setDividerAllowedBelow(false);
                TextView summaryView = (TextView) view.findViewById(16908304);
                summaryView.setText(getSummary());
                sendAccessibilityEvent(summaryView);
            }

            private void sendAccessibilityEvent(View view) {
                AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(ToggleFeaturePreferenceFragment.this.getActivity());
                if (accessibilityManager.isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain();
                    event.setEventType(8);
                    view.onInitializeAccessibilityEvent(event);
                    view.dispatchPopulateAccessibilityEvent(event);
                    accessibilityManager.sendAccessibilityEvent(event);
                }
            }
        };
        this.mSummaryPreference.setSelectable(false);
        this.mSummaryPreference.setPersistent(false);
        this.mSummaryPreference.setLayoutResource(2130969211);
        preferenceScreen.addPreference(this.mSummaryPreference);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onInstallToggleSwitch();
        onProcessArguments(getArguments());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (this.mSettingsTitle != null && this.mSettingsIntent != null) {
            MenuItem menuItem = menu.add(0, 0, 0, this.mSettingsTitle).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), this.mSettingsMenuIcon));
            menuItem.setShowAsAction(1);
            menuItem.setIntent(this.mSettingsIntent);
        }
    }

    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    protected void onProcessArguments(Bundle arguments) {
        if (arguments == null) {
            getPreferenceScreen().removePreference(this.mSummaryPreference);
            return;
        }
        this.mPreferenceKey = arguments.getString("preference_key");
        if (arguments.containsKey("checked")) {
            this.mToggleSwitch.setChecked(arguments.getBoolean("checked"));
        }
        if (arguments.containsKey("title")) {
            setTitle(arguments.getString("title"));
        }
        if (arguments.containsKey("summary")) {
            CharSequence summary = arguments.getCharSequence("summary");
            if (this.mSummaryPreference != null) {
                this.mSummaryPreference.setSummary(summary);
            }
        } else {
            getPreferenceScreen().removePreference(this.mSummaryPreference);
        }
    }

    private void initSwitchPreferenceAndHelpCategory(PreferenceScreen root) {
        this.mToggleSwitch = new CustomSwitchPreference(getActivity());
        PreferenceCategory helpCategory = new PreferenceCategory(getActivity());
        helpCategory.setLayoutResource(2130968916);
        helpCategory.setTitle(2131626521);
        root.addPreference(this.mToggleSwitch);
        root.addPreference(helpCategory);
    }

    protected void onInstallToggleSwitch() {
    }
}
