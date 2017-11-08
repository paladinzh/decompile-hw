package com.android.settingslib;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;

public class RestrictedLockUtils {

    public static class EnforcedAdmin {
        public static final EnforcedAdmin MULTIPLE_ENFORCED_ADMIN = new EnforcedAdmin();
        public ComponentName component = null;
        public int userId = -10000;

        public EnforcedAdmin(ComponentName component, int userId) {
            this.component = component;
            this.userId = userId;
        }

        public EnforcedAdmin(EnforcedAdmin other) {
            if (other == null) {
                throw new IllegalArgumentException();
            }
            this.component = other.component;
            this.userId = other.userId;
        }

        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof EnforcedAdmin)) {
                return false;
            }
            EnforcedAdmin other = (EnforcedAdmin) object;
            if (this.userId != other.userId) {
                return false;
            }
            return (this.component == null && other.component == null) || (this.component != null && this.component.equals(other.component));
        }

        public String toString() {
            return "EnforcedAdmin{component=" + this.component + ",userId=" + this.userId + "}";
        }

        public void copyTo(EnforcedAdmin other) {
            if (other == null) {
                throw new IllegalArgumentException();
            }
            other.component = this.component;
            other.userId = this.userId;
        }
    }

    public static EnforcedAdmin checkIfRestrictionEnforced(Context context, String userRestriction, int userId) {
        if (((DevicePolicyManager) context.getSystemService("device_policy")) == null) {
            return null;
        }
        int restrictionSource = UserManager.get(context).getUserRestrictionSource(userRestriction, UserHandle.of(userId));
        if (restrictionSource == 0 || restrictionSource == 1) {
            return null;
        }
        boolean enforcedByProfileOwner = (restrictionSource & 4) != 0;
        boolean enforcedByDeviceOwner = (restrictionSource & 2) != 0;
        if (enforcedByProfileOwner) {
            return getProfileOwner(context, userId);
        }
        if (!enforcedByDeviceOwner) {
            return null;
        }
        EnforcedAdmin deviceOwner = getDeviceOwner(context);
        if (deviceOwner == null) {
            return EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
        }
        if (deviceOwner.userId != userId) {
            deviceOwner = EnforcedAdmin.MULTIPLE_ENFORCED_ADMIN;
        }
        return deviceOwner;
    }

    public static boolean hasBaseUserRestriction(Context context, String userRestriction, int userId) {
        return ((UserManager) context.getSystemService("user")).hasBaseUserRestriction(userRestriction, UserHandle.of(userId));
    }

    public static EnforcedAdmin getDeviceOwner(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        ComponentName adminComponent = dpm.getDeviceOwnerComponentOnAnyUser();
        if (adminComponent != null) {
            return new EnforcedAdmin(adminComponent, dpm.getDeviceOwnerUserId());
        }
        return null;
    }

    private static EnforcedAdmin getProfileOwner(Context context, int userId) {
        if (userId == -10000) {
            return null;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        if (dpm == null) {
            return null;
        }
        ComponentName adminComponent = dpm.getProfileOwnerAsUser(userId);
        if (adminComponent != null) {
            return new EnforcedAdmin(adminComponent, userId);
        }
        return null;
    }

    public static void sendShowAdminSupportDetailsIntent(Context context, EnforcedAdmin admin) {
        Intent intent = getShowAdminSupportDetailsIntent(context, admin);
        int targetUserId = UserHandle.myUserId();
        if (!(admin == null || admin.userId == -10000 || !isCurrentUserOrProfile(context, admin.userId))) {
            targetUserId = admin.userId;
        }
        context.startActivityAsUser(intent, new UserHandle(targetUserId));
    }

    public static Intent getShowAdminSupportDetailsIntent(Context context, EnforcedAdmin admin) {
        Intent intent = new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS");
        if (admin != null) {
            if (admin.component != null) {
                intent.putExtra("android.app.extra.DEVICE_ADMIN", admin.component);
            }
            int adminUserId = UserHandle.myUserId();
            if (admin.userId != -10000) {
                adminUserId = admin.userId;
            }
            intent.putExtra("android.intent.extra.USER_ID", adminUserId);
        }
        return intent;
    }

    public static boolean isCurrentUserOrProfile(Context context, int userId) {
        for (UserInfo userInfo : UserManager.get(context).getProfiles(UserHandle.myUserId())) {
            if (userInfo.id == userId) {
                return true;
            }
        }
        return false;
    }
}
