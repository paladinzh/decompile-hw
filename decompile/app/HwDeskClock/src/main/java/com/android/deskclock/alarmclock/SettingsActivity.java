package com.android.deskclock.alarmclock;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.RingtoneHelper;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.alarmclock.AlarmSetDialogManager.SelectDialogCallBack;
import com.android.deskclock.worldclock.City.LocationColumns;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.util.ClockReporter;
import com.android.util.CompatUtils;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class SettingsActivity extends Activity {
    private static int mClockStyleValue = 0;
    private PlaceholderFragment fragment;
    private FrameLayout mFrameLayout;
    private LinearLayout mLinearLayout;

    public static class PlaceholderFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
        private static final String[] PROJECTION = new String[]{"_id"};
        private static final String[] SELECTIONARGS = new String[]{"Beep-Beep-Beep Alarm"};
        private String defaultRingtone;
        OnClickListener dialogInterface = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
                        intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", PlaceholderFragment.this.onRestoreRingtone());
                        intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
                        intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
                        intent.putExtra("android.intent.extra.ringtone.TYPE", 4);
                        intent.putExtra("android.intent.extra.ringtone.TITLE", PlaceholderFragment.this.getActivity().getTitle());
                        PlaceholderFragment.this.startActivityForResult(intent, 14);
                        return;
                    case 1:
                        try {
                            Intent musicIntent = new Intent("android.intent.action.PICK");
                            musicIntent.setData(Uri.parse("content://media/external/audio/media"));
                            musicIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", PlaceholderFragment.this.onRestoreRingtone());
                            PlaceholderFragment.this.startActivityForResult(musicIntent, 15);
                            return;
                        } catch (RuntimeException e) {
                            Log.e("settings", "onClick : RuntimeException = " + e.getMessage());
                            return;
                        }
                    default:
                        return;
                }
            }
        };
        private Activity mActivity;
        private SwitchPreference mAlarmSlient;
        private Dialog mAlertDialog;
        private int mBellChoiceNum = 1;
        private String mChoice;
        private int mChoiceNum = 1;
        private TextArrowPreference mClockStyle;
        private Preference mDateTime;
        private SwitchPreference mDisplayHome;
        private Editor mEditor;
        private Preference mHomeTime;
        private View mHomeTimeView;
        private SharedPreferences mPreferences;
        private SyncQueryRington mQueryRington;
        private Preference mRingDuration;
        private Editor mRingEditor;
        private TextArrowPreference mRingtone;
        private boolean mSettingState = false;
        private Preference mSnoozeDuration;
        private SeekBar mSnoozeMin;
        private SeekBar mSnoozeTimers;
        private Uri mTempUri;
        private long mTime;
        private Editor mTimerEditor;
        private SharedPreferences mTimerPreferences;
        private TextArrowPreference mVlumeButtons;
        private int min = 0;
        private int num = 0;
        private BroadcastReceiver syncDatereceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                PlaceholderFragment.this.mHomeTime.setSummary(PlaceholderFragment.this.buildGmtDisplayName(PlaceholderFragment.this.mPreferences.getString("home_city_timezone", ""), TimeZoneUtils.getTimeZoneMapValue(context, PlaceholderFragment.this.mPreferences.getString("home_time_index", "")), new StringBuilder()));
                PlaceholderFragment.this.initHomeTime();
                PlaceholderFragment.this.initSettingsValue();
            }
        };

        private class SyncQueryRington extends AsyncQueryHandler {
            private boolean finished = false;

            public SyncQueryRington(ContentResolver cr) {
                super(cr);
            }

            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                if (PlaceholderFragment.this.getActivity() != null && token == 1) {
                    if (cursor == null || !cursor.moveToFirst()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (this.finished) {
                            HwLog.d("SyncQueryRington", "get default");
                        } else {
                            startQuery(1, Integer.valueOf(0), Uri.parse("content://media/internal/audio/media"), PlaceholderFragment.PROJECTION, "title= ?", PlaceholderFragment.SELECTIONARGS, null);
                            this.finished = true;
                        }
                    } else {
                        if (this.finished) {
                            HwLog.d("SyncQueryRington", "get title");
                        } else {
                            HwLog.d("SyncQueryRington", "get display_name");
                        }
                        PlaceholderFragment.this.defaultRingtone = "content://media/internal/audio/media/" + cursor.getInt(0);
                        cursor.close();
                    }
                }
            }
        }

        private void queryRington() {
            Log.dRelease("settings", "startQuery");
            this.defaultRingtone = "silent";
            String titleRingtone = SystemProperties.get("ro.config.deskclock_timer_alert", "Timer_Beep.ogg");
            if (this.mQueryRington != null) {
                this.mQueryRington.startQuery(1, Integer.valueOf(0), Uri.parse("content://media/internal/audio/media"), PROJECTION, "_display_name= ?", new String[]{titleRingtone}, null);
            }
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            Log.i("settings", "onActivityCreated");
            super.onActivityCreated(savedInstanceState);
            TimeZoneUtils.registerUpdateUIBroadcast(getActivity(), this.syncDatereceiver);
            initSettingsValue();
            scrollWindow();
            initHomeTime();
            this.mQueryRington = new SyncQueryRington(getActivity().getContentResolver());
            queryRington();
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.alarm_settings);
            initSettingsView();
        }

        public void initHomeTime() {
            if (this.mPreferences == null) {
                this.mPreferences = Utils.getSharedPreferences(this.mActivity, "setting_activity", 0);
            }
            if (this.mPreferences.getBoolean("ISCHECKED", false)) {
                setHomeTimeEnable(true);
            } else {
                setHomeTimeEnable(false);
            }
        }

        public void onResume() {
            super.onResume();
            getListView().setDivider(null);
        }

        private void scrollWindow() {
            Intent intent = this.mActivity.getIntent();
            if (intent != null) {
                String action = intent.getAction();
                ListView listView = getListView();
                if ("action_from_alarm".equals(action)) {
                    listView.setSelection(0);
                } else if ("action_from_worldclock".equals(action)) {
                    listView.setSelection(5);
                } else if ("action_from_clockstyle".equals(action)) {
                    listView.setSelection(9);
                } else if ("action_from_timer".equals(action)) {
                    listView.setSelection(11);
                }
            }
        }

        private void initSettingsView() {
            this.mAlarmSlient = (SwitchPreference) findPreference("alarm_in_slient_mode");
            this.mRingDuration = findPreference("ring_duration");
            this.mSnoozeDuration = findPreference("snooze_duration_settings");
            this.mVlumeButtons = (TextArrowPreference) findPreference("vloume_buttons");
            this.mDisplayHome = (SwitchPreference) findPreference("display_home_clock");
            this.mHomeTime = findPreference("home_time_zone");
            this.mDateTime = findPreference("date_time");
            this.mClockStyle = (TextArrowPreference) findPreference("clock_stylep");
            this.mRingtone = (TextArrowPreference) findPreference("ringtone");
            this.mAlarmSlient.setOnPreferenceClickListener(this);
            this.mAlarmSlient.setOnPreferenceChangeListener(this);
            this.mRingDuration.setOnPreferenceClickListener(this);
            this.mSnoozeDuration.setOnPreferenceClickListener(this);
            this.mVlumeButtons.setOnPreferenceClickListener(this);
            this.mDisplayHome.setOnPreferenceClickListener(this);
            this.mDisplayHome.setOnPreferenceChangeListener(this);
            this.mHomeTime.setOnPreferenceClickListener(this);
            this.mDateTime.setOnPreferenceClickListener(this);
            this.mClockStyle.setOnPreferenceClickListener(this);
            this.mRingtone.setOnPreferenceClickListener(this);
            this.mHomeTimeView = this.mHomeTime.getView(null, null);
        }

        public void initSettingsValue() {
            boolean z;
            this.mActivity = getActivity();
            this.mPreferences = Utils.getSharedPreferences(this.mActivity, "setting_activity", 0);
            this.mEditor = this.mPreferences.edit();
            this.mTimerPreferences = Utils.getSharedPreferences(this.mActivity, "timer", 0);
            this.mTimerEditor = this.mTimerPreferences.edit();
            SharedPreferences preference = Utils.getDefaultSharedPreferences(getActivity());
            int silentModeStreams = System.getInt(this.mActivity.getContentResolver(), "mode_ringer_streams_affected", 0);
            SwitchPreference switchPreference = this.mAlarmSlient;
            if (silentModeStreams == 294) {
                z = true;
            } else {
                z = false;
            }
            switchPreference.setChecked(z);
            this.mBellChoiceNum = preference.getInt("bell_duration_choice_num", 1);
            setBellLengthStr(preference);
            this.min = preference.getInt("snooze_duration", 10);
            this.num = preference.getInt("snooze_timers", 3);
            String mSnoozeMinSeekbar = getResources().getQuantityString(R.plurals.setting_intervals, this.min, new Object[]{Integer.valueOf(this.min)});
            this.mSnoozeDuration.setSummary(mSnoozeMinSeekbar + getResources().getQuantityString(R.plurals.setting_silence, this.num, new Object[]{Integer.valueOf(this.num)}));
            this.mChoiceNum = this.mPreferences.getInt("choice", 1);
            this.mChoice = getResources().getStringArray(R.array.volume_button_setting_entries)[this.mChoiceNum];
            this.mVlumeButtons.setDetail(this.mChoice);
            this.mTime = System.currentTimeMillis();
            initDisplayHome();
            initRingtone();
            getClockStyle();
        }

        public void initRingtone() {
            String defaultRing = SystemProperties.get("ro.config.deskclock_timer_alert", "Timer_Beep.ogg");
            if ("Timer_Beep.ogg".equals(defaultRing)) {
                defaultRing = this.mActivity.getResources().getString(R.string.default_rington);
            } else if (defaultRing.endsWith(".ogg")) {
                defaultRing = defaultRing.substring(0, defaultRing.lastIndexOf(".ogg"));
            }
            if ("silent".equals(this.mTimerPreferences.getString("ringtone", null))) {
                this.mRingtone.setDetail(getActivity().getResources().getString(R.string.silent_alarm_summary));
                return;
            }
            this.mRingtone.setDetail(this.mTimerPreferences.getString("setting_ringtone", defaultRing));
        }

        public void initDisplayHome() {
            String tz = this.mPreferences.getString("home_city_timezone", "");
            String id = this.mPreferences.getString("home_time_index", "");
            boolean checked = this.mPreferences.getBoolean("ISCHECKED", false);
            if (checked) {
                this.mDisplayHome.setChecked(checked);
            }
            if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(tz)) {
                StringBuilder cityGmtName = new StringBuilder();
                this.mHomeTime.setSummary(buildGmtDisplayName(tz, TimeZoneUtils.getTimeZoneMapValue(this.mActivity, id), cityGmtName));
            }
        }

        public void getClockStyle() {
            SettingsActivity.mClockStyleValue = this.mPreferences.getInt("clock_style", 1);
            int summaryResId = 0;
            if (SettingsActivity.mClockStyleValue == 0) {
                summaryResId = R.string.digital_clock;
            } else if (SettingsActivity.mClockStyleValue == 1) {
                summaryResId = R.string.clockstyle_analog;
            }
            this.mClockStyle.setDetail(getActivity().getResources().getString(summaryResId));
        }

        public void setHomeTimeEnable(boolean flag) {
            if (flag) {
                this.mHomeTime.setEnabled(true);
                this.mHomeTimeView.setEnabled(true);
                this.mHomeTimeView.setAlpha(0.85f);
                return;
            }
            this.mHomeTime.setEnabled(false);
            this.mHomeTimeView.setEnabled(false);
            this.mHomeTimeView.setAlpha(0.5f);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ("alarm_in_slient_mode".equals(preference.getKey())) {
                doSwitchAlarmInSlient(this.mActivity, newValue);
                this.mEditor.putBoolean("alarm_in_silent", ((Boolean) newValue).booleanValue());
                this.mEditor.commit();
                return true;
            } else if ("display_home_clock".equals(preference.getKey())) {
                return doSwitchDisplayHomeClock(newValue);
            } else {
                return false;
            }
        }

        public boolean onPreferenceClick(Preference preference) {
            if ("alarm_in_slient_mode".equals(preference.getKey())) {
                return true;
            }
            if ("ring_duration".equals(preference.getKey())) {
                doRingDuration();
                return true;
            } else if ("snooze_duration_settings".equals(preference.getKey())) {
                doSnoozeDurationSettings();
                return true;
            } else if ("vloume_buttons".equals(preference.getKey())) {
                doVolumeButtons();
                return true;
            } else if ("display_home_clock".equals(preference.getKey())) {
                return true;
            } else {
                if ("home_time_zone".equals(preference.getKey())) {
                    doHomeTimeZone();
                    return true;
                } else if ("date_time".equals(preference.getKey())) {
                    doDateAndTime();
                    return true;
                } else if ("clock_stylep".equals(preference.getKey())) {
                    doClockStyle();
                    return true;
                } else if (!"ringtone".equals(preference.getKey())) {
                    return false;
                } else {
                    doRington();
                    return true;
                }
            }
        }

        public void doClockStyle() {
            dismissDialog();
            Builder builder = new Builder(this.mActivity);
            final String[] items = getResources().getStringArray(R.array.Clock_style);
            builder.setTitle(R.string.clock_style);
            builder.setSingleChoiceItems(items, SettingsActivity.mClockStyleValue, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SettingsActivity.mClockStyleValue = which;
                    PlaceholderFragment.this.mClockStyle.setDetail(items[which]);
                    PlaceholderFragment.this.mEditor.putInt("clock_style", which);
                    PlaceholderFragment.this.mEditor.commit();
                    PlaceholderFragment.this.mAlertDialog.dismiss();
                }
            });
            builder.setNegativeButton(17039360, null);
            this.mAlertDialog = builder.create();
            this.mAlertDialog.show();
        }

        public void doRington() {
            if (CompatUtils.hasPermission(this.mActivity, "android.permission.READ_EXTERNAL_STORAGE")) {
                showRingtoneList(this.mActivity);
                return;
            }
            Log.iRelease("settings", "onClickListener->onClick->set ringtong->has no permission");
            CompatUtils.grantPermissionsByManager(this, 101);
        }

        private void showRingtoneList(Activity activity) {
            Intent ringtoneIntent = new Intent("android.intent.action.RINGTONE_PICKER");
            ringtoneIntent.addCategory("android.intent.category.HWRING");
            if (activity.getPackageManager().queryIntentActivities(ringtoneIntent, 0).size() > 0) {
                Intent ringtone_intent = new Intent();
                ringtone_intent.setAction("android.intent.action.RINGTONE_PICKER");
                ringtone_intent.addCategory("android.intent.category.HWRING");
                ringtone_intent.putExtra("android.intent.extra.ringtone.TYPE", 4);
                ringtone_intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", onRestoreRingtone());
                ringtone_intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
                ringtone_intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
                ringtone_intent.putExtra("android.intent.extra.ringtone.INCLUDE_DRM", false);
                try {
                    if (!this.mSettingState) {
                        startActivityForResult(ringtone_intent, 0);
                        this.mSettingState = true;
                        return;
                    }
                    return;
                } catch (Exception e) {
                    Log.e("settings", "onClick : Exception = " + e.getMessage());
                    return;
                }
            }
            String[] alarmRingtoneType = getResources().getStringArray(R.array.alarm_ringtone_filters);
            Builder builder = new Builder(activity);
            builder.setTitle(R.string.alert);
            builder.setCancelable(true);
            builder.setItems(alarmRingtoneType, this.dialogInterface);
            builder.create().show();
        }

        protected Uri onRestoreRingtone() {
            Uri ringtoneUri = Uri.parse(this.mTimerPreferences.getString("ringtone", this.defaultRingtone));
            if (ringtoneUri == null) {
                return Uri.parse(this.defaultRingtone);
            }
            if ("silent".equals(ringtoneUri.toString())) {
                return null;
            }
            if (!this.defaultRingtone.equals(ringtoneUri.toString())) {
                ringtoneUri = RingtoneHelper.getUriByPath(DeskClockApplication.getDeskClockApplication(), ringtoneUri);
            }
            if ("content://settings/system/ringtone1".equals(ringtoneUri.toString())) {
                ringtoneUri = Uri.parse(this.defaultRingtone);
            }
            return ringtoneUri;
        }

        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            Log.iRelease("settings", "onRequestPermissionsResult  requestCode =" + requestCode);
            onActivityResult(requestCode, -1, null);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data == null || requestCode != 5) {
                handleResult(requestCode, resultCode, data);
                return;
            }
            Bundle bundle = data.getExtras();
            if (bundle != null && resultCode == -1) {
                updateDataSource(bundle);
            }
        }

        public void handleResult(int requestCode, int resultCode, Intent data) {
            this.mSettingState = false;
            String ringTitle;
            if (requestCode == 0) {
                if (resultCode == -1 && data != null) {
                    this.mTempUri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                    if (CompatUtils.hasPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE")) {
                        String ringUri = RingtoneHelper.getActualUri(DeskClockApplication.getDeskClockApplication(), this.mTempUri);
                        ringTitle = RingtoneHelper.getActualUriTitle(DeskClockApplication.getDeskClockApplication(), this.mTempUri);
                        if (ringTitle == null) {
                            ringTitle = this.mActivity.getResources().getString(R.string.silent_alarm_summary);
                            this.mRingtone.setDetail(ringTitle);
                        } else {
                            this.mRingtone.setDetail(ringTitle);
                        }
                        this.mTimerEditor.putString("setting_ringtone", ringTitle);
                        this.mTimerEditor.putString("ringtone", ringUri);
                        this.mTimerEditor.commit();
                    } else {
                        Log.iRelease("settings", "onActivityResult->has no permission");
                        CompatUtils.grantPermissionsByManager(this, 102);
                    }
                }
            } else if (requestCode == 101) {
                if (CompatUtils.hasPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE")) {
                    showRingtoneList(getActivity());
                }
            } else if (requestCode == 102 && CompatUtils.hasPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE") && this.mTempUri != null) {
                this.mTimerEditor.putString("ringtone", RingtoneHelper.getActualUri(DeskClockApplication.getDeskClockApplication(), this.mTempUri));
                this.mTimerEditor.commit();
                ringTitle = RingtoneHelper.getActualUriTitle(DeskClockApplication.getDeskClockApplication(), this.mTempUri);
                if (ringTitle == null) {
                    ringTitle = this.mActivity.getResources().getString(R.string.silent_alarm_summary);
                    this.mRingtone.setDetail(ringTitle);
                } else {
                    this.mRingtone.setDetail(ringTitle);
                }
                this.mTimerEditor.putString("setting_ringtone", ringTitle);
                this.mTimerEditor.commit();
            }
        }

        public void doVolumeButtons() {
            ClockReporter.reportEventMessage(getActivity(), 41, "");
            dismissDialog();
            this.mAlertDialog = AlarmSetDialogManager.getInstance().createSingleDialogSetting(getActivity(), this.mChoiceNum, new SelectDialogCallBack() {
                public void confirm(SparseBooleanArray selectArray) {
                }

                public void confirm(int choice) {
                    Log.d("settings", "choice:" + choice);
                    PlaceholderFragment.this.mChoiceNum = choice;
                    PlaceholderFragment.this.mChoice = PlaceholderFragment.this.getResources().getStringArray(R.array.volume_button_setting_entries)[choice];
                    PlaceholderFragment.this.mEditor.putInt("choice", choice);
                    PlaceholderFragment.this.mEditor.commit();
                    PlaceholderFragment.this.mVlumeButtons.setDetail(PlaceholderFragment.this.mChoice);
                }

                public void confirm(String selectStr) {
                }

                public void confirm(String selectStr, ArrayList<Integer> arrayList, int selectId) {
                }

                public void cancel() {
                }
            });
            if (this.mAlertDialog != null) {
                this.mAlertDialog.show();
            }
        }

        public void doHomeTimeZone() {
            ClockReporter.reportEventMessage(getActivity(), 49, "");
            Bundle bundle = new Bundle();
            bundle.putInt("request_type", 7);
            bundle.putString("request_description", "HWDESKCLOCK_HOME_TIME_ZONE");
            TimeZoneUtils.startPickZoneActivity((Fragment) this, 5, bundle);
        }

        public static void doSwitchAlarmInSlient(Context context, Object newValue) {
            int i = 0;
            boolean checked = ((Boolean) newValue).booleanValue();
            int ringerModeStreamTypes = System.getInt(context.getContentResolver(), "mode_ringer_streams_affected", 0);
            if (checked) {
                ringerModeStreamTypes &= -17;
                if (ringerModeStreamTypes != 294) {
                    ringerModeStreamTypes = 294;
                }
            } else {
                ringerModeStreamTypes |= 16;
            }
            String str = "SILENT";
            if (checked) {
                i = 1;
            }
            ClockReporter.reportEventContainMessage(context, 34, str, i);
            setModeRingerStreamAffectedSafty(context, ringerModeStreamTypes);
            Log.dRelease("settings", "onClick : ringerModeStreamTypes = " + ringerModeStreamTypes);
        }

        public boolean doSwitchDisplayHomeClock(Object newValue) {
            boolean checked = ((Boolean) newValue).booleanValue();
            String tz = this.mPreferences.getString("home_city_timezone", "");
            String id = this.mPreferences.getString("home_time_index", "");
            ClockReporter.reportEventContainMessage(getActivity(), 48, "HOMETIMESWITCH", checked ? 1 : 0);
            if (!checked) {
                setHomeTimeEnable(false);
                this.mEditor.putBoolean("ISCHECKED", false);
                this.mEditor.commit();
                if (!(tz.equals("") && id.equals(""))) {
                    switchUpdate(true, id, tz);
                }
            } else if (unableOpenHomeTime()) {
                ToastMaster.showToast(getActivity(), (int) R.string.city_full_Toast, 1);
                this.mDisplayHome.setChecked(false);
                return false;
            } else {
                setHomeTimeEnable(true);
                this.mEditor.putBoolean("ISCHECKED", true);
                this.mEditor.commit();
                if (!(tz.equals("") && id.equals(""))) {
                    switchUpdate(false, id, tz);
                }
            }
            return true;
        }

        public void doRingDuration() {
            ClockReporter.reportEventMessage(this.mActivity, 35, "");
            dismissDialog();
            this.mAlertDialog = AlarmSetDialogManager.getInstance().createBellLengthSDg(this.mActivity, this.mBellChoiceNum, new SelectDialogCallBack() {
                public void confirm(String selectStr, ArrayList<Integer> arrayList, int selectId) {
                }

                public void cancel() {
                }

                public void confirm(String selectStr) {
                }

                public void confirm(int choice) {
                    PlaceholderFragment.this.mBellChoiceNum = choice;
                    SharedPreferences shPreferences = Utils.getDefaultSharedPreferences(PlaceholderFragment.this.mActivity);
                    int duration = 1;
                    switch (choice) {
                        case 0:
                            duration = 1;
                            break;
                        case 1:
                            duration = 5;
                            break;
                        case 2:
                            duration = 10;
                            break;
                        case 3:
                            duration = 15;
                            break;
                        case MetaballPath.POINT_NUM /*4*/:
                            duration = 20;
                            break;
                        case 5:
                            duration = 30;
                            break;
                    }
                    shPreferences.edit().putInt("bell_duration_choice_num", PlaceholderFragment.this.mBellChoiceNum).putInt("bell_duration", duration).apply();
                    PlaceholderFragment.this.setBellLengthStr(shPreferences);
                }

                public void confirm(SparseBooleanArray selectArray) {
                }
            });
            if (this.mAlertDialog != null) {
                this.mAlertDialog.show();
            }
        }

        public void doDateAndTime() {
            ClockReporter.reportEventMessage(this.mActivity, 47, "");
            startActivity(new Intent("android.settings.DATE_SETTINGS"));
        }

        public void doSnoozeDurationSettings() {
            dismissDialog();
            this.mAlertDialog = createRingDialog();
            this.mAlertDialog.show();
        }

        private Dialog createRingDialog() {
            final SharedPreferences preference = Utils.getDefaultSharedPreferences(this.mActivity);
            this.mRingEditor = preference.edit();
            AlertDialog textDialog = new Builder(this.mActivity).setIconAttribute(16843605).setTitle(R.string.duration_dialog_title).setPositiveButton(R.string.Dialog_alarm_OK, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ClockReporter.reportEventContainMessage(PlaceholderFragment.this.mActivity, 39, "KEY_ALARM_SNOOZE", PlaceholderFragment.this.min);
                    ClockReporter.reportEventContainMessage(PlaceholderFragment.this.mActivity, 39, "KEY_ALARM_SNOOZE_NUM", PlaceholderFragment.this.num);
                    PlaceholderFragment.this.mRingEditor.putInt("snooze_duration", PlaceholderFragment.this.min);
                    PlaceholderFragment.this.mRingEditor.putInt("snooze_timers", PlaceholderFragment.this.num);
                    PlaceholderFragment.this.mRingEditor.commit();
                    PlaceholderFragment.this.setSnoozeTypeStr(preference);
                    dialog.dismiss();
                }
            }).setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ClockReporter.reportEventMessage(PlaceholderFragment.this.mActivity, 40, "");
                }
            }).create();
            View setRingView = textDialog.getLayoutInflater().inflate(R.layout.setting_dialog, (ViewGroup) null);
            setLocalTextView(setRingView);
            textDialog.setView(setRingView);
            this.mSnoozeTimers = (SeekBar) setRingView.findViewById(R.id.SnoozeTimers);
            this.mSnoozeMin = (SeekBar) setRingView.findViewById(R.id.SnoozeMin);
            LinearLayout snoozeMinIndexLyt = (LinearLayout) setRingView.findViewById(R.id.SnoozeMin_index);
            LinearLayout snoozeTimersIndexLyt = (LinearLayout) setRingView.findViewById(R.id.SnoozeTimers_index);
            if (Locale.getDefault().getLanguage().contains("ur")) {
                snoozeMinIndexLyt.setLayoutDirection(0);
                snoozeTimersIndexLyt.setLayoutDirection(0);
            }
            this.min = preference.getInt("snooze_duration", 10);
            int snoozemin = getSnoozeMin(this.min);
            this.num = preference.getInt("snooze_timers", 3);
            int snooze = getSnoozeTimers(this.num);
            this.mSnoozeMin.setProgress(snoozemin);
            this.mSnoozeTimers.setProgress(snooze);
            String mSnoozeMinSeekbar = getResources().getQuantityString(R.plurals.setting_intervals, this.min, new Object[]{Integer.valueOf(this.min)});
            String mSnoozeTimersSeekbar = getResources().getQuantityString(R.plurals.setting_silence, this.num, new Object[]{Integer.valueOf(this.num)});
            this.mSnoozeMin.setContentDescription(mSnoozeMinSeekbar);
            this.mSnoozeTimers.setContentDescription(mSnoozeTimersSeekbar);
            listenSeekbar();
            return textDialog;
        }

        private void setSnoozeTypeStr(SharedPreferences preference) {
            this.min = preference.getInt("snooze_duration", 10);
            this.num = preference.getInt("snooze_timers", 3);
            String mSnoozeMinSeekbar = getResources().getQuantityString(R.plurals.setting_intervals, this.min, new Object[]{Integer.valueOf(this.min)});
            this.mSnoozeDuration.setSummary(mSnoozeMinSeekbar + getResources().getQuantityString(R.plurals.setting_silence, this.num, new Object[]{Integer.valueOf(this.num)}));
        }

        private void setLocalTextView(View view) {
            if (view != null) {
                int[] textViewId = new int[]{R.id.snooze_text_1, R.id.snooze_text_2, R.id.snooze_text_3, R.id.snooze_text_4, R.id.snooze_text_5, R.id.snooze_text_6, R.id.repeat_text_1, R.id.repeat_text_2, R.id.repeat_text_3, R.id.repeat_text_4};
                int i = 0;
                int length = textViewId.length;
                while (i < length) {
                    TextView textView = (TextView) view.findViewById(textViewId[i]);
                    if (textView != null) {
                        textView.setText(Utils.getLocalDigitString((long) Integer.parseInt(textView.getText().toString())));
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }

        private int getSnoozeMin(int min) {
            switch (min) {
                case 5:
                    return 0;
                case 10:
                    return 20;
                case 15:
                    return 40;
                case 20:
                    return 60;
                case PortCallPanelView.DEFAUT_RADIUS /*25*/:
                    return 80;
                case 30:
                    return 100;
                default:
                    return 5;
            }
        }

        private int getSnoozeTimers(int min) {
            switch (this.num) {
                case 1:
                    return 0;
                case 3:
                    return 30;
                case 5:
                    return 60;
                case 10:
                    return 90;
                default:
                    return 1;
            }
        }

        private void listenSeekbar() {
            this.mSnoozeMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int seekProgress = seekBar.getProgress();
                    if (seekProgress < 10) {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(0);
                    } else if (seekProgress < 30) {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(20);
                    } else if (seekProgress < 50) {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(40);
                    } else if (seekProgress < 70) {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(60);
                    } else if (seekProgress < 90) {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(80);
                    } else {
                        PlaceholderFragment.this.mSnoozeMin.setProgress(100);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 0) {
                        PlaceholderFragment.this.min = 5;
                    } else if (20 == progress) {
                        PlaceholderFragment.this.min = 10;
                    } else if (40 == progress) {
                        PlaceholderFragment.this.min = 15;
                    } else if (60 == progress) {
                        PlaceholderFragment.this.min = 20;
                    } else if (80 == progress) {
                        PlaceholderFragment.this.min = 25;
                    } else if (100 == progress) {
                        PlaceholderFragment.this.min = 30;
                    }
                    Log.dRelease("settings", "mSnoozeMin = " + progress + ", min = " + PlaceholderFragment.this.min);
                    PlaceholderFragment.this.mSnoozeMin.setContentDescription(PlaceholderFragment.this.getResources().getQuantityString(R.plurals.setting_intervals, PlaceholderFragment.this.min, new Object[]{Integer.valueOf(PlaceholderFragment.this.min)}));
                }
            });
            listenSeekbarOnSnoozeTimers();
        }

        private void listenSeekbarOnSnoozeTimers() {
            this.mSnoozeTimers.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int seekProgress = seekBar.getProgress();
                    if (seekProgress < 15) {
                        PlaceholderFragment.this.mSnoozeTimers.setProgress(0);
                    } else if (seekProgress < 45) {
                        PlaceholderFragment.this.mSnoozeTimers.setProgress(30);
                    } else if (seekProgress < 75) {
                        PlaceholderFragment.this.mSnoozeTimers.setProgress(60);
                    } else {
                        PlaceholderFragment.this.mSnoozeTimers.setProgress(90);
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 0) {
                        PlaceholderFragment.this.num = 1;
                    } else if (30 == progress) {
                        PlaceholderFragment.this.num = 3;
                    } else if (60 == progress) {
                        PlaceholderFragment.this.num = 5;
                    } else if (90 == progress) {
                        PlaceholderFragment.this.num = 10;
                    }
                    Log.dRelease("settings", "mSnoozeTimers = " + progress + ", num = " + PlaceholderFragment.this.num);
                    PlaceholderFragment.this.mSnoozeTimers.setContentDescription(PlaceholderFragment.this.getResources().getQuantityString(R.plurals.setting_silence, PlaceholderFragment.this.num, new Object[]{Integer.valueOf(PlaceholderFragment.this.num)}));
                }
            });
        }

        private void setBellLengthStr(SharedPreferences preference) {
            int duration = preference.getInt("bell_duration", 5);
            this.mRingDuration.setSummary(getResources().getQuantityString(R.plurals.setting_ring_duration, duration, new Object[]{Integer.valueOf(duration)}));
        }

        private void dismissDialog() {
            if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
                this.mAlertDialog.dismiss();
                this.mAlertDialog = null;
            }
        }

        private void switchUpdate(boolean check, String id, String timezone) {
            Activity activity = getActivity();
            activity.setResult(-1);
            if (check) {
                ContentResolver resolver = activity.getContentResolver();
                Cursor cursor = resolver.query(LocationColumns.CONTENT_URI, new String[]{"homecity"}, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        if ("2".equals(cursor.getString(cursor.getColumnIndex("homecity")))) {
                            ContentValues values = new ContentValues();
                            values.put("sort_order", Integer.valueOf(9999));
                            values.put("homecity", Integer.valueOf(0));
                            resolver.update(LocationColumns.CONTENT_URI, values, "homecity = 2 OR sort_order = ?", new String[]{id});
                            cursor.close();
                            return;
                        }
                    }
                    cursor.close();
                }
                resolver.delete(LocationColumns.CONTENT_URI, "homecity = ?", new String[]{"1"});
            } else {
                updateTimeZone(check, id, timezone);
            }
        }

        private void updateTimeZone(boolean check, String id, String timezone) {
            Activity activity = getActivity();
            ContentValues values = new ContentValues();
            values.put("city_index", id);
            values.put("timezone", timezone);
            values.put("sort_order", Integer.valueOf(0));
            values.put("homecity", Integer.valueOf(1));
            ContentResolver resolver = activity.getContentResolver();
            Cursor cursor = resolver.query(LocationColumns.CONTENT_URI, new String[]{"city_index", "homecity"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String cityindex = cursor.getString(cursor.getColumnIndex("city_index"));
                    String homeCity = cursor.getString(cursor.getColumnIndex("homecity"));
                    ContentValues v;
                    if (!cityindex.equals(id)) {
                        resolver.delete(LocationColumns.CONTENT_URI, "homecity = 1 OR city_index = ?", new String[]{id});
                        v = new ContentValues();
                        v.put("sort_order", Integer.valueOf(9999));
                        v.put("homecity", Integer.valueOf(0));
                        resolver.update(LocationColumns.CONTENT_URI, v, "homecity = 2 OR sort_order = ?", new String[]{id});
                    } else if ("1".equals(homeCity)) {
                        resolver.delete(LocationColumns.CONTENT_URI, "homecity = 1 OR city_index = ?", new String[]{id});
                        resolver.insert(LocationColumns.CONTENT_URI, values);
                    } else if (!"2".equals(homeCity) || check) {
                        values.put("homecity", Integer.valueOf(2));
                        resolver.update(LocationColumns.CONTENT_URI, values, "homecity = 2 OR city_index = ?", new String[]{id});
                    } else {
                        v = new ContentValues();
                        v.put("sort_order", Integer.valueOf(9999));
                        v.put("homecity", Integer.valueOf(0));
                        resolver.update(LocationColumns.CONTENT_URI, v, "homecity = 2 OR sort_order = ?", new String[]{id});
                    }
                }
                cursor.close();
            }
            resolver.delete(LocationColumns.CONTENT_URI, "homecity = 1 OR city_index = ?", new String[]{id});
            resolver.insert(LocationColumns.CONTENT_URI, values);
        }

        public String buildGmtDisplayName(String id, String displayName, StringBuilder cityGmtName) {
            int mOffset = TimeZone.getTimeZone(id).getOffset(this.mTime);
            int p = Math.abs(mOffset);
            cityGmtName.append(displayName);
            cityGmtName.append(" (GMT");
            if (mOffset < 0) {
                cityGmtName.append('-');
            } else {
                cityGmtName.append('+');
            }
            cityGmtName.append(((long) p) / 3600000);
            cityGmtName.append(':');
            int min = (p / 60000) % 60;
            if (min < 10) {
                cityGmtName.append('0');
            }
            cityGmtName.append(min);
            cityGmtName.append(")");
            return cityGmtName.toString();
        }

        public void updatePrefAfterSelect(String index, String cityName) {
            String str = null;
            String[] projection = new String[]{"city_index"};
            Activity activity = getActivity();
            Cursor cursor = activity.getContentResolver().query(LocationColumns.CONTENT_URI, projection, "homecity = ?", new String[]{"1"}, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    str = cursor.getString(cursor.getColumnIndex("city_index"));
                }
                cursor.close();
            }
            if (!(str == null || str.equals(index))) {
                TimeZoneUtils.updatePreFromWorldPage(activity, str);
            }
            Map<String, String> map = new HashMap();
            map.put(index, cityName);
            TimeZoneUtils.saveTimeZoneMap(activity, map);
        }

        public void updateDataSource(Bundle bundle) {
            if (bundle != null) {
                String timezone = bundle.getString("id");
                String displayName = bundle.getString("name");
                String index = bundle.getString("unique_id");
                Activity activity = getActivity();
                if (!TextUtils.isEmpty(timezone) && !TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(index)) {
                    String cityName = TimeZoneUtils.getCityName(displayName);
                    updatePrefAfterSelect(index, cityName);
                    updateTimeZone(true, index, timezone);
                    this.mEditor.putString("home_city_timezone", timezone);
                    this.mEditor.putString("home_time_index", index);
                    this.mEditor.commit();
                    TimeZoneUtils.worldPageUpdate(activity);
                    this.mHomeTime.setSummary(buildGmtDisplayName(timezone, cityName, new StringBuilder()));
                    ClockReporter.reportEventContainMessage(getActivity(), 87, "timezone:" + timezone, 0);
                }
            }
        }

        protected boolean unableOpenHomeTime() {
            String id = this.mPreferences.getString("home_time_index", "");
            Cursor cursor = getActivity().getContentResolver().query(LocationColumns.CONTENT_URI, null, "homecity != 1 OR city_index != ?", new String[]{id}, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() >= 24) {
                        return true;
                    }
                    cursor.close();
                } finally {
                    cursor.close();
                }
            }
            return false;
        }

        private static void setModeRingerStreamAffectedSafty(Context context, int types) {
            if (CompatUtils.hasPermission(context, "android.permission.WRITE_SETTINGS")) {
                Log.iRelease("settings", "setModeRingerStreamAffectedSafty->has no WRITE_SETTINGS permission");
            }
            try {
                System.putInt(context.getContentResolver(), "mode_ringer_streams_affected", types);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            Log.i("settings", "onDestroy");
            dismissDialog();
            TimeZoneUtils.unRegisterUpdateUIBroadcast(this.mActivity, this.syncDatereceiver);
            if (this.mQueryRington != null) {
                this.mQueryRington.cancelOperation(1);
                this.mQueryRington.removeCallbacksAndMessages(null);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.mLinearLayout = (LinearLayout) findViewById(R.id.setting_layout);
        this.mFrameLayout = (FrameLayout) findViewById(R.id.container);
        if (AlarmsMainActivity.ismLockedEnter()) {
            sendBroadcastAsUser(new Intent("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD"), UserHandle.OWNER);
        }
        this.fragment = new PlaceholderFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, this.fragment, "settings").commit();
        }
        initActionBar();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(null);
        actionBar.setTitle(R.string.settings);
    }
}
