package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;

public class StorageItemPreference extends Preference {
    public int userHandle;

    public StorageItemPreference(Context context) {
        super(context);
        setLayoutResource(2130968980);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
    }
}
