package com.android.settings.wifi;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import com.android.settings.wifi.AccessPointPreference.UserBadgeCache;
import com.android.settingslib.wifi.AccessPoint;

public class LongPressAccessPointPreference extends AccessPointPreference {
    private final Fragment mFragment;

    public LongPressAccessPointPreference(AccessPoint accessPoint, Context context, UserBadgeCache cache, boolean forSavedNetworks, Fragment fragment) {
        super(accessPoint, context, cache, forSavedNetworks);
        this.mFragment = fragment;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mFragment != null) {
            view.itemView.setOnCreateContextMenuListener(this.mFragment);
            view.itemView.setTag(this);
            view.itemView.setLongClickable(true);
        }
    }
}
