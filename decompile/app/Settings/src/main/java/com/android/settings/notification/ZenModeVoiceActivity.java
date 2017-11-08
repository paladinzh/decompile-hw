package com.android.settings.notification;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.UserHandle;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.Log;
import com.android.settings.utils.VoiceSettingsActivity;
import java.util.Locale;

public class ZenModeVoiceActivity extends VoiceSettingsActivity {
    protected boolean onVoiceSettingInteraction(Intent intent) {
        if (intent.hasExtra("android.settings.extra.do_not_disturb_mode_enabled")) {
            int minutes = intent.getIntExtra("android.settings.extra.do_not_disturb_mode_minutes", -1);
            Condition condition = null;
            int mode = 0;
            if (intent.getBooleanExtra("android.settings.extra.do_not_disturb_mode_enabled", false)) {
                if (minutes > 0) {
                    condition = ZenModeConfig.toTimeCondition(this, minutes, UserHandle.myUserId());
                }
                mode = 3;
            }
            setZenModeConfig(mode, condition);
            AudioManager audioManager = (AudioManager) getSystemService("audio");
            if (audioManager != null) {
                audioManager.adjustStreamVolume(5, 0, 1);
            }
            notifySuccess(getChangeSummary(mode, minutes));
        } else {
            Log.v("ZenModeVoiceActivity", "Missing extra android.provider.Settings.EXTRA_DO_NOT_DISTURB_MODE_ENABLED");
            finish();
        }
        return false;
    }

    private void setZenModeConfig(int mode, Condition condition) {
        if (condition != null) {
            NotificationManager.from(this).setZenMode(mode, condition.id, "ZenModeVoiceActivity");
        } else {
            NotificationManager.from(this).setZenMode(mode, null, "ZenModeVoiceActivity");
        }
    }

    private CharSequence getChangeSummary(int mode, int minutes) {
        int indefinite = -1;
        int byMinute = -1;
        int byHour = -1;
        int byTime = -1;
        switch (mode) {
            case 0:
                indefinite = 2131626838;
                break;
            case 3:
                indefinite = 2131626836;
                byMinute = 2131689495;
                byHour = 2131689496;
                byTime = 2131626837;
                break;
        }
        if (minutes < 0 || mode == 0) {
            return getString(indefinite);
        }
        CharSequence formattedTime = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(this, UserHandle.myUserId()) ? "Hm" : "hma"), System.currentTimeMillis() + ((long) (60000 * minutes)));
        Resources res = getResources();
        if (minutes < 60) {
            return res.getQuantityString(byMinute, minutes, new Object[]{Integer.valueOf(minutes), formattedTime});
        } else if (minutes % 60 != 0) {
            return res.getString(byTime, new Object[]{formattedTime});
        } else {
            return res.getQuantityString(byHour, minutes / 60, new Object[]{Integer.valueOf(minutes / 60), formattedTime});
        }
    }
}
