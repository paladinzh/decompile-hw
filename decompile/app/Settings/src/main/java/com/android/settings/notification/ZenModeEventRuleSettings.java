package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.ItemUseStat;
import com.android.settings.notification.ZenModeEventProvider.CalendarInfo;
import java.util.ArrayList;
import java.util.List;

public class ZenModeEventRuleSettings extends ZenModeRuleSettingsBase {
    private static final String[] ENTRY_VALUES_REPLY = new String[]{"REPLY_ANY_EXCEPT_NO", "REPLY_YES_OR_MAYBE", "REPLY_YES"};
    private ListPreference mCalendar;
    private List<CalendarInfo> mCalendars;
    private boolean mCreate;
    private EventInfo mEvent;
    private ListPreference mReply;

    protected boolean setRule(AutomaticZenRule rule) {
        EventInfo eventInfo = null;
        if (rule != null) {
            eventInfo = ZenModeConfig.tryParseEventConditionId(rule.getConditionId());
        }
        this.mEvent = eventInfo;
        return this.mEvent != null;
    }

    public void onResume() {
        if (!this.mCreate) {
            reloadCalendar();
        }
        super.onResume();
        if (!isUiRestricted()) {
            this.mCreate = false;
        }
    }

    private void reloadCalendar() {
        ZenModeEventProvider.getInstance();
        this.mCalendars = ZenModeEventProvider.getCalendars(this.mContext);
        ArrayList<CharSequence> calendarEntries = new ArrayList();
        ArrayList<CharSequence> calendarValues = new ArrayList();
        calendarEntries.add(getResources().getString(2131626798));
        ZenModeEventProvider.getInstance();
        calendarValues.add(ZenModeEventProvider.key(0, null));
        String str = this.mEvent != null ? this.mEvent.calendar : null;
        for (CalendarInfo calendar : this.mCalendars) {
            calendarEntries.add(calendar.displayName);
            ZenModeEventProvider.getInstance();
            calendarValues.add(ZenModeEventProvider.key(calendar));
            if (str != null && str.equals(calendar.name)) {
            }
        }
        if (this.mCalendar != null) {
            this.mCalendar.setEntries((CharSequence[]) calendarEntries.toArray(new CharSequence[calendarEntries.size()]));
            this.mCalendar.setEntryValues((CharSequence[]) calendarValues.toArray(new CharSequence[calendarValues.size()]));
        }
    }

    protected void onCreateInternal() {
        if (this.mEvent != null) {
            this.mCreate = true;
            addPreferencesFromResource(2131230950);
            PreferenceScreen root = getPreferenceScreen();
            this.mCalendar = (ListPreference) root.findPreference("calendar");
            this.mCalendar.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String calendarKey = (String) newValue;
                    ZenModeEventRuleSettings.this.updateRuleSchedule(calendarKey);
                    String selectedCalendar = "ALL";
                    if (ZenModeEventRuleSettings.this.mCalendar.findIndexOfValue(calendarKey) > 0) {
                        selectedCalendar = calendarKey.substring(calendarKey.indexOf(58) + 1);
                    }
                    ItemUseStat.getInstance().handleClick(ZenModeEventRuleSettings.this.getActivity(), 2, ZenModeEventRuleSettings.this.mCalendar.getKey(), selectedCalendar);
                    return true;
                }
            });
            this.mReply = (ListPreference) root.findPreference("reply");
            this.mReply.setEntries(new CharSequence[]{getResources().getString(2131626800), getResources().getString(2131626801), getResources().getString(2131626802)});
            this.mReply.setEntryValues(new CharSequence[]{String.valueOf(0), String.valueOf(1), String.valueOf(2)});
            this.mReply.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int reply = Integer.parseInt((String) newValue);
                    ItemUseStat.getInstance().handleClickListPreference(ZenModeEventRuleSettings.this.getActivity(), ZenModeEventRuleSettings.this.mReply, ZenModeEventRuleSettings.ENTRY_VALUES_REPLY, String.valueOf(reply));
                    if (reply == ZenModeEventRuleSettings.this.mEvent.reply) {
                        return false;
                    }
                    ZenModeEventRuleSettings.this.mEvent.reply = reply;
                    ZenModeEventRuleSettings.this.updateRule(ZenModeConfig.toEventConditionId(ZenModeEventRuleSettings.this.mEvent));
                    return true;
                }
            });
            reloadCalendar();
            updateControlsInternal();
        }
    }

    protected void updateControlsInternal() {
        ListPreference listPreference = this.mCalendar;
        ZenModeEventProvider.getInstance();
        int selectedIndex = ZenModeUtils.setSelectedValue(listPreference, ZenModeEventProvider.key(this.mEvent));
        if (selectedIndex < 0) {
            selectedIndex = 0;
            ZenModeEventProvider.getInstance();
            updateRuleSchedule(ZenModeEventProvider.key(0, null));
            listPreference = this.mCalendar;
            ZenModeEventProvider.getInstance();
            ZenModeUtils.setSelectedValue(listPreference, ZenModeEventProvider.key(this.mEvent));
        }
        if (selectedIndex == 0 || "com.android.exchange".equals(((CalendarInfo) this.mCalendars.get(selectedIndex - 1)).accountType)) {
            if (findPreference("reply") == null) {
                getPreferenceScreen().addPreference(this.mReply);
            }
            ZenModeUtils.setSelectedValue(this.mReply, String.valueOf(this.mEvent.reply));
            return;
        }
        removePreference("reply");
    }

    protected int getMetricsCategory() {
        return 146;
    }

    private void updateRuleSchedule(String calendarKey) {
        if (this.mEvent != null) {
            ZenModeEventProvider.getInstance();
            if (!calendarKey.equals(ZenModeEventProvider.key(this.mEvent))) {
                int i = calendarKey.indexOf(58);
                this.mEvent.userId = Integer.parseInt(calendarKey.substring(0, i));
                this.mEvent.calendar = calendarKey.substring(i + 1);
                if (this.mEvent.calendar.isEmpty()) {
                    this.mEvent.calendar = null;
                }
                updateRule(ZenModeConfig.toEventConditionId(this.mEvent));
            }
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            getActivity().getActionBar().setTitle(2131628619);
            setDeleteTitle(2131628619);
        }
    }
}
