package com.huawei.systemmanager.preventmode.util;

import android.app.INotificationManager.Stub;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.service.notification.ZenModeConfig;
import com.huawei.systemmanager.util.HwLog;

public class PreventDataHelper {
    private static String TAG = "PreventDataHelper";
    private Context mContext = null;
    private IPreventDataChange mDataChangeCallback = null;
    private Handler mHandler = null;
    private SettingsObserver mSettingsObserver = null;

    private final class SettingsObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_URI = Global.getUriFor("zen_mode");

        public SettingsObserver() {
            super(PreventDataHelper.this.mHandler);
        }

        public void register() {
            PreventDataHelper.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_URI, false, this, 0);
            PreventDataHelper.this.mContext.getContentResolver().registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this, 0);
        }

        public void unregister() {
            PreventDataHelper.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ZEN_MODE_URI.equals(uri)) {
                PreventDataHelper.this.mDataChangeCallback.onZenModeChange();
            } else if (this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                PreventDataHelper.this.mDataChangeCallback.onZenModeConfigChange();
            }
        }
    }

    public PreventDataHelper(Context context) {
        this.mContext = context;
    }

    public synchronized void registDataChangeObserver(IPreventDataChange observer) {
        this.mDataChangeCallback = observer;
        if (this.mSettingsObserver == null) {
            initDataObserver();
        }
    }

    public synchronized void unregistDataChangeObserver() {
        if (this.mSettingsObserver != null) {
            this.mSettingsObserver.unregister();
        }
    }

    private void initDataObserver() {
        this.mHandler = new Handler();
        this.mSettingsObserver = new SettingsObserver();
        this.mSettingsObserver.register();
    }

    public static int getCurrentZenMode(Context context) {
        try {
            return Global.getInt(context.getContentResolver(), "zen_mode");
        } catch (SettingNotFoundException e) {
            HwLog.w(TAG, "Get current zen mode fail.");
            return 0;
        }
    }

    public static ZenModeConfig getCurrentZenModeConfig() {
        try {
            return Stub.asInterface(ServiceManager.getService("notification")).getZenModeConfig();
        } catch (Exception e) {
            HwLog.w(TAG, "Error calling NoMan", e);
            return new ZenModeConfig();
        }
    }

    public static void updateNotification(Context context) {
    }

    public static void updateVisibility(Context context) {
        updateNotification(context);
    }
}
