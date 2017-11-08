package com.android.systemui.statusbar.policy;

import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;

public interface ZenModeController {

    public static class Callback {
        public void onZenChanged(int zen) {
        }

        public void onConditionsChanged(Condition[] conditions) {
        }

        public void onNextAlarmChanged() {
        }

        public void onZenAvailableChanged(boolean available) {
        }

        public void onEffectsSupressorChanged() {
        }

        public void onManualRuleChanged(ZenRule rule) {
        }

        public void onConfigChanged(ZenModeConfig config) {
        }
    }

    void addCallback(Callback callback);

    ZenModeConfig getConfig();

    int getCurrentUser();

    ZenRule getManualRule();

    long getNextAlarm();

    int getZen();

    boolean isCountdownConditionSupported();

    boolean isVolumeRestricted();

    void removeCallback(Callback callback);

    void setUserId(int i);

    void setZen(int i, Uri uri, String str);
}
