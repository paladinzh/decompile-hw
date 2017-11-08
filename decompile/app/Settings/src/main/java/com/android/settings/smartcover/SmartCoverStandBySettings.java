package com.android.settings.smartcover;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.ImageViewPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.smartcover.RadioButtonPreference.OnClickListener;

@SuppressLint({"NewApi"})
public class SmartCoverStandBySettings extends SettingsPreferenceFragment implements OnClickListener, OnPreferenceChangeListener {
    private IntentFilter mIntentFilterWordChanged = null;
    private RadioButtonPreference mOnlyTime;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("action_animation_mode_text_changed".equals(intent.getAction())) {
                    String standByWord = intent.getStringExtra("standByWord");
                    Global.putString(SmartCoverStandBySettings.this.getActivity().getContentResolver(), "cover_standby_word", standByWord);
                    SmartCoverStandBySettings.this.updateStandbyWord(standByWord);
                }
            }
        }
    };
    private SwitchPreference mSwitchPreference;
    private RadioButtonPreference mTimeWord;
    private ImageViewPreference mTutorialPreference;
    private Preference mWordSetting = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230897);
        this.mSwitchPreference = (SwitchPreference) findPreference("smart_cover_standby_switch");
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setOnPreferenceChangeListener(this);
        }
        this.mTutorialPreference = (ImageViewPreference) findPreference("smart_cover_standby_tutorial");
        this.mOnlyTime = (RadioButtonPreference) findPreference("standby_only_time");
        if (this.mOnlyTime != null) {
            this.mOnlyTime.setOnClickListener(this);
        }
        this.mTimeWord = (RadioButtonPreference) findPreference("standby_time_word");
        if (this.mTimeWord != null) {
            this.mTimeWord.setOnClickListener(this);
        }
        this.mWordSetting = findPreference("cover_standby_word_setting_title");
        this.mIntentFilterWordChanged = new IntentFilter("action_animation_mode_text_changed");
    }

    private void updateStandbyWord(String standByWorkd) {
        if (this.mWordSetting != null) {
            this.mWordSetting.setSummary((CharSequence) standByWorkd);
        }
    }

    private void loadSettings() {
        if (this.mSwitchPreference != null) {
            boolean z;
            int standbySelfMode = Global.getInt(getActivity().getContentResolver(), "cover_standby_self_mode", 9);
            SwitchPreference switchPreference = this.mSwitchPreference;
            if (standbySelfMode == 9) {
                z = true;
            } else {
                z = false;
            }
            switchPreference.setChecked(z);
            setSelfStandBySwitchEnable(standbySelfMode);
        }
        onModeChanged(Global.getInt(getActivity().getContentResolver(), "cover_standby_self_method", 2));
        updateStandbyWord(Global.getString(getActivity().getContentResolver(), "cover_standby_word"));
    }

    private void setSelfStandBySwitchEnable(int standbyMethod) {
        boolean isEnable = standbyMethod == 9;
        if (this.mOnlyTime != null) {
            this.mOnlyTime.setEnabled(isEnable);
        }
        if (this.mTimeWord != null) {
            this.mTimeWord.setEnabled(isEnable);
            setWordSettingEnable(isEnable);
        }
    }

    private void setWordSettingEnable(boolean switchChecked) {
        if (this.mTimeWord != null) {
            boolean z;
            boolean isTimeWordChecked = this.mTimeWord.isChecked();
            Preference preference = this.mWordSetting;
            if (switchChecked && isTimeWordChecked) {
                z = true;
            } else {
                z = false;
            }
            preference.setEnabled(z);
        }
    }

    @SuppressLint({"NewApi"})
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilterWordChanged);
        loadSettings();
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mTutorialPreference != null) {
            this.mTutorialPreference.cancelAnimation();
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        int mode = 0;
        if (emiter == this.mOnlyTime) {
            mode = 1;
        } else if (emiter == this.mTimeWord) {
            mode = 2;
        }
        Global.putInt(getActivity().getContentResolver(), "cover_standby_self_method", mode);
        onModeChanged(mode);
    }

    public void onModeChanged(int mode) {
        switch (mode) {
            case 1:
                updateRadioButtons(this.mOnlyTime);
                return;
            case 2:
                updateRadioButtons(this.mTimeWord);
                return;
            default:
                return;
        }
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated != null) {
            if (activated == this.mOnlyTime) {
                this.mOnlyTime.setChecked(true);
                this.mTimeWord.setChecked(false);
            } else if (activated == this.mTimeWord) {
                this.mTimeWord.setChecked(true);
                this.mOnlyTime.setChecked(false);
            }
            setWordSettingEnable(this.mSwitchPreference.isChecked());
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mWordSetting) {
            new StandByWordDialogFragment().show(getFragmentManager(), "rename device");
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if ("smart_cover_standby_switch".equals(pref.getKey())) {
            int standbyMode;
            if (((Boolean) newValue).booleanValue()) {
                standbyMode = 9;
            } else {
                standbyMode = -1;
            }
            Global.putInt(getActivity().getContentResolver(), "cover_standby_self_mode", standbyMode);
            setSelfStandBySwitchEnable(standbyMode);
        }
        return true;
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
