package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.settings.AppHeader;
import com.android.settings.CancellablePreference;
import com.android.settings.CancellablePreference.OnCancelListener;
import com.android.settings.ProgressBarPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ProcStatsEntry.Service;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProcessStatsDetail extends SettingsPreferenceFragment {
    static final Comparator<ProcStatsEntry> sEntryCompare = new Comparator<ProcStatsEntry>() {
        public int compare(ProcStatsEntry lhs, ProcStatsEntry rhs) {
            if (lhs.mRunWeight < rhs.mRunWeight) {
                return 1;
            }
            if (lhs.mRunWeight > rhs.mRunWeight) {
                return -1;
            }
            return 0;
        }
    };
    static final Comparator<Service> sServiceCompare = new Comparator<Service>() {
        public int compare(Service lhs, Service rhs) {
            if (lhs.mDuration < rhs.mDuration) {
                return 1;
            }
            if (lhs.mDuration > rhs.mDuration) {
                return -1;
            }
            return 0;
        }
    };
    static final Comparator<PkgService> sServicePkgCompare = new Comparator<PkgService>() {
        public int compare(PkgService lhs, PkgService rhs) {
            if (lhs.mDuration < rhs.mDuration) {
                return 1;
            }
            if (lhs.mDuration > rhs.mDuration) {
                return -1;
            }
            return 0;
        }
    };
    private ProcStatsPackageEntry mApp;
    private DevicePolicyManager mDpm;
    private MenuItem mForceStop;
    private double mMaxMemoryUsage;
    private long mOnePercentTime;
    private PackageManager mPm;
    private PreferenceCategory mProcGroup;
    private final ArrayMap<ComponentName, CancellablePreference> mServiceMap = new ArrayMap();
    private double mTotalScale;
    private long mTotalTime;
    private double mWeightToRam;

    static class PkgService {
        long mDuration;
        final ArrayList<Service> mServices = new ArrayList();

        PkgService() {
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getActivity().getPackageManager();
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        Bundle args = getArguments();
        this.mApp = (ProcStatsPackageEntry) args.getParcelable("package_entry");
        if (this.mApp == null) {
            getActivity().finish();
            return;
        }
        this.mApp.retrieveUiData(getActivity(), this.mPm);
        this.mWeightToRam = args.getDouble("weight_to_ram");
        this.mTotalTime = args.getLong("total_time");
        this.mMaxMemoryUsage = args.getDouble("max_memory_usage");
        this.mTotalScale = args.getDouble("total_scale");
        this.mOnePercentTime = this.mTotalTime / 100;
        this.mServiceMap.clear();
        createDetails();
        setHasOptionsMenu(true);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mApp.mUiTargetApp == null) {
            finish();
        } else if (this.mApp.mPackage == null) {
            Log.e("ProcessStatsDetail", "mApp.mPackage == null");
        } else {
            AppHeader.createAppHeader(this, this.mApp.mUiTargetApp != null ? this.mApp.mUiTargetApp.loadIcon(this.mPm) : new ColorDrawable(0), this.mApp.mUiLabel, this.mApp.mPackage, this.mApp.mUiTargetApp.uid);
        }
    }

    protected int getMetricsCategory() {
        return 21;
    }

    public void onResume() {
        super.onResume();
        checkForceStop();
        updateRunningServices();
    }

    private void updateRunningServices() {
        int i;
        List<RunningServiceInfo> runningServices = ((ActivityManager) getActivity().getSystemService("activity")).getRunningServices(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
        int N = this.mServiceMap.size();
        for (i = 0; i < N; i++) {
            ((CancellablePreference) this.mServiceMap.valueAt(i)).setCancellable(false);
        }
        N = runningServices.size();
        for (i = 0; i < N; i++) {
            RunningServiceInfo runningService = (RunningServiceInfo) runningServices.get(i);
            if ((runningService.started || runningService.clientLabel != 0) && (runningService.flags & 8) == 0) {
                final ComponentName service = runningService.service;
                CancellablePreference pref = (CancellablePreference) this.mServiceMap.get(service);
                if (pref != null) {
                    pref.setOnCancelListener(new OnCancelListener() {
                        public void onCancel(CancellablePreference preference) {
                            ProcessStatsDetail.this.stopService(service.getPackageName(), service.getClassName());
                        }
                    });
                    pref.setCancellable(true);
                }
            }
        }
    }

    private void createDetails() {
        addPreferencesFromResource(2131230737);
        this.mProcGroup = (PreferenceCategory) findPreference("processes");
        fillProcessesSection();
        ProgressBarPreference progressBar = new ProgressBarPreference(getActivity());
        getPreferenceScreen().addPreference(progressBar);
        progressBar.setOrder(-1);
        double avgRam = ((this.mApp.mRunWeight > this.mApp.mBgWeight ? 1 : (this.mApp.mRunWeight == this.mApp.mBgWeight ? 0 : -1)) > 0 ? this.mApp.mRunWeight : this.mApp.mBgWeight) * this.mWeightToRam;
        progressBar.setPercent((int) ((100.0d * avgRam) / this.mMaxMemoryUsage));
        String avgRamSize = Formatter.formatShortFileSize(getContext(), (long) avgRam);
        progressBar.setTitle(getString(2131627012, new Object[]{avgRamSize.toString()}));
        findPreference("frequency").setSummary(ProcStatsPackageEntry.getFrequency(((float) Math.max(this.mApp.mRunDuration, this.mApp.mBgDuration)) / ((float) this.mTotalTime), getActivity()));
        findPreference("max_usage").setSummary(Formatter.formatShortFileSize(getContext(), (long) ((((double) Math.max(this.mApp.mMaxBgMem, this.mApp.mMaxRunMem)) * this.mTotalScale) * 1024.0d)));
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mForceStop = menu.add(0, 1, 0, 2131625610);
        checkForceStop();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                killProcesses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillProcessesSection() {
        int ie;
        this.mProcGroup.removeAll();
        ArrayList<ProcStatsEntry> entries = new ArrayList();
        for (ie = 0; ie < this.mApp.mEntries.size(); ie++) {
            ProcStatsEntry entry = (ProcStatsEntry) this.mApp.mEntries.get(ie);
            if (entry.mPackage.equals("os")) {
                entry.mLabel = entry.mName;
            } else {
                entry.mLabel = getProcessName(this.mApp.mUiLabel, entry);
            }
            entries.add(entry);
        }
        Collections.sort(entries, sEntryCompare);
        for (ie = 0; ie < entries.size(); ie++) {
            entry = (ProcStatsEntry) entries.get(ie);
            Preference processPref = new Preference(getPrefContext());
            processPref.setTitle(entry.mLabel);
            processPref.setLayoutResource(2130968980);
            long duration = Math.max(entry.mRunDuration, entry.mBgDuration);
            String memoryString = Formatter.formatShortFileSize(getActivity(), Math.max((long) (entry.mRunWeight * this.mWeightToRam), (long) (entry.mBgWeight * this.mWeightToRam)));
            CharSequence frequency = ProcStatsPackageEntry.getFrequency(((float) duration) / ((float) this.mTotalTime), getActivity());
            processPref.setSummary(getString(2131626972, new Object[]{memoryString, frequency}));
            this.mProcGroup.addPreference(processPref);
        }
        if (this.mProcGroup.getPreferenceCount() < 2) {
            getPreferenceScreen().removePreference(this.mProcGroup);
        }
    }

    private static String capitalize(String processName) {
        char c = processName.charAt(0);
        if (Character.isLowerCase(c)) {
            return Character.toUpperCase(c) + processName.substring(1);
        }
        return processName;
    }

    private static String getProcessName(String appLabel, ProcStatsEntry entry) {
        String processName = entry.mName;
        if (processName.contains(":")) {
            return capitalize(processName.substring(processName.lastIndexOf(58) + 1));
        }
        if (!processName.startsWith(entry.mPackage)) {
            return processName;
        }
        if (processName.length() == entry.mPackage.length()) {
            return appLabel;
        }
        int start = entry.mPackage.length();
        if (processName.charAt(start) == '.') {
            start++;
        }
        return capitalize(processName.substring(start));
    }

    private void stopService(String pkg, String name) {
        try {
            if ((getActivity().getPackageManager().getApplicationInfo(pkg, 0).flags & 1) != 0) {
                showStopServiceDialog(pkg, name);
            } else {
                doStopService(pkg, name);
            }
        } catch (NameNotFoundException e) {
            Log.e("ProcessStatsDetail", "Can't find app " + pkg, e);
        }
    }

    private void showStopServiceDialog(final String pkg, final String name) {
        new Builder(getActivity()).setTitle(2131625744).setMessage(2131625745).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ProcessStatsDetail.this.doStopService(pkg, name);
            }
        }).setNegativeButton(2131625657, null).show();
    }

    private void doStopService(String pkg, String name) {
        getActivity().stopService(new Intent().setClassName(pkg, name));
        updateRunningServices();
    }

    private void killProcesses() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService("activity");
        for (int i = 0; i < this.mApp.mEntries.size(); i++) {
            ProcStatsEntry ent = (ProcStatsEntry) this.mApp.mEntries.get(i);
            for (int j = 0; j < ent.mPackages.size(); j++) {
                am.forceStopPackage((String) ent.mPackages.get(j));
            }
        }
    }

    private void checkForceStop() {
        if (this.mForceStop != null) {
            if (((ProcStatsEntry) this.mApp.mEntries.get(0)).mUid < 10000) {
                this.mForceStop.setVisible(false);
                return;
            }
            boolean isStarted = false;
            for (int i = 0; i < this.mApp.mEntries.size(); i++) {
                ProcStatsEntry ent = (ProcStatsEntry) this.mApp.mEntries.get(i);
                for (int j = 0; j < ent.mPackages.size(); j++) {
                    String pkg = (String) ent.mPackages.get(j);
                    if (this.mDpm.packageHasActiveAdmins(pkg)) {
                        this.mForceStop.setEnabled(false);
                        return;
                    }
                    try {
                        if ((this.mPm.getApplicationInfo(pkg, 0).flags & 2097152) == 0) {
                            isStarted = true;
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
            }
            if (isStarted) {
                this.mForceStop.setVisible(true);
            }
        }
    }
}
