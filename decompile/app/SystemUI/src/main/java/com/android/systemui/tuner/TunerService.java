package com.android.systemui.tuner;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.HashMap;
import java.util.Set;

public class TunerService extends SystemUI {
    private static TunerService sInstance;
    private ContentResolver mContentResolver;
    private int mCurrentUser;
    private final ArrayMap<Uri, String> mListeningUris = new ArrayMap();
    private final Observer mObserver = new Observer();
    private final HashMap<String, Set<Tunable>> mTunableLookup = new HashMap();
    private CurrentUserTracker mUserTracker;

    public interface Tunable {
        void onTuningChanged(String str, String str2);
    }

    public static class ClearReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (!"com.android.systemui.action.CLEAR_TUNER".equals(intent.getAction())) {
            }
        }
    }

    private class Observer extends ContentObserver {
        public Observer() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (uri != null && userId == UserSwitchUtils.getCurrentUser()) {
                TunerService.this.reloadSetting(uri);
            }
        }
    }

    public void start() {
        this.mContentResolver = this.mContext.getContentResolver();
        for (UserInfo user : UserManager.get(this.mContext).getUsers()) {
            this.mCurrentUser = user.getUserHandle().getIdentifier();
            if (getValue("sysui_tuner_version", 0) != 1) {
                upgradeTuner(getValue("sysui_tuner_version", 0), 1);
            }
        }
        putComponent(TunerService.class, this);
        this.mCurrentUser = UserSwitchUtils.getCurrentUser();
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int newUserId) {
                TunerService.this.mCurrentUser = newUserId;
                TunerService.this.reloadAll();
                TunerService.this.reregisterAll();
            }
        };
        this.mUserTracker.startTracking();
    }

    private void upgradeTuner(int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            String blacklistStr = getValue("icon_blacklist");
            if (blacklistStr != null) {
                ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(blacklistStr);
                iconBlacklist.add("rotate");
                iconBlacklist.add("headset");
                Secure.putStringForUser(this.mContentResolver, "icon_blacklist", TextUtils.join(",", iconBlacklist), this.mCurrentUser);
            }
        }
        setValue("sysui_tuner_version", newVersion);
    }

    public String getValue(String setting) {
        return Secure.getStringForUser(this.mContentResolver, setting, this.mCurrentUser);
    }

    public void setValue(String setting, String value) {
        Secure.putStringForUser(this.mContentResolver, setting, value, this.mCurrentUser);
    }

    public int getValue(String setting, int def) {
        return Secure.getIntForUser(this.mContentResolver, setting, def, this.mCurrentUser);
    }

    public void setValue(String setting, int value) {
        Secure.putIntForUser(this.mContentResolver, setting, value, this.mCurrentUser);
    }

    public void addTunable(Tunable tunable, String... keys) {
        for (String key : keys) {
            addTunable(tunable, key);
        }
    }

    private void addTunable(Tunable tunable, String key) {
        if (!this.mTunableLookup.containsKey(key)) {
            this.mTunableLookup.put(key, new ArraySet());
        }
        ((Set) this.mTunableLookup.get(key)).add(tunable);
        Uri uri = Secure.getUriFor(key);
        if (!this.mListeningUris.containsKey(uri)) {
            this.mListeningUris.put(uri, key);
            this.mContentResolver.registerContentObserver(uri, false, this.mObserver, this.mCurrentUser);
        }
        tunable.onTuningChanged(key, Secure.getStringForUser(this.mContentResolver, key, this.mCurrentUser));
    }

    public void removeTunable(Tunable tunable) {
        for (Set<Tunable> list : this.mTunableLookup.values()) {
            list.remove(tunable);
        }
    }

    protected void reregisterAll() {
        if (this.mListeningUris.size() != 0) {
            this.mContentResolver.unregisterContentObserver(this.mObserver);
            for (Uri uri : this.mListeningUris.keySet()) {
                this.mContentResolver.registerContentObserver(uri, false, this.mObserver, this.mCurrentUser);
            }
        }
    }

    public void reloadSetting(Uri uri) {
        String key = (String) this.mListeningUris.get(uri);
        Set<Tunable> tunables = (Set) this.mTunableLookup.get(key);
        if (tunables != null) {
            String value = Secure.getStringForUser(this.mContentResolver, key, this.mCurrentUser);
            for (Tunable tunable : tunables) {
                tunable.onTuningChanged(key, value);
            }
        }
    }

    private void reloadAll() {
        for (String key : this.mTunableLookup.keySet()) {
            String value = Secure.getStringForUser(this.mContentResolver, key, this.mCurrentUser);
            for (Tunable tunable : (Set) this.mTunableLookup.get(key)) {
                tunable.onTuningChanged(key, value);
            }
        }
    }

    public static TunerService get(Context context) {
        TunerService tunerService = null;
        if (context.getApplicationContext() instanceof SystemUIApplication) {
            tunerService = (TunerService) ((SystemUIApplication) context.getApplicationContext()).getComponent(TunerService.class);
        }
        if (tunerService == null) {
            return getStaticService(context);
        }
        return tunerService;
    }

    private static TunerService getStaticService(Context context) {
        if (sInstance == null) {
            sInstance = new TunerService();
            sInstance.mContext = context.getApplicationContext();
            sInstance.mComponents = new HashMap();
            sInstance.start();
        }
        return sInstance;
    }

    public static final void showResetRequest(final Context context, final Runnable onDisabled) {
        SystemUIDialog dialog = new SystemUIDialog(context);
        dialog.setShowForAllUsers(true);
        dialog.setMessage(R.string.remove_from_settings_prompt);
        dialog.setButton(-2, context.getString(R.string.cancel), (OnClickListener) null);
        dialog.setButton(-1, context.getString(R.string.guest_exit_guest_dialog_remove), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                context.sendBroadcast(new Intent("com.android.systemui.action.CLEAR_TUNER"));
                TunerService.setTunerEnabled(context, false);
                Secure.putInt(context.getContentResolver(), "seen_tuner_warning", 0);
                if (onDisabled != null) {
                    onDisabled.run();
                }
            }
        });
        dialog.show();
    }

    public static final void setTunerEnabled(Context context, boolean enabled) {
        int i;
        PackageManager packageManager = userContext(context).getPackageManager();
        ComponentName componentName = new ComponentName(context, TunerActivity.class);
        if (enabled) {
            i = 1;
        } else {
            i = 2;
        }
        packageManager.setComponentEnabledSetting(componentName, i, 1);
    }

    public static final boolean isTunerEnabled(Context context) {
        return userContext(context).getPackageManager().getComponentEnabledSetting(new ComponentName(context, TunerActivity.class)) == 1;
    }

    private static Context userContext(Context context) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, new UserHandle(UserSwitchUtils.getCurrentUser()));
        } catch (NameNotFoundException e) {
            return context;
        }
    }
}
