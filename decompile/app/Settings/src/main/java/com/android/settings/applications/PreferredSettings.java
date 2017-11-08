package com.android.settings.applications;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import com.android.internal.util.ArrayUtils;
import com.android.settings.LauncherModeSimpleActivity;
import com.android.settings.MLog;
import com.android.settings.PrivacyModeManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.PreferredSettingsUtils.PreferredApplication;
import java.util.ArrayList;
import java.util.List;

public class PreferredSettings extends SettingsPreferenceFragment implements OnClickListener, OnDismissListener {
    private static final String[] BLACK_LIST = new String[]{"com.huawei.kidsmode", "com.huawei.android.dsdscardmanager", "com.huawei.wildkids", "com.huawei.vdrive"};
    private boolean mBuildNeeded = true;
    AppPreference mCurrentAppPreference = null;
    private boolean mIsUserConfirmed;
    PackageManager mPm;
    PreferenceGroup mPrefGroup;
    private View.OnClickListener mPreferredAppClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            AppPreference pref = (AppPreference) PreferredSettings.this.mPrefs.get(((Integer) v.getTag()).intValue());
            if (!pref.isChecked) {
                String systemPreferredPackageName = Global.getString(PreferredSettings.this.getContentResolver(), PreferredSettings.this.mPreferredApplication.name());
                if (TextUtils.isEmpty(systemPreferredPackageName) || systemPreferredPackageName.equals(pref.activityName.getPackageName())) {
                    PreferredSettings.this.changePreferredApplication(pref);
                    return;
                }
                PreferredSettings.this.buildWarningDialog();
                PreferredSettings.this.mTempAppPreference = pref;
            }
        }
    };
    protected String mPreferredAppLabel;
    protected String mPreferredAppPackageName;
    protected PreferredApplication mPreferredApplication;
    ComponentName[] mPreferredComponentSet;
    protected Intent mPreferredIntent;
    protected IntentFilter mPreferredIntentFilter;
    protected int mPreferredMatch = 0;
    ArrayList<AppPreference> mPrefs;
    AppPreference mSystemAppPreference = null;
    @SuppressLint({"HwViewID"})
    AppPreference mTempAppPreference = null;
    AlertDialog mWarningDialog;

    class AppPreference extends Preference {
        ComponentName activityName;
        int index;
        boolean isChecked;
        boolean isSystem;
        RadioButton mRadio;

        public AppPreference(Context context, ComponentName activity, int i, Drawable icon, CharSequence title, ActivityInfo info) {
            super(context);
            setLayoutResource(2130968966);
            setIcon(icon);
            setTitle(title);
            this.activityName = activity;
            this.index = i;
            this.isSystem = PreferredSettingsUtils.isSystemAndUnRemovable(info.applicationInfo);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            this.mRadio = (RadioButton) view.findViewById(2131886165);
            this.mRadio.setChecked(this.isChecked);
            View v = view.findViewById(2131886935);
            v.setOnClickListener(PreferredSettings.this.mPreferredAppClickListener);
            v.setTag(Integer.valueOf(this.index));
        }

        private void setChecked(boolean state) {
            if (state != this.isChecked) {
                this.isChecked = state;
                if (this.mRadio != null) {
                    this.mRadio.setChecked(this.isChecked);
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void buildWarningDialog() {
        if (this.mWarningDialog == null) {
            boolean isMmsApplication = PreferredSettingsUtils.isMmsApplication(this.mPreferredIntentFilter);
            Builder builder = new Builder(getActivity());
            builder.setTitle(2131627350);
            if (isMmsApplication) {
                builder.setMessage(getString(2131628632, new Object[]{this.mPreferredAppLabel}));
            } else {
                builder.setMessage(getString(2131627591, new Object[]{this.mPreferredAppLabel}));
            }
            builder.setPositiveButton(2131627589, this);
            builder.setNegativeButton(2131627590, this);
            this.mWarningDialog = builder.show();
            this.mWarningDialog.setOnDismissListener(this);
        }
    }

    private void changePreferredApplication(String packageName, String className) {
        AppPreference pref = findPreference(packageName, className);
        if (pref != null) {
            changePreferredApplication(pref);
        }
    }

    private void changePreferredApplication(AppPreference newApp) {
        if (this.mCurrentAppPreference != null) {
            this.mCurrentAppPreference.setChecked(false);
        }
        String oldPackageName = null;
        if (this.mCurrentAppPreference != null) {
            oldPackageName = this.mCurrentAppPreference.activityName.getPackageName();
        }
        PreferredSettingsUtils.changePreferredApplication(this.mPm, oldPackageName, newApp.activityName, getActivity(), this.mPreferredMatch, this.mPreferredIntentFilter, this.mPreferredComponentSet);
        newApp.setChecked(true);
        this.mCurrentAppPreference = newApp;
    }

    private List<ResolveInfo> getIntentActivities() {
        List<ResolveInfo> list;
        if (PreferredSettingsUtils.isMusicApplication(this.mPreferredIntentFilter)) {
            Intent musicIntent = new Intent(this.mPreferredIntent);
            musicIntent.setDataAndType(musicIntent.getData(), "audio/mpeg");
            list = this.mPm.queryIntentActivities(musicIntent, 0);
        } else {
            list = this.mPm.queryIntentActivities(this.mPreferredIntent, 0);
        }
        this.mPreferredComponentSet = new ComponentName[list.size()];
        int i = 0;
        int j = 0;
        while (i < list.size()) {
            ResolveInfo ri = (ResolveInfo) list.get(i);
            this.mPreferredComponentSet[j] = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            if (ri.priority < 0) {
                list.remove(i);
                i--;
            } else if (PreferredSettingsUtils.isMmsApplication(this.mPreferredIntentFilter) && PreferredSettingsUtils.isIllegalMMSApplication(getActivity(), ri.activityInfo.packageName)) {
                list.remove(i);
                i--;
            } else if (ArrayUtils.contains(BLACK_LIST, ri.activityInfo.packageName)) {
                list.remove(i);
                i--;
            }
            i++;
            j++;
        }
        for (i = 0; i < list.size(); i++) {
            ri = (ResolveInfo) list.get(i);
            if (PreferredSettingsUtils.isSystemAndUnRemovable(ri.activityInfo.applicationInfo)) {
                if (i != 0) {
                    ResolveInfo tmpInfo = (ResolveInfo) list.get(0);
                    list.set(0, ri);
                    list.set(i, tmpInfo);
                }
                this.mPreferredAppPackageName = PreferredSettingsUtils.getPreferredPackageName(this.mPm, list, this.mPreferredIntentFilter, this.mPreferredIntent, getActivity());
                return list;
            }
        }
        this.mPreferredAppPackageName = PreferredSettingsUtils.getPreferredPackageName(this.mPm, list, this.mPreferredIntentFilter, this.mPreferredIntent, getActivity());
        return list;
    }

    private void buildPreferredActivitiesList() {
        List<ResolveInfo> preferredActivities = getIntentActivities();
        Context context = getActivity();
        this.mCurrentAppPreference = null;
        this.mPrefGroup.removeAll();
        this.mPrefs = new ArrayList();
        int prefIndex = 0;
        for (int i = 0; i < preferredActivities.size(); i++) {
            ResolveInfo candidate = (ResolveInfo) preferredActivities.get(i);
            ActivityInfo info = candidate.activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            try {
                AppPreference pref = new AppPreference(context, activityName, prefIndex, info.loadIcon(this.mPm), PreferredSettingsUtils.getApplicationlabel(this.mPm, this.mPreferredIntentFilter, info.packageName, info.loadLabel(this.mPm).toString()), info);
                this.mPrefs.add(pref);
                this.mPrefGroup.addPreference(pref);
                pref.setEnabled(true);
                if ((this.mPreferredAppPackageName == null && pref.isSystem) || info.packageName.equals(this.mPreferredAppPackageName)) {
                    this.mCurrentAppPreference = pref;
                }
                String systemPreferredPackageName = Global.getString(getContentResolver(), this.mPreferredApplication.name());
                if (!TextUtils.isEmpty(systemPreferredPackageName) && systemPreferredPackageName.equals(pref.activityName.getPackageName())) {
                    this.mSystemAppPreference = pref;
                }
                if (candidate.match > this.mPreferredMatch) {
                    this.mPreferredMatch = candidate.match;
                }
                prefIndex++;
            } catch (Throwable e) {
                MLog.e("PreferredSettings", "Problem dealing with activity " + activityName, e);
            }
        }
        if (this.mCurrentAppPreference != null) {
            new Handler().post(new Runnable() {
                public void run() {
                    PreferredSettings.this.mCurrentAppPreference.setChecked(true);
                }
            });
        }
    }

    private AppPreference findPreference(String packageName, String className) {
        ComponentName c = new ComponentName(packageName, className);
        for (AppPreference pref : this.mPrefs) {
            if (c.equals(pref.activityName)) {
                return pref;
            }
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230836);
        Bundle args = getArguments();
        if (args != null && args.containsKey("preferred_app_intent_filter") && args.containsKey("preferred_app_intent") && args.containsKey("preferred_app_package_name") && args.containsKey("preferred_app_type") && args.containsKey("preferred_app_label")) {
            this.mPreferredIntentFilter = (IntentFilter) args.get("preferred_app_intent_filter");
            this.mPreferredIntent = (Intent) args.get("preferred_app_intent");
            this.mPreferredAppPackageName = (String) args.get("preferred_app_package_name");
            this.mPreferredApplication = (PreferredApplication) args.get("preferred_app_type");
            this.mPreferredAppLabel = (String) args.get("preferred_app_label");
            getActivity().setTitle(this.mPreferredAppLabel);
            this.mPm = getPackageManager();
            this.mPrefGroup = (PreferenceGroup) findPreference("app_list");
            if (savedInstanceState == null) {
                this.mBuildNeeded = true;
                return;
            }
            buildPreferredActivitiesList();
            this.mBuildNeeded = false;
            String pkgName = savedInstanceState.getString("preferred_app_package_name");
            String clsName = savedInstanceState.getString("preferred_app_class_name");
            boolean isConfirmed = savedInstanceState.getBoolean("is_user_confirmed", false);
            if (!(pkgName == null || clsName == null)) {
                if (isConfirmed && PreferredSettingsUtils.isHomeApplication(this.mPreferredIntentFilter)) {
                    changePreferredApplication(pkgName, clsName);
                } else {
                    this.mTempAppPreference = findPreference(pkgName, clsName);
                    buildWarningDialog();
                }
            }
            return;
        }
        MLog.e("PreferredSettings", "We don't have arguments to build Preference list, just quit.");
        this.mBuildNeeded = false;
        getActivity().finish();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(this.mPreferredAppLabel);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume() {
        super.onResume();
        PrivacyModeManager pmm = new PrivacyModeManager(getActivity());
        if (PreferredSettingsUtils.isMmsApplication(this.mPreferredIntentFilter) && PrivacyModeManager.isFeatrueSupported() && pmm.isGuestModeOn()) {
            finish();
            return;
        }
        if (this.mBuildNeeded) {
            buildPreferredActivitiesList();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWarningDialog != null && this.mWarningDialog.isShowing()) {
            this.mWarningDialog.dismiss();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog != this.mWarningDialog) {
            return;
        }
        if (which == -1) {
            this.mIsUserConfirmed = true;
            if (this.mTempAppPreference == null) {
                return;
            }
            if (PreferredSettingsUtils.isHomeApplication(this.mPreferredIntentFilter) && Utils.isSimpleModeOn()) {
                LauncherModeSimpleActivity.exitSimpleUiMode(getActivity(), false);
                return;
            }
            changePreferredApplication(this.mTempAppPreference);
            this.mTempAppPreference = null;
        } else if (which == -2) {
            if (!(this.mSystemAppPreference == null || this.mSystemAppPreference == this.mCurrentAppPreference)) {
                changePreferredApplication(this.mSystemAppPreference);
            }
            this.mWarningDialog.dismiss();
            this.mTempAppPreference = null;
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (dialog == this.mWarningDialog) {
            if (!this.mIsUserConfirmed) {
                this.mTempAppPreference = null;
            }
            this.mWarningDialog.dismiss();
            this.mWarningDialog = null;
        }
    }

    public void onSaveInstanceState(Bundle data) {
        super.onSaveInstanceState(data);
        if (this.mTempAppPreference != null && this.mTempAppPreference.activityName != null) {
            ComponentName c = this.mTempAppPreference.activityName;
            data.putString("preferred_app_package_name", c.getPackageName());
            data.putString("preferred_app_class_name", c.getClassName());
            data.putBoolean("is_user_confirmed", this.mIsUserConfirmed);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (getListView() != null) {
            setDivider(getResources().getDrawable(2130838530));
        }
        return root;
    }
}
