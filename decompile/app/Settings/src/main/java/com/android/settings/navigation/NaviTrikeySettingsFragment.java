package com.android.settings.navigation;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.ItemUseStat;
import com.android.settings.location.RadioButtonPreference;
import com.android.settings.location.RadioButtonPreference.OnClickListener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NaviTrikeySettingsFragment extends NaviSettingsBaseFragment implements OnClickListener, Indexable {
    private static final int[] NAVI_LIGHT_OPT_VALUES = new int[]{0, 1};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enable) {
            if (!NaviUtils.isFrontFingerNaviEnabled()) {
                return null;
            }
            if (NaviUtils.isFrontFingerNaviEnabled() && !NaviUtils.isTrikeyDevice()) {
                return null;
            }
            new SearchIndexableResource(context).xmlResId = 2131230820;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (NaviUtils.isTrikeyDevice()) {
                result.add("enable_virtual_key");
            }
            if (System.getInt(context.getContentResolver(), "swap_key_position", NaviUtils.getTrikeyTypeDefaultValue()) == -1) {
                result.add("navi_light_category");
                result.add("navi_light_key");
            }
            return result;
        }
    };
    private static final int[] TRIKEY_TYPE_DB_VALUE = new int[]{-1, 0, 1};
    private static final String[] TRIKEY_TYPE_PREF_KEYS = new String[]{"physic_trikey_type_1", "physic_trikey_type_2", "physic_trikey_type_3"};
    private static ArrayMap<String, Integer> sTrikeyValues = new ArrayMap();
    private Context mContext;
    private SwitchPreference mEnableNaviPref;
    private NaviLightPreference mNaviLightPref;
    private OnPreferenceChangeListener mPreferenceChangedListener = new NaviOnPreferenceChangeListener();
    private List<RadioButtonPreference> mTrikeyTypePrefList;

    public class NaviOnPreferenceChangeListener implements OnPreferenceChangeListener {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (NaviTrikeySettingsFragment.this.mContext == null) {
                return false;
            }
            Log.d("NaviTrikeySettingsFragment", "Preference is " + preference.getKey() + ", value = " + newValue);
            if (preference instanceof NaviLightPreference) {
                try {
                    int value = Integer.parseInt((String) newValue);
                    ItemUseStat.getInstance().handleClick(NaviTrikeySettingsFragment.this.mContext, 2, "button_light_mode", value);
                    System.putInt(NaviTrikeySettingsFragment.this.getContentResolver(), "button_light_mode", value);
                    NaviTrikeySettingsFragment.this.updateNaviLightSummary(value, NaviTrikeySettingsFragment.this.getNaviLightSummaries());
                } catch (NumberFormatException e) {
                    Log.e("NaviTrikeySettingsFragment", "could not persist screen timeout setting");
                    e.printStackTrace();
                }
                return true;
            } else if (!(preference instanceof SwitchPreference)) {
                return false;
            } else {
                boolean isChecked = ((Boolean) newValue).booleanValue();
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(NaviTrikeySettingsFragment.this.getActivity(), preference, newValue);
                if ("enable_virtual_key".equals(preference.getKey())) {
                    int i;
                    ContentResolver contentResolver = NaviTrikeySettingsFragment.this.getActivity().getContentResolver();
                    String str = "enable_navbar";
                    if (isChecked) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    System.putInt(contentResolver, str, i);
                }
                return true;
            }
        }
    }

    static {
        for (int index = 0; index < TRIKEY_TYPE_PREF_KEYS.length; index++) {
            sTrikeyValues.put(TRIKEY_TYPE_PREF_KEYS[index], Integer.valueOf(TRIKEY_TYPE_DB_VALUE[index]));
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        if (!NaviUtils.isFrontFingerNaviEnabled()) {
            getActivity().finish();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    public void onPause() {
        super.onPause();
        stopAnimLoop();
    }

    protected FrontFingerDemoPagerAdapter buildPagerAdapter() {
        FrontFingerDemoPagerAdapter adapter = super.buildPagerAdapter();
        adapter.setPageLayoutRes(2130968897);
        return adapter;
    }

    private void updatePreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            screen.removeAll();
        }
        addPreferencesFromResource(2131230820);
        initEnableNaviPref();
        int currentType = initTrikeyTypePrefs();
        initNaviLightPref(currentType);
        initIntroAnime(currentType);
    }

    private void initIntroAnime(int currentTrikeyType) {
        if (currentTrikeyType == -1) {
            this.mViewPager.setCurrentItem(0);
            this.mViewPager.setVisibility(0);
            this.mSpotView.setVisibility(0);
            LazyLoadingAnimationContainer anime = this.mAdapter.getLiveAnimation(0);
            Log.d("NaviTrikeySettingsFragment", "initIntroAnime try to start the first animation, anime = " + anime);
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

    private void initNaviLightPref(int currentTrikeyType) {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            Preference pref;
            if (currentTrikeyType == -1) {
                pref = findPreference("navi_light_key");
                findPreference("navi_light_category").setTitle(this.mContext.getString(2131628736));
                screen.removePreference(pref);
                return;
            }
            pref = findPreference("navi_light_key");
            if (pref != null && (pref instanceof NaviLightPreference)) {
                this.mNaviLightPref = (NaviLightPreference) pref;
            }
            if (this.mNaviLightPref == null) {
                Log.e("NaviTrikeySettingsFragment", "failed to initialize navigation light preference.");
                return;
            }
            CharSequence[] entries = getNaviLightSummaries();
            this.mNaviLightPref.setEntries(entries);
            this.mNaviLightPref.setNetherSummary(getResources().getString(2131628684));
            int currentLightMode = System.getInt(getContentResolver(), "button_light_mode", NAVI_LIGHT_OPT_VALUES[1]);
            updateNaviLightSummary(currentLightMode, entries);
            this.mNaviLightPref.setValue(String.valueOf(currentLightMode));
            this.mNaviLightPref.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
    }

    private CharSequence[] getNaviLightSummaries() {
        CharSequence[] entries = new CharSequence[NAVI_LIGHT_OPT_VALUES.length];
        entries[0] = String.format(getResources().getString(2131628681), new Object[]{Integer.valueOf(5)});
        entries[1] = getResources().getString(2131628680);
        return entries;
    }

    private void updateNaviLightSummary(int value, CharSequence[] entries) {
        if (this.mNaviLightPref != null && entries != null && entries.length == NAVI_LIGHT_OPT_VALUES.length) {
            if (value == NAVI_LIGHT_OPT_VALUES[0]) {
                this.mNaviLightPref.setSummary(entries[0]);
            } else {
                this.mNaviLightPref.setSummary(entries[1]);
            }
        }
    }

    private void initEnableNaviPref() {
        boolean z = false;
        if (NaviUtils.isTrikeyDevice()) {
            removePreference("enable_virtual_key");
            this.mEnableNaviPref = null;
            return;
        }
        this.mEnableNaviPref = null;
        Preference pref = findPreference("enable_virtual_key");
        if (pref instanceof SwitchPreference) {
            this.mEnableNaviPref = (SwitchPreference) pref;
        }
        if (this.mEnableNaviPref == null) {
            Log.e("NaviTrikeySettingsFragment", "enable_virtual_key not found");
            return;
        }
        int defaultValue = NaviUtils.getEnableNaviDefaultValue();
        SwitchPreference switchPreference = this.mEnableNaviPref;
        if (System.getInt(this.mContext.getContentResolver(), "enable_navbar", defaultValue) > 0) {
            z = true;
        }
        switchPreference.setChecked(z);
        this.mEnableNaviPref.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
    }

    private int initTrikeyTypePrefs() {
        int currentType = System.getInt(this.mContext.getContentResolver(), "swap_key_position", NaviUtils.getTrikeyTypeDefaultValue());
        this.mTrikeyTypePrefList = new ArrayList();
        for (int index = 0; index < TRIKEY_TYPE_PREF_KEYS.length; index++) {
            RadioButtonPreference radioPreference = null;
            Preference pref = findPreference(TRIKEY_TYPE_PREF_KEYS[index]);
            if (pref instanceof RadioButtonPreference) {
                radioPreference = (RadioButtonPreference) pref;
            }
            if (radioPreference == null) {
                Log.e("NaviTrikeySettingsFragment", "can not find preference type_" + index);
            } else {
                radioPreference.setLayoutResource(2130968945);
                radioPreference.setChecked(currentType == TRIKEY_TYPE_DB_VALUE[index]);
                radioPreference.setOnClickListener(this);
                this.mTrikeyTypePrefList.add(radioPreference);
            }
        }
        return currentType;
    }

    public void onRadioButtonClicked(RadioButtonPreference selectedPref) {
        int size = this.mTrikeyTypePrefList.size();
        for (int index = 0; index < size; index++) {
            RadioButtonPreference pref = (RadioButtonPreference) this.mTrikeyTypePrefList.get(index);
            boolean prefSelect = pref.equals(selectedPref);
            if (prefSelect && !pref.isChecked()) {
                ItemUseStat.getInstance().handleClick(this.mContext, 2, "swap_key_position", TRIKEY_TYPE_DB_VALUE[index]);
                System.putInt(this.mContext.getContentResolver(), "swap_key_position", TRIKEY_TYPE_DB_VALUE[index]);
                updatePreferences();
                Index.getInstance(getActivity()).updateFromClassNameResource(NaviTrikeySettingsFragment.class.getCanonicalName(), true, true);
            }
            pref.setChecked(prefSelect);
        }
    }

    public boolean useNormalDividerOnly() {
        return true;
    }
}
