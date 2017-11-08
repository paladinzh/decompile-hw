package com.android.settings.applications;

import android.app.AppOpsManager.OpEntry;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.AppOpsState.AppOpEntry;
import com.android.settings.applications.AppOpsState.OpsTemplate;
import java.util.List;

public class AppOpsCategory extends ListFragment implements LoaderCallbacks<List<AppOpEntry>> {
    AppListAdapter mAdapter;
    String mCurrentPkgName;
    AppOpsState mState;
    boolean mUserControlled;

    public static class AppListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        List<AppOpEntry> mList;
        private final Resources mResources;
        private final AppOpsState mState;
        private final boolean mUserControlled;

        public AppListAdapter(Context context, AppOpsState state, boolean userControlled) {
            this.mResources = context.getResources();
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mState = state;
            this.mUserControlled = userControlled;
        }

        public void setData(List<AppOpEntry> data) {
            this.mList = data;
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.mList != null ? this.mList.size() : 0;
        }

        public AppOpEntry getItem(int position) {
            return (AppOpEntry) this.mList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            boolean z = false;
            if (convertView == null) {
                view = this.mInflater.inflate(2130968631, parent, false);
            } else {
                view = convertView;
            }
            AppOpEntry item = getItem(position);
            ((ImageView) view.findViewById(2131886245)).setImageDrawable(item.getAppEntry().getIcon());
            ((TextView) view.findViewById(2131886246)).setText(item.getAppEntry().getLabel());
            if (this.mUserControlled) {
                ((TextView) view.findViewById(2131886252)).setText(item.getTimeText(this.mResources, false));
                view.findViewById(2131886253).setVisibility(8);
                Switch switchR = (Switch) view.findViewById(2131886255);
                if (item.getPrimaryOpMode() == 0) {
                    z = true;
                }
                switchR.setChecked(z);
            } else {
                ((TextView) view.findViewById(2131886252)).setText(item.getSummaryText(this.mState));
                ((TextView) view.findViewById(2131886253)).setText(item.getTimeText(this.mResources, false));
                view.findViewById(2131886255).setVisibility(8);
            }
            return view;
        }
    }

    public static class AppListLoader extends AsyncTaskLoader<List<AppOpEntry>> {
        List<AppOpEntry> mApps;
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        PackageIntentReceiver mPackageObserver;
        final AppOpsState mState;
        final OpsTemplate mTemplate;
        final boolean mUserControlled;

        public AppListLoader(Context context, AppOpsState state, OpsTemplate template, boolean userControlled) {
            super(context);
            this.mState = state;
            this.mTemplate = template;
            this.mUserControlled = userControlled;
        }

        public List<AppOpEntry> loadInBackground() {
            return this.mState.buildState(this.mTemplate, 0, null, this.mUserControlled ? AppOpsState.LABEL_COMPARATOR : AppOpsState.RECENCY_COMPARATOR);
        }

        public void deliverResult(List<AppOpEntry> apps) {
            if (isReset() && apps != null) {
                onReleaseResources(apps);
            }
            List<AppOpEntry> oldApps = apps;
            this.mApps = apps;
            if (isStarted()) {
                super.deliverResult(apps);
            }
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        protected void onStartLoading() {
            onContentChanged();
            if (this.mApps != null) {
                deliverResult(this.mApps);
            }
            if (this.mPackageObserver == null) {
                this.mPackageObserver = new PackageIntentReceiver(this);
            }
            boolean configChange = this.mLastConfig.applyNewConfig(getContext().getResources());
            if (!(takeContentChanged() || this.mApps == null)) {
                if (!configChange) {
                    return;
                }
            }
            forceLoad();
        }

        protected void onStopLoading() {
            cancelLoad();
        }

        public void onCanceled(List<AppOpEntry> apps) {
            super.onCanceled(apps);
            onReleaseResources(apps);
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            if (this.mApps != null) {
                onReleaseResources(this.mApps);
                this.mApps = null;
            }
            if (this.mPackageObserver != null) {
                getContext().unregisterReceiver(this.mPackageObserver);
                this.mPackageObserver = null;
            }
        }

        protected void onReleaseResources(List<AppOpEntry> list) {
        }
    }

    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res) {
            boolean densityChanged;
            int configChanges = this.mLastConfiguration.updateFrom(res.getConfiguration());
            if (this.mLastDensity != res.getDisplayMetrics().densityDpi) {
                densityChanged = true;
            } else {
                densityChanged = false;
            }
            if (!densityChanged && (configChanges & 772) == 0) {
                return false;
            }
            this.mLastDensity = res.getDisplayMetrics().densityDpi;
            return true;
        }
    }

    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            this.mLoader = loader;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            this.mLoader.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            this.mLoader.getContext().registerReceiver(this, sdFilter);
        }

        public void onReceive(Context context, Intent intent) {
            this.mLoader.onContentChanged();
        }
    }

    public AppOpsCategory(OpsTemplate template) {
        this(template, false);
    }

    public AppOpsCategory(OpsTemplate template, boolean userControlled) {
        Bundle args = new Bundle();
        args.putParcelable("template", template);
        args.putBoolean("userControlled", userControlled);
        setArguments(args);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mState = new AppOpsState(getActivity());
        this.mUserControlled = getArguments().getBoolean("userControlled");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No applications");
        setHasOptionsMenu(true);
        this.mAdapter = new AppListAdapter(getActivity(), this.mState, this.mUserControlled);
        setListAdapter(this.mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    private void startApplicationDetailsActivity() {
        Bundle args = new Bundle();
        args.putString("package", this.mCurrentPkgName);
        ((SettingsActivity) getActivity()).startPreferencePanel(AppOpsDetails.class.getName(), args, 2131625703, null, this, 1);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        AppOpEntry entry = this.mAdapter.getItem(position);
        if (entry == null) {
            return;
        }
        if (this.mUserControlled) {
            Switch sw = (Switch) v.findViewById(2131886255);
            boolean checked = !sw.isChecked();
            sw.setChecked(checked);
            OpEntry op = entry.getOpEntry(0);
            int mode = checked ? 0 : 1;
            this.mState.getAppOpsManager().setMode(op.getOp(), entry.getAppEntry().getApplicationInfo().uid, entry.getAppEntry().getApplicationInfo().packageName, mode);
            entry.overridePrimaryOpMode(mode);
            return;
        }
        this.mCurrentPkgName = entry.getAppEntry().getApplicationInfo().packageName;
        startApplicationDetailsActivity();
    }

    public Loader<List<AppOpEntry>> onCreateLoader(int id, Bundle args) {
        Bundle fargs = getArguments();
        OpsTemplate opsTemplate = null;
        if (fargs != null) {
            opsTemplate = (OpsTemplate) fargs.getParcelable("template");
        }
        return new AppListLoader(getActivity(), this.mState, opsTemplate, this.mUserControlled);
    }

    public void onLoadFinished(Loader<List<AppOpEntry>> loader, List<AppOpEntry> data) {
        this.mAdapter.setData(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<List<AppOpEntry>> loader) {
        this.mAdapter.setData(null);
    }
}
