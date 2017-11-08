package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.IConditionListener.Stub;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.statusbar.policy.ZenModeController.Callback;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class ZenModeControllerImpl implements ZenModeController {
    private static final boolean DEBUG = Log.isLoggable("ZenModeController", 3);
    private final AlarmManager mAlarmManager;
    private final ArrayList<Callback> mCallbacks = new ArrayList();
    private final LinkedHashMap<Uri, Condition> mConditions = new LinkedHashMap();
    private ZenModeConfig mConfig;
    private final GlobalSetting mConfigSetting;
    private final Context mContext;
    private final IConditionListener mListener = new Stub() {
        public void onConditionsReceived(Condition[] conditions) {
            if (ZenModeControllerImpl.DEBUG) {
                Slog.d("ZenModeController", "onConditionsReceived " + (conditions == null ? 0 : conditions.length) + " mRequesting=" + ZenModeControllerImpl.this.mRequesting);
            }
            if (ZenModeControllerImpl.this.mRequesting) {
                ZenModeControllerImpl.this.updateConditions(conditions);
            }
        }
    };
    private final GlobalSetting mModeSetting;
    private final NotificationManager mNoMan;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(intent.getAction())) {
                ZenModeControllerImpl.this.fireNextAlarmChanged();
            }
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(intent.getAction())) {
                ZenModeControllerImpl.this.fireEffectsSuppressorChanged();
            }
        }
    };
    private boolean mRegistered;
    private boolean mRequesting;
    private final SetupObserver mSetupObserver;
    private int mUserId;
    private final UserManager mUserManager;

    private final class SetupObserver extends ContentObserver {
        private boolean mRegistered;
        private final ContentResolver mResolver;

        public SetupObserver(Handler handler) {
            super(handler);
            this.mResolver = ZenModeControllerImpl.this.mContext.getContentResolver();
        }

        public boolean isUserSetup() {
            return Secure.getIntForUser(this.mResolver, "user_setup_complete", 0, ZenModeControllerImpl.this.mUserId) != 0;
        }

        public boolean isDeviceProvisioned() {
            return Global.getInt(this.mResolver, "device_provisioned", 0) != 0;
        }

        public void register() {
            if (this.mRegistered) {
                this.mResolver.unregisterContentObserver(this);
            }
            this.mResolver.registerContentObserver(Global.getUriFor("device_provisioned"), false, this);
            this.mResolver.registerContentObserver(Secure.getUriFor("user_setup_complete"), false, this, ZenModeControllerImpl.this.mUserId);
            ZenModeControllerImpl.this.fireZenAvailableChanged(ZenModeControllerImpl.this.isZenAvailable());
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (Global.getUriFor("device_provisioned").equals(uri) || Secure.getUriFor("user_setup_complete").equals(uri)) {
                ZenModeControllerImpl.this.fireZenAvailableChanged(ZenModeControllerImpl.this.isZenAvailable());
            }
        }
    }

    public ZenModeControllerImpl(Context context, Handler handler) {
        this.mContext = context;
        this.mModeSetting = new GlobalSetting(this.mContext, handler, "zen_mode") {
            protected void handleValueChanged(int value) {
                ZenModeControllerImpl.this.fireZenChanged(value);
            }
        };
        this.mConfigSetting = new GlobalSetting(this.mContext, handler, "zen_mode_config_etag") {
            protected void handleValueChanged(int value) {
                ZenModeControllerImpl.this.updateZenModeConfig();
            }
        };
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mConfig = this.mNoMan.getZenModeConfig();
        this.mModeSetting.setListening(true);
        this.mConfigSetting.setListening(true);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mSetupObserver = new SetupObserver(handler);
        this.mSetupObserver.register();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    public boolean isVolumeRestricted() {
        return this.mUserManager.hasUserRestriction("no_adjust_volume", new UserHandle(this.mUserId));
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public int getZen() {
        return this.mModeSetting.getValue();
    }

    public void setZen(int zen, Uri conditionId, String reason) {
        this.mNoMan.setZenMode(zen, conditionId, reason);
    }

    public boolean isZenAvailable() {
        return this.mSetupObserver.isDeviceProvisioned() ? this.mSetupObserver.isUserSetup() : false;
    }

    public ZenRule getManualRule() {
        return this.mConfig == null ? null : this.mConfig.manualRule;
    }

    public ZenModeConfig getConfig() {
        return this.mConfig;
    }

    public long getNextAlarm() {
        AlarmClockInfo info = this.mAlarmManager.getNextAlarmClock(this.mUserId);
        return info != null ? info.getTriggerTime() : 0;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        IntentFilter filter = new IntentFilter("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        filter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        this.mContext.registerReceiverAsUser(this.mReceiver, new UserHandle(this.mUserId), filter, null, null);
        this.mRegistered = true;
        this.mSetupObserver.register();
    }

    public boolean isCountdownConditionSupported() {
        return NotificationManager.from(this.mContext).isSystemConditionProviderEnabled("countdown");
    }

    public int getCurrentUser() {
        return UserSwitchUtils.getCurrentUser();
    }

    private void fireNextAlarmChanged() {
        for (Callback cb : this.mCallbacks) {
            cb.onNextAlarmChanged();
        }
    }

    private void fireEffectsSuppressorChanged() {
        for (Callback cb : this.mCallbacks) {
            cb.onEffectsSupressorChanged();
        }
    }

    private void fireZenChanged(int zen) {
        for (Callback cb : this.mCallbacks) {
            cb.onZenChanged(zen);
        }
    }

    private void fireZenAvailableChanged(boolean available) {
        for (Callback cb : this.mCallbacks) {
            cb.onZenAvailableChanged(available);
        }
    }

    private void fireConditionsChanged(Condition[] conditions) {
        for (Callback cb : this.mCallbacks) {
            cb.onConditionsChanged(conditions);
        }
    }

    private void fireManualRuleChanged(ZenRule rule) {
        for (Callback cb : this.mCallbacks) {
            cb.onManualRuleChanged(rule);
        }
    }

    private void fireConfigChanged(ZenModeConfig config) {
        for (Callback cb : this.mCallbacks) {
            cb.onConfigChanged(config);
        }
    }

    private void updateConditions(Condition[] conditions) {
        if (conditions != null && conditions.length != 0) {
            for (Condition c : conditions) {
                if ((c.flags & 1) != 0) {
                    this.mConditions.put(c.id, c);
                }
            }
            fireConditionsChanged((Condition[]) this.mConditions.values().toArray(new Condition[this.mConditions.values().size()]));
        }
    }

    private void updateZenModeConfig() {
        ZenRule newRule = null;
        ZenModeConfig config = this.mNoMan.getZenModeConfig();
        if (!Objects.equals(config, this.mConfig)) {
            Object obj = this.mConfig != null ? this.mConfig.manualRule : null;
            this.mConfig = config;
            fireConfigChanged(config);
            if (config != null) {
                newRule = config.manualRule;
            }
            if (!Objects.equals(obj, newRule)) {
                fireManualRuleChanged(newRule);
            }
        }
    }
}
