package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.datetime.ZoneGetter;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateTimeSettings extends DateTimeSettingsHwBase implements OnTimeSetListener, OnDateSetListener, OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230764;
            result.add(sir);
            return DateTimeSettings.mHwCustSearchIndexProvider.addDateTimeXmlResourcesToIndex(context, result);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (Utils.isWifiOnly(context)) {
                keys.add("dual_clocks");
                keys.add("set_home_city");
                keys.add("auto_zone");
            }
            return keys;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> indexables = new ArrayList();
            if (context == null) {
                return indexables;
            }
            SearchIndexableRaw indexable = new SearchIndexableRaw(context);
            indexable.key = "24 hour";
            indexable.title = String.format(context.getString(2131627239, new Object[]{Integer.valueOf(24)}), new Object[0]);
            indexable.screenTitle = context.getString(2131624599);
            indexables.add(indexable);
            return indexables;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private Calendar mDummyDate;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Activity activity = DateTimeSettings.this.getActivity();
            if (activity == null) {
                return;
            }
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                DateTimeSettings.this.setDataTimeByNetworkType();
            } else {
                DateTimeSettings.this.updateTimeAndDateDisplay(activity);
            }
        }
    };

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                Calendar now = Calendar.getInstance();
                this.mSummaryLoader.setSummary(this, ZoneGetter.getTimeZoneOffsetAndName(now.getTimeZone(), now.getTime()));
            }
        }
    }

    protected int getMetricsCategory() {
        return 38;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230764);
        initUI();
    }

    protected void initUI() {
        boolean z;
        boolean z2 = false;
        boolean autoTimeEnabled = getAutoState("auto_time");
        boolean autoTimeZoneEnabled = getAutoState("auto_time_zone");
        this.mAutoTimePref = (RestrictedSwitchPreference) findPreference("auto_time");
        this.mAutoTimePref.setOnPreferenceChangeListener(this);
        this.mAutoTimePref.setDisabledByAdmin(RestrictedLockUtils.checkIfAutoTimeRequired(getActivity()));
        boolean isFirstRun = getActivity().getIntent().getBooleanExtra("firstRun", false);
        this.mDummyDate = Calendar.getInstance();
        this.mAutoTimePref.setChecked(autoTimeEnabled);
        this.mAutoTimeZonePref = (TwoStatePreference) findPreference("auto_zone");
        this.mAutoTimeZonePref.setOnPreferenceChangeListener(this);
        if (Utils.isWifiOnly(getActivity()) || isFirstRun) {
            getPreferenceScreen().removePreference(this.mAutoTimeZonePref);
            autoTimeZoneEnabled = false;
        }
        this.mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);
        this.mTimePref = findPreference("time");
        this.mTime24Pref = (TwoStatePreference) findPreference("24 hour");
        this.mTime24Pref.setTitle(String.format(getResources().getString(2131627239, new Object[]{Integer.valueOf(24)}), new Object[0]));
        this.mTime24Pref.setOnPreferenceChangeListener(this);
        this.mTimeZone = findPreference("timezone");
        this.mDatePref = findPreference("date");
        if (isFirstRun) {
            getPreferenceScreen().removePreference(this.mTime24Pref);
        }
        int status = ParentControl.getParentControlStatus(getActivity());
        ParentControl.enablePref(this.mTimePref, !autoTimeEnabled, status);
        Preference preference = this.mDatePref;
        if (autoTimeEnabled) {
            z = false;
        } else {
            z = true;
        }
        ParentControl.enablePref(preference, z, status);
        Preference preference2 = this.mTimeZone;
        if (!autoTimeZoneEnabled) {
            z2 = true;
        }
        ParentControl.enablePref(preference2, z2, status);
        super.initUI();
    }

    public void onResume() {
        super.onResume();
        this.mTime24Pref.setChecked(is24Hour());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.SERVICE_STATE");
        getActivity().registerReceiver(this.mIntentReceiver, filter, null, null);
        updateParentCtrlAutoOptions();
        updateTimeAndDateDisplay(getActivity());
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mIntentReceiver);
    }

    public void updateTimeAndDateDisplay(Context context) {
        Calendar now = Calendar.getInstance();
        Date dummyDate = this.mDummyDate.getTime();
        this.mDatePref.setSummary(DateFormat.getLongDateFormat(context).format(now.getTime()));
        this.mTimePref.setSummary(DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
        this.mTimeZone.setSummary(ZoneGetter.getTimeZoneOffsetAndName(now.getTimeZone(), now.getTime()));
        updateParentControlItems();
        setDataTimeByNetworkType();
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Activity activity = getActivity();
        if (activity != null) {
            setDate(activity, year, month, day);
            updateTimeAndDateDisplay(activity);
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Activity activity = getActivity();
        if (activity != null) {
            setTime(activity, hourOfDay, minute);
            updateTimeAndDateDisplay(activity);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean z = false;
        int status = ParentControl.getParentControlStatus(getActivity());
        String key = preference.getKey();
        ContentResolver contentResolver;
        String str;
        int i;
        Preference preference2;
        if (preference.getKey().equals("auto_time")) {
            boolean z2;
            boolean autoEnabled = ((Boolean) newValue).booleanValue();
            ItemUseStat.getInstance().handleClick(getActivity(), 3, key, autoEnabled ? "on" : "off");
            contentResolver = getContentResolver();
            str = "auto_time";
            if (autoEnabled) {
                i = 1;
            } else {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
            Preference preference3 = this.mTimePref;
            if (autoEnabled) {
                z2 = false;
            } else {
                z2 = true;
            }
            ParentControl.enablePref(preference3, z2, status);
            preference2 = this.mDatePref;
            if (!autoEnabled) {
                z = true;
            }
            ParentControl.enablePref(preference2, z, status);
        } else if (preference.getKey().equals("auto_zone")) {
            boolean autoZoneEnabled = ((Boolean) newValue).booleanValue();
            ItemUseStat.getInstance().handleClick(getActivity(), 3, key, autoZoneEnabled ? "on" : "off");
            contentResolver = getContentResolver();
            str = "auto_time_zone";
            if (autoZoneEnabled) {
                i = 1;
            } else {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
            preference2 = this.mTimeZone;
            if (!autoZoneEnabled) {
                z = true;
            }
            ParentControl.enablePref(preference2, z, status);
        }
        return super.onPreferenceChange(preference, newValue);
    }

    public Dialog onCreateDialog(int id) {
        Calendar calendar = Calendar.getInstance();
        switch (id) {
            case 0:
                DatePickerDialog d = new DatePickerDialog(getActivity(), this, calendar.get(1), calendar.get(2), calendar.get(5));
                configureDatePicker(d.getDatePicker());
                return d;
            case 1:
                return new TimePickerDialog(getActivity(), this, calendar.get(11), calendar.get(12), DateFormat.is24HourFormat(getActivity()));
            default:
                throw new IllegalArgumentException();
        }
    }

    static void configureDatePicker(DatePicker datePicker) {
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, 0, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, 11, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if (preference == this.mDatePref) {
            showDialog(0);
        } else if (preference == this.mTimePref) {
            removeDialog(1);
            showDialog(1);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateTimeAndDateDisplay(getActivity());
    }

    protected void timeUpdated(boolean is24Hour) {
        Intent timeChanged = new Intent("android.intent.action.TIME_SET");
        timeChanged.putExtra("android.intent.extra.TIME_PREF_24_HOUR_FORMAT", is24Hour);
        getActivity().sendBroadcast(timeChanged);
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    protected void set24Hour(boolean is24Hour) {
        System.putString(getContentResolver(), "time_12_24", is24Hour ? "24" : "12");
    }

    private boolean getAutoState(String name) {
        boolean z = false;
        try {
            if (Global.getInt(getContentResolver(), name) > 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException snfe) {
            Log.w("DateTimeSettings", "getAutoState Settings not found:", snfe);
            return false;
        }
    }

    static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(1, year);
        c.set(2, month);
        c.set(5, day);
        long when = Math.max(c.getTimeInMillis(), 1194220800000L);
        if (when / 1000 < 2145888000) {
            ((AlarmManager) context.getSystemService("alarm")).setTime(when);
        }
    }

    static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(11, hourOfDay);
        c.set(12, minute);
        c.set(13, 0);
        c.set(14, 0);
        long when = Math.max(c.getTimeInMillis(), 1194220800000L);
        if (when / 1000 < 2145888000) {
            ((AlarmManager) context.getSystemService("alarm")).setTime(when);
        }
    }
}
