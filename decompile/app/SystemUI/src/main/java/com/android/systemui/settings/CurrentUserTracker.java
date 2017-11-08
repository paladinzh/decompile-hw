package com.android.systemui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.utils.UserSwitchUtils;

public abstract class CurrentUserTracker extends BroadcastReceiver {
    private Context mContext;
    private int mCurrentUserId;

    public abstract void onUserSwitched(int i);

    public CurrentUserTracker(Context context) {
        this.mContext = context;
    }

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
            int oldUserId = this.mCurrentUserId;
            this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
            if (oldUserId != this.mCurrentUserId) {
                onUserSwitched(this.mCurrentUserId);
            }
        }
    }

    public void startTracking() {
        this.mCurrentUserId = UserSwitchUtils.getCurrentUser();
        this.mContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_SWITCHED"));
    }

    public void stopTracking() {
        try {
            this.mContext.unregisterReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
