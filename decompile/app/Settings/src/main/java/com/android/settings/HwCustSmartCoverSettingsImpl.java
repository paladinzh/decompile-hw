package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.smartcover.SmartCoverKindPreference;
import com.android.settings.smartcover.SmartCoverKindPreference.onListenerSwitchStatus;
import com.android.settings.smartcover.Utils;

public class HwCustSmartCoverSettingsImpl extends HwCustSmartCoverSettings implements OnPreferenceChangeListener {
    private static final int COVER_WEATHER_DISPLAY_DEFAULT = 0;
    private static final int COVER_WEATHER_DISPLAY_OFF = -1;
    private static final int COVER_WEATHER_DISPLAY_ON = 9;
    public static final String KEY_COVER_ENALBED = "cover_enabled";
    private static final String KEY_COVER_WEATHER_DISPLAY_MODE = "cover_weather_display_mode";
    private static final String KEY_PEDOMETER_SWITCH = "pedometer_switch";
    public static final String KEY_SMART_BACKGROUND_SETTINGS = "smart_cover_background_settings";
    public static final String KEY_SMART_COVER_ANIMATION_MODE_SETTINGS = "smart_cover_animation_mode_settings";
    private static final String KEY_SMART_COVER_STANDBY_SETTINGS = "smart_cover_standby_settings";
    private static final String KEY_SMART_COVER_WEATHER_SWITCH = "smart_cover_weather_switch";
    private static final int SMART_COVER_MODE_DEFAULT = 1;
    private static final int SMART_COVER_MODE_OFF = 0;
    private static final int SMART_COVER_MODE_ON = 1;
    private static final String TAG = "HwCustSmartCoverSettingsImpl";
    private Preference mAnimationModePreference = null;
    private Preference mBackgroundPrference = null;
    private Context mContext = null;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            HwCustSmartCoverSettingsImpl.this.updateSwitchStatus();
        }
    };
    private SwitchPreference mPedometerSwitchPreference;
    private SmartCoverKindPreference mSmartCoverKindPreference;
    private Preference mStandByPrference;
    private SwitchPreference mSwitchWeatherPreference;

    public HwCustSmartCoverSettingsImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void inflateCustPreferenceScreen(SettingsPreferenceFragment mainPreferenceFragment, ImageViewPreference imageViewPreference) {
        if (Utils.IS_SHOW_SMART_COVER && mainPreferenceFragment != null) {
            mainPreferenceFragment.addPreferencesFromResource(2131230894);
            if (imageViewPreference != null) {
                imageViewPreference.setSummary(2131629192);
                initPreferences(imageViewPreference);
            }
            setSmartCoverKindPreference(mainPreferenceFragment, imageViewPreference);
            if (this.mStandByPrference != null) {
                mainPreferenceFragment.removePreference(KEY_SMART_COVER_STANDBY_SETTINGS);
            }
            if (this.mPedometerSwitchPreference != null) {
                mainPreferenceFragment.removePreference(KEY_PEDOMETER_SWITCH);
            }
        }
    }

    private void setSmartCoverKindPreference(SettingsPreferenceFragment mainPreferenceFragment, ImageViewPreference imageViewPreference) {
        if (imageViewPreference != null && mainPreferenceFragment != null) {
            Context context = imageViewPreference.getContext();
            if (context != null && mainPreferenceFragment.getPreferenceScreen() != null) {
                final PreferenceScreen mainPreferenceScreen = mainPreferenceFragment.getPreferenceScreen();
                if (mainPreferenceScreen != null) {
                    int orderIndex = imageViewPreference.getOrder();
                    imageViewPreference.cancelAnimation();
                    mainPreferenceScreen.removePreference(imageViewPreference);
                    this.mSmartCoverKindPreference = new SmartCoverKindPreference(context);
                    this.mSmartCoverKindPreference.setOrder(orderIndex);
                    if (this.mSmartCoverKindPreference.checkIfAllowSmartMode()) {
                        addSmartModePreference(mainPreferenceScreen);
                    } else {
                        removeSmartModePreference(mainPreferenceScreen);
                    }
                    this.mSmartCoverKindPreference.setOnSmertCoverChangeListener(new onListenerSwitchStatus() {
                        public void onSmartModeChanged(boolean isChekedSmartMode) {
                            if (isChekedSmartMode) {
                                HwCustSmartCoverSettingsImpl.this.addSmartModePreference(mainPreferenceScreen);
                            } else {
                                HwCustSmartCoverSettingsImpl.this.removeSmartModePreference(mainPreferenceScreen);
                            }
                        }
                    });
                    mainPreferenceScreen.addPreference(this.mSmartCoverKindPreference);
                }
            }
        }
    }

    private void addSmartModePreference(PreferenceScreen mainPreferenceScreen) {
        if (mainPreferenceScreen != null) {
            if (this.mSwitchWeatherPreference != null) {
                mainPreferenceScreen.addPreference(this.mSwitchWeatherPreference);
            }
            if (this.mBackgroundPrference != null) {
                mainPreferenceScreen.addPreference(this.mBackgroundPrference);
            }
            if (this.mAnimationModePreference != null) {
                mainPreferenceScreen.addPreference(this.mAnimationModePreference);
            }
        }
    }

    private void removeSmartModePreference(PreferenceScreen mainPreferenceScreen) {
        if (mainPreferenceScreen != null) {
            if (this.mSwitchWeatherPreference != null) {
                mainPreferenceScreen.removePreference(this.mSwitchWeatherPreference);
            }
            if (this.mBackgroundPrference != null) {
                mainPreferenceScreen.removePreference(this.mBackgroundPrference);
            }
            if (this.mAnimationModePreference != null) {
                mainPreferenceScreen.removePreference(this.mAnimationModePreference);
            }
        }
    }

    private void initPreferences(Preference preference) {
        if (preference != null) {
            PreferenceManager preferenceManager = preference.getPreferenceManager();
            if (preferenceManager != null) {
                this.mBackgroundPrference = preferenceManager.findPreference(KEY_SMART_BACKGROUND_SETTINGS);
                this.mAnimationModePreference = preferenceManager.findPreference(KEY_SMART_COVER_ANIMATION_MODE_SETTINGS);
                this.mSwitchWeatherPreference = (SwitchPreference) preferenceManager.findPreference(KEY_SMART_COVER_WEATHER_SWITCH);
                this.mStandByPrference = preferenceManager.findPreference(KEY_SMART_COVER_STANDBY_SETTINGS);
                this.mPedometerSwitchPreference = (SwitchPreference) preferenceManager.findPreference(KEY_PEDOMETER_SWITCH);
                if (this.mSwitchWeatherPreference != null) {
                    this.mSwitchWeatherPreference.setOnPreferenceChangeListener(this);
                }
            }
        }
    }

    public void onResume() {
        if (Utils.IS_SHOW_SMART_COVER) {
            if (!(this.mObserver == null || this.mContext == null)) {
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_COVER_ENALBED), true, this.mObserver);
            }
            updateSwitchStatus();
            if (this.mSmartCoverKindPreference != null) {
                this.mSmartCoverKindPreference.onResume();
            }
        }
    }

    public void onPause() {
        if (Utils.IS_SHOW_SMART_COVER) {
            if (!(this.mObserver == null || this.mContext == null)) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            }
            if (this.mSmartCoverKindPreference != null) {
                this.mSmartCoverKindPreference.onPause();
            }
        }
    }

    private void updateSwitchStatus() {
        boolean z = true;
        if (this.mContext != null) {
            onModeChanged(Global.getInt(this.mContext.getContentResolver(), KEY_COVER_ENALBED, 1));
            if (this.mBackgroundPrference != null) {
                String coverBgNameHint = this.mContext.getString(2131629211);
                CharSequence coverBgNameHintNone = this.mContext.getString(2131629196);
                int coverBgSrcIndex = Global.getInt(this.mContext.getContentResolver(), "cover_background_src_index", 0);
                Preference preference = this.mBackgroundPrference;
                if (coverBgSrcIndex != 0) {
                    coverBgNameHintNone = coverBgNameHint + coverBgSrcIndex;
                }
                preference.setSummary(coverBgNameHintNone);
            }
            if (this.mAnimationModePreference != null) {
                callbackAnimationModeIndex(Global.getInt(this.mContext.getContentResolver(), "animation_mode_checked", 1));
            }
            if (this.mSwitchWeatherPreference != null) {
                int weatherDisplayMode = Global.getInt(this.mContext.getContentResolver(), KEY_COVER_WEATHER_DISPLAY_MODE, 0);
                SwitchPreference switchPreference = this.mSwitchWeatherPreference;
                if (weatherDisplayMode != 9) {
                    z = false;
                }
                switchPreference.setChecked(z);
            }
        }
    }

    private void callbackAnimationModeIndex(int checkedIndex) {
        if (this.mAnimationModePreference != null) {
            switch (checkedIndex) {
                case 1:
                    this.mAnimationModePreference.setSummary(this.mContext.getString(2131629287));
                    break;
                case 2:
                    this.mAnimationModePreference.setSummary(this.mContext.getString(2131629288));
                    break;
                case 3:
                    this.mAnimationModePreference.setSummary(this.mContext.getString(2131629289));
                    break;
                case 4:
                    this.mAnimationModePreference.setSummary(this.mContext.getString(2131629290));
                    break;
            }
        }
    }

    private void onModeChanged(int mode) {
        switch (mode) {
            case 0:
                setPreferenceEnable(false);
                return;
            case 1:
                setPreferenceEnable(true);
                return;
            default:
                return;
        }
    }

    private void setPreferenceEnable(boolean isEnable) {
        if (this.mBackgroundPrference != null) {
            this.mBackgroundPrference.setEnabled(isEnable);
        }
        if (this.mAnimationModePreference != null) {
            this.mAnimationModePreference.setEnabled(isEnable);
        }
        if (this.mSwitchWeatherPreference != null) {
            this.mSwitchWeatherPreference.setEnabled(isEnable);
        }
        if (this.mStandByPrference != null) {
            this.mStandByPrference.setEnabled(isEnable);
        }
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        boolean isChecked = ((Boolean) newValue).booleanValue();
        if (pref == null) {
            return false;
        }
        if (KEY_SMART_COVER_WEATHER_SWITCH.equals(pref.getKey())) {
            int i;
            if (isChecked) {
                i = 9;
            } else {
                i = COVER_WEATHER_DISPLAY_OFF;
            }
            setWeatherDisplayEnable(i);
        }
        return true;
    }

    private void setWeatherDisplayEnable(int mode) {
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), KEY_COVER_WEATHER_DISPLAY_MODE, mode);
        }
    }
}
