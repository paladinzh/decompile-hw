package com.android.settings.datausage;

import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import com.android.settings.datausage.CycleAdapter.CycleItem;
import com.android.settings.datausage.CycleAdapter.SpinnerInterface;
import com.android.settingslib.AppItem;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;
import com.android.settingslib.net.SummaryForAllUidLoader;
import com.android.settingslib.net.UidDetailProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUsageList extends DataUsageBase {
    private PreferenceGroup mApps;
    private boolean mBinding;
    private ChartDataUsagePreference mChart;
    private ChartData mChartData;
    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(DataUsageList.this.getActivity(), DataUsageList.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            DataUsageList.this.setLoading(false, true);
            DataUsageList.this.mChartData = data;
            DataUsageList.this.mChart.setNetworkStats(DataUsageList.this.mChartData.network);
            DataUsageList.this.updatePolicy(true);
        }

        public void onLoaderReset(Loader<ChartData> loader) {
            DataUsageList.this.mChartData = null;
            DataUsageList.this.mChart.setNetworkStats(null);
        }
    };
    private CycleAdapter mCycleAdapter;
    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            CycleItem cycle = (CycleItem) DataUsageList.this.mCycleSpinner.getSelectedItem();
            DataUsageList.this.mChart.setVisibleRange(cycle.start, cycle.end);
            DataUsageList.this.updateDetailData();
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private Spinner mCycleSpinner;
    private View mHeader;
    private final Map<String, Boolean> mMobileDataEnabled = new HashMap();
    private INetworkStatsSession mStatsSession;
    private int mSubId;
    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(DataUsageList.this.getActivity(), DataUsageList.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
            DataUsageList.this.bindStats(data, DataUsageList.this.services.mPolicyManager.getUidsWithPolicy(1));
            updateEmptyVisible();
        }

        public void onLoaderReset(Loader<NetworkStats> loader) {
            DataUsageList.this.bindStats(null, new int[0]);
            updateEmptyVisible();
        }

        private void updateEmptyVisible() {
            Object obj;
            Object obj2 = 1;
            if (DataUsageList.this.mApps.getPreferenceCount() != 0) {
                obj = 1;
            } else {
                obj = null;
            }
            if (DataUsageList.this.getPreferenceScreen().getPreferenceCount() == 0) {
                obj2 = null;
            }
            if (obj == obj2) {
                return;
            }
            if (DataUsageList.this.mApps.getPreferenceCount() != 0) {
                DataUsageList.this.getPreferenceScreen().addPreference(DataUsageList.this.mUsageAmount);
                DataUsageList.this.getPreferenceScreen().addPreference(DataUsageList.this.mApps);
                return;
            }
            DataUsageList.this.getPreferenceScreen().removeAll();
        }
    };
    private NetworkTemplate mTemplate;
    private UidDetailProvider mUidDetailProvider;
    private Preference mUsageAmount;

    protected int getMetricsCategory() {
        return 341;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        if (!isBandwidthControlEnabled()) {
            Log.w("DataUsage", "No bandwidth control; leaving");
            getActivity().finish();
        }
        try {
            this.mStatsSession = this.services.mStatsService.openSession();
            this.mUidDetailProvider = new UidDetailProvider(context);
            addPreferencesFromResource(2131230761);
            this.mUsageAmount = findPreference("usage_amount");
            this.mChart = (ChartDataUsagePreference) findPreference("chart_data");
            this.mApps = (PreferenceGroup) findPreference("apps_group");
            Bundle args = getArguments();
            this.mSubId = args.getInt("sub_id", -1);
            this.mTemplate = (NetworkTemplate) args.getParcelable("network_template");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        this.mHeader = setPinnedHeaderView(2130968637);
        this.mCycleSpinner = (Spinner) this.mHeader.findViewById(2131886266);
        this.mCycleAdapter = new CycleAdapter(getContext(), new SpinnerInterface() {
            public void setAdapter(CycleAdapter cycleAdapter) {
                DataUsageList.this.mCycleSpinner.setAdapter(cycleAdapter);
            }

            public void setOnItemSelectedListener(OnItemSelectedListener listener) {
                DataUsageList.this.mCycleSpinner.setOnItemSelectedListener(listener);
            }

            public Object getSelectedItem() {
                return DataUsageList.this.mCycleSpinner.getSelectedItem();
            }

            public void setSelection(int position) {
                DataUsageList.this.mCycleSpinner.setSelection(position);
            }
        }, this.mCycleListener, true);
        setLoading(true, false);
    }

    public void onResume() {
        super.onResume();
        updateBody();
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                    DataUsageList.this.services.mStatsService.forceUpdate();
                } catch (InterruptedException e) {
                } catch (RemoteException e2) {
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                if (DataUsageList.this.isAdded()) {
                    DataUsageList.this.updateBody();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void onDestroy() {
        this.mUidDetailProvider.clearCache();
        this.mUidDetailProvider = null;
        TrafficStats.closeQuietly(this.mStatsSession);
        super.onDestroy();
    }

    private void updateBody() {
        this.mBinding = true;
        if (isAdded()) {
            Context context = getActivity();
            getLoaderManager().restartLoader(2, ChartDataLoader.buildArgs(this.mTemplate, null), this.mChartDataCallbacks);
            getActivity().invalidateOptionsMenu();
            this.mBinding = false;
            int seriesColor = context.getColor(2131427459);
            if (this.mSubId != -1) {
                SubscriptionInfo sir = this.services.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
                if (sir != null) {
                    seriesColor = sir.getIconTint();
                }
            }
            this.mChart.setColors(seriesColor, Color.argb(127, Color.red(seriesColor), Color.green(seriesColor), Color.blue(seriesColor)));
        }
    }

    private void updatePolicy(boolean refreshCycle) {
        NetworkPolicy policy = this.services.mPolicyEditor.getPolicy(this.mTemplate);
        if (isNetworkPolicyModifiable(policy, this.mSubId) && isMobileDataAvailable(this.mSubId)) {
            this.mChart.setNetworkPolicy(policy);
            this.mHeader.findViewById(2131886268).setVisibility(0);
            this.mHeader.findViewById(2131886268).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putParcelable("network_template", DataUsageList.this.mTemplate);
                    DataUsageList.this.startFragment(DataUsageList.this, BillingCycleSettings.class.getName(), 2131627146, 0, args);
                }
            });
        } else {
            this.mChart.setNetworkPolicy(null);
            this.mHeader.findViewById(2131886268).setVisibility(8);
        }
        if (refreshCycle && this.mCycleAdapter.updateCycleList(policy, this.mChartData)) {
            updateDetailData();
        }
    }

    private void updateDetailData() {
        long start = this.mChart.getInspectStart();
        long end = this.mChart.getInspectEnd();
        long now = System.currentTimeMillis();
        Context context = getActivity();
        Entry entry = null;
        if (this.mChartData != null) {
            entry = this.mChartData.network.getValues(start, end, now, null);
        }
        getLoaderManager().restartLoader(3, SummaryForAllUidLoader.buildArgs(this.mTemplate, start, end), this.mSummaryCallbacks);
        String totalPhrase = Formatter.formatFileSize(context, entry != null ? entry.rxBytes + entry.txBytes : 0);
        this.mUsageAmount.setTitle(getString(2131627151, new Object[]{totalPhrase}));
    }

    public void bindStats(NetworkStats stats, int[] restrictedUids) {
        int i;
        ArrayList<AppItem> items = new ArrayList();
        long largest = 0;
        int currentUserId = ActivityManager.getCurrentUser();
        UserManager userManager = UserManager.get(getContext());
        List<UserHandle> profiles = userManager.getUserProfiles();
        SparseArray<AppItem> knownItems = new SparseArray();
        NetworkStats.Entry entry = null;
        int size = stats != null ? stats.size() : 0;
        for (i = 0; i < size; i++) {
            int collapseKey;
            int category;
            entry = stats.getValues(i, entry);
            int uid = entry.uid;
            int userId = UserHandle.getUserId(uid);
            if (UserHandle.isApp(uid)) {
                if (profiles.contains(new UserHandle(userId))) {
                    if (userId != currentUserId) {
                        largest = accumulate(UidDetailProvider.buildKeyForUser(userId), knownItems, entry, 0, items, largest);
                    }
                    collapseKey = uid;
                    category = 2;
                } else if (userManager.getUserInfo(userId) == null) {
                    collapseKey = -4;
                    category = 2;
                } else {
                    collapseKey = UidDetailProvider.buildKeyForUser(userId);
                    category = 0;
                }
            } else if (uid == -4 || uid == -5) {
                collapseKey = uid;
                category = 2;
            } else {
                collapseKey = 1000;
                category = 2;
            }
            largest = accumulate(collapseKey, knownItems, entry, category, items, largest);
        }
        for (int uid2 : restrictedUids) {
            if (profiles.contains(new UserHandle(UserHandle.getUserId(uid2)))) {
                AppItem item = (AppItem) knownItems.get(uid2);
                if (item == null) {
                    AppItem appItem = new AppItem(uid2);
                    appItem.total = -1;
                    items.add(appItem);
                    knownItems.put(appItem.key, appItem);
                }
                item.restricted = true;
            }
        }
        Collections.sort(items);
        this.mApps.removeAll();
        for (i = 0; i < items.size(); i++) {
            Preference appDataUsagePreference = new AppDataUsagePreference(getContext(), (AppItem) items.get(i), largest != 0 ? (int) ((((AppItem) items.get(i)).total * 100) / largest) : 0, this.mUidDetailProvider);
            appDataUsagePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    DataUsageList.this.startAppDataUsage(((AppDataUsagePreference) preference).getItem());
                    return true;
                }
            });
            this.mApps.addPreference(appDataUsagePreference);
        }
    }

    private void startAppDataUsage(AppItem item) {
        Bundle args = new Bundle();
        args.putParcelable("app_item", item);
        args.putParcelable("network_template", this.mTemplate);
        startFragment(this, AppDataUsage.class.getName(), 2131626899, 0, args);
    }

    private static long accumulate(int collapseKey, SparseArray<AppItem> knownItems, NetworkStats.Entry entry, int itemCategory, ArrayList<AppItem> items, long largest) {
        int uid = entry.uid;
        AppItem item = (AppItem) knownItems.get(collapseKey);
        if (item == null) {
            item = new AppItem(collapseKey);
            item.category = itemCategory;
            items.add(item);
            knownItems.put(item.key, item);
        }
        item.addUid(uid);
        item.total += entry.rxBytes + entry.txBytes;
        return Math.max(largest, item.total);
    }

    public static boolean hasReadyMobileRadio(Context context) {
        ConnectivityManager conn = ConnectivityManager.from(context);
        TelephonyManager tele = TelephonyManager.from(context);
        List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        if (subInfoList == null) {
            return false;
        }
        boolean retVal;
        boolean isReady = true;
        for (SubscriptionInfo subInfo : subInfoList) {
            isReady &= tele.getSimState(subInfo.getSimSlotIndex()) == 5 ? 1 : 0;
        }
        if (conn.isNetworkSupported(0)) {
            retVal = isReady;
        } else {
            retVal = false;
        }
        return retVal;
    }
}
