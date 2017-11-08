package com.android.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;
import com.android.settings.EyeComfortSeekBarPreference.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EyeComfortSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final int EYECOMFORT_INFO_ENABLE = SystemProperties.getInt("ro.config.hw_eyescomfort_info", 0);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230784;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            result.add("end_time");
            result.add("start_time");
            return result;
        }
    };
    Callback mCallback = new Callback() {
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d("EyeCareMainActivity", "onStartTrackingTouch called.");
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            EyeComfortSettings.this.convertSeekbarProgressToWarm(progress);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            ItemUseStat.getInstance().handleClick(EyeComfortSettings.this.getActivity(), 2, "eyecomfort_seekbar_preference", seekBar.getProgress());
        }
    };
    private TimePickerPreference mEnd;
    private ContentObserver mEyeComfortScheduleModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            EyeComfortSettings.this.updateScheduleSwitchStatus();
        }
    };
    private ContentObserver mEyeProtectionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            EyeComfortSettings.this.updateSwitchStatus();
        }
    };
    private LinearLayout mEyesComfortInfo;
    private int mLessWarm = 20;
    private int mMoreWarm = 20;
    private ScheduleInfo mScheduleInfo;
    private CustomSwitchPreference mScheduleSwitch;
    private EyeComfortSeekBarPreference mSeekBarPreference;
    private TimePickerPreference mStart;
    private CustomSwitchPreference mSwitch;

    private class ScheduleInfo {
        public int mEndHour;
        public int mEndMinute;
        public int mStartHour;
        public int mStartMinute;

        private ScheduleInfo() {
        }
    }

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230784);
        initWarmValue();
        initTimeValue();
        this.mSwitch = (CustomSwitchPreference) getPreferenceScreen().findPreference("eyes_protection_mode");
        this.mSwitch.setOnPreferenceChangeListener(this);
        PreferenceCategory root = (PreferenceCategory) getPreferenceScreen().findPreference("auto_rules");
        this.mScheduleSwitch = (CustomSwitchPreference) root.findPreference("eye_comfort_schedule_switch");
        this.mScheduleSwitch.setOnPreferenceChangeListener(this);
        getContentResolver().registerContentObserver(System.getUriFor("eyes_protection_mode"), true, this.mEyeProtectionModeObserver);
        getContentResolver().registerContentObserver(System.getUriFor("eye_comfort_schedule_switch"), true, this.mEyeComfortScheduleModeObserver);
        FragmentManager mgr = getFragmentManager();
        this.mStart = new TimePickerPreference(getPrefContext(), mgr);
        this.mStart.setKey("start_time");
        this.mStart.setTitle(2131626833);
        this.mStart.setWidgetLayoutResource(2130968998);
        this.mStart.setLayoutResource(2130968977);
        this.mStart.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (EyeComfortSettings.this.mScheduleInfo.mStartHour == hour && EyeComfortSettings.this.mScheduleInfo.mStartMinute == minute) {
                    return true;
                }
                if (EyeComfortSettings.this.isEqual(hour, minute, true)) {
                    Toast.makeText(EyeComfortSettings.this.getActivity(), 2131628550, 1).show();
                    return false;
                }
                EyeComfortSettings.this.mScheduleInfo.mStartHour = hour;
                EyeComfortSettings.this.mScheduleInfo.mStartMinute = minute;
                EyeComfortSettings.this.setValidTime(false);
                System.putIntForUser(EyeComfortSettings.this.getContentResolver(), "eye_comfort_starttime", EyeComfortSettings.this.getStaticStartTime(), UserHandle.myUserId());
                EyeComfortSettings.this.updateTime();
                ItemUseStat.getInstance().handleClick(EyeComfortSettings.this.getActivity(), 2, EyeComfortSettings.this.mStart.getKey(), "start at hour=" + hour + " miniute=" + minute);
                return true;
            }
        });
        root.addPreference(this.mStart);
        this.mEnd = new TimePickerPreference(getPrefContext(), mgr);
        this.mEnd.setKey("end_time");
        this.mEnd.setTitle(2131626834);
        this.mEnd.setWidgetLayoutResource(2130968998);
        this.mEnd.setLayoutResource(2130968977);
        this.mEnd.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (hour == EyeComfortSettings.this.mScheduleInfo.mEndHour && minute == EyeComfortSettings.this.mScheduleInfo.mEndMinute) {
                    return true;
                }
                if (EyeComfortSettings.this.isEqual(hour, minute, false)) {
                    Toast.makeText(EyeComfortSettings.this.getActivity(), 2131628550, 1).show();
                    return false;
                }
                EyeComfortSettings.this.mScheduleInfo.mEndHour = hour;
                EyeComfortSettings.this.mScheduleInfo.mEndMinute = minute;
                EyeComfortSettings.this.setValidTime(false);
                System.putIntForUser(EyeComfortSettings.this.getContentResolver(), "eye_comfort_endtime", EyeComfortSettings.this.getStaticEndTime(), UserHandle.myUserId());
                EyeComfortSettings.this.updateTime();
                ItemUseStat.getInstance().handleClick(EyeComfortSettings.this.getActivity(), 2, EyeComfortSettings.this.mEnd.getKey(), "end at hour=" + hour + " miniute=" + minute);
                return true;
            }
        });
        root.addPreference(this.mEnd);
        this.mSeekBarPreference = (EyeComfortSeekBarPreference) ((PreferenceCategory) getPreferenceScreen().findPreference("eye_comfort_color")).findPreference("eyecomfort_seekbar_preference");
        this.mSeekBarPreference.setCallback(this.mCallback);
    }

    public void onResume() {
        super.onResume();
        updateSwitchStatus();
        updateScheduleSwitchStatus();
        updateTime();
        updateSeekbar();
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        getContentResolver().unregisterContentObserver(this.mEyeProtectionModeObserver);
        getContentResolver().unregisterContentObserver(this.mEyeComfortScheduleModeObserver);
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130968776, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        this.mEyesComfortInfo = (LinearLayout) view.findViewById(2131886571);
        if (EYECOMFORT_INFO_ENABLE == 0 && this.mEyesComfortInfo != null) {
            this.mEyesComfortInfo.setVisibility(8);
        }
        return view;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mSwitch) {
            setEyeComfortSwitch(((Boolean) newValue).booleanValue());
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            return true;
        } else if (preference != this.mScheduleSwitch) {
            return false;
        } else {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
            setValidTime(false);
            setEyeComfortScheduleSwitch(((Boolean) newValue).booleanValue());
            return true;
        }
    }

    private void initWarmValue() {
        this.mLessWarm = System.getIntForUser(getContentResolver(), "eye_comfort_lesswarm", this.mLessWarm, UserHandle.myUserId());
        this.mMoreWarm = System.getIntForUser(getContentResolver(), "eye_comfort_morewarm", this.mMoreWarm, UserHandle.myUserId());
    }

    private void initTimeValue() {
        if (this.mScheduleInfo != null) {
            this.mScheduleInfo = null;
        }
        this.mScheduleInfo = new ScheduleInfo();
        this.mScheduleInfo.mStartHour = 22;
        this.mScheduleInfo.mEndHour = 7;
        this.mScheduleInfo.mEndMinute = 0;
        this.mScheduleInfo.mStartMinute = 0;
        int start = System.getIntForUser(getContentResolver(), "eye_comfort_starttime", 0, UserHandle.myUserId());
        int end = System.getIntForUser(getContentResolver(), "eye_comfort_endtime", 0, UserHandle.myUserId());
        if (!setStaticTime(start, false)) {
            System.putIntForUser(getContentResolver(), "eye_comfort_starttime", getStaticStartTime(), UserHandle.myUserId());
        }
        if (!setStaticTime(end, true)) {
            System.putIntForUser(getContentResolver(), "eye_comfort_endtime", getStaticEndTime(), UserHandle.myUserId());
        }
    }

    private boolean isEqual(int hour, int minute, boolean bStartFlg) {
        int mStartTime;
        int mEndTime;
        if (bStartFlg) {
            mStartTime = (hour * 60) + minute;
            mEndTime = (this.mScheduleInfo.mEndHour * 60) + this.mScheduleInfo.mEndMinute;
        } else {
            mStartTime = (this.mScheduleInfo.mStartHour * 60) + this.mScheduleInfo.mStartMinute;
            mEndTime = (hour * 60) + minute;
        }
        if (mStartTime == mEndTime) {
            return true;
        }
        return false;
    }

    private boolean getNextDayFlag() {
        if ((this.mScheduleInfo.mStartHour * 60) + this.mScheduleInfo.mStartMinute >= (this.mScheduleInfo.mEndHour * 60) + this.mScheduleInfo.mEndMinute) {
            return true;
        }
        return false;
    }

    private boolean setStaticTime(int nTime, boolean bEndFlg) {
        if (nTime == 0) {
            return false;
        }
        if (bEndFlg) {
            if (getStaticEndTime() == nTime) {
                return true;
            }
            this.mScheduleInfo.mEndMinute = nTime % 60;
            this.mScheduleInfo.mEndHour = nTime / 60;
        } else if (getStaticStartTime() == nTime) {
            return true;
        } else {
            this.mScheduleInfo.mStartHour = nTime / 60;
            this.mScheduleInfo.mStartMinute = nTime % 60;
        }
        return false;
    }

    private int getStaticStartTime() {
        return (this.mScheduleInfo.mStartHour * 60) + this.mScheduleInfo.mStartMinute;
    }

    private int getStaticEndTime() {
        return (this.mScheduleInfo.mEndHour * 60) + this.mScheduleInfo.mEndMinute;
    }

    private void convertSeekbarProgressToWarm(int progress) {
        int nValue;
        if (progress > 50) {
            nValue = (this.mMoreWarm * (progress - 50)) / 50;
            setWarmValue(nValue);
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "EyeComfortSeekBarPreference", "More Warm" + nValue);
        } else if (progress < 50) {
            nValue = (this.mLessWarm * (progress - 50)) / 50;
            setWarmValue(nValue);
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "EyeComfortSeekBarPreference", "Less Warm" + nValue);
        } else {
            setWarmValue(0);
        }
    }

    private void setWarmValue(int nValue) {
        System.putIntForUser(getContentResolver(), "user_set_warm", nValue, UserHandle.myUserId());
    }

    private int getWarmValue() {
        return System.getIntForUser(getContentResolver(), "user_set_warm", 0, UserHandle.myUserId());
    }

    private void updateSwitchStatus() {
        int status = System.getIntForUser(getContentResolver(), "eyes_protection_mode", 0, UserHandle.myUserId());
        int statusSchedule = System.getIntForUser(getContentResolver(), "eye_comfort_schedule_switch", 0, UserHandle.myUserId());
        if (2 != status && this.mSwitch != null && this.mScheduleSwitch != null && this.mSeekBarPreference != null) {
            this.mSwitch.setChecked(status != 0);
            if (status == 0 || 3 == status) {
                this.mScheduleSwitch.setEnabled(true);
            } else {
                this.mScheduleSwitch.setEnabled(false);
            }
            if (statusSchedule == 1 && (status == 0 || status == 3)) {
                this.mStart.setEnabled(true);
                this.mEnd.setEnabled(true);
            } else {
                this.mStart.setEnabled(false);
                this.mEnd.setEnabled(false);
                setValidTime(false);
            }
            if (status == 3) {
                setValidTime(false);
            }
            if (status == 0) {
                this.mSeekBarPreference.setEnabled(false);
            } else {
                this.mSeekBarPreference.setEnabled(true);
            }
        }
    }

    private void updateScheduleSwitchStatus() {
        int status = System.getIntForUser(getContentResolver(), "eye_comfort_schedule_switch", 0, UserHandle.myUserId());
        int statusEyeComfort = System.getIntForUser(getContentResolver(), "eyes_protection_mode", 0, UserHandle.myUserId());
        if (2 != status && this.mSwitch != null && this.mScheduleSwitch != null && this.mStart != null && this.mEnd != null) {
            this.mScheduleSwitch.setChecked(status != 0);
            if (status == 0 && statusEyeComfort == 3) {
                this.mSwitch.setChecked(false);
            }
            if (status == 1 && (statusEyeComfort == 0 || statusEyeComfort == 3)) {
                this.mStart.setEnabled(true);
                this.mEnd.setEnabled(true);
            } else {
                this.mStart.setEnabled(false);
                this.mEnd.setEnabled(false);
            }
        }
    }

    private void updateSeekbar() {
        if (this.mSeekBarPreference != null) {
            int warmvalue = getWarmValue();
            if (warmvalue > 0) {
                warmvalue = ((warmvalue * 50) / this.mMoreWarm) + 50;
            } else if (warmvalue < 0) {
                warmvalue = ((warmvalue * 50) / this.mLessWarm) + 50;
            } else {
                warmvalue = 50;
            }
            this.mSeekBarPreference.setProgress(warmvalue);
        }
    }

    private void updateTime() {
        this.mStart.setTime(this.mScheduleInfo.mStartHour, this.mScheduleInfo.mStartMinute);
        this.mEnd.setTime(this.mScheduleInfo.mEndHour, this.mScheduleInfo.mEndMinute);
        updateEndSummary();
    }

    private void updateEndSummary() {
        this.mEnd.setSummaryFormat(getNextDayFlag() ? 2131626835 : 0);
    }

    private void showToast2() {
        Calendar c = Calendar.getInstance();
        c.set(11, this.mScheduleInfo.mStartHour);
        c.set(12, this.mScheduleInfo.mStartMinute);
        String time = DateFormat.getTimeFormat(getActivity()).format(c.getTime());
        Toast.makeText(getActivity(), getActivity().getResources().getString(2131628549, new Object[]{time}), 1).show();
    }

    private void setEyeComfortSwitch(boolean enable) {
        int i = 1;
        int status = System.getIntForUser(getContentResolver(), "eyes_protection_mode", 0, UserHandle.myUserId());
        if (status != 3 || !enable) {
            if (status != 3 || enable) {
                setValidTime(false);
            } else {
                setValidTime(true);
                showToast2();
            }
            ContentResolver contentResolver = getContentResolver();
            String str = "eyes_protection_mode";
            if (!enable) {
                i = 0;
            }
            System.putIntForUser(contentResolver, str, i, UserHandle.myUserId());
        }
    }

    private void setEyeComfortScheduleSwitch(boolean enable) {
        System.putIntForUser(getContentResolver(), "eye_comfort_schedule_switch", enable ? 1 : 0, UserHandle.myUserId());
    }

    private void setValidTime(boolean bValidFlg) {
        long validtime = 0;
        if (bValidFlg) {
            validtime = getValidTime();
        }
        System.putLongForUser(getContentResolver(), "eye_comfort_valid", validtime, UserHandle.myUserId());
    }

    private long getValidTime() {
        Calendar c = Calendar.getInstance();
        c.set(5, c.get(5) + 1);
        return c.getTimeInMillis();
    }
}
