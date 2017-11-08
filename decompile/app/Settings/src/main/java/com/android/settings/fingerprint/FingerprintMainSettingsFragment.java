package com.android.settings.fingerprint;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.Log;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FingerprintMainSettingsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final ArrayMap<String, Integer> DEFAULT_TOUCH_VALUES = new ArrayMap();
    private static final boolean FP_SHOW_NOTIFICATION_ON = SystemProperties.getBoolean("ro.config.fp_add_notification", true);
    private static final boolean FRONT_FP_NAVIGATION_KEYWORD = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final boolean HAS_FP_CUST_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation_plk", false);
    private static final boolean HAS_FP_NAVIGATION = SystemProperties.getBoolean("ro.config.fp_navigation", true);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!BiometricManager.isFingerprintSupported(context)) {
                return null;
            }
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230788;
            result.add(sir);
            return result;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (BiometricManager.isFingerprintSupported(context)) {
                return new ArrayList();
            }
            return null;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new LinkedList();
            if (!FingerprintMainSettingsFragment.HAS_FP_CUST_NAVIGATION) {
                keys.add("key_fp_return_desk");
                keys.add("key_fp_recent_application");
            }
            keys.add("fp_touch_function_category_single");
            keys.add("key_fp_go_back");
            if (!FingerprintMainSettingsFragment.HAS_FP_NAVIGATION) {
                keys.add("key_fp_browse_picture");
            }
            if (!FingerprintMainSettingsFragment.FP_SHOW_NOTIFICATION_ON) {
                keys.add("key_fp_show_notification");
            }
            if (FingerprintMainSettingsFragment.FRONT_FP_NAVIGATION_KEYWORD) {
                keys.add("key_fp_answer_call");
                keys.add("key_fp_take_photo");
                keys.add("key_fp_stop_alarm");
                keys.add("key_fp_show_notification");
                keys.add("key_fp_browse_picture");
            }
            return keys;
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private HwCustFingerprintMainSettingsFragment mHwCustFingerprintMainSettingsFragment;

    static {
        DEFAULT_TOUCH_VALUES.put("fp_answer_call", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_take_photo", Integer.valueOf(1));
        DEFAULT_TOUCH_VALUES.put("fp_stop_alarm", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_show_notification", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_go_back", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_return_desk", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_recent_application", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fp_show_notification", Integer.valueOf(0));
        DEFAULT_TOUCH_VALUES.put("fingerprint_gallery_slide", Integer.valueOf(1));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BiometricManager.isFingerprintSupported(getActivity())) {
            if (Utils.onlySupportPortrait()) {
                getActivity().setRequestedOrientation(1);
            }
            setHasOptionsMenu(true);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(2131230788);
        this.mHwCustFingerprintMainSettingsFragment = (HwCustFingerprintMainSettingsFragment) HwCustUtils.createObj(HwCustFingerprintMainSettingsFragment.class, new Object[]{this});
        removeNavigationItems();
    }

    public void onResume() {
        super.onResume();
        if (BiometricManager.isFingerprintSupported(getActivity())) {
            if (this.mHwCustFingerprintMainSettingsFragment != null) {
                this.mHwCustFingerprintMainSettingsFragment.checkRemovePref("fp_function_category");
            }
            updateSwitchState();
        }
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getActivity());
        super.onPause();
        if (!BiometricManager.isFingerprintSupported(getActivity())) {
        }
    }

    private CustomSwitchPreference findPreferenceAndSetListener(String key) {
        CustomSwitchPreference pref = (CustomSwitchPreference) findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
        }
        return pref;
    }

    private void updateSwitchState() {
        boolean z;
        boolean z2 = true;
        CustomSwitchPreference answerCall = findPreferenceAndSetListener("key_fp_answer_call");
        if (answerCall != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_answer_call", getDefaultValue("fp_answer_call"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            answerCall.setChecked(z);
        }
        CustomSwitchPreference takePhoto = findPreferenceAndSetListener("key_fp_take_photo");
        if (takePhoto != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_take_photo", getDefaultValue("fp_take_photo"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            takePhoto.setChecked(z);
        }
        CustomSwitchPreference stopAlarm = findPreferenceAndSetListener("key_fp_stop_alarm");
        if (stopAlarm != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_stop_alarm", getDefaultValue("fp_stop_alarm"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            stopAlarm.setChecked(z);
        }
        CustomSwitchPreference showNotification = findPreferenceAndSetListener("key_fp_show_notification");
        if (showNotification != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_show_notification", getDefaultValue("fp_show_notification"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            showNotification.setChecked(z);
        }
        CustomSwitchPreference goback = findPreferenceAndSetListener("key_fp_go_back");
        if (goback != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_go_back", getDefaultValue("fp_go_back"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            goback.setChecked(z);
        }
        CustomSwitchPreference returnDesk = findPreferenceAndSetListener("key_fp_return_desk");
        if (returnDesk != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_return_desk", getDefaultValue("fp_return_desk"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            returnDesk.setChecked(z);
        }
        CustomSwitchPreference recentApplication = findPreferenceAndSetListener("key_fp_recent_application");
        if (recentApplication != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_recent_application", getDefaultValue("fp_recent_application"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            recentApplication.setChecked(z);
        }
        CustomSwitchPreference notificationSwitch = findPreferenceAndSetListener("key_fp_show_notification");
        if (notificationSwitch != null) {
            if (Secure.getIntForUser(getContentResolver(), "fp_show_notification", getDefaultValue("fp_show_notification"), UserHandle.myUserId()) == 1) {
                z = true;
            } else {
                z = false;
            }
            notificationSwitch.setChecked(z);
        }
        CustomSwitchPreference browsePicture = findPreferenceAndSetListener("key_fp_browse_picture");
        if (browsePicture != null) {
            if (Secure.getIntForUser(getContentResolver(), "fingerprint_gallery_slide", getDefaultValue("fingerprint_gallery_slide"), UserHandle.myUserId()) != 1) {
                z2 = false;
            }
            browsePicture.setChecked(z2);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        String key = preference.getKey();
        boolean isChecked = ((Boolean) newValue).booleanValue();
        ContentResolver contentResolver;
        String str;
        if ("key_fp_answer_call".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_answer_call";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_take_photo".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_take_photo";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_stop_alarm".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_stop_alarm";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_show_notification".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_show_notification";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_go_back".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_go_back";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_return_desk".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_return_desk";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_recent_application".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_recent_application";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_show_notification".equals(key)) {
            contentResolver = getContentResolver();
            str = "fp_show_notification";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        } else if ("key_fp_browse_picture".equals(key)) {
            contentResolver = getContentResolver();
            str = "fingerprint_gallery_slide";
            if (isChecked) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }
        return true;
    }

    protected int getMetricsCategory() {
        return 81;
    }

    private void removeNavigationItems() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            Log.e("FingerprintMainSettingsFragment", "Cannot get PreferenceScreen! Fingerprint Navigation items won't be removed.");
            return;
        }
        Preference preference = findPreference("fp_touch_function_category_single");
        if (preference != null) {
            screen.removePreference(preference);
        }
        PreferenceCategory category = (PreferenceCategory) findPreference("fp_touch_function_category");
        if (!HAS_FP_NAVIGATION) {
            preference = findPreference("key_fp_browse_picture");
            if (!(category == null || preference == null)) {
                category.removePreference(preference);
            }
        }
        if (!FP_SHOW_NOTIFICATION_ON) {
            preference = findPreference("key_fp_show_notification");
            if (!(category == null || preference == null)) {
                category.removePreference(preference);
            }
        }
        if (!HAS_FP_CUST_NAVIGATION) {
            category = (PreferenceCategory) findPreference("fp_touch_function_category");
            if (category != null) {
                preference = findPreference("key_fp_recent_application");
                if (preference != null) {
                    category.removePreference(preference);
                }
            }
            category = (PreferenceCategory) findPreference("fp_touch_function_category_longpress");
            if (category != null) {
                category.removePreference(findPreference("key_fp_return_desk"));
            }
        }
        if (!(HAS_FP_NAVIGATION || FP_SHOW_NOTIFICATION_ON || HAS_FP_CUST_NAVIGATION)) {
            preference = findPreference("fp_touch_function_category");
            if (preference != null) {
                screen.removePreference(preference);
            }
        }
        if (FRONT_FP_NAVIGATION_KEYWORD) {
            screen.removePreference((PreferenceCategory) findPreference("fp_touch_function_category_longpress"));
            screen.removePreference((PreferenceCategory) findPreference("fp_touch_function_category"));
        }
    }

    private int getDefaultValue(String key) {
        int i = 0;
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            return 0;
        }
        Integer value = (Integer) DEFAULT_TOUCH_VALUES.get(key);
        if (value != null) {
            i = value.intValue();
        }
        return i;
    }
}
