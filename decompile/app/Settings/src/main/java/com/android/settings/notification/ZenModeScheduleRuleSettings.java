package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AutomaticZenRule;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.TimePicker;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ZenModeScheduleRuleSettings extends ZenModeRuleSettingsBase {
    private boolean[] mCheckedItemsArrayTransformed;
    private final SimpleDateFormat mDayFormat = new SimpleDateFormat("EEE");
    private ListPreference mDays;
    private AlertDialog mDialog;
    private TimePickerPreference mEnd;
    private final SparseBooleanArray mInitDays = new SparseBooleanArray();
    private ScheduleInfo mSchedule;
    private TimePickerPreference mStart;

    private static class TimePickerPreference extends Preference {
        private Callback mCallback;
        private final Context mContext;
        private int mHourOfDay;
        private int mMinute;
        private int mSummaryFormat;

        public interface Callback {
            boolean onSetTime(int i, int i2);
        }

        public static class TimePickerFragment extends DialogFragment implements OnTimeSetListener {
            public TimePickerPreference pref;

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                boolean usePref = this.pref != null && this.pref.mHourOfDay >= 0 && this.pref.mMinute >= 0;
                Calendar c = Calendar.getInstance();
                return new TimePickerDialog(getActivity(), this, usePref ? this.pref.mHourOfDay : c.get(11), usePref ? this.pref.mMinute : c.get(12), DateFormat.is24HourFormat(getActivity()));
            }

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (this.pref != null) {
                    this.pref.setTime(hourOfDay, minute);
                }
            }
        }

        public TimePickerPreference(Context context, final FragmentManager mgr) {
            super(context);
            this.mContext = context;
            setPersistent(false);
            setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    TimePickerFragment frag = new TimePickerFragment();
                    frag.pref = TimePickerPreference.this;
                    frag.show(mgr, TimePickerPreference.class.getName());
                    return true;
                }
            });
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        public void setSummaryFormat(int resId) {
            this.mSummaryFormat = resId;
            updateSummary();
        }

        public void setTime(int hourOfDay, int minute) {
            if (this.mCallback == null || this.mCallback.onSetTime(hourOfDay, minute)) {
                this.mHourOfDay = hourOfDay;
                this.mMinute = minute;
                updateSummary();
            }
        }

        private void updateSummary() {
            Calendar c = Calendar.getInstance();
            c.set(11, this.mHourOfDay);
            c.set(12, this.mMinute);
            String time = DateFormat.getTimeFormat(this.mContext).format(c.getTime());
            if (this.mSummaryFormat != 0) {
                time = this.mContext.getResources().getString(this.mSummaryFormat, new Object[]{time});
            }
            setSummary((CharSequence) time);
        }
    }

    protected boolean setRule(AutomaticZenRule rule) {
        ScheduleInfo scheduleInfo = null;
        if (rule != null) {
            scheduleInfo = ZenModeConfig.tryParseScheduleConditionId(rule.getConditionId());
        }
        this.mSchedule = scheduleInfo;
        return this.mSchedule != null;
    }

    protected void onCreateInternal() {
        addPreferencesFromResource(2131230954);
        PreferenceScreen root = getPreferenceScreen();
        this.mDays = (ListPreference) root.findPreference("days");
        this.mDays.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int newRepeat = Integer.valueOf(newValue.toString()).intValue();
                ZenModeScheduleRuleSettings.this.saveSelectedValueResId(newRepeat);
                switch (newRepeat) {
                    case 0:
                    case 1:
                    case 2:
                        ZenModeScheduleRuleSettings.this.updateScheduleDays(newRepeat);
                        return true;
                    case 3:
                        ZenModeScheduleRuleSettings.this.showDaysDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        FragmentManager mgr = getFragmentManager();
        this.mStart = new TimePickerPreference(getPrefContext(), mgr);
        this.mStart.setKey("start_time");
        this.mStart.setTitle(2131626833);
        this.mStart.setWidgetLayoutResource(2130968998);
        this.mStart.setLayoutResource(2130968977);
        this.mStart.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (ZenModeScheduleRuleSettings.this.mDisableListeners) {
                    return true;
                }
                if (!ZenModeConfig.isValidHour(hour) || !ZenModeConfig.isValidMinute(minute)) {
                    return false;
                }
                if (hour == ZenModeScheduleRuleSettings.this.mSchedule.startHour && minute == ZenModeScheduleRuleSettings.this.mSchedule.startMinute) {
                    return true;
                }
                if (ZenModeScheduleRuleSettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange start h=" + hour + " m=" + minute);
                }
                ZenModeScheduleRuleSettings.this.mSchedule.startHour = hour;
                ZenModeScheduleRuleSettings.this.mSchedule.startMinute = minute;
                ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                ItemUseStat.getInstance().handleClick(ZenModeScheduleRuleSettings.this.getActivity(), 2, ZenModeScheduleRuleSettings.this.mStart.getKey(), "start at hour=" + hour + " miniute=" + minute);
                return true;
            }
        });
        root.addPreference(this.mStart);
        this.mStart.setDependency(this.mDays.getKey());
        this.mEnd = new TimePickerPreference(getPrefContext(), mgr);
        this.mEnd.setKey("end_time");
        this.mEnd.setTitle(2131626834);
        this.mEnd.setWidgetLayoutResource(2130968998);
        this.mEnd.setLayoutResource(2130968977);
        this.mEnd.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (ZenModeScheduleRuleSettings.this.mDisableListeners) {
                    return true;
                }
                if (!ZenModeConfig.isValidHour(hour) || !ZenModeConfig.isValidMinute(minute)) {
                    return false;
                }
                if (hour == ZenModeScheduleRuleSettings.this.mSchedule.endHour && minute == ZenModeScheduleRuleSettings.this.mSchedule.endMinute) {
                    return true;
                }
                if (ZenModeScheduleRuleSettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange end h=" + hour + " m=" + minute);
                }
                ZenModeScheduleRuleSettings.this.mSchedule.endHour = hour;
                ZenModeScheduleRuleSettings.this.mSchedule.endMinute = minute;
                ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                ItemUseStat.getInstance().handleClick(ZenModeScheduleRuleSettings.this.getActivity(), 2, ZenModeScheduleRuleSettings.this.mEnd.getKey(), "end at hour=" + hour + " miniute=" + minute);
                return true;
            }
        });
        root.addPreference(this.mEnd);
        this.mEnd.setDependency(this.mDays.getKey());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(2131628620);
            setDeleteTitle(2131628620);
        }
    }

    private void updateDays() {
        if (this.mSchedule != null) {
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
                            sb.append(this.mDayFormat.format(c.getTime()));
                        } else {
                            j++;
                        }
                    }
                }
                if (sb.length() > 0) {
                    this.mDays.setValueIndex(getSelectedValueResId());
                    this.mDays.setSummary(sb);
                    this.mDays.notifyDependencyChange(false);
                    return;
                }
            }
            this.mDays.setValueIndex(getSelectedValueResId());
            this.mDays.setSummary(2131626807);
            this.mDays.notifyDependencyChange(true);
        }
    }

    private void updateEndSummary() {
        this.mEnd.setSummaryFormat((this.mSchedule.startHour * 60) + this.mSchedule.startMinute >= (this.mSchedule.endHour * 60) + this.mSchedule.endMinute ? 2131626835 : 0);
    }

    protected void updateControlsInternal() {
        updateDays();
        this.mStart.setTime(this.mSchedule.startHour, this.mSchedule.startMinute);
        this.mEnd.setTime(this.mSchedule.endHour, this.mSchedule.endMinute);
        updateEndSummary();
    }

    protected int getMetricsCategory() {
        return 144;
    }

    private void showDaysDialog() {
        int i;
        int offSet = (Calendar.getInstance().getFirstDayOfWeek() + 5) % 7;
        String[] repeatArray = this.mContext.getResources().getStringArray(2131361952);
        String[] repeatArrayTransformed = new String[7];
        this.mCheckedItemsArrayTransformed = new boolean[7];
        this.mInitDays.clear();
        if (this.mSchedule.days != null) {
            for (int put : this.mSchedule.days) {
                this.mInitDays.put(put, true);
            }
        }
        for (i = 0; i < 7; i++) {
            repeatArrayTransformed[i] = repeatArray[(i + offSet) % 7];
            this.mCheckedItemsArrayTransformed[i] = this.mInitDays.get(resetIndexByOffSet(i) + 1);
        }
        this.mDialog = new Builder(this.mContext).setTitle(2131627984).setMultiChoiceItems(repeatArrayTransformed, this.mCheckedItemsArrayTransformed, new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                ZenModeScheduleRuleSettings.this.mInitDays.put(ZenModeScheduleRuleSettings.this.resetIndexByOffSet(whichButton) + 1, isChecked);
            }
        }).setPositiveButton(2131626431, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ZenModeScheduleRuleSettings.this.updateRuleSchedule(ZenModeScheduleRuleSettings.this.getDays());
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                ZenModeScheduleRuleSettings.this.updateDays();
            }
        }).show();
    }

    private int[] getDays() {
        int i;
        SparseBooleanArray rt = new SparseBooleanArray(this.mInitDays.size());
        for (i = 0; i < this.mInitDays.size(); i++) {
            int day = this.mInitDays.keyAt(i);
            if (this.mInitDays.valueAt(i)) {
                rt.put(day, true);
            }
        }
        int[] rta = new int[rt.size()];
        for (i = 0; i < rta.length; i++) {
            rta[i] = rt.keyAt(i);
        }
        Arrays.sort(rta);
        return rta;
    }

    private int resetIndexByOffSet(int index) {
        if (index < 0) {
            return 0;
        }
        return (index + ((Calendar.getInstance().getFirstDayOfWeek() + 6) % 7)) % 7;
    }

    private String getKey() {
        return "zen_event_rule" + this.mId;
    }

    private int getSelectedValueResId() {
        return Global.getInt(getContentResolver(), getKey(), 3);
    }

    private void saveSelectedValueResId(int id) {
        Global.putInt(getContentResolver(), getKey(), id);
    }

    protected void toastAndFinish() {
        if (this.mDeleting) {
            Log.d("ZenModeSettings", "delete rule info in database success: " + (1 == getContentResolver().delete(Global.getUriFor(getKey()), null, null) ? "true" : "false") + ". id is: " + this.mId);
        }
        super.toastAndFinish();
    }

    private void updateRuleSchedule(int[] days) {
        if (!this.mDisableListeners && this.mSchedule != null && !Arrays.equals(days, this.mSchedule.days)) {
            StringBuilder sb = new StringBuilder();
            for (int day : days) {
                sb.append(day);
                sb.append(",");
            }
            if (DEBUG) {
                Log.d("ZenModeSettings", "days.onChanged days=" + sb.toString());
            }
            this.mSchedule.days = days;
            updateRule(ZenModeConfig.toScheduleConditionId(this.mSchedule));
            ItemUseStat.getInstance().handleClick(getActivity(), 2, this.mDays.getKey(), sb.toString());
        }
    }

    private void updateScheduleDays(int repeat) {
        switch (repeat) {
            case 0:
                updateRuleSchedule(new int[]{2, 3, 4, 5, 6});
                return;
            case 1:
                updateRuleSchedule(new int[]{1, 7});
                return;
            case 2:
                updateRuleSchedule(ZenModeScheduleDaysSelection.DAYS);
                break;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }
}
