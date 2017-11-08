package com.android.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.android.util.NoExtAPIException;

public class HwCustAdvancedSettingsImpl extends HwCustAdvancedSettings {
    private static final String KEY_CHARGINGALBUM = "ChargingAlbum";
    private static final String KEY_DIVICES = "category_device";
    private static final String KEY_PEDOMETER = "StepCalculator";
    private final boolean HAS_CHARGINGALBUM = SystemProperties.getBoolean("ro.config.ChargingAlbum", false);
    private final boolean HAS_PEDOMETER = SystemProperties.getBoolean("ro.config.StepCalculator", false);
    private final boolean HAS_ROG = SystemProperties.getBoolean("ro.config.ROG", false);
    private RogButtonEnabler mRogButtonEnabler;

    public HwCustAdvancedSettingsImpl(AdvancedSettings advancedSettings) {
        super(advancedSettings);
    }

    public void loadCustHeader() {
        PreferenceCategory divicesPreference = (PreferenceCategory) this.mAdvancedSettings.findPreference(KEY_DIVICES);
        PackageManager packageManager = this.mAdvancedSettings.getPackageManager();
        if (this.HAS_CHARGINGALBUM) {
            Intent chargeAlbumIntent = getIntent(packageManager, "com.android.dreams.album", "com.android.dreams.album.PhotoChargeSettings");
            if (chargeAlbumIntent != null) {
                Preference chargeAlbumPreference = this.mAdvancedSettings.getPreferenceManager().createPreferenceScreen(this.mAdvancedSettings.getActivity());
                chargeAlbumPreference.setLayoutResource(2130968977);
                chargeAlbumPreference.setWidgetLayoutResource(2130968998);
                chargeAlbumPreference.setTitle(2131629105);
                chargeAlbumPreference.setIntent(chargeAlbumIntent);
                chargeAlbumPreference.setKey(KEY_CHARGINGALBUM);
                divicesPreference.addPreference(chargeAlbumPreference);
            }
        }
        if (this.HAS_PEDOMETER) {
            Intent stepCalculatorIntent = getIntent(packageManager, "com.huawei.health", "com.huawei.health.ui.SplashActivity");
            if (stepCalculatorIntent != null) {
                Preference calculatorPreference = this.mAdvancedSettings.getPreferenceManager().createPreferenceScreen(this.mAdvancedSettings.getActivity());
                calculatorPreference.setLayoutResource(2130968977);
                calculatorPreference.setWidgetLayoutResource(2130968998);
                calculatorPreference.setTitle(2131629224);
                calculatorPreference.setIntent(stepCalculatorIntent);
                calculatorPreference.setKey(KEY_PEDOMETER);
                divicesPreference.addPreference(calculatorPreference);
            }
        }
        if (this.HAS_ROG) {
            SwitchPreference rogPreference = new SwitchPreference(this.mAdvancedSettings.getActivity(), null);
            rogPreference.setTitle(2131629106);
            rogPreference.setKey("display_low_power_switch");
            if (LockPatternUtils.isDeviceEncryptionEnabled()) {
                rogPreference.setSummary(2131629309);
                rogPreference.setEnabled(false);
            } else {
                this.mRogButtonEnabler = new RogButtonEnabler(this.mAdvancedSettings.getActivity(), rogPreference);
            }
            divicesPreference.addPreference(rogPreference);
        }
    }

    public void onResume() {
        if (this.mRogButtonEnabler != null) {
            try {
                this.mRogButtonEnabler.onResume();
            } catch (NoExtAPIException e) {
                this.mAdvancedSettings.removePreference(KEY_DIVICES, "display_low_power_switch");
                this.mRogButtonEnabler = null;
            }
        }
    }

    public void onPause() {
        if (this.mRogButtonEnabler != null) {
            this.mRogButtonEnabler.onPause();
        }
    }

    private Intent getIntent(PackageManager packageManager, String packageName, String activity) {
        Intent intent = new Intent().setClassName(packageName, activity);
        if (Utils.hasIntentActivity(packageManager, intent)) {
            return intent;
        }
        return null;
    }
}
