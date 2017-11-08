package com.android.settings.pressure;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.location.RadioButtonPreference;
import com.android.settings.location.RadioButtonPreference.OnClickListener;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.views.pagerHelper.PagerHelperPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class PressureLauncherShortcutFragment extends PagerHelperPreferenceFragment implements OnPreferenceChangeListener, Indexable, OnClickListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!PressureUtil.isSupportPressureHabit(context)) {
                return null;
            }
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230840;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            if (PressureUtil.isSupportPressureHabit(context)) {
                return new ArrayList();
            }
            return null;
        }
    };
    private static final String TAG = PressureLauncherShortcutFragment.class.getSimpleName();
    private RadioButtonPreference mForcePressOnly;
    private Dialog mForcePressOnlyDialog;
    private boolean mIsForcePressOnlyEnabled = false;
    private boolean mIsLaucnherShortcutEnabled = false;
    private CustomSwitchPreference mLauncherShorcutSwitch;
    private CustomSwitchPreference mStarredOptionSwitch;
    private RadioButtonPreference mTwoPressDistributed;

    public int[] getDrawables() {
        return new int[]{2130838601, 2130838592};
    }

    public int[] getSummaries() {
        return new int[]{2131628426, 2131628427};
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230840);
        this.mLauncherShorcutSwitch = (CustomSwitchPreference) findPreference("launcher_shortcut_key_toggle");
        this.mLauncherShorcutSwitch.setOnPreferenceChangeListener(this);
        this.mStarredOptionSwitch = (CustomSwitchPreference) findPreference("starred_optioin_switch");
        this.mStarredOptionSwitch.setOnPreferenceChangeListener(this);
        this.mTwoPressDistributed = (RadioButtonPreference) findPreference("two_press_distributed");
        this.mTwoPressDistributed.setOnClickListener(this);
        this.mForcePressOnly = (RadioButtonPreference) findPreference("force_press_only");
        this.mForcePressOnly.setOnClickListener(this);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mForcePressOnlyDialog != null && this.mForcePressOnlyDialog.isShowing()) {
            this.mForcePressOnlyDialog.dismiss();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateState();
    }

    private void updateState() {
        this.mIsLaucnherShortcutEnabled = isChecked("pressure_launcher_shortcut_enable");
        if (this.mLauncherShorcutSwitch != null) {
            this.mLauncherShorcutSwitch.setChecked(this.mIsLaucnherShortcutEnabled);
        }
        updateOneStepActionCategory();
        updateStarredOptionSwitch();
    }

    private void updateStarredOptionSwitch() {
        boolean z = false;
        if (this.mStarredOptionSwitch != null) {
            CustomSwitchPreference customSwitchPreference = this.mStarredOptionSwitch;
            if (this.mIsLaucnherShortcutEnabled && !this.mIsForcePressOnlyEnabled) {
                z = true;
            }
            customSwitchPreference.setEnabled(z);
            this.mStarredOptionSwitch.setChecked(isChecked("pressure_edit_shortcut_mark"));
        }
    }

    private void updateOneStepActionCategory() {
        if (this.mTwoPressDistributed != null && this.mForcePressOnly != null) {
            switch (System.getInt(getActivity().getContentResolver(), "pressure_one_step_action", 0)) {
                case 0:
                    this.mTwoPressDistributed.setChecked(true);
                    this.mForcePressOnly.setChecked(false);
                    this.mIsForcePressOnlyEnabled = false;
                    break;
                case 1:
                    this.mTwoPressDistributed.setChecked(false);
                    this.mForcePressOnly.setChecked(true);
                    this.mIsForcePressOnlyEnabled = true;
                    break;
            }
            this.mTwoPressDistributed.setEnabled(this.mIsLaucnherShortcutEnabled);
            this.mForcePressOnly.setEnabled(this.mIsLaucnherShortcutEnabled);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("launcher_shortcut_key_toggle".equals(preference.getKey())) {
            handleChecked("pressure_launcher_shortcut_enable", ((Boolean) newValue).booleanValue(), preference);
            updateState();
        } else if ("starred_optioin_switch".equals(preference.getKey())) {
            handleChecked("pressure_edit_shortcut_mark", ((Boolean) newValue).booleanValue(), preference);
        }
        return true;
    }

    public void onRadioButtonClicked(RadioButtonPreference preference) {
        if (preference != null && !preference.isChecked()) {
            if (preference == this.mForcePressOnly) {
                showForcePressAlertDialog();
            } else if (preference == this.mTwoPressDistributed) {
                System.putInt(getActivity().getContentResolver(), "pressure_one_step_action", 0);
                updateOneStepActionCategory();
                updateStarredOptionSwitch();
            }
            ItemUseStat.getInstance().handleClick(getActivity(), 2, preference.getKey());
        }
    }

    private void showForcePressAlertDialog() {
        if (this.mForcePressOnlyDialog == null) {
            this.mForcePressOnlyDialog = new Builder(getActivity()).setTitle(2131628437).setMessage(2131628477).setPositiveButton(2131625656, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.putInt(PressureLauncherShortcutFragment.this.getActivity().getContentResolver(), "pressure_one_step_action", 1);
                    PressureLauncherShortcutFragment.this.updateOneStepActionCategory();
                    PressureLauncherShortcutFragment.this.updateStarredOptionSwitch();
                }
            }).setNegativeButton(2131625657, null).create();
        }
        if (this.mForcePressOnlyDialog.isShowing()) {
            this.mForcePressOnlyDialog.dismiss();
        }
        this.mForcePressOnlyDialog.show();
    }

    private boolean isChecked(String key) {
        return System.getInt(getContentResolver(), key, 1) == 1;
    }

    private void handleChecked(String dbKey, boolean isChecked, Preference preference) {
        System.putInt(getContentResolver(), dbKey, isChecked ? 1 : 0);
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, Boolean.valueOf(isChecked));
    }
}
