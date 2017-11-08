package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.twopane.TwoPaneActivity;
import com.huawei.cspcommon.ICspActivity;
import com.huawei.mms.util.FloatMmsRequsetReceiver;

public class CspFragment extends HwBaseFragment {
    private static boolean sContactChangedWhenPause = false;
    private static boolean sFragActived = false;
    private static boolean sNotificationCleared = false;
    private int mFirstLauchTab = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MmsConfig.checkSimpleUi();
        Activity activity = getActivity();
        if (activity != null) {
            int i;
            if (MmsConfig.isInSimpleUI()) {
                i = 1;
            } else {
                i = -1;
            }
            activity.setRequestedOrientation(i);
        }
    }

    public void onPause() {
        super.onPause();
        setFragmentActived(false);
    }

    public boolean checkIsFragmentActived() {
        checkFragmentTab();
        return isFragmentActived();
    }

    private void checkFragmentTab() {
        boolean z = true;
        Activity act = getActivity();
        if (act == null) {
            setFragmentActived(false);
        } else if (act instanceof ICspActivity) {
            if (((ICspActivity) act).getCurrentTab() != 2) {
                z = false;
            }
            setFragmentActived(z);
        } else if (act instanceof ConversationList) {
            setFragmentActived(true);
        } else if (act instanceof TwoPaneActivity) {
            setFragmentActived(true);
        }
    }

    public void onResume() {
        super.onResume();
        checkFragmentTab();
        if (isFragmentActived()) {
            clearNotification();
        }
    }

    public void clearNotification() {
        if (!sNotificationCleared && isFragmentActived()) {
            MessagingNotification.cancelNotification(getContext(), 239);
            FloatMmsRequsetReceiver.cancelNewMessageNotification(getContext());
            FloatMmsRequsetReceiver.stopPopupMsgAcitvity(getContext());
            setNotificationCleared(true);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MmsConfig.checkSimpleUi();
    }

    public void setFirstStartTab(int tab) {
        this.mFirstLauchTab = tab;
        viewLog("CSP first launch tab is " + tab);
    }

    public int getFirstStartTab() {
        return this.mFirstLauchTab;
    }

    public static synchronized void setFragmentActived(boolean act) {
        synchronized (CspFragment.class) {
            sFragActived = act;
        }
    }

    public static synchronized boolean isFragmentActived() {
        boolean z;
        synchronized (CspFragment.class) {
            z = sFragActived;
        }
        return z;
    }

    public static void setNotificationCleared(boolean notificationCleared) {
        sNotificationCleared = notificationCleared;
    }

    public static boolean getNotificationCleared() {
        return sNotificationCleared;
    }

    protected boolean checkNewIntent() {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }
        Intent intent = activity.getIntent();
        if (!intent.getBooleanExtra("new_intent", true)) {
            return false;
        }
        intent.putExtra("new_intent", false);
        return true;
    }

    public static synchronized void setContactChangedWhenPause(boolean changed) {
        synchronized (CspFragment.class) {
            sContactChangedWhenPause = changed;
        }
    }
}
