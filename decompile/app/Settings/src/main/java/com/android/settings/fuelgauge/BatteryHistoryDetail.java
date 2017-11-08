package com.android.settings.fuelgauge;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.SettingsExtUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.graph.UsageView;

public class BatteryHistoryDetail extends SettingsPreferenceFragment {
    private Intent mBatteryBroadcast;
    private BatteryFlagParser mCameraParser;
    private BatteryFlagParser mChargingParser;
    private BatteryFlagParser mCpuParser;
    private double mDSurplusTime;
    private BatteryFlagParser mFlashlightParser;
    private BatteryFlagParser mGpsParser;
    private BatteryCellParser mPhoneParser;
    private String mSSurplusTime;
    private BatteryFlagParser mScreenOn;
    private BatteryStats mStats;
    private BatteryStatsHelper mStatsHelper;
    private double mThresholdTime;
    private UserManager mUm;
    private BatteryWifiParser mWifiParser;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            this.mDSurplusTime = bundle.getDouble("dtime");
            this.mSSurplusTime = bundle.getString("stime");
            this.mThresholdTime = bundle.getDouble("threshold_time");
        }
        Context context = getActivity();
        this.mUm = (UserManager) getActivity().getSystemService("user");
        this.mStatsHelper = new BatteryStatsHelper(context, true);
        this.mStatsHelper.create((Bundle) null);
        this.mStats = this.mStatsHelper.getStats();
        this.mStatsHelper.refreshStats(0, this.mUm.getUserProfiles());
        this.mBatteryBroadcast = this.mStatsHelper.getBatteryBroadcast();
        setHasOptionsMenu(true);
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(16843829, value, true);
        int accentColor = getContext().getColor(value.resourceId);
        this.mChargingParser = new BatteryFlagParser(accentColor, false, 524288);
        this.mScreenOn = new BatteryFlagParser(accentColor, false, 1048576);
        this.mGpsParser = new BatteryFlagParser(accentColor, false, 536870912);
        this.mFlashlightParser = new BatteryFlagParser(accentColor, true, 134217728);
        this.mCameraParser = new BatteryFlagParser(accentColor, true, 2097152);
        this.mWifiParser = new BatteryWifiParser(accentColor);
        this.mCpuParser = new BatteryFlagParser(accentColor, false, Integer.MIN_VALUE);
        this.mPhoneParser = new BatteryCellParser();
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(2130968645, container, false);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 2, 0, 2131628113).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), 2130838228)).setAlphabeticShortcut('r').setShowAsAction(1);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                try {
                    getActivity().startActivity(new Intent("android.intent.action.POWER_CONSUME_LIST"));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initActionAndStatusBar() {
        getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(2131427522));
        Window win = getActivity().getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        win.setStatusBarColor(getResources().getColor(2131427522));
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) getActivity().findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(-1);
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateEverything();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initActionAndStatusBar();
    }

    private void updateEverything() {
        BatteryInfo info = BatteryInfo.getBatteryInfo(getContext(), this.mBatteryBroadcast, this.mStats, SystemClock.elapsedRealtime() * 1000);
        View view = getView();
        info.bindHistory((UsageView) view.findViewById(2131886278), this.mChargingParser, this.mScreenOn, this.mGpsParser, this.mFlashlightParser, this.mCameraParser, this.mWifiParser, this.mCpuParser, this.mPhoneParser);
        ((TextView) view.findViewById(2131886276)).setText(info.batteryPercentString);
        ((TextView) view.findViewById(2131886277)).setText(info.remainingLabel);
        bindData(this.mChargingParser, 2131625960, 2131886286);
        bindData(this.mScreenOn, 2131625961, 2131886285);
        bindData(this.mGpsParser, 2131625962, 2131886282);
        bindData(this.mFlashlightParser, 2131625964, 2131886281);
        bindData(this.mCameraParser, 2131625963, 2131886280);
        bindData(this.mWifiParser, 2131625965, 2131886283);
        bindData(this.mCpuParser, 2131625966, 2131886284);
        bindData(this.mPhoneParser, 2131625967, 2131886279);
    }

    private void bindData(BatteryActiveProvider provider, int label, int groupId) {
        View group = getView().findViewById(groupId);
        group.setVisibility(provider.hasData() ? 0 : 8);
        ((TextView) group.findViewById(16908310)).setText(label);
        ((BatteryActiveView) group.findViewById(2131886274)).setProvider(provider);
    }

    protected int getMetricsCategory() {
        return 51;
    }
}
