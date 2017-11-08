package com.android.settings.accessibility;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import com.android.settings.ItemUseStat;
import com.android.settings.views.GifView;

public class ToggleScreenMagnificationPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnPreferenceChangeListener {
    protected GifPreference mGifPreference;
    private Handler mObserverhandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean value = Secure.getInt(ToggleScreenMagnificationPreferenceFragment.this.getContentResolver(), "accessibility_display_magnification_enabled", 0) == 1;
            ToggleScreenMagnificationPreferenceFragment.this.mToggleSwitch.setChecked(value);
            ToggleScreenMagnificationPreferenceFragment.this.getArguments().putBoolean("checked", value);
        }
    };
    private final ContentObserver mSettingsObserver = new ContentObserver(this.mObserverhandler) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            ToggleScreenMagnificationPreferenceFragment.this.mObserverhandler.sendMessage(Message.obtain());
        }
    };

    protected static class GifPreference extends Preference {
        public GifPreference(Context context) {
            super(context);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            GifView gifView = (GifView) view.findViewById(2131886712);
            gifView.setMovieResource(2130837581);
            gifView.resetMovieTime();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mGifPreference = new GifPreference(getPrefContext());
        this.mGifPreference.setLayoutResource(2130968826);
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        preferenceScreen.setOrderingAsAdded(false);
        this.mGifPreference.setOrder(0);
        this.mSummaryPreference.setOrder(1);
        preferenceScreen.addPreference(this.mGifPreference);
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_magnification_enabled"), true, this.mSettingsObserver);
    }

    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        Secure.putInt(getContentResolver(), "accessibility_display_magnification_enabled", enabled ? 1 : 0);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
        if (!this.mToggleSwitch.isChecked()) {
            setMagnificationEnabled(0);
        }
    }

    private void setMagnificationEnabled(int enabled) {
        Secure.putInt(getContentResolver(), "accessibility_display_magnification_enabled", enabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mToggleSwitch == preference) {
            boolean value = ((Boolean) newValue).booleanValue();
            getArguments().putBoolean("checked", value);
            onPreferenceToggled(this.mPreferenceKey, value);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        return true;
    }

    protected void onInstallToggleSwitch() {
        this.mToggleSwitch.setKey("screen_magnification");
        this.mToggleSwitch.setTitle(2131625853);
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
    }

    public void onDestroy() {
        getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 7;
    }
}
