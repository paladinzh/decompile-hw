package com.android.deskclock.alarmclock;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TimePicker;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.CalendarPreference.Callback;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Utils;

public class SetAlarmFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Callback, ButtonPreference.Callback {
    private ButtonPreference mButton;
    private CalendarPreference mCalendar;
    private TextArrowPreference mLabel;
    private SetAlarm mParentActivity = null;
    private TextArrowPreference mRepeat;
    private TextArrowPreference mRington;
    private SwitchPreference mVirbate;

    public static SetAlarmFragment newInstance(Bundle args) {
        SetAlarmFragment f = new SetAlarmFragment();
        f.setArguments(args);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().getBoolean("add", false)) {
            if (Config.istablet()) {
                addPreferencesFromResource(R.xml.set_alarm_pad);
            } else {
                addPreferencesFromResource(R.xml.set_alarm);
            }
        } else if (Config.istablet()) {
            addPreferencesFromResource(R.xml.set_alarm_add_pad);
        } else {
            addPreferencesFromResource(R.xml.set_alarm_add);
        }
        this.mVirbate = (SwitchPreference) findPreference("Vibrate_layout");
        this.mRepeat = (TextArrowPreference) findPreference("repeat_layout");
        this.mRington = (TextArrowPreference) findPreference("Ringtone_layout");
        this.mLabel = (TextArrowPreference) findPreference("label_layout");
        this.mVirbate.setOnPreferenceClickListener(this);
        this.mVirbate.setOnPreferenceChangeListener(this);
        this.mRepeat.setOnPreferenceClickListener(this);
        this.mRington.setOnPreferenceClickListener(this);
        this.mLabel.setOnPreferenceClickListener(this);
        this.mCalendar = (CalendarPreference) findPreference("calendar_layout");
        this.mButton = (ButtonPreference) findPreference("button_layout");
        if (this.mCalendar != null) {
            this.mCalendar.setCallback(this);
        }
        if (this.mButton != null) {
            this.mButton.setCallback(this);
        }
        restoreState();
    }

    public void onResume() {
        super.onResume();
        if (this.mCalendar != null) {
            this.mCalendar.updateCalendarMode();
        }
        boolean add = getArguments().getBoolean("add", false);
        ListView listView = getListView();
        if (1 > listView.getFooterViewsCount() && !add && Utils.isLandScreen(getActivity()) && Config.istablet()) {
            listView.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.set_alarm_footview, listView, false), null, false);
        }
    }

    public void restoreState() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            String label = arguments.getString("label");
            boolean vibrate = arguments.getBoolean("vibrate");
            String repeat = arguments.getString("repeat");
            String rington = arguments.getString("rington");
            if (this.mRepeat != null) {
                this.mRepeat.setDetail(repeat);
            }
            if (this.mRington != null) {
                this.mRington.setDetail(rington);
            }
            if (this.mVirbate != null) {
                this.mVirbate.setChecked(vibrate);
            }
            if (this.mLabel != null) {
                this.mLabel.setDetail(label);
            }
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mParentActivity = (SetAlarm) activity;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!"Vibrate_layout".equals(preference.getKey())) {
            return false;
        }
        switchVibrate(newValue);
        return true;
    }

    private void switchVibrate(Object newValue) {
        boolean checked = ((Boolean) newValue).booleanValue();
        this.mParentActivity.updateVibrate(checked);
        ClockReporter.reportEventContainMessage(this.mParentActivity, 25, "VIBRATE_STATE", checked ? 1 : 0);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference == null) {
            return false;
        }
        String key = preference.getKey();
        if ("Vibrate_layout".equals(key)) {
            this.mParentActivity.updateVibrate(((SwitchPreference) preference).isChecked());
            return true;
        } else if ("repeat_layout".equals(key)) {
            this.mParentActivity.updateRepeat();
            return true;
        } else if ("Ringtone_layout".equals(key)) {
            this.mParentActivity.updateRington();
            return true;
        } else if (!"label_layout".equals(key)) {
            return false;
        } else {
            this.mParentActivity.updateLabel();
            return true;
        }
    }

    public void updateRington(String rington) {
        if (this.mRington != null) {
            this.mRington.setDetail(rington);
        }
    }

    public void updateRepeat(String daysOfWeekStr) {
        if (this.mRepeat != null) {
            this.mRepeat.setDetail(daysOfWeekStr);
        }
    }

    public void updateLabel(String lable) {
        if (this.mLabel != null) {
            this.mLabel.setDetail(lable);
        }
    }

    public void clearTimeFocus() {
        if (this.mCalendar != null) {
            this.mCalendar.clearFocus();
        }
    }

    public void onClick(View view) {
        this.mParentActivity.updateDelete();
    }

    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.mParentActivity.getTimePickTime(hourOfDay, minute);
    }

    public int getHour() {
        return this.mParentActivity.getHour();
    }

    public int getMinute() {
        return this.mParentActivity.getMinute();
    }
}
