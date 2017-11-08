package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.content.PackageMonitor;
import com.android.settings.DialogCreatable;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessibilitySettings extends SettingsPreferenceFragment implements DialogCreatable, OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList();
            PackageManager packageManager = context.getPackageManager();
            AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
            String screenTitle = context.getResources().getString(2131625847);
            List<AccessibilityServiceInfo> services = accessibilityManager.getInstalledAccessibilityServiceList();
            int serviceCount = services.size();
            for (int i = 0; i < serviceCount; i++) {
                AccessibilityServiceInfo service = (AccessibilityServiceInfo) services.get(i);
                if (!(service == null || service.getResolveInfo() == null)) {
                    ServiceInfo serviceInfo = service.getResolveInfo().serviceInfo;
                    if (!"com.huawei.HwMultiScreenShot".equals(serviceInfo.packageName)) {
                        ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                        SearchIndexableRaw indexable = new SearchIndexableRaw(context);
                        indexable.key = componentName.flattenToString();
                        indexable.title = service.getResolveInfo().loadLabel(packageManager).toString();
                        indexable.screenTitle = screenTitle;
                        indexables.add(indexable);
                    }
                }
            }
            return indexables;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = 2131230724;
            indexables.add(indexable);
            return indexables;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (Utils.isChinaArea()) {
                result.add("toggle_inversion_preference");
                result.add("toggle_high_text_contrast_preference");
            }
            if (Utils.isWifiOnly(context) || !Utils.isVoiceCapable(context)) {
                result.add("toggle_power_button_ends_call_preference");
            }
            return result;
        }
    };
    private static final Uri URI_COMPETITOR_CONFIG = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/competitorConfigTable");
    static final Set<ComponentName> sInstalledServices = new HashSet();
    private PreferenceScreen mAutoclickPreferenceScreen;
    private PreferenceScreen mCaptioningPreferenceScreen;
    private HwCustAccessibilitySettings mCust;
    private PreferenceScreen mDisplayDaltonizerPreferenceScreen;
    private PreferenceScreen mDisplayMagnificationPreferenceScreen;
    private DevicePolicyManager mDpm;
    private PreferenceScreen mGlobalGesturePreferenceScreen;
    private final Handler mHandler = new Handler();
    private int mLongPressTimeoutDefault;
    private final Map<String, String> mLongPressTimeoutValuetoTitleMap = new HashMap();
    private Preference mNoServicesMessagePreference;
    private ListPreference mSelectLongPressTimeoutPreference;
    private PreferenceCategory mServicesCategory;
    private final SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(this.mHandler) {
        public void onChange(boolean selfChange, Uri uri) {
            AccessibilitySettings.this.updateServicesPreferences();
        }
    };
    private final PackageMonitor mSettingsPackageMonitor = new PackageMonitor() {
        public void onPackageAdded(String packageName, int uid) {
            sendUpdate();
        }

        public void onPackageAppeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageDisappeared(String packageName, int reason) {
            sendUpdate();
        }

        public void onPackageRemoved(String packageName, int uid) {
            sendUpdate();
        }

        private void sendUpdate() {
            AccessibilitySettings.this.mHandler.postDelayed(AccessibilitySettings.this.mUpdateRunnable, 1000);
        }
    };
    private PreferenceCategory mSystemsCategory;
    private TwoStatePreference mToggleHighTextContrastPreference;
    private SwitchPreference mToggleInversionPreference;
    private TwoStatePreference mToggleLargePointerIconPreference;
    private SwitchPreference mToggleMasterMonoPreference;
    private TwoStatePreference mTogglePowerButtonEndsCallPreference;
    private TwoStatePreference mToggleSpeakPasswordPreference;
    private final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            if (AccessibilitySettings.this.getActivity() != null) {
                AccessibilitySettings.this.updateServicesPreferences();
            }
        }
    };
    final String[] projection = new String[]{"packageName"};

    private java.util.ArrayList<java.lang.String> getServiceBlackList() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003e in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r8 = new java.util.ArrayList;
        r8.<init>();
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r1 = URI_COMPETITOR_CONFIG;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r2 = r10.projection;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r3 = 0;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r4 = 0;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r5 = 0;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        if (r6 != 0) goto L_0x0026;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
    L_0x0017:
        r0 = "AccessibilitySettings";	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r1 = "Cursor is null of competition_config_table";	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        android.util.Log.e(r0, r1);	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        if (r6 == 0) goto L_0x0025;
    L_0x0022:
        r6.close();
    L_0x0025:
        return r8;
    L_0x0026:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        if (r0 == 0) goto L_0x003f;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
    L_0x002c:
        r0 = 0;	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r9 = r6.getString(r0);	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        r8.add(r9);	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        goto L_0x0026;
    L_0x0035:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x0035, all -> 0x0045 }
        if (r6 == 0) goto L_0x003e;
    L_0x003b:
        r6.close();
    L_0x003e:
        return r8;
    L_0x003f:
        if (r6 == 0) goto L_0x003e;
    L_0x0041:
        r6.close();
        goto L_0x003e;
    L_0x0045:
        r0 = move-exception;
        if (r6 == 0) goto L_0x004b;
    L_0x0048:
        r6.close();
    L_0x004b:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.accessibility.AccessibilitySettings.getServiceBlackList():java.util.ArrayList<java.lang.String>");
    }

    protected int getMetricsCategory() {
        return 2;
    }

    protected int getHelpResource() {
        return 2131626532;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230724);
        initializeAllPreferences();
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        this.mCust = (HwCustAccessibilitySettings) HwCustUtils.createObj(HwCustAccessibilitySettings.class, new Object[]{this});
        this.mCust.addCustPreferences();
        setHasOptionsMenu(true);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        updateAllPreferences();
        this.mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        this.mSettingsContentObserver.register(getContentResolver());
    }

    public void onPause() {
        this.mSettingsPackageMonitor.unregister();
        this.mSettingsContentObserver.unregister(getContentResolver());
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        if (this.mSelectLongPressTimeoutPreference == preference) {
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mSelectLongPressTimeoutPreference, ItemUseStat.KEY_LISTPREFERENCE_TOUCH_HOLD_DELAY, (String) newValue);
            handleLongPressTimeoutPreferenceChange((String) newValue);
            return true;
        } else if (this.mToggleInversionPreference == preference) {
            handleToggleInversionPreferenceChange(((Boolean) newValue).booleanValue());
            return true;
        } else if (this.mTogglePowerButtonEndsCallPreference == preference) {
            handleTogglePowerButtonEndsCallPreferenceClick(newValue);
            return true;
        } else if (this.mToggleSpeakPasswordPreference == preference) {
            handleToggleSpeakPasswordPreferenceClick(newValue);
            return true;
        } else if (this.mToggleHighTextContrastPreference == preference) {
            handleToggleTextContrastPreferenceClick(newValue);
            return true;
        } else if (this.mToggleLargePointerIconPreference == preference) {
            handleToggleLargePointerIconPreferenceClick(newValue);
            return true;
        } else if (this.mToggleMasterMonoPreference != preference) {
            return false;
        } else {
            handleToggleMasterMonoPreferenceClick(((Boolean) newValue).booleanValue());
            return true;
        }
    }

    private void handleLongPressTimeoutPreferenceChange(String stringValue) {
        Secure.putInt(getContentResolver(), "long_press_timeout", Integer.parseInt(stringValue));
        this.mSelectLongPressTimeoutPreference.setSummary((CharSequence) this.mLongPressTimeoutValuetoTitleMap.get(stringValue));
    }

    private void handleToggleInversionPreferenceChange(boolean checked) {
        Secure.putInt(getContentResolver(), "accessibility_display_inversion_enabled", checked ? 1 : 0);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if (this.mGlobalGesturePreferenceScreen == preference) {
            handleToggleEnableAccessibilityGesturePreferenceClick();
            return true;
        } else if (this.mDisplayMagnificationPreferenceScreen == preference) {
            handleDisplayMagnificationPreferenceScreenClick();
            return true;
        } else if (this.mCust == null || !this.mCust.onPreferenceTreeClick(preference)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            return true;
        }
    }

    private void handleToggleTextContrastPreferenceClick(Object newValue) {
        Secure.putInt(getContentResolver(), "high_text_contrast_enabled", ((Boolean) newValue).booleanValue() ? 1 : 0);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick(Object newValue) {
        int i;
        ContentResolver contentResolver = getContentResolver();
        String str = "incall_power_button_behavior";
        if (((Boolean) newValue).booleanValue()) {
            i = 2;
        } else {
            i = 1;
        }
        Secure.putInt(contentResolver, str, i);
    }

    private void handleToggleSpeakPasswordPreferenceClick(Object newValue) {
        Secure.putInt(getContentResolver(), "speak_password", ((Boolean) newValue).booleanValue() ? 1 : 0);
    }

    private void handleToggleLargePointerIconPreferenceClick(Object newValue) {
        Secure.putInt(getContentResolver(), "accessibility_large_pointer_icon", ((Boolean) newValue).booleanValue() ? 1 : 0);
    }

    private void handleToggleMasterMonoPreferenceClick(boolean checked) {
        System.putIntForUser(getContentResolver(), "master_mono", checked ? 1 : 0, -2);
    }

    private void handleToggleEnableAccessibilityGesturePreferenceClick() {
        boolean z = true;
        Bundle extras = this.mGlobalGesturePreferenceScreen.getExtras();
        extras.putString("title", getString(2131625856));
        extras.putString("summary", getString(2131625859));
        String str = "checked";
        if (Global.getInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", 0) != 1) {
            z = false;
        }
        extras.putBoolean(str, z);
        super.onPreferenceTreeClick(this.mGlobalGesturePreferenceScreen);
    }

    private void handleDisplayMagnificationPreferenceScreenClick() {
        boolean z = true;
        Bundle extras = this.mDisplayMagnificationPreferenceScreen.getExtras();
        extras.putString("title", getString(2131625853));
        extras.putCharSequence("summary", getActivity().getResources().getText(2131625855));
        String str = "checked";
        if (Secure.getInt(getContentResolver(), "accessibility_display_magnification_enabled", 0) != 1) {
            z = false;
        }
        extras.putBoolean(str, z);
        super.onPreferenceTreeClick(this.mDisplayMagnificationPreferenceScreen);
    }

    private void initializeAllPreferences() {
        this.mServicesCategory = (PreferenceCategory) findPreference("services_category");
        this.mSystemsCategory = (PreferenceCategory) findPreference("system_category");
        this.mToggleHighTextContrastPreference = (TwoStatePreference) findPreference("toggle_high_text_contrast_preference");
        if (Utils.isChinaArea()) {
            this.mSystemsCategory.removePreference(this.mToggleHighTextContrastPreference);
        } else {
            this.mToggleHighTextContrastPreference.setOnPreferenceChangeListener(this);
        }
        this.mToggleInversionPreference = (SwitchPreference) findPreference("toggle_inversion_preference");
        if (Utils.isChinaArea()) {
            ((PreferenceCategory) findPreference("display_category")).removePreference(this.mToggleInversionPreference);
        } else {
            this.mToggleInversionPreference.setOnPreferenceChangeListener(this);
        }
        this.mTogglePowerButtonEndsCallPreference = (TwoStatePreference) findPreference("toggle_power_button_ends_call_preference");
        this.mTogglePowerButtonEndsCallPreference.setOnPreferenceChangeListener(this);
        if (!(KeyCharacterMap.deviceHasKey(26) && Utils.isVoiceCapable(getActivity()) && !Utils.isWifiOnly(getActivity()))) {
            this.mSystemsCategory.removePreference(this.mTogglePowerButtonEndsCallPreference);
        }
        this.mToggleSpeakPasswordPreference = (TwoStatePreference) findPreference("toggle_speak_password_preference");
        this.mToggleSpeakPasswordPreference.setOnPreferenceChangeListener(this);
        this.mToggleLargePointerIconPreference = (TwoStatePreference) findPreference("toggle_large_pointer_icon");
        this.mToggleLargePointerIconPreference.setOnPreferenceChangeListener(this);
        this.mToggleMasterMonoPreference = (SwitchPreference) findPreference("toggle_master_mono");
        this.mToggleMasterMonoPreference.setOnPreferenceChangeListener(this);
        this.mSelectLongPressTimeoutPreference = (ListPreference) findPreference("select_long_press_timeout_preference");
        this.mSelectLongPressTimeoutPreference.setOnPreferenceChangeListener(this);
        if (this.mLongPressTimeoutValuetoTitleMap.size() == 0) {
            String[] timeoutValues = getResources().getStringArray(2131361890);
            this.mLongPressTimeoutDefault = Integer.parseInt(timeoutValues[0]);
            String[] timeoutTitles = getResources().getStringArray(2131361889);
            int timeoutValueCount = timeoutValues.length;
            for (int i = 0; i < timeoutValueCount; i++) {
                this.mLongPressTimeoutValuetoTitleMap.put(timeoutValues[i], timeoutTitles[i]);
            }
        }
        this.mCaptioningPreferenceScreen = (PreferenceScreen) findPreference("captioning_preference_screen");
        this.mDisplayMagnificationPreferenceScreen = (PreferenceScreen) findPreference("screen_magnification_preference_screen");
        this.mAutoclickPreferenceScreen = (PreferenceScreen) findPreference("autoclick_preference_screen");
        this.mDisplayDaltonizerPreferenceScreen = (PreferenceScreen) findPreference("daltonizer_preference_screen");
        this.mGlobalGesturePreferenceScreen = (PreferenceScreen) findPreference("enable_global_gesture_preference_screen");
        int longPressOnPowerBehavior = getActivity().getResources().getInteger(17694799);
        if (!KeyCharacterMap.deviceHasKey(26) || longPressOnPowerBehavior != 1) {
            this.mSystemsCategory.removePreference(this.mGlobalGesturePreferenceScreen);
        }
    }

    private void updateAllPreferences() {
        updateServicesPreferences();
        updateSystemPreferences();
    }

    private void updateServicesPreferences() {
        if (this.mCust != null) {
            this.mCust.isTalkBackPositiveButtonClicked = false;
        }
        this.mServicesCategory.removeAll();
        List<AccessibilityServiceInfo> installedServices = AccessibilityManager.getInstance(getActivity()).getInstalledAccessibilityServiceList();
        Set<ComponentName> enabledServices = AccessibilityUtils.getEnabledServicesFromSettings(getActivity());
        List<String> permittedServices = this.mDpm.getPermittedAccessibilityServices(UserHandle.myUserId());
        boolean accessibilityEnabled = Secure.getInt(getContentResolver(), "accessibility_enabled", 0) == 1;
        ArrayList<String> blakList = getServiceBlackList();
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            AccessibilityServiceInfo info = (AccessibilityServiceInfo) installedServices.get(i);
            Preference restrictedPreference = new RestrictedPreference(getActivity());
            if (!(info == null || info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null)) {
                String title = info.getResolveInfo().loadLabel(getPackageManager()).toString();
                ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
                if (!("com.huawei.HwMultiScreenShot".equals(serviceInfo.packageName) || "android".equals(serviceInfo.packageName))) {
                    boolean serviceEnabled;
                    String serviceEnabledString;
                    ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                    restrictedPreference.setKey(componentName.flattenToString());
                    restrictedPreference.setTitle((CharSequence) title);
                    if (accessibilityEnabled) {
                        serviceEnabled = enabledServices.contains(componentName);
                    } else {
                        serviceEnabled = false;
                    }
                    if (serviceEnabled) {
                        serviceEnabledString = getString(2131625876);
                    } else {
                        serviceEnabledString = getString(2131625877);
                    }
                    String packageName = serviceInfo.packageName;
                    if (serviceEnabled || !blakList.contains(packageName)) {
                        if ((permittedServices != null ? permittedServices.contains(packageName) : true) || serviceEnabled) {
                            restrictedPreference.setEnabled(true);
                        } else {
                            EnforcedAdmin admin = RestrictedLockUtils.checkIfAccessibilityServiceDisallowed(getActivity(), serviceInfo.packageName, UserHandle.myUserId());
                            if (admin != null) {
                                restrictedPreference.setDisabledByAdmin(admin);
                            } else {
                                restrictedPreference.setEnabled(false);
                            }
                        }
                        restrictedPreference.setSummary((CharSequence) serviceEnabledString);
                        restrictedPreference.setOrder(i);
                        restrictedPreference.setFragment(ToggleAccessibilityServicePreferenceFragment.class.getName());
                        restrictedPreference.setPersistent(true);
                        restrictedPreference.setLayoutResource(2130968977);
                        restrictedPreference.setWidgetLayoutResource(2130968998);
                        Bundle extras = restrictedPreference.getExtras();
                        extras.putString("preference_key", restrictedPreference.getKey());
                        extras.putBoolean("checked", serviceEnabled);
                        extras.putString("title", title);
                        String description = info.loadDescription(getPackageManager());
                        if (TextUtils.isEmpty(description)) {
                            description = getString(2131625922);
                        }
                        extras.putString("summary", description);
                        String settingsClassName = info.getSettingsActivityName();
                        if (!TextUtils.isEmpty(settingsClassName)) {
                            extras.putString("settings_title", getString(2131625875));
                            extras.putInt("menu_icon", 2130838288);
                            extras.putString("settings_component_name", new ComponentName(info.getResolveInfo().serviceInfo.packageName, settingsClassName).flattenToString());
                        }
                        extras.putParcelable("component_name", componentName);
                        this.mServicesCategory.addPreference(restrictedPreference);
                        if (this.mCust != null) {
                            this.mCust.custamizeServicePreferences(serviceInfo, this.mServicesCategory, restrictedPreference);
                        }
                        if (this.mCust != null && HwCustAccessibilitySettings.TalkBack_TITLE.equals(title)) {
                            this.mCust.mTalkBackPreferenceScreen = restrictedPreference;
                        }
                    } else {
                        Log.d("AccessibilitySettings", "Current application is: " + packageName + ", it is in the blacklist");
                    }
                }
            }
        }
        if (this.mServicesCategory.getPreferenceCount() == 0) {
            if (this.mNoServicesMessagePreference == null) {
                this.mNoServicesMessagePreference = new Preference(getPrefContext());
                this.mNoServicesMessagePreference.setPersistent(false);
                this.mNoServicesMessagePreference.setLayoutResource(2130968602);
                this.mNoServicesMessagePreference.setSelectable(false);
                this.mNoServicesMessagePreference.setSummary(getString(2131625921));
            }
            this.mNoServicesMessagePreference.setEnabled(false);
            this.mServicesCategory.addPreference(this.mNoServicesMessagePreference);
            this.mGlobalGesturePreferenceScreen.setEnabled(false);
            this.mGlobalGesturePreferenceScreen.setSummary(2131625877);
            Global.putInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", 0);
            return;
        }
        this.mGlobalGesturePreferenceScreen.setEnabled(true);
    }

    private void updateSystemPreferences() {
        TwoStatePreference twoStatePreference;
        boolean z;
        boolean globalGestureEnabled;
        if (this.mToggleHighTextContrastPreference != null) {
            twoStatePreference = this.mToggleHighTextContrastPreference;
            if (Secure.getInt(getContentResolver(), "high_text_contrast_enabled", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            twoStatePreference.setChecked(z);
        }
        if (this.mToggleInversionPreference != null) {
            SwitchPreference switchPreference = this.mToggleInversionPreference;
            if (Secure.getInt(getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            switchPreference.setChecked(z);
        }
        if (KeyCharacterMap.deviceHasKey(26) && Utils.isVoiceCapable(getActivity())) {
            this.mTogglePowerButtonEndsCallPreference.setChecked(Secure.getInt(getContentResolver(), "incall_power_button_behavior", 1) == 2);
        }
        this.mToggleSpeakPasswordPreference.setChecked(Secure.getInt(getContentResolver(), "speak_password", 0) != 0);
        twoStatePreference = this.mToggleLargePointerIconPreference;
        if (Secure.getInt(getContentResolver(), "accessibility_large_pointer_icon", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        twoStatePreference.setChecked(z);
        updateMasterMono();
        String value = String.valueOf(Secure.getInt(getContentResolver(), "long_press_timeout", this.mLongPressTimeoutDefault));
        this.mSelectLongPressTimeoutPreference.setValue(value);
        this.mSelectLongPressTimeoutPreference.setSummary((CharSequence) this.mLongPressTimeoutValuetoTitleMap.get(value));
        updateFeatureSummary("accessibility_captioning_enabled", this.mCaptioningPreferenceScreen);
        updateFeatureSummary("accessibility_display_magnification_enabled", this.mDisplayMagnificationPreferenceScreen);
        updateFeatureSummary("accessibility_display_daltonizer_enabled", this.mDisplayDaltonizerPreferenceScreen);
        updateAutoclickSummary(this.mAutoclickPreferenceScreen);
        if (Global.getInt(getContentResolver(), "enable_accessibility_global_gesture_enabled", 0) == 1) {
            globalGestureEnabled = true;
        } else {
            globalGestureEnabled = false;
        }
        if (globalGestureEnabled) {
            this.mGlobalGesturePreferenceScreen.setSummary(2131625876);
        } else {
            this.mGlobalGesturePreferenceScreen.setSummary(2131625877);
        }
        if (this.mCust != null) {
            this.mCust.updateCustPreference();
        }
    }

    private void updateFeatureSummary(String prefKey, Preference pref) {
        int i;
        boolean enabled = true;
        if (Secure.getInt(getContentResolver(), prefKey, 0) != 1) {
            enabled = false;
        }
        if (enabled) {
            i = 2131627698;
        } else {
            i = 2131627699;
        }
        pref.setSummary(i);
    }

    private void updateAutoclickSummary(Preference pref) {
        boolean enabled = true;
        if (Secure.getInt(getContentResolver(), "accessibility_autoclick_enabled", 0) != 1) {
            enabled = false;
        }
        if (enabled) {
            pref.setSummary(ToggleAutoclickPreferenceFragment.getAutoclickPreferenceSummary(getResources(), Secure.getInt(getContentResolver(), "accessibility_autoclick_delay", 600)));
            return;
        }
        pref.setSummary(2131625877);
    }

    private void updateMasterMono() {
        this.mToggleMasterMonoPreference.setChecked(System.getIntForUser(getContentResolver(), "master_mono", 0, -2) == 1);
    }

    public void onDetach() {
        if (this.mHandler.hasCallbacks(this.mUpdateRunnable)) {
            this.mHandler.removeCallbacks(this.mUpdateRunnable);
        }
        super.onDetach();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (Utils.isOwnerUser()) {
            menu.add(0, 1, 0, 2131627374).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_RESTORE))).setShowAsAction(1);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                confirmRestoreSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmRestoreSettingsDialog() {
        new Builder(getActivity()).setMessage(2131628155).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AccessibilitySettings.this.factoryResetSettings();
                ItemUseStat.getInstance().handleClick(AccessibilitySettings.this.getActivity(), 2, "restore_accessibility_settings");
            }
        }).setNegativeButton(17039360, null).create().show();
    }

    private void factoryResetSettings() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                Utils.factoryReset(AccessibilitySettings.this.getActivity(), "1");
                if (AccessibilitySettings.this.mCust != null) {
                    AccessibilitySettings.this.mCust.factoryReset();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                AccessibilitySettings.this.updateAllPreferences();
            }
        }.execute(new Void[0]);
    }
}
