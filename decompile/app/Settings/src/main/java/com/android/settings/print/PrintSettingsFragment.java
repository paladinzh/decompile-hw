package com.android.settings.print;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.print.PrintManager.PrintJobStateChangeListener;
import android.print.PrintServicesLoader;
import android.printservice.PrintServiceInfo;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.DialogCreatable;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.utils.ProfileSettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class PrintSettingsFragment extends ProfileSettingsPreferenceFragment implements DialogCreatable, Indexable, OnClickListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList();
            PackageManager packageManager = context.getPackageManager();
            PrintManager printManager = (PrintManager) context.getSystemService("print");
            String screenTitle = context.getResources().getString(2131625924);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            indexables.add(data);
            List<PrintServiceInfo> services = printManager.getPrintServices(3);
            if (services != null) {
                int serviceCount = services.size();
                for (int i = 0; i < serviceCount; i++) {
                    PrintServiceInfo service = (PrintServiceInfo) services.get(i);
                    ComponentName componentName = new ComponentName(service.getResolveInfo().serviceInfo.packageName, service.getResolveInfo().serviceInfo.name);
                    data = new SearchIndexableRaw(context);
                    data.key = componentName.flattenToString();
                    data.title = service.getResolveInfo().loadLabel(packageManager).toString();
                    data.screenTitle = screenTitle;
                    indexables.add(data);
                }
            }
            return indexables;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = 2131230843;
            indexables.add(indexable);
            return indexables;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new PrintSummaryProvider(activity, summaryLoader);
        }
    };
    private PreferenceCategory mActivePrintJobsCategory;
    private Button mAddNewServiceButton;
    private PrintJobsController mPrintJobsController;
    private PreferenceCategory mPrintServicesCategory;
    private PrintServicesController mPrintServicesController;

    private final class PrintJobsController implements LoaderCallbacks<List<PrintJobInfo>> {
        private PrintJobsController() {
        }

        public Loader<List<PrintJobInfo>> onCreateLoader(int id, Bundle args) {
            if (id == 1) {
                return new PrintJobsLoader(PrintSettingsFragment.this.getContext());
            }
            return null;
        }

        public void onLoadFinished(Loader<List<PrintJobInfo>> loader, List<PrintJobInfo> printJobs) {
            if (printJobs == null || printJobs.isEmpty()) {
                PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
                return;
            }
            if (PrintSettingsFragment.this.getPreferenceScreen().findPreference("print_jobs_category") == null) {
                PrintSettingsFragment.this.getPreferenceScreen().addPreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
            }
            PrintSettingsFragment.this.mActivePrintJobsCategory.removeAll();
            int printJobCount = printJobs.size();
            for (int i = 0; i < printJobCount; i++) {
                PrintJobInfo printJob = (PrintJobInfo) printJobs.get(i);
                PreferenceScreen preference = PrintSettingsFragment.this.getPreferenceManager().createPreferenceScreen(PrintSettingsFragment.this.getActivity());
                preference.setPersistent(false);
                preference.setFragment(PrintJobSettingsFragment.class.getName());
                preference.setKey(printJob.getId().flattenToString());
                preference.setLayoutResource(2130969013);
                switch (printJob.getState()) {
                    case 2:
                    case 3:
                        if (!printJob.isCancelling()) {
                            preference.setTitle(PrintSettingsFragment.this.getString(2131625942, new Object[]{printJob.getLabel()}));
                            break;
                        }
                        preference.setTitle(PrintSettingsFragment.this.getString(2131625943, new Object[]{printJob.getLabel()}));
                        break;
                    case 4:
                        if (!printJob.isCancelling()) {
                            preference.setTitle(PrintSettingsFragment.this.getString(2131625945, new Object[]{printJob.getLabel()}));
                            break;
                        }
                        preference.setTitle(PrintSettingsFragment.this.getString(2131625943, new Object[]{printJob.getLabel()}));
                        break;
                    case 6:
                        preference.setTitle(PrintSettingsFragment.this.getString(2131625944, new Object[]{printJob.getLabel()}));
                        break;
                }
                preference.setSummary(PrintSettingsFragment.this.getString(2131625941, new Object[]{printJob.getPrinterName(), DateUtils.formatSameDayTime(printJob.getCreationTime(), printJob.getCreationTime(), 3, 3)}));
                switch (printJob.getState()) {
                    case 2:
                    case 3:
                        preference.setIcon(2130838304);
                        break;
                    case 4:
                    case 6:
                        preference.setIcon(2130838305);
                        break;
                    default:
                        break;
                }
                preference.getExtras().putString("EXTRA_PRINT_JOB_ID", printJob.getId().flattenToString());
                PrintSettingsFragment.this.mActivePrintJobsCategory.addPreference(preference);
            }
        }

        public void onLoaderReset(Loader<List<PrintJobInfo>> loader) {
            PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
        }
    }

    private static final class PrintJobsLoader extends AsyncTaskLoader<List<PrintJobInfo>> {
        private PrintJobStateChangeListener mPrintJobStateChangeListener;
        private List<PrintJobInfo> mPrintJobs = new ArrayList();
        private final PrintManager mPrintManager;

        public PrintJobsLoader(Context context) {
            super(context);
            this.mPrintManager = ((PrintManager) context.getSystemService("print")).getGlobalPrintManagerForUser(context.getUserId());
        }

        public void deliverResult(List<PrintJobInfo> printJobs) {
            if (isStarted()) {
                super.deliverResult(printJobs);
            }
        }

        protected void onStartLoading() {
            if (!this.mPrintJobs.isEmpty()) {
                deliverResult(new ArrayList(this.mPrintJobs));
            }
            if (this.mPrintJobStateChangeListener == null) {
                this.mPrintJobStateChangeListener = new PrintJobStateChangeListener() {
                    public void onPrintJobStateChanged(PrintJobId printJobId) {
                        PrintJobsLoader.this.onForceLoad();
                    }
                };
                this.mPrintManager.addPrintJobStateChangeListener(this.mPrintJobStateChangeListener);
            }
            if (this.mPrintJobs.isEmpty()) {
                onForceLoad();
            }
        }

        protected void onStopLoading() {
            onCancelLoad();
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            this.mPrintJobs.clear();
            if (this.mPrintJobStateChangeListener != null) {
                this.mPrintManager.removePrintJobStateChangeListener(this.mPrintJobStateChangeListener);
                this.mPrintJobStateChangeListener = null;
            }
        }

        public List<PrintJobInfo> loadInBackground() {
            List<PrintJobInfo> printJobInfos = null;
            List<PrintJob> printJobs = this.mPrintManager.getPrintJobs();
            int printJobCount = printJobs.size();
            for (int i = 0; i < printJobCount; i++) {
                PrintJobInfo printJob = ((PrintJob) printJobs.get(i)).getInfo();
                if (PrintSettingsFragment.shouldShowToUser(printJob)) {
                    if (printJobInfos == null) {
                        printJobInfos = new ArrayList();
                    }
                    printJobInfos.add(printJob);
                }
            }
            return printJobInfos;
        }
    }

    private final class PrintServicesController implements LoaderCallbacks<List<PrintServiceInfo>> {
        private PrintServicesController() {
        }

        public Loader<List<PrintServiceInfo>> onCreateLoader(int id, Bundle args) {
            PrintManager printManager = (PrintManager) PrintSettingsFragment.this.getContext().getSystemService("print");
            if (printManager != null) {
                return new PrintServicesLoader(printManager, PrintSettingsFragment.this.getContext(), 3);
            }
            return null;
        }

        public void onLoadFinished(Loader<List<PrintServiceInfo>> loader, List<PrintServiceInfo> services) {
            if (services.isEmpty()) {
                PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mPrintServicesCategory);
                return;
            }
            if (PrintSettingsFragment.this.getPreferenceScreen().findPreference("print_services_category") == null) {
                PrintSettingsFragment.this.getPreferenceScreen().addPreference(PrintSettingsFragment.this.mPrintServicesCategory);
            }
            PrintSettingsFragment.this.mPrintServicesCategory.removeAll();
            PackageManager pm = PrintSettingsFragment.this.getActivity().getPackageManager();
            int numServices = services.size();
            for (int i = 0; i < numServices; i++) {
                PrintServiceInfo service = (PrintServiceInfo) services.get(i);
                boolean bMopriaFlg = false;
                PreferenceScreen preference = PrintSettingsFragment.this.getPreferenceManager().createPreferenceScreen(PrintSettingsFragment.this.getActivity());
                String title = service.getResolveInfo().loadLabel(pm).toString();
                preference.setTitle((CharSequence) title);
                ComponentName componentName = service.getComponentName();
                preference.setKey(componentName.flattenToString());
                if (componentName.getClassName().contains("mopria") && !SettingsExtUtils.isGlobalVersion() && PrintSettingsFragment.this.getServiceInitState()) {
                    bMopriaFlg = true;
                }
                preference.setFragment(PrintServiceSettingsFragment.class.getName());
                preference.setPersistent(false);
                preference.setLayoutResource(2130968977);
                preference.setWidgetLayoutResource(2130968998);
                if (!service.isEnabled() || bMopriaFlg) {
                    preference.setSummary(PrintSettingsFragment.this.getString(2131625931));
                } else {
                    preference.setSummary(PrintSettingsFragment.this.getString(2131625930));
                }
                Drawable drawable = service.getResolveInfo().loadIcon(pm);
                if (drawable != null) {
                    preference.setIcon(drawable);
                }
                Bundle extras = preference.getExtras();
                String str = "EXTRA_CHECKED";
                boolean z = service.isEnabled() && !bMopriaFlg;
                extras.putBoolean(str, z);
                extras.putString("EXTRA_TITLE", title);
                extras.putString("EXTRA_SERVICE_COMPONENT_NAME", componentName.flattenToString());
                PrintSettingsFragment.this.mPrintServicesCategory.addPreference(preference);
            }
            Preference addNewServicePreference = PrintSettingsFragment.this.newAddServicePreferenceOrNull();
            if (addNewServicePreference != null) {
                PrintSettingsFragment.this.mPrintServicesCategory.addPreference(addNewServicePreference);
            }
        }

        public void onLoaderReset(Loader<List<PrintServiceInfo>> loader) {
            PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mPrintServicesCategory);
        }
    }

    private static class PrintSummaryProvider implements SummaryProvider, PrintJobStateChangeListener {
        private final Context mContext;
        private final PrintManager mPrintManager;
        private final SummaryLoader mSummaryLoader;

        public PrintSummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mPrintManager = ((PrintManager) context.getSystemService("print")).getGlobalPrintManagerForUser(context.getUserId());
        }

        public void setListening(boolean isListening) {
            if (this.mPrintManager == null) {
                return;
            }
            if (isListening) {
                this.mPrintManager.addPrintJobStateChangeListener(this);
                onPrintJobStateChanged(null);
                return;
            }
            this.mPrintManager.removePrintJobStateChangeListener(this);
        }

        public void onPrintJobStateChanged(PrintJobId printJobId) {
            List<PrintJob> printJobs = this.mPrintManager.getPrintJobs();
            int numActivePrintJobs = 0;
            int numPrintJobs = printJobs.size();
            for (int i = 0; i < numPrintJobs; i++) {
                if (PrintSettingsFragment.shouldShowToUser(((PrintJob) printJobs.get(i)).getInfo())) {
                    numActivePrintJobs++;
                }
            }
            this.mSummaryLoader.setSummary(this, this.mContext.getResources().getQuantityString(2131689489, numActivePrintJobs, new Object[]{Integer.valueOf(numActivePrintJobs)}));
        }
    }

    protected int getMetricsCategory() {
        return 80;
    }

    protected int getHelpResource() {
        return 2131626533;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230843);
        this.mActivePrintJobsCategory = (PreferenceCategory) findPreference("print_jobs_category");
        this.mPrintServicesCategory = (PreferenceCategory) findPreference("print_services_category");
        getPreferenceScreen().removePreference(this.mActivePrintJobsCategory);
        this.mPrintJobsController = new PrintJobsController();
        getLoaderManager().initLoader(1, null, this.mPrintJobsController);
        this.mPrintServicesController = new PrintServicesController();
        getLoaderManager().initLoader(2, null, this.mPrintServicesController);
    }

    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        startSubSettingsIfNeeded();
    }

    public void onStop() {
        super.onStop();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(2130968770, contentRoot, false);
        ((ImageView) emptyView.findViewById(2131886560)).setImageResource(2130838391);
        ((TextView) emptyView.findViewById(2131886561)).setText(2131625926);
        if (createAddNewServiceIntentOrNull() != null) {
            this.mAddNewServiceButton = (Button) emptyView.findViewById(2131886562);
            if (this.mAddNewServiceButton != null) {
                this.mAddNewServiceButton.setOnClickListener(this);
                this.mAddNewServiceButton.setVisibility(0);
            }
        }
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    protected String getIntentActionString() {
        return "android.settings.ACTION_PRINT_SETTINGS";
    }

    private boolean getServiceInitState() {
        if (System.getIntForUser(getContentResolver(), "MopriaSwitch", 0, UserHandle.myUserId()) == 0) {
            return true;
        }
        return false;
    }

    private Preference newAddServicePreferenceOrNull() {
        Intent addNewServiceIntent = createAddNewServiceIntentOrNull();
        if (addNewServiceIntent == null) {
            return null;
        }
        Preference preference = new Preference(getPrefContext());
        preference.setTitle(2131625932);
        preference.setIcon(2130838273);
        preference.setOrder(2147483646);
        preference.setIntent(addNewServiceIntent);
        preference.setPersistent(false);
        return preference;
    }

    private Intent createAddNewServiceIntentOrNull() {
        String searchUri = Secure.getString(getContentResolver(), "print_service_search_uri");
        if (TextUtils.isEmpty(searchUri)) {
            return null;
        }
        return new Intent("android.intent.action.VIEW", Uri.parse(searchUri));
    }

    private void startSubSettingsIfNeeded() {
        if (getArguments() != null) {
            String componentName = getArguments().getString("EXTRA_PRINT_SERVICE_COMPONENT_NAME");
            if (componentName != null) {
                getArguments().remove("EXTRA_PRINT_SERVICE_COMPONENT_NAME");
                Preference prereference = findPreference(componentName);
                if (prereference != null) {
                    prereference.performClick();
                }
            }
        }
    }

    public void onClick(View v) {
        if (this.mAddNewServiceButton == v) {
            Intent addNewServiceIntent = createAddNewServiceIntentOrNull();
            if (addNewServiceIntent != null) {
                try {
                    startActivity(addNewServiceIntent);
                } catch (ActivityNotFoundException e) {
                    Log.w("PrintSettingsFragment", "Unable to start activity", e);
                }
            }
        }
    }

    private static boolean shouldShowToUser(PrintJobInfo printJob) {
        switch (printJob.getState()) {
            case 2:
            case 3:
            case 4:
            case 6:
                return true;
            default:
                return false;
        }
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        ItemUseStat.getInstance().handleClick(getActivity(), 2, pref.getKey());
        return super.onPreferenceTreeClick(pref);
    }
}
