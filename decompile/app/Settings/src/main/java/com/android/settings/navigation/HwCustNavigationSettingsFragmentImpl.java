package com.android.settings.navigation;

import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingNavigationBarPositionPreference;

public class HwCustNavigationSettingsFragmentImpl extends HwCustNavigationSettingsFragment {
    private static final int LEFT_CHECKED = 0;
    private static final int MIDDLE_CHECKED = 1;
    private static final int NAVI_TYPE_PHYSIC = 0;
    private static final int NAVI_TYPE_VIRTUAL = 1;
    private static final int RIGHT_CHECKED = 2;
    private static final String VIRTUAL_KEY_GESTURE_SLIDE = "virtual_key_gesture_slide";
    private static final String VIRTUAL_KEY_GESTURE_SLIDE_HIDE = "virtual_key_gesture_slide_hide";
    private static final String VIRTUAL_KEY_POSITION = "virtual_key_position";
    private static final String VIRTUAL_KEY_SETTINGS_DESCRIPTION = "description";
    private static final String VIRTUAL_KEY_SETTINGS_PREFERENCE_CATEGORY = "virtual_key_settings_preference_category";
    private SwitchPreference mGestureSlidePreference;
    private PreferenceCategory mNavigationTitle;
    private SettingNavigationBarPositionPreference mTextRadioPreference;

    public HwCustNavigationSettingsFragmentImpl(NavigationSettingsFragment navigationSettings) {
        super(navigationSettings);
    }

    public SettingNavigationBarPositionPreference getTextRadioPreference() {
        return this.mTextRadioPreference;
    }

    public void initVirtualKeyPositionPreferences(OnPreferenceChangeListener mVirNaviListener, int selectedNaviType) {
        if (this.mNavigationSettingsFragment != null) {
            PreferenceScreen preferenceScreen = this.mNavigationSettingsFragment.getPreferenceScreen();
            if (preferenceScreen != null) {
                if (selectedNaviType == 0) {
                    if (this.mNavigationTitle != null) {
                        preferenceScreen.removePreference(this.mNavigationTitle);
                        this.mNavigationTitle = null;
                    }
                    if (this.mTextRadioPreference != null) {
                        preferenceScreen.removePreference(this.mTextRadioPreference);
                        this.mTextRadioPreference = null;
                    }
                    if (this.mGestureSlidePreference != null) {
                        preferenceScreen.removePreference(this.mGestureSlidePreference);
                        this.mGestureSlidePreference = null;
                    }
                    return;
                }
                if (this.mNavigationTitle == null) {
                    this.mNavigationTitle = new PreferenceCategory(this.mNavigationSettingsFragment.getActivity());
                    this.mNavigationTitle.setKey(VIRTUAL_KEY_SETTINGS_PREFERENCE_CATEGORY);
                    this.mNavigationTitle.setLayoutResource(2130968916);
                    this.mNavigationTitle.setTitle(2131628880);
                    preferenceScreen.addPreference(this.mNavigationTitle);
                }
                if (this.mTextRadioPreference == null) {
                    this.mTextRadioPreference = new SettingNavigationBarPositionPreference(this.mNavigationSettingsFragment.getActivity());
                    this.mTextRadioPreference.setKey(VIRTUAL_KEY_POSITION);
                    preferenceScreen.addPreference(this.mTextRadioPreference);
                }
                if (this.mGestureSlidePreference == null) {
                    boolean z;
                    this.mGestureSlidePreference = new SwitchPreference(this.mNavigationSettingsFragment.getActivity());
                    this.mGestureSlidePreference.setKey(VIRTUAL_KEY_GESTURE_SLIDE);
                    this.mGestureSlidePreference.setTitle(2131628884);
                    this.mGestureSlidePreference.setSummary(2131628885);
                    this.mGestureSlidePreference.setLayoutResource(2130968710);
                    SwitchPreference switchPreference = this.mGestureSlidePreference;
                    if (System.getIntForUser(this.mNavigationSettingsFragment.getActivity().getContentResolver(), VIRTUAL_KEY_GESTURE_SLIDE_HIDE, 1, UserHandle.myUserId()) > 0) {
                        z = true;
                    } else {
                        z = false;
                    }
                    switchPreference.setChecked(z);
                    this.mGestureSlidePreference.setOnPreferenceChangeListener(mVirNaviListener);
                    preferenceScreen.addPreference(this.mGestureSlidePreference);
                }
                int selectedVirtualKeyType = System.getIntForUser(this.mNavigationSettingsFragment.getActivity().getContentResolver(), VIRTUAL_KEY_POSITION, 1, UserHandle.myUserId());
                if (selectedVirtualKeyType == 1) {
                    this.mTextRadioPreference.initRadioButton(1);
                } else if (selectedVirtualKeyType == 0) {
                    this.mTextRadioPreference.initRadioButton(0);
                } else if (selectedVirtualKeyType == 2) {
                    this.mTextRadioPreference.initRadioButton(2);
                }
            }
        }
    }
}
