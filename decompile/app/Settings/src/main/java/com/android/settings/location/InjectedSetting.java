package com.android.settings.location;

import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.Immutable;
import com.android.internal.util.Preconditions;

@Immutable
class InjectedSetting {
    public final String className;
    public final int iconId;
    public final UserHandle mUserHandle;
    public final String packageName;
    public final String settingsActivity;
    public final String title;

    private InjectedSetting(String packageName, String className, String title, int iconId, UserHandle userHandle, String settingsActivity) {
        this.packageName = (String) Preconditions.checkNotNull(packageName, "packageName");
        this.className = (String) Preconditions.checkNotNull(className, "className");
        this.title = (String) Preconditions.checkNotNull(title, "title");
        this.iconId = iconId;
        this.mUserHandle = userHandle;
        this.settingsActivity = (String) Preconditions.checkNotNull(settingsActivity);
    }

    public static InjectedSetting newInstance(String packageName, String className, String title, int iconId, UserHandle userHandle, String settingsActivity) {
        if (packageName != null && className != null && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(settingsActivity)) {
            return new InjectedSetting(packageName, className, title, iconId, userHandle, settingsActivity);
        }
        if (Log.isLoggable("SettingsInjector", 5)) {
            Log.w("SettingsInjector", "Illegal setting specification: package=" + packageName + ", class=" + className + ", title=" + title + ", settingsActivity=" + settingsActivity);
        }
        return null;
    }

    public String toString() {
        return "InjectedSetting{mPackageName='" + this.packageName + '\'' + ", mClassName='" + this.className + '\'' + ", label=" + this.title + ", iconId=" + this.iconId + ", userId=" + this.mUserHandle.getIdentifier() + ", settingsActivity='" + this.settingsActivity + '\'' + '}';
    }

    public Intent getServiceIntent() {
        Intent intent = new Intent();
        intent.setClassName(this.packageName, this.className);
        return intent;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof InjectedSetting)) {
            return false;
        }
        InjectedSetting that = (InjectedSetting) o;
        if (this.packageName.equals(that.packageName) && this.className.equals(that.className) && this.title.equals(that.title) && this.iconId == that.iconId && this.mUserHandle.equals(that.mUserHandle)) {
            z = this.settingsActivity.equals(that.settingsActivity);
        }
        return z;
    }

    public int hashCode() {
        return (((((((((this.packageName.hashCode() * 31) + this.className.hashCode()) * 31) + this.title.hashCode()) * 31) + this.iconId) * 31) + this.mUserHandle.hashCode()) * 31) + this.settingsActivity.hashCode();
    }
}
