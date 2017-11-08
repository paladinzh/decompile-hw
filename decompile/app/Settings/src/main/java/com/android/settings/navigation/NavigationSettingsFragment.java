package com.android.settings.navigation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.ImageRadioDialogPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.NetherSummaryImageRadioPreference;
import com.android.settings.RadioListPreference;
import com.android.settings.RadioListPreferenceManager;
import com.android.settings.RadioListPreferenceManager.OnOptionSelectedListener;
import com.android.settings.SettingNavigationBarPositionPreference;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NavigationSettingsFragment extends NaviSettingsBaseFragment implements OnOptionSelectedListener, Indexable {
    private static final boolean IS_NAVBAR_SUPPORT_SLIDE = SystemProperties.getBoolean("ro.config.navbar_support_slide", false);
    private static final String[] NAVI_TYPE_PREF_KEYS = new String[]{"navi_single_type_1", "navi_single_type_2"};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enable) {
            if (!NaviUtils.isFrontFingerNaviEnabled()) {
                return null;
            }
            if (NaviUtils.isFrontFingerNaviEnabled() && NaviUtils.isTrikeyDevice()) {
                return null;
            }
            new SearchIndexableResource(context).xmlResId = 2131230819;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (!NaviUtils.isFrontFingerNaviEnabled()) {
                return null;
            }
            if (NaviUtils.isFrontFingerNaviEnabled() && NaviUtils.isTrikeyDevice()) {
                return null;
            }
            List<SearchIndexableRaw> result = new LinkedList();
            int enableVirtualNavi = System.getInt(context.getContentResolver(), "enable_navbar", NaviUtils.getEnableNaviDefaultValue());
            SearchIndexableRaw naviFuncEntry = new SearchIndexableRaw(context);
            String screenTitle = context.getString(2131628846);
            naviFuncEntry.screenTitle = screenTitle;
            if (enableVirtualNavi == 0) {
                naviFuncEntry.title = context.getString(2131628736);
            } else {
                naviFuncEntry.title = context.getString(2131628910);
                SearchIndexableRaw virtualTypeEntry = new SearchIndexableRaw(context);
                virtualTypeEntry.title = context.getString(2131628913);
                virtualTypeEntry.screenTitle = screenTitle;
                result.add(virtualTypeEntry);
                if (Utils.isChinaArea()) {
                    SearchIndexableRaw virtualHideEntry = new SearchIndexableRaw(context);
                    virtualHideEntry.title = context.getString(2131627417);
                    virtualHideEntry.screenTitle = screenTitle;
                    result.add(virtualHideEntry);
                }
                if (NavigationSettingsFragment.IS_NAVBAR_SUPPORT_SLIDE) {
                    SearchIndexableRaw navigationTitle = new SearchIndexableRaw(context);
                    navigationTitle.title = context.getString(2131628880);
                    navigationTitle.screenTitle = screenTitle;
                    result.add(navigationTitle);
                    SearchIndexableRaw gestureSlidePreference = new SearchIndexableRaw(context);
                    gestureSlidePreference.title = context.getString(2131628884);
                    gestureSlidePreference.screenTitle = screenTitle;
                    result.add(gestureSlidePreference);
                }
            }
            result.add(naviFuncEntry);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            int enableVirtualNavi = System.getInt(context.getContentResolver(), "enable_navbar", NaviUtils.getEnableNaviDefaultValue());
            if (enableVirtualNavi == 0) {
                result.add("virtual_navi_bar_type");
            }
            if (enableVirtualNavi == 0 || !Utils.isChinaArea()) {
                result.add("virtual_key_hide");
            }
            return result;
        }
    };
    private static Map<String, Integer> sVirtualNaviRes;
    private static Map<Integer, Integer> sVirtualNaviSummaryRes;
    private Context mContext;
    private ContentObserver mCurrentVirtualObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int navigationBarType = System.getInt(NavigationSettingsFragment.this.getContentResolver(), "virtual_key_position", 0);
            SettingNavigationBarPositionPreference textRadioPreference = NavigationSettingsFragment.this.mHwCustNavigationSettingsFragment.getTextRadioPreference();
            if (textRadioPreference != null) {
                textRadioPreference.initRadioButton(navigationBarType);
            }
        }
    };
    private SwitchPreference mHideVirPreference;
    private HwCustNavigationSettingsFragment mHwCustNavigationSettingsFragment;
    private RadioListPreferenceManager mNaviTypePrefManager;
    private ArrayList<RadioListPreference> mNaviTypePreferenceList;
    private NetherSummaryImageRadioPreference mPhysicNaviPref;
    private VirNaviPreferenceChangeListener mVirNaviListener;
    private ImageRadioDialogPreference mVirtualNaviDlgPreference;
    private NetherSummaryImageRadioPreference mVirtualNaviPref;

    public class VirNaviPreferenceChangeListener implements OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int i = 0;
            if (NavigationSettingsFragment.this.mContext == null) {
                return false;
            }
            Log.d("NavigationSettingsFragment", "Preference is " + (preference != null ? preference.getKey() : "null preference") + ", value = " + newValue);
            if (preference instanceof ImageRadioDialogPreference) {
                try {
                    int value = Integer.parseInt((String) newValue);
                    ItemUseStat.getInstance().handleClick(NavigationSettingsFragment.this.mContext, 2, "virtual_key_type", value);
                    System.putInt(NavigationSettingsFragment.this.getContentResolver(), "virtual_key_type", value);
                } catch (NumberFormatException e) {
                    Log.e("NavigationSettingsFragment", "could not persist screen timeout setting, invalid value : " + newValue);
                }
                NavigationSettingsFragment.this.updatePreferences(false);
                return true;
            } else if (!(preference instanceof SwitchPreference)) {
                return false;
            } else {
                boolean isChecked = ((Boolean) newValue).booleanValue();
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(NavigationSettingsFragment.this.mContext, preference, newValue);
                if ("virtual_key_hide".equals(preference.getKey())) {
                    System.putInt(NavigationSettingsFragment.this.mContext.getContentResolver(), "hide_virtual_key", isChecked ? 1 : 0);
                } else if ("virtual_key_gesture_slide".equals(preference.getKey())) {
                    ContentResolver contentResolver = NavigationSettingsFragment.this.mContext.getContentResolver();
                    String str = "virtual_key_gesture_slide_hide";
                    if (isChecked) {
                        i = 1;
                    }
                    System.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
                }
                return true;
            }
        }
    }

    static {
        initVirtualNaviRes();
    }

    private static void initVirtualNaviRes() {
        sVirtualNaviRes = new HashMap();
        sVirtualNaviRes.put(String.valueOf(0), Integer.valueOf(2130838734));
        sVirtualNaviRes.put(String.valueOf(1), Integer.valueOf(2130838735));
        sVirtualNaviRes.put(String.valueOf(2), Integer.valueOf(2130838736));
        sVirtualNaviRes.put(String.valueOf(3), Integer.valueOf(2130838737));
        sVirtualNaviSummaryRes = new HashMap();
        sVirtualNaviSummaryRes.put(Integer.valueOf(0), Integer.valueOf(2131628737));
        sVirtualNaviSummaryRes.put(Integer.valueOf(1), Integer.valueOf(2131628738));
        sVirtualNaviSummaryRes.put(Integer.valueOf(2), Integer.valueOf(2131628737));
        sVirtualNaviSummaryRes.put(Integer.valueOf(3), Integer.valueOf(2131628738));
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!NaviUtils.isFrontFingerNaviEnabled()) {
            getActivity().finish();
        }
        this.mHwCustNavigationSettingsFragment = (HwCustNavigationSettingsFragment) HwCustUtils.createObj(HwCustNavigationSettingsFragment.class, new Object[]{this});
        init();
    }

    public void onResume() {
        super.onResume();
        updatePreferences(true);
        getContentResolver().registerContentObserver(System.getUriFor("virtual_key_position"), true, this.mCurrentVirtualObserver);
    }

    public void onPause() {
        super.onPause();
        stopAnimLoop();
        getContentResolver().unregisterContentObserver(this.mCurrentVirtualObserver);
    }

    protected FrontFingerDemoPagerAdapter buildPagerAdapter() {
        FrontFingerDemoPagerAdapter adapter = super.buildPagerAdapter();
        adapter.setPageLayoutRes(2130968897);
        return adapter;
    }

    private void init() {
        this.mContext = getActivity();
        this.mVirNaviListener = new VirNaviPreferenceChangeListener();
        this.mNaviTypePreferenceList = new ArrayList();
        addPreferencesFromResource(2131230819);
        initNaviTypePrefs();
    }

    private void updatePreferences(boolean updateChecked) {
        int selectedNaviType = 0;
        int selectedVirtualKeyType = System.getInt(this.mContext.getContentResolver(), "virtual_key_type", 0);
        if (System.getInt(this.mContext.getContentResolver(), "enable_navbar", NaviUtils.getEnableNaviDefaultValue()) != 0) {
            selectedNaviType = 1;
        }
        updateNaviTypePrefs(selectedNaviType, selectedVirtualKeyType, updateChecked);
        initVirtualNaviTypeDialogPref(selectedNaviType, selectedVirtualKeyType);
        initHideVirNaviPref(selectedNaviType);
        initIntroAnime(selectedNaviType);
        if (this.mHwCustNavigationSettingsFragment != null && IS_NAVBAR_SUPPORT_SLIDE) {
            this.mHwCustNavigationSettingsFragment.initVirtualKeyPositionPreferences(this.mVirNaviListener, selectedNaviType);
        }
    }

    private void initHideVirNaviPref(int selectedNaviType) {
        boolean z = false;
        Preference pref = findPreference("virtual_key_hide");
        if (!Utils.isChinaArea()) {
            Log.d("NavigationSettingsFragment", "do not show hide-virtual-keys switch on non-China version");
            removePreference("virtual_key_hide");
        } else if (selectedNaviType == 0) {
            Log.d("NavigationSettingsFragment", "do not show hide-virtual-keys switch when virtual-key disabled");
            removePreference("virtual_key_hide");
        } else {
            if (pref == null) {
                this.mHideVirPreference = createNewHideVirPref();
                PreferenceScreen screen = getPreferenceScreen();
                if (screen != null) {
                    screen.addPreference(this.mHideVirPreference);
                }
            } else if (pref instanceof SwitchPreference) {
                this.mHideVirPreference = (SwitchPreference) pref;
            }
            if (this.mHideVirPreference != null) {
                SwitchPreference switchPreference = this.mHideVirPreference;
                if (System.getInt(this.mContext.getContentResolver(), "hide_virtual_key", 0) > 0) {
                    z = true;
                }
                switchPreference.setChecked(z);
                this.mHideVirPreference.setOnPreferenceChangeListener(this.mVirNaviListener);
            }
        }
    }

    private NetherSummaryImageRadioPreference initNaviTypePref(String key) {
        Preference pref = findPreference(key);
        if (pref == null || !(pref instanceof NetherSummaryImageRadioPreference)) {
            return null;
        }
        NetherSummaryImageRadioPreference radioPreference = (NetherSummaryImageRadioPreference) pref;
        radioPreference.setLayoutResource(2130968946);
        this.mNaviTypePreferenceList.add(radioPreference);
        return radioPreference;
    }

    private void updateNaviTypePrefs(int selectedNaviType, int selectedVirtualKeyType, boolean updateChecked) {
        boolean z = true;
        if (this.mPhysicNaviPref != null && updateChecked) {
            boolean z2;
            NetherSummaryImageRadioPreference netherSummaryImageRadioPreference = this.mPhysicNaviPref;
            if (selectedNaviType == 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            netherSummaryImageRadioPreference.setChecked(z2);
        }
        if (this.mVirtualNaviPref != null) {
            if (updateChecked) {
                NetherSummaryImageRadioPreference netherSummaryImageRadioPreference2 = this.mVirtualNaviPref;
                if (selectedNaviType == 0) {
                    z = false;
                }
                netherSummaryImageRadioPreference2.setChecked(z);
            }
            this.mVirtualNaviPref.setIcon(((Integer) sVirtualNaviRes.get(String.valueOf(selectedVirtualKeyType))).intValue());
        }
    }

    private void initNaviTypePrefs() {
        this.mNaviTypePreferenceList.clear();
        this.mPhysicNaviPref = initNaviTypePref("navi_single_type_1");
        this.mVirtualNaviPref = initNaviTypePref("navi_single_type_2");
        if (this.mPhysicNaviPref != null) {
            this.mPhysicNaviPref.setNetherSummary(this.mContext.getText(2131628911));
        }
        if (this.mVirtualNaviPref != null) {
            this.mVirtualNaviPref.setNetherSummary(this.mContext.getText(2131628912));
        }
        this.mNaviTypePrefManager = new RadioListPreferenceManager(this.mNaviTypePreferenceList);
        this.mNaviTypePrefManager.setOnOptionSelectedListener(this);
    }

    private void initVirtualNaviTypeDialogPref(int selectedNaviType, int selectedVirtualKeyType) {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            Preference prefCategory = findPreference("navi_func_category");
            if (selectedNaviType == 0) {
                prefCategory.setTitle(this.mContext.getString(2131628736));
                removePreference("virtual_navi_bar_type");
                return;
            }
            prefCategory.setTitle(this.mContext.getString(2131628910));
            Preference pref = findPreference("virtual_navi_bar_type");
            if (pref == null) {
                pref = createNewVirNaviDlgPref();
                screen.addPreference(pref);
            }
            if (pref instanceof ImageRadioDialogPreference) {
                this.mVirtualNaviDlgPreference = (ImageRadioDialogPreference) pref;
                this.mVirtualNaviDlgPreference.setListAdapterResMap(sVirtualNaviRes);
                this.mVirtualNaviDlgPreference.setValue(String.valueOf(selectedVirtualKeyType));
                this.mVirtualNaviDlgPreference.setOnPreferenceChangeListener(this.mVirNaviListener);
                this.mVirtualNaviDlgPreference.setSummary(this.mContext.getString(((Integer) sVirtualNaviSummaryRes.get(Integer.valueOf(selectedVirtualKeyType))).intValue()));
            }
        }
    }

    private SwitchPreference createNewHideVirPref() {
        SwitchPreference pref = new SwitchPreference(this.mContext, null);
        pref.setTitle(this.mContext.getText(2131627417));
        pref.setKey("virtual_key_hide");
        return pref;
    }

    private ImageRadioDialogPreference createNewVirNaviDlgPref() {
        ImageRadioDialogPreference pref = new ImageRadioDialogPreference(this.mContext, null);
        pref.setTitle(this.mContext.getText(2131628913));
        pref.setKey("virtual_navi_bar_type");
        pref.setWidgetLayoutResource(2130968998);
        pref.setLayoutResource(2130968991);
        return pref;
    }

    private void initIntroAnime(int selectedNaviType) {
        if (selectedNaviType == 0) {
            this.mViewPager.setCurrentItem(0);
            this.mViewPager.setVisibility(0);
            this.mSpotView.setVisibility(0);
            LazyLoadingAnimationContainer anime = this.mAdapter.getLiveAnimation(0);
            Log.d("NavigationSettingsFragment", "initIntroAnime try to start the first animation, anime = " + anime);
            if (anime != null) {
                anime.start();
            }
            startAnimeLoop();
            return;
        }
        this.mViewPager.setVisibility(8);
        this.mSpotView.setVisibility(8);
        this.mViewPager.setCurrentItem(0);
        stopAnimLoop();
    }

    public void onOptionSelected(RadioListPreference preference, int index) {
        Log.d("NavigationSettingsFragment", "onOptionSelected index = " + index + ", preference = " + preference);
        int enableNavi = index == 0 ? 0 : 1;
        System.putInt(this.mContext.getContentResolver(), "enable_navbar", enableNavi);
        ItemUseStat.getInstance().handleClick(this.mContext, 2, "enable_navbar", enableNavi);
        updatePreferences(false);
        Index.getInstance(this.mContext).updateFromClassNameResource(NavigationSettingsFragment.class.getName(), true, true);
    }

    public boolean isSelectEnabled() {
        return true;
    }
}
