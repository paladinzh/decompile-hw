package com.android.settingslib.wifi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Looper;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settingslib.R$attr;
import com.android.settingslib.R$string;

public class AccessPointPreference extends Preference {
    private static final int[] STATE_NONE = new int[0];
    private static final int[] STATE_SECURED = new int[]{R$attr.state_encrypted};
    static final int[] WIFI_CONNECTION_STRENGTH = new int[]{R$string.accessibility_wifi_one_bar, R$string.accessibility_wifi_two_bars, R$string.accessibility_wifi_three_bars, R$string.accessibility_wifi_signal_full};
    private static int[] wifi_signal_attributes = new int[]{R$attr.wifi_signal};
    private AccessPoint mAccessPoint;
    private Drawable mBadge;
    private final UserBadgeCache mBadgeCache = null;
    private final int mBadgePadding = 0;
    private CharSequence mContentDescription;
    private boolean mForSavedNetworks = false;
    private int mLevel;
    private final Runnable mNotifyChanged = new Runnable() {
        public void run() {
            AccessPointPreference.this.notifyChanged();
        }
    };
    private TextView mTitleView;
    private final StateListDrawable mWifiSld = null;

    public static class UserBadgeCache {
    }

    public AccessPointPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mAccessPoint != null) {
            Drawable drawable = getIcon();
            if (drawable != null) {
                drawable.setLevel(this.mLevel);
            }
            this.mTitleView = (TextView) view.findViewById(16908310);
            if (this.mTitleView != null) {
                this.mTitleView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, this.mBadge, null);
                this.mTitleView.setCompoundDrawablePadding(this.mBadgePadding);
            }
            view.itemView.setContentDescription(this.mContentDescription);
        }
    }

    protected void notifyChanged() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            postNotifyChanged();
        } else {
            super.notifyChanged();
        }
    }

    private void postNotifyChanged() {
        if (this.mTitleView != null) {
            this.mTitleView.post(this.mNotifyChanged);
        }
    }
}
