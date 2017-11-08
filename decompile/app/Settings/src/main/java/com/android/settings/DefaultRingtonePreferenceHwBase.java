package com.android.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings.System;
import android.util.AttributeSet;

public class DefaultRingtonePreferenceHwBase extends RingtonePreference {
    private Fragment mParentFragment;

    public DefaultRingtonePreferenceHwBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFragment(Fragment fragment) {
        this.mParentFragment = fragment;
    }

    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", true);
        if (getRingtoneType() == 2) {
            String notificationPath = System.getString(getContext().getContentResolver(), "theme_notification_sound_path");
            if (notificationPath != null) {
                ringtonePickerIntent.putExtra("android.intent.extra.ringtone.DEFAULT_STRING", notificationPath);
            }
        }
    }

    protected void onClick() {
        Intent musicIntent = new Intent("android.intent.action.RINGTONE_PICKER");
        musicIntent.addCategory("android.intent.category.HWRING");
        onPrepareRingtonePickerIntent(musicIntent);
        try {
            if (this.mParentFragment != null) {
                this.mParentFragment.startActivityForResult(musicIntent, 20);
                SettingsExtUtils.setAnimationReflection(this.mParentFragment.getActivity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeRingtone(Uri ringtoneUri) {
        onSaveRingtone(ringtoneUri);
    }
}
