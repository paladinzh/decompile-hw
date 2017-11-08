package com.android.settings.location;

import android.R;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.android.settings.DimmableIconPreference;
import com.android.settings.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

class SettingsInjector {
    private final Context mContext;
    private final Handler mHandler = new StatusLoadingHandler();
    private final Set<Setting> mSettings = new HashSet();

    private class ServiceSettingClickedListener implements OnPreferenceClickListener {
        private InjectedSetting mInfo;

        public ServiceSettingClickedListener(InjectedSetting info) {
            this.mInfo = info;
        }

        public boolean onPreferenceClick(Preference preference) {
            Intent settingIntent = new Intent();
            settingIntent.setClassName(this.mInfo.packageName, this.mInfo.settingsActivity);
            settingIntent.setFlags(268468224);
            SettingsInjector.this.mContext.startActivityAsUser(settingIntent, this.mInfo.mUserHandle);
            return true;
        }
    }

    private final class Setting {
        public final Preference preference;
        public final InjectedSetting setting;
        public long startMillis;

        private Setting(InjectedSetting setting, Preference preference) {
            this.setting = setting;
            this.preference = preference;
        }

        public String toString() {
            return "Setting{setting=" + this.setting + ", preference=" + this.preference + '}';
        }

        public boolean equals(Object o) {
            if (this != o) {
                return o instanceof Setting ? this.setting.equals(((Setting) o).setting) : false;
            } else {
                return true;
            }
        }

        public int hashCode() {
            return this.setting.hashCode();
        }

        public void startService() {
            if (((ActivityManager) SettingsInjector.this.mContext.getSystemService("activity")).isUserRunning(this.setting.mUserHandle.getIdentifier())) {
                Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        boolean enabled = bundle.getBoolean("enabled", true);
                        if (Log.isLoggable("SettingsInjector", 3)) {
                            Log.d("SettingsInjector", Setting.this.setting + ": received " + msg + ", bundle: " + bundle);
                        }
                        Setting.this.preference.setSummary(null);
                        Setting.this.preference.setEnabled(enabled);
                        SettingsInjector.this.mHandler.sendMessage(SettingsInjector.this.mHandler.obtainMessage(2, Setting.this));
                    }
                };
                Messenger messenger = new Messenger(handler);
                Intent intent = this.setting.getServiceIntent();
                intent.putExtra("messenger", messenger);
                if (Log.isLoggable("SettingsInjector", 3)) {
                    Log.d("SettingsInjector", this.setting + ": sending update intent: " + intent + ", handler: " + handler);
                    this.startMillis = SystemClock.elapsedRealtime();
                } else {
                    this.startMillis = 0;
                }
                SettingsInjector.this.mContext.startServiceAsUser(intent, this.setting.mUserHandle);
                return;
            }
            if (Log.isLoggable("SettingsInjector", 2)) {
                Log.v("SettingsInjector", "Cannot start service as user " + this.setting.mUserHandle.getIdentifier() + " is not running");
            }
        }

        public long getElapsedTime() {
            return SystemClock.elapsedRealtime() - this.startMillis;
        }

        public void maybeLogElapsedTime() {
            if (Log.isLoggable("SettingsInjector", 3) && this.startMillis != 0) {
                Log.d("SettingsInjector", this + " update took " + getElapsedTime() + " millis");
            }
        }
    }

    private final class StatusLoadingHandler extends Handler {
        private boolean mReloadRequested;
        private Set<Setting> mSettingsBeingLoaded;
        private Set<Setting> mSettingsToLoad;
        private Set<Setting> mTimedOutSettings;

        private StatusLoadingHandler() {
            this.mSettingsToLoad = new HashSet();
            this.mSettingsBeingLoaded = new HashSet();
            this.mTimedOutSettings = new HashSet();
        }

        public void handleMessage(Message msg) {
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", "handleMessage start: " + msg + ", " + this);
            }
            switch (msg.what) {
                case 1:
                    this.mReloadRequested = true;
                    break;
                case 2:
                    Setting receivedSetting = msg.obj;
                    receivedSetting.maybeLogElapsedTime();
                    this.mSettingsBeingLoaded.remove(receivedSetting);
                    this.mTimedOutSettings.remove(receivedSetting);
                    removeMessages(3, receivedSetting);
                    break;
                case 3:
                    Setting timedOutSetting = msg.obj;
                    this.mSettingsBeingLoaded.remove(timedOutSetting);
                    this.mTimedOutSettings.add(timedOutSetting);
                    if (Log.isLoggable("SettingsInjector", 5)) {
                        Log.w("SettingsInjector", "Timed out after " + timedOutSetting.getElapsedTime() + " millis trying to get status for: " + timedOutSetting);
                        break;
                    }
                    break;
                default:
                    Log.wtf("SettingsInjector", "Unexpected what: " + msg);
                    break;
            }
            if (this.mSettingsBeingLoaded.size() > 0 || this.mTimedOutSettings.size() > 1) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "too many services already live for " + msg + ", " + this);
                }
                return;
            }
            if (this.mReloadRequested && this.mSettingsToLoad.isEmpty() && this.mSettingsBeingLoaded.isEmpty() && this.mTimedOutSettings.isEmpty()) {
                if (Log.isLoggable("SettingsInjector", 2)) {
                    Log.v("SettingsInjector", "reloading because idle and reload requesteed " + msg + ", " + this);
                }
                this.mSettingsToLoad.addAll(SettingsInjector.this.mSettings);
                this.mReloadRequested = false;
            }
            Iterator<Setting> iter = this.mSettingsToLoad.iterator();
            if (iter.hasNext()) {
                Setting setting = (Setting) iter.next();
                iter.remove();
                setting.startService();
                this.mSettingsBeingLoaded.add(setting);
                sendMessageDelayed(obtainMessage(3, setting), 1000);
                if (Log.isLoggable("SettingsInjector", 3)) {
                    Log.d("SettingsInjector", "handleMessage end " + msg + ", " + this + ", started loading " + setting);
                }
                return;
            }
            if (Log.isLoggable("SettingsInjector", 2)) {
                Log.v("SettingsInjector", "nothing left to do for " + msg + ", " + this);
            }
        }

        public String toString() {
            return "StatusLoadingHandler{mSettingsToLoad=" + this.mSettingsToLoad + ", mSettingsBeingLoaded=" + this.mSettingsBeingLoaded + ", mTimedOutSettings=" + this.mTimedOutSettings + ", mReloadRequested=" + this.mReloadRequested + '}';
        }
    }

    public SettingsInjector(Context context) {
        this.mContext = context;
    }

    private List<InjectedSetting> getSettings(UserHandle userHandle) {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent("android.location.SettingInjectorService");
        int profileId = userHandle.getIdentifier();
        List<ResolveInfo> resolveInfos = pm.queryIntentServicesAsUser(intent, 128, profileId);
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "Found services for profile id " + profileId + ": " + resolveInfos);
        }
        List<InjectedSetting> settings = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            try {
                if (!Utils.isChinaArea() || !resolveInfo.toString().contains("com.google.android.gms")) {
                    InjectedSetting setting = parseServiceInfo(resolveInfo, userHandle, pm);
                    if (setting == null) {
                        Log.w("SettingsInjector", "Unable to load service info " + resolveInfo);
                    } else {
                        settings.add(setting);
                    }
                }
            } catch (XmlPullParserException e) {
                Log.w("SettingsInjector", "Unable to load service info " + resolveInfo, e);
            } catch (IOException e2) {
                Log.w("SettingsInjector", "Unable to load service info " + resolveInfo, e2);
            }
        }
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "Loaded settings for profile id " + profileId + ": " + settings);
        }
        return settings;
    }

    private static InjectedSetting parseServiceInfo(ResolveInfo service, UserHandle userHandle, PackageManager pm) throws XmlPullParserException, IOException {
        ServiceInfo si = service.serviceInfo;
        if ((si.applicationInfo.flags & 1) == 0 && Log.isLoggable("SettingsInjector", 5)) {
            Log.w("SettingsInjector", "Ignoring attempt to inject setting from app not in system image: " + service);
            return null;
        }
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = si.loadXmlMetaData(pm, "android.location.SettingInjectorService");
            if (xmlResourceParser == null) {
                throw new XmlPullParserException("No android.location.SettingInjectorService meta-data for " + service + ": " + si);
            }
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if ("injected-location-setting".equals(xmlResourceParser.getName())) {
                InjectedSetting parseAttributes = parseAttributes(si.packageName, si.name, userHandle, pm.getResourcesForApplicationAsUser(si.packageName, userHandle.getIdentifier()), attrs);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return parseAttributes;
            }
            throw new XmlPullParserException("Meta-data does not start with injected-location-setting tag");
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException("Unable to load resources for package " + si.packageName);
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private static InjectedSetting parseAttributes(String packageName, String className, UserHandle userHandle, Resources res, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.SettingInjectorService);
        try {
            String title = sa.getString(1);
            int iconId = sa.getResourceId(0, 0);
            String settingsActivity = sa.getString(2);
            if (Log.isLoggable("SettingsInjector", 3)) {
                Log.d("SettingsInjector", "parsed title: " + title + ", iconId: " + iconId + ", settingsActivity: " + settingsActivity);
            }
            InjectedSetting newInstance = InjectedSetting.newInstance(packageName, className, title, iconId, userHandle, settingsActivity);
            return newInstance;
        } finally {
            sa.recycle();
        }
    }

    public List<Preference> getInjectedSettings(int profileId) {
        List<UserHandle> profiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        ArrayList<Preference> prefs = new ArrayList();
        int profileCount = profiles.size();
        for (int i = 0; i < profileCount; i++) {
            UserHandle userHandle = (UserHandle) profiles.get(i);
            if (profileId == -2 || profileId == userHandle.getIdentifier()) {
                for (InjectedSetting setting : getSettings(userHandle)) {
                    this.mSettings.add(new Setting(setting, addServiceSetting(prefs, setting)));
                }
            }
        }
        reloadStatusMessages();
        return prefs;
    }

    public void reloadStatusMessages() {
        if (Log.isLoggable("SettingsInjector", 3)) {
            Log.d("SettingsInjector", "reloadingStatusMessages: " + this.mSettings);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
    }

    private Preference addServiceSetting(List<Preference> prefs, InjectedSetting info) {
        PackageManager pm = this.mContext.getPackageManager();
        Drawable icon = pm.getUserBadgedIcon(pm.getDrawable(info.packageName, info.iconId, null), info.mUserHandle);
        CharSequence badgedAppLabel = pm.getUserBadgedLabel(info.title, info.mUserHandle);
        if (info.title.contentEquals(badgedAppLabel)) {
            badgedAppLabel = null;
        }
        Preference pref = new DimmableIconPreference(this.mContext, badgedAppLabel);
        pref.setLayoutResource(2130969013);
        pref.setWidgetLayoutResource(2130968998);
        pref.setTitle(info.title);
        pref.setSummary(null);
        pref.setIcon(icon);
        pref.setOnPreferenceClickListener(new ServiceSettingClickedListener(info));
        prefs.add(pref);
        return pref;
    }
}
