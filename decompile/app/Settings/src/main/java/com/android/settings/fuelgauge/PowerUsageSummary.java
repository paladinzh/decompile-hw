package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.internal.os.PowerProfile;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.BatteryInfo.Callback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PowerUsageSummary extends PowerUsageSummaryHwBase {
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private PreferenceGroup mAppListGroup;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    BatteryEntry entry = msg.obj;
                    PowerGaugePreference pgp = (PowerGaugePreference) PowerUsageSummary.this.findPreference(Integer.toString(entry.sipper.uidObj.getUid()));
                    if (pgp != null) {
                        pgp.setIcon(PowerUsageSummary.this.mUm.getBadgedIconForUser(entry.getIcon(), new UserHandle(UserHandle.getUserId(entry.sipper.getUid()))));
                        pgp.setTitle(entry.name);
                        if (entry.sipper.drainType == DrainType.APP) {
                            pgp.setContentDescription(entry.name);
                            break;
                        }
                    }
                    break;
                case 2:
                    Activity activity = PowerUsageSummary.this.getActivity();
                    if (activity != null) {
                        activity.reportFullyDrawn();
                        break;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private int mStatsType = 0;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                BatteryInfo.getBatteryInfo(this.mContext, new Callback() {
                    public void onBatteryInfoLoaded(BatteryInfo info) {
                        SummaryProvider.this.mLoader.setSummary(SummaryProvider.this, info.mChargeLabelString);
                    }
                });
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setAnimationAllowed(true);
        addPreferencesFromResource(2131230834);
        this.mAppListGroup = (PreferenceGroup) findPreference("app_list");
        this.mBatteryStatusPref = findPreference("battery_status");
        try {
            getActivity().startActivity(new Intent("android.intent.action.POWER_CONSUME_LIST"));
            getActivity().finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected int getMetricsCategory() {
        return 54;
    }

    public void onResume() {
        super.onResume();
        refreshStats();
    }

    public void onPause() {
        BatteryEntry.stopRequestQueue();
        this.mHandler.removeMessages(1);
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            BatteryEntry.clearUidCache();
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof BatteryStatusPreference) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "battery_stats_on_battery");
        } else if (preference != this.mBatteryStatusPref) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "app_consuming_battery");
        }
        if (preference instanceof BatteryStatusPreference) {
            this.mStatsHelper.storeStatsHistoryInFile("tmp_bat_history.bin");
            Bundle args = new Bundle();
            args.putString("stats", "tmp_bat_history.bin");
            args.putParcelable("broadcast", this.mStatsHelper.getBatteryBroadcast());
            ((SettingsActivity) getActivity()).startPreferencePanel(BatteryHistoryDetail.class.getName(), args, 2131625972, null, null, 0);
            return super.onPreferenceTreeClick(preference);
        } else if (!(preference instanceof PowerGaugePreference)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            PowerUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(), this.mStatsHelper, this.mStatsType, ((PowerGaugePreference) preference).getInfo(), true, true);
            return super.onPreferenceTreeClick(preference);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected int getHelpResource() {
        return 2131626543;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (this.mStatsType == 0) {
                    this.mStatsType = 2;
                } else {
                    this.mStatsType = 0;
                }
                refreshStats();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNotAvailableMessage() {
        String NOT_AVAILABLE = "not_available";
        if (getCachedPreference("not_available") == null) {
            Preference notAvailable = new Preference(getPrefContext());
            notAvailable.setKey("not_available");
            notAvailable.setTitle(2131625951);
            notAvailable.setOrder(0);
            this.mAppListGroup.addPreference(notAvailable);
        }
    }

    private static boolean isSharedGid(int uid) {
        return UserHandle.getAppIdFromSharedAppGid(uid) > 0;
    }

    private static boolean isSystemUid(int uid) {
        return uid >= 1000 && uid < 10000;
    }

    private static List<BatterySipper> getCoalescedUsageList(List<BatterySipper> sippers) {
        int i;
        SparseArray<BatterySipper> uidList = new SparseArray();
        ArrayList<BatterySipper> results = new ArrayList();
        int numSippers = sippers.size();
        for (i = 0; i < numSippers; i++) {
            BatterySipper sipper = (BatterySipper) sippers.get(i);
            if (sipper.getUid() > 0) {
                int realUid = sipper.getUid();
                if (isSharedGid(sipper.getUid())) {
                    realUid = UserHandle.getUid(0, UserHandle.getAppIdFromSharedAppGid(sipper.getUid()));
                }
                if (isSystemUid(realUid) && !"mediaserver".equals(sipper.packageWithHighestDrain)) {
                    realUid = 1000;
                }
                if (realUid != sipper.getUid()) {
                    BatterySipper newSipper = new BatterySipper(sipper.drainType, new FakeUid(realUid), 0.0d);
                    newSipper.add(sipper);
                    newSipper.packageWithHighestDrain = sipper.packageWithHighestDrain;
                    newSipper.mPackages = sipper.mPackages;
                    sipper = newSipper;
                }
                int index = uidList.indexOfKey(realUid);
                if (index < 0) {
                    uidList.put(realUid, sipper);
                } else {
                    BatterySipper existingSipper = (BatterySipper) uidList.valueAt(index);
                    existingSipper.add(sipper);
                    if (existingSipper.packageWithHighestDrain == null && sipper.packageWithHighestDrain != null) {
                        existingSipper.packageWithHighestDrain = sipper.packageWithHighestDrain;
                    }
                    int existingPackageLen = existingSipper.mPackages != null ? existingSipper.mPackages.length : 0;
                    int newPackageLen = sipper.mPackages != null ? sipper.mPackages.length : 0;
                    if (newPackageLen > 0) {
                        String[] newPackages = new String[(existingPackageLen + newPackageLen)];
                        if (existingPackageLen > 0) {
                            System.arraycopy(existingSipper.mPackages, 0, newPackages, 0, existingPackageLen);
                        }
                        System.arraycopy(sipper.mPackages, 0, newPackages, existingPackageLen, newPackageLen);
                        existingSipper.mPackages = newPackages;
                    }
                }
            } else {
                results.add(sipper);
            }
        }
        int numUidSippers = uidList.size();
        for (i = 0; i < numUidSippers; i++) {
            results.add((BatterySipper) uidList.valueAt(i));
        }
        Collections.sort(results, new Comparator<BatterySipper>() {
            public int compare(BatterySipper a, BatterySipper b) {
                return Double.compare(b.totalPowerMah, a.totalPowerMah);
            }
        });
        return results;
    }

    protected void refreshStats() {
        super.refreshStats();
        deletePreference();
        PreferenceScreen root = getPreferenceScreen();
        this.mAppListGroup.setOrderingAsAdded(false);
        this.mBatteryStatusPref.setOrder(-2);
        root.addPreference(this.mBatteryStatusPref);
        BatteryStatusPreference hist = new BatteryStatusPreference(getActivity(), this.mStatsHelper.getStats());
        hist.setOrder(-1);
        root.addPreference(hist);
        boolean addedSome = false;
        PowerProfile powerProfile = this.mStatsHelper.getPowerProfile();
        BatteryStats stats = this.mStatsHelper.getStats();
        double averagePower = powerProfile.getAveragePower("screen.full");
        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(16843817, value, true);
        int colorControl = getContext().getColor(value.resourceId);
        if (averagePower >= 0.0d) {
            int dischargeAmount;
            List<BatterySipper> usageList = getCoalescedUsageList(this.mStatsHelper.getUsageList());
            if (stats != null) {
                dischargeAmount = stats.getDischargeAmount(this.mStatsType);
            } else {
                dischargeAmount = 0;
            }
            int numSippers = usageList.size();
            for (int i = 0; i < numSippers; i++) {
                BatterySipper sipper = (BatterySipper) usageList.get(i);
                if (sipper.totalPowerMah * 3600.0d >= 5.0d) {
                    double totalPower = this.mStatsHelper.getTotalPower();
                    double percentOfTotal = (sipper.totalPowerMah / totalPower) * ((double) dischargeAmount);
                    double percentOfUsedTotal = (sipper.totalPowerMah / totalPower) * 100.0d;
                    if (((int) (0.5d + percentOfUsedTotal)) >= 1 && ((sipper.drainType != DrainType.OVERCOUNTED || (sipper.totalPowerMah >= (this.mStatsHelper.getMaxRealPower() * 2.0d) / 3.0d && percentOfTotal >= 10.0d && !"user".equals(Build.TYPE))) && (sipper.drainType != DrainType.UNACCOUNTED || (sipper.totalPowerMah >= this.mStatsHelper.getMaxRealPower() / 2.0d && percentOfTotal >= 5.0d && !"user".equals(Build.TYPE))))) {
                        String key;
                        UserHandle userHandle = new UserHandle(UserHandle.getUserId(sipper.getUid()));
                        BatteryEntry entry = new BatteryEntry(getActivity(), this.mHandler, this.mUm, sipper);
                        Drawable badgedIcon = this.mUm.getBadgedIconForUser(entry.getIcon(), userHandle);
                        CharSequence contentDescription = this.mUm.getBadgedLabelForUser(entry.getLabel(), userHandle);
                        if (sipper.drainType != DrainType.APP) {
                            key = sipper.drainType.toString();
                        } else if (sipper.getPackages() != null) {
                            key = TextUtils.concat(sipper.getPackages()).toString();
                        } else {
                            key = String.valueOf(sipper.getUid());
                        }
                        Preference pref = (PowerGaugePreference) getCachedPreference(key);
                        if (pref == null) {
                            Preference powerGaugePreference = new PowerGaugePreference(getPrefContext(), badgedIcon, contentDescription, entry);
                            powerGaugePreference.setKey(key);
                        }
                        double percentOfMax = (sipper.totalPowerMah * 100.0d) / this.mStatsHelper.getMaxPower();
                        sipper.percent = percentOfTotal;
                        if (TextUtils.isEmpty(entry.getLabel())) {
                            pref.setTitle(2131624355);
                        } else {
                            pref.setTitle((CharSequence) entry.getLabel());
                        }
                        pref.setOrder(i + 1);
                        pref.setPercent(percentOfMax, percentOfUsedTotal);
                        if (sipper.uidObj != null) {
                            pref.setKey(Integer.toString(sipper.uidObj.getUid()));
                        }
                        if ((sipper.drainType != DrainType.APP || sipper.uidObj.getUid() == 0) && sipper.drainType != DrainType.USER) {
                            pref.setTint(colorControl);
                        }
                        addedSome = true;
                        this.mAppListGroup.addPreference(pref);
                        if (this.mAppListGroup.getPreferenceCount() - getCachedCount() > 11) {
                            break;
                        }
                    }
                }
            }
        }
        if (!addedSome) {
            addNotAvailableMessage();
        }
        removeCachedPrefs(this.mAppListGroup);
        BatteryEntry.startRequestQueue();
    }
}
