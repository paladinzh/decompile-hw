package com.android.settings.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.settings.bluetooth.Utils;
import com.android.settings.wifi.WifiExtUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class SettingsAppWidgetProvider extends AppWidgetProvider {
    static final ComponentName THIS_APPWIDGET = new ComponentName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    private static final StateTracker sBluetoothState = new BluetoothStateTracker();
    private static LocalBluetoothAdapter sLocalBluetoothAdapter = null;
    private static final StateTracker sLocationState = new LocationStateTracker();
    private static SettingsObserver sSettingsObserver;
    private static final StateTracker sSyncState = new SyncStateTracker();
    private static final StateTracker sWifiState = new WifiStateTracker();

    private static abstract class StateTracker {
        private Boolean mActualState;
        private boolean mDeferredStateChangeRequestNeeded;
        private boolean mInTransition;
        private Boolean mIntendedState;

        public abstract int getActualState(Context context);

        public abstract int getButtonDescription();

        public abstract int getButtonId();

        public abstract int getButtonImageId(boolean z);

        public abstract int getContainerId();

        public abstract int getIndicatorId();

        public abstract void onActualStateChange(Context context, Intent intent);

        protected abstract void requestStateChange(Context context, boolean z);

        private StateTracker() {
            this.mInTransition = false;
            this.mActualState = null;
            this.mIntendedState = null;
            this.mDeferredStateChangeRequestNeeded = false;
        }

        public final void toggleState(Context context) {
            boolean newState = false;
            switch (getTriState(context)) {
                case 0:
                    newState = true;
                    break;
                case 1:
                    newState = false;
                    break;
                case 5:
                    if (this.mIntendedState != null) {
                        if (!this.mIntendedState.booleanValue()) {
                            newState = true;
                            break;
                        } else {
                            newState = false;
                            break;
                        }
                    }
                    break;
            }
            this.mIntendedState = Boolean.valueOf(newState);
            if (this.mInTransition) {
                this.mDeferredStateChangeRequestNeeded = true;
                return;
            }
            this.mInTransition = true;
            requestStateChange(context, newState);
        }

        public void setImageViewResources(Context context, RemoteViews views) {
            int containerId = getContainerId();
            int buttonId = getButtonId();
            int indicatorId = getIndicatorId();
            boolean bOnFlg = true;
            switch (getTriState(context)) {
                case 0:
                    views.setContentDescription(containerId, getContentDescription(context, 2131626104));
                    views.setImageViewResource(buttonId, getButtonImageId(false));
                    bOnFlg = false;
                    break;
                case 1:
                    views.setContentDescription(containerId, getContentDescription(context, 2131626102));
                    views.setImageViewResource(buttonId, getButtonImageId(true));
                    bOnFlg = true;
                    break;
                case 5:
                    if (!isTurningOn()) {
                        views.setContentDescription(containerId, getContentDescription(context, 2131626106));
                        views.setImageViewResource(buttonId, getButtonImageId(false));
                        bOnFlg = false;
                        break;
                    }
                    views.setContentDescription(containerId, getContentDescription(context, 2131626105));
                    views.setImageViewResource(buttonId, getButtonImageId(true));
                    bOnFlg = true;
                    break;
            }
            SettingsAppWidgetProvider.setTxtColor(context, views, bOnFlg, indicatorId);
        }

        protected final String getContentDescription(Context context, int stateResId) {
            String gadget = context.getString(getButtonDescription());
            String state = context.getString(stateResId);
            return context.getString(2131626101, new Object[]{gadget, state});
        }

        protected final void setCurrentState(Context context, int newState) {
            boolean wasInTransition = this.mInTransition;
            switch (newState) {
                case 0:
                    this.mInTransition = false;
                    this.mActualState = Boolean.valueOf(false);
                    break;
                case 1:
                    this.mInTransition = false;
                    this.mActualState = Boolean.valueOf(true);
                    break;
                case 2:
                    this.mInTransition = true;
                    this.mActualState = Boolean.valueOf(false);
                    break;
                case 3:
                    this.mInTransition = true;
                    this.mActualState = Boolean.valueOf(true);
                    break;
            }
            if (wasInTransition && !this.mInTransition && this.mDeferredStateChangeRequestNeeded) {
                if ((this.mActualState == null || this.mIntendedState == null || !this.mIntendedState.equals(this.mActualState)) && this.mIntendedState != null) {
                    this.mInTransition = true;
                    requestStateChange(context, this.mIntendedState.booleanValue());
                }
                this.mDeferredStateChangeRequestNeeded = false;
            }
        }

        public final boolean isTurningOn() {
            return this.mIntendedState != null ? this.mIntendedState.booleanValue() : false;
        }

        public final int getTriState(Context context) {
            if (this.mInTransition) {
                return 5;
            }
            switch (getActualState(context)) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                default:
                    return 5;
            }
        }
    }

    private static final class BluetoothStateTracker extends StateTracker {
        private BluetoothStateTracker() {
            super();
        }

        public int getContainerId() {
            return 2131887430;
        }

        public int getButtonId() {
            return 2131887431;
        }

        public int getIndicatorId() {
            return 2131887432;
        }

        public int getButtonDescription() {
            return 2131626108;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return 2130838297;
            }
            return 2130838296;
        }

        public int getActualState(Context context) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                LocalBluetoothManager manager = Utils.getLocalBtManager(context);
                if (manager == null) {
                    return 4;
                }
                SettingsAppWidgetProvider.sLocalBluetoothAdapter = manager.getBluetoothAdapter();
                if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                    return 4;
                }
            }
            return bluetoothStateToFiveState(SettingsAppWidgetProvider.sLocalBluetoothAdapter.getBluetoothState());
        }

        protected void requestStateChange(Context context, final boolean desiredState) {
            if (SettingsAppWidgetProvider.sLocalBluetoothAdapter == null) {
                Log.d("SettingsAppWidgetProvider", "No LocalBluetoothManager");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... args) {
                        SettingsAppWidgetProvider.sLocalBluetoothAdapter.setBluetoothEnabled(desiredState);
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        public void onActualStateChange(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                setCurrentState(context, bluetoothStateToFiveState(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1)));
            }
        }

        private static int bluetoothStateToFiveState(int bluetoothState) {
            switch (bluetoothState) {
                case 10:
                    return 0;
                case 11:
                    return 2;
                case 12:
                    return 1;
                case 13:
                    return 3;
                default:
                    return 4;
            }
        }
    }

    private static final class LocationStateTracker extends StateTracker {
        private int mCurrentLocationMode;

        private LocationStateTracker() {
            super();
            this.mCurrentLocationMode = 0;
        }

        public int getContainerId() {
            return 2131887434;
        }

        public int getButtonId() {
            return 2131887435;
        }

        public int getIndicatorId() {
            return 2131887436;
        }

        public int getButtonDescription() {
            return 2131626109;
        }

        public int getButtonImageId(boolean on) {
            if (!on) {
                return 2130838320;
            }
            switch (this.mCurrentLocationMode) {
                case 1:
                case 2:
                case 3:
                    return 2130838319;
                default:
                    return 2130838320;
            }
        }

        public int getActualState(Context context) {
            this.mCurrentLocationMode = Secure.getInt(context.getContentResolver(), "location_mode", 0);
            if (this.mCurrentLocationMode == 0) {
                return 0;
            }
            return 1;
        }

        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        public void requestStateChange(final Context context, boolean desiredState) {
            final ContentResolver resolver = context.getContentResolver();
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... args) {
                    boolean z = true;
                    if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_share_location")) {
                        if (LocationStateTracker.this.getActualState(context) != 1) {
                            z = false;
                        }
                        return Boolean.valueOf(z);
                    }
                    int mode = 3;
                    switch (Secure.getInt(resolver, "location_mode", 0)) {
                        case 0:
                            mode = -1;
                            break;
                        case 1:
                            mode = 0;
                            break;
                        case 2:
                            mode = 0;
                            break;
                        case 3:
                            mode = 0;
                            break;
                    }
                    Secure.putInt(resolver, "location_mode", mode);
                    if (mode == 0) {
                        z = false;
                    }
                    return Boolean.valueOf(z);
                }

                protected void onPostExecute(Boolean result) {
                    LocationStateTracker.this.setCurrentState(context, result.booleanValue() ? 1 : 0);
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }

        public void setImageViewResources(Context context, RemoteViews views) {
            int containerId = getContainerId();
            int buttonId = getButtonId();
            int indicatorId = getIndicatorId();
            boolean bOnFlg = true;
            switch (getTriState(context)) {
                case 0:
                    views.setContentDescription(containerId, getContentDescription(context, 2131626104));
                    views.setImageViewResource(buttonId, getButtonImageId(false));
                    bOnFlg = false;
                    break;
                case 1:
                    views.setContentDescription(containerId, getContentDescription(context, 2131626102));
                    views.setImageViewResource(buttonId, getButtonImageId(true));
                    bOnFlg = true;
                    break;
                case 5:
                    if (!isTurningOn()) {
                        views.setContentDescription(containerId, getContentDescription(context, 2131626106));
                        views.setImageViewResource(buttonId, getButtonImageId(false));
                        bOnFlg = false;
                        break;
                    }
                    views.setContentDescription(containerId, getContentDescription(context, 2131626105));
                    views.setImageViewResource(buttonId, getButtonImageId(true));
                    bOnFlg = true;
                    break;
            }
            SettingsAppWidgetProvider.setTxtColor(context, views, bOnFlg, indicatorId);
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private Context mContext;

        SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        void startObserving() {
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor("screen_brightness"), false, this);
            resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), false, this);
            resolver.registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), false, this);
        }

        void stopObserving() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            SettingsAppWidgetProvider.updateWidget(this.mContext);
        }
    }

    private static final class SyncStateTracker extends StateTracker {
        private SyncStateTracker() {
            super();
        }

        public int getContainerId() {
            return 2131887438;
        }

        public int getButtonId() {
            return 2131887439;
        }

        public int getIndicatorId() {
            return 2131887440;
        }

        public int getButtonDescription() {
            return 2131626110;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return 2130838333;
            }
            return 2130838334;
        }

        public int getActualState(Context context) {
            return ContentResolver.getMasterSyncAutomatically() ? 1 : 0;
        }

        public void onActualStateChange(Context context, Intent unused) {
            setCurrentState(context, getActualState(context));
        }

        public void requestStateChange(final Context context, final boolean desiredState) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService("connectivity");
            new AsyncTask<Void, Void, Boolean>() {
                protected Boolean doInBackground(Void... args) {
                    boolean sync = ContentResolver.getMasterSyncAutomatically();
                    if (desiredState) {
                        if (!sync) {
                            ContentResolver.setMasterSyncAutomatically(true);
                        }
                        return Boolean.valueOf(true);
                    }
                    if (sync) {
                        ContentResolver.setMasterSyncAutomatically(false);
                    }
                    return Boolean.valueOf(false);
                }

                protected void onPostExecute(Boolean result) {
                    SyncStateTracker.this.setCurrentState(context, result.booleanValue() ? 1 : 0);
                    SettingsAppWidgetProvider.updateWidget(context);
                }
            }.execute(new Void[0]);
        }
    }

    private static final class WifiStateTracker extends StateTracker {
        private WifiStateTracker() {
            super();
        }

        public int getContainerId() {
            return 2131887426;
        }

        public int getButtonId() {
            return 2131887427;
        }

        public int getIndicatorId() {
            return 2131887428;
        }

        public int getButtonDescription() {
            return 2131626107;
        }

        public int getButtonImageId(boolean on) {
            if (on) {
                return 2130838299;
            }
            return 2130838298;
        }

        public int getActualState(Context context) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
            if (wifiManager != null) {
                return wifiStateToFiveState(wifiManager.getWifiState());
            }
            return 4;
        }

        protected void requestStateChange(final Context context, final boolean desiredState) {
            Context finalContext = context;
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
            if (wifiManager == null) {
                Log.d("SettingsAppWidgetProvider", "No wifiManager.");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... args) {
                        int wifiApState = wifiManager.getWifiApState();
                        if (desiredState && (wifiApState == 12 || wifiApState == 13)) {
                            WifiExtUtils.setWifiApEnabled(null, wifiManager, null, false);
                        }
                        if (!wifiManager.setWifiEnabled(desiredState)) {
                            WifiStateTracker.this.setCurrentState(context, WifiStateTracker.this.getActualState(context));
                        }
                        return null;
                    }
                }.execute(new Void[0]);
            }
        }

        public void onActualStateChange(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                setCurrentState(context, wifiStateToFiveState(intent.getIntExtra("wifi_state", -1)));
            }
        }

        private static int wifiStateToFiveState(int wifiState) {
            switch (wifiState) {
                case 0:
                    return 3;
                case 1:
                    return 0;
                case 2:
                    return 2;
                case 3:
                    return 1;
                default:
                    return 4;
            }
        }
    }

    private static void checkObserver(Context context) {
        if (sSettingsObserver == null) {
            sSettingsObserver = new SettingsObserver(new Handler(), context.getApplicationContext());
            sSettingsObserver.startObserving();
        }
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews view = buildUpdate(context);
        for (int updateAppWidget : appWidgetIds) {
            appWidgetManager.updateAppWidget(updateAppWidget, view);
        }
    }

    public void onEnabled(Context context) {
        super.onEnabled(context);
        checkObserver(context);
    }

    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (sSettingsObserver != null) {
            sSettingsObserver.stopObserving();
            sSettingsObserver = null;
        }
    }

    static RemoteViews buildUpdate(Context context) {
        RemoteViews views = WidgetExtUtils.getView(context);
        views.setOnClickPendingIntent(2131887426, getLaunchPendingIntent(context, 0));
        views.setOnClickPendingIntent(2131887442, getLaunchPendingIntent(context, 1));
        views.setOnClickPendingIntent(2131887438, getLaunchPendingIntent(context, 2));
        views.setOnClickPendingIntent(2131887434, getLaunchPendingIntent(context, 3));
        views.setOnClickPendingIntent(2131887430, getLaunchPendingIntent(context, 4));
        updateButtons(views, context);
        return views;
    }

    public static void updateWidget(Context context) {
        RemoteViews views = buildUpdate(context);
        try {
            AppWidgetManager.getInstance(context).updateAppWidget(THIS_APPWIDGET, views);
        } catch (Exception ex) {
            Log.d("SettingsAppWidgetProvider", "updateAppWidget error: " + ex);
        }
        checkObserver(context);
    }

    private static void updateButtons(RemoteViews views, Context context) {
        sWifiState.setImageViewResources(context, views);
        sBluetoothState.setImageViewResources(context, views);
        sLocationState.setImageViewResources(context, views);
        sSyncState.setImageViewResources(context, views);
        if (getBrightnessMode(context)) {
            views.setContentDescription(2131887442, context.getString(2131626111, new Object[]{context.getString(2131626112)}));
            views.setImageViewResource(2131887443, 2130838352);
            return;
        }
        boolean bOnFlg;
        int brightness = getBrightness(context);
        PowerManager pm = (PowerManager) context.getSystemService("power");
        int half = (int) (((float) pm.getMaximumScreenBrightnessSetting()) * 0.12f);
        if (brightness > ((int) (((float) pm.getMaximumScreenBrightnessSetting()) * 0.8f))) {
            views.setContentDescription(2131887442, context.getString(2131626111, new Object[]{context.getString(2131626113)}));
            views.setImageViewResource(2131887443, 2130838354);
            bOnFlg = true;
        } else if (brightness > half) {
            views.setContentDescription(2131887442, context.getString(2131626111, new Object[]{context.getString(2131626114)}));
            views.setImageViewResource(2131887443, 2130838357);
            bOnFlg = true;
        } else {
            views.setContentDescription(2131887442, context.getString(2131626111, new Object[]{context.getString(2131626115)}));
            views.setImageViewResource(2131887443, 2130838355);
            bOnFlg = false;
        }
        setTxtColor(context, views, bOnFlg, 2131887444);
    }

    private static PendingIntent getLaunchPendingIntent(Context context, int buttonId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, SettingsAppWidgetProvider.class);
        launchIntent.addCategory("android.intent.category.ALTERNATIVE");
        launchIntent.setData(Uri.parse("custom:" + buttonId));
        return PendingIntent.getBroadcast(context, 0, launchIntent, 0);
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        String action_sync_conn_status_changed = ContentResolver.ACTION_SYNC_CONN_STATUS_CHANGED.getAction();
        if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
            sWifiState.onActualStateChange(context, intent);
        } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            sBluetoothState.onActualStateChange(context, intent);
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            sLocationState.onActualStateChange(context, intent);
        } else if (action_sync_conn_status_changed != null && action_sync_conn_status_changed.equals(action)) {
            sSyncState.onActualStateChange(context, intent);
        } else if (intent.hasCategory("android.intent.category.ALTERNATIVE")) {
            int buttonId = Integer.parseInt(intent.getData().getSchemeSpecificPart());
            if (buttonId == 0) {
                sWifiState.toggleState(context);
            } else if (buttonId == 1) {
                toggleBrightness(context);
            } else if (buttonId == 2) {
                sSyncState.toggleState(context);
            } else if (buttonId == 3) {
                sLocationState.toggleState(context);
            } else if (buttonId == 4) {
                sBluetoothState.toggleState(context);
            }
        } else {
            return;
        }
        updateWidget(context);
    }

    private static int getBrightness(Context context) {
        try {
            return System.getInt(context.getContentResolver(), "screen_brightness");
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean getBrightnessMode(Context context) {
        boolean z = true;
        try {
            if (System.getInt(context.getContentResolver(), "screen_brightness_mode") != 1) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            Log.d("SettingsAppWidgetProvider", "getBrightnessMode: " + e);
            return false;
        }
    }

    private void toggleBrightness(Context context) {
        try {
            IPowerManager power = Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                PowerManager pm = (PowerManager) context.getSystemService("power");
                ContentResolver cr = context.getContentResolver();
                int brightness = System.getInt(cr, "screen_brightness");
                int brightnessMode = 0;
                if (context.getResources().getBoolean(17956900)) {
                    brightnessMode = System.getInt(cr, "screen_brightness_mode");
                }
                if (brightnessMode == 1) {
                    brightness = pm.getMinimumScreenBrightnessSetting();
                    brightnessMode = 0;
                } else if (brightness < pm.getDefaultScreenBrightnessSetting()) {
                    brightness = pm.getDefaultScreenBrightnessSetting();
                } else if (brightness < pm.getMaximumScreenBrightnessSetting()) {
                    brightness = pm.getMaximumScreenBrightnessSetting();
                } else {
                    if (com.android.settings.Utils.isSupportAutoBrightness(context)) {
                        brightnessMode = 1;
                    }
                    brightness = pm.getMinimumScreenBrightnessSetting();
                }
                if (context.getResources().getBoolean(17956900)) {
                    System.putInt(context.getContentResolver(), "screen_brightness_mode", brightnessMode);
                } else {
                    brightnessMode = 0;
                }
                if (brightnessMode == 0) {
                    power.setTemporaryScreenBrightnessSettingOverride(brightness);
                    System.putInt(cr, "screen_brightness", brightness);
                }
            }
        } catch (RemoteException e) {
            Log.d("SettingsAppWidgetProvider", "toggleBrightness: " + e);
        } catch (SettingNotFoundException e2) {
            Log.d("SettingsAppWidgetProvider", "toggleBrightness: " + e2);
        }
    }

    private static int getColorId(Context context, boolean on) {
        if (context == null) {
            return 0;
        }
        int colId;
        if (on) {
            colId = context.getResources().getIdentifier("accent_emui", "color", "androidhwext");
        } else {
            colId = context.getResources().getIdentifier("emui_secondary_text_dark", "color", "androidhwext");
        }
        return context.getResources().getColor(colId);
    }

    private static void setTxtColor(Context context, RemoteViews views, boolean bOnFlg, int indicatorId) {
        if (context != null && views != null) {
            int nColorId = getColorId(context, bOnFlg);
            if (nColorId != 0) {
                views.setTextColor(indicatorId, nColorId);
            }
        }
    }
}
