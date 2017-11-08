package com.android.settings.notification;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo.DisplayNameComparator;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArraySet;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZenAccessSettings extends EmptyTextSettings {
    private Context mContext;
    private NotificationManager mNoMan;
    private final SettingObserver mObserver = new SettingObserver();
    private PackageManager mPkgMan;

    public static class FriendlyWarningDialogFragment extends DialogFragment {
        public FriendlyWarningDialogFragment setPkgInfo(String pkg, CharSequence label) {
            Bundle args = new Bundle();
            args.putString("p", pkg);
            String str = "l";
            if (!TextUtils.isEmpty(label)) {
                pkg = label.toString();
            }
            args.putString(str, pkg);
            setArguments(args);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            final String pkg = args.getString("p");
            String label = args.getString("l");
            return new Builder(getContext()).setMessage(getResources().getString(2131627033)).setTitle(getResources().getString(2131627032, new Object[]{label})).setCancelable(true).setPositiveButton(2131624573, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ZenAccessSettings.deleteRules(FriendlyWarningDialogFragment.this.getContext(), pkg);
                    ZenAccessSettings.setAccess(FriendlyWarningDialogFragment.this.getContext(), pkg, false);
                }
            }).setNegativeButton(2131624572, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    public static class ScaryWarningDialogFragment extends DialogFragment {
        public ScaryWarningDialogFragment setPkgInfo(String pkg, CharSequence label) {
            Bundle args = new Bundle();
            args.putString("p", pkg);
            String str = "l";
            if (!TextUtils.isEmpty(label)) {
                pkg = label.toString();
            }
            args.putString(str, pkg);
            setArguments(args);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            final String pkg = args.getString("p");
            String label = args.getString("l");
            return new Builder(getContext()).setMessage(getResources().getString(2131627030)).setTitle(getResources().getString(2131627029, new Object[]{label})).setCancelable(true).setPositiveButton(2131624351, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ZenAccessSettings.setAccess(ScaryWarningDialogFragment.this.getContext(), pkg, true);
                }
            }).setNegativeButton(2131624352, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    private final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void onChange(boolean selfChange, Uri uri) {
            ZenAccessSettings.this.reloadList();
        }
    }

    protected int getMetricsCategory() {
        return 180;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPkgMan = this.mContext.getPackageManager();
        this.mNoMan = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this.mContext));
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(2131626767);
    }

    public void onResume() {
        super.onResume();
        reloadList();
        getContentResolver().registerContentObserver(Secure.getUriFor("enabled_notification_policy_access_packages"), false, this.mObserver);
        getContentResolver().registerContentObserver(Secure.getUriFor("enabled_notification_listeners"), false, this.mObserver);
    }

    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(this.mObserver);
    }

    private boolean hasManagerNotificationPermission(String pkg) {
        try {
            return ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void reloadList() {
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        ArrayList<ApplicationInfo> apps = new ArrayList();
        ArraySet<String> requesting = this.mNoMan.getPackagesRequestingNotificationPolicyAccess();
        if (!requesting.isEmpty()) {
            List<ApplicationInfo> installed = this.mPkgMan.getInstalledApplications(0);
            if (installed != null) {
                for (ApplicationInfo app : installed) {
                    if (requesting.contains(app.packageName)) {
                        apps.add(app);
                    }
                }
            }
        }
        ArraySet<String> autoApproved = getEnabledNotificationListeners();
        requesting.addAll(autoApproved);
        Collections.sort(apps, new DisplayNameComparator(this.mPkgMan));
        for (ApplicationInfo app2 : apps) {
            final String pkg = app2.packageName;
            final CharSequence label = app2.loadLabel(this.mPkgMan);
            SwitchPreference pref = new SwitchPreference(getPrefContext());
            pref.setPersistent(false);
            pref.setIcon(app2.loadIcon(this.mPkgMan));
            pref.setTitle(label);
            pref.setChecked(hasAccess(pkg));
            if (autoApproved.contains(pkg) || hasManagerNotificationPermission(pkg)) {
                pref.setEnabled(false);
                pref.setSummary(getString(2131627031));
            }
            pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (((Boolean) newValue).booleanValue()) {
                        new ScaryWarningDialogFragment().setPkgInfo(pkg, label).show(ZenAccessSettings.this.getFragmentManager(), "dialog");
                    } else {
                        new FriendlyWarningDialogFragment().setPkgInfo(pkg, label).show(ZenAccessSettings.this.getFragmentManager(), "dialog");
                    }
                    return false;
                }
            });
            screen.addPreference(pref);
        }
    }

    private ArraySet<String> getEnabledNotificationListeners() {
        ArraySet<String> packages = new ArraySet();
        String settingValue = Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(settingValue)) {
            String[] restored = settingValue.split(":");
            for (String unflattenFromString : restored) {
                ComponentName value = ComponentName.unflattenFromString(unflattenFromString);
                if (value != null) {
                    packages.add(value.getPackageName());
                }
            }
        }
        return packages;
    }

    private boolean hasAccess(String pkg) {
        return this.mNoMan.isNotificationPolicyAccessGrantedForPackage(pkg);
    }

    private static void setAccess(final Context context, final String pkg, final boolean access) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                ((NotificationManager) context.getSystemService(NotificationManager.class)).setNotificationPolicyAccessGranted(pkg, access);
            }
        });
    }

    private static void deleteRules(final Context context, final String pkg) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                ((NotificationManager) context.getSystemService(NotificationManager.class)).removeAutomaticZenRules(pkg);
            }
        });
    }
}
