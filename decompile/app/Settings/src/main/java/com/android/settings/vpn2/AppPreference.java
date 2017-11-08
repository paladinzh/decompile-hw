package com.android.settings.vpn2;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import com.android.internal.net.VpnConfig;

public class AppPreference extends ManageablePreference {
    public static final int STATE_DISCONNECTED = STATE_NONE;
    private final String mName;
    private final String mPackageName;

    public AppPreference(Context context, int userId, String packageName) {
        super(context, null);
        super.setUserId(userId);
        this.mPackageName = packageName;
        String label = packageName;
        Drawable drawable = null;
        try {
            Context userContext = getUserContext();
            PackageManager pm = userContext.getPackageManager();
            try {
                PackageInfo pkgInfo = pm.getPackageInfo(this.mPackageName, 0);
                if (pkgInfo != null) {
                    drawable = pkgInfo.applicationInfo.loadIcon(pm);
                    label = VpnConfig.getVpnLabel(userContext, this.mPackageName).toString();
                }
            } catch (NameNotFoundException e) {
            }
            if (drawable == null) {
                drawable = pm.getDefaultActivityIcon();
            }
        } catch (NameNotFoundException e2) {
        }
        this.mName = label;
        setTitle(this.mName);
        setIcon(drawable);
    }

    public PackageInfo getPackageInfo() {
        try {
            return getUserContext().getPackageManager().getPackageInfo(this.mPackageName, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public String getLabel() {
        return this.mName;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    private Context getUserContext() throws NameNotFoundException {
        return getContext().createPackageContextAsUser(getContext().getPackageName(), 0, UserHandle.of(this.mUserId));
    }

    public int compareTo(Preference preference) {
        if (preference instanceof AppPreference) {
            AppPreference another = (AppPreference) preference;
            int result = another.mState - this.mState;
            if (result == 0) {
                result = this.mName.compareToIgnoreCase(another.mName);
                if (result == 0) {
                    result = this.mPackageName.compareTo(another.mPackageName);
                    if (result == 0) {
                        result = this.mUserId - another.mUserId;
                    }
                }
            }
            return result;
        } else if (preference instanceof LegacyVpnPreference) {
            return -((LegacyVpnPreference) preference).compareTo(this);
        } else {
            return super.compareTo(preference);
        }
    }
}
