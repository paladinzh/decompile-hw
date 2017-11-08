package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.location.RadioButtonPreference;
import java.util.ArrayList;

public class HwCustVirtualKeySettingsImpl extends HwCustVirtualKeySettings {
    private static final String[] CUST_VIRTUAL_KEY_TYPE_LIST = new String[]{"virtual_key_type_1", "virtual_key_type_2", "virtual_key_type_3", "virtual_key_type_4", VIRTUAL_KEY_TYPE_SCREENLOCK, VIRTUAL_KEY_TYPE_SCREENLOCK_SWAP};
    private static final int LEFT_CHECKED = 0;
    private static final int MIDDLE_CHECKED = 1;
    private static final int RIGHT_CHECKED = 2;
    private static final int SCREENLOCK_INDEX = 4;
    private static final int SCREENLOCK_SWAP_INDEX = 5;
    private static final String VIRTUAL_KEY_GESTURE_SLIDE = "virtual_key_gesture_slide";
    private static final String VIRTUAL_KEY_GESTURE_SLIDE_HIDE = "virtual_key_gesture_slide_hide";
    private static final String VIRTUAL_KEY_POSITION = "virtual_key_position";
    private static final String VIRTUAL_KEY_SETTINGS_DESCRIPTION = "description";
    private static final String VIRTUAL_KEY_SETTINGS_PREFERENCE_SCREEN = "virtual_key_settings_preference_screen";
    private static final String VIRTUAL_KEY_TYPE_SCREENLOCK = "virtual_key_type_5";
    private static final String VIRTUAL_KEY_TYPE_SCREENLOCK_SWAP = "virtual_key_type_6";
    private boolean isShowNavigationBarSwitch = SystemProperties.getBoolean("ro.config.showNavigationBar", false);
    private boolean isSoftlockRequired = SystemProperties.getBoolean("ro.config.soft_lock_enable", false);
    private SettingNavigationBarPositionPreference textRadioPreference;

    public boolean isShowNavigationBarFootView() {
        return SystemProperties.getBoolean("ro.config.navbar_support_slide", false);
    }

    public SettingNavigationBarPositionPreference getTextRadioPreference() {
        return this.textRadioPreference;
    }

    public void initVirtualKeyPositionPreferences(OnPreferenceChangeListener mPreferenceChangedListener) {
        PreferenceScreen preferenceScreen = (PreferenceScreen) this.mVirtualKeySettings.findPreference(VIRTUAL_KEY_SETTINGS_PREFERENCE_SCREEN);
        if (preferenceScreen != null) {
            PreferenceCategory navigationTitle = new PreferenceCategory(this.mVirtualKeySettings.getActivity());
            if (navigationTitle != null) {
                navigationTitle.setLayoutResource(2130968916);
                navigationTitle.setTitle(2131628880);
                preferenceScreen.addPreference(navigationTitle);
            }
            this.textRadioPreference = new SettingNavigationBarPositionPreference(this.mVirtualKeySettings.getActivity());
            if (this.textRadioPreference != null) {
                this.textRadioPreference.setKey(VIRTUAL_KEY_POSITION);
                preferenceScreen.addPreference(this.textRadioPreference);
            }
            SwitchPreference gestureSlidePreference = new SwitchPreference(this.mVirtualKeySettings.getActivity());
            if (gestureSlidePreference != null) {
                boolean z;
                gestureSlidePreference.setKey(VIRTUAL_KEY_GESTURE_SLIDE);
                gestureSlidePreference.setTitle(2131628884);
                gestureSlidePreference.setSummary(2131628885);
                gestureSlidePreference.setLayoutResource(2130968710);
                if (System.getIntForUser(this.mVirtualKeySettings.getActivity().getContentResolver(), VIRTUAL_KEY_GESTURE_SLIDE_HIDE, 1, UserHandle.myUserId()) > 0) {
                    z = true;
                } else {
                    z = false;
                }
                gestureSlidePreference.setChecked(z);
                gestureSlidePreference.setOnPreferenceChangeListener(mPreferenceChangedListener);
                preferenceScreen.addPreference(gestureSlidePreference);
            }
            int selectedVirtualKeyType = System.getIntForUser(this.mVirtualKeySettings.getActivity().getContentResolver(), VIRTUAL_KEY_POSITION, 1, UserHandle.myUserId());
            if (selectedVirtualKeyType == 1) {
                this.textRadioPreference.initRadioButton(1);
            } else if (selectedVirtualKeyType == 0) {
                this.textRadioPreference.initRadioButton(0);
            } else if (selectedVirtualKeyType == 2) {
                this.textRadioPreference.initRadioButton(2);
            }
        }
    }

    public HwCustVirtualKeySettingsImpl(VirtualKeySettings virtualKeySettings) {
        super(virtualKeySettings);
    }

    public void addScreenLockPreference(Context context, int virtualKeyType, ArrayList<RadioButtonPreference> virtualKeyList, PreferenceScreen root) {
        boolean z = true;
        if (this.isSoftlockRequired) {
            boolean z2;
            RadioButtonPreference pref1 = new RadioButtonPreference(context, null);
            pref1.setKey(VIRTUAL_KEY_TYPE_SCREENLOCK);
            pref1.setIcon(2130838738);
            pref1.setLayoutResource(2130968945);
            pref1.setOnClickListener(this.mVirtualKeySettings);
            if (virtualKeyType == 4) {
                z2 = true;
            } else {
                z2 = false;
            }
            pref1.setChecked(z2);
            root.addPreference(pref1);
            virtualKeyList.add(pref1);
            RadioButtonPreference pref2 = new RadioButtonPreference(context, null);
            pref2.setKey(VIRTUAL_KEY_TYPE_SCREENLOCK_SWAP);
            pref2.setIcon(2130838739);
            pref2.setLayoutResource(2130968945);
            pref2.setOnClickListener(this.mVirtualKeySettings);
            if (virtualKeyType != 5) {
                z = false;
            }
            pref2.setChecked(z);
            root.addPreference(pref2);
            virtualKeyList.add(pref2);
        }
    }

    public boolean handleCustItemUseStatClick(int index) {
        if (!this.isSoftlockRequired) {
            return false;
        }
        ItemUseStat.getInstance().handleClick(this.mVirtualKeySettings.getActivity(), 2, CUST_VIRTUAL_KEY_TYPE_LIST[index]);
        return true;
    }

    public boolean isShowNavigationBarSwitch() {
        return this.isShowNavigationBarSwitch;
    }
}
