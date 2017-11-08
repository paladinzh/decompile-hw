package com.android.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.ListPopupWindow;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.TimeZone;

public class DateTimeSettingsSetupWizard extends Activity implements OnClickListener, OnItemClickListener, OnCheckedChangeListener, OnPreferenceStartFragmentCallback {
    private static final String TAG = DateTimeSettingsSetupWizard.class.getSimpleName();
    private CompoundButton mAutoDateTimeButton;
    private DatePicker mDatePicker;
    private InputMethodManager mInputMethodManager;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DateTimeSettingsSetupWizard.this.updateTimeAndDateDisplay();
        }
    };
    private TimeZone mSelectedTimeZone;
    private TimePicker mTimePicker;
    private BaseAdapter mTimeZoneAdapter;
    private Button mTimeZoneButton;
    private ListPopupWindow mTimeZonePopup;
    private boolean mUsingXLargeLayout;

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        setContentView(2130968730);
        if (findViewById(2131886475) == null) {
            z = false;
        }
        this.mUsingXLargeLayout = z;
        if (this.mUsingXLargeLayout) {
            initUiForXl();
        } else {
            findViewById(2131886371).setOnClickListener(this);
        }
        this.mTimeZoneAdapter = ZonePicker.constructTimezoneAdapter(this, 2130968731, 0);
        if (!this.mUsingXLargeLayout) {
            findViewById(2131886471).setSystemUiVisibility(4194304);
        }
    }

    public void initUiForXl() {
        boolean autoDateTimeEnabled;
        boolean z;
        boolean z2 = true;
        TimeZone tz = TimeZone.getDefault();
        this.mSelectedTimeZone = tz;
        this.mTimeZoneButton = (Button) findViewById(2131886475);
        this.mTimeZoneButton.setText(tz.getDisplayName());
        this.mTimeZoneButton.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent.hasExtra("extra_initial_auto_datetime_value")) {
            autoDateTimeEnabled = intent.getBooleanExtra("extra_initial_auto_datetime_value", false);
        } else {
            autoDateTimeEnabled = isAutoDateTimeEnabled();
        }
        this.mAutoDateTimeButton = (CompoundButton) findViewById(2131886477);
        this.mAutoDateTimeButton.setChecked(autoDateTimeEnabled);
        this.mAutoDateTimeButton.setOnCheckedChangeListener(this);
        this.mTimePicker = (TimePicker) findViewById(2131886482);
        TimePicker timePicker = this.mTimePicker;
        if (autoDateTimeEnabled) {
            z = false;
        } else {
            z = true;
        }
        timePicker.setEnabled(z);
        this.mDatePicker = (DatePicker) findViewById(2131886479);
        DatePicker datePicker = this.mDatePicker;
        if (autoDateTimeEnabled) {
            z2 = false;
        }
        datePicker.setEnabled(z2);
        this.mDatePicker.setCalendarViewShown(false);
        DateTimeSettings.configureDatePicker(this.mDatePicker);
        this.mInputMethodManager = (InputMethodManager) getSystemService("input_method");
        ((Button) findViewById(2131886371)).setOnClickListener(this);
        Button skipButton = (Button) findViewById(2131886925);
        if (skipButton != null) {
            skipButton.setOnClickListener(this);
        }
    }

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        registerReceiver(this.mIntentReceiver, filter, null, null);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mIntentReceiver);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case 2131886371:
                if (!(this.mSelectedTimeZone == null || TimeZone.getDefault().equals(this.mSelectedTimeZone))) {
                    ((AlarmManager) getSystemService("alarm")).setTimeZone(this.mSelectedTimeZone.getID());
                }
                if (this.mAutoDateTimeButton != null) {
                    Global.putInt(getContentResolver(), "auto_time", this.mAutoDateTimeButton.isChecked() ? 1 : 0);
                    if (!this.mAutoDateTimeButton.isChecked()) {
                        DateTimeSettings.setDate(this, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
                        DateTimeSettings.setTime(this, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
                        break;
                    }
                }
                break;
            case 2131886475:
                showTimezonePicker(2131886475);
                return;
            case 2131886925:
                break;
            default:
                Log.w(TAG, "onClick unknown id");
                return;
        }
        setResult(-1);
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        boolean z = true;
        boolean autoEnabled = isChecked;
        if (buttonView == this.mAutoDateTimeButton) {
            int i;
            boolean z2;
            ContentResolver contentResolver = getContentResolver();
            String str = "auto_time";
            if (isChecked) {
                i = 1;
            } else {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
            TimePicker timePicker = this.mTimePicker;
            if (isChecked) {
                z2 = false;
            } else {
                z2 = true;
            }
            timePicker.setEnabled(z2);
            DatePicker datePicker = this.mDatePicker;
            if (isChecked) {
                z = false;
            }
            datePicker.setEnabled(z);
        }
        if (isChecked) {
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                this.mInputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                focusedView.clearFocus();
            }
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimeZone tz = ZonePicker.obtainTimeZoneFromItem(parent.getItemAtPosition(position));
        if (this.mUsingXLargeLayout) {
            this.mSelectedTimeZone = tz;
            Calendar now = Calendar.getInstance(tz);
            if (this.mTimeZoneButton != null) {
                this.mTimeZoneButton.setText(tz.getDisplayName());
            }
            this.mDatePicker.updateDate(now.get(1), now.get(2), now.get(5));
            this.mTimePicker.setCurrentHour(Integer.valueOf(now.get(11)));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(now.get(12)));
        } else {
            ((AlarmManager) getSystemService("alarm")).setTimeZone(tz.getID());
            ((DateTimeSettings) getFragmentManager().findFragmentById(2131886473)).updateTimeAndDateDisplay(this);
        }
        this.mTimeZonePopup.dismiss();
    }

    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        showTimezonePicker(2131886472);
        return true;
    }

    private void showTimezonePicker(int anchorViewId) {
        View anchorView = findViewById(anchorViewId);
        if (anchorView == null) {
            Log.e(TAG, "Unable to find zone picker anchor view " + anchorViewId);
            return;
        }
        this.mTimeZonePopup = new ListPopupWindow(this, null);
        this.mTimeZonePopup.setWidth(anchorView.getWidth());
        this.mTimeZonePopup.setAnchorView(anchorView);
        this.mTimeZonePopup.setAdapter(this.mTimeZoneAdapter);
        this.mTimeZonePopup.setOnItemClickListener(this);
        this.mTimeZonePopup.setModal(true);
        this.mTimeZonePopup.show();
    }

    private boolean isAutoDateTimeEnabled() {
        boolean z = true;
        try {
            if (Global.getInt(getContentResolver(), "auto_time") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            Log.w(TAG, "isAutoDateTimeEnabled Settings not found", e);
            return true;
        }
    }

    private void updateTimeAndDateDisplay() {
        if (this.mUsingXLargeLayout) {
            Calendar now = Calendar.getInstance();
            this.mTimeZoneButton.setText(now.getTimeZone().getDisplayName());
            this.mDatePicker.updateDate(now.get(1), now.get(2), now.get(5));
            this.mTimePicker.setCurrentHour(Integer.valueOf(now.get(11)));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(now.get(12)));
        }
    }
}
