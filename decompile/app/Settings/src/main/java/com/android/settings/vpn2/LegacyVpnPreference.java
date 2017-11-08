package com.android.settings.vpn2;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import com.android.internal.net.VpnProfile;

public class LegacyVpnPreference extends ManageablePreference {
    private final Fragment mFragment;

    LegacyVpnPreference(Context context, Fragment fragment) {
        super(context, null);
        this.mFragment = fragment;
    }

    public VpnProfile getProfile() {
        return this.mProfile;
    }

    public void setProfile(VpnProfile profile) {
        String newLabel = null;
        CharSequence charSequence = this.mProfile != null ? this.mProfile.name : null;
        if (profile != null) {
            newLabel = profile.name;
        }
        if (!TextUtils.equals(charSequence, newLabel)) {
            setTitle((CharSequence) newLabel);
            notifyHierarchyChanged();
        }
        this.mProfile = profile;
    }

    public int compareTo(Preference preference) {
        if (preference instanceof LegacyVpnPreference) {
            LegacyVpnPreference another = (LegacyVpnPreference) preference;
            int result = another.mState - this.mState;
            if (result == 0) {
                result = this.mProfile.name.compareToIgnoreCase(another.mProfile.name);
                if (result == 0) {
                    result = this.mProfile.type - another.mProfile.type;
                    if (result == 0) {
                        result = this.mProfile.key.compareTo(another.mProfile.key);
                    }
                }
            }
            return result;
        } else if (!(preference instanceof AppPreference)) {
            return super.compareTo(preference);
        } else {
            AppPreference another2 = (AppPreference) preference;
            if (this.mState == 3 || another2.getState() != 3) {
                return -1;
            }
            return 1;
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (this.mFragment != null) {
            view.itemView.setOnCreateContextMenuListener(this.mFragment);
            view.itemView.setTag(this);
            view.itemView.setLongClickable(true);
        }
    }

    public void update() {
        updateSummary();
        if (this.mProfile != null) {
            setTitle(this.mProfile.name);
        }
        notifyHierarchyChanged();
    }
}
