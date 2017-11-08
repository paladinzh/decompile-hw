package com.android.deskclock;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.android.deskclock.alarmclock.SetAlarm;
import com.android.util.Log;
import com.android.util.Utils;
import com.huawei.android.quickaction.ActionIcon;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import java.util.ArrayList;
import java.util.List;

public class DeskClockQuickActionService extends QuickActionService {
    private QuickAction createTimer() {
        int state;
        int action_type;
        int requestCode;
        Intent intent = new Intent(this, AlarmsMainActivity.class);
        intent.setComponent(new ComponentName(this, AlarmsMainActivity.class));
        intent.putExtra("deskclock.select.tab", 3);
        intent.putExtra("is_quickaction_type", true);
        int titleId = R.string.quickaction_start_timer;
        int mState = Utils.getSharedPreferences(this, "timer", 0).getInt("state", 0);
        if (mState == 1) {
            titleId = R.string.quickaction_pause_timer;
            state = 2;
            action_type = 2;
            requestCode = 32;
        } else if (mState == 2) {
            titleId = R.string.quickaction_continues_timer;
            state = 1;
            action_type = 3;
            requestCode = 33;
        } else {
            state = 1;
            action_type = 1;
            requestCode = 31;
        }
        Log.dRelease("DskActionService", "createTimer state = " + state + ", action_type = " + action_type + ", mState = " + mState);
        intent.putExtra("quickaction_type", action_type);
        intent.putExtra("quickaction_type_state", state);
        QuickActionConfig action = new QuickActionConfig(titleId, R.drawable.ic_quickaction_timer);
        return new QuickAction(getResources().getString(action.getNameId()), ActionIcon.createWithResource((Context) this, action.getIcon()), intent.getComponent(), PendingIntent.getActivity(this, requestCode, intent, 134217728).getIntentSender());
    }

    private QuickAction createStopWatchs() {
        int state;
        int action_type;
        int requestCode;
        Intent intent = new Intent(this, AlarmsMainActivity.class);
        intent.setComponent(new ComponentName(this, AlarmsMainActivity.class));
        intent.putExtra("deskclock.select.tab", 2);
        intent.putExtra("is_quickaction_type", true);
        int titleId = R.string.quickaction_start_stopwatch;
        int mState = Utils.getDefaultSharedPreferences(this).getInt("sw_state", 0);
        if (mState == 1) {
            titleId = R.string.quickaction_pause_stopwatch;
            state = 2;
            action_type = 2;
            requestCode = 22;
        } else if (mState == 2) {
            titleId = R.string.quickaction_continues_stopwatch;
            state = 1;
            action_type = 3;
            requestCode = 23;
        } else {
            state = 1;
            action_type = 1;
            requestCode = 21;
        }
        intent.putExtra("quickaction_type", action_type);
        intent.putExtra("quickaction_type_state", state);
        Log.dRelease("DskActionService", "createStopWatchs state = " + state + ", action_type = " + action_type + ", mState = " + mState);
        QuickActionConfig action = new QuickActionConfig(titleId, R.drawable.ic_quickaction_stopwatch);
        return new QuickAction(getResources().getString(action.getNameId()), ActionIcon.createWithResource((Context) this, action.getIcon()), intent.getComponent(), PendingIntent.getActivity(this, requestCode, intent, 134217728).getIntentSender());
    }

    private QuickAction createAddAlarm() {
        Intent intent = new Intent(this, SetAlarm.class);
        intent.setComponent(new ComponentName(this, SetAlarm.class));
        intent.putExtra("is_quickaction_type", true);
        QuickActionConfig action = new QuickActionConfig(R.string.quickaction_add_alarm, R.drawable.ic_quickaction_clock);
        return new QuickAction(getResources().getString(action.getNameId()), ActionIcon.createWithResource((Context) this, action.getIcon()), intent.getComponent(), PendingIntent.getActivity(this, 2, intent, 134217728).getIntentSender());
    }

    private List<QuickAction> createQuickActions() {
        ArrayList<QuickAction> actions = new ArrayList();
        actions.add(createAddAlarm());
        actions.add(createStopWatchs());
        actions.add(createTimer());
        return actions;
    }

    public List<QuickAction> onGetQuickActions(ComponentName targetActivityName) {
        return createQuickActions();
    }
}
