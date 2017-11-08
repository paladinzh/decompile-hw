package com.huawei.permissionmanager.model;

import android.graphics.drawable.Drawable;

public final class PermissionGroup implements Comparable<PermissionGroup> {
    private final String mDeclaringPackage;
    private final Drawable mIcon;
    private final CharSequence mLabel;
    private final String mName;

    PermissionGroup(String name, String declaringPackage, CharSequence label, Drawable icon) {
        this.mDeclaringPackage = declaringPackage;
        this.mName = name;
        this.mLabel = label;
        this.mIcon = icon;
    }

    public String getName() {
        return this.mName;
    }

    public String getDeclaringPackage() {
        return this.mDeclaringPackage;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public int compareTo(PermissionGroup another) {
        return this.mLabel.toString().compareTo(another.mLabel.toString());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PermissionGroup other = (PermissionGroup) obj;
        if (this.mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!this.mName.equals(other.mName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mName != null ? this.mName.hashCode() : 0;
    }
}
