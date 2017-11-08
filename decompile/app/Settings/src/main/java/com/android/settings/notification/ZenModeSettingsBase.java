package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import com.android.settings.RestrictedSettingsFragment;
import java.util.Map.Entry;
import java.util.Set;

public abstract class ZenModeSettingsBase extends RestrictedSettingsFragment {
    protected static final boolean DEBUG = Log.isLoggable("ZenModeSettings", 3);
    protected Context mContext;
    private final Handler mHandler = new Handler();
    protected Set<Entry<String, AutomaticZenRule>> mRules;
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    protected int mZenMode;

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI;
        private final Uri ZEN_MODE_LAST_CHOOSE_URI;
        private final Uri ZEN_MODE_URI;

        private SettingsObserver() {
            super(ZenModeSettingsBase.this.mHandler);
            this.ZEN_MODE_URI = Global.getUriFor("zen_mode");
            this.ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
            this.ZEN_MODE_LAST_CHOOSE_URI = Global.getUriFor("zen_mode_last_choosen");
        }

        public void register() {
            ZenModeSettingsBase.this.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this);
            ZenModeSettingsBase.this.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this);
            ZenModeSettingsBase.this.getContentResolver().registerContentObserver(this.ZEN_MODE_LAST_CHOOSE_URI, false, this);
        }

        public void unregister() {
            ZenModeSettingsBase.this.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ZEN_MODE_URI.equals(uri)) {
                ZenModeSettingsBase.this.updateZenMode(true);
            }
            if (this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                ZenModeSettingsBase.this.maybeRefreshRules(true, true);
            }
            if (this.ZEN_MODE_LAST_CHOOSE_URI.equals(uri)) {
                ZenModeSettingsBase.this.maybeRefreshRules(true, true);
            }
        }
    }

    protected abstract void onZenModeChanged();

    protected abstract void onZenModeConfigChanged();

    public ZenModeSettingsBase() {
        super("no_adjust_volume");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        updateZenMode(false);
        maybeRefreshRules(true, false);
        if (DEBUG) {
            Log.d("ZenModeSettings", "Loaded mRules=" + this.mRules);
        }
    }

    public void onResume() {
        super.onResume();
        updateZenMode(true);
        maybeRefreshRules(true, true);
        this.mSettingsObserver.register();
        if (isUiRestricted()) {
            if (isUiRestrictedByOnlyAdmin()) {
                getPreferenceScreen().removeAll();
                return;
            }
            finish();
        }
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.unregister();
    }

    protected ZenModeConfig getZenModeConfig() {
        return NotificationManager.from(this.mContext).getZenModeConfig();
    }

    private void updateZenMode(boolean fireChanged) {
        int zenMode = Global.getInt(getContentResolver(), "zen_mode", this.mZenMode);
        if (getZenModeConfig() != null && zenMode != this.mZenMode) {
            this.mZenMode = zenMode;
            if (DEBUG) {
                Log.d("ZenModeSettings", "updateZenMode mZenMode=" + this.mZenMode);
            }
            if (fireChanged) {
                onZenModeChanged();
            }
        }
    }

    protected String addZenRule(AutomaticZenRule rule) {
        boolean z = true;
        try {
            String id = NotificationManager.from(this.mContext).addAutomaticZenRule(rule);
            if (NotificationManager.from(this.mContext).getAutomaticZenRule(id) == null) {
                z = false;
            }
            maybeRefreshRules(z, true);
            return id;
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean setZenRule(String id, AutomaticZenRule rule) {
        boolean success = false;
        try {
            success = NotificationManager.from(this.mContext).updateAutomaticZenRule(id, rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        maybeRefreshRules(success, true);
        return success;
    }

    protected boolean removeZenRule(String id) {
        boolean success = NotificationManager.from(this.mContext).removeAutomaticZenRule(id);
        maybeRefreshRules(success, true);
        return success;
    }

    protected void maybeRefreshRules(boolean success, boolean fireChanged) {
        if (success) {
            this.mRules = getZenModeRules();
            if (DEBUG) {
                Log.d("ZenModeSettings", "Refreshed mRules=" + this.mRules);
            }
            if (fireChanged) {
                onZenModeConfigChanged();
            }
        }
    }

    protected void setZenMode(int zenMode, Uri conditionId) {
        NotificationManager.from(this.mContext).setZenMode(zenMode, conditionId, "ZenModeSettings");
    }

    private Set<Entry<String, AutomaticZenRule>> getZenModeRules() {
        return NotificationManager.from(this.mContext).getAutomaticZenRules().entrySet();
    }
}
