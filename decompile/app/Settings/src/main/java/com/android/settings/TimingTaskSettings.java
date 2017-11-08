package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;
import com.android.settings.TimingTask.TimingColumns;
import java.util.Calendar;

public class TimingTaskSettings extends SettingsPreferenceFragment implements OnTimeSetListener, OnPreferenceChangeListener {
    private static final String[] KEY_SCHEDULED_REPEAT_CUSTOM = new String[]{"sunday_", "monday_", "tuesday_", "wednesday_", "thursday_", "friday_", "saturday_"};
    private TimingTask mBootTask;
    private TimingTask mCachedTask;
    private int mCachedType = -1;
    private boolean[] mCheckedItemsArrayTransformed;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            TimingTaskSettings.this.handleDataChanged();
        }
    };
    private DaysOfWeek mDaysOfWeek;
    private CheckBox mRememberChoiceCheckBox;
    private TimingTask mShutdownTask;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230914);
        if (icicle != null) {
            this.mCachedType = icicle.getInt("type", -1);
            if (this.mCachedType != -1) {
                this.mShutdownTask = Utils.getTimingTask(getActivity(), 0);
                if (this.mShutdownTask == null) {
                    Log.e("TimingTaskSettings", "mShutdownTask is null");
                    getActivity().finish();
                    return;
                }
                this.mBootTask = Utils.getTimingTask(getActivity(), 1);
                if (this.mBootTask == null) {
                    Log.e("TimingTaskSettings", "mBootTask is null");
                    getActivity().finish();
                    return;
                } else if (this.mCachedType == 0) {
                    this.mCachedTask = this.mShutdownTask;
                } else if (this.mCachedType == 1) {
                    this.mCachedTask = this.mBootTask;
                }
            }
        }
        if (Utils.isMonkeyRunning()) {
            findPreference("timing_shutdown_switch").setEnabled(false);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        refreshUi();
        getContentResolver().registerContentObserver(TimingColumns.CONTENT_URI, false, this.mContentObserver);
    }

    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(this.mContentObserver);
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new TimePickerDialog(getActivity(), this, this.mCachedTask.getHour(), this.mCachedTask.getMinute(), DateFormat.is24HourFormat(getActivity()));
            case 2:
                return createAccessNetworkDialog();
            case 3:
                return createCustomRepeatDialog();
            default:
                return super.onCreateDialog(id);
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Activity activity = getActivity();
        if (activity != null) {
            int endHourOfDay;
            int endMinute;
            if (this.mCachedTask.getType() == 0) {
                endHourOfDay = this.mBootTask.getHour();
                endMinute = this.mBootTask.getMinute();
            } else {
                endHourOfDay = this.mShutdownTask.getHour();
                endMinute = this.mShutdownTask.getMinute();
            }
            if (isTimeDeltaValid(hourOfDay, minute, endHourOfDay, endMinute)) {
                this.mCachedTask.setHour(hourOfDay);
                this.mCachedTask.setMinute(minute);
                Utils.updateTimingTask(getActivity(), this.mCachedTask);
                if (this.mCachedTask.getType() == 0) {
                    refreshTime("shutdown_time");
                } else {
                    refreshTime("boot_time");
                }
            } else {
                Toast.makeText(activity, getResources().getQuantityString(2131689526, 5, new Object[]{Long.valueOf(5)}), 1).show();
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if ("shutdown_time".equals(key) || "boot_time".equals(key)) {
            if ("shutdown_time".equals(key)) {
                this.mCachedTask = this.mShutdownTask;
            } else {
                this.mCachedTask = this.mBootTask;
            }
            removeDialog(1);
            showDialog(1);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (preference instanceof TwoStatePreference) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        if ("timing_shutdown_switch".equals(key) || "timing_boot_switch".equals(key)) {
            if ("timing_shutdown_switch".equals(key)) {
                this.mCachedTask = this.mShutdownTask;
            } else {
                this.mCachedTask = this.mBootTask;
            }
            boolean checked = ((Boolean) newValue).booleanValue();
            if (checked != ((TwoStatePreference) preference).isChecked()) {
                this.mCachedTask.setEnabled(checked);
                Utils.updateTimingTask(getActivity(), this.mCachedTask);
            }
            return true;
        } else if (!"shutdown_repeat".equals(key) && !"boot_repeat".equals(key)) {
            return false;
        } else {
            if ("shutdown_repeat".equals(key)) {
                this.mCachedTask = this.mShutdownTask;
                if (preference instanceof ListPreference) {
                    ItemUseStat.getInstance().handleClickListPreference(getActivity(), (ListPreference) preference, ItemUseStat.KEY_SCHEDULED_POWER_OFF_REPEAT, (String) newValue);
                }
            } else {
                this.mCachedTask = this.mBootTask;
                if (preference instanceof ListPreference) {
                    ItemUseStat.getInstance().handleClickListPreference(getActivity(), (ListPreference) preference, ItemUseStat.KEY_SCHEDULED_POWER_ON_REPEAT, (String) newValue);
                }
            }
            int newRepeat = Integer.valueOf(newValue.toString()).intValue();
            this.mCachedTask.setRepeat(newRepeat);
            if (newRepeat == 2) {
                boolean rememberChoiceChecked = System.getInt(getContentResolver(), "timing_task_access_network_remember_choice", 0) == 1;
                if (!Utils.getRecessInfoExists(getActivity())) {
                    if (rememberChoiceChecked && !Utils.isNetworkAvailable(getActivity())) {
                        Toast.makeText(getActivity(), 2131627989, 1).show();
                        return false;
                    } else if (rememberChoiceChecked) {
                        Utils.startDownloadRecessInfo(getActivity());
                    } else {
                        showDialog(2);
                        return false;
                    }
                }
            }
            if (String.valueOf(4).equals(newValue)) {
                showDialog(3);
                return false;
            }
            Utils.updateTimingTask(getActivity(), this.mCachedTask);
            refreshRepeatPrefsSummary((ListPreference) preference, newValue.toString());
            return true;
        }
    }

    private Dialog createCustomRepeatDialog() {
        this.mDaysOfWeek = new DaysOfWeek(this.mCachedTask.getCustomRepeatCycle());
        final int offSet = (Calendar.getInstance().getFirstDayOfWeek() + 5) % 7;
        String[] repeatArray = getResources().getStringArray(2131361952);
        String[] repeatArrayTransformed = new String[7];
        this.mCheckedItemsArrayTransformed = new boolean[7];
        for (int i = 0; i < 7; i++) {
            int transformedIndex = (i + offSet) % 7;
            repeatArrayTransformed[i] = repeatArray[transformedIndex];
            this.mCheckedItemsArrayTransformed[i] = this.mDaysOfWeek.isChecked(transformedIndex);
        }
        return new Builder(getActivity()).setTitle(2131627984).setMultiChoiceItems(repeatArrayTransformed, this.mCheckedItemsArrayTransformed, new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                TimingTaskSettings.this.mCheckedItemsArrayTransformed[whichButton] = isChecked;
            }
        }).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TimingTaskSettings.this.handleClickShutdownRepeatCustom(TimingTaskSettings.this.mCheckedItemsArrayTransformed, TimingTaskSettings.this.mCachedTask.getType());
                for (int i = 0; i < 7; i++) {
                    TimingTaskSettings.this.mDaysOfWeek.set((offSet + i) % 7, TimingTaskSettings.this.mCheckedItemsArrayTransformed[i]);
                }
                int daysOfWeekBitmask = TimingTaskSettings.this.mDaysOfWeek.getCoded();
                TimingTaskSettings.this.mCachedTask.setRepeat(4);
                TimingTaskSettings.this.mCachedTask.setCustomRepeatCycle(daysOfWeekBitmask);
                if (TimingTaskSettings.this.mCachedTask.getType() == 0) {
                    TimingTaskSettings.this.refreshRepeat("shutdown_repeat");
                } else {
                    TimingTaskSettings.this.refreshRepeat("boot_repeat");
                }
                Utils.updateTimingTask(TimingTaskSettings.this.getActivity(), TimingTaskSettings.this.mCachedTask);
            }
        }).setNegativeButton(17039360, null).create();
    }

    private Dialog createAccessNetworkDialog() {
        View view = getActivity().getLayoutInflater().inflate(2130968618, null);
        this.mRememberChoiceCheckBox = (CheckBox) view.findViewById(2131886216);
        return new Builder(getActivity()).setTitle(2131627987).setView(view).setPositiveButton(2131627783, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int i = 0;
                ContentResolver contentResolver = TimingTaskSettings.this.getContentResolver();
                String str = "timing_task_access_network_remember_choice";
                if (TimingTaskSettings.this.mRememberChoiceCheckBox.isChecked()) {
                    i = 1;
                }
                System.putInt(contentResolver, str, i);
                if (Utils.isNetworkAvailable(TimingTaskSettings.this.getActivity())) {
                    Utils.startDownloadRecessInfo(TimingTaskSettings.this.getActivity());
                    TimingTaskSettings.this.mCachedTask.setRepeat(2);
                    Utils.updateTimingTask(TimingTaskSettings.this.getActivity(), TimingTaskSettings.this.mCachedTask);
                    if (TimingTaskSettings.this.mCachedTask.getType() == 0) {
                        TimingTaskSettings.this.refreshRepeat("shutdown_repeat");
                        return;
                    } else {
                        TimingTaskSettings.this.refreshRepeat("boot_repeat");
                        return;
                    }
                }
                Toast.makeText(TimingTaskSettings.this.getActivity(), 2131627989, 1).show();
            }
        }).setNegativeButton(17039360, null).create();
    }

    private void refreshTime(String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            TimingTask currentTask;
            if ("shutdown_time".equals(key)) {
                currentTask = this.mShutdownTask;
            } else if ("boot_time".equals(key)) {
                currentTask = this.mBootTask;
            } else {
                return;
            }
            Calendar when = Calendar.getInstance();
            when.set(11, currentTask.getHour());
            when.set(12, currentTask.getMinute());
            preference.setSummary(DateFormat.getTimeFormat(getActivity()).format(when.getTime()));
        }
    }

    private void refreshRepeat(String key) {
        ListPreference preference = (ListPreference) findPreference(key);
        if (preference != null) {
            TimingTask currentTask;
            if ("shutdown_repeat".equals(key)) {
                currentTask = this.mShutdownTask;
            } else if ("boot_repeat".equals(key)) {
                currentTask = this.mBootTask;
            } else {
                return;
            }
            String[] repeatEntries = getResources().getStringArray(2131361950);
            String[] repeatEntryValues = getResources().getStringArray(2131361951);
            if (SettingsExtUtils.isGlobalVersion()) {
                int i;
                CharSequence[] withoutWorkdayRepeatEntries = new String[(repeatEntries.length - 1)];
                int j = 0;
                for (i = 0; i <= repeatEntries.length - 1; i++) {
                    if (2 != i) {
                        withoutWorkdayRepeatEntries[j] = repeatEntries[i];
                        j++;
                    }
                }
                CharSequence[] withoutWorkdayRepeatEntryValues = new String[(repeatEntryValues.length - 1)];
                j = 0;
                for (i = 0; i <= repeatEntryValues.length - 1; i++) {
                    if (2 != i) {
                        withoutWorkdayRepeatEntryValues[j] = repeatEntryValues[i];
                        j++;
                    }
                }
                preference.setEntries(withoutWorkdayRepeatEntries);
                preference.setEntryValues(withoutWorkdayRepeatEntryValues);
            }
            String repeat = String.valueOf(currentTask.getRepeat());
            preference.setValue(repeat);
            refreshRepeatPrefsSummary(preference, repeat);
            preference.setOnPreferenceChangeListener(this);
        }
    }

    private void refreshRepeatPrefsSummary(ListPreference preference, String newValue) {
        if (preference != null) {
            int index = preference.findIndexOfValue(newValue);
            CharSequence[] entries = preference.getEntries();
            if (index >= 0 && index < entries.length) {
                CharSequence summary = preference.getEntries()[index];
                if (String.valueOf(4).equals(newValue)) {
                    String key = preference.getKey();
                    DaysOfWeek daysOfWeek = null;
                    if ("shutdown_repeat".equals(key)) {
                        daysOfWeek = new DaysOfWeek(this.mShutdownTask.getCustomRepeatCycle());
                    } else if ("boot_repeat".equals(key)) {
                        daysOfWeek = new DaysOfWeek(this.mBootTask.getCustomRepeatCycle());
                    }
                    if (daysOfWeek != null) {
                        summary = daysOfWeek.toGogaleString(getActivity());
                    }
                }
                preference.setSummary(summary);
            }
        }
    }

    private boolean isTimeDeltaValid(int startHourOfDay, int startMinute, int endHourOfDay, int endMinute) {
        Calendar start = Calendar.getInstance();
        start.set(11, startHourOfDay);
        start.set(12, startMinute);
        long startTime = start.getTimeInMillis();
        Calendar end = Calendar.getInstance();
        end.set(11, endHourOfDay);
        end.set(12, endMinute);
        return Math.abs(startTime - end.getTimeInMillis()) >= 300000;
    }

    private void handleDataChanged() {
        if (this.mShutdownTask != null && this.mBootTask != null) {
            TimingTask shutdownTask = Utils.getTimingTask(getActivity(), 0);
            TimingTask bootTask = Utils.getTimingTask(getActivity(), 1);
            if (!(this.mShutdownTask.equals(shutdownTask) && this.mBootTask.equals(bootTask))) {
                refreshUi();
            }
        }
    }

    private void refreshUi() {
        this.mShutdownTask = Utils.getTimingTask(getActivity(), 0);
        if (this.mShutdownTask == null) {
            Log.e("TimingTaskSettings", "mShutdownTask is null");
            getActivity().finish();
            return;
        }
        this.mBootTask = Utils.getTimingTask(getActivity(), 1);
        if (this.mBootTask == null) {
            Log.e("TimingTaskSettings", "mBootTask is null");
            getActivity().finish();
            return;
        }
        this.mCachedTask = this.mShutdownTask;
        TwoStatePreference timingShutdownSwitch = (TwoStatePreference) findPreference("timing_shutdown_switch");
        timingShutdownSwitch.setOnPreferenceChangeListener(null);
        timingShutdownSwitch.setChecked(this.mShutdownTask.isEnabled());
        timingShutdownSwitch.setOnPreferenceChangeListener(this);
        TwoStatePreference timingBootSwitch = (TwoStatePreference) findPreference("timing_boot_switch");
        timingBootSwitch.setOnPreferenceChangeListener(null);
        timingBootSwitch.setChecked(this.mBootTask.isEnabled());
        timingBootSwitch.setOnPreferenceChangeListener(this);
        refreshTime("shutdown_time");
        refreshTime("boot_time");
        refreshRepeat("shutdown_repeat");
        refreshRepeat("boot_repeat");
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mCachedTask != null) {
            outState.putInt("type", this.mCachedTask.getType());
        }
    }

    private void handleClickShutdownRepeatCustom(boolean[] checkedItemsArrayTransformed, int type) {
        if (checkedItemsArrayTransformed != null) {
            StringBuilder reportStr = new StringBuilder();
            boolean sevenDaysChecked = true;
            for (int i = 0; i < checkedItemsArrayTransformed.length; i++) {
                if (checkedItemsArrayTransformed[i]) {
                    reportStr.append(KEY_SCHEDULED_REPEAT_CUSTOM[i]);
                } else {
                    sevenDaysChecked = false;
                }
            }
            if (sevenDaysChecked) {
                if (1 == type) {
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, "scheduled_power_on_everyday");
                } else if (type == 0) {
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, "scheduled_power_off_everyday");
                }
            } else if (reportStr.length() > 0) {
                if (1 == type) {
                    reportStr.append("scheduled_custom_boot");
                } else if (type == 0) {
                    reportStr.append("scheduled_custom_shutdown");
                }
                ItemUseStat.getInstance().handleClick(getActivity(), 2, reportStr.toString());
            }
        }
    }
}
