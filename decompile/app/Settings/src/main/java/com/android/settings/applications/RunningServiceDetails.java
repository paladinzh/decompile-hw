package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog.Builder;
import android.app.ApplicationErrorReport;
import android.app.ApplicationErrorReport.RunningServiceInfo;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.Utils;
import com.android.settings.applications.RunningProcessesView.ActiveItem;
import com.android.settings.applications.RunningProcessesView.ViewHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class RunningServiceDetails extends InstrumentedFragment implements OnRefreshUiListener {
    final ArrayList<ActiveDetail> mActiveDetails = new ArrayList();
    ViewGroup mAllDetails;
    ActivityManager mAm;
    StringBuilder mBuilder = new StringBuilder(128);
    boolean mHaveData;
    LayoutInflater mInflater;
    MergedItem mMergedItem;
    int mNumProcesses;
    int mNumServices;
    String mProcessName;
    View mProcessesHeader;
    View mRootView;
    View mServicesHeader;
    boolean mShowBackground;
    ViewGroup mSnippet;
    ActiveItem mSnippetActiveItem;
    ViewHolder mSnippetViewHolder;
    RunningState mState;
    int mUid;
    int mUserId;

    class ActiveDetail implements OnClickListener {
        ActiveItem mActiveItem;
        ComponentName mInstaller;
        PendingIntent mManageIntent;
        Button mReportButton;
        View mRootView;
        ServiceItem mServiceItem;
        Button mStopButton;
        ViewHolder mViewHolder;

        ActiveDetail() {
        }

        void stopActiveService(boolean confirmed) {
            ServiceItem si = this.mServiceItem;
            if (confirmed || (si.mServiceInfo.applicationInfo.flags & 1) == 0) {
                RunningServiceDetails.this.getActivity().stopService(new Intent().setComponent(si.mRunningService.service));
                if (RunningServiceDetails.this.mMergedItem == null) {
                    RunningServiceDetails.this.mState.updateNow();
                    RunningServiceDetails.this.finish();
                } else if (RunningServiceDetails.this.mShowBackground || RunningServiceDetails.this.mMergedItem.mServices.size() > 1) {
                    RunningServiceDetails.this.mState.updateNow();
                } else {
                    RunningServiceDetails.this.mState.updateNow();
                    RunningServiceDetails.this.finish();
                }
                return;
            }
            RunningServiceDetails.this.showConfirmStopDialog(si.mRunningService.service);
        }

        public void onClick(View v) {
            IOException e;
            FileInputStream fileInputStream;
            FileInputStream fileInputStream2;
            byte[] buffer;
            Intent intent;
            Throwable th;
            if (v == this.mReportButton) {
                ApplicationErrorReport report = new ApplicationErrorReport();
                report.type = 5;
                report.packageName = this.mServiceItem.mServiceInfo.packageName;
                report.installerPackageName = this.mInstaller.getPackageName();
                report.processName = this.mServiceItem.mRunningService.process;
                report.time = System.currentTimeMillis();
                report.systemApp = (this.mServiceItem.mServiceInfo.applicationInfo.flags & 1) != 0;
                RunningServiceInfo info = new RunningServiceInfo();
                if (this.mActiveItem.mFirstRunTime >= 0) {
                    info.durationMillis = SystemClock.elapsedRealtime() - this.mActiveItem.mFirstRunTime;
                } else {
                    info.durationMillis = -1;
                }
                ComponentName comp = new ComponentName(this.mServiceItem.mServiceInfo.packageName, this.mServiceItem.mServiceInfo.name);
                File filename = RunningServiceDetails.this.getActivity().getFileStreamPath("service_dump.txt");
                FileOutputStream fileOutputStream = null;
                try {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(filename);
                    try {
                        Debug.dumpService("activity", fileOutputStream2.getFD(), new String[]{"-a", "service", comp.flattenToString()});
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (IOException e2) {
                            }
                        }
                        fileOutputStream = fileOutputStream2;
                    } catch (IOException e3) {
                        e = e3;
                        fileOutputStream = fileOutputStream2;
                        try {
                            Log.w("RunningServicesDetails", "Can't dump service: " + comp, e);
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e4) {
                                }
                            }
                            fileInputStream = null;
                            fileInputStream2 = new FileInputStream(filename);
                            try {
                                buffer = new byte[((int) filename.length())];
                                fileInputStream2.read(buffer);
                                info.serviceDetails = new String(buffer);
                                if (fileInputStream2 != null) {
                                    try {
                                        fileInputStream2.close();
                                    } catch (IOException e5) {
                                    }
                                }
                                fileInputStream = fileInputStream2;
                            } catch (IOException e6) {
                                e = e6;
                                fileInputStream = fileInputStream2;
                                try {
                                    Log.w("RunningServicesDetails", "Can't read service dump: " + comp, e);
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e7) {
                                        }
                                    }
                                    filename.delete();
                                    Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                                    report.runningServiceInfo = info;
                                    intent = new Intent("android.intent.action.APP_ERROR");
                                    intent.setComponent(this.mInstaller);
                                    intent.putExtra("android.intent.extra.BUG_REPORT", report);
                                    intent.addFlags(268435456);
                                    RunningServiceDetails.this.startActivity(intent);
                                    return;
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                fileInputStream = fileInputStream2;
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                throw th;
                            }
                            filename.delete();
                            Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                            report.runningServiceInfo = info;
                            intent = new Intent("android.intent.action.APP_ERROR");
                            intent.setComponent(this.mInstaller);
                            intent.putExtra("android.intent.extra.BUG_REPORT", report);
                            intent.addFlags(268435456);
                            RunningServiceDetails.this.startActivity(intent);
                            return;
                        } catch (Throwable th4) {
                            th = th4;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e9) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        fileOutputStream = fileOutputStream2;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (IOException e10) {
                    e = e10;
                    Log.w("RunningServicesDetails", "Can't dump service: " + comp, e);
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fileInputStream = null;
                    fileInputStream2 = new FileInputStream(filename);
                    buffer = new byte[((int) filename.length())];
                    fileInputStream2.read(buffer);
                    info.serviceDetails = new String(buffer);
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                    fileInputStream = fileInputStream2;
                    filename.delete();
                    Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                    report.runningServiceInfo = info;
                    intent = new Intent("android.intent.action.APP_ERROR");
                    intent.setComponent(this.mInstaller);
                    intent.putExtra("android.intent.extra.BUG_REPORT", report);
                    intent.addFlags(268435456);
                    RunningServiceDetails.this.startActivity(intent);
                    return;
                }
                fileInputStream = null;
                try {
                    fileInputStream2 = new FileInputStream(filename);
                    buffer = new byte[((int) filename.length())];
                    fileInputStream2.read(buffer);
                    info.serviceDetails = new String(buffer);
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                    fileInputStream = fileInputStream2;
                } catch (IOException e11) {
                    e = e11;
                    Log.w("RunningServicesDetails", "Can't read service dump: " + comp, e);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    filename.delete();
                    Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                    report.runningServiceInfo = info;
                    intent = new Intent("android.intent.action.APP_ERROR");
                    intent.setComponent(this.mInstaller);
                    intent.putExtra("android.intent.extra.BUG_REPORT", report);
                    intent.addFlags(268435456);
                    RunningServiceDetails.this.startActivity(intent);
                    return;
                }
                filename.delete();
                Log.i("RunningServicesDetails", "Details: " + info.serviceDetails);
                report.runningServiceInfo = info;
                intent = new Intent("android.intent.action.APP_ERROR");
                intent.setComponent(this.mInstaller);
                intent.putExtra("android.intent.extra.BUG_REPORT", report);
                intent.addFlags(268435456);
                RunningServiceDetails.this.startActivity(intent);
                return;
            }
            if (this.mManageIntent != null) {
                try {
                    RunningServiceDetails.this.getActivity().startIntentSender(this.mManageIntent.getIntentSender(), null, 268959744, 524288, 0);
                } catch (SendIntentException e12) {
                    Log.w("RunningServicesDetails", e12);
                } catch (IllegalArgumentException e13) {
                    Log.w("RunningServicesDetails", e13);
                } catch (ActivityNotFoundException e14) {
                    Log.w("RunningServicesDetails", e14);
                }
            } else if (this.mServiceItem != null) {
                stopActiveService(false);
            } else if (this.mActiveItem.mItem.mBackground) {
                RunningServiceDetails.this.mAm.killBackgroundProcesses(this.mActiveItem.mItem.mPackageInfo.packageName);
                RunningServiceDetails.this.finish();
            } else {
                RunningServiceDetails.this.mAm.forceStopPackage(this.mActiveItem.mItem.mPackageInfo.packageName);
                RunningServiceDetails.this.finish();
            }
        }
    }

    public static class MyAlertDialogFragment extends DialogFragment {
        public static MyAlertDialogFragment newConfirmStop(int id, ComponentName comp) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putParcelable("comp", comp);
            frag.setArguments(args);
            return frag;
        }

        RunningServiceDetails getOwner() {
            return (RunningServiceDetails) getTargetFragment();
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case 1:
                    final ComponentName comp = (ComponentName) getArguments().getParcelable("comp");
                    if (getOwner().activeDetailForService(comp) == null) {
                        return null;
                    }
                    return new Builder(getActivity()).setTitle(getActivity().getString(2131625744)).setMessage(getActivity().getString(2131625745)).setPositiveButton(2131625656, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActiveDetail ad = MyAlertDialogFragment.this.getOwner().activeDetailForService(comp);
                            if (ad != null) {
                                ad.stopActiveService(true);
                            }
                        }
                    }).setNegativeButton(2131625657, null).create();
                default:
                    throw new IllegalArgumentException("unknown id " + id);
            }
        }
    }

    boolean findMergedItem() {
        MergedItem item = null;
        ArrayList<MergedItem> newItems = this.mShowBackground ? this.mState.getCurrentBackgroundItems() : this.mState.getCurrentMergedItems();
        if (newItems != null) {
            for (int i = 0; i < newItems.size(); i++) {
                MergedItem mi = (MergedItem) newItems.get(i);
                if (mi.mUserId == this.mUserId && ((this.mUid < 0 || mi.mProcess == null || mi.mProcess.mUid == this.mUid) && (this.mProcessName == null || (mi.mProcess != null && this.mProcessName.equals(mi.mProcess.mProcessName))))) {
                    item = mi;
                    break;
                }
            }
        }
        if (this.mMergedItem == item) {
            return false;
        }
        this.mMergedItem = item;
        return true;
    }

    void addServicesHeader() {
        if (this.mNumServices == 0) {
            this.mServicesHeader = this.mInflater.inflate(2130969097, this.mAllDetails, false);
            ((TextView) this.mServicesHeader.findViewById(2131886455)).setText(2131625733);
            this.mAllDetails.addView(this.mServicesHeader);
        }
        this.mNumServices++;
    }

    void addProcessesHeader() {
        if (this.mNumProcesses == 0) {
            this.mProcessesHeader = this.mInflater.inflate(2130969097, this.mAllDetails, false);
            ((TextView) this.mProcessesHeader.findViewById(2131886455)).setText(2131625734);
            this.mAllDetails.addView(this.mProcessesHeader);
        }
        this.mNumProcesses++;
    }

    void addServiceDetailsView(ServiceItem si, MergedItem mi, boolean isService, boolean inclDetails, boolean isLastService) {
        if (isService) {
            addServicesHeader();
        } else if (mi.mUserId != UserHandle.myUserId()) {
            addProcessesHeader();
        }
        BaseItem bi = si != null ? si : mi;
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(2130969072, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mServiceItem = si;
        detail.mViewHolder = new ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, bi, this.mBuilder);
        if (isLastService) {
            root.findViewById(2131887101).setVisibility(8);
        }
        if (!inclDetails) {
            root.findViewById(2131887099).setVisibility(8);
        }
        if (!(si == null || si.mRunningService.clientLabel == 0)) {
            detail.mManageIntent = this.mAm.getRunningServiceControlPanel(si.mRunningService.service);
        }
        TextView description = (TextView) root.findViewById(2131887100);
        detail.mStopButton = (Button) root.findViewById(2131887288);
        detail.mReportButton = (Button) root.findViewById(2131887289);
        if (!isService || mi.mUserId == UserHandle.myUserId()) {
            if (si != null && si.mServiceInfo.descriptionRes != 0) {
                description.setText(getActivity().getPackageManager().getText(si.mServiceInfo.packageName, si.mServiceInfo.descriptionRes, si.mServiceInfo.applicationInfo));
            } else if (mi.mBackground) {
                description.setText(2131625739);
            } else if (detail.mManageIntent == null) {
                int i;
                Activity activity = getActivity();
                if (si != null) {
                    i = 2131625737;
                } else {
                    i = 2131625738;
                }
                description.setText(activity.getText(i));
            } else if (si != null) {
                try {
                    String label = getActivity().getPackageManager().getResourcesForApplication(si.mRunningService.clientPackage).getString(si.mRunningService.clientLabel);
                    description.setText(getActivity().getString(2131625740, new Object[]{label}));
                } catch (NameNotFoundException e) {
                }
            }
            detail.mStopButton.setOnClickListener(detail);
            detail.mStopButton.setText(getActivity().getText(detail.mManageIntent != null ? 2131625736 : 2131625735));
            detail.mReportButton.setOnClickListener(detail);
            detail.mReportButton.setText(17040275);
            if (Global.getInt(getActivity().getContentResolver(), "send_action_app_error", 0) == 0 || si == null) {
                detail.mReportButton.setEnabled(false);
            } else {
                boolean z;
                detail.mInstaller = ApplicationErrorReport.getErrorReportReceiver(getActivity(), si.mServiceInfo.packageName, si.mServiceInfo.applicationInfo.flags);
                Button button = detail.mReportButton;
                if (detail.mInstaller != null) {
                    z = true;
                } else {
                    z = false;
                }
                button.setEnabled(z);
            }
        } else {
            description.setVisibility(8);
            root.findViewById(2131886718).setVisibility(8);
        }
        this.mActiveDetails.add(detail);
    }

    void addProcessDetailsView(ProcessItem pi, boolean isMain, boolean isLastProcess) {
        addProcessesHeader();
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(2130969071, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mViewHolder = new ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, pi, this.mBuilder);
        if (isLastProcess) {
            root.findViewById(2131887101).setVisibility(8);
        }
        TextView description = (TextView) root.findViewById(2131887100);
        if (pi.mUserId != UserHandle.myUserId()) {
            description.setVisibility(8);
        } else if (isMain) {
            description.setText(2131625741);
        } else {
            int textid = 0;
            CharSequence label = null;
            RunningAppProcessInfo rpi = pi.mRunningProcessInfo;
            ComponentName comp = rpi.importanceReasonComponent;
            switch (rpi.importanceReasonCode) {
                case 1:
                    textid = 2131625743;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ProviderInfo prov = getActivity().getPackageManager().getProviderInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), prov.name, prov);
                            break;
                        } catch (NameNotFoundException e) {
                            break;
                        }
                    }
                    break;
                case 2:
                    textid = 2131625742;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ServiceInfo serv = getActivity().getPackageManager().getServiceInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), serv.name, serv);
                            break;
                        } catch (NameNotFoundException e2) {
                            break;
                        }
                    }
                    break;
            }
            if (!(textid == 0 || label == null)) {
                description.setText(getActivity().getString(textid, new Object[]{label}));
            }
        }
        this.mActiveDetails.add(detail);
    }

    void addDetailsViews(MergedItem item, boolean inclServices, boolean inclProcesses) {
        if (item != null) {
            int i;
            if (inclServices) {
                for (i = 0; i < item.mServices.size(); i++) {
                    boolean z;
                    ServiceItem serviceItem = (ServiceItem) item.mServices.get(i);
                    if (i >= item.mServices.size() - 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    addServiceDetailsView(serviceItem, item, true, true, z);
                }
            }
            if (!inclProcesses) {
                return;
            }
            if (item.mServices.size() <= 0) {
                boolean z2;
                if (item.mUserId != UserHandle.myUserId()) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                addServiceDetailsView(null, item, false, z2, false);
                return;
            }
            i = -1;
            while (i < item.mOtherProcesses.size()) {
                ProcessItem pi;
                if (i < 0) {
                    pi = item.mProcess;
                } else {
                    pi = (ProcessItem) item.mOtherProcesses.get(i);
                }
                if (pi == null || pi.mPid > 0) {
                    addProcessDetailsView(pi, i < 0, i == item.mOtherProcesses.size() + -1);
                }
                i++;
            }
        }
    }

    void addDetailViews() {
        int i;
        for (i = this.mActiveDetails.size() - 1; i >= 0; i--) {
            this.mAllDetails.removeView(((ActiveDetail) this.mActiveDetails.get(i)).mRootView);
        }
        this.mActiveDetails.clear();
        if (this.mServicesHeader != null) {
            this.mAllDetails.removeView(this.mServicesHeader);
            this.mServicesHeader = null;
        }
        if (this.mProcessesHeader != null) {
            this.mAllDetails.removeView(this.mProcessesHeader);
            this.mProcessesHeader = null;
        }
        this.mNumProcesses = 0;
        this.mNumServices = 0;
        if (this.mMergedItem == null) {
            return;
        }
        if (this.mMergedItem.mUser != null) {
            ArrayList<MergedItem> items;
            if (this.mShowBackground) {
                items = new ArrayList(this.mMergedItem.mChildren);
                Collections.sort(items, this.mState.mBackgroundComparator);
            } else {
                items = this.mMergedItem.mChildren;
            }
            for (i = 0; i < items.size(); i++) {
                addDetailsViews((MergedItem) items.get(i), true, false);
            }
            for (i = 0; i < items.size(); i++) {
                addDetailsViews((MergedItem) items.get(i), false, true);
            }
            return;
        }
        addDetailsViews(this.mMergedItem, true, true);
    }

    void refreshUi(boolean dataChanged) {
        if (findMergedItem()) {
            dataChanged = true;
        }
        if (dataChanged) {
            if (this.mMergedItem != null) {
                this.mSnippetActiveItem = this.mSnippetViewHolder.bind(this.mState, this.mMergedItem, this.mBuilder);
            } else if (this.mSnippetActiveItem != null) {
                this.mSnippetActiveItem.mHolder.size.setText("");
                this.mSnippetActiveItem.mHolder.uptime.setText("");
                this.mSnippetActiveItem.mHolder.description.setText(2131625732);
            } else {
                finish();
                return;
            }
            addDetailViews();
        }
    }

    private void finish() {
        new Handler().post(new Runnable() {
            public void run() {
                try {
                    Activity a = RunningServiceDetails.this.getActivity();
                    if (a != null) {
                        FragmentManager fm = a.getFragmentManager();
                        Field fcfm = fm.getClass().getDeclaredField("mStateSaved");
                        fcfm.setAccessible(true);
                        if (!fcfm.getBoolean(fm)) {
                            a.onBackPressed();
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (SecurityException e2) {
                    e2.printStackTrace();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUid = getArguments().getInt("uid", -1);
        this.mUserId = getArguments().getInt("user_id", 0);
        this.mProcessName = getArguments().getString("process", null);
        this.mShowBackground = getArguments().getBoolean("background", false);
        this.mAm = (ActivityManager) getActivity().getSystemService("activity");
        this.mInflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
        this.mState = RunningState.getInstance(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130969070, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, true);
        this.mRootView = view;
        this.mAllDetails = (ViewGroup) view.findViewById(2131886240);
        this.mSnippet = (ViewGroup) view.findViewById(2131887098);
        this.mSnippetViewHolder = new ViewHolder(this.mSnippet);
        ensureData();
        return view;
    }

    public void onPause() {
        super.onPause();
        this.mHaveData = false;
        this.mState.pause();
    }

    protected int getMetricsCategory() {
        return 85;
    }

    public void onResume() {
        super.onResume();
        ensureData();
    }

    ActiveDetail activeDetailForService(ComponentName comp) {
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            ActiveDetail ad = (ActiveDetail) this.mActiveDetails.get(i);
            if (ad.mServiceItem != null && ad.mServiceItem.mRunningService != null && comp.equals(ad.mServiceItem.mRunningService.service)) {
                return ad;
            }
        }
        return null;
    }

    private void showConfirmStopDialog(ComponentName comp) {
        DialogFragment newFragment = MyAlertDialogFragment.newConfirmStop(1, comp);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "confirmstop");
    }

    void ensureData() {
        if (!this.mHaveData) {
            this.mHaveData = true;
            this.mState.resume(this);
            this.mState.waitForData();
            refreshUi(true);
        }
    }

    void updateTimes() {
        if (this.mSnippetActiveItem != null) {
            this.mSnippetActiveItem.updateTime(getActivity(), this.mBuilder);
        }
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            ((ActiveDetail) this.mActiveDetails.get(i)).mActiveItem.updateTime(getActivity(), this.mBuilder);
        }
    }

    public void onRefreshUi(int what) {
        if (getActivity() != null) {
            switch (what) {
                case 0:
                    updateTimes();
                    break;
                case 1:
                    refreshUi(false);
                    updateTimes();
                    break;
                case 2:
                    refreshUi(true);
                    updateTimes();
                    break;
            }
        }
    }
}
