package com.android.settings.dashboard;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.InstrumentedFragment;
import com.android.settings.ItemUseStat;
import com.android.settings.Settings.BluetoothSettingsActivity;
import com.android.settings.Settings.DataUsageSummaryActivity;
import com.android.settings.Settings.ManageApplicationsActivity;
import com.android.settings.Settings.PowerUsageSummaryActivity;
import com.android.settings.Settings.StorageSettingsActivity;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.DashboardAdapter.SearchViewClickListener;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionAdapterUtils;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.dashboard.conditional.ConditionManager.ConditionListener;
import com.android.settings.dashboard.conditional.FocusRecyclerView;
import com.android.settings.dashboard.conditional.FocusRecyclerView.FocusListener;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.SuggestionParser;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.android.settingslib.drawer.SettingsDrawerActivity.CategoryListener;
import com.android.settingslib.drawer.Tile;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class DashboardSummary extends InstrumentedFragment implements CategoryListener, ConditionListener, FocusListener, SearchViewClickListener {
    public static final String[] INITIAL_ITEMS = new String[]{WifiSettingsActivity.class.getName(), BluetoothSettingsActivity.class.getName(), DataUsageSummaryActivity.class.getName(), PowerUsageSummaryActivity.class.getName(), ManageApplicationsActivity.class.getName(), StorageSettingsActivity.class.getName()};
    private DashboardAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DashboardSummary.this.mConditionManager.refreshAll();
            DashboardSummary.this.rebuildUI();
        }
    };
    private ConditionManager mConditionManager;
    private FocusRecyclerView mDashboard;
    private ContentObserver mHiCloudObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DashboardSummary.this.mAdapter.setHiCloudSummary();
            DashboardSummary.this.mAdapter.notifyDataSetChanged();
        }
    };
    private HwCustSplitUtils mHwCustSplitUtils;
    private ContentObserver mHwTrustSpaceObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DashboardSummary.this.mAdapter.setHwTrustSpaceSummary();
            DashboardSummary.this.mAdapter.notifyDataSetChanged();
        }
    };
    private LinearLayoutManager mLayoutManager;
    private ContentObserver mMobileDataObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DashboardSummary.this.mConditionManager.refreshAll();
            DashboardSummary.this.rebuildUI();
        }
    };
    private SuggestionParser mSuggestionParser;
    private SuggestionsChecks mSuggestionsChecks;
    private SummaryLoader mSummaryLoader;
    private TileCacheListener mTileCacheListener = new TileCacheListener();
    private ContentObserver mZenModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DashboardSummary.this.handleZenModeChange();
        }
    };

    private class SuggestionLoader extends AsyncTask<Void, Void, List<Tile>> {
        private SuggestionLoader() {
        }

        protected List<Tile> doInBackground(Void... params) {
            List<Tile> suggestions = DashboardSummary.this.mSuggestionParser.getSuggestions();
            int i = 0;
            while (i < suggestions.size()) {
                if (DashboardSummary.this.mSuggestionsChecks.isSuggestionComplete((Tile) suggestions.get(i))) {
                    DashboardSummary.this.mAdapter.disableSuggestion((Tile) suggestions.get(i));
                    int i2 = i - 1;
                    suggestions.remove(i);
                    i = i2;
                }
                i++;
            }
            return suggestions;
        }

        protected void onPostExecute(List<Tile> tiles) {
            if (DashboardSummary.this.getActivity() != null) {
                DashboardSummary.this.mAdapter.setSuggestions(tiles);
                ((SettingsActivity) DashboardSummary.this.getActivity()).setSpliterLineVis(true);
            }
        }
    }

    public class TileCacheListener implements com.android.settingslib.drawer.SettingsDrawerActivity.TileCacheListener {
        public void onTileCacheChanged() {
            if (DashboardSummary.this.getView() != null && DashboardSummary.this.mSummaryLoader != null) {
                DashboardSummary.this.getView().post(new Runnable() {
                    public void run() {
                        DashboardSummary.this.mSummaryLoader.setListening(false);
                    }
                });
            }
        }
    }

    protected int getMetricsCategory() {
        return 35;
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        long startTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        this.mSummaryLoader = new SummaryLoader(getActivity(), ((SettingsActivity) getActivity()).getDashboardCategories());
        setHasOptionsMenu(true);
        Context context = getContext();
        this.mConditionManager = ConditionManager.get(context, false);
        this.mSuggestionParser = new SuggestionParser(context, context.getSharedPreferences("suggestions", 0), 2131230905);
        this.mSuggestionsChecks = new SuggestionsChecks(getContext());
        ((SettingsActivity) getActivity()).addTileCacheListener(this.mTileCacheListener);
    }

    public void onDestroy() {
        ((SettingsActivity) getActivity()).remTileCacheListener(this.mTileCacheListener);
        this.mSummaryLoader.unregisterReceivers();
        this.mSummaryLoader.release();
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null) {
            HelpUtils.prepareHelpMenuItem(getActivity(), menu, 2131626523, getClass().getName());
        }
    }

    public void onStart() {
        if (this.mHwCustSplitUtils.reachSplitSize()) {
            this.mAdapter.getmSplitSelector().updatetCurrentTileByIntent(this.mHwCustSplitUtils.getCurrentSubIntent());
            if (this.mHwCustSplitUtils.getCurrentSubIntent() != null && getActivity().hasWindowFocus()) {
                getActivity().startActivity(this.mHwCustSplitUtils.getCurrentSubIntent());
            }
        }
        long startTime = System.currentTimeMillis();
        super.onStart();
        ((SettingsDrawerActivity) getActivity()).addCategoryListener(this);
        this.mSummaryLoader.setListening(true);
        this.mConditionManager.addListener(this);
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                MetricsLogger.visible(getContext(), c.getMetricsConstant());
            }
        }
        if (this.mAdapter.getSuggestions() != null) {
            for (Tile suggestion : this.mAdapter.getSuggestions()) {
                MetricsLogger.action(getContext(), 384, DashboardAdapter.getSuggestionIdentifier(getContext(), suggestion));
            }
        }
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://com.huawei.android.hicloud.SwitchStatusProvider/hicloud"), true, this.mHiCloudObserver);
        getContext().getContentResolver().registerContentObserver(System.getUriFor("trust_space_switch"), true, this.mHwTrustSpaceObserver);
        this.mAdapter.resumeAllSwitchEnabler();
        this.mAdapter.resumeUpdateEnabler();
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver, -1);
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("zen_mode"), true, this.mZenModeObserver, -1);
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mMobileDataObserver, -1);
    }

    public void onStop() {
        super.onStop();
        this.mAdapter.pauseAllSwitchEnabler();
        this.mAdapter.pauseUpdateEnabler();
        getContext().getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        getContext().getContentResolver().unregisterContentObserver(this.mZenModeObserver);
        getContext().getContentResolver().unregisterContentObserver(this.mMobileDataObserver);
        ((SettingsDrawerActivity) getActivity()).remCategoryListener(this);
        this.mSummaryLoader.setListening(false);
        this.mConditionManager.remListener(this);
        getActivity().getContentResolver().unregisterContentObserver(this.mHiCloudObserver);
        getActivity().getContentResolver().unregisterContentObserver(this.mHwTrustSpaceObserver);
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                MetricsLogger.hidden(getContext(), c.getMetricsConstant());
            }
        }
        if (this.mAdapter.getSuggestions() != null) {
            for (Tile suggestion : this.mAdapter.getSuggestions()) {
                MetricsLogger.action(getContext(), 385, DashboardAdapter.getSuggestionIdentifier(getContext(), suggestion));
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        long startTime = System.currentTimeMillis();
        if (hasWindowFocus) {
            this.mConditionManager.addListener(this);
            getView().postDelayed(new Runnable() {
                public void run() {
                    DashboardSummary.this.mConditionManager.refreshAll();
                }
            }, 500);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(2130968711, container, false);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mLayoutManager != null) {
            outState.putInt("scroll_position", this.mLayoutManager.findFirstVisibleItemPosition());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mAdapter.getmSplitSelector().updateSplitMode();
        super.onConfigurationChanged(newConfig);
        ((SettingsActivity) getActivity()).setSpliterLineVis(this.mHwCustSplitUtils.reachSplitSize());
    }

    public void onViewCreated(View view, Bundle bundle) {
        long startTime = System.currentTimeMillis();
        this.mDashboard = (FocusRecyclerView) view.findViewById(2131886436);
        this.mLayoutManager = new LinearLayoutManager(getContext());
        this.mLayoutManager.setOrientation(1);
        if (bundle != null) {
            this.mLayoutManager.scrollToPosition(bundle.getInt("scroll_position"));
        }
        this.mDashboard.setLayoutManager(this.mLayoutManager);
        this.mDashboard.setHasFixedSize(true);
        this.mDashboard.setListener(this);
        this.mDashboard.setItemAnimator(null);
        this.mDashboard.addItemDecoration(new DashboardDecorator(getContext()));
        this.mAdapter = new DashboardAdapter(getActivity(), getContext(), this.mSuggestionParser, this);
        this.mAdapter.setConditions(this.mConditionManager.getConditions());
        this.mDashboard.setAdapter(this.mAdapter);
        this.mSummaryLoader.setAdapter(this.mAdapter);
        ConditionAdapterUtils.addDismiss(this.mDashboard);
        rebuildUI();
    }

    private void rebuildUI() {
        if (isAdded()) {
            this.mAdapter.setCategories(((SettingsActivity) getActivity()).getDashboardCategories());
            this.mSummaryLoader.setListening(true);
            new SuggestionLoader().execute(new Void[0]);
            return;
        }
        Log.w("DashboardSummary", "Cannot build the DashboardSummary UI yet as the Fragment is not added");
    }

    private void switchToSearchResult() {
        ((SettingsActivity) getActivity()).switchToSearchResult();
    }

    public void onCategoriesChanged() {
        rebuildUI();
    }

    public void onConditionsChanged() {
        Log.d("DashboardSummary", "onConditionsChanged");
        this.mAdapter.setConditions(this.mConditionManager.getConditions());
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        this.mAdapter.getmSplitSelector().updateSplitMode();
    }

    private void handleZenModeChange() {
        this.mConditionManager.refreshAll();
        rebuildUI();
        boolean isON = Global.getInt(getContext().getContentResolver(), "zen_mode", 0) != 0;
        if (this.mSummaryLoader != null) {
            SummaryProvider p = this.mSummaryLoader.getZenModeProvider();
            if (p != null) {
                int i;
                SummaryLoader summaryLoader = this.mSummaryLoader;
                Context context = getContext();
                if (isON) {
                    i = 2131625876;
                } else {
                    i = 2131625877;
                }
                summaryLoader.setSummary(p, context.getString(i));
            }
        }
    }

    public void onSearchViewClicked() {
        if (this.mHwCustSplitUtils.reachSplitSize()) {
            this.mHwCustSplitUtils.finishAllSubActivities();
        }
        switchToSearchResult();
        ItemUseStat.getInstance().handleClick(getActivity(), 1, "search_view_clicked");
    }
}
