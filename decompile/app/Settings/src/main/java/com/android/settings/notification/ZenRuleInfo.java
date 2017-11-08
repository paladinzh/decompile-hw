package com.android.settings.notification;

import android.content.ComponentName;
import android.net.Uri;

public class ZenRuleInfo {
    public ComponentName configurationActivity;
    public Uri defaultConditionId;
    public boolean isSystem;
    public CharSequence packageLabel;
    public String packageName;
    public int ruleInstanceLimit = -1;
    public ComponentName serviceComponent;
    public String settingsAction;
    public String title;

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZenRuleInfo that = (ZenRuleInfo) o;
        if (this.isSystem != that.isSystem || this.ruleInstanceLimit != that.ruleInstanceLimit) {
            return false;
        }
        if (!this.packageName == null ? this.packageName.equals(that.packageName) : that.packageName == null) {
            return false;
        }
        if (!this.title == null ? this.title.equals(that.title) : that.title == null) {
            return false;
        }
        if (!this.settingsAction == null ? this.settingsAction.equals(that.settingsAction) : that.settingsAction == null) {
            return false;
        }
        if (!this.configurationActivity == null ? this.configurationActivity.equals(that.configurationActivity) : that.configurationActivity == null) {
            return false;
        }
        if (!this.defaultConditionId == null ? this.defaultConditionId.equals(that.defaultConditionId) : that.defaultConditionId == null) {
            return false;
        }
        if (!this.serviceComponent == null ? this.serviceComponent.equals(that.serviceComponent) : that.serviceComponent == null) {
            return false;
        }
        if (this.packageLabel != null) {
            z = this.packageLabel.equals(that.packageLabel);
        } else if (that.packageLabel != null) {
            z = false;
        }
        return z;
    }
}
