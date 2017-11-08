package com.android.settings.datausage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.SummaryPreference;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.Utils;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import java.util.ArrayList;
import java.util.List;

public class DataUsageSummary extends DataUsageBase implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> resources = new ArrayList();
            SearchIndexableResource resource = new SearchIndexableResource(context);
            resource.xmlResId = 2131230758;
            resources.add(resource);
            if (DataUsageSummary.hasMobileData(context)) {
                resource = new SearchIndexableResource(context);
                resource.xmlResId = 2131230759;
                resources.add(resource);
            }
            if (DataUsageSummary.hasWifiRadio(context)) {
                resource = new SearchIndexableResource(context);
                resource.xmlResId = 2131230763;
                resources.add(resource);
            }
            return resources;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> keys = new ArrayList();
            if (ConnectivityManager.from(context).isNetworkSupported(0)) {
                keys.add("restrict_background");
            }
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private DataUsageController mDataUsageController;
    private int mDataUsageTemplate;
    private NetworkTemplate mDefaultTemplate;
    private Preference mLimitPreference;
    private SummaryPreference mSummaryPreference;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Activity mActivity;
        private final DataUsageController mDataController;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            this.mActivity = activity;
            this.mSummaryLoader = summaryLoader;
            this.mDataController = new DataUsageController(activity);
        }

        public void setListening(boolean listening) {
            if (listening) {
                String used;
                DataUsageInfo info = this.mDataController.getDataUsageInfo();
                if (info == null) {
                    used = Formatter.formatFileSize(this.mActivity, 0);
                } else if (info.limitLevel <= 0) {
                    used = Formatter.formatFileSize(this.mActivity, info.usageLevel);
                } else {
                    used = Utils.formatPercentage(info.usageLevel, info.limitLevel);
                }
                this.mSummaryLoader.setSummary(this, this.mActivity.getString(2131627092, new Object[]{used}));
            }
        }
    }

    public void onCreate(Bundle icicle) {
        int i;
        super.onCreate(icicle);
        boolean hasMobileData = hasMobileData(getContext());
        this.mDataUsageController = new DataUsageController(getContext());
        addPreferencesFromResource(2131230758);
        int defaultSubId = getDefaultSubscriptionId(getContext());
        if (defaultSubId == -1) {
            hasMobileData = false;
        }
        this.mDefaultTemplate = getDefaultTemplate(getContext(), defaultSubId);
        if (hasMobileData) {
            this.mLimitPreference = findPreference("limit_summary");
        } else {
            removePreference("limit_summary");
        }
        if (!(hasMobileData && isAdmin())) {
            removePreference("restrict_background");
        }
        if (hasMobileData) {
            List<SubscriptionInfo> subscriptions = this.services.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions == null || subscriptions.size() == 0) {
                addMobileSection(defaultSubId);
            }
            int i2 = 0;
            while (subscriptions != null && i2 < subscriptions.size()) {
                addMobileSection(((SubscriptionInfo) subscriptions.get(i2)).getSubscriptionId());
                i2++;
            }
        }
        boolean hasWifiRadio = hasWifiRadio(getContext());
        if (hasWifiRadio) {
            addWifiSection();
        }
        if (hasEthernet(getContext())) {
            addEthernetSection();
        }
        if (hasMobileData) {
            i = 2131627141;
        } else if (hasWifiRadio) {
            i = 2131627142;
        } else {
            i = 2131627143;
        }
        this.mDataUsageTemplate = i;
        this.mSummaryPreference = (SummaryPreference) findPreference("status_header");
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (UserManager.get(getContext()).isAdminUser()) {
            inflater.inflate(2132017153, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2131887641:
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    private void addMobileSection(int subId) {
        TemplatePreferenceCategory category = (TemplatePreferenceCategory) inflatePreferences(2131230759);
        category.setTemplate(getNetworkTemplate(subId), subId, this.services);
        category.pushTemplates(this.services);
    }

    private void addWifiSection() {
        ((TemplatePreferenceCategory) inflatePreferences(2131230763)).setTemplate(NetworkTemplate.buildTemplateWifiWildcard(), 0, this.services);
    }

    private void addEthernetSection() {
        ((TemplatePreferenceCategory) inflatePreferences(2131230760)).setTemplate(NetworkTemplate.buildTemplateEthernet(), 0, this.services);
    }

    private Preference inflatePreferences(int resId) {
        PreferenceScreen rootPreferences = getPreferenceManager().inflateFromResource(getPrefContext(), resId, null);
        Preference pref = rootPreferences.getPreference(0);
        rootPreferences.removeAll();
        PreferenceScreen screen = getPreferenceScreen();
        pref.setOrder(screen.getPreferenceCount());
        screen.addPreference(pref);
        return pref;
    }

    private NetworkTemplate getNetworkTemplate(int subscriptionId) {
        return NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(this.services.mTelephonyManager.getSubscriberId(subscriptionId)), this.services.mTelephonyManager.getMergedSubscriberIds());
    }

    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        DataUsageInfo info = this.mDataUsageController.getDataUsageInfo(this.mDefaultTemplate);
        Context context = getContext();
        if (this.mSummaryPreference != null) {
            this.mSummaryPreference.setAmount(Formatter.formatBytes(context.getResources(), info.usageLevel, 1).value);
            this.mSummaryPreference.setUnits(getString(this.mDataUsageTemplate, new Object[]{usedResult.units}));
            long limit = info.limitLevel;
            if (limit <= 0) {
                limit = info.warningLevel;
            }
            if (info.usageLevel > limit) {
                limit = info.usageLevel;
            }
            this.mSummaryPreference.setSummary(info.period);
            this.mSummaryPreference.setLabels(Formatter.formatFileSize(context, 0), Formatter.formatFileSize(context, limit));
            this.mSummaryPreference.setRatios(((float) info.usageLevel) / ((float) limit), 0.0f, ((float) (limit - info.usageLevel)) / ((float) limit));
        }
        if (this.mLimitPreference != null) {
            int i;
            String warning = Formatter.formatFileSize(context, info.warningLevel);
            String limit2 = Formatter.formatFileSize(context, info.limitLevel);
            Preference preference = this.mLimitPreference;
            if (info.limitLevel <= 0) {
                i = 2131627144;
            } else {
                i = 2131627145;
            }
            preference.setSummary(getString(i, new Object[]{warning, limit2}));
        }
        PreferenceScreen screen = getPreferenceScreen();
        for (int i2 = 1; i2 < screen.getPreferenceCount(); i2++) {
            ((TemplatePreferenceCategory) screen.getPreference(i2)).pushTemplates(this.services);
        }
    }

    protected int getMetricsCategory() {
        return 37;
    }

    public boolean hasEthernet(Context context) {
        boolean hasEthernet = ConnectivityManager.from(context).isNetworkSupported(9);
        try {
            INetworkStatsSession statsSession = this.services.mStatsService.openSession();
            long ethernetBytes;
            if (statsSession != null) {
                ethernetBytes = statsSession.getSummaryForNetwork(NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE).getTotalBytes();
                TrafficStats.closeQuietly(statsSession);
            } else {
                ethernetBytes = 0;
            }
            if (!hasEthernet || ethernetBytes <= 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasMobileData(Context context) {
        return ConnectivityManager.from(context).isNetworkSupported(0);
    }

    public static boolean hasWifiRadio(Context context) {
        return ConnectivityManager.from(context).isNetworkSupported(1);
    }

    public static int getDefaultSubscriptionId(Context context) {
        SubscriptionManager subManager = SubscriptionManager.from(context);
        if (subManager == null) {
            return -1;
        }
        SubscriptionInfo subscriptionInfo = subManager.getDefaultDataSubscriptionInfo();
        if (subscriptionInfo == null) {
            List<SubscriptionInfo> list = subManager.getAllSubscriptionInfoList();
            if (list.size() == 0) {
                return -1;
            }
            subscriptionInfo = (SubscriptionInfo) list.get(0);
        }
        return subscriptionInfo.getSubscriptionId();
    }

    public static NetworkTemplate getDefaultTemplate(Context context, int defaultSubId) {
        if (hasMobileData(context) && defaultSubId != -1) {
            TelephonyManager telephonyManager = TelephonyManager.from(context);
            return NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(telephonyManager.getSubscriberId(defaultSubId)), telephonyManager.getMergedSubscriberIds());
        } else if (hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        } else {
            return NetworkTemplate.buildTemplateEthernet();
        }
    }
}
