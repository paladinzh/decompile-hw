package com.android.mms.ui;

import android.content.Intent;
import android.os.Bundle;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;

public class NotificationList extends ConversationList {
    private boolean isHwNotification = false;
    private boolean isNotification = false;
    private int mRunningMode = -1;

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d("ConversationServ", "onCreate intance : " + this);
        super.onCreate(savedInstanceState);
        getRunningMode(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        if (getRunningMode(intent)) {
            finish();
            intent.setComponent(getComponentName());
            startActivity(intent);
            MLog.d("NotificationList", "Recrate activity for NotificationList: " + this.mRunningMode);
            overridePendingTransition(getResources().getIdentifier("androidhwext:anim/activity_open_enter", null, null), getResources().getIdentifier("androidhwext:anim/activity_open_exit", null, null));
        }
    }

    private boolean getRunningMode(Intent intent) {
        int runningMode = -1;
        if (intent != null) {
            runningMode = intent.getIntExtra("running_mode", -1);
        }
        if (runningMode == 4) {
            ResEx.self().createNotificationCache();
            this.isNotification = true;
        } else if (runningMode == 5) {
            ResEx.self().createHwNotificationCache();
            this.isHwNotification = true;
        }
        if (this.mRunningMode == runningMode || runningMode == -1) {
            return false;
        }
        this.mRunningMode = runningMode;
        return true;
    }

    public void onStop() {
        super.onStop();
        if (this.isNotification) {
            ResEx.self().clearNotificationDrawableCache();
        }
        if (this.isHwNotification) {
            ResEx.self().clearHwNotificationDrawableCache();
        }
    }
}
