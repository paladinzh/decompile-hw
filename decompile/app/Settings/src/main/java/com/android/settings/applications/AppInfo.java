package com.android.settings.applications;

import android.graphics.drawable.Drawable;
import java.text.Collator;

public class AppInfo implements Comparable<AppInfo> {
    private static Collator sCollator = Collator.getInstance();
    private boolean mChecked;
    private Drawable mIcon;
    private String mLabel;
    private String mPackageName;
    private String summary;

    public AppInfo(boolean checked, Drawable mIcon, String mPackageName, String mLabel, String summary) {
        this.mChecked = checked;
        this.mIcon = mIcon;
        this.mPackageName = mPackageName;
        this.mLabel = mLabel;
        this.summary = summary;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean getChecked() {
        return this.mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    public Drawable getmIcon() {
        return this.mIcon;
    }

    public String getmPackageName() {
        return this.mPackageName;
    }

    public String getmLabel() {
        return this.mLabel;
    }

    public int compareTo(AppInfo another) {
        if (this.mLabel == null || another == null || another.getmLabel() == null) {
            return 0;
        }
        return sCollator.compare(this.mLabel, another.getmLabel());
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof AppInfo) {
            return ((AppInfo) obj).getmPackageName().equals(this.mPackageName);
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
