package com.android.settings;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;
import com.android.settings.wifi.WifiSettings;
import com.huawei.android.quickaction.ActionIcon;
import com.huawei.android.quickaction.QuickAction;
import com.huawei.android.quickaction.QuickActionService;
import java.util.ArrayList;
import java.util.List;

public class SettingsQuickActionService extends QuickActionService {
    public List<QuickAction> onGetQuickActions(ComponentName targetActivityName) {
        Log.d("SettingsQuickActionService", "SettingsQuickActionService-->onGetQuickActions()-->targetActivityName = " + targetActivityName);
        ComponentName display = new ComponentName(this, DisplaySettings.class);
        ComponentName sound = new ComponentName(this, SoundSettings.class);
        ComponentName wifi = new ComponentName(this, WifiSettings.class);
        ComponentName sysUpdate = new ComponentName("com.huawei.android.hwouc", "com.huawei.android.hwouc.ui.activities.MainEntranceActivity");
        QuickAction soundAction = new QuickAction(getResources().getString(2131625101), ActionIcon.createWithResource((Context) this, 2130838371), sound, getIntentSender(sound));
        QuickAction displayAction = new QuickAction(getResources().getString(2131625100), ActionIcon.createWithResource((Context) this, 2130838370), display, getIntentSender(display));
        QuickAction wifiAction = new QuickAction(getResources().getString(2131624904), ActionIcon.createWithResource((Context) this, 2130838373), wifi, getIntentSender(wifi));
        QuickAction sysUpdateAction = new QuickAction(getResources().getString(2131627577), ActionIcon.createWithResource((Context) this, 2130838372), sysUpdate, getIntentSender(sysUpdate));
        ArrayList<QuickAction> actions = new ArrayList();
        actions.add(wifiAction);
        actions.add(displayAction);
        actions.add(soundAction);
        actions.add(sysUpdateAction);
        return actions;
    }

    private IntentSender getIntentSender(ComponentName componentName) {
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(8421376);
        return PendingIntent.getActivity(this, 0, intent, 0).getIntentSender();
    }
}
