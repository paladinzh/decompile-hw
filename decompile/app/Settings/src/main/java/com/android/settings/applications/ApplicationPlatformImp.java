package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

class ApplicationPlatformImp extends ApplicationExtAbsBase {
    ApplicationPlatformImp() {
    }

    public void registerBlackListSharePreferenceListener(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        super.registerBlackListSharePreferenceListener(applications, filterAdapter);
    }

    public void unregisterBlackListSharePreferenceListener() {
        super.unregisterBlackListSharePreferenceListener();
    }

    public void setShowingBlackListAppFlagIfNeeded(Context context, Intent intent) {
        super.setShowingBlackListAppFlagIfNeed(context, intent);
    }

    public void changeDisableTextViewIfNeeded(String packageName, TextView disabled) {
        super.changeDisableTextViewIfNeeded(packageName, disabled);
    }

    public void selectForbiddenFilterIfNeeded(ManageApplications applications, Spinner filterSpinner, ArrayList<Integer> filterOptions) {
        super.selectForbiddenFilterIfNeeded(applications, filterSpinner, filterOptions);
    }

    public void setForbiddenFilterEnabledIfNeeded(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        super.setForbiddenFilterEnabledIfNeeded(applications, filterAdapter);
    }

    public void showWarningDialog(Context context, ApplicationInfo info) {
        super.showWarningDialog(context, info);
    }

    public void dismissWarningDialog() {
        super.dismissWarningDialog();
    }

    public void setShowingBlackListAppFlag(boolean state) {
        super.setShowingBlackListAppFlag(state);
    }

    public boolean getShowingBlackListAppFlag() {
        return super.getShowingBlackListAppFlag();
    }
}
