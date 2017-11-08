package com.android.settings.smartcover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.smartcover.SmartCoverAnimationModePreference.onListenerTextModeChecked;

public class SmartCoverAnimationModeSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private SmartCoverAnimationModePreference mAnimationModePreference;
    private Preference mAnimationTextModePreference;
    private Context mContext = null;
    private Preference mImageViewPreference;
    private IntentFilter mIntentFilterTextChanged = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("action_animation_mode_text_changed".equals(intent.getAction())) {
                    String animationModeText = intent.getStringExtra("animation_mode_text");
                    SmartCoverAnimationModeSettings.this.saveAnimationModeTextStr(animationModeText);
                    SmartCoverAnimationModeSettings.this.updateAnimationModeText(animationModeText);
                }
            }
        }
    };
    private SwitchPreference mSwitchAnimationPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230893);
        this.mContext = getActivity();
        initView();
    }

    private void initView() {
        if (this.mContext != null) {
            this.mImageViewPreference = findPreference("smart_cover_animation_mode_preference");
            this.mAnimationTextModePreference = findPreference("animation_standby_text_mode_settings");
            this.mSwitchAnimationPreference = (SwitchPreference) findPreference("smart_cover_animation_switch");
            if (this.mImageViewPreference != null && this.mAnimationTextModePreference != null) {
                if (this.mSwitchAnimationPreference != null) {
                    this.mSwitchAnimationPreference.setOnPreferenceChangeListener(this);
                }
                int orderIndex = this.mImageViewPreference.getOrder();
                this.mAnimationModePreference = new SmartCoverAnimationModePreference(this.mContext);
                PreferenceScreen mainPreferenceScreen = getPreferenceScreen();
                if (mainPreferenceScreen != null) {
                    mainPreferenceScreen.removePreference(this.mImageViewPreference);
                    this.mAnimationModePreference.setOrder(orderIndex);
                    this.mAnimationModePreference.setOnTextModeChangeListener(new onListenerTextModeChecked() {
                        public void onTextModeChanged(boolean isCheckedTextMode) {
                            if (isCheckedTextMode) {
                                SmartCoverAnimationModeSettings.this.setAnimationTextModeEnable(true);
                            } else {
                                SmartCoverAnimationModeSettings.this.setAnimationTextModeEnable(false);
                            }
                        }
                    });
                    mainPreferenceScreen.addPreference(this.mAnimationModePreference);
                    this.mIntentFilterTextChanged = new IntentFilter("action_animation_mode_text_changed");
                }
            }
        }
    }

    private void updateSwitchStatus() {
        if (this.mContext != null) {
            String checkAnimationTextStr;
            if (this.mSwitchAnimationPreference != null) {
                boolean z;
                int animationDisplayMode = Global.getInt(this.mContext.getContentResolver(), "cover_animation_display_mode", -1);
                SwitchPreference switchPreference = this.mSwitchAnimationPreference;
                if (animationDisplayMode == 9) {
                    z = true;
                } else {
                    z = false;
                }
                switchPreference.setChecked(z);
            }
            if (this.mAnimationTextModePreference != null) {
                if (getAnimationTextModeIndex() < 4 || !this.mSwitchAnimationPreference.isChecked()) {
                    setAnimationTextModeEnable(false);
                } else {
                    setAnimationTextModeEnable(true);
                }
            }
            String animationModeText = Global.getString(getActivity().getContentResolver(), "cover_standby_word");
            if (animationModeText == null) {
                checkAnimationTextStr = this.mContext.getString(2131629205);
            } else {
                checkAnimationTextStr = animationModeText;
            }
            updateAnimationModeText(checkAnimationTextStr);
            saveAnimationModeTextStr(checkAnimationTextStr);
        }
    }

    private void setAnimationTextModeEnable(boolean isChecked) {
        if (this.mAnimationTextModePreference != null) {
            this.mAnimationTextModePreference.setEnabled(isChecked);
        }
    }

    private int getAnimationTextModeIndex() {
        return Global.getInt(this.mContext.getContentResolver(), "animation_mode_checked", 1);
    }

    private void updateAnimationModeText(String modeTextStr) {
        if (this.mAnimationTextModePreference != null) {
            this.mAnimationTextModePreference.setSummary((CharSequence) modeTextStr);
        }
    }

    private void saveAnimationModeTextStr(String standByText) {
        if (standByText != null) {
            Global.putString(getActivity().getContentResolver(), "cover_standby_word", standByText);
        }
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilterTextChanged);
        updateSwitchStatus();
        if (this.mAnimationModePreference != null) {
            this.mAnimationModePreference.onResume();
        }
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        if (this.mAnimationModePreference != null) {
            this.mAnimationModePreference.onPause();
        }
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = ((Boolean) newValue).booleanValue();
        if (preference == null) {
            return false;
        }
        if ("smart_cover_animation_switch".equals(preference.getKey())) {
            int i;
            if (isChecked) {
                i = 9;
            } else {
                i = -1;
            }
            setAnimationDisplayEnable(i);
        }
        if (this.mAnimationTextModePreference != null) {
            boolean z;
            Preference preference2 = this.mAnimationTextModePreference;
            if (!isChecked || getAnimationTextModeIndex() < 4) {
                z = false;
            } else {
                z = true;
            }
            preference2.setEnabled(z);
        }
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mAnimationTextModePreference) {
            new StandByWordDialogFragment().show(getFragmentManager(), "Rename Text Mode");
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void setAnimationDisplayEnable(int mode) {
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), "cover_animation_display_mode", mode);
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
