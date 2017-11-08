package com.android.settings.print;

import android.os.Bundle;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.print.PrintManager.PrintJobStateChangeListener;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;

public class PrintJobSettingsFragment extends SettingsPreferenceFragment {
    private Preference mMessagePreference;
    private PrintJobId mPrintJobId;
    private Preference mPrintJobPreference;
    private final PrintJobStateChangeListener mPrintJobStateChangeListener = new PrintJobStateChangeListener() {
        public void onPrintJobStateChanged(PrintJobId printJobId) {
            PrintJobSettingsFragment.this.updateUi();
        }
    };
    private PrintManager mPrintManager;

    protected int getMetricsCategory() {
        return 78;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230842);
        this.mPrintJobPreference = findPreference("print_job_preference");
        this.mMessagePreference = findPreference("print_job_message_preference");
        this.mPrintManager = ((PrintManager) getActivity().getSystemService("print")).getGlobalPrintManagerForUser(getActivity().getUserId());
        setHasOptionsMenu(true);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setEnabled(false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(2131625938);
    }

    public void onStart() {
        super.onStart();
        processArguments();
        this.mPrintManager.addPrintJobStateChangeListener(this.mPrintJobStateChangeListener);
        updateUi();
    }

    public void onStop() {
        super.onStop();
        this.mPrintManager.removePrintJobStateChangeListener(this.mPrintJobStateChangeListener);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        PrintJob printJob = getPrintJob();
        if (printJob != null) {
            if (!printJob.getInfo().isCancelling()) {
                menu.add(0, 1, 0, getString(2131625940)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE))).setShowAsAction(1);
            }
            if (printJob.isFailed()) {
                menu.add(0, 2, 0, getString(2131625939)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_REFRESH))).setShowAsAction(1);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        PrintJob printJob = getPrintJob();
        if (printJob != null) {
            switch (item.getItemId()) {
                case 1:
                    printJob.cancel();
                    finish();
                    return true;
                case 2:
                    printJob.restart();
                    finish();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void processArguments() {
        if (getArguments() == null) {
            finish();
            return;
        }
        String printJobId = getArguments().getString("EXTRA_PRINT_JOB_ID");
        if (printJobId == null) {
            finish();
        } else {
            this.mPrintJobId = PrintJobId.unflattenFromString(printJobId);
        }
    }

    private PrintJob getPrintJob() {
        return this.mPrintManager.getPrintJob(this.mPrintJobId);
    }

    private void updateUi() {
        PrintJob printJob = getPrintJob();
        if (printJob == null) {
            finish();
        } else if (printJob.isCancelled() || printJob.isCompleted()) {
            finish();
        } else {
            PrintJobInfo info = printJob.getInfo();
            switch (info.getState()) {
                case 2:
                case 3:
                    if (!printJob.getInfo().isCancelling()) {
                        this.mPrintJobPreference.setTitle(getString(2131625942, new Object[]{info.getLabel()}));
                        break;
                    }
                    this.mPrintJobPreference.setTitle(getString(2131625943, new Object[]{info.getLabel()}));
                    break;
                case 4:
                    if (!printJob.getInfo().isCancelling()) {
                        this.mPrintJobPreference.setTitle(getString(2131625945, new Object[]{info.getLabel()}));
                        break;
                    }
                    this.mPrintJobPreference.setTitle(getString(2131625943, new Object[]{info.getLabel()}));
                    break;
                case 6:
                    this.mPrintJobPreference.setTitle(getString(2131625944, new Object[]{info.getLabel()}));
                    break;
            }
            this.mPrintJobPreference.setSummary(getString(2131625941, new Object[]{info.getPrinterName(), DateUtils.formatSameDayTime(info.getCreationTime(), info.getCreationTime(), 3, 3)}));
            switch (info.getState()) {
                case 2:
                case 3:
                    this.mPrintJobPreference.setIcon(2130838304);
                    break;
                case 4:
                case 6:
                    this.mPrintJobPreference.setIcon(2130838305);
                    break;
            }
            CharSequence status = info.getStatus(getPackageManager());
            if (TextUtils.isEmpty(status)) {
                getPreferenceScreen().removePreference(this.mMessagePreference);
            } else {
                if (getPreferenceScreen().findPreference("print_job_message_preference") == null) {
                    getPreferenceScreen().addPreference(this.mMessagePreference);
                }
                this.mMessagePreference.setSummary(status);
            }
            getActivity().invalidateOptionsMenu();
        }
    }
}
