package com.android.contacts.preference;

import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;

public class HwCustContactPreferenceCustomizationImpl extends HwCustContactPreferenceCustomization {
    public void customizePreferences(DisplayOptionsPreferenceFragment aPreferenceFragment) {
        if (HwCustContactFeatureUtils.IS_ATT_MY_INFO) {
            PreferenceCategory lCategory = (PreferenceCategory) aPreferenceFragment.findPreference("AdvancedFunctionsPrefCategory");
            Preference lSetUpMyProfile = aPreferenceFragment.findPreference(aPreferenceFragment.getString(R.string.string_setup_profile));
            if (!(lCategory == null || lSetUpMyProfile == null)) {
                lCategory.removePreference(lSetUpMyProfile);
            }
        }
        if (SystemProperties.getInt("ro.config.disable_user_order", 0) == 1) {
            String mLan = aPreferenceFragment.getActivity().getApplicationContext().getResources().getConfiguration().locale.getLanguage();
            Preference sortOrderPreference = aPreferenceFragment.findPreference("sortOrder");
            Preference displayOrderPreference = aPreferenceFragment.findPreference("displayOrder");
            if (!(!"hu".equals(mLan) || sortOrderPreference == null || displayOrderPreference == null)) {
                sortOrderPreference.setEnabled(false);
                displayOrderPreference.setEnabled(false);
            }
        }
        if (SystemProperties.getInt("ro.config.hw_hide_im_option", 0) == 1) {
            PreferenceCategory category = (PreferenceCategory) aPreferenceFragment.findPreference("key_display_options_category");
            SwitchPreference showImMsgPreference = (SwitchPreference) aPreferenceFragment.findPreference("key_show_im_message");
            if (category != null && showImMsgPreference != null) {
                category.removePreference(showImMsgPreference);
            }
        }
    }
}
