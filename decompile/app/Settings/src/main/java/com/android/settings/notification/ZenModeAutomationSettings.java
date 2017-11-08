package com.android.settings.notification;

import android.app.Activity;
import android.app.AutomaticZenRule;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.service.notification.ZenModeConfig.ZenRule;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.utils.ManagedServiceSettings.Config;
import com.android.settings.utils.ZenServiceListing;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class ZenModeAutomationSettings extends ZenModeSettingsMain implements Indexable {
    static final Config CONFIG = getConditionProviderConfig();
    private static final Comparator<Entry<String, AutomaticZenRule>> RULE_COMPARATOR = new Comparator<Entry<String, AutomaticZenRule>>() {
        public int compare(Entry<String, AutomaticZenRule> lhs, Entry<String, AutomaticZenRule> rhs) {
            return key((AutomaticZenRule) lhs.getValue()).compareTo(key((AutomaticZenRule) rhs.getValue()));
        }

        private String key(AutomaticZenRule rule) {
            int type;
            if (ZenModeConfig.isValidScheduleConditionId(rule.getConditionId())) {
                type = 1;
            } else if (ZenModeConfig.isValidEventConditionId(rule.getConditionId())) {
                type = 2;
            } else {
                type = 3;
            }
            return type + rule.getName().toString();
        }
    };
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!Utils.isOwnerUser()) {
                return null;
            }
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230949;
            result.add(sir);
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private final Calendar mCalendar = Calendar.getInstance();
    private final SimpleDateFormat mDayFormat = new SimpleDateFormat("EEE");
    private PackageManager mPm;
    private ZenServiceListing mServiceListing;
    ZenModeEventProvider mZenModeEventProvider;
    private ZenRuleNameDialog mZenRuleNameDialog;

    private class LoadIconTask extends AsyncTask<ApplicationInfo, Void, Drawable> {
        private final WeakReference<Preference> prefReference;

        public LoadIconTask(Preference pref) {
            this.prefReference = new WeakReference(pref);
        }

        protected Drawable doInBackground(ApplicationInfo... params) {
            return params[0].loadIcon(ZenModeAutomationSettings.this.mPm);
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            int i;
            boolean isON = Global.getInt(this.mContext.getContentResolver(), "zen_mode", 0) != 0;
            SummaryLoader summaryLoader = this.mLoader;
            Context context = this.mContext;
            if (isON) {
                i = 2131625876;
            } else {
                i = 2131625877;
            }
            summaryLoader.setSummary(this, context.getString(i));
        }
    }

    private class ZenRulePreference extends Preference {
        final boolean appExists;
        private EventInfo mEvent;
        final String mId;
        final CharSequence mName;
        private AutomaticZenRule mRule;
        private int mRuleType;
        private ScheduleInfo mSchedule;
        private final ZenRuleChangeListener mSwitchListener = new ZenRuleChangeListener();
        private int mTitleId;

        private class ZenRuleChangeListener implements OnCheckedChangeListener {
            private ZenRuleChangeListener() {
            }

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ZenModeAutomationSettings.this.clickSwitch(ZenRulePreference.this.mRule, ZenRulePreference.this.mId, isChecked);
            }
        }

        public ZenRulePreference(Context context, Entry<String, AutomaticZenRule> ruleEntry) {
            super(context);
            AutomaticZenRule rule = (AutomaticZenRule) ruleEntry.getValue();
            this.mRule = rule;
            this.mName = rule.getName();
            this.mId = (String) ruleEntry.getKey();
            boolean isSchedule = ZenModeConfig.isValidScheduleConditionId(rule.getConditionId());
            boolean isEvent = ZenModeConfig.isValidEventConditionId(rule.getConditionId());
            boolean z = !isSchedule ? isEvent : true;
            if (isSchedule) {
                this.mTitleId = 2131628864;
                this.mRuleType = 1;
            }
            if (isEvent) {
                this.mTitleId = 2131628865;
                this.mRuleType = 2;
            }
            try {
                ApplicationInfo info = ZenModeAutomationSettings.this.mPm.getApplicationInfo(rule.getOwner().getPackageName(), 0);
                new LoadIconTask(this).execute(new ApplicationInfo[]{info});
                this.appExists = true;
                setPersistent(false);
                String action = isSchedule ? "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS" : isEvent ? "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS" : "";
                ComponentName settingsActivity = ZenModeAutomationSettings.getSettingsActivity(ZenModeAutomationSettings.this.mServiceListing.findService(rule.getOwner()));
                setIntent(ZenModeAutomationSettings.this.getRuleIntent(action, settingsActivity, this.mId));
                if (settingsActivity != null) {
                    z = true;
                }
                setSelectable(z);
            } catch (NameNotFoundException e) {
                this.appExists = false;
            }
        }

        public void setInterruption(int zenmode) {
            if (this.mRule != null && this.mRule.isEnabled() && this.mRule.getInterruptionFilter() != ZenModeAutomationSettings.this.zenModeFromInterruptionFilter(zenmode)) {
                this.mRule.setInterruptionFilter(ZenModeAutomationSettings.this.zenModeFromInterruptionFilter(zenmode));
                ZenModeAutomationSettings.this.setZenRule(this.mId, this.mRule);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateRuleView(PreferenceViewHolder view) {
            ImageView imageView = (ImageView) view.findViewById(2131886972);
            TextView titleTxtView = (TextView) view.findViewById(2131886974);
            TextView sum1TxtView = (TextView) view.findViewById(2131886975);
            TextView sum2TxtView = (TextView) view.findViewById(2131886976);
            Switch swithView = (Switch) view.findViewById(2131886977);
            if (imageView != null && titleTxtView != null && swithView != null && sum1TxtView != null && sum2TxtView != null && this.mRule != null && ZenModeAutomationSettings.this.mContext != null) {
                int i;
                titleTxtView.setText(ZenModeAutomationSettings.this.mContext.getString(this.mTitleId));
                setRuleSummary(titleTxtView, sum1TxtView, sum2TxtView);
                swithView.setOnCheckedChangeListener(null);
                swithView.setChecked(this.mRule.isEnabled());
                swithView.setOnCheckedChangeListener(this.mSwitchListener);
                boolean isEvent = ZenModeConfig.isValidEventConditionId(this.mRule.getConditionId());
                boolean isSchedule = ZenModeConfig.isValidScheduleConditionId(this.mRule.getConditionId());
                Resources resources = ZenModeAutomationSettings.this.mContext.getResources();
                if (isSchedule) {
                    i = 2130838330;
                } else if (isEvent) {
                    i = 2130838236;
                } else {
                    i = 2130838253;
                }
                imageView.setImageDrawable(resources.getDrawable(i));
            }
        }

        private void setRuleSummary(TextView titleTxtView, TextView sum1View, TextView sum2View) {
            EventInfo eventInfo = null;
            if (this.mRuleType == 1) {
                ScheduleInfo tryParseScheduleConditionId;
                if (this.mRule != null) {
                    tryParseScheduleConditionId = ZenModeConfig.tryParseScheduleConditionId(this.mRule.getConditionId());
                } else {
                    tryParseScheduleConditionId = null;
                }
                this.mSchedule = tryParseScheduleConditionId;
                if (this.mSchedule != null) {
                    updateDays(sum1View);
                    updateTimes(sum2View);
                    sum2View.setVisibility(0);
                } else {
                    return;
                }
            }
            if (this.mRuleType == 2) {
                if (this.mRule != null) {
                    eventInfo = ZenModeConfig.tryParseEventConditionId(this.mRule.getConditionId());
                }
                this.mEvent = eventInfo;
                if (this.mEvent != null) {
                    ZenModeEventProvider zenModeEventProvider = ZenModeAutomationSettings.this.mZenModeEventProvider;
                    sum1View.setText(ZenModeEventProvider.getEventType(ZenModeAutomationSettings.this.mContext, this.mEvent));
                    sum2View.setVisibility(8);
                }
            }
        }

        private void updateTimes(TextView sum2View) {
            String summary;
            if (isNextday()) {
                summary = ZenModeAutomationSettings.this.mContext.getResources().getString(2131628903, new Object[]{getStartTime(), getEndTime()});
            } else {
                summary = ZenModeAutomationSettings.this.mContext.getResources().getString(2131628902, new Object[]{getStartTime(), getEndTime()});
            }
            sum2View.setText(summary);
        }

        private String getStartTime() {
            Calendar c = Calendar.getInstance();
            c.set(11, this.mSchedule.startHour);
            c.set(12, this.mSchedule.startMinute);
            return DateFormat.getTimeFormat(ZenModeAutomationSettings.this.mContext).format(c.getTime());
        }

        private String getEndTime() {
            Calendar c = Calendar.getInstance();
            c.set(11, this.mSchedule.endHour);
            c.set(12, this.mSchedule.endMinute);
            return DateFormat.getTimeFormat(ZenModeAutomationSettings.this.mContext).format(c.getTime());
        }

        private boolean isNextday() {
            return (this.mSchedule.startHour * 60) + this.mSchedule.startMinute >= (this.mSchedule.endHour * 60) + this.mSchedule.endMinute;
        }

        private void updateDays(TextView sum1View) {
            int[] days = this.mSchedule.days;
            if (days != null && days.length > 0) {
                StringBuilder sb = new StringBuilder();
                Calendar c = Calendar.getInstance();
                for (int i = 0; i < ZenModeScheduleDaysSelection.DAYS.length; i++) {
                    int day = ZenModeScheduleDaysSelection.DAYS[i];
                    if (!SettingsExtUtils.isGlobalVersion()) {
                        day = ZenModeScheduleDaysSelection.CHINA_DAYS[i];
                    }
                    int j = 0;
                    while (j < days.length) {
                        if (day == days[j]) {
                            c.set(7, day);
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(ZenModeAutomationSettings.this.mDayFormat.format(c.getTime()));
                        } else {
                            j++;
                        }
                    }
                }
                if (sb.length() > 0) {
                    sum1View.setText(sb);
                    return;
                }
            }
            sum1View.setText(2131626807);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            updateRuleView(view);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = this.mContext.getPackageManager();
        this.mServiceListing = new ZenServiceListing(this.mContext, CONFIG);
        this.mServiceListing.reloadApprovedServices();
        this.mZenModeEventProvider = ZenModeEventProvider.getInstance();
        setHasOptionsMenu(true);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mZenRuleNameDialog != null && this.mZenRuleNameDialog.isShowing()) {
            this.mZenRuleNameDialog.dismiss();
        }
    }

    protected void onZenModeChanged() {
        super.onZenModeChanged();
        updateControls();
    }

    protected void onZenModeConfigChanged() {
        super.onZenModeConfigChanged();
        updateControls();
    }

    public void onResume() {
        super.onResume();
        if (!isUiRestricted()) {
            updateControls();
        }
    }

    private void showAddRuleDialog() {
        if (this.mZenRuleNameDialog != null && this.mZenRuleNameDialog.isShowing()) {
            this.mZenRuleNameDialog.dismiss();
        }
        this.mZenRuleNameDialog = new ZenRuleNameDialog(this.mContext, this.mServiceListing, null, null) {
            public void onOk(String ruleName, ZenRuleInfo ri) {
                if (ri == null) {
                    Log.e("ZenModeSettingsMain", "Zen rule is null!");
                    return;
                }
                MetricsLogger.action(ZenModeAutomationSettings.this.mContext, 173);
                AutomaticZenRule rule = new AutomaticZenRule(ri.title, ri.serviceComponent, ri.defaultConditionId, 2, true);
                rule.setInterruptionFilter(ZenModeAutomationSettings.this.mLastZenMode);
                rule.setEnabled(true);
                String savedRuleId = ZenModeAutomationSettings.this.addZenRule(rule);
                if (savedRuleId != null && ZenModeAutomationSettings.this.setZenRule(savedRuleId, rule)) {
                    ZenModeAutomationSettings.this.startActivity(ZenModeAutomationSettings.this.getRuleIntent(ri.settingsAction, null, savedRuleId));
                }
                ItemUseStat.getInstance().handleClick(ZenModeAutomationSettings.this.getActivity(), 2, "add_automation_rule", ri.settingsAction);
            }
        };
        this.mZenRuleNameDialog.show();
    }

    private Intent getRuleIntent(String settingsAction, ComponentName configurationActivity, String ruleId) {
        Intent intent = new Intent().addFlags(67108864).putExtra("android.service.notification.extra.RULE_ID", ruleId);
        if (configurationActivity != null) {
            intent.setComponent(configurationActivity);
        } else {
            intent.setAction(settingsAction);
        }
        return intent;
    }

    private Entry<String, AutomaticZenRule>[] sortedRules() {
        Entry[] rt = (Entry[]) this.mRules.toArray(new Entry[this.mRules.size()]);
        Arrays.sort(rt, RULE_COMPARATOR);
        return rt;
    }

    protected void updateControls() {
        super.updateControls();
        PreferenceCategory root = (PreferenceCategory) getPreferenceScreen().findPreference("auto_rules");
        root.removeAll();
        getLastZenMode();
        Entry<String, AutomaticZenRule>[] sortedRules = sortedRules();
        boolean isManualZenconfig = false;
        ZenModeConfig config = getZenModeConfig();
        if (config != null) {
            ZenRule zenRule = config.manualRule;
            if (zenRule != null) {
                isManualZenconfig = zenRule.zenMode != 0;
            }
        }
        for (Entry<String, AutomaticZenRule> sortedRule : sortedRules) {
            ZenRulePreference pref = new ZenRulePreference(getPrefContext(), sortedRule);
            if (pref.appExists) {
                AutomaticZenRule rule = (AutomaticZenRule) sortedRule.getValue();
                boolean isSchedule = ZenModeConfig.isValidScheduleConditionId(rule.getConditionId());
                boolean isEvent = ZenModeConfig.isValidEventConditionId(rule.getConditionId());
                pref.setLayoutResource(2130969017);
                pref.setKey("zen_automation_rule_" + (isSchedule ? "schedule" : "event"));
                pref.setInterruption(this.mLastZenMode);
                pref.setEnabled(!isManualZenconfig);
                root.addPreference(pref);
            }
        }
        Preference p = new Preference(getPrefContext());
        p.setLayoutResource(2130969016);
        p.setKey("zen_add_rule");
        p.setTitle(2131628867);
        p.setEnabled(!isManualZenconfig);
        p.setPersistent(false);
        p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                MetricsLogger.action(ZenModeAutomationSettings.this.mContext, 172);
                ZenModeAutomationSettings.this.showAddRuleDialog();
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(ZenModeAutomationSettings.this.getActivity(), preference);
                return true;
            }
        });
        root.addPreference(p);
    }

    protected int getMetricsCategory() {
        return 142;
    }

    private static Config getConditionProviderConfig() {
        Config c = new Config();
        c.tag = "ZenModeSettingsMain";
        c.setting = "enabled_notification_policy_access_packages";
        c.secondarySetting = "enabled_notification_listeners";
        c.intentAction = "android.service.notification.ConditionProviderService";
        c.permission = "android.permission.BIND_CONDITION_PROVIDER_SERVICE";
        c.noun = "condition provider";
        return c;
    }

    public static ZenRuleInfo getRuleInfo(PackageManager pm, ServiceInfo si) {
        if (si == null || si.metaData == null) {
            return null;
        }
        String ruleType = si.metaData.getString("android.service.zen.automatic.ruleType");
        ComponentName configurationActivity = getSettingsActivity(si);
        if (ruleType == null || ruleType.trim().isEmpty() || configurationActivity == null) {
            return null;
        }
        ZenRuleInfo ri = new ZenRuleInfo();
        ri.serviceComponent = new ComponentName(si.packageName, si.name);
        ri.settingsAction = "android.settings.ZEN_MODE_EXTERNAL_RULE_SETTINGS";
        ri.title = ruleType;
        ri.packageName = si.packageName;
        ri.configurationActivity = getSettingsActivity(si);
        ri.packageLabel = si.applicationInfo.loadLabel(pm);
        ri.ruleInstanceLimit = si.metaData.getInt("android.service.zen.automatic.ruleInstanceLimit", -1);
        return ri;
    }

    private static ComponentName getSettingsActivity(ServiceInfo si) {
        if (si == null || si.metaData == null) {
            return null;
        }
        String configurationActivity = si.metaData.getString("android.service.zen.automatic.configurationActivity");
        if (configurationActivity != null) {
            return ComponentName.unflattenFromString(configurationActivity);
        }
        return null;
    }

    private void clickSwitch(AutomaticZenRule rule, String sId, boolean newValue) {
        if (rule != null) {
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "ZenModeRuleSwitchChanged");
            MetricsLogger.action(this.mContext, 176, newValue);
            rule.setEnabled(newValue);
            rule.setInterruptionFilter(zenModeFromInterruptionFilter(this.mLastZenMode));
            setZenRule(sId, rule);
        }
    }

    private int zenModeFromInterruptionFilter(int zenmode) {
        switch (zenmode) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return 4;
        }
    }
}
