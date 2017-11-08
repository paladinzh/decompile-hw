package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.RadioListPreferenceManager.OnOptionSelectedListener;
import com.android.settings.location.RadioButtonPreference;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualKeySettings extends SettingsPreferenceFragment implements OnClickListener, OnDismissListener, OnOptionSelectedListener, RadioButtonPreference.OnClickListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enable) {
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                return null;
            }
            boolean hasNavigationBar = context.getResources().getBoolean(17956970);
            String navBarOverride = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
            if (!hasNavigationBar) {
                return null;
            }
            new SearchIndexableResource(context).xmlResId = 2131230926;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            if (!NaviUtils.isFrontFingerNaviEnabled()) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = context.getString(2131628846);
                data.screenTitle = context.getString(2131628846);
                result.add(data);
            }
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (!PressureUtil.isSupportPressureHabit(context)) {
                result.add("virtual_notification_key_toggle");
            }
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                result.add("virtual_key");
            } else {
                result.add("enable_virtual_key");
                result.add(context.getString(2131627539));
            }
            return result;
        }
    };
    private static final String[] VIRTUAL_KEY_TYPE_LIST = new String[]{"virtual_key_type_1", "virtual_key_type_2", "virtual_key_type_3", "virtual_key_type_4"};
    private ContentObserver mCurrentVirtualObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int navigationBarType = System.getInt(VirtualKeySettings.this.getContentResolver(), "virtual_key_position", 0);
            if (VirtualKeySettings.this.mHwCustVirtualKeySettings.getTextRadioPreference() != null) {
                VirtualKeySettings.this.mHwCustVirtualKeySettings.getTextRadioPreference().initRadioButton(navigationBarType);
            }
        }
    };
    private HwCustVirtualKeySettings mHwCustVirtualKeySettings;
    private AlertDialog mNotificationDialog;
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int i = 0;
            if (VirtualKeySettings.this.getActivity() == null) {
                return false;
            }
            boolean isChecked = ((Boolean) newValue).booleanValue();
            if ("virtual_notification_key_toggle".equals(preference.getKey())) {
                int i2;
                ContentResolver contentResolver = VirtualKeySettings.this.getContentResolver();
                String str = "virtual_notification_key_type";
                if (isChecked) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                System.putInt(contentResolver, str, i2);
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(VirtualKeySettings.this.getActivity(), preference, newValue);
                return true;
            } else if (!(preference instanceof SwitchPreference)) {
                return false;
            } else {
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(VirtualKeySettings.this.getActivity(), preference, newValue);
                ContentResolver contentResolver2;
                String str2;
                if ("virtual_key".equals(preference.getKey())) {
                    contentResolver2 = VirtualKeySettings.this.getActivity().getContentResolver();
                    str2 = "hide_virtual_key";
                    if (isChecked) {
                        i = 1;
                    }
                    System.putInt(contentResolver2, str2, i);
                } else if ("virtual_key_gesture_slide".equals(preference.getKey())) {
                    contentResolver2 = VirtualKeySettings.this.getActivity().getContentResolver();
                    str2 = "virtual_key_gesture_slide_hide";
                    if (isChecked) {
                        i = 1;
                    }
                    System.putIntForUser(contentResolver2, str2, i, UserHandle.myUserId());
                } else {
                    contentResolver2 = VirtualKeySettings.this.getActivity().getContentResolver();
                    str2 = "enable_navbar";
                    if (isChecked) {
                        i = 1;
                    }
                    System.putInt(contentResolver2, str2, i);
                }
                VirtualKeySettings.this.notifyVirtualNotificationDependent(isChecked);
                return true;
            }
        }
    };
    private SwitchPreference mSwitchPreference;
    private RadioListPreferenceManager mVirtualKeyPrefManager;
    private ArrayList<RadioListPreference> mVirtualKeyTypePreferenceList;
    private CustomSwitchPreference mVirtualNotificationPreference;

    public void onRadioButtonClicked(RadioButtonPreference rbp) {
    }

    public boolean useNormalDividerOnly() {
        return true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        addPreferencesFromResource(2131230926);
        setActivityTitle();
        initVirtualKeyStatus();
        initVirtualNotificationStatus();
        initVirtualKeyTypePreferences();
        if (System.getInt(getContentResolver(), "hw_membrane_touch_enabled", 0) == 1 && System.getInt(getContentResolver(), "hw_membrane_touch_navbar_enabled", 0) == 1) {
            buildAndShowNotificationDialog(0);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setActivityTitle() {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            getActivity().setTitle(2131627539);
        } else {
            getActivity().setTitle(2131628846);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mHwCustVirtualKeySettings == null || !this.mHwCustVirtualKeySettings.isShowNavigationBarFootView()) {
            getActivity().getLayoutInflater().inflate(2130968947, null).setClickable(false);
        } else {
            this.mHwCustVirtualKeySettings.initVirtualKeyPositionPreferences(this.mPreferenceChangedListener);
        }
    }

    private void initVirtualKeyStatus() {
        boolean z = true;
        this.mHwCustVirtualKeySettings = (HwCustVirtualKeySettings) HwCustUtils.createObj(HwCustVirtualKeySettings.class, new Object[]{this});
        if (Utils.isChinaArea() || (this.mHwCustVirtualKeySettings != null && this.mHwCustVirtualKeySettings.isShowNavigationBarSwitch())) {
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                boolean z2;
                removePreference("virtual_key");
                this.mSwitchPreference = (SwitchPreference) findPreference("enable_virtual_key");
                int defaultValue = NaviUtils.getEnableNaviDefaultValue();
                SwitchPreference switchPreference = this.mSwitchPreference;
                if (System.getInt(getActivity().getContentResolver(), "enable_navbar", defaultValue) > 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                switchPreference.setChecked(z2);
            } else {
                removePreference("enable_virtual_key");
                this.mSwitchPreference = (SwitchPreference) findPreference("virtual_key");
                SwitchPreference switchPreference2 = this.mSwitchPreference;
                if (System.getInt(getActivity().getContentResolver(), "hide_virtual_key", 0) <= 0) {
                    z = false;
                }
                switchPreference2.setChecked(z);
            }
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
            return;
        }
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mSwitchPreference = (SwitchPreference) findPreference("enable_virtual_key");
            switchPreference2 = this.mSwitchPreference;
            if (System.getInt(getActivity().getContentResolver(), "enable_navbar", 1) <= 0) {
                z = false;
            }
            switchPreference2.setChecked(z);
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        } else {
            removePreference("enable_virtual_key");
            this.mSwitchPreference = null;
        }
        removePreference("virtual_key");
    }

    private void initVirtualNotificationStatus() {
        if (PressureUtil.isSupportPressureHabit(getActivity())) {
            boolean z;
            this.mVirtualNotificationPreference = (CustomSwitchPreference) findPreference("virtual_notification_key_toggle");
            CustomSwitchPreference customSwitchPreference = this.mVirtualNotificationPreference;
            if (System.getInt(getContentResolver(), "virtual_notification_key_type", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
            this.mVirtualNotificationPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
            if (Utils.isChinaArea() && this.mSwitchPreference != null) {
                this.mVirtualNotificationPreference.setDependency("virtual_key");
                return;
            }
            return;
        }
        removePreference("virtual_notification_key_toggle");
        this.mVirtualNotificationPreference = null;
    }

    private void initVirtualKeyTypePreferences() {
        this.mVirtualKeyTypePreferenceList = new ArrayList();
        int selectedVirtualKeyType = System.getInt(getActivity().getContentResolver(), "virtual_key_type", 0);
        for (int index = 0; index < VIRTUAL_KEY_TYPE_LIST.length; index++) {
            Preference pref = findPreference(VIRTUAL_KEY_TYPE_LIST[index]);
            if (pref != null && (pref instanceof RadioListPreference)) {
                boolean z;
                RadioListPreference radioPreference = (RadioListPreference) pref;
                radioPreference.setLayoutResource(2130968945);
                if (selectedVirtualKeyType == index) {
                    z = true;
                } else {
                    z = false;
                }
                radioPreference.setChecked(z);
                this.mVirtualKeyTypePreferenceList.add(radioPreference);
            }
        }
        if (this.mHwCustVirtualKeySettings != null) {
            this.mHwCustVirtualKeySettings.addVirtualKeyPreference(getActivity(), selectedVirtualKeyType, this.mVirtualKeyTypePreferenceList, getPreferenceScreen());
        }
        this.mVirtualKeyPrefManager = new RadioListPreferenceManager(this.mVirtualKeyTypePreferenceList);
        this.mVirtualKeyPrefManager.setOnOptionSelectedListener(this);
    }

    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(this.mCurrentVirtualObserver);
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(System.getUriFor("virtual_key_position"), true, this.mCurrentVirtualObserver);
    }

    private void buildAndShowNotificationDialog(int dialogId) {
        if (dialogId == 0 && getActivity() != null) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(2131627766);
            builder.setMessage(2131627767);
            builder.setNegativeButton(2131624572, this);
            this.mNotificationDialog = builder.show();
            this.mNotificationDialog.setOnDismissListener(this);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (dialog == this.mNotificationDialog) {
            finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == this.mNotificationDialog) {
            this.mNotificationDialog.dismiss();
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void notifyVirtualNotificationDependent(boolean isChecked) {
        if (this.mVirtualNotificationPreference != null && !isChecked) {
            this.mVirtualNotificationPreference.setChecked(false);
            System.putInt(getContentResolver(), "virtual_notification_key_type", 0);
        }
    }

    public void onOptionSelected(RadioListPreference preference, int index) {
        if (this.mHwCustVirtualKeySettings == null || !this.mHwCustVirtualKeySettings.handleCustItemUseStatClick(index)) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, VIRTUAL_KEY_TYPE_LIST[index]);
        }
        System.putInt(getActivity().getContentResolver(), "virtual_key_type", index);
    }

    public boolean isSelectEnabled() {
        return true;
    }
}
