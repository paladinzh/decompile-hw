package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminInfo.PolicyInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.icu.text.ListFormatter;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.IWebViewUpdateService.Stub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.DeviceAdminAdd;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.PermissionsSummaryHelper.PermissionsResultCallback;
import com.android.settings.datausage.DataUsageList;
import com.android.settings.datausage.DataUsageSummary;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settingslib.AppItem;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.Utils;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.cust.HwCustUtils;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class InstalledAppDetails extends AppInfoBase implements OnClickListener, OnPreferenceClickListener {
    static final String[] NOT_DISABLABLE_APP_LIST = new String[]{"com.android.providers.contacts", "com.android.contacts", "com.android.mms"};
    private ViewGroup mAdminPolicies;
    private TextView mAdminWarning;
    private final NotificationBackend mBackend = new NotificationBackend();
    private BatteryStatsHelper mBatteryHelper;
    private Preference mBatteryPreference;
    private ChartData mChartData;
    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            InstalledAppDetails installedAppDetails = InstalledAppDetails.this;
            if (getResultCode() != 0) {
                z = true;
            }
            installedAppDetails.updateForceStopButton(z);
        }
    };
    private HwCustInstalledAppDetails mCustInstalledAppDetails;
    private final LoaderCallbacks<ChartData> mDataCallbacks = new LoaderCallbacks<ChartData>() {
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(InstalledAppDetails.this.getActivity(), InstalledAppDetails.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            InstalledAppDetails.this.mChartData = data;
        }

        public void onLoaderReset(Loader<ChartData> loader) {
        }
    };
    private Preference mDataPreference;
    private DeviceAdminInfo mDeviceAdmin;
    private boolean mDisableAfterUninstall;
    private Button mForceStopButton;
    private LayoutPreference mHeader;
    private final HashSet<String> mHomePackages = new HashSet();
    private boolean mInitialized;
    private Preference mLaunchPreference;
    private Preference mMemoryPreference;
    private Preference mNotificationPreference;
    private final PermissionsResultCallback mPermissionCallback = new PermissionsResultCallback() {
        public void onPermissionSummaryResult(int standardGrantedPermissionCount, int requestedPermissionCount, int additionalGrantedPermissionCount, List<CharSequence> grantedGroupLabels) {
            if (InstalledAppDetails.this.getActivity() != null) {
                CharSequence summary;
                Resources res = InstalledAppDetails.this.getResources();
                if (requestedPermissionCount == 0) {
                    summary = res.getString(2131626914);
                    InstalledAppDetails.this.mPermissionsPreference.setOnPreferenceClickListener(null);
                    InstalledAppDetails.this.mPermissionsPreference.setEnabled(false);
                } else {
                    ArrayList<CharSequence> list = new ArrayList(grantedGroupLabels);
                    if (additionalGrantedPermissionCount > 0) {
                        list.add(res.getQuantityString(2131689499, additionalGrantedPermissionCount, new Object[]{Integer.valueOf(additionalGrantedPermissionCount)}));
                    }
                    if (list.size() == 0) {
                        summary = res.getString(2131626913);
                    } else {
                        summary = ListFormatter.getInstance().format(list);
                    }
                    InstalledAppDetails.this.mPermissionsPreference.setOnPreferenceClickListener(InstalledAppDetails.this);
                    InstalledAppDetails.this.mPermissionsPreference.setEnabled(true);
                }
                InstalledAppDetails.this.mPermissionsPreference.setSummary(summary);
            }
        }
    };
    private Preference mPermissionsPreference;
    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor();
    private boolean mShowUninstalled;
    private BatterySipper mSipper;
    protected ProcStatsPackageEntry mStats;
    protected ProcStatsData mStatsManager;
    private INetworkStatsSession mStatsSession;
    private Preference mStoragePreference;
    private Button mUninstallButton;
    private boolean mUpdatedSysApp = false;

    private class BatteryUpdater extends AsyncTask<Void, Void, Void> {
        private BatteryUpdater() {
        }

        protected Void doInBackground(Void... params) {
            InstalledAppDetails.this.mBatteryHelper.create((Bundle) null);
            InstalledAppDetails.this.mBatteryHelper.refreshStats(0, InstalledAppDetails.this.mUserManager.getUserProfiles());
            List<BatterySipper> usageList = InstalledAppDetails.this.mBatteryHelper.getUsageList();
            int N = usageList.size();
            for (int i = 0; i < N; i++) {
                BatterySipper sipper = (BatterySipper) usageList.get(i);
                if (sipper.getUid() == InstalledAppDetails.this.mPackageInfo.applicationInfo.uid) {
                    InstalledAppDetails.this.mSipper = sipper;
                    break;
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (InstalledAppDetails.this.getActivity() != null) {
                InstalledAppDetails.this.refreshUi();
            }
        }
    }

    private static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final WeakReference<InstalledAppDetails> mActivity;
        final ApplicationInfo mInfo;
        final PackageManager mPm;
        final int mState;

        DisableChanger(InstalledAppDetails activity, ApplicationInfo info, int state) {
            this.mPm = activity.mPm;
            this.mActivity = new WeakReference(activity);
            this.mInfo = info;
            this.mState = state;
        }

        protected Object doInBackground(Object... params) {
            this.mPm.setApplicationEnabledSetting(this.mInfo.packageName, this.mState, 0);
            return null;
        }
    }

    private class MemoryUpdater extends AsyncTask<Void, Void, ProcStatsPackageEntry> {
        private MemoryUpdater() {
        }

        protected ProcStatsPackageEntry doInBackground(Void... params) {
            if (InstalledAppDetails.this.getActivity() == null || InstalledAppDetails.this.mPackageInfo == null) {
                return null;
            }
            if (InstalledAppDetails.this.mStatsManager == null) {
                InstalledAppDetails.this.mStatsManager = new ProcStatsData(InstalledAppDetails.this.getActivity(), false);
                InstalledAppDetails.this.mStatsManager.setDuration(ProcessStatsBase.sDurations[0]);
            }
            InstalledAppDetails.this.mStatsManager.refreshStats(true);
            for (ProcStatsPackageEntry pkgEntry : InstalledAppDetails.this.mStatsManager.getEntries()) {
                for (ProcStatsEntry entry : pkgEntry.mEntries) {
                    if (entry.mUid == InstalledAppDetails.this.mPackageInfo.applicationInfo.uid && TextUtils.equals(entry.mPackage, InstalledAppDetails.this.mPackageInfo.applicationInfo.packageName)) {
                        pkgEntry.updateMetrics();
                        return pkgEntry;
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(ProcStatsPackageEntry entry) {
            if (InstalledAppDetails.this.getActivity() != null) {
                if (entry != null) {
                    InstalledAppDetails.this.mStats = entry;
                    InstalledAppDetails.this.mMemoryPreference.setEnabled(true);
                    double amount = Math.max(entry.mRunWeight, entry.mBgWeight) * InstalledAppDetails.this.mStatsManager.getMemInfo().weightToRam;
                    InstalledAppDetails.this.mMemoryPreference.setSummary(InstalledAppDetails.this.getString(2131627253, new Object[]{Formatter.formatShortFileSize(InstalledAppDetails.this.getContext(), (long) amount), Integer.valueOf(3)}));
                } else {
                    InstalledAppDetails.this.mMemoryPreference.setEnabled(false);
                    InstalledAppDetails.this.mMemoryPreference.setSummary(String.format(InstalledAppDetails.this.getString(2131627254), new Object[]{Integer.valueOf(3)}));
                }
            }
        }
    }

    private class SettingsPackageMonitor extends PackageMonitor {
        private SettingsPackageMonitor() {
        }

        public void onPackageAdded(String packageName, int uid) {
        }

        public void onPackageAppeared(String packageName, int reason) {
        }

        public void onPackageDisappeared(String packageName, int reason) {
            handlePackageDisappeared(packageName);
        }

        public void onPackageRemoved(String packageName, int uid) {
        }

        private void handlePackageDisappeared(String packageName) {
            if (!(InstalledAppDetails.this.mAppEntry == null || InstalledAppDetails.this.mPackageInfo == null)) {
                if (!TextUtils.equals(packageName, InstalledAppDetails.this.mPackageName)) {
                    return;
                }
            }
            InstalledAppDetails.this.setIntentAndFinish(true, true);
        }
    }

    private boolean handleDisableable(Button button) {
        if ((this.mHomePackages.contains(this.mAppEntry.info.packageName) || ((this.mCustInstalledAppDetails != null && this.mCustInstalledAppDetails.isForbidDisablableBtn(getActivity()) && this.mAppEntry.info.enabled) || Utils.isSystemPackage(this.mPm, this.mPackageInfo) || ((checkSignatue(this.mPm, this.mPackageInfo) && this.mAppEntry.info.enabled) || isNotDisablableApp(this.mAppEntry.info.packageName)))) && (this.mCustInstalledAppDetails == null || this.mCustInstalledAppDetails.isEnableSpecialDisableButton(getActivity()))) {
            button.setText(2131625619);
            return false;
        } else if (!this.mAppEntry.info.enabled || isDisabledUntilUsed()) {
            button.setText(2131625620);
            return true;
        } else {
            button.setText(2131625619);
            return true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean checkSignatue(PackageManager pm, PackageInfo pkg) {
        if (pm == null || pkg == null || pm.checkSignatures(pkg.packageName, "android") != 0) {
            return false;
        }
        Log.i("SettingsLib.Utils", "checkSignatue packageName = " + pkg.packageName);
        return true;
    }

    private boolean isDisabledUntilUsed() {
        return this.mAppEntry.info.enabledSetting == 4;
    }

    private void initUninstallButtons() {
        boolean isBundled;
        if ((this.mAppEntry.info.flags & 1) != 0) {
            isBundled = true;
        } else {
            isBundled = false;
        }
        boolean z = true;
        if (!isBundled) {
            if ((this.mPackageInfo.applicationInfo.flags & 8388608) == 0 && this.mUserManager.getUsers().size() >= 2) {
                z = false;
            }
            this.mUninstallButton.setText(2131625616);
        } else if (!PreferredSettingsUtils.isSystemAndUnRemovable(this.mAppEntry.info) || isUpdatedRemovablePreInstalledApp(this.mAppEntry.info)) {
            this.mUninstallButton.setText(2131625616);
        } else {
            z = handleDisableable(this.mUninstallButton);
        }
        if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName) && isBundled) {
            z = true;
        }
        if (isProfileOrDeviceOwner(this.mPackageInfo.packageName)) {
            z = false;
        }
        if (this.mDpm.isUninstallInQueue(this.mPackageName)) {
            z = false;
        }
        if (z && this.mHomePackages.contains(this.mPackageInfo.packageName)) {
            if (isBundled) {
                z = false;
            } else {
                ComponentName currentDefaultHome = this.mPm.getHomeActivities(new ArrayList());
                z = currentDefaultHome == null ? this.mHomePackages.size() > 1 : !this.mPackageInfo.packageName.equals(currentDefaultHome.getPackageName());
            }
        }
        if (this.mAppsControlDisallowedBySystem) {
            z = false;
        }
        try {
            if (Stub.asInterface(ServiceManager.getService("webviewupdate")).isFallbackPackage(this.mAppEntry.info.packageName)) {
                z = false;
            }
            if (this.mCustInstalledAppDetails != null) {
                z = this.mCustInstalledAppDetails.getUninstallBtnEnableState(z);
            }
            this.mUninstallButton.setEnabled(z);
            if (this.mPackageName != null && (this.mPackageName.contains("com.android.inputmethod.latin") || this.mPackageName.contains("com.google.android.inputmethod.latin"))) {
                this.mUninstallButton.setEnabled(false);
            }
            if (z) {
                this.mUninstallButton.setOnClickListener(this);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isProfileOrDeviceOwner(String packageName) {
        List<UserInfo> userInfos = this.mUserManager.getUsers();
        DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService("device_policy");
        if (dpm.isDeviceOwnerAppOnAnyUser(packageName)) {
            return true;
        }
        for (UserInfo userInfo : userInfos) {
            ComponentName cn = dpm.getProfileOwnerAsUser(userInfo.id);
            if (cn != null && cn.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private ComponentName getCurrentComponent(String packageName) {
        List<ComponentName> listCompName = ((DevicePolicyManager) getContext().getSystemService("device_policy")).getActiveAdmins();
        if (listCompName == null) {
            return null;
        }
        for (ComponentName i : listCompName) {
            if (i.getPackageName().equals(packageName)) {
                return i;
            }
        }
        return null;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mCustInstalledAppDetails = (HwCustInstalledAppDetails) HwCustUtils.createObj(HwCustInstalledAppDetails.class, new Object[]{this});
        setHasOptionsMenu(true);
        addPreferencesFromResource(2131230802);
        if (this.mPackageInfo != null) {
            addDynamicPrefs();
        }
        if (com.android.settings.Utils.isBandwidthControlEnabled()) {
            try {
                this.mStatsSession = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats")).openSession();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        removePreference("data_settings");
        if (com.android.settings.Utils.isWifiOnly(getActivity()) && findPreference("data_settings") != null) {
            removePreference("data_settings");
        }
        hideDataIfNeeded();
        this.mBatteryHelper = new BatteryStatsHelper(getActivity(), true);
    }

    private void hideDataIfNeeded() {
        if (this.mAppEntry != null && this.mAppEntry.info != null && this.mAppEntry.info.uid == 1000) {
            Log.d(TAG, "hideDataIfNeeded->uid is system uid, hide Data entrance");
            removePreference("data_settings");
        }
    }

    protected int getMetricsCategory() {
        return 20;
    }

    public void onResume() {
        super.onResume();
        this.mSettingsPackageMonitor.register(getActivity(), getActivity().getMainLooper(), false);
        if (!this.mFinishing) {
            this.mState.requestSize(this.mPackageName, this.mUserId);
            AppItem app = new AppItem(this.mAppEntry.info.uid);
            app.addUid(this.mAppEntry.info.uid);
            if (this.mStatsSession != null) {
                getLoaderManager().restartLoader(2, ChartDataLoader.buildArgs(getTemplate(getContext()), app), this.mDataCallbacks);
            }
            new BatteryUpdater().execute(new Void[0]);
            new MemoryUpdater().execute(new Void[0]);
            updateDynamicPrefs();
        }
    }

    public void onPause() {
        this.mSettingsPackageMonitor.unregister();
        getLoaderManager().destroyLoader(2);
        super.onPause();
    }

    public void onDestroy() {
        TrafficStats.closeQuietly(this.mStatsSession);
        super.onDestroy();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!this.mFinishing) {
            handleHeader();
            this.mNotificationPreference = findPreference("notification_settings");
            this.mNotificationPreference.setOnPreferenceClickListener(this);
            this.mStoragePreference = findPreference("storage_settings");
            this.mStoragePreference.setOnPreferenceClickListener(this);
            this.mPermissionsPreference = findPreference("permission_settings");
            this.mPermissionsPreference.setOnPreferenceClickListener(this);
            this.mDataPreference = findPreference("data_settings");
            if (this.mDataPreference != null) {
                this.mDataPreference.setOnPreferenceClickListener(this);
            }
            this.mBatteryPreference = findPreference("battery");
            this.mBatteryPreference.setEnabled(false);
            this.mBatteryPreference.setOnPreferenceClickListener(this);
            this.mMemoryPreference = findPreference("memory");
            this.mMemoryPreference.setOnPreferenceClickListener(this);
            this.mLaunchPreference = findPreference("preferred_settings");
            if (this.mAppEntry == null || this.mAppEntry.info == null) {
                this.mLaunchPreference.setEnabled(false);
            } else if ((this.mAppEntry.info.flags & 8388608) == 0 || !this.mAppEntry.info.enabled) {
                this.mLaunchPreference.setEnabled(false);
            } else {
                this.mLaunchPreference.setOnPreferenceClickListener(this);
            }
        }
    }

    private void handleHeader() {
        this.mHeader = (LayoutPreference) findPreference("header_view");
        View btnPanel = this.mHeader.findViewById(2131886718);
        this.mForceStopButton = (Button) btnPanel.findViewById(2131887289);
        this.mForceStopButton.setText(2131625610);
        this.mUninstallButton = (Button) btnPanel.findViewById(2131887288);
        this.mForceStopButton.setEnabled(false);
        View gear = this.mHeader.findViewById(2131886717);
        Intent i = new Intent("android.intent.action.APPLICATION_PREFERENCES");
        i.setPackage(this.mPackageName);
        final Intent intent = resolveIntent(i);
        if (intent != null) {
            gear.setVisibility(0);
            gear.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    InstalledAppDetails.this.startActivity(intent);
                }
            });
        } else {
            gear.setVisibility(8);
        }
        View arrow = this.mHeader.findViewById(2131886968);
        if (arrow != null) {
            arrow.setVisibility(4);
        }
    }

    private Intent resolveIntent(Intent i) {
        ResolveInfo result = getContext().getPackageManager().resolveActivity(i, 0);
        if (result != null) {
            return new Intent(i.getAction()).setClassName(result.activityInfo.packageName, result.activityInfo.name);
        }
        return null;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 2, 0, 2131625622).setShowAsAction(0);
        menu.add(0, 1, 1, 2131625617).setShowAsAction(0);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        if (!this.mFinishing) {
            boolean showIt = true;
            if (this.mUpdatedSysApp) {
                showIt = false;
            } else if (this.mAppEntry == null) {
                showIt = false;
            } else if ((this.mAppEntry.info.flags & 1) != 0) {
                showIt = false;
            } else if (this.mPackageInfo == null || this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
                showIt = false;
            } else if (UserHandle.myUserId() != 0) {
                showIt = false;
            } else if (this.mUserManager.getUsers().size() < 2) {
                showIt = false;
            }
            menu.findItem(1).setVisible(showIt);
            if (this.mAppEntry != null) {
                boolean z2 = (this.mAppEntry.info.flags & 128) != 0 ? !isUpdatedRemovablePreInstalledApp(this.mAppEntry.info) : false;
                this.mUpdatedSysApp = z2;
            }
            MenuItem uninstallUpdatesItem = menu.findItem(2);
            if (this.mUpdatedSysApp && !this.mAppsControlDisallowedBySystem) {
                z = true;
            }
            uninstallUpdatesItem.setVisible(z);
            if (uninstallUpdatesItem.isVisible()) {
                RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getActivity(), uninstallUpdatesItem, this.mAppsControlDisallowedAdmin);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                uninstallPkg(this.mAppEntry.info.packageName, true, false);
                return true;
            case 2:
                uninstallPkg(this.mAppEntry.info.packageName, false, false);
                return true;
            default:
                return false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (this.mDisableAfterUninstall) {
                this.mDisableAfterUninstall = false;
                new DisableChanger(this, this.mAppEntry.info, 3).execute(new Object[]{null});
            }
            if (!refreshUi()) {
                setIntentAndFinish(true, true);
            }
        }
        if (requestCode == 1 && !refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    private void setAppLabelAndIcon(PackageInfo pkgInfo) {
        CharSequence charSequence = null;
        View appSnippet = this.mHeader.findViewById(2131886249);
        this.mState.ensureIcon(this.mAppEntry);
        CharSequence charSequence2 = this.mAppEntry.label;
        Drawable drawable = this.mAppEntry.icon;
        if (pkgInfo != null) {
            charSequence = pkgInfo.versionName;
        }
        setupAppSnippet(appSnippet, charSequence2, drawable, charSequence);
    }

    private boolean signaturesMatch(String pkg1, String pkg2) {
        if (!(pkg1 == null || pkg2 == null)) {
            try {
                if (this.mPm.checkSignatures(pkg1, pkg2) >= 0) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    protected boolean refreshUi() {
        retrieveAppEntry();
        if (this.mAppEntry == null) {
            return false;
        }
        if (this.mPackageInfo == null) {
            return false;
        }
        List<ResolveInfo> homeActivities = new ArrayList();
        this.mPm.getHomeActivities(homeActivities);
        this.mHomePackages.clear();
        for (int i = 0; i < homeActivities.size(); i++) {
            ResolveInfo ri = (ResolveInfo) homeActivities.get(i);
            String activityPkg = ri.activityInfo.packageName;
            this.mHomePackages.add(activityPkg);
            Bundle metadata = ri.activityInfo.metaData;
            if (metadata != null) {
                String metaPkg = metadata.getString("android.app.home.alternate");
                if (signaturesMatch(metaPkg, activityPkg)) {
                    this.mHomePackages.add(metaPkg);
                }
            }
        }
        checkForceStop();
        setAppLabelAndIcon(this.mPackageInfo);
        initUninstallButtons();
        Activity context = getActivity();
        this.mStoragePreference.setSummary(AppStorageSettings.getSummary(this.mAppEntry, context));
        PermissionsSummaryHelper.getPermissionSummary(getContext(), this.mPackageName, this.mPermissionCallback);
        this.mLaunchPreference.setSummary(AppUtils.getLaunchByDefaultSummary(this.mAppEntry, this.mUsbManager, this.mPm, context));
        updateBattery();
        if (this.mInitialized) {
            try {
                ApplicationInfo ainfo = context.getPackageManager().getApplicationInfo(this.mAppEntry.info.packageName, 8704);
                if (!this.mShowUninstalled) {
                    return (ainfo.flags & 8388608) != 0;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        boolean z;
        this.mInitialized = true;
        if ((this.mAppEntry.info.flags & 8388608) == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mShowUninstalled = z;
        return true;
    }

    private void updateBattery() {
        if (this.mSipper != null) {
            this.mBatteryPreference.setEnabled(true);
            this.mBatteryPreference.setSummary((CharSequence) "");
            return;
        }
        this.mBatteryPreference.setEnabled(false);
        this.mBatteryPreference.setSummary(getString(2131626983));
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        switch (id) {
            case 1:
                return new Builder(getActivity()).setTitle(getActivity().getText(2131625687)).setMessage(getActivity().getText(2131625688)).setPositiveButton(2131625656, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        InstalledAppDetails.this.forceStopPackage(InstalledAppDetails.this.mAppEntry.info.packageName);
                    }
                }).setNegativeButton(2131625657, null).create();
            case 2:
                return new Builder(getActivity()).setMessage(getActivity().getText(2131625695)).setPositiveButton(2131625694, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new DisableChanger(InstalledAppDetails.this, InstalledAppDetails.this.mAppEntry.info, 3).execute(new Object[]{null});
                    }
                }).setNegativeButton(2131625657, null).create();
            case 3:
                return new Builder(getActivity()).setMessage(getActivity().getText(2131625695)).setPositiveButton(2131625694, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        InstalledAppDetails.this.uninstallPkg(InstalledAppDetails.this.mAppEntry.info.packageName, false, true);
                    }
                }).setNegativeButton(2131625657, null).create();
            case 4:
                ComponentName cn = getCurrentComponent(this.mAppEntry.info.packageName);
                if (cn == null) {
                    return null;
                }
                AlertDialog builder = new Builder(getActivity()).setPositiveButton(2131626164, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ActivityManagerNative.getDefault().resumeAppSwitches();
                            ComponentName cn = InstalledAppDetails.this.getCurrentComponent(InstalledAppDetails.this.mAppEntry.info.packageName);
                            if (cn != null) {
                                InstalledAppDetails.this.mDpm.removeActiveAdmin(cn);
                            }
                            InstalledAppDetails.this.uninstallPkg(InstalledAppDetails.this.mAppEntry.info.packageName, false, false);
                        } catch (RemoteException e) {
                        }
                        InstalledAppDetails.this.finish();
                    }
                }).setTitle(getText(2131627350)).setNegativeButton(2131625657, null).create();
                ResolveInfo ri = new ResolveInfo();
                try {
                    ri.activityInfo = getPackageManager().getReceiverInfo(cn, 128);
                    try {
                        this.mDeviceAdmin = new DeviceAdminInfo(getActivity(), ri);
                        View view = LayoutInflater.from(getActivity()).inflate(2130968738, null);
                        this.mAdminWarning = (TextView) view.findViewById(2131886495);
                        this.mAdminPolicies = (ViewGroup) view.findViewById(2131886496);
                        this.mAdminWarning.setText(getString(2131628198, new Object[]{this.mDeviceAdmin.getActivityInfo().applicationInfo.loadLabel(getPackageManager())}));
                        boolean isAdmin = UserManager.get(getActivity()).isAdminUser();
                        for (PolicyInfo pi : this.mDeviceAdmin.getUsedPolicies()) {
                            int labelId = isAdmin ? pi.label : pi.labelForSecondaryUsers;
                            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
                            Drawable icon = getActivity().getDrawable(17302272);
                            View permView = inflater.inflate(2130968633, null);
                            TextView permGrpView = (TextView) permView.findViewById(2131886259);
                            ((ImageView) permView.findViewById(2131886258)).setImageDrawable(icon);
                            permGrpView.setText(getText(labelId));
                            this.mAdminPolicies.addView(permView);
                        }
                        builder.setView(view);
                        return builder;
                    } catch (XmlPullParserException e) {
                        Log.w(TAG, "Unable to retrieve device policy " + cn, e);
                        return builder;
                    } catch (IOException e2) {
                        Log.w(TAG, "Unable to retrieve device policy " + cn, e2);
                        return builder;
                    }
                } catch (NameNotFoundException e3) {
                    Log.w(TAG, "Unable to retrieve device policy " + cn, e3);
                    return builder;
                }
            default:
                return null;
        }
    }

    private void uninstallPkg(String packageName, boolean allUsers, boolean andDisable) {
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + packageName));
        uninstallIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", allUsers);
        startActivityForResult(uninstallIntent, 0);
        this.mDisableAfterUninstall = andDisable;
    }

    private void forceStopPackage(String pkgName) {
        if (getActivity() != null) {
            ((ActivityManager) getActivity().getSystemService("activity")).forceStopPackage(pkgName);
            int userId = UserHandle.getUserId(this.mAppEntry.info.uid);
            this.mState.invalidatePackage(pkgName, userId);
            AppEntry newEnt = this.mState.getEntry(pkgName, userId);
            if (newEnt != null) {
                this.mAppEntry = newEnt;
            }
            checkForceStop();
        }
    }

    private void updateForceStopButton(boolean enabled) {
        if (this.mAppsControlDisallowedBySystem) {
            this.mForceStopButton.setEnabled(false);
            return;
        }
        this.mForceStopButton.setEnabled(enabled);
        this.mForceStopButton.setOnClickListener(this);
        if (this.mCustInstalledAppDetails != null) {
            this.mCustInstalledAppDetails.custUpdateForceStopButton(this.mForceStopButton);
        }
    }

    private void checkForceStop() {
        if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
            updateForceStopButton(false);
        } else if ((this.mAppEntry.info.flags & 2097152) == 0) {
            updateForceStopButton(true);
        } else {
            Intent intent = new Intent("android.intent.action.QUERY_PACKAGE_RESTART", Uri.fromParts("package", this.mAppEntry.info.packageName, null));
            intent.putExtra("android.intent.extra.PACKAGES", new String[]{this.mAppEntry.info.packageName});
            intent.putExtra("android.intent.extra.UID", this.mAppEntry.info.uid);
            intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(this.mAppEntry.info.uid));
            getActivity().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mCheckKillProcessesReceiver, null, 0, null, null);
        }
    }

    private void startManagePermissionsActivity() {
        Intent intent = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
        intent.putExtra("android.intent.extra.PACKAGE_NAME", this.mAppEntry.info.packageName);
        intent.putExtra("hideInfoButton", true);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w("InstalledAppDetails", "No app can handle android.intent.action.MANAGE_APP_PERMISSIONS");
        }
    }

    private void startAppInfoFragment(Class<?> fragment, CharSequence title) {
        startAppInfoFragment(fragment, title, this, this.mAppEntry);
    }

    public static void startAppInfoFragment(Class<?> fragment, CharSequence title, SettingsPreferenceFragment caller, AppEntry appEntry) {
        Bundle args = new Bundle();
        args.putString("package", appEntry.info.packageName);
        args.putInt("uid", appEntry.info.uid);
        args.putBoolean("hideInfoButton", true);
        ((SettingsActivity) caller.getActivity()).startPreferencePanel(fragment.getName(), args, -1, title, caller, 1);
    }

    public void onClick(View v) {
        if (this.mAppEntry == null) {
            setIntentAndFinish(true, true);
            return;
        }
        String packageName = this.mAppEntry.info.packageName;
        if (v == this.mUninstallButton) {
            if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
                Activity activity = getActivity();
                Intent uninstallDAIntent = new Intent(activity, DeviceAdminAdd.class);
                uninstallDAIntent.putExtra("android.app.extra.DEVICE_ADMIN_PACKAGE_NAME", this.mPackageName);
                activity.startActivityForResult(uninstallDAIntent, 1);
                return;
            }
            EnforcedAdmin admin = RestrictedLockUtils.checkIfUninstallBlocked(getActivity(), packageName, this.mUserId);
            boolean uninstallBlockedBySystem;
            if (this.mAppsControlDisallowedBySystem) {
                uninstallBlockedBySystem = true;
            } else {
                uninstallBlockedBySystem = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), packageName, this.mUserId);
            }
            if (admin != null && !r3) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), admin);
            } else if (PreferredSettingsUtils.isSystemAndUnRemovable(this.mAppEntry.info)) {
                if (!this.mAppEntry.info.enabled || isDisabledUntilUsed()) {
                    new DisableChanger(this, this.mAppEntry.info, 0).execute(new Object[]{null});
                } else if (this.mUpdatedSysApp && isSingleUser()) {
                    showDialogInner(3, 0);
                } else {
                    showDialogInner(2, 0);
                }
            } else if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
                showDialogInner(4, 0);
            } else if ((this.mAppEntry.info.flags & 8388608) == 0) {
                uninstallPkg(packageName, true, false);
            } else {
                uninstallPkg(packageName, false, false);
            }
        } else if (v == this.mForceStopButton) {
            if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
                showDialogInner(1, 0);
            } else {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
            }
        }
    }

    private boolean isSingleUser() {
        int userCount = this.mUserManager.getUserCount();
        if (userCount == 1) {
            return true;
        }
        UserManager userManager = this.mUserManager;
        if (UserManager.isSplitSystemUser() && userCount == 2) {
            return true;
        }
        return false;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mStoragePreference) {
            startAppInfoFragment(AppStorageSettings.class, this.mStoragePreference.getTitle());
        } else if (preference == this.mNotificationPreference) {
            intent = new Intent("huawei.intent.action.NOTIFICATIONSETTING");
            intent.putExtra("packageName", this.mAppEntry.info.packageName);
            intent.putExtra("uid", this.mAppEntry.info.uid);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (preference == this.mPermissionsPreference) {
            startManagePermissionsActivity();
        } else if (preference == this.mLaunchPreference) {
            startAppInfoFragment(AppLaunchSettings.class, this.mLaunchPreference.getTitle());
        } else if (preference == this.mMemoryPreference) {
            ProcessStatsBase.launchMemoryDetail((SettingsActivity) getActivity(), this.mStatsManager.getMemInfo(), this.mStats, false);
        } else if (preference == this.mDataPreference) {
            intent = new Intent("huawei.intent.action.TRAFFIC_APP_DETAIL");
            intent.putExtra("package", this.mAppEntry.info.packageName);
            intent.putExtra("uid", this.mAppEntry.info.uid);
            try {
                startActivity(intent);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else if (preference != this.mBatteryPreference) {
            return false;
        } else {
            intent = new Intent("huawei.intent.action.SOFT_CONSUMPTION_DETAIL");
            intent.putExtra("package", this.mAppEntry.info.packageName);
            intent.putExtra("uid", this.mAppEntry.info.uid);
            try {
                startActivity(intent);
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
        return true;
    }

    private void addDynamicPrefs() {
        if (!com.android.settings.Utils.isManagedProfile(UserManager.get(getContext()))) {
            PreferenceScreen screen = getPreferenceScreen();
            if (DefaultHomePreference.hasHomePreference(this.mPackageName, getContext())) {
                screen.addPreference(new ShortcutPreference(getPrefContext(), AdvancedAppSettings.class, "default_home", 2131627165, 2131626926));
            }
            if (DefaultBrowserPreference.hasBrowserPreference(this.mPackageName, getContext())) {
                screen.addPreference(new ShortcutPreference(getPrefContext(), AdvancedAppSettings.class, "default_browser", 2131626952, 2131626926));
            }
            if (DefaultPhonePreference.hasPhonePreference(this.mPackageName, getContext())) {
                screen.addPreference(new ShortcutPreference(getPrefContext(), AdvancedAppSettings.class, "default_phone_app", 2131626954, 2131626926));
            }
            if (DefaultEmergencyPreference.hasEmergencyPreference(this.mPackageName, getContext())) {
                screen.addPreference(new ShortcutPreference(getPrefContext(), AdvancedAppSettings.class, "default_emergency_app", 2131625634, 2131626926));
            }
            if (DefaultSmsPreference.hasSmsPreference(this.mPackageName, getContext())) {
                screen.addPreference(new ShortcutPreference(getPrefContext(), AdvancedAppSettings.class, "default_sms_app", 2131625455, 2131626926));
            }
            boolean hasDrawOverOtherApps = hasPermission("android.permission.SYSTEM_ALERT_WINDOW");
            boolean hasWriteSettings = hasPermission("android.permission.WRITE_SETTINGS");
            if (hasDrawOverOtherApps || hasWriteSettings) {
                Preference pref;
                PreferenceCategory category = new PreferenceCategory(getPrefContext());
                category.setLayoutResource(2130968916);
                category.setTitle(2131626925);
                screen.addPreference(category);
                if (hasDrawOverOtherApps) {
                    pref = new Preference(getPrefContext());
                    pref.setTitle(2131627043);
                    pref.setKey("system_alert_window");
                    pref.setWidgetLayoutResource(2130968998);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            InstalledAppDetails.this.startAppInfoFragment(DrawOverlayDetails.class, InstalledAppDetails.this.getString(2131627043));
                            return true;
                        }
                    });
                    category.addPreference(pref);
                }
                if (hasWriteSettings) {
                    pref = new Preference(getPrefContext());
                    pref.setTitle(2131627057);
                    pref.setKey("write_settings_apps");
                    pref.setWidgetLayoutResource(2130968998);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            InstalledAppDetails.this.startAppInfoFragment(WriteSettingsDetails.class, InstalledAppDetails.this.getString(2131627057));
                            return true;
                        }
                    });
                    category.addPreference(pref);
                }
            }
            addAppInstallerInfoPref(screen);
        }
    }

    private void addAppInstallerInfoPref(PreferenceScreen screen) {
        String installerPackageName = null;
        try {
            installerPackageName = getContext().getPackageManager().getInstallerPackageName(this.mPackageName);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Exception while retrieving the package installer of " + this.mPackageName, e);
        }
        if (installerPackageName != null && com.android.settings.Utils.getApplicationLabel(getContext(), installerPackageName) != null) {
            PreferenceCategory category = new PreferenceCategory(getPrefContext());
            category.setLayoutResource(2130968916);
            category.setTitle(2131625700);
            screen.addPreference(category);
            Preference pref = new Preference(getPrefContext());
            pref.setTitle(2131625701);
            pref.setKey("app_info_store");
            pref.setSummary(getString(2131625702, new Object[]{installerLabel}));
            Intent result = resolveIntent(new Intent("android.intent.action.SHOW_APP_INFO").setPackage(installerPackageName));
            if (result != null) {
                result.putExtra("android.intent.extra.PACKAGE_NAME", this.mPackageName);
                pref.setIntent(result);
            } else {
                pref.setEnabled(false);
            }
            category.addPreference(pref);
        }
    }

    private boolean hasPermission(String permission) {
        if (this.mPackageInfo == null || this.mPackageInfo.requestedPermissions == null) {
            return false;
        }
        for (String equals : this.mPackageInfo.requestedPermissions) {
            if (equals.equals(permission)) {
                return true;
            }
        }
        return false;
    }

    private void updateDynamicPrefs() {
        int i;
        int i2 = 2131624348;
        Preference pref = findPreference("default_home");
        if (pref != null) {
            if (DefaultHomePreference.isHomeDefault(this.mPackageName, getContext())) {
                i = 2131624348;
            } else {
                i = 2131624349;
            }
            pref.setSummary(i);
        }
        pref = findPreference("default_browser");
        if (pref != null) {
            if (DefaultBrowserPreference.isBrowserDefault(this.mPackageName, getContext())) {
                i = 2131624348;
            } else {
                i = 2131624349;
            }
            pref.setSummary(i);
        }
        pref = findPreference("default_phone_app");
        if (pref != null) {
            if (DefaultPhonePreference.isPhoneDefault(this.mPackageName, getContext())) {
                i = 2131624348;
            } else {
                i = 2131624349;
            }
            pref.setSummary(i);
        }
        pref = findPreference("default_emergency_app");
        if (pref != null) {
            if (DefaultEmergencyPreference.isEmergencyDefault(this.mPackageName, getContext())) {
                i = 2131624348;
            } else {
                i = 2131624349;
            }
            pref.setSummary(i);
        }
        pref = findPreference("default_sms_app");
        if (pref != null) {
            if (!DefaultSmsPreference.isSmsDefault(this.mPackageName, getContext())) {
                i2 = 2131624349;
            }
            pref.setSummary(i2);
        }
        pref = findPreference("system_alert_window");
        if (pref != null) {
            pref.setSummary(DrawOverlayDetails.getSummary(getContext(), this.mAppEntry));
        }
        pref = findPreference("write_settings_apps");
        if (pref != null) {
            pref.setSummary(WriteSettingsDetails.getSummary(getContext(), this.mAppEntry));
        }
    }

    public static void setupAppSnippet(View appSnippet, CharSequence label, Drawable icon, CharSequence versionName) {
        LayoutInflater.from(appSnippet.getContext()).inflate(2130969258, (ViewGroup) appSnippet.findViewById(16908312));
        ((ImageView) appSnippet.findViewById(16908294)).setImageDrawable(icon);
        ((TextView) appSnippet.findViewById(16908310)).setText(label);
        TextView appVersion = (TextView) appSnippet.findViewById(2131887452);
        if (TextUtils.isEmpty(versionName)) {
            appVersion.setVisibility(4);
            return;
        }
        appVersion.setSelected(true);
        appVersion.setVisibility(0);
        appVersion.setText(appSnippet.getContext().getString(2131625675, new Object[]{String.valueOf(versionName)}));
    }

    public static NetworkTemplate getTemplate(Context context) {
        if (DataUsageList.hasReadyMobileRadio(context)) {
            return NetworkTemplate.buildTemplateMobileWildcard();
        }
        if (DataUsageSummary.hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        }
        return NetworkTemplate.buildTemplateEthernet();
    }

    public static CharSequence getNotificationSummary(AppRow appRow, Context context) {
        boolean showSlider = Secure.getInt(context.getContentResolver(), "show_importance_slider", 0) == 1;
        List<String> summaryAttributes = new ArrayList();
        StringBuffer summary = new StringBuffer();
        if (showSlider) {
            if (appRow.appImportance != -1000) {
                summaryAttributes.add(context.getString(2131626912, new Object[]{Integer.valueOf(appRow.appImportance)}));
            }
        } else if (appRow.banned) {
            summaryAttributes.add(context.getString(2131626906));
        } else if (appRow.appImportance > 0 && appRow.appImportance < 3) {
            summaryAttributes.add(context.getString(2131626907));
        }
        if (new LockPatternUtils(context).isSecure(UserHandle.myUserId())) {
            if (appRow.appVisOverride == 0) {
                summaryAttributes.add(context.getString(2131626908));
            } else if (appRow.appVisOverride == -1) {
                summaryAttributes.add(context.getString(2131626909));
            }
        }
        if (appRow.appBypassDnd) {
            summaryAttributes.add(context.getString(2131626910));
        }
        int N = summaryAttributes.size();
        for (int i = 0; i < N; i++) {
            if (i > 0) {
                summary.append(context.getString(2131626911));
            }
            summary.append((String) summaryAttributes.get(i));
        }
        return summary.toString();
    }

    protected static boolean isUpdatedRemovablePreInstalledApp(ApplicationInfo info) {
        try {
            return (new ApplicationInfoEx(info).getHwFlags() & 67108864) != 0;
        } catch (Exception ex) {
            Log.e(TAG, "Error happened when checking update removablity, msg is " + ex.getMessage());
            return false;
        }
    }

    private static boolean isNotDisablableApp(String packageName) {
        if (ArrayUtils.contains(NOT_DISABLABLE_APP_LIST, packageName)) {
            return true;
        }
        return false;
    }
}
