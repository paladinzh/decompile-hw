package com.android.settings;

import android.app.LauncherActivity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.android.settings.Settings.TetherSettingsActivity;
import com.android.settings.Settings.VpnSettingsActivity;
import com.android.settings.Settings.ZenModeAutomationSettingsActivity;
import com.android.settings.Settings.ZenModePrioritySettingsActivity;
import com.android.settingslib.TetherUtil;
import java.util.ArrayList;
import java.util.List;

public class CreateShortcut extends LauncherActivity {
    protected Intent getTargetIntent() {
        Intent targetIntent = new Intent("android.intent.action.MAIN", null);
        targetIntent.addCategory("com.android.settings.SHORTCUT");
        targetIntent.addFlags(268435456);
        return targetIntent;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent shortcutIntent = intentForPosition(position);
        if (shortcutIntent != null) {
            shortcutIntent.setFlags(2097152);
        }
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(this, 2130903040));
        intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
        intent.putExtra("android.intent.extra.shortcut.NAME", itemForPosition(position).label);
        setResult(-1, intent);
        finish();
    }

    protected boolean onEvaluateShowIcons() {
        return false;
    }

    private boolean shouoldAddToShortcutList(ResolveInfo info) {
        String activityName = info.activityInfo.name;
        if (TextUtils.isEmpty(activityName)) {
            return false;
        }
        if ((activityName.endsWith(DualCardSettings.class.getSimpleName()) || activityName.endsWith(VpnSettingsActivity.class.getSimpleName()) || activityName.endsWith("CallForwardSettingWidget") || activityName.endsWith(ZenModeAutomationSettingsActivity.class.getSimpleName()) || activityName.endsWith(ZenModePrioritySettingsActivity.class.getSimpleName())) && !Utils.isOwnerUser()) {
            Log.d("CreateShortcut", "shouoldAddToShortcutList()-->exclude activityName = " + activityName);
            return false;
        } else if (activityName.endsWith(TetherSettingsActivity.class.getSimpleName()) && (!TetherUtil.isTetheringSupported(this) || !Utils.isOwnerUser() || Utils.isWifiOnly(this))) {
            Log.d("CreateShortcut", "shouoldAddToShortcutList()-->exclude activityName = " + activityName);
            return false;
        } else if (!activityName.endsWith("CallForwardSettingWidget") || (!Utils.isWifiOnly(this) && Utils.isVoiceCapable(this))) {
            return true;
        } else {
            return false;
        }
    }

    protected List<ResolveInfo> onQueryPackageManager(Intent queryIntent) {
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(queryIntent, 128);
        List<ResolveInfo> activitiesToReturn = new ArrayList();
        if (activities == null) {
            return null;
        }
        for (int i = activities.size() - 1; i >= 0; i--) {
            if (shouoldAddToShortcutList((ResolveInfo) activities.get(i))) {
                activitiesToReturn.add((ResolveInfo) activities.get(i));
            }
        }
        return SettingsExtUtils.filterDualCardSettings(activitiesToReturn);
    }
}
