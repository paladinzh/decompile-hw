package com.android.settings;

import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import java.util.ArrayList;

public class HwCustDisplaySettingsImpl extends HwCustDisplaySettings implements OnPreferenceChangeListener {
    private static final String BOOT_UP_FLAG = "not_show_boot_dialog";
    private static final String CARRIER_NAME_SWITCH = "carrier_name_switch";
    protected static final String CATEGORY_SCREEN = "category_screen";
    private static final String COLOR_TEMPERATURE = "color_temperature";
    private static final int EVENT_SERVICE_STATE_CHANGED = 300;
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_ACCELEROMETER_SMART = "accelerometer_smart";
    private static final String KEY_BOOT_DATA_COST_TIP = "enable_boot_tip";
    private static final String KEY_CARRIER_NAME = "key_carrier_name";
    private static final String KEY_HIDE_NOTIFICATION_BACKGROUND = "hide_notification_background";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_SMART_BACKLIGHT = "smart_backlight";
    protected static final String KEY_WALLPAPER = "wallpaper";
    private static final String SMART_BACKLIGHT = "smart_backlight_enable";
    private static final boolean SUPPORT_EXCHANGE_POLICY = SystemProperties.getBoolean("hw_exchange_security_policy", false);
    private static final String TAG = "HwCustDisplaySettingsImpl";
    private static boolean mOperatorNameSwitch = SystemProperties.getBoolean("ro.config.hw_carrier_name", false);
    private CustomSwitchPreference mCarrierName;
    private Context mContext;
    private ColorTemperatureCallback mCtCallback = null;
    private CustomSwitchPreference mCustomswitch;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwCustDisplaySettingsImpl.EVENT_SERVICE_STATE_CHANGED /*300*/:
                    HwCustDisplaySettingsImpl.this.mDisplaySettings.updateState();
                    return;
                default:
                    return;
            }
        }
    };
    private CustomSwitchPreference mHideNotificationBackground;
    private TelephonyManager mPhone;
    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private SwitchPreference mSmartBackLightPreference;
    private String[] screen_timeout_array_include_ten_second = new String[]{"screen_timeout_10_seconds", "screen_timeout_15_seconds", "screen_timeout_30_seconds", "screen_timeout_1_minute", "screen_timeout_2_minutes", "screen_timeout_5_minutes", "screen_timeout_10_minutes", "screen_timeout_30_minutes"};

    public interface ColorTemperatureCallback {
        void onPause();

        void onResume();
    }

    public HwCustDisplaySettingsImpl(DisplaySettings displaySettings) {
        super(displaySettings);
    }

    public void updateCustPreference(Context context) {
        PreferenceScreen root = this.mDisplaySettings.getPreferenceScreen();
        this.mContext = context;
        if (mOperatorNameSwitch) {
            this.mPhone = (TelephonyManager) this.mContext.getSystemService("phone");
            this.mPhoneStateReceiver = new PhoneStateIntentReceiver(this.mContext, this.mHandler);
            this.mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);
            this.mCarrierName = new CustomSwitchPreference(this.mContext);
            this.mCarrierName.setKey(KEY_CARRIER_NAME);
            this.mCarrierName.setPersistent(false);
            this.mCarrierName.setTitle(2131629112);
            this.mCarrierName.setOnPreferenceChangeListener(this);
            root.addPreference(this.mCarrierName);
        }
        if (SystemProperties.getBoolean("ro.config.hw_claro_boot_tip", false)) {
            this.mCustomswitch = new CustomSwitchPreference(this.mContext);
            this.mCustomswitch.setKey(KEY_BOOT_DATA_COST_TIP);
            this.mCustomswitch.setTitle(2131629116);
            this.mCustomswitch.setSummary(2131629117);
            this.mCustomswitch.setPersistent(false);
            this.mCustomswitch.setOnPreferenceChangeListener(this);
            root.addPreference(this.mCustomswitch);
        }
        if (SystemProperties.getBoolean("ro.config.rm_noti_pulse", false)) {
            Preference csp = root.findPreference("notification_pulse");
            if (csp != null) {
                root.removePreference(csp);
            }
        }
        if (SystemProperties.getBoolean("ro.config.ChargingAlbum", false)) {
            Preference screenSaver = root.findPreference("screensaver");
            if (screenSaver != null) {
                root.removePreference(screenSaver);
            }
        }
        this.mDisplaySettings.getPreferenceManager().inflateFromResource(context, 2131230782, root);
        root = this.mDisplaySettings.getPreferenceScreen();
        DialogPreference colorTemperaturetSettings = (DialogPreference) root.findPreference(COLOR_TEMPERATURE);
        Preference accelerometerSmart = root.findPreference(KEY_ACCELEROMETER_SMART);
        Preference accelerometer = root.findPreference(KEY_ACCELEROMETER);
        this.mHideNotificationBackground = (CustomSwitchPreference) root.findPreference(KEY_HIDE_NOTIFICATION_BACKGROUND);
        if (this.mHideNotificationBackground != null) {
            if (SystemProperties.getBoolean("ro.config.HideNotification", false)) {
                this.mHideNotificationBackground.setChecked(System.getInt(this.mContext.getContentResolver(), KEY_HIDE_NOTIFICATION_BACKGROUND, 0) == 0);
                this.mHideNotificationBackground.setOnPreferenceChangeListener(this);
            } else {
                root.removePreference(this.mHideNotificationBackground);
            }
        }
        PreferenceCategory categoryScreen = (PreferenceCategory) root.findPreference(CATEGORY_SCREEN);
        if (SystemProperties.getBoolean("ro.config.smart_rotation", false)) {
            categoryScreen.removePreference(accelerometer);
        } else {
            root.removePreference(accelerometerSmart);
        }
        if (!SystemProperties.getBoolean("ro.config.colorTemperature_K3", false)) {
            root.removePreference(colorTemperaturetSettings);
        } else if (colorTemperaturetSettings != null) {
            Preference wallPaper = root.findPreference(KEY_WALLPAPER);
            if (wallPaper != null) {
                int preferenceCount = root.getPreferenceCount();
                int insertPosition = wallPaper.getOrder();
                for (int i = 0; i < preferenceCount; i++) {
                    Preference tempPreference = root.getPreference(i);
                    if (tempPreference.getOrder() >= insertPosition) {
                        tempPreference.setOrder(tempPreference.getOrder() + 1);
                    }
                }
                colorTemperaturetSettings.setOrder(insertPosition);
            }
        }
        if (colorTemperaturetSettings instanceof ColorTemperatureCallback) {
            this.mCtCallback = (ColorTemperatureCallback) colorTemperaturetSettings;
        }
        this.mSmartBackLightPreference = (SwitchPreference) root.findPreference(KEY_SMART_BACKLIGHT);
        if (isSupportSmartBackLight()) {
            this.mSmartBackLightPreference.setOnPreferenceChangeListener(this);
        } else {
            root.removePreference(this.mSmartBackLightPreference);
        }
    }

    public void onResume() {
        boolean z = false;
        if (mOperatorNameSwitch && this.mPhoneStateReceiver != null) {
            this.mPhoneStateReceiver.registerIntent();
        }
        if (this.mCtCallback != null) {
            this.mCtCallback.onResume();
        }
        if (isSupportSmartBackLight()) {
            updateSmartBacklight();
        }
        if (this.mCustomswitch != null) {
            CustomSwitchPreference customSwitchPreference = this.mCustomswitch;
            if (Global.getInt(this.mContext.getContentResolver(), BOOT_UP_FLAG, 0) == 0) {
                z = true;
            }
            customSwitchPreference.setChecked(z);
        }
    }

    public void onPause() {
        if (mOperatorNameSwitch && this.mPhoneStateReceiver != null) {
            this.mPhoneStateReceiver.unregisterIntent();
        }
        if (this.mCtCallback != null) {
            this.mCtCallback.onPause();
        }
    }

    public void updateCarrierView() {
        boolean z = true;
        int mSimState = 0;
        if (mOperatorNameSwitch && this.mCarrierName != null && this.mPhoneStateReceiver != null) {
            if (this.mPhone != null) {
                mSimState = this.mPhone.getSimState();
            }
            if (mSimState == 1) {
                this.mCarrierName.setEnabled(false);
                return;
            }
            this.mCarrierName.setEnabled(true);
            CustomSwitchPreference customSwitchPreference = this.mCarrierName;
            if (System.getInt(this.mContext.getContentResolver(), CARRIER_NAME_SWITCH, 1) == 0) {
                z = false;
            }
            customSwitchPreference.setChecked(z);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        Boolean value = (Boolean) newValue;
        ContentResolver contentResolver;
        String str;
        if (KEY_CARRIER_NAME.equals(key)) {
            contentResolver = this.mContext.getContentResolver();
            str = CARRIER_NAME_SWITCH;
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(contentResolver, str, i);
            return true;
        } else if (KEY_SMART_BACKLIGHT.equals(key)) {
            contentResolver = this.mContext.getContentResolver();
            str = SMART_BACKLIGHT;
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(contentResolver, str, i);
            return true;
        } else if (KEY_HIDE_NOTIFICATION_BACKGROUND.equals(key)) {
            contentResolver = this.mContext.getContentResolver();
            str = KEY_HIDE_NOTIFICATION_BACKGROUND;
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(contentResolver, str, i);
            return true;
        } else if (!KEY_BOOT_DATA_COST_TIP.equals(key)) {
            return false;
        } else {
            contentResolver = this.mContext.getContentResolver();
            str = BOOT_UP_FLAG;
            if (!value.booleanValue()) {
                i = 1;
            }
            Global.putInt(contentResolver, str, i);
            return true;
        }
    }

    public int getCustomTimeout(Context context, int value) {
        return System.getInt(context.getContentResolver(), "custom_screen_off_timeout", value);
    }

    private boolean isSupportSmartBackLight() {
        if (SystemProperties.getInt("ro.config.hw_smart_backlight", 0) == 1) {
            return true;
        }
        return false;
    }

    private void updateSmartBacklight() {
        boolean z = false;
        if (this.mSmartBackLightPreference != null && this.mContext != null) {
            SwitchPreference switchPreference = this.mSmartBackLightPreference;
            if (System.getInt(this.mContext.getContentResolver(), SMART_BACKLIGHT, 0) != 0) {
                z = true;
            }
            switchPreference.setChecked(z);
        }
    }

    private boolean isAddTenSecondScreenTimeout() {
        return SystemProperties.getBoolean("ro.config.scr_timeout_10sec", false);
    }

    public void updateScreenTimeoutPreference(ListPreference mScreenTimeoutPreference) {
        if (isAddTenSecondScreenTimeout()) {
            mScreenTimeoutPreference.setEntries(2131362008);
            mScreenTimeoutPreference.setEntryValues(2131362009);
        }
        String removeEntriesValue = SystemProperties.get("ro.config.removeScreenTimeout", null);
        if (removeEntriesValue != null && !"".equals(removeEntriesValue)) {
            CharSequence[] arrayEntries = mScreenTimeoutPreference.getEntries();
            CharSequence[] arrayEntriesValue = mScreenTimeoutPreference.getEntryValues();
            ArrayList<CharSequence> newArrayEntries = new ArrayList();
            ArrayList<CharSequence> newArrayEntriesValue = new ArrayList();
            for (int i = 0; i < arrayEntriesValue.length; i++) {
                if (!removeEntriesValue.contains(arrayEntriesValue[i] + "ms")) {
                    newArrayEntries.add(arrayEntries[i]);
                    newArrayEntriesValue.add(arrayEntriesValue[i]);
                }
            }
            arrayEntries = (CharSequence[]) newArrayEntries.toArray(new CharSequence[newArrayEntries.size()]);
            arrayEntriesValue = (CharSequence[]) newArrayEntriesValue.toArray(new CharSequence[newArrayEntriesValue.size()]);
            if (arrayEntries.length == arrayEntriesValue.length) {
                mScreenTimeoutPreference.setEntries(arrayEntries);
                mScreenTimeoutPreference.setEntryValues(arrayEntriesValue);
            }
        }
    }

    public String[] getScreenTimeOutValues(String[] screen_timeout_values) {
        if (isAddTenSecondScreenTimeout()) {
            return this.screen_timeout_array_include_ten_second;
        }
        return screen_timeout_values;
    }

    public void changeScreenOffTimeoutArrays(ArrayList<CharSequence> revisedEntries, ArrayList<CharSequence> revisedValues) {
        if (SUPPORT_EXCHANGE_POLICY && this.mDisplaySettings != null) {
            DevicePolicyManager dpm = (DevicePolicyManager) this.mDisplaySettings.getSystemService("device_policy");
            long screenlockTimeout = Secure.getLong(this.mDisplaySettings.getContentResolver(), "lock_screen_lock_after_timeout", dpm != null ? dpm.getMaximumTimeToLock(null) : 0);
            int i = 0;
            int len = revisedValues.size();
            while (i < len) {
                if (Long.parseLong(((CharSequence) revisedValues.get(i)).toString()) > screenlockTimeout) {
                    revisedEntries.remove(i);
                    revisedValues.remove(i);
                    len--;
                    i--;
                }
                i++;
            }
        }
    }

    public void setCurrentScreenOffTimeoutValue() {
        if (SUPPORT_EXCHANGE_POLICY && this.mDisplaySettings != null) {
            DevicePolicyManager dpm = (DevicePolicyManager) this.mDisplaySettings.getSystemService("device_policy");
            long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
            ListPreference screenTimeoutPreference = (ListPreference) this.mDisplaySettings.findPreference(KEY_SCREEN_TIMEOUT);
            if (screenTimeoutPreference != null && screenTimeoutPreference.getContext() != null) {
                CharSequence[] values = screenTimeoutPreference.getEntryValues();
                int currentTimeout = System.getInt(this.mDisplaySettings.getContentResolver(), "screen_off_timeout", FALLBACK_SCREEN_TIMEOUT_VALUE);
                long defaultTimeout = 30000;
                boolean containCurrentTimeout = false;
                for (CharSequence charSequence : values) {
                    long timeout = Long.parseLong(charSequence.toString());
                    if (timeout <= Secure.getLong(this.mDisplaySettings.getContentResolver(), "lock_screen_lock_after_timeout", maxTimeout)) {
                        defaultTimeout = timeout;
                        if (timeout == ((long) currentTimeout)) {
                            containCurrentTimeout = true;
                            break;
                        }
                    }
                }
                if (!containCurrentTimeout) {
                    System.putInt(this.mDisplaySettings.getContentResolver(), "screen_off_timeout", (int) defaultTimeout);
                }
            }
        }
    }
}
