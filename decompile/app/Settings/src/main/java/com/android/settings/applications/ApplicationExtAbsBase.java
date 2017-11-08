package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

abstract class ApplicationExtAbsBase {
    private static String TAG = "ApplicationExtAbsBase";

    ApplicationExtAbsBase() {
    }

    public void registerBlackListSharePreferenceListener(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        if (applications != null && filterAdapter != null) {
            BlackListUtils.registerBlackListSharePreferenceListener(applications, filterAdapter);
        }
    }

    public void unregisterBlackListSharePreferenceListener() {
        BlackListUtils.unregisterBlackListSharePreferenceListener();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setShowingBlackListAppFlagIfNeed(Context context, Intent intent) {
        if (!(context == null || intent == null || intent.getExtra("showBlackListApp") == null || !intent.getBooleanExtra("showBlackListApp", false) || !BlackListUtils.hasForBiddenApps(context))) {
            BlackListUtils.setShowingBlackListAppFlag(true);
        }
    }

    public void setForbiddenFilterEnabledIfNeeded(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        if (applications != null && filterAdapter != null) {
            BlackListUtils.setHasForbidden(applications, filterAdapter, BlackListUtils.hasForBiddenApps(applications.getActivity()));
        }
    }

    public void changeDisableTextViewIfNeeded(String packageName, TextView disabled) {
        if (disabled != null && !TextUtils.isEmpty(packageName)) {
            disabled.setTextColor(2130706432);
            if (BlackListUtils.isBlackListApp(packageName)) {
                disabled.setVisibility(0);
                disabled.setText(2131628368);
                disabled.setTextColor(-65536);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void selectForbiddenFilterIfNeeded(ManageApplications applications, Spinner filterSpinner, ArrayList<Integer> filterOptions) {
        if (!(applications == null || filterOptions == null || filterOptions.size() == 0 || !BlackListUtils.getShowingBlackListAppFlag())) {
            int indexOfForbiddenFilter = filterOptions.indexOf(Integer.valueOf(101));
            if (indexOfForbiddenFilter != -1) {
                filterSpinner.setSelection(indexOfForbiddenFilter);
                applications.onItemSelected(null, null, indexOfForbiddenFilter, (long) indexOfForbiddenFilter);
            }
        }
    }

    public void showWarningDialog(Context context, ApplicationInfo info) {
        if (context != null && info != null) {
            BlackListUtils.showWarningDialog(context, info);
        }
    }

    public void dismissWarningDialog() {
        BlackListUtils.dismissWarningDialog();
    }

    public boolean getShowingBlackListAppFlag() {
        return BlackListUtils.getShowingBlackListAppFlag();
    }

    public void setShowingBlackListAppFlag(boolean state) {
        BlackListUtils.setShowingBlackListAppFlag(state);
    }
}
